var parallaxBlock = document.getElementsByClassName('parallax');
window.onscroll = function() {
    var scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    parallaxBlock[0].style.backgroundPositionY = scrollTop/1.5+"px";
};