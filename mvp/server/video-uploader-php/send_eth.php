<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

define('SEND_FROM_ADDRESS', '0xbbc887fdeeba38f1ebbdae6d07908a104e543da4');

function uploadResult($isSuccess, $error, $debug = false)
{
    return json_encode([
        'success' => $isSuccess,
        'error' => $error,
        'debug' => $debug
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
}

// для вывода результата строго в JSON делаем фокус
error_reporting(0); //show all errors
register_shutdown_function(function () {
    $lastError = error_get_last();
    if ($lastError) {
        $error = "Server internal error: {$lastError['message']} ({$lastError['line']})";
        die(uploadResult(false, $error));
    }
});

function worker()
{
    $ethAddress = @$_POST['ethAddress'];
    if (!preg_match("/^0x[a-fA-F0-9]{40}$/", $ethAddress)) {
        return [
            'isSuccess' => false,
            'error' => 'bad eth address'
        ];
    }

    $gethClient = new JsonRpc\Client(GETH_NODE_URL);

    $gethClient->call('personal_unlockAccount', [
        SEND_FROM_ADDRESS,
        SEND_ETH_PASSWORD,
        5
    ]);
    if (!$gethClient->result) {
        return [
            'isSuccess' => false,
            'error' => 'not authed'
        ];
    }

    $params = [[
        "from" => SEND_FROM_ADDRESS,
        "to" => $ethAddress,
        "value" => '0x' . dechex(0.05)
    ]];

    if (!$gethClient->call('eth_sendTransaction', $params)) {
        return [
            'isSuccess' => false,
            'error' => 'send error: ' . $gethClient->error
        ];
    }

    return [
        'isSuccess' => true,
        'error' => ''
    ];
}

$workerResult = worker();
die(uploadResult($workerResult['isSuccess'], $workerResult['error']));