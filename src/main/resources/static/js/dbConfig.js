$(document).ready(function(){
	var dbReady = false;
	//获取配置
    $.get("loadConfig",function(data,status){
        $("#dbForm  #mysql_address").val(data.mysql_address);
        $("#dbForm  #mysql_database").val(data.mysql_database);
        $("#dbForm  #mysql_username").val(data.mysql_username);
        $("#dbForm  #mysql_password").val(data.mysql_password);
    });
    
    //提交配置
    $("#postBtn").click(function(){
    	if (dbReady) {
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
	    formData.append("mysql_address", $.trim($("#dbForm  #mysql_address").val()));
	    formData.append("mysql_database", $.trim($("#dbForm  #mysql_database").val()));
	    formData.append("mysql_username", $.trim($("#dbForm  #mysql_username").val()));
	    formData.append("mysql_password", $.trim($("#dbForm  #mysql_password").val()));
	    $("#checkBody").html("<p>配置提交中,请稍后...</p>");
	    $("#modal-default").modal();
	    $("#configBtn").addClass("disabled");
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
	            	$("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='success-span'>成功</span>, 检查准备中,请稍后...</p>");
	            	setTimeout(checkForTimeout,2000);
	            } else if (res=="fail") {
	            	 $("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            } else {
	                console.log(res);
	            }
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
    	$("#checkBody").html($("#checkBody").html() + "<p>配置检查中,请稍后...</p>");
    	setTimeout(checkdb,2000);
    }
    
    function  checkdb() {
    	$.get("checkDb",function(data,status){
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
    $.get("dbCheckState",function(data,status){
        if(data) {//检查成功
            $("#checkBody").html("<p>当前配置正常，无需再次配置。</p>");
            $("#modal-default").modal();
            disabledInput();
            setTimeout(step2,150);
        } else {
        	step1();
        }
     });
    
    function disabledInput() {
    	$("#dbForm  #mysql_address").attr("disabled", true);
        $("#dbForm  #mysql_database").attr("disabled", true);
        $("#dbForm  #mysql_username").attr("disabled", true);
        $("#dbForm  #mysql_password").attr("disabled", true);
        $("#configBtn").removeClass("disabled");
        $("#i-db").removeClass("fa-circle");
        $("#i-db").addClass("fa-check-circle");
			dbReady = true;
			// 1 - 需要配置公私钥 
			const guide_weid = '1'
			if (guide_deployment === '1') {
				sessionStorage.setItem('guide_step', '4')
				window.location.href = './index.html'
			} else {
				sessionStorage.setItem('guide_step', '5')
				window.location.href = './deploy.html'
			}
    }
    
    function step1() {
    	if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({
			onSkip:function(){
				$.cookie("skip",true);
			}
		});
		var enjoyhint_script_steps = [{
		    'next #dbFormDiv': "请完成配置，并点击' 下一步'",
		    'showSkip': false,
		    'nextButton': {
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
		var enjoyhint_instance = new EnjoyHint({
			onSkip:function(){
				$.cookie("skip",true);
			}
		});
		var enjoyhint_script_steps = [{
		    'click #configBtn': '下一步，前往合约部署。',
		    'showSkip': false
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
});
function goConfig(thiObj) {
	$.get("dbCheckState",function(data,status){
        if(data) {//检查成功
     	   goTo(thiObj, "deploy.html");
        }
     });
}