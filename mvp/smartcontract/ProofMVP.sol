pragma solidity ^0.4.11;

contract ProofMVP
{
    event SwypeCodeCommited(address indexed _who, bytes32 _referenceBlockHash);
    event MediaHashCommited(address indexed _who, bytes32 _publishedMediaHash);

    function commitSwypeCode(bytes32 _referenceBlockHash) public
    {
        SwypeCodeCommited(msg.sender, _referenceBlockHash);
    }

    function commitMediaHash(bytes32 _publishedMediaHash) public
    {
        MediaHashCommited(msg.sender, _publishedMediaHash);
    }
}
