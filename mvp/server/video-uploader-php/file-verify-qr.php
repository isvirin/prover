<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

DEFINE('SUBMIT_HASH', '0x708b34fe');

$videoFile = @$argv[1];

$mvpHelloInfo = json_decode(httpPost(MVP_CGI_BIN_URL . '/hello'), true);
if (!$mvpHelloInfo['contractAddress']) {
    echo 'Sorry, contract not available now';
    exit;
}
$contract = $mvpHelloInfo['contractAddress'];

$result = exec("searchqrcode $videoFile 2> /dev/null", $output, $return_code);

if ($return_code !== 0) {
    echo 'wrong searchqrcode input file';
    exit;
}

$qrcodeInfo = json_decode($result, true);

if (!$qrcodeInfo || !isset($qrcodeInfo['txhash']) || !isset($qrcodeInfo['blockhash'])) {
    echo 'wrong searchqrcode result';
    exit;
}

$gethClient = new JsonRpc\Client(GETH_NODE_URL);
$params = [
    $qrcodeInfo['txhash']
];

if ($gethClient->call('eth_getTransactionByHash', $params)) {
    if (substr($gethClient->result->blockHash, 0, 30) != $qrcodeInfo['blockhash']) {
        echo 'wrong block hash';
        exit;
    }
    if ($gethClient->result->to != $contract) {
        echo 'wrong contract';
        exit;
    }
    if (substr($gethClient->result->input, 0, 10) != SUBMIT_HASH) {
        echo 'wrong submit hash input';
        exit;
    }
    $inputStrLength = hexdec(substr($gethClient->result->input, 2 + (4 + 32) * 2, 64));
    $inputStrHex = substr($gethClient->result->input, 2 + (4 + 32 + 32) * 2, $inputStrLength * 2);
    $inputStr = hexToStr($inputStrHex);
    echo 'success! ' . $inputStr;
} else {
    echo 'Error:';
    var_dump($gethClient->error);
    exit;
}

