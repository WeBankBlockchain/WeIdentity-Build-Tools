var isReadyForDb = false;
$(document).ready(function(){
	var times = 6;
	$.ajaxSettings.async = false;
    $.get("dbCheckState",function(data,status){
    	if(!data) {
    		showConfigMessage();
    	} else {
    		isReadyForDb = true;
    	}
    });
    $.ajaxSettings.async = true;
});

function showConfigMessage() {
	$("#configType").val("1");
	$("#configBody").html("<p>您数据库配置异常，如果需要使用存证(Evidence)异步上链功能，请配置数据库。</p>");
	$("#modal-config").modal();
}
