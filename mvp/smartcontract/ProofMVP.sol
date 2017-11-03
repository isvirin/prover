pragma solidity ^0.4.11;

contract ProofMVP
{
    event SwypeCodeCommited(address _who, bytes32 _referenceBlockHash);
    event MediaHashCommited(address indexed _who, bytes32 indexed _publishedMediaHash, bytes32 _swypeCodeTransactionHash);

    function commitSwypeCode(bytes32 _referenceBlockHash) public
    {
        SwypeCodeCommited(msg.sender, _referenceBlockHash);
    }

    function commitMediaHash(bytes32 _publishedMediaHash, bytes32 _swypeCodeTransactionHash) public
    {
        MediaHashCommited(msg.sender, _publishedMediaHash, _swypeCodeTransactionHash);
    }
}
