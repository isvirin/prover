$(document).ready(function(){
    $("#sidebar-wrapper").on("click","a[target!='_blank']", function (event) {
        event.preventDefault();
        var id  = $(this).attr('href');
        $("#wrapper").removeClass("toggled");
        if (id && id[0] === '#' && $(id)) {
            var top = $(id).offset().top - $('#top-menu:visible').height();
            $('body,html').animate({
                scrollTop: top
            }, {
                duration: 750,
                complete: function () {
                    var y = window.scrollY;
                    window.location = id;
                    window.scrollTo(window.scrollX, y);
                }
            });
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