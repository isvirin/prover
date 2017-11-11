<?php

$upload_success = false;
$upload_error = 'error text';

if (!empty($_FILES['file'])) {
    $upload_success = true;
    $upload_error = '';
}

die(json_encode([
    'success' => $upload_success,
    'error' => $upload_error
], JSON_UNESCAPED_UNICODE));