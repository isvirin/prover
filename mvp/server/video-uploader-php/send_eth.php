<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

saveClientInfo('sendeth');

define('SEND_ETH_DIR', __DIR__ . '/send_eth');
define('SEND_ETH_REPEAT_TIME_MIN', 86400);
define('SEND_ETH_VALUE', '0xB1A2BC2EC50000'); // 0.05 Eth ('0x' . dechex(0.05 * pow(10, 18)))
define('SEND_FROM_ADDRESS', '0xbbc887fdeeba38f1ebbdae6d07908a104e543da4');

function uploadResult($isSuccess, $message, $debug = false)
{
    return json_encode([
        'success' => $isSuccess,
        'message' => $message,
        'debug' => $debug
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
}

// для вывода результата строго в JSON делаем фокус
error_reporting(0); //show all errors

function worker()
{
    $ethAddress = @$_POST['ethAddress'];
    if (!preg_match("/^0x[a-fA-F0-9]{40}$/", $ethAddress)) {
        return [
            'isSuccess' => false,
            'message' => 'bad eth address'
        ];
    }
    $ethAddress = strtolower($ethAddress);

    $time = 0;
    if (is_file(SEND_ETH_DIR . "/$ethAddress")) {
        $time = (int)@file_get_contents(SEND_ETH_DIR . "/$ethAddress");
    }
    if (time() - $time < SEND_ETH_REPEAT_TIME_MIN) {
        return [
            'isSuccess' => false,
            'message' => 'ether requested too often'
        ];
    }
    file_put_contents(SEND_ETH_DIR . "/$ethAddress", time());

    $gethClient = new JsonRpc\Client(GETH_NODE_URL);

    $gethClient->call('personal_unlockAccount', [
        SEND_FROM_ADDRESS,
        SEND_ETH_PASSWORD,
        5
    ]);
    if (!$gethClient->result) {
        return [
            'isSuccess' => false,
            'message' => 'not authed'
        ];
    }

    $params = [[
        "from" => SEND_FROM_ADDRESS,
        "to" => $ethAddress,
        "value" => SEND_ETH_VALUE
    ]];

    if (!$gethClient->call('eth_sendTransaction', $params)) {
        return [
            'isSuccess' => false,
            'message' => 'send error: ' . $gethClient->error
        ];
    }

    return [
        'isSuccess' => true,
        'message' => $gethClient->result
    ];
}

$workerResult = worker();
die(uploadResult($workerResult['isSuccess'], $workerResult['message']));