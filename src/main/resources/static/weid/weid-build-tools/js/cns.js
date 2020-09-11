$(document).ready(function(){
    $("#checkCnsBtn").click(function(){
    	var orgId = $.trim($("#orgId").val());
    	if (orgId.length == 0) {
    		$("#messageBody").html("<p>请输入机构Id.</p>");
            $("#modal-message").modal();
    		return;
    	}
    	$("#messageBody").html("<p>CNS检查中...</p>");
        $("#modal-message").modal();
        var $this = this;
        $.get("checkCns/" + orgId,function(data,status){
            if(data) {
                loadData(null);
            } else {
                $("#messageBody").html("<p>该机构未注册CNS。</p>");
                $("#modal-message").modal();
            }
        });
    });
    if (isReady) {
    	loadData(null);
    }
});

var template = $("#data-div").html();

function loadData(obj) {
	 //加载部署数据
	var message = "<p>数据加载中，请稍后...</p>";
	if (obj != null) {
		message = $("#messageBody").html() + message;
	}
    $("#messageBody").html(message);
    $("#modal-message").modal();
    $.get("getCnsList",function(data,status){
   	    if (data.length > 0) {
   	        $("#data-div").renderData(template,data);
   	        $("[cns-use=true]").css("backgroundColor","#EDEDED");
   	        $("[use-btn=true]").html("已启用").addClass("disabled");
   	        $("#modal-message").modal('hide');
   	        if(obj != null) {
   	        	$(obj).html("启用");
   	            $(obj).removeClass("disabled");
   	        }
   	    } else {
   	        $("#messageBody").html("<p>未加载到数据</p>");
   	    }
   })
}

function enableCns(obj, orgId) {
	var disabled = $(obj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
    $(obj).addClass("disabled");
	$(obj).html("启用中，请稍后...");
	$("#messageBody").html("<p>CNS启用中,请耐心等待....</p>");
    $("#modal-message").modal();
	 $.get("enableCns/" + orgId,function(data,status){
        if (data) {
            $("#messageBody").html($("#messageBody").html()+"<p>CNS启用成功.</p>");
    	} else {
    		$("#messageBody").html($("#messageBody").html()+"<p>CNS启用失败.</p>");
    	}
        $("#modal-message").modal();
        loadData(obj);
    })
}
