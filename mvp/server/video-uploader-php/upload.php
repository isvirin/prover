<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

define('GETLOGS_FILE_EVENT_ID', '0x461afacbe8920fcf3516d8b18e2634291cc96d0151ab7d324cca32fb77c44986');
define('USER_ADDRESS_FILTER', null);
define('EXAMPLE_FILE_HASH', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855');
define('TRANSACTIONBYHASH_CORRECT_INPUT', '0x74305b38');

$isSuccess = false;
$error = 'error text';
$transactions = [];

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
            GETLOGS_FILE_EVENT_ID,
            USER_ADDRESS_FILTER,
            '0x' . $hash
        ]
    ]];

    if ($gethClient->call('eth_getLogs', $params)) {
        $isSuccess = true;
        $eth_getLogs_result = $gethClient->result;
        foreach ($eth_getLogs_result as $transaction) {
            /* EXAMPLE transaction
            object(stdClass)#5 (9) {
              ["address"]=> string(42) "0x675dfc2a32683bc4287ca6376a9613e0c68037fa"
              ["topics"]=> array(3) {
                [0]=> string(66) "0x461afacbe8920fcf3516d8b18e2634291cc96d0151ab7d324cca32fb77c44986"
                [1]=> string(66) "0x00000000000000000000000042e1e53a644e3f8d5dc606c5104f6666163f2c76"
                [2]=> string(66) "0xe3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
              }
              ["data"]=> string(66) "0xc208cea8d09f1bf4d8625e653d4b5e387dabbb9a565c65f97a0698cd69168379"
              ["blockNumber"]=> string(8) "0x1f4c1e"
              ["transactionHash"]=> string(66) "0xe43e32f2866d5cc53b4f24996d62d1dde11698d1b7867c4950cb6b5efd8de4a0"
              ["transactionIndex"]=> string(3) "0x1"
              ["blockHash"]=> string(66) "0xcb127b04b583a70919e1550fb49139586a5b6d02f0156f4464878201c3106989"
              ["logIndex"]=> string(3) "0x0"
              ["removed"]=> bool(false)
            }
            */
            $data = $transaction->data;
            $senderAddress = $transaction->topics[1];
            $validated = false;

            if ($gethClient->call('eth_getTransactionByHash', [
                $data
            ])) {
                $transactionDetails = $gethClient->result;
                /* EXAMPLE $transactionDetails
                object(stdClass)#6 (14) {
                  ["blockHash"]=> string(66) "0x228fa0d54cc00dba260de045bddf5aae84c7e11f27a3f455959b73b257d299b4"
                  ["blockNumber"]=> string(8) "0x1f4b86"
                  ["from"]=> string(42) "0x42e1e53a644e3f8d5dc606c5104f6666163f2c76"
                  ["gas"]=> string(7) "0xf4240"
                  ["gasPrice"]=> string(10) "0xee6b2800"
                  ["hash"]=> string(66) "0xc208cea8d09f1bf4d8625e653d4b5e387dabbb9a565c65f97a0698cd69168379"
                  ["input"]=> string(10) "0x74305b38"
                  ["nonce"]=> string(3) "0x3"
                  ["to"]=> string(42) "0x675dfc2a32683bc4287ca6376a9613e0c68037fa"
                  ["transactionIndex"]=> string(3) "0x3"
                  ["value"]=> string(3) "0x0"
                  ["v"]=> string(4) "0x1b"
                  ["r"]=> string(66) "0x4e0cc00f7e782f1b45f9b53a810e211f82028288ccc35bd8957c11de958aa7d4"
                  ["s"]=> string(65) "0x12f49c4ac822f7914a6fcdcca9d44e73fdfd4f5dfbab3e36abbdb0bca5c784c"
                }
                */
                if (
                    preg_replace('/0x[0]*(.*)/', '$1', $transactionDetails->from) ===
                    preg_replace('/0x[0]*(.*)/', '$1', $senderAddress)
                ) {
                    if ($transactionDetails->input === TRANSACTIONBYHASH_CORRECT_INPUT) {
                        if ($transactionDetails->blockHash) {
                            $validated = true;
                            // TODO: для окончательного подтверждения мы должны еще прогнать видео через утилитку
                        }
                    }
                }
            }

            $transactions[] = [
                'senderAddress' => $senderAddress,
                'validated' => $validated
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