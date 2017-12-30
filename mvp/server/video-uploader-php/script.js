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
            $('#clapperboard').css('display','block');
            $('#prover').css('display','none');
            $('body').addClass('clapperboard');
            console.log('checked');
        } else {
            $('#clapperboard').css('display','none');
            $('#prover').css('display','block');
            $('body').removeClass('clapperboard');
        }
    });
});