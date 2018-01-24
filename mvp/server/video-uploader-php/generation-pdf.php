<?php

const namePDFConstructor = 'Constructor';
const pathLibPDFConstructor = '/usr/local/lib';

function generationPdf($fileName) {

    $pdfPath = __DIR__ . '/pdf/';
    $tmpPath = __DIR__ . '/pdf/tmp/';
    if(!file_exists($pdfPath)) {
        mkdir($pdfPath, 0777);
        if(!file_exists($tmpPath))
            mkdir($tmpPath, 0777);
    }

    $htmlPage = templateHTML();
    $nameConfigFileHTML = $tmpPath . $fileName . '.html';
    file_put_contents($nameConfigFileHTML, $htmlPage);
    chmod($nameConfigFileHTML, 0777);

    $config['content'][] = [
        'type' => "html",
        'file' => $nameConfigFileHTML,
        'id' => $fileName
    ];

    file_put_contents($tmpPath . '/config.json', json_encode($config, JSON_UNESCAPED_SLASHES));
    chmod($tmpPath . '/config.json', 0777);

    $commandStartPDFConstructor =
        'LD_LIBRARY_PATH=' . pathLibPDFConstructor .
        ' xvfb-run ' . namePDFConstructor . ' ' .
        $config . '/config.json ' .
        $pdfPath . '/' .
        $fileName;

    shell_exec($commandStartPDFConstructor);
}

function templateHTML() {
    $html = <<<EOD
        <style>
    * {
        font-family: "Open Sans", sans-serif;
    }
    html,
    body {
        margin: 0;
        width: 1024px;
    }
    body {
        padding: 50px;
        box-sizing: border-box;
    }
    .wrapper {
        background: url("https://prover.io/assets/images/pdf_template/border@3x.png") no-repeat;
        width: 924px;
        height: 1351px;
        background-size: cover;
    }
    .content {
        width: 660px;
        margin: 0 auto;
    }
    h1 {
        margin: 0 0 50px;
        font-size: 63px;
        font-weight: 900;
        line-height: 80px;
        text-align: center;
        text-transform: uppercase;
    }
    .content p.content__filename {
        text-transform: uppercase;
        font-weight: 600;
        color: #7f7f7f;
        text-align: center;
        font-size: 15px;
        margin: 10px 0;
    }
    .content .logo {
        margin: 0 auto;
        display: block;
        height: 24px;
        padding: 106px 0 30px;
    }
    .content h3 {
        text-align: center;
        font-size: 30px;
        margin: 10px 0;
    }
    .content .content-text {
        margin-top: 100px;
    }
    .content .characteristics:after {
        content: "";
        display: table;
        clear: both;
    }
    .content .characteristics div {
        float: left;
    }
    .content .characteristics h4 {
        text-transform: uppercase;
        font-weight: 600;
        color: #7f7f7f;
        font-size: 13px;
        margin: 10px 0;
    }
    .content .characteristics p {
        font-size: 18px;
        margin: 10px 0;
    }
    .content .content-text p {
        font-size: 20px;
        line-height: 25px;
    }
    .content .content-link {
        background: url("https://prover.io/assets/images/pdf_template/stamp@3x.png") center no-repeat;
        height: 140px;
        background-size: contain;
        margin-top: 80px;
        clear: both;
    }
    .content .content-link p {
        display: inline-block;
        font-size: 18px;
        font-weight: 600;
    }
    .content .qr-code {
        margin-top: 45px;
        width: 50%;
        float: left;
    }
    .content .qr-code img {
        width: 84px;
    }
    .content .qr-code h5 {
        text-transform: uppercase;
        display: inline-block;
        color: #7f7f7f;
        margin: 30px 0;
    }
    .content .qr-code.left img,
    .content .qr-code.left h5 {
        float: left;
        text-align: left;
    }
    .content .qr-code.right img,
    .content .qr-code.right h5 {
        float: right;
        text-align: right;
    }
</style>

<div class="wrapper">
    <div class="content">
        <img class="logo" src="https://prover.io/assets/images/pdf_template/logo@3x.png">
        <h1>Certificate <br> of authenticity</h1>
        <p class="content__filename">Video file name</p>
        <h3>VID_20171213_191433.mp4</h3>
        <div class="characteristics">
            <div style="width:100%;margin:30px 0 15px">
                <h4>File hash</h4>
                <p>0xf7523509a911628973c0cc52c18ae19e90c453b81342ee487b1a955803bd01f6</p>
            </div>
            <div style="width:220px;">
                <h4>Swype code</h4>
                <p>1234567</p>
            </div>
            <div style="width:220px;">
                <h4>Refference start time</h4>
                <p>15.12.2017 01:58:29</p>
            </div>
            <div style="width:220px;">
                <h4>Refference end time</h4>
                <p>15.12.2017 02:16:54</p>
            </div>
        </div>
        <div class="content-text">
            <p>We hereby confirm, that this video file was actually recorded exactly during the time interval, that is described in this certificate. The basis for this statement is the fact of presence the swype code that was generated, with the number of the block, current at that time, and the hash of the file, stored in the block, current at that time. Links to the blocks are presented here.</p>
        </div>
        <div class="content-link">
            <p style="float:left;margin:60px 0 60px 115px;">www.prover.io</p>
            <p style="float:right;margin: 60px 115px 60px 0;">mvp.prover.io</p>
        </div>
        <div class="qr-code left">
            <img src="https://prover.io/assets/images/pdf_template/qr.png">
            <h5>Reference block<br>with swype-code</h5>
        </div>
        <div class="qr-code right">
            <img src="https://prover.io/assets/images/pdf_template/qr.png">
            <h5>Reference block<br>with hash</h5>
        </div>
    </div>
</div>
EOD;

    return $html;

}