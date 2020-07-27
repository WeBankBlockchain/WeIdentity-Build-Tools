$(document).ready(function(){

	$('#setGroupId').click(function(){
		var $this = this;
		var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
		let val = $('#guide_groupID').find('option:selected').val()
    	const formData = {}
    	formData.groupId = val
    	$.post("setGroupId", formData, function(value,status){
			if (value) {
				$("#messageBody").html("<p>设置主群组ID<span class='success-span'>成功</span></p>");
				$("#messageBody").html($("#messageBody").html() + "<p><span class='success-span'>目前暂不支持动态修改，如修改配置请重启服务生效。</span></p>");
			} else {
				$("#messageBody").html("<p>设置主群组ID<span class='fail-span'>失败</span></p>");
			}
			$("#modal-message").modal();
			$($this).removeClass("disabled");
		})
	})
	
	$("#guide_groupID").loadSelect("getAllGroup/false","value", "value",function(data){
    	$.get("loadConfig",function(data,status){
    		//获取设置的groupId
    		const id = data.group_id
    		const str = "option[value='"+ id +"']"
    		var selectOption = $("#guide_groupID").find(str)
    		selectOption.prop("selected",true);
    		$("#i-group").removeClass("fa-check-circle");
    	    $("#i-group").addClass("fa-circle");
    		if (selectOption.length == 1) {
    			$("#i-group").removeClass("fa-circle");
        	    $("#i-group").addClass("fa-check-circle");
    		}
	    })
	});
});



