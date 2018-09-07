$(document).ready(function() {
    $("#query").keyup(function(event){
        // not enter or blank
        if(event.keyCode!=38 && event.keyCode!=40 && event.keyCode!=13){
            var q = $("#query").val();
            // not ""
            if(q != "") {
                $.get("cgi-bin/get_suggest.py?q=" + q, function(data, status){
                    if (data.length > 0 && status == "success") {
                        $('ul.list').html(data);
                        $("ul.list").show();
                    // remove possible highlight ones
                    if($("ul.list li:visible").hasClass("lilight")){
                        $("ul.list li").removeClass("lilight");
                    }
                    // any visible exist: high light it
                    if($("ul.list li:visible")){
                        $("ul.list li:visible:eq(0)").addClass("lilight");
                    }
                    }
                })
            } else {
                // nothing to show
                $("ul.list").hide();
                $("ul.list li").removeClass("lilight");
            }
        }
        // enter: write current suggestion to textfield
        if(event.keyCode==13){
            $("#query").val($("ul.list li.lilight:visible").text());
            /*var q = $("#query").val()
            if(q.lastIndexOf(" ") == -1) {
                $("#query").val($("ul.list li.lilight:visible").text());
            } else {
                $("#query").val(q.substring(0, q.lastIndexOf(" ") + 1) + $("ul.list li.lilight:visible").text());
            }*/
            $('ul.list').empty();
            $("ul.list").hide();
        }
    });

    // up and down
    $("#query").keydown(function(event){
        // up: log and make the previous high light
        if(event.keyCode==40){
            if($("ul.list li").is(".lilight")){
                console.log("down");
                if($("ul.list li.lilight").nextAll().is("li:visible")){
                    $("ul.list li.lilight").removeClass("lilight").next("li").addClass("lilight");
                }
            }
        }
        // down: make the next high light
        if(event.keyCode==38){
            if($("ul.list li").is(".lilight")){
                if($("ul.list li.lilight").prevAll().is("li:visible")){
                    $("ul.list li.lilight").removeClass("lilight").prev("li").addClass("lilight");
                }
            }
        }
    });

    // mouth click: fill in the choosen one, hide list
    $("ul.list li").live("click", function() {
        $("#query").val($(this).text());
        /*var q = $("#query").val()
        if(q.lastIndexOf(" ") == -1) {
            $("#query").val($(this).text());
        } else {
            $("#query").val(q.substring(0, q.lastIndexOf(" ") + 1) + $(this).text());
        }*/
        query.focus();
        $('ul.list').empty();
        $("ul.list").hide();
    });

    // hover: high light hover one, hide list
    $("ul.list li").live("hover", function(){
        $("ul.list li").removeClass("lilight");
        $(this).addClass("lilight");
    });

    // click other postion: hide list
    $(document).click(function(){
        $("ul.list").hide();
    });
});

function validate_required(field, alerttxt)
{
    with (field)
    {
        if (value == null || value == "" ||value.replace(/(^\s*)|(\s*$)/g, "") === "") {
            alert(alerttxt);
            return false;
        }
        else {
            return true;
        }
    }
}

function validate_form(thisform)
{
    with (thisform)
    {
        if (validate_required(query, "Query must be filled out!") == false) {
            query.focus();
            return false;
        }
    }
}