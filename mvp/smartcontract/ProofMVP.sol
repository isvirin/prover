pragma solidity ^0.4.11;

contract ProofMVP
{
    event SwypeCodeRequested(address indexed _who);
    event MediaHashSubmitted(address indexed _who, bytes32 indexed _publishedMediaHash, bytes32 _swypeCodeTransactionHash);
    event MessageSubmitted(address indexed _who, string _message);

    function requestSwypeCode() public
    {
        SwypeCodeRequested(msg.sender);
    }

    function submitMediaHash(bytes32 _publishedMediaHash, bytes32 _swypeCodeTransactionHash) public
    {
        MediaHashSubmitted(msg.sender, _publishedMediaHash, _swypeCodeTransactionHash);
    }

    function submitMessage(string _message) public
    {
        MessageSubmitted(msg.sender, _message);
    }
}
