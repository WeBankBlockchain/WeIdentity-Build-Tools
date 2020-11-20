$(document).ready(function(){
    //获取配置
    $.get("loadConfig",function(data,status){
            $("#persistence_type").val(data.persistence_type);
            $("#persistence_type").attr("disabled","disabled");
        });
	$('#verifyPersistence').click(function(){
		var $this = this;
		var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
		let val = $('#persistence_type').find('option:selected').val()
		console.log(val)
    	var formData = new FormData();
    	formData.persistenceType = val;
    	$("#messageBody").html("<p>> 配置提交中...</p>");
    	$.post("verifyPersistence", {persistenceType : val}, function(value,status){
			if (value) {
				$("#messageBody").html($("#messageBody").html() + "<p>> 检查数据库配置<span class='success-span'>成功</span></p>");
				$("#messageBody").html($("#messageBody").html() + "<p><span class='success-span'>目前暂不支持动态修改，如修改配置请重启服务生效。</span></p>");
			    $("#i-verifyDb").removeClass("fa-circle");
                $("#i-verifyDb").addClass("fa-check-circle");
			} else {
				$("#messageBody").html("<p>> 检查数据库配置<span class='fail-span'>失败</span></p>");
			}
			$("#modal-message").modal();
			$($this).removeClass("disabled");
		})
	})
	
//	$("#persistence_type").loadSelect("getAllGroup/false","value", "value",function(data){
//    	$.get("loadConfig",function(data,status){
//    		//获取存储的数据库类型
//    		const type = data.persistence_type
//    		const str = "option[value='"+ type +"']"
//    		var selectOption = $("#db_verify").find(str)
//    		selectOption.prop("selected",true);
//    		$("#i-verify").removeClass("fa-check-circle");
//    	    $("#i-verify").addClass("fa-circle");
//    		if (selectOption.length == 1) {
//    			$("#i-verify").removeClass("fa-circle");
//        	    $("#i-verify").addClass("fa-check-circle");
//    		}
//	    })
//	});
});



