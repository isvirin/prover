/*
This file is part of the PROOF MVP Contract.

The PROOF MVP Contract is free software: you can redistribute it and/or
modify it under the terms of the GNU lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The PROOF MVP Contract is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU lesser General Public License for more details.

You should have received a copy of the GNU lesser General Public License
along with the PROOF MVP Contract. If not, see <http://www.gnu.org/licenses/>.

@author Ilya Svirin <i.svirin@nordavind.ru>
*/

pragma solidity ^0.4.11;

contract ProofMVP {

    event SwypeGen(address _who, uint16 _swype);

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


    function ProofImpl() public payable {}

    function swypeCode(address _who) public {
        bytes32 blockHash = block.blockhash(block.number - 1);
        bytes32 shaTemp = keccak256(msg.sender, blockHash);
        uint16 newSwype = uint16(uint256(shaTemp) % 65536);
        swypes[_who] = Swype({swype: newSwype, timestampSwype: now});
        SwypeGen(_who, newSwype);
    }
    
    function setHash(address _who, uint16 _swype, bytes32 _hash) public {
        require(swypes[_who].timestampSwype != 0);
        require(swypes[_who].swype == _swype);
        videos[_hash] = Video({swype: _swype, timestampSwype:swypes[_who].timestampSwype, 
            timestampHash: now, owner: _who});
        delete swypes[_who];
    }
}