<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

define('EVENT_ID', '0x461afacbe8920fcf3516d8b18e2634291cc96d0151ab7d324cca32fb77c44986');
define('USER_ADDRESS_FILTER', null);
define('EXAMPLE_FILE_HASH', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855');

$isSuccess = false;
$error = 'error text';
$data = '';

$mvpHelloInfo = json_decode(httpPost(MVP_CGI_BIN_URL . '/hello'), true);
if (!$mvpHelloInfo['contractAddress']) {
    error_log('no contractAddress');
    $error = 'Sorry, contract not available now';
} else if (!empty($_FILES['file'])) {
    $hash = hash_file('sha256', $_FILES['file']['tmp_name']);
    $gethClient = new JsonRpc\Client(GETH_NODE_URL);
    $params = [[
        "fromBlock" => "0x1",
        "toBlock" => "latest",
        "address" => $mvpHelloInfo['contractAddress'],
        "topics" => [
            EVENT_ID,
            USER_ADDRESS_FILTER,
            "0x$hash"
        ]
    ]];

    if ($gethClient->call('eth_getLogs', $params)) {
        $isSuccess = true;
        $data = $gethClient->result;
    } else {
        $error = $gethClient->error;
    }
}

die(json_encode([
    'success' => $isSuccess,
    'data' => $data,
    'error' => $error
], JSON_UNESCAPED_UNICODE));