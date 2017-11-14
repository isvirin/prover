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

    if (!isset($config['gethNodeAddr'])) {
        $needConfigure = true;
        $config['!_gethNodeAddr'] = 'http://';
    }

    // если конфиг отличается после проверки всех параметров
    if (json_encode($config) !== json_encode($config_saved)) {
        error_log("CONFIG CHANGES SAVED\nNEW:\n" . json_encode($config) . "\nOLD:\n" . json_encode($config_saved));
        if (file_put_contents($configFile, json_encode($config, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE)) === false) {
            return [false, 'ERROR CREATE CONFIG'];
        }
    }

    if ($needConfigure) {
        return [false, "NEED CONFIGURE CLOUD: $configFile.\n<br>Change properties started with !_ and remove !_"];
    }

    return [true, ''];
}