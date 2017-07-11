/*
This file is part of the PROOF Contract.

The PROOF Contract is free software: you can redistribute it and/or
modify it under the terms of the GNU lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The PROOF Contract is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU lesser General Public License for more details.

You should have received a copy of the GNU lesser General Public License
along with the PROOF Contract. If not, see <http://www.gnu.org/licenses/>.
*/

pragma solidity ^0.4.0;

contract owned {

    address public owner;

    function owned() payable {
        owner = msg.sender;
    }
    
    modifier onlyOwner {
        require(owner == msg.sender);
        _;
    }

    function changeOwner(address _owner) onlyOwner public {
        require(_owner != 0);
        owner = _owner;
    }
}

contract Crowdsale is owned {
    
    uint256 public totalSupply;
    mapping (address => uint256) public balanceOf;

    uint public etherPrice;
    address public crowdsaleOwner;
    uint public totalLimitUSD;
    uint public minimalSuccessUSD;
    uint public collectedUSD;

    enum State { Disabled, PreICO, CompletePreICO, Crowdsale, Enabled, Migration }
    event NewState(State state);
    State public state = State.Disabled;
    uint  public crowdsaleStartTime;
    uint  public crowdsaleFinishTime;

    modifier enabledState {
        require(state == State.Enabled);
        _;
    }

    struct Investor {
        address investor;
        uint256 tokensAmount;
    }
    Investor[] public investorsList;
    mapping (address => uint) public investors;
    uint public numberOfInvestors;
    
    function () payable {
        require(state == State.PreICO || state == State.Crowdsale);
        uint256 tokensPerUSD = 0;
        if (state == State.PreICO) {
            tokensPerUSD = 125;
        } else if (state == State.Crowdsale) {
            if (now < crowdsaleStartTime + 1 days) {
                tokensPerUSD = 115;
            } else if (now < crowdsaleStartTime + 1 weeks) {
                tokensPerUSD = 110;
            } else {
                tokensPerUSD = 100;
            }
        }
        if (tokensPerUSD > 0) {
            uint valueETH = msg.value;
            uint valueUSD = valueETH * etherPrice / 1000000000000000000;
            if (collectedUSD + valueUSD > totalLimitUSD) { // don't need so much ether
                valueUSD = totalLimitUSD - collectedUSD;
                valueETH = valueUSD * 1000000000000000000 / etherPrice;
                msg.sender.transfer(msg.value - valueETH);
                collectedUSD = totalLimitUSD; // to be sure!
            }
            uint256 tokens = tokensPerUSD * valueUSD;
            require(balanceOf[msg.sender] + tokens > balanceOf[msg.sender]); // overflow
            require(tokens > 0);
            if (investors[msg.sender] == 0) {
                numberOfInvestors = investorsList.length++;
                investorsList[numberOfInvestors] = Investor({investor: msg.sender, tokensAmount: tokens});
            }
            investors[msg.sender] += valueETH;
            balanceOf[msg.sender] += tokens;
            totalSupply += tokens;
            collectedUSD += valueUSD;
        }
    }
    
    function startTokensSale(address _crowdsaleOwner, uint _etherPrice) public onlyOwner {
        require(state == State.Disabled || state == State.CompletePreICO);
        crowdsaleStartTime = now;
        crowdsaleOwner = _crowdsaleOwner;
        etherPrice = _etherPrice;
        delete numberOfInvestors;
        delete collectedUSD;
        if (state == State.Disabled) {
            crowdsaleFinishTime = now + 14 days;
            state = State.PreICO;
            totalLimitUSD = 300000;
            minimalSuccessUSD = 300000;
        } else {
            crowdsaleFinishTime = now + 30 days;
            state = State.Crowdsale;
            totalLimitUSD = 5200000;
            minimalSuccessUSD = 3600000;
        }
        NewState(state);
    }
    
    function timeToFinishTokensSale() public constant returns(uint t) {
        require(state == State.PreICO || state == State.Crowdsale);
        if (now > crowdsaleFinishTime) {
            t = 0;
        } else {
            t = crowdsaleFinishTime - now;
        }
    }
    
    function finishTokensSale() public onlyOwner {
        require(state == State.PreICO || state == State.Crowdsale);
        require(now >= crowdsaleFinishTime || collectedUSD >= minimalSuccessUSD);
        if (collectedUSD < minimalSuccessUSD) {
            // Investors can get their ether calling withdrawBack() function
            for (uint i = 0; i <  investorsList.length; ++i) {
                Investor inv = investorsList[i];
                investorsList[i].tokensAmount = 0;
                balanceOf[inv.investor] -= inv.tokensAmount;
                totalSupply -= inv.tokensAmount;
            }
            if (state == State.PreICO) {
                state = State.Disabled;
            } else {
                state = State.CompletePreICO;
            }
        } else {
            for (uint j = 0; j <  investorsList.length; ++j) {
                delete investors[investorsList[j].investor];
            }
            if (state == State.PreICO) {
                if (!crowdsaleOwner.send(this.balance)) throw;
                state = State.CompletePreICO;
            } else {
                if (!crowdsaleOwner.send(1500000 * 1000000000000000000 / etherPrice)) throw;
                // Create additional tokens for owner (28% of complete totalSupply)
                balanceOf[msg.sender] = totalSupply * 28 / 72;
                totalSupply += totalSupply * 28 / 72;
                state = State.Enabled;
            }
        }
        delete investorsList;
        NewState(state);
    }
    
    // This function must be called by token holder in case of crowdsale failed
    function withdrawBack() public {
        require(state == State.Disabled || state == State.CompletePreICO);
        uint value = investors[msg.sender];
        if (value > 0) {
            investors[msg.sender] = 0;
            msg.sender.send(value);
        }
    }
}

contract Token is Crowdsale {
    
    string  public standard    = 'Token 0.1';
    string  public name        = 'PROOF';
    string  public symbol      = "PF";
    uint8   public decimals    = 0;

    modifier onlyTokenHolders {
        require(balanceOf[msg.sender] != 0);
        _;
    }

    mapping (address => mapping (address => uint256)) public allowed;

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);

    function Token() payable Crowdsale() {}

    function transfer(address _to, uint256 _value) public enabledState {
        require(balanceOf[msg.sender] >= _value);
        require(balanceOf[_to] + _value >= balanceOf[_to]); // overflow
        balanceOf[msg.sender] -= _value;
        balanceOf[_to] += _value;
        Transfer(msg.sender, _to, _value);
    }
    
    function transferFrom(address _from, address _to, uint256 _value) public {
        require(balanceOf[_from] >= _value);
        require(balanceOf[_to] + _value >= balanceOf[_to]); // overflow
        require(allowed[_from][msg.sender] >= _value);
        balanceOf[_from] -= _value;
        balanceOf[_to] += _value;
        allowed[_from][msg.sender] -= _value;
        Transfer(_from, _to, _value);
    }

    function approve(address _spender, uint256 _value) public enabledState {
        allowed[msg.sender][_spender] = _value;
        Approval(msg.sender, _spender, _value);
    }

    function allowance(address _owner, address _spender) public constant enabledState
        returns (uint256 remaining) {
        return allowed[_owner][_spender];
    }
}

contract MigrationAgent {
    function migrateFrom(address _from, uint256 _value);
}

contract TokenMigration is Token {
    
    address public migrationAgent;
    uint256 public totalMigrated;

    event Migrate(address indexed from, address indexed to, uint256 value);

    function TokenMigration() payable Token() {}

    // Migrate _value of tokens to the new token contract
    function migrate(uint256 _value) external {
        require(state == State.Migration);
        require(migrationAgent != 0);
        require(_value != 0);
        require(_value <= balanceOf[msg.sender]);
        balanceOf[msg.sender] -= _value;
        totalSupply -= _value;
        totalMigrated += _value;
        MigrationAgent(migrationAgent).migrateFrom(msg.sender, _value);
        Migrate(msg.sender, migrationAgent, _value);
    }

    function setMigrationAgent(address _agent) external onlyOwner enabledState {
        require(migrationAgent == 0);
        migrationAgent = _agent;
        state = State.Migration;
    }
}

contract ProofTeamVote is TokenMigration {

    function ProofTeamVote() payable TokenMigration() {}

    event VotingStarted(uint weiReqFund);
    event Voted(address indexed voter, bool inSupport);
    event VotingFinished(bool inSupport);

    struct Vote {
        bool    inSupport;
        address voter;
    }

    uint   weiReqFund;
    uint   votingDeadline;
    Vote[] votes;
    mapping (address => bool) voted;
    uint   numberOfVotes;

    function startVoting(uint _weiReqFund) public enabledState onlyOwner {
        require(_weiReqFund > 0 && _weiReqFund <= this.balance);
        weiReqFund = _weiReqFund;
        votingDeadline = now + 7 days;
        VotingStarted(_weiReqFund);
    }
    
    function votingInfo() public constant enabledState 
        returns(uint _weiReqFund, uint _timeToFinish) {
        _weiReqFund = weiReqFund;
        if (votingDeadline <= now) {
            _timeToFinish = 0;
        } else {
            _timeToFinish = votingDeadline - now;
        }
    }

    function vote(bool _inSupport) public onlyTokenHolders enabledState
        returns (uint voteId) {
        require(voted[msg.sender] != true);
        require(votingDeadline > now);
        voteId = votes.length++;
        votes[voteId] = Vote({inSupport: _inSupport, voter: msg.sender});
        voted[msg.sender] = true;
        numberOfVotes = voteId + 1;
        Voted(msg.sender, _inSupport); 
        return voteId;
    }

    function finishVoting() public enabledState onlyOwner
        returns (bool _inSupport) {
        require(now >= votingDeadline && weiReqFund <= this.balance);

        uint yea = 0;
        uint nay = 0;

        for (uint i = 0; i <  votes.length; ++i) {
            Vote v = votes[i];
            voted[v.voter] = false;
            uint voteWeight = balanceOf[v.voter];
            if (v.inSupport) {
                yea += voteWeight;
            } else {
                nay += voteWeight;
            }
        }

        _inSupport = (yea > nay);

        if (_inSupport) {
            if (!owner.send(weiReqFund)) throw;
        }

        VotingFinished(_inSupport);
        weiReqFund = 0;
        votingDeadline = 0;
        delete votes;
        numberOfVotes = 0;
    }
}

contract ProofPublicVote is ProofTeamVote {

    function ProofPublicVote() payable ProofTeamVote() {}

    event Deployed(address indexed projectOwner, uint proofReqFund, string urlInfo);
    event Voted(address indexed projectOwner, address indexed voter, bool inSupport);
    event VotingFinished(address indexed projectOwner, bool inSupport);

    struct Project {
        uint   proofReqFund;
        string urlInfo;
        uint   votingDeadline;
        Vote[] votes;
        mapping (address => bool) voted;
        uint   numberOfVotes;
    }
    mapping (address => Project) public projects;

    function deployProject(uint _proofReqFund, string _urlInfo) public onlyTokenHolders enabledState {
        require(_proofReqFund > 0 && _proofReqFund <= balanceOf[this]);
        require(_proofReqFund <= balanceOf[msg.sender] * 1000);
        require(projects[msg.sender].proofReqFund == 0);
        projects[msg.sender].proofReqFund = _proofReqFund;
        projects[msg.sender].urlInfo = _urlInfo;
        projects[msg.sender].votingDeadline = now + 7 days;
        Deployed(msg.sender, _proofReqFund, _urlInfo);
    }
    
    function projectInfo(address _projectOwner) enabledState public 
        returns(uint _proofReqFund, string _urlInfo, uint _timeToFinish) {
        _proofReqFund = projects[_projectOwner].proofReqFund;
        _urlInfo = projects[_projectOwner].urlInfo;
        if (projects[_projectOwner].votingDeadline <= now) {
            _timeToFinish = 0;
        } else {
            _timeToFinish = projects[_projectOwner].votingDeadline - now;
        }
    }

    function vote(address _projectOwner, bool _inSupport) public onlyTokenHolders enabledState
        returns (uint voteId) {
        Project p = projects[_projectOwner];
        require(p.voted[msg.sender] != true);
        require(p.votingDeadline > now);
        voteId = p.votes.length++;
        p.votes[voteId] = Vote({inSupport: _inSupport, voter: msg.sender});
        p.voted[msg.sender] = true;
        p.numberOfVotes = voteId + 1;
        Voted(_projectOwner, msg.sender, _inSupport); 
        return voteId;
    }

    function finishVoting(address _projectOwner) public enabledState returns (bool _inSupport) {
        Project p = projects[_projectOwner];
        require(now >= p.votingDeadline && p.proofReqFund <= balanceOf[this]);

        uint yea = 0;
        uint nay = 0;

        for (uint i = 0; i <  p.votes.length; ++i) {
            Vote v = p.votes[i];
            uint voteWeight = balanceOf[v.voter];
            if (v.inSupport) {
                yea += voteWeight;
            } else {
                nay += voteWeight;
            }
        }

        _inSupport = (yea > nay);

        if (_inSupport) {
            transfer(_projectOwner, p.proofReqFund);
        }

        VotingFinished(_projectOwner, _inSupport);
        delete projects[_projectOwner];
    }
}

contract Proof is ProofPublicVote {

    struct Swype {
        uint16  swype;
        uint    timestampSwype;
    }
    
    struct Video {
        uint16  swype;
        uint    timestampSwype;
        uint    timestampHash;
        address owner;
    }

    mapping (address => Swype) public swypes;
    mapping (bytes32 => Video) public videos;

    uint priceInTokens;
    uint teamFee;

    function Proof() payable ProofPublicVote() {}

    function setPrice(uint _priceInTokens) public onlyOwner {
        require(_priceInTokens >= 2);
        teamFee = _priceInTokens / 10;
        if (teamFee == 0) {
            teamFee = 1;
        }
        priceInTokens = _priceInTokens - teamFee;
    }

    function swypeCode() public enabledState returns (uint16 _swype) {
        bytes32 blockHash = block.blockhash(block.number - 1);
        bytes32 shaTemp = sha3(msg.sender, blockHash);
        _swype = uint16(uint256(shaTemp) % 65536);
        swypes[msg.sender] = Swype({swype: _swype, timestampSwype: now});
    }
    
    function setHash(uint16 _swype, bytes32 _hash) public enabledState {
        require(balanceOf[msg.sender] >= priceInTokens);
        require(swypes[msg.sender].timestampSwype != 0);
        require(swypes[msg.sender].swype == _swype);
        transfer(owner, teamFee);
        transfer(this, priceInTokens);
        videos[_hash] = Video({swype: _swype, timestampSwype:swypes[msg.sender].timestampSwype, 
            timestampHash: now, owner: msg.sender});
        delete swypes[msg.sender];
    }
}