document.getElementById('get_eth_result').style.display = 'none';
var getEthSections = document.querySelectorAll('.get-ether');
console.log(getEthSections);
Array.prototype.forEach.call(getEthSections, function (getEthSection) {
    var sendEthBtn = getEthSection.querySelector('#send_eth_btn'),
        sendEthAddr = getEthSection.querySelector('#send_eth_addr');
    console.log(sendEthBtn);
    sendEthBtn.addEventListener('click', function (e) {
        console.log(sendEthAddr);
        e.preventDefault();
        var xhr = new XMLHttpRequest();
        var params = 'ethAddress=' + sendEthAddr.value;
        xhr.open('POST', '/send_eth.php', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onreadystatechange = function () {
            if (this.readyState === 4 && this.status === 200) {
                var result = JSON.parse(this.responseText);
                var resultBlock = document.getElementById('get_eth_result');
                if (!result.success) {
                    resultBlock.classList.add('error');
                    resultBlock.innerHTML = result.message;
                } else {
                    resultBlock.classList.remove('error');
                    document.getElementById('get_eth_result').innerHTML = 'Success<br>' +
                        '<a href="https://ropsten.etherscan.io/tx/' + result.message + '" target="_blank">' +
                        result.message +
                        '</a>';
                }
                document.getElementById('get_eth_block').style.display = 'none';
                resultBlock.style.display = 'inline';
            }
        };
        // document.getElementById('get_eth_block').style.display = 'none';
        // document.getElementById('get_eth_result').style.display = 'none';
        xhr.send(params);
        return false;
    });

    document.addEventListener('DOMContentLoaded', function () {
        var hash = window.location.hash.substr(1);
        if (hash === 'get_ropsten_testnet_ether') {
            document.getElementById('get_eth_open').click();
            setTimeout(function () {
                sendEthAddr.tabIndex = "-1";
                sendEthAddr.focus();
            }, 100);

            document.getElementById('get_eth_block').style.backgroundColor = '#ffffff';
            setTimeout(function () {
                document.getElementById('get_eth_block').style.backgroundColor = '';
            }, 500);
        }
    });
});
