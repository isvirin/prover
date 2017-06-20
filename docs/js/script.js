$(document).ready(function(){
    $(".sidebar-nav").on("click","a[target!='_blank']", function (event) {
        var id  = $(this).attr('href');
        $("#wrapper").removeClass("toggled");
        if (id && id[0] === '#' && $(id)) {
            var top = $(id).offset().top - 45;
            $('body,html').animate({scrollTop: top}, 750);
        }
    });

    $(document).click(function(event) {
        if ($(event.target).closest("#sidebar-wrapper").length || $(event.target).closest("#top-menu .lines-button").length) return;
        $("#wrapper").removeClass("toggled");
        event.stopPropagation();
    });

    $("#top-menu .lines-button").click(function () {
        $("#wrapper").toggleClass("toggled");
    });
});