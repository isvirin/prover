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
    address public newOwner;

    function owned() payable {
        owner = msg.sender;
    }
    
    modifier onlyOwner {
        require(owner == msg.sender);
        _;
    }

    function changeOwner(address _owner) onlyOwner public {
        require(_owner != 0);
        newOwner = _owner;
    }
    
    function confirmOwner() public {
        require(newOwner == msg.sender);
        owner = newOwner;
        delete newOwner;
    }
}

/**
 * @title ERC20 interface
 * @dev see https://github.com/ethereum/EIPs/issues/20
 */
contract ERC20 {
    uint public totalSupply;
    function balanceOf(address who) constant returns (uint);
    function transfer(address to, uint value);
    function allowance(address owner, address spender) constant returns (uint);
    function transferFrom(address from, address to, uint value);
    function approve(address spender, uint value);
    event Approval(address indexed owner, address indexed spender, uint value);
    event Transfer(address indexed from, address indexed to, uint value);
}

contract ManualMigration is owned, ERC20 {
    mapping (address => uint) internal balances;
    address public migrationHost;

    function ManualMigration(address _migrationHost) payable owned() {
        migrationHost = _migrationHost;
    }

    function migrateManual(address _tokensHolder) onlyOwner {
        require(migrationHost != 0);
        uint tokens = ERC20(migrationHost).balanceOf(_tokensHolder);
        tokens = tokens * 125 / 100;
        balances[_tokensHolder] = tokens;
        totalSupply += tokens;
        Transfer(migrationHost, _tokensHolder, tokens);
    }
    
    function sealManualMigration() onlyOwner {
        delete migrationHost;
    }
}

/**
 * @title Crowdsale implementation
 */
contract Crowdsale is ManualMigration {
    uint    public etherPrice;
    address public crowdsaleOwner;
    uint    public totalLimitUSD;
    uint    public minimalSuccessUSD;
    uint    public collectedUSD;

    enum State { Disabled, PreICO, CompletePreICO, Crowdsale, Enabled, Migration }
    event NewState(State state);
    State   public state = State.Disabled;
    uint    public crowdsaleStartTime;
    uint    public crowdsaleFinishTime;

    modifier enabledState {
        require(state == State.Enabled);
        _;
    }

    modifier enabledOrMigrationState {
        require(state == State.Enabled || state == State.Migration);
        _;
    }

    struct Investor {
        uint amountTokens;
        uint amountWei;
    }
    mapping (address => Investor) public investors;
    mapping (uint => address)     public investorsIter;
    uint                          public numberOfInvestors;

    function Crowdsale(address _migrationHost)
        payable ManualMigration(_migrationHost) {
    }
    
    function () payable {
        require(state == State.PreICO || state == State.Crowdsale);
        require(now < crowdsaleFinishTime);
        uint valueWei = msg.value;
        uint valueUSD = valueWei * etherPrice / 1000000000000000000;
        if (collectedUSD + valueUSD > totalLimitUSD) { // don't need so much ether
            valueUSD = totalLimitUSD - collectedUSD;
            valueWei = valueUSD * 1000000000000000000 / etherPrice;
            if (!msg.sender.call.gas(3000000).value(msg.value - valueWei)()) throw;
            collectedUSD = totalLimitUSD; // to be sure!
        } else {
            collectedUSD += valueUSD;
        }
        uint tokensPerUSD = 100;
        if (state == State.PreICO) {
            if (now < crowdsaleStartTime + 1 days && valueUSD >= 50000) {
                tokensPerUSD = 150;
            } else {
                tokensPerUSD = 125;
            }
        } else if (state == State.Crowdsale) {
            if (now < crowdsaleStartTime + 1 days) {
                tokensPerUSD = 115;
            } else if (now < crowdsaleStartTime + 1 weeks) {
                tokensPerUSD = 110;
            }
        }
        uint tokens = tokensPerUSD * valueUSD;
        require(balances[msg.sender] + tokens > balances[msg.sender]); // overflow
        require(tokens > 0);
        Investor storage inv = investors[msg.sender];
        if (inv.amountWei == 0) { // new investor
            investorsIter[numberOfInvestors++] = msg.sender;
        }
        inv.amountTokens += tokens;
        inv.amountWei += valueWei;
        balances[msg.sender] += tokens;
        Transfer(this, msg.sender, tokens);
        totalSupply += tokens;
    }
    
    function startTokensSale(
            address _crowdsaleOwner,
            uint    _crowdsaleDurationDays,
            uint    _totalLimitUSD,
            uint    _minimalSuccessUSD,
            uint    _etherPrice) public onlyOwner {
        require(state == State.Disabled || state == State.CompletePreICO);
        crowdsaleStartTime = now;
        crowdsaleOwner = _crowdsaleOwner;
        etherPrice = _etherPrice;
        delete numberOfInvestors;
        delete collectedUSD;
        crowdsaleFinishTime = now + _crowdsaleDurationDays * 1 days;
        totalLimitUSD = _totalLimitUSD;
        minimalSuccessUSD = _minimalSuccessUSD;
        if (state == State.Disabled) {
            state = State.PreICO;
        } else {
            state = State.Crowdsale;
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
    
    function finishTokensSale(uint _investorsToProcess) public {
        require(state == State.PreICO || state == State.Crowdsale);
        require(now >= crowdsaleFinishTime || collectedUSD == totalLimitUSD);
        if (collectedUSD < minimalSuccessUSD) {
            // Investors can get their ether calling withdrawBack() function
            while (_investorsToProcess > 0 && numberOfInvestors > 0) {
                address addr = investorsIter[--numberOfInvestors];
                Investor memory inv = investors[addr];
                balances[addr] -= inv.amountTokens;
                totalSupply -= inv.amountTokens;
                Transfer(addr, this, inv.amountTokens);
                --_investorsToProcess;
                delete investorsIter[numberOfInvestors];
            }
            if (numberOfInvestors > 0) {
                return;
            }
            if (state == State.PreICO) {
                state = State.Disabled;
            } else {
                state = State.CompletePreICO;
            }
        } else {
            while (_investorsToProcess > 0 && numberOfInvestors > 0) {
                --numberOfInvestors;
                --_investorsToProcess;
                delete investors[investorsIter[numberOfInvestors]];
                delete investorsIter[numberOfInvestors];
            }
            if (numberOfInvestors > 0) {
                return;
            }
            if (state == State.PreICO) {
                if (!crowdsaleOwner.call.gas(3000000).value(this.balance)()) throw;
                state = State.CompletePreICO;
            } else {
                if (!crowdsaleOwner.call.gas(3000000).value(minimalSuccessUSD * 1000000000000000000 / etherPrice)()) throw;
                // Create additional tokens for owner (28% of complete totalSupply)
                uint tokens = totalSupply * 28 / 72;
                balances[owner] = tokens;
                totalSupply += tokens;
                Transfer(this, owner, tokens);
                state = State.Enabled;
            }
        }
        NewState(state);
    }
    
    // This function must be called by token holder in case of crowdsale failed
    function withdrawBack() public {
        require(state == State.Disabled || state == State.CompletePreICO);
        uint value = investors[msg.sender].amountWei;
        if (value > 0) {
            delete investors[msg.sender];
            if (!msg.sender.call.gas(3000000).value(value)()) throw;
        }
    }
}

/**
 * @title Token PROOF implementation
 */
contract Token is Crowdsale {
    
    string  public standard    = 'Token 0.1';
    string  public name        = 'PROOF';
    string  public symbol      = "PF";
    uint8   public decimals    = 0;

    modifier onlyTokenHolders {
        require(balances[msg.sender] != 0);
        _;
    }

    // Fix for the ERC20 short address attack
    modifier onlyPayloadSize(uint size) {
        require(msg.data.length >= size + 4);
        _;
    }

    mapping (address => mapping (address => uint)) public allowed;

    function Token(address _migrationHost)
        payable Crowdsale(_migrationHost) {}

    function balanceOf(address who) constant returns (uint) {
        return balances[who];
    }

    function transfer(address _to, uint _value)
        public enabledState onlyPayloadSize(2 * 32) {
        require(balances[msg.sender] >= _value);
        require(balances[_to] + _value >= balances[_to]); // overflow
        balances[msg.sender] -= _value;
        balances[_to] += _value;
        Transfer(msg.sender, _to, _value);
    }
    
    function transferFrom(address _from, address _to, uint _value)
        public enabledState onlyPayloadSize(3 * 32) {
        require(balances[_from] >= _value);
        require(balances[_to] + _value >= balances[_to]); // overflow
        require(allowed[_from][msg.sender] >= _value);
        balances[_from] -= _value;
        balances[_to] += _value;
        allowed[_from][msg.sender] -= _value;
        Transfer(_from, _to, _value);
    }

    function approve(address _spender, uint _value) public enabledState {
        allowed[msg.sender][_spender] = _value;
        Approval(msg.sender, _spender, _value);
    }

    function allowance(address _owner, address _spender) public constant enabledState
        returns (uint remaining) {
        return allowed[_owner][_spender];
    }
}

/**
 * @title Migration agent intefrace for possibility of moving tokens
 *        to another contract
 */
contract MigrationAgent {
    function migrateFrom(address _from, uint _value);
}

/**
 * @title Migration functionality for possibility of moving tokens
 *        to another contract
 */
contract TokenMigration is Token {
    
    address public migrationAgent;
    uint    public totalMigrated;

    event Migrate(address indexed from, address indexed to, uint value);

    function TokenMigration(address _migrationHost) payable Token(_migrationHost) {}

    // Migrate _value of tokens to the new token contract
    function migrate() external {
        require(state == State.Migration);
        require(migrationAgent != 0);
        uint value = balances[msg.sender];
        balances[msg.sender] -= value;
        Transfer(msg.sender, this, value);
        totalSupply -= value;
        totalMigrated += value;
        MigrationAgent(migrationAgent).migrateFrom(msg.sender, value);
        Migrate(msg.sender, migrationAgent, value);
    }

    function setMigrationAgent(address _agent) external onlyOwner {
        require(migrationAgent == 0);
        migrationAgent = _agent;
        state = State.Migration;
    }
}

contract ProofTeamVote is TokenMigration {

    function ProofTeamVote(address _migrationHost)
        payable TokenMigration(_migrationHost) {}

    event VotingStarted(uint weiReqFund);
    event Voted(address indexed voter, bool inSupport);
    event VotingFinished(bool inSupport);

    enum Vote { NoVote, VoteYea, VoteNay }

    uint public weiReqFund;
    uint public votingDeadline;
    uint public numberOfVotes;
    uint public yea;
    uint public nay;
    mapping (address => Vote) public votes;
    mapping (uint => address) public votesIter;

    function startVotingTeam(uint _weiReqFund) public enabledOrMigrationState onlyOwner {
        require(weiReqFund == 0 && _weiReqFund > 0 && _weiReqFund <= this.balance);
        weiReqFund = _weiReqFund;
        votingDeadline = now + 7 days;
        delete yea;
        delete nay;
        VotingStarted(_weiReqFund);
    }
    
    function votingInfoTeam() public constant enabledOrMigrationState
        returns(uint _weiReqFund, uint _timeToFinish) {
        _weiReqFund = weiReqFund;
        if (votingDeadline <= now) {
            _timeToFinish = 0;
        } else {
            _timeToFinish = votingDeadline - now;
        }
    }

    function voteTeam(bool _inSupport) public onlyTokenHolders enabledOrMigrationState
        returns (uint voteId) {
        require(votes[msg.sender] == Vote.NoVote);
        require(votingDeadline > now);
        voteId = numberOfVotes++;
        votesIter[voteId] = msg.sender;
        if (_inSupport) {
            votes[msg.sender] = Vote.VoteYea;
        } else {
            votes[msg.sender] = Vote.VoteNay;
        }
        Voted(msg.sender, _inSupport);
        return voteId;
    }

    function finishVotingTeam(uint _votesToProcess) public enabledOrMigrationState
        returns (bool _inSupport) {
        require(now >= votingDeadline);

        while (_votesToProcess > 0 && numberOfVotes > 0) {
            address voter = votesIter[--numberOfVotes];
            Vote v = votes[voter];
            uint voteWeight = balances[voter];
            if (v == Vote.VoteYea) {
                yea += voteWeight;
            } else if (v == Vote.VoteNay) {
                nay += voteWeight;
            }
            delete votes[voter];
            delete votesIter[numberOfVotes];
            --_votesToProcess;
        }
        if (numberOfVotes > 0) {
            _inSupport = false;
            return;
        }

        _inSupport = (yea > nay);
        uint weiForSend = weiReqFund;
        delete weiReqFund;
        delete votingDeadline;
        delete numberOfVotes;

        if (_inSupport) {
            if (migrationAgent == 0) {
                if (!owner.call.gas(3000000).value(weiForSend)()) throw;
            } else {
                if (!migrationAgent.call.gas(3000000).value(this.balance)()) throw;
            }
        }

        VotingFinished(_inSupport);
    }
}

contract ProofFund is ProofTeamVote {

    function ProofFund(address _migrationHost)
        payable ProofTeamVote(_migrationHost) {}

    event Deployed(address indexed projectOwner, uint proofReqFund, string urlInfo);
    event Voted(address indexed projectOwner, address indexed voter, bool inSupport);
    event VotingFinished(address indexed projectOwner, bool inSupport);

    struct Project {
        uint   proofReqFund;
        string urlInfo;
        uint   votingDeadline;
        uint   numberOfVotes;
        uint   yea;
        uint   nay;
        mapping (address => Vote) votes;
        mapping (uint => address) votesIter;
    }
    mapping (address => Project) public projects;

    function deployProject(uint _proofReqFund, string _urlInfo) public
        onlyTokenHolders enabledOrMigrationState {
        require(_proofReqFund > 0 && _proofReqFund <= balances[this]);
        require(_proofReqFund <= balances[msg.sender] * 1000);
        require(projects[msg.sender].proofReqFund == 0);
        projects[msg.sender].proofReqFund = _proofReqFund;
        projects[msg.sender].urlInfo = _urlInfo;
        projects[msg.sender].votingDeadline = now + 7 days;
        Deployed(msg.sender, _proofReqFund, _urlInfo);
    }
    
    function projectInfoPublic(address _projectOwner) enabledOrMigrationState constant public 
        returns(uint _proofReqFund, string _urlInfo, uint _timeToFinish) {
        _proofReqFund = projects[_projectOwner].proofReqFund;
        _urlInfo = projects[_projectOwner].urlInfo;
        if (projects[_projectOwner].votingDeadline <= now) {
            _timeToFinish = 0;
        } else {
            _timeToFinish = projects[_projectOwner].votingDeadline - now;
        }
    }

    function votePublic(address _projectOwner, bool _inSupport) public
        onlyTokenHolders enabledOrMigrationState returns (uint voteId) {
        Project storage p = projects[_projectOwner];
        require(p.proofReqFund > 0);
        require(p.votes[msg.sender] == Vote.NoVote);
        require(p.votingDeadline > now);
        voteId = p.numberOfVotes++;
        p.votesIter[voteId] = msg.sender;
        if (_inSupport) {
            p.votes[msg.sender] = Vote.VoteYea;
        } else {
            p.votes[msg.sender] = Vote.VoteNay;
        }
        Voted(_projectOwner, msg.sender, _inSupport); 
        return voteId;
    }

    function finishVotingPublic(address _projectOwner, uint _votesToProcess) public
        enabledOrMigrationState returns (bool _inSupport) {
        Project storage p = projects[_projectOwner];
        require(p.proofReqFund > 0);
        require(now >= p.votingDeadline && p.proofReqFund <= balances[this]);

        while (_votesToProcess > 0 && p.numberOfVotes > 0) {
            address voter = p.votesIter[--p.numberOfVotes];
            Vote v = p.votes[voter];
            uint voteWeight = balances[voter];
            if (v == Vote.VoteYea) {
                p.yea += voteWeight;
            } else if (v == Vote.VoteNay) {
                p.nay += voteWeight;
            }
            delete p.votesIter[p.numberOfVotes];
            delete p.votes[voter];
            --_votesToProcess;
        }
        if (p.numberOfVotes > 0) {
            _inSupport = false;
            return;
        }

        _inSupport = (p.yea > p.nay);

        uint proofReqFund = p.proofReqFund;
        delete projects[_projectOwner];

        if (_inSupport) {
            require(balances[_projectOwner] + proofReqFund >= balances[_projectOwner]); // overflow
            balances[this] -= proofReqFund;
            balances[_projectOwner] += proofReqFund;
            Transfer(this, _projectOwner, proofReqFund);
        }

        VotingFinished(_projectOwner, _inSupport);
    }
}

/**
 * @title Prover business-logic contract
 */
contract Proof is owned {
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

    uint    public priceInTokens;
    uint    public teamFee;
    address public proofFund;

    function Proof(address _proofFund) payable owned() {
        proofFund = _proofFund;
    }

    function setPrice(uint _priceInTokens) public onlyOwner {
        require(_priceInTokens >= 2);
        teamFee = _priceInTokens / 10;
        if (teamFee == 0) {
            teamFee = 1;
        }
        priceInTokens = _priceInTokens - teamFee;
    }

    function swypeCode() public returns (uint16 _swype) {
        bytes32 blockHash = block.blockhash(block.number - 1);
        bytes32 shaTemp = sha3(msg.sender, blockHash);
        _swype = uint16(uint256(shaTemp) % 65536);
        swypes[msg.sender] = Swype({swype: _swype, timestampSwype: now});
    }
    
    function setHash(uint16 _swype, bytes32 _hash) public {
        require(swypes[msg.sender].timestampSwype != 0);
        require(swypes[msg.sender].swype == _swype);
        ERC20(proofFund).transfer(owner, teamFee);
        ERC20(proofFund).transfer(proofFund, priceInTokens);
        videos[_hash] = Video({swype: _swype, timestampSwype:swypes[msg.sender].timestampSwype, 
            timestampHash: now, owner: msg.sender});
        delete swypes[msg.sender];
    }
}