<?php
require('utils.php');
require('generation-pdf.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}

saveClientInfo('upload');

define('GETLOGS_FILE_EVENT_ID', '0x461afacbe8920fcf3516d8b18e2634291cc96d0151ab7d324cca32fb77c44986');
define('USER_ADDRESS_FILTER', null);
define('EXAMPLE_FILE_HASH', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855');
define('TRANSACTIONBYHASH_CORRECT_INPUT', '0x74305b38');

function uploadResult($isSuccess, $fileName, $transactions, $hash, $error, $debug = false)
{
    return json_encode([
        'fileName' => '/pdf/' . $fileName . '.pdf',
        'success' => $isSuccess,
        'transactions' => $transactions,
        'hash' => $hash,
        'error' => $error,
        'debug' => $debug
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
}

// для вывода результата строго в JSON делаем фокус
error_reporting(0); //show all errors

function callAnalyticProgramm($file, $blockHash, $txHash, $user)
{
    $validated = false;
    $swype = 0;
    $beginSwypeTime = 0;
    $endSwypeTime = 0;

    $cmd = '';
    if ($txHash) {
        $cmd = "analyzefile $file --txhash $txHash --blockhash $blockHash 2> /dev/null";
    } else if ($user) {
        $cmd = "analyzefile $file --user $user --blockhash $blockHash 2> /dev/null";
    }

    $resultJson = [];
    $return_code = 999;
    if ($cmd) {
        $resultJson = exec($cmd, $output, $return_code);
    }

    if ($return_code === 0) {
        $result = @json_decode($resultJson, true);
        if ($result && isset($result['result'])) {
            $swype = @$result['swype-code'];
            $beginSwypeTime = @$result['result']['time-begin'];
            $endSwypeTime = @$result['result']['time-end'];
        }
    }
    return [
        'validated' => $validated,
        'swype' => $swype,
        'beginSwypeTime' => $beginSwypeTime,
        'endSwypeTime' => $endSwypeTime,
        'cmd' => $cmd
    ];
}

/**
 * @param JsonRpc\Client $gethClient
 * @param string $hash
 * @return array
 */
function getBlockByHash(&$gethClient, $hash)
{
    $block = [];
    if ($gethClient->call('eth_getBlockByHash', [
        $hash,
        false
    ])) {
        /* Example object(stdClass)
        difficulty: "0x99379ead"
        extraData: "0xd783010700846765746887676f312e372e34856c696e7578"
        gasLimit: "0x665333"
        gasUsed: "0x3aa59"
        hash: "0xcb127b04b583a70919e1550fb49139586a5b6d02f0156f4464878201c3106989"
        logsBloom: "0x00000000000080000000000000000000000000080000000000000000400000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004000020000000000000000000000000000000000000000800000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000800000000002000000000000100000000000000000000000000000"
        miner: "0x8c60d40a2e848251d139fc2b0b6b770bb3351ffd"
        mixHash: "0xfc438bb77786f3f5921343ecfc227a80461a8c9e6c5831ba5f3ceca8a7d3ab4b"
        nonce: "0x4fa8ce0eeb1c72cb"
        number: "0x1f4c1e"
        parentHash: "0x2b4bc64b05e67c5dedf646e3fa6e685175a93780af6b7dd138759d5e234c5ddf"
        receiptsRoot: "0x8c1f2693dd7cc19ea03235c1cd6312ce076101088db6b64814128009e1d076f6"
        sha3Uncles: "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347"
        size: "0x773"
        stateRoot: "0x991d6513378d8517860a3e06768292d99954d60bfebb06c9ff58e6851918fe47"
        timestamp: "0x5a07199a"
        totalDifficulty: "0x17b50790497fd8"
        transactions: (11) ["0x9ec75201bcd7dbea8898f82b7ba1bfb18c5d89e9b64feeee602060ef60da008b", "0xe43e32f2866d5cc53b4f24996d62d1dde11698d1b7867c4950cb6b5efd8de4a0", "0xb078678716ddf8a8873d64d3771ec3584bb29f59c56a6efc5019988b7733f8df", "0xfadd9dd5cef89da914bd0a02fa18dd2f1ac28b73d469088b503e8c97b3f7e8c4", "0x73dffe907111577b98b16733b51e0a2f350f67905c14d3eb30f2f3651f083b77", "0x60717d5fc26db71b6817b6db5a1bd112b0eada77eaefd1c2e5b94d401401b98b", "0xbb42cc66fe2d4ddcdf3bc2f3e5e18f2bd7c587c979bf75055312a1d21ade4bf3", "0x9381b9c011083a7e8030e2871fc13b2eac3d7bf51525227aa4f637db1a44f8db", "0x9a7c6953a42de9f6b6b6cf4a7d5834587a3001a8d21c3777eb49633d24b442dc", "0xd05f9167ba17253b1972c1bc5677d908a11586a9f1b0d26f77d4d7d9aaf77810", "0x509b34abb056cbde1244eec58e094511cecd48c14aac3594d5f4031001b0bfd4"]
        transactionsRoot: "0x0eebcecdbb90d7917d190a5491006e5bad2b465cd61e789e1ba4353950d407b5"
        uncles: []
         */
        $block = $gethClient->result;
    }
    return $block;
}

/**
 * @param string $file
 * @param string $fileName
 * @return array
 */
function worker($file, $fileName)
{
    $isSuccess = false;
    $transactions = [];
    $hash = '';
    $error = '';

    $mvpHelloInfo = json_decode(httpPost(MVP_CGI_BIN_URL . '/hello'), true);
    if (!$mvpHelloInfo['contractAddress']) {
        error_log('no contractAddress');
        $error = 'Sorry, contract not available now';
    } else if (is_file($file)) {
        $hash = hash_file('sha256', $file);
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
            $eth_getLogs_result = $gethClient->result; // ищем верхнии транзакции
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
                $data = $transaction->data; // хэш нижней транзакции
                $senderAddress = $transaction->topics[1]; // адрес пользователя
                $validated = false;
                $swype = '';
                $beginSwypeTime = 0;
                $endSwypeTime = 0;
                $transaction1_details = json_decode(json_encode($transaction));
                $transaction2_details = [];

                $submitMediaHash_block = getBlockByHash($gethClient, $transaction->blockHash);
                $requestSwypeCode_block = [];

                $call = '';
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
                    $requestSwypeCode_block = getBlockByHash($gethClient, $transactionDetails->blockHash);
                    if (
                        preg_replace('/0x[0]*(.*)/', '$1', $transactionDetails->from) ===
                        preg_replace('/0x[0]*(.*)/', '$1', $senderAddress)
                    ) {
                        if ($transactionDetails->input === TRANSACTIONBYHASH_CORRECT_INPUT) {
                            if ($transactionDetails->blockHash) {
                                $transaction2_details = json_decode(json_encode($requestSwypeCode_block));
                                $analyticResult = callAnalyticProgramm($file, $transactionDetails->blockHash, $transactionDetails->hash, '');
                                $call = $analyticResult['cmd'];
                                $validated = $analyticResult['validated'];
                                $swype = $analyticResult['swype'];
                                $beginSwypeTime = $analyticResult['beginSwypeTime'];
                                $endSwypeTime = $analyticResult['endSwypeTime'];
                                if ($beginSwypeTime && $endSwypeTime) {
                                    $isSuccess = true;
                                }
                            }
                        }
                    }
                }

                // try fast mode
                if (!$transaction2_details) {
                    $fastBlock = getBlockByHash($gethClient, $data);
                    if ($fastBlock) {
                        $requestSwypeCode_block = $fastBlock;
                        $user = preg_replace('/(0x)[0]*(.*)/', '$1$2', $senderAddress);
                        $analyticResult = callAnalyticProgramm($file, $data, '', $user);
                        $call = $analyticResult['cmd'];
                        $validated = $analyticResult['validated'];
                        $swype = $analyticResult['swype'];
                        $beginSwypeTime = $analyticResult['beginSwypeTime'];
                        $endSwypeTime = $analyticResult['endSwypeTime'];
                        if ($beginSwypeTime && $endSwypeTime) {
                            $isSuccess = true;
                        }
                    }
                }

                $transactions[] = [
                    'transaction1_details' => $transaction1_details,
                    'transaction2_details' => $transaction2_details,
                    'senderAddress' => $senderAddress,
                    'validated' => $validated,
                    'call' => $call,
                    'submitMediaHash_block' => $submitMediaHash_block,
                    'requestSwypeCode_block' => $requestSwypeCode_block,
                    'swype' => $swype,
                    'beginSwypeTime' => $beginSwypeTime,
                    'endSwypeTime' => $endSwypeTime
                ];

                break; // будем проверять только первую найденную транзакцию
            }
        } else {
            $error = $gethClient->error;
        }
    }

    // todo: нужно явно определить $swype, $requestSwypeCode_block->timestamp и $submitMediaHash_block->timestamp, потому что они могут оказаться пустыми
    generationPdf($fileName, '0x' . $hash, $swype, $requestSwypeCode_block->timestamp, $submitMediaHash_block->timestamp);
    return [
        'fileName' => $fileName,
        'isSuccess' => $isSuccess,
        'transactions' => $transactions,
        'hash' => '0x' . $hash,
        'error' => $error
    ];
}

$file = '';
$fileName = '';
if (!empty($_FILES['file'])) {
    $file = $_FILES['file']['tmp_name'];
    $fileName = $_FILES['file']['name'];
} else if (isset($argv[1])) {
    $file = $argv[1];
}

$workerResult = worker($file, $fileName);
die(uploadResult($workerResult['isSuccess'], $workerResult['fileName'], $workerResult['transactions'], $workerResult['hash'], $workerResult['error']));