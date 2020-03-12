$(document).ready(function(){
	bsCustomFileInput.init();
	var nodeReady = false;
	//获取配置
    $.get("loadConfig",function(data,status){
        $("#nodeForm  #orgId").val(data.org_id);
        $("#nodeForm  #version").val(data.blockchain_fiscobcos_version);
        $("#nodeForm  #cnsProFileActive").val(data.cns_profile_active);
        $("#nodeForm  #ipPort").val(data.blockchain_address);
        $("#nodeForm  #chainId").val(data.chain_id);
        caDisplay(data.blockchain_fiscobcos_version);
        checkCa(data);
    });
    
    function  checkCa(data) {
    	var message = "该证书已存在，重新上传将被覆盖。";
    	if (data["ca.crt"] == "true") {
    		$("#caCrtSpan").html(message).show();
    	}
    	if (data["node.key"] == "true") {
    		$("#nodeKeySpan").html(message).show();
    	}
    	if (data["node.crt"] == "true") {
    		$("#nodeCrtSpan").html(message).show();
    	}
    	if (data["client.keystore"] == "true") {
    		$("#clientKeyStoreSpan").html(message).show();
    	}
    }
    
    function caDisplay(v) {
    	if (v == "1") {
        	$("#caV1").show();
        	$("#caV2").hide();
        } else {
        	$("#caV2").show();
        	$("#caV1").hide();
        }
    }
    
    $("#nodeForm  #version").change(function(){
    	var selected = $(this).children('option:selected').val();
    	caDisplay(selected);
    })
    
    //提交配置
    $("#postBtn").click(function(){
    	if (nodeReady) {
    		goConfig(this);
    		return;
    	}
    	var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var message = checkInput();
        if (message != null) {
        	$("#messageBody").html("<p>"+message+"</p>");
    	    $("#modal-message").modal();
    	    return ;
        }
        
	    var formData = new FormData();
	    formData.append("file", $("#caCrtFile")[0].files[0]);
	    formData.append("file", $("#nodeCrtFile")[0].files[0]);
	    formData.append("file", $("#nodeKeyFile")[0].files[0]);
	    formData.append("file", $("#clientKeyStoreFile")[0].files[0]);
	    formData.append("orgId", $("#nodeForm  #orgId").val());
	    formData.append("version", $("#nodeForm  #version").val());
	    formData.append("cnsProFileActive", $("#nodeForm  #cnsProFileActive").val());
	    formData.append("ipPort", $("#nodeForm  #ipPort").val());
	    formData.append("chainId", $("#nodeForm  #chainId").val());
	    $("#checkBody").html("<p>配置提交中,请稍后...</p>");
	    $("#modal-default").modal();
	    $("#configBtn").addClass("disabled");
	    $.ajax({
	        url:'nodeConfigUpload', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            console.log(res);
	            if (res=="success") {
	            	//检查节点是否正确
	            	$("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='success-span'>成功</span>, 检查准备中,请稍后...</p>");
	            	setTimeout(checkForTimeout,2000);
	            } else if (res=="fail") {
	            	 $("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            } else {
	                console.log(res);
	            }
	        }
	    })
    })
    
    function checkInput() {
    	var orgId = $.trim($("#nodeForm  #orgId").val());
    	if (orgId.length == 0) {
    		return "请输入您的机构名称";
    	}
    	
    	var ipPort = $.trim($("#nodeForm  #ipPort").val());
    	if (ipPort.length == 0) {
    		return "请输入您的节点IP与Port";
    	}
    	var chainId = $.trim($("#nodeForm  #chainId").val());
    	if (chainId.length == 0) {
    		return "请输入您的chainId";
    	}
    	var r = /^[1-9][0-9]*$/;
    	if(!r.test(chainId)) {
    		return "chainId必须为整数";
    	}
    	
    	return null;
    }
    
    function checkForTimeout() {
    	$("#checkBody").html($("#checkBody").html() + "<p>配置检查中,请稍后...</p>");
    	setTimeout(checkNode,2000);
    }
    
    function checkNode() {
    	$.get("checkNode",function(data,status){
            if(data) {//检查成功
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='success-span'>成功</span>。</p>");
         	   $("#configBtn").removeClass("disabled");
         	  disabledInput();
         	  step2();
            } else {//检查失败
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
            }
         });
    }
    $.get("nodeCheckState",function(data,status){
        if(data) {//检查成功
            $("#checkBody").html("<p>当前配置正常，无需再次配置。</p>");
            $("#modal-default").modal();
            disabledInput();
            step2();
        } else {
        	step1();
        }
     });
    
    function disabledInput() {
    	$("#nodeForm  #orgId").attr("disabled",true);
        $("#nodeForm  #version").attr("disabled",true);
        $("#nodeForm  #cnsProFileActive").attr("disabled",true);
        $("#nodeForm  #ipPort").attr("disabled",true);
        $("#nodeForm  #chainId").attr("disabled",true);
        $("#caCrtFile").attr("disabled",true);
        $("#caCrtSpan").hide();
        $("#nodeCrtFile").attr("disabled",true);
        $("#nodeCrtSpan").hide();
        $("#nodeKeyFile").attr("disabled",true);
        $("#nodeKeySpan").hide();
        $("#clientKeyStoreFile").attr("disabled",true);
        $("#clientKeyStoreSpan").hide();
        $("#configBtn").removeClass("disabled");
        $("#i-node").removeClass("fa-circle");
        $("#i-node").addClass("fa-check-circle");
        nodeReady = true;
    }
    
    function step1() {
    	if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({});
		var enjoyhint_script_steps = [{
		    'next #nodeForm': "请完成配置，并点击' 下一步'",
		    'showSkip': false,
		    'nextButton': {
		        className: "myNext",
		        text: "确定"
		    }
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
    
    function step2() {
    	if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({});
		var enjoyhint_script_steps = [{
		    'click #configBtn': '下一步，配置数据库。',
		    'showSkip': false
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
});

function goConfig(thiObj) {
	$.get("nodeCheckState",function(data,status){
        if(data) {//检查成功
     	   goTo(thiObj, "dbConfig.html");
        }
     });
}