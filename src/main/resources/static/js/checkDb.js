var isReadyForDb = false;
$(document).ready(function(){
	var times = 6;
	$.ajaxSettings.async = false;
    $.get("dbCheckState",function(data,status){
    	if(!data) {
    		$('#modal-message').on('hide.bs.modal', function () {
    			goToDbConfig();
	    	})
    		showTime();
    		setInterval(showTime,1000);
            setTimeout(goToDbConfig,5000);
    	} else {
    		isReadyForDb = true;
    	}
    });
    $.ajaxSettings.async = true;
    function showTime() {
    	times--;
    	$("#messageBody").html("<p>数据库配置异常，" + times + "秒后自动进入数据库配置页面。</p>");
    	$("#modal-message").modal();
    }
    function goToDbConfig() {
        window.location.href="dbConfig.html";
    }
});


