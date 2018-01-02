<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

DEFINE('CONTRACT', '0x8a45bbbbd8ea55350a62aa5c69826227a18151cd');
DEFINE('SUBMIT_HASH', '0x708b34fe');

$videoFile = @$argv[1];

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
    if ($gethClient->result->to != CONTRACT) {
        echo 'wrong contract';
        exit;
    }
    if (substr($gethClient->result->input, 0, 10) != SUBMIT_HASH) {
        echo 'wrong submit hash input';
        exit;
    }
    echo 'success!';
} else {
    echo 'Error:';
    var_dump($gethClient->error);
    exit;
}

