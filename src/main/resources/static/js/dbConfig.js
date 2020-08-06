$(document).ready(function(){
	//获取配置
    $.get("loadConfig",function(data,status){
        $("#dbForm  #mysql_address").val(data.mysql_address);
        $("#dbForm  #mysql_database").val(data.mysql_database);
        $("#dbForm  #mysql_username").val(data.mysql_username);
        $("#dbForm  #mysql_password").val(data.mysql_password);
//        $.get("dbCheckState",function(data,status){
//            if(data) {//检查成功
//                disabledInput();
//            } else {
//            	$("#postBtnDiv").show();
//            }
//         });
    });
    
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
	    formData.append("mysql_address", $.trim($("#dbForm  #mysql_address").val()));
	    formData.append("mysql_database", $.trim($("#dbForm  #mysql_database").val()));
	    formData.append("mysql_username", $.trim($("#dbForm  #mysql_username").val()));
	    formData.append("mysql_password", $.trim($("#dbForm  #mysql_password").val()));
	    $("#confirmMessage1Body").html("<p>配置提交中,请稍后...</p>");
	    $("#confirmMessage1Btn").addClass("disabled");
	    $("#postBtn").addClass("disabled");
	    $("#modal-confirm-message1").modal();
	    $.ajax({
	        url:'submitDbConfig', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            console.log(res);
	            if (res=="success") {
	            	//检查节点是否正确
	            	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置提交<span class='success-span'>成功</span>, 检查准备中,请稍后...</p>");
	            	setTimeout(checkForTimeout,2000);
	            } else if (res=="fail") {
	            	 $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
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
    });
    function checkInput() {
    	var address = $.trim($("#dbForm  #mysql_address").val());
    	if (address.length == 0) {
    		return "请输入您的数据库地址";
    	}
    	var database = $.trim($("#dbForm  #mysql_database").val());
    	if (database.length == 0) {
    		return "请输入您的数据库名称";
    	}
    	var username = $.trim($("#dbForm  #mysql_username").val());
    	if (username.length == 0) {
    		return "请输入您的数据库用户名";
    	}
    	var password = $.trim($("#dbForm  #mysql_password").val());
    	if (password.length == 0) {
    		return "请输入您的数据库密码";
    	}
    	return null;
    }
    function checkForTimeout() {
    	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置检查中,请稍后...</p>");
    	$("#modal-confirm-message1").modal();
    	setTimeout(checkdb,2000);
    }
    
    function  checkdb() {
    	$.get("checkDb",function(data,status){
           if(data) {//检查成功
        	   $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置检查<span class='success-span'>成功</span>。</p>");
   			   $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p><span class='success-span'>目前暂不支持动态修改，如修改配置请重启服务生效。</span></p>");
        	   $("#configBtn").removeClass("disabled");
        	   //disabledInput();
        	   $("#i-db").removeClass("fa-circle");
               $("#i-db").addClass("fa-check-circle");
           } else {//检查失败
        	   $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
           }
           $("#modal-confirm-message1").modal();
           $("#postBtn").removeClass("disabled");
           $("#confirmMessage1Btn").removeClass("disabled");
        });
    }
    
    
    function disabledInput() {
    	$("#dbForm  #version").attr("disabled",true);
    	$("#dbForm  #mysql_address").attr("disabled", true);
        $("#dbForm  #mysql_database").attr("disabled", true);
        $("#dbForm  #mysql_username").attr("disabled", true);
        $("#dbForm  #mysql_password").attr("disabled", true);
    }

    $("#confirmMessage1Btn").click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
		$("#modal-confirm-message1").modal("hide");
	})
});
