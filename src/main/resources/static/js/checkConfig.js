var isReady = false;
var enEableMasterCns = true;
$(document).ready(function(){
	var times = 6;
	$.ajaxSettings.async = false;
    $.get("nodeCheckState",function(data,status){
    	if(!data) {
    		showMessageForNodeException();
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
    
    function isEnableMasterCns() {
    	$.get("isEnableMasterCns",function(data,status){
    	    if(data) {
    	    	enEableMasterCns = false;
    	    	showMessageForNodeException();
    	    } else {
    	    	isReady = true;
    	    }
    	});
    }
});

function showMessageForNodeException() {
	if (enEableMasterCns) {
		$("#configType").val("2");
		$("#configBody").html("<p>您区块链节点异常，请配置正确的区块链节点。</p>");
	} else {
		$("#configType").val("3");
		$("#configBody").html("<p>您未启用主合约，请前往启用主合约。</p>");
	}
	$("#modal-config").modal();
}
