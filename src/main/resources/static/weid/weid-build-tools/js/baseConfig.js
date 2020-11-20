$(document).ready(function(){
    
    //加载配置
    $.get("loadConfig",function(data,status){
        //节点配置
        $("#nodeForm  #orgId").val(data.org_id);
        $("#nodeForm  #version").val(data.blockchain_fiscobcos_version);
        if (data.blockchain_fiscobcos_version == "2") {
            $("#nodeForm  #version").val("FISCO-BCOS 2.X");
        } else if (data.blockchain_fiscobcos_version == "1") {
            $("#nodeForm  #version").val("FISCO-BCOS 1.X");
        }
        $("#nodeForm  #ipPort").val(data.blockchain_address);
        $("#nodeForm  #chainId").val(data.chain_id);
        if(data.blockchain_address.length == 0 || data["ca.crt"] == "false") {
            $("#nodeTest").addClass("disabled");
        }
        //数据库配置
        $("#dbForm  #mysql_address").val(data.mysql_address);
        $("#dbForm  #mysql_database").val(data.mysql_database);
        $("#dbForm  #mysql_username").val(data.mysql_username);
        if (data.mysql_address.length == 0 
            || data.mysql_database.length == 0 
            || data.mysql_username.length == 0) {
            $("#dbTest").addClass("disabled");
        }
    });

    $("#nodeTest").click(function() {
        var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var thisBtn = this;
        $(thisBtn).addClass("disabled");
        $(thisBtn).html("> 测试中...");
        $.get("checkNode",function(data,status){
           if(data) {//检查成功
               $("#checkBody").html("<p>配置检查成功。</p>");
           } else {//检查失败
               $("#checkBody").html("<p>配置检查失败。</p>");
           }
           $("#modal-default").modal();
           $(thisBtn).removeClass("disabled");
           $(thisBtn).html("测试");
        });
    });
    
    $("#dbTest").click(function() {
        var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var thisBtn = this;
        $(thisBtn).addClass("disabled");
        $(thisBtn).html("测试中...");
        $.get("checkDb",function(data,status){
           if(data) {//检查成功
               $("#checkBody").html("<p>配置检查成功。</p>");
           } else {//检查失败
               $("#checkBody").html("<p>配置检查失败。</p>");
           }
           $("#modal-default").modal();
           $(thisBtn).removeClass("disabled");
           $(thisBtn).html("测试");
        });
    });
    
    $("#downConfig").click(function() {
        var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $(this).html("配置检查下载中...");
        $(this).addClass("disabled");
        checkConfig(true, true);
    });
    function checkConfig(isDown,alert) {
    	//检查node
        $.get("checkNode",function(data,status){
            if(data) {//检查成功
           	 $("#nodeEdit").addClass("disabled");
               //检查DB
                $.get("checkDb",function(data,status){
                    if(data) {//检查成功
	                    if(isDown) {
	                        window.location.href="downConfig";
	                    }
                   	    $("#dbEdit").addClass("disabled");
                    } else {//检查失败
                        if(alert) {
                            $("#checkBody").html("<p>数据库配置检查失败，请确认配置是否正确。</p>");
                            $("#modal-default").modal();	
                        }
                    }
                    replayDownBtn();
                 });
            } else {//检查失败
                if(alert) {
                    $("#checkBody").html("<p>节点配置检查失败，请确认配置是否正确。</p>");
                    $("#modal-default").modal();
                }
                replayDownBtn();
            }
       });
    }
    
    function replayDownBtn() {
    	$("#downConfig").html("下载配置文件");
    	$("#downConfig").removeClass("disabled");
    }
    
    checkConfig(false,false);
    
    $.get("isDownFile",function(data,status){
		if(data) {
			$("button[downFile='file']").each(function(){
				$(this).css("display","inline-block");
	  		})
		}
	})
});
