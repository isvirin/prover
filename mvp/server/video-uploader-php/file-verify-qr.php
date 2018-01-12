<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

DEFINE('SUBMIT_HASH', '0x708b34fe');

function uploadResult($isSuccess, $typeText, $hash, $error, $debug = false)
{
    return json_encode([
        'success' => $isSuccess,
        'typeText' => $typeText,
        'hash' => $hash,
        'error' => $error,
        'debug' => $debug
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
}

/**
 * @param string $file
 * @return array
 */
function worker($file)
{
    $mvpHelloInfo = json_decode(httpPost(MVP_CGI_BIN_URL . '/hello'), true);
    if (!$mvpHelloInfo['contractAddress']) {
        return [
            'isSuccess' => false,
            'typeText' => '',
            'hash' => '',
            'error' => 'Sorry, contract not available now'
        ];
    }
    $contract = $mvpHelloInfo['contractAddress'];

    $hash = hash_file('sha256', $file);
    $result = exec("searchqrcode $file 2> /dev/null", $output, $return_code);

    if ($return_code !== 0) {
        return [
            'isSuccess' => false,
            'typeText' => '',
            'hash' => '0x' . $hash,
            'error' => 'wrong searchqrcode input file'
        ];
    }

    $qrcodeInfo = json_decode($result, true);

    if (!$qrcodeInfo || !isset($qrcodeInfo['txhash']) || !isset($qrcodeInfo['blockhash'])) {
        return [
            'isSuccess' => false,
            'typeText' => '',
            'hash' => '0x' . $hash,
            'error' => 'wrong searchqrcode result'
        ];
    }

    $gethClient = new JsonRpc\Client(GETH_NODE_URL);
    $params = [
        $qrcodeInfo['txhash']
    ];
    if ($gethClient->call('eth_getTransactionByHash', $params)) {
        if (substr($gethClient->result->blockHash, 0, 30) != $qrcodeInfo['blockhash']) {
            return [
                'isSuccess' => false,
                'typeText' => '',
                'hash' => '0x' . $hash,
                'error' => 'wrong block hash'
            ];
        }
        if ($gethClient->result->to != $contract) {
            return [
                'isSuccess' => false,
                'typeText' => '',
                'hash' => '0x' . $hash,
                'error' => 'wrong contract'
            ];
        }
        if (substr($gethClient->result->input, 0, 10) != SUBMIT_HASH) {
            return [
                'isSuccess' => false,
                'typeText' => '',
                'hash' => '0x' . $hash,
                'error' => 'wrong submit hash input'
            ];
        }
        $inputStrLength = hexdec(substr($gethClient->result->input, 2 + (4 + 32) * 2, 64));
        $inputStrHex = substr($gethClient->result->input, 2 + (4 + 32 + 32) * 2, $inputStrLength * 2);
        $inputStr = hexToStr($inputStrHex);
        return [
            'isSuccess' => true,
            'typeText' => $inputStr,
            'hash' => '0x' . $hash,
            'error' => ''
        ];
    } else {
        return [
            'isSuccess' => false,
            'typeText' => '',
            'hash' => '0x' . $hash,
            'error' => $gethClient->error
        ];
    }
}

$file = '';
if (!empty($_FILES['file'])) {
    $file = $_FILES['file']['tmp_name'];
} else if (isset($argv[1])) {
    $file = $argv[1];
}

$workerResult = worker($file);
die(uploadResult($workerResult['isSuccess'], $workerResult['typeText'], $workerResult['hash'], $workerResult['error']));


