$(document).ready(function(){
    $(".sidebar-nav").on("click","a[target!='_blank']", function (event) {
        event.preventDefault();
        var id  = $(this).attr('href');
        if (id && id[0] === '#' && $(id)) {
            var top = $(id).offset().top;
            $('body,html').animate({scrollTop: top}, 750);
        }
    });
});