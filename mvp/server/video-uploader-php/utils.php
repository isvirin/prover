<?php

/**
 * @param string $url
 * @param array $data
 * @return mixed
 */
function httpPost($url, $data = [])
{
    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_POSTFIELDS, http_build_query($data));
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($curl);
    curl_close($curl);
    return $response;
}

/**
 * @return array [isSuccess, $errorText]
 */
function loadConfig()
{
    $config_saved = [];
    $configFile = __DIR__ . '/config.json';
    if (is_file($configFile)) {
        $config_saved = json_decode(file_get_contents($configFile), true);
        if (is_null($config_saved)) {
            return [false, 'CONFIG NOT VALID JSON: ' . $configFile];
        }
    }

    $config = $config_saved;
    $needConfigure = false;

    if (!isset($config['gethNodeUrl'])) {
        $needConfigure = true;
        $config['!_gethNodeUrl'] = 'http://';
    } else {
        $config['gethNodeUrl'] = preg_replace('/(.*)\/$/', '$1', $config['gethNodeUrl']);
        DEFINE('GETH_NODE_URL', $config['gethNodeUrl']);
    }

    if (!isset($config['mvpCgiBinUrl'])) {
        $needConfigure = true;
        $config['!_mvpCgiBinUrl'] = 'http://';
    } else {
        $config['mvpCgiBinUrl'] = preg_replace('/(.*)\/$/', '$1', $config['mvpCgiBinUrl']);
        DEFINE('MVP_CGI_BIN_URL', $config['mvpCgiBinUrl']);
    }

    if (!isset($config['sendEthPassword'])) {
        $config['sendEthPassword'] = '';
    }
    DEFINE('SEND_ETH_PASSWORD', $config['sendEthPassword']);

    // если конфиг отличается после проверки всех параметров
    if (json_encode($config) !== json_encode($config_saved)) {
        error_log("CONFIG CHANGES SAVED\nNEW:\n" . json_encode($config) . "\nOLD:\n" . json_encode($config_saved));
        if (file_put_contents($configFile, json_encode($config, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE)) === false) {
            return [false, 'ERROR CREATE CONFIG'];
        }
    }

    if ($needConfigure) {
        return [false, "NEED CONFIGURE: $configFile.\n<br>Change properties started with !_ and remove !_"];
    }

    return [true, ''];
}

spl_autoload_register('autoload');

function autoload($className)
{

    $className = ltrim($className, '\\');
    $fileName = '';

    if ($lastNsPos = strripos($className, '\\')) {
        $namespace = substr($className, 0, $lastNsPos);
        $className = substr($className, $lastNsPos + 1);
        $fileName = str_replace('\\', DIRECTORY_SEPARATOR, $namespace) . DIRECTORY_SEPARATOR;
    }

    $fileName .= $className . '.php';

    // set the path to our source directory, relative to the directory we are in
    $src = realpath('src');

    require $src . DIRECTORY_SEPARATOR . $fileName;
}