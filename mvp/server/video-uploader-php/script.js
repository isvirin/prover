// var parallaxBlock = document.getElementsByClassName('parallax');
// window.onscroll = function() {
//     var scrollTop = window.pageYOffset || document.documentElement.scrollTop;
//     parallaxBlock[0].style.backgroundPositionY = scrollTop/1.5+"px";
// };
$(document).ready(function() {
    var menuItem = $('.menu-list a');
    menuItem.click(function(e) {
        var itemClick = $(this).attr("href")
        var destination = $(itemClick).offset().top;
        $('html, body').animate({
            scrollTop: destination
        }, 1000);
        e.preventDefault();
    });
    var switchBtn = $('.switch input[type=checkbox]');
    switchBtn.change(function() {
        if(this.checked) {
            $('#prover').animate({ opacity: 0 }, 350);
            $('body').addClass('clapperboard');
            setTimeout(function () {
                $('#prover').css('display','none');
                $('#clapperboard').css('display','block');
                $('#clapperboard').animate({ opacity: 1 }, 350);
            }, 350);
        } else {
            $('#clapperboard').animate({ opacity: 0 }, 350);
            $('body').removeClass('clapperboard');
            setTimeout(function () {
                $('#clapperboard').css('display','none');
                $('#prover').css('display','block');
                $('#prover').animate({ opacity: 1 }, 350);
            }, 350);
        }
    });
});