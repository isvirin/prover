<?php
require('utils.php');
$loadConfig_result = loadConfig();
if (!$loadConfig_result[0]) {
    echo $loadConfig_result[1];
    exit(1);
}
?>

<!DOCTYPE html>

<html lang="en" class="no-js">

<head>
    <meta charset="utf-8">
    <title>Prover video uploader</title>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <link rel="icon" type="image/x-icon" href="prover-icon-32.png"/>
    <link rel="stylesheet" href="main.css?<?= md5_file('main.css') ?>">
    <link rel="stylesheet" href="//fonts.googleapis.com/css?family=Open+Sans">
    <script type="text/javascript" src="upload.js?<?= md5_file('upload.js') ?>" defer></script>

    <script>
        var r = document.querySelectorAll("html")[0];
        r.className = r.className.replace(/(^|\s)no-js(\s|$)/, "$1js$2")
    </script>
</head>

<body>

<div class="wrapper">
    <div class="container">
        <div class="logo">
            <a href="https://prover.io"><img class="logo__img" src="images/logo.svg"></a>
            <a href="#"><img class="google__play" src="images/gplay_eng.svg"></a>
        </div>
        <form method="post" action="upload.php" enctype="multipart/form-data" novalidate class="box"
              onclick="document.getElementById('file').click()">
            <div class="box__input">
                <svg class="box__icon" viewBox="0 0 1024 1024" width="100"><title>download</title>
                    <path d="M760.499 493.901c-9.995-9.997-26.206-9.997-36.203 0l-212.296 212.294v-578.195c0-14.138-11.462-25.6-25.6-25.6s-25.6 11.462-25.6 25.6v578.195l-212.298-212.294c-9.998-9.997-26.206-9.997-36.205 0-9.997 9.995-9.997 26.206 0 36.203l256 256c5 4.997 11.55 7.496 18.102 7.496s13.102-2.499 18.102-7.501l256-256c9.997-9.995 9.997-26.203-0.003-36.198z"></path>
                    <path d="M896 972.8h-819.2c-42.347 0-76.8-34.451-76.8-76.8v-102.4c0-14.139 11.462-25.6 25.6-25.6s25.6 11.461 25.6 25.6v102.4c0 14.115 11.485 25.6 25.6 25.6h819.2c14.115 0 25.6-11.485 25.6-25.6v-102.4c0-14.139 11.461-25.6 25.6-25.6s25.6 11.461 25.6 25.6v102.4c0 42.349-34.451 76.8-76.8 76.8z"></path>
                </svg>
                <input type="file" name="file" id="file" class="box__file">
                <label class="box__labelFile_default" for="file">
                    <strong>Choose a file</strong>
                    <span class="box__dragndrop"> or drag it here.</span>
                </label>
                <label class="box__labelFile_file" for="file"></label>
                <button type="submit" class="box__button">Upload</button>
            </div>

            <div class="box__uploading">Uploading&hellip;</div>
            <div class="box__success">
                Done!
                <br>
                <span class="box__success_msg"></span>
                <br>
                <span class="box__restart">Try another file</span>
            </div>
            <div class="box__error">
                Error!
                <br>
                <span></span>
                <br>
                <span class="box__restart">Try another file</span>
            </div>
        </form>
        <div class="content">
            <p>To authenticate a file, please upload your video. Our system will verify the file hash and existence of swype code. In the case of coincidence of hashes and swype codes - the video file will be considered as authentic.</p>
        </div>
        <a href="manual.html" class="btn">how it works</a>
        <div class="block_client_address_info"></div>
    </div>
    <footer>
        <div class="link mail"><a href="mailto:info@prover.io">info@prover.io</a></div>
        <div class="link social_networks">
            <a target="_blank" href="https://www.facebook.com/prover.blockchain/" ><img src="images/facebook.svg"></a>
            <a target="_blank" href="https://twitter.com/prover_io"><img src="images/twitter.svg"></a>
            <a target="_blank" href="https://t.me/joinchat/AAHHrURp4xhK-RuCYhtPlA"><img src="images/telegram.svg"></a>
        </div>
        <div class="link copyright">Prover © 2017</div>
    </footer>
</div>
</body>

</html>