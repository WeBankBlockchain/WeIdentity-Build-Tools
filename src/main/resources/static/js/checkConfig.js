var isReady = false;
$(document).ready(function(){
	var times = 6;
	$.ajaxSettings.async = false;
    $.get("isReady",function(data,status){
    	if(!data) {
    		showTime();
    		setInterval(showTime,1000);
            setTimeout(goToIndex,5000);
    	} else {
    		isReady = true;
    	}
    });
    $.ajaxSettings.async = true;
    function showTime() {
    	times--;
    	$("#messageBody").html("<p>配置未准备完成，" + times + "秒后自动进入配置页面。</p>");
    	$("#modal-message").modal();
    }
    function goToIndex() {
        window.location.href="index.html";
    }
});


