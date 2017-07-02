$(document).ready(function(){
    $("body").on("click","a[target!='_blank']", function(event) {
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

    var lastId,
        menu = $(".sidebar-nav"),
        menuItems = menu.find("a"),
        scrollItems = menuItems.map(function(){
            var item = $($(this).attr("href"));
            if (item.length) { return item; }
        });

    $(window).scroll(function(){
        var fromTop = $(this).scrollTop();
        var cur = scrollItems.map(function(){
            if ($(this).offset().top - 40 < fromTop)
                return this;
        });
        cur = cur[cur.length-1];
        var id = cur && cur.length ? cur[0].id : "";

        if (lastId !== id) {
            lastId = id;
            menuItems
                .parent().removeClass("active")
                .end().filter("[href='#"+id+"']").parent().addClass("active");
        }
    });

    $(document).click(function(event) {
        var t = $(event.target);
        if (t.closest("#sidebar-wrapper").length || t.closest("#top-menu .lines-button").length) {
            return;
        }
        $("#wrapper").removeClass("toggled");
        event.stopPropagation();
    });

    $("#top-menu .lines-button").click(function () {
        $("#wrapper").toggleClass("toggled");
    });

    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('.sidebar-language > a').click(function (e) {
        e.preventDefault();
        localStorage['lang'] = e.target.dataset.lang;
        location.reload();
    });
});