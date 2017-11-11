pragma solidity ^0.4.11;

contract ProofMVP
{
    event SwypeCodeRequested(address indexed _who);
    event MediaHashSubmitted(address indexed _who, bytes32 indexed _publishedMediaHash, bytes32 _swypeCodeTransactionHash);

    function requestSwypeCode() public
    {
        SwypeCodeRequested(msg.sender);
    }

    function submitMediaHash(bytes32 _publishedMediaHash, bytes32 _swypeCodeTransactionHash) public
    {
        MediaHashSubmitted(msg.sender, _publishedMediaHash, _swypeCodeTransactionHash);
    }
}
