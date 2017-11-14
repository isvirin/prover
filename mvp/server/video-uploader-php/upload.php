<?php

$isSuccess = false;
$error = 'error text';
$data = '';

if (!empty($_FILES['file'])) {
    $isSuccess = true;
    $error = '';
    $data = hash_file('sha256', $_FILES['file']['tmp_name']);
}

die(json_encode([
    'success' => $isSuccess,
    'data' => $data,
    'error' => $error
], JSON_UNESCAPED_UNICODE));