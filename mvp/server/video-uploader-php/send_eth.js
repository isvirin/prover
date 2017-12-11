document.getElementById('get_eth_open').addEventListener('click', function (e) {
    document.getElementById('get_eth_block').style.display = 'inline-block';
    document.getElementById('get_eth_loading').style.display = 'none';
    document.getElementById('get_eth_result').style.display = 'none';
});

document.getElementById('send_eth_btn').addEventListener('click', function (e) {
    e.preventDefault();
    var xhr = new XMLHttpRequest();
    var params = 'ethAddress=' + document.getElementById('send_eth_addr').value;
    xhr.open('POST', '/send_eth.php', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            var result = JSON.parse(this.responseText);
            var resultBlock = document.getElementById('get_eth_result');
            if (!result.success) {
                resultBlock.classList.add('error');
                document.getElementById('get_eth_result_text').innerHTML = result.message;
            } else {
                resultBlock.classList.remove('error');
                document.getElementById('get_eth_result_text').innerHTML = 'Success<br>' +
                    '<a href="https://ropsten.etherscan.io/tx/' + result.message + '" target="_blank">' +
                    result.message +
                    '</a>';
            }
            document.getElementById('get_eth_block').style.display = 'none';
            document.getElementById('get_eth_loading').style.display = 'none';
            resultBlock.style.display = 'inline';
        }
    };
    document.getElementById('get_eth_block').style.display = 'none';
    document.getElementById('get_eth_loading').style.display = 'inline';
    document.getElementById('get_eth_result').style.display = 'none';
    xhr.send(params);
    return false;
});

document.addEventListener('DOMContentLoaded', function () {
    var hash = window.location.hash.substr(1);
    if (hash === 'get_ropsten_testnet_ether') {
        document.getElementById('get_eth_open').click();
        setTimeout(function () {
            document.getElementById('send_eth_addr').tabIndex = "-1";
            document.getElementById('send_eth_addr').focus();
        }, 100);

        document.getElementById('get_eth_block').style.backgroundColor = '#ffffff';
        setTimeout(function () {
            document.getElementById('get_eth_block').style.backgroundColor = '';
        }, 500);
    }
});
