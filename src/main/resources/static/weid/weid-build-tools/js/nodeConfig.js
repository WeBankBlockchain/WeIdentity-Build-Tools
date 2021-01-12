$(document).ready(function(){
	bsCustomFileInput.init();
	//获取配置
    $.get("loadConfig",function(data,status){
        $("#nodeForm  #orgId").val(data.org_id);
        $("#nodeForm  #amopId").val(data.amop_id);
        $("#nodeForm  #encryptType").val(data.encrypt_type);
        $("#nodeForm  #version").val(data.blockchain_fiscobcos_version);
        $("#nodeForm  #cnsProFileActive").val(data.cns_profile_active);
        $("#nodeForm  #ipPort").val(data.blockchain_address);
        caDisplay(data.encrypt_type);
        checkCa(data);
//        $.get("nodeCheckState",function(data,status){
//            if(data) {//检查成功
//               disabledInput();
//            }
//         });
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
        if (data["gmca.crt"] == "true") {
            $("#gmCaCrtSpan").html(message).show();
        }
        if (data["gmsdk.crt"] == "true") {
            $("#gmSdkCrtSpan").html(message).show();
        }
        if (data["gmsdk.key"] == "true") {
            $("#gmSdkKeySpan").html(message).show();
        }
        if (data["gmensdk.crt"] == "true") {
            $("#gmenSdkCrtSpan").html(message).show();
        }
        if (data["gmensdk.key"] == "true") {
            $("#gmenSdkKeySpan").html(message).show();
        }
    }
    
    function caDisplay(v) {
        if (v == "0") {
            $("#ecdsa").show();
            $("#sm2").hide();
        } else {
            $("#sm2").show();
            $("#ecdsa").hide();
        }
    }
    
    $("#nodeForm  #encryptType").change(function(){
    	var selected = $(this).children('option:selected').val();
    	caDisplay(selected);
    })
    
    //提交配置
    $("#postBtn").click(function(){
    	var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var message = checkInput();
        if (message != null) {
        	$("#messageBody").html("<p>"+message+"</p>");
    	    $("#modal-message").modal();
    	    return ;
        }
        
	    var formData = new FormData();
	    var encryptType = $("#encryptType option:selected").val();
        if (encryptType == "0") {
            formData.append("file", $("#caCrtFile")[0].files[0]);
            formData.append("file", $("#nodeCrtFile")[0].files[0]);
            formData.append("file", $("#nodeKeyFile")[0].files[0]);
        } else {
            formData.append("file", $("#gmCaCrtFile")[0].files[0]);
            formData.append("file", $("#gmSdkCrtFile")[0].files[0]);
            formData.append("file", $("#gmSdkKeyFile")[0].files[0]);
            formData.append("file", $("#gmenSdkCrtFile")[0].files[0]);
            formData.append("file", $("#gmenSdkKeyFile")[0].files[0]);
        }
	    formData.append("orgId", $.trim($("#nodeForm  #orgId").val()));
	    formData.append("amopId", $.trim($("#nodeForm  #amopId").val()));
	    formData.append("version", $("#nodeForm  #version").val());
	    formData.append("encryptType", encryptType);
	    formData.append("cnsProFileActive", $("#nodeForm  #cnsProFileActive").val());
	    formData.append("ipPort", $.trim($("#nodeForm  #ipPort").val()));
	    $("#confirmMessage1Body").html("<p>> 配置提交中...</p>");
	    $("#confirmMessage1Btn").addClass("disabled");
	    $("#postBtn").addClass("disabled");
	    $("#modal-confirm-message1").modal();
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
	            	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置提交<span class='success-span'>成功</span>, 检查准备中...</p>");
	            	setTimeout(checkForTimeout,2000);
	            } else if (res=="fail") {
	            	 $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            	 $("#confirmMessage1Btn").removeClass("disabled");
	            	 $("#postBtn").removeClass("disabled");
	            } else {
	            	 $("#confirmMessage1Btn").removeClass("disabled");
	            	 $("#postBtn").removeClass("disabled");
	                console.log(res);
	            }
	            $("#modal-confirm-message1").modal();
	        }
	    })
    })
    
    function checkInput() {
    	var orgId = $.trim($("#nodeForm  #orgId").val());
    	if (orgId.length == 0) {
    		return "请输入您的机构名称";
    	}
    	var amopId = $.trim($("#nodeForm  #amopId").val());
    	if (amopId.length == 0) {
    		return "请输入您的通讯ID";
    	}
    	var ipPort = $.trim($("#nodeForm  #ipPort").val());
    	if (ipPort.length == 0) {
    		return "请输入您的节点IP与Port";
    	}
        var encryptType = $("#encryptType option:selected").val();
        // 检查上传文件是否跟要求一致
        if (encryptType == "0") {
            var caCrtFile = $("#caCrtFile")[0].files[0];
            if (caCrtFile != null && caCrtFile.name != "ca.crt") {
                return "请选择正确的ca.crt文件";
            }
            var nodeCrtFile = $("#nodeCrtFile")[0].files[0];
            if (nodeCrtFile != null && nodeCrtFile.name != "node.crt") {
                return "请选择正确的node.crt文件";
            }
            var nodeKeyFile = $("#nodeKeyFile")[0].files[0];
            if (nodeKeyFile != null && nodeKeyFile.name != "node.key") {
                return "请选择正确的node.key文件";
            }
        } else {
            var gmCaCrtFile = $("#gmCaCrtFile")[0].files[0];
            if (gmCaCrtFile != null && gmCaCrtFile.name != "gmca.crt") {
                return "请选择正确的gmca.crt文件";
            }
            var gmSdkCrtFile = $("#gmSdkCrtFile")[0].files[0];
            if (gmSdkCrtFile != null && gmSdkCrtFile.name != "gmsdk.crt") {
                return "请选择正确的gmsdk.crt文件";
            }
            var gmSdkKeyFile = $("#gmSdkKeyFile")[0].files[0];
            if (gmSdkKeyFile != null && gmSdkKeyFile.name != "gmsdk.key") {
                return "请选择正确的gmsdk.key文件";
            }
            var gmenSdkCrtFile = $("#gmenSdkCrtFile")[0].files[0];
            if (gmenSdkCrtFile != null && gmenSdkCrtFile.name != "gmensdk.crt") {
                return "请选择正确的gmensdk.crt文件";
            }
            var gmenSdkKeyFile = $("#gmenSdkKeyFile")[0].files[0];
            if (gmenSdkKeyFile != null && gmenSdkKeyFile.name != "gmensdk.key") {
                return "请选择正确的gmensdk.key文件";
            }
        }
    	return null;
    }
    
    function checkForTimeout() {
    	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置检查中...</p>");
    	$("#modal-confirm-message1").modal();
    	setTimeout(checkNode,2000);
    }
    
    function checkNode() {
    	$.get("checkNode",function(data,status){
    		if(data == "success") {//检查成功
    			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置检查<span class='success-span'>成功</span>。</p>");
    			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p><span class='success-span'>提示：目前暂不支持修改配置动态实时生效，修改配置需重启服务才能生效。</span></p>");
           	    //disabledInput();
           	    $("#i-node").removeClass("fa-circle");
                $("#i-node").addClass("fa-check-circle");
             } else if (data == "fail"){//检查失败
          	    $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
          	    $("#i-node").removeClass("fa-check-circle");
                $("#i-node").addClass("fa-circle");
             } else {//检查失败
          	    $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>> 配置检查<span class='fail-span'>失败</span>: " + data + "</p>");
          	    $("#i-node").removeClass("fa-check-circle");
                $("#i-node").addClass("fa-circle");
             }
    		 $("#confirmMessage1Btn").removeClass("disabled");
    		 $("#postBtn").removeClass("disabled");
             $("#modal-confirm-message1").modal();
         });
    }

    function disabledInput() {
    	$("#postBtn").attr("disabled",true);
    	$("#nodeForm  #orgId").attr("disabled",true);
    	$("#nodeForm  #amopId").attr("disabled",true);
        $("#nodeForm  #version").attr("disabled",true);
        $("#nodeForm  #cnsProFileActive").attr("disabled",true);
        $("#nodeForm  #ipPort").attr("disabled",true);
        $("#caCrtFile").attr("disabled",true);
        $("#caCrtSpan").hide();
        $("#nodeCrtFile").attr("disabled",true);
        $("#nodeCrtSpan").hide();
        $("#nodeKeyFile").attr("disabled",true);
        $("#nodeKeySpan").hide();
        $("#clientKeyStoreFile").attr("disabled",true);
        $("#clientKeyStoreSpan").hide();
    }
    
    $("#confirmMessage1Btn").click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
		$("#modal-confirm-message1").modal("hide");
	})
});