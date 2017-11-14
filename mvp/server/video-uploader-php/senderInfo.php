<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

define('GETLOGS_SENDER_EVENT_ID', '0x4328c421156f7877c4adccdc8c132d0678b25caeec5e719aeaa881942f5aa2d2');
define('EXAMPLE_SENDER_ADDRESS', '00000000000000000000000042e1e53a644e3f8d5dc606c5104f6666163f2c76');

$isSuccess = false;
$error = 'error text';
$transactions = [];

$mvpHelloInfo = json_decode(httpPost(MVP_CGI_BIN_URL . '/hello'), true);
if (!$mvpHelloInfo['contractAddress']) {
    error_log('no contractAddress');
    $error = 'Sorry, contract not available now';
} else {
    $gethClient = new JsonRpc\Client(GETH_NODE_URL);
    $params = [[
        "fromBlock" => "0x1",
        "toBlock" => "latest",
        "address" => $mvpHelloInfo['contractAddress'],
        "topics" => [
            GETLOGS_SENDER_EVENT_ID,
            '0x' . $_POST['senderAddress']
        ]
    ]];

    if ($gethClient->call('eth_getLogs', $params)) {
        $isSuccess = true;
        $eth_getLogs_result = $gethClient->result;
        foreach ($eth_getLogs_result as $transaction) {
            /* EXAMPLE transaction
            object(stdClass)#5 (9) {
              ["address"]=>
              string(42) "0x675dfc2a32683bc4287ca6376a9613e0c68037fa"
              ["topics"]=>
              array(2) {
                [0]=>
                string(66) "0x4328c421156f7877c4adccdc8c132d0678b25caeec5e719aeaa881942f5aa2d2"
                [1]=>
                string(66) "0x00000000000000000000000042e1e53a644e3f8d5dc606c5104f6666163f2c76"
              }
              ["data"]=>
              string(2) "0x"
              ["blockNumber"]=>
              string(8) "0x1f4b86"
              ["transactionHash"]=>
              string(66) "0xc208cea8d09f1bf4d8625e653d4b5e387dabbb9a565c65f97a0698cd69168379"
              ["transactionIndex"]=>
              string(3) "0x3"
              ["blockHash"]=>
              string(66) "0x228fa0d54cc00dba260de045bddf5aae84c7e11f27a3f455959b73b257d299b4"
              ["logIndex"]=>
              string(3) "0x3"
              ["removed"]=>
              bool(false)
            }
            */
            $blockNumber = $transaction->blockNumber;
            $transactions[] = [
                'blockNumber' => $blockNumber
            ];
        }
    } else {
        $error = $gethClient->error;
    }
}

die(json_encode([
    'success' => $isSuccess,
    'transactions' => $transactions,
    'error' => $error
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));