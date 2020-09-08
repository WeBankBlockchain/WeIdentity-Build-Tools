$(document).ready(function(){
	//获取配置
    $.get("loadConfig",function(data,status){
        $("#dbForm  #persistence_type").val(data.persistence_type);
        $("#dbForm  #mysql_address").val(data.mysql_address);
        $("#dbForm  #mysql_database").val(data.mysql_database);
        $("#dbForm  #mysql_username").val(data.mysql_username);
        $("#dbForm  #mysql_password").val(data.mysql_password);
        $("#dbForm  #redis_address").val(data.redis_address);
        $("#dbForm  #redis_password").val(data.redis_password);
        if (data.persistenceType == "mysql" || data.persistence_type == "redis") {
            dbDisplay(data.persistence_type);
        }
    });

    function dbDisplay(v) {
        if (v == "mysql") {
            $("#mysqlForm").show();
            $("#redisForm").hide();
        } else {
            $("#redisForm").show();
            $("#mysqlForm").hide();
        }
    }

    var persistenceType = $.trim($("#dbForm  #persistence_type").val());
    dbDisplay(persistenceType);

    $("#persistence_type").change(function(){
         var selected = $(this).children('option:selected').val();
//         console.log(selected);
         dbDisplay(selected);
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
	    var dbVersion = $.trim($("#dbForm  #persistence_type").val());
//	    console.log(dbVersion);
        formData.append("persistence_type", $.trim($("#dbForm  #persistence_type").val()));
        console.log(formData.get("persistence_type"));
	    if (dbVersion == "mysql") {
            formData.append("mysql_address", $.trim($("#dbForm  #mysql_address").val()));
            formData.append("mysql_database", $.trim($("#dbForm  #mysql_database").val()));
            formData.append("mysql_username", $.trim($("#dbForm  #mysql_username").val()));
            formData.append("mysql_password", $.trim($("#dbForm  #mysql_password").val()));
        } else {
	        formData.append("redis_address", $.trim($("#dbForm  #redis_address").val()));
            formData.append("redis_password", $.trim($("#dbForm  #redis_password").val()));
        }
        $("#confirmMessage1Body").html("<p>配置提交中,请稍后...</p>");
        $("#confirmMessage1Btn").addClass("disabled");
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
                } else {
                    $("#confirmMessage1Btn").removeClass("disabled");
                    console.log(res);
                }
                $("#modal-confirm-message1").modal();
            }
        })
    });
    function checkInput() {
        var dbVersion = $.trim($("#dbForm  #persistence_type").val());
        console.log(dbVersion);
        if (dbVersion == "mysql") {
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
    	} else {
            var redisAddress = $.trim($("#dbForm  #redis_address").val());
            if (redisAddress.length == 0) {
                return "请输入您的服务器地址";
            }
    	}
    	return null;
    }
    function checkForTimeout() {
    	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>配置检查中,请稍后...</p>");
    	setTimeout(checkpersistence,2000);
    }
    function checkpersistence() {
    	$.get("checkPersistence",function(data,status){
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
           $("#confirmMessage1Btn").removeClass("disabled");
        });
    }


    function disabledInput() {
    	$("#dbForm  #persistence_type").attr("disabled",true);
    	$("#dbForm  #mysql_address").attr("disabled", true);
        $("#dbForm  #mysql_database").attr("disabled", true);
        $("#dbForm  #mysql_username").attr("disabled", true);
        $("#dbForm  #mysql_password").attr("disabled", true);
        $("#dbForm  #redis_address").attr("disabled", true);
        $("#dbForm  #redis_password").attr("disabled", true);
    }

    $("#confirmMessage1Btn").click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
		$("#modal-confirm-message1").modal("hide");
	})
});