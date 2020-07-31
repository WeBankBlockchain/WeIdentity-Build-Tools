var isReady = false;
var enEableMasterCns = true;
$(document).ready(function(){
	
	if(!getNodeCheckState()) {
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
	
    function isEnableMasterCns() {
    	if(!getEnableState()) {
	    	enEableMasterCns = false;
	    	showMessageForNodeException();
	    } else {
	    	isReady = true;
	    }
    }
    
    function getNodeCheckState() {
    	var nodeCheckState = sessionStorage.getItem('nodeCheckState');
    	if (!nodeCheckState) {
    		$.ajaxSettings.async = false;
    		$.get("nodeCheckState",function(value,status){
    			if (value) {
    				nodeCheckState = value;
    				sessionStorage.setItem("nodeCheckState", nodeCheckState);
    			}
    		})
    		$.ajaxSettings.async = true;
    	}
    	return nodeCheckState;
    }
    
    function getEnableState() {
    	var isEnable = sessionStorage.getItem('isEnableMasterCns');
    	if (!isEnable) {
    		$.ajaxSettings.async = false;
    		$.get("isEnableMasterCns",function(value,status){
    			if (!value) {
    				isEnable = true;
    				sessionStorage.setItem("isEnableMasterCns", isEnable);
    			}
    		})
    		$.ajaxSettings.async = true;
    	}
    	return isEnable;
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
