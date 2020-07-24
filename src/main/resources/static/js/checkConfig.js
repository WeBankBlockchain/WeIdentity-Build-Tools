var isReady = false;
$(document).ready(function(){
	var times = 6;
	$.ajaxSettings.async = false;
    $.get("nodeCheckState",function(data,status){
    	if(!data) {
    		$('#modal-message').on('hide.bs.modal', function () {
    			goToNodeConfig();
	    	})
    		showTime();
    		setInterval(showTime,1000);
            setTimeout(goToNodeConfig,5000);
    	} else {
    		var url = window.location.pathname;
    		url = url.substring(1);
    		if (url == "index.html") {
    			isReady = true;
    		} else {
    			isEnableMasterCns();
    		}
    	}
    });
    $.ajaxSettings.async = true;
    function showTime() {
    	times--;
    	$("#messageBody").html("<p>节点配置异常，" + times + "秒后自动进入节点配置页面。</p>");
    	$("#modal-message").modal();
    }
    
    function goToNodeConfig() {
        window.location.href="nodeConfig.html";
    }
    
    function showCns() {
    	times--;
    	$("#messageBody").html("<p>您未启用主合约，" + times + "秒后自动进入主合约部署页面，请进行启用。</p>");
    	$("#modal-message").modal();
    }
    function goToIndex() {
    	window.location.href="index.html";
    }
    
    function isEnableMasterCns() {
    	$.get("isEnableMasterCns",function(data,status){
    	    if(data) {
    	    	$('#modal-message').on('hide.bs.modal', function () {
    	    		goToIndex();
    	    	})
    	    	times = 6;
    	    	showCns();
    	    	setInterval(showCns,1000);
    	        setTimeout(goToIndex,5000);
    	    } else {
    	    	isReady = true;
    	    }
    	});
    }
});


