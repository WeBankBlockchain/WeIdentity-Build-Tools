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
				$("#messageBody").html("<p>> 设置主群组ID<span class='success-span'>成功</span></p>");
				$("#messageBody").html($("#messageBody").html() + "<p><span class='success-span'>提示：目前暂不支持修改配置动态实时生效，修改配置需重启服务才能生效。</span></p>");
				loadData();
			} else {
				$("#messageBody").html("<p>> 设置主群组ID<span class='fail-span'>失败</span></p>");
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
	
	loadData();
});

function loadData() {
	//加载部署数据
	$('#example2').DataTable({
      "paging": false,
      "lengthChange": false,
      "searching": false,
      "serverSide": false,
      "ordering": false,
      "info": false,
      "destroy": true,
      "autoWidth": false,
      "pagingType":"simple",
      "oLanguage": {
    	  "sZeroRecords": "对不起，查询不到任何相关数据",
    	  "oPaginate": {
            "sFirst":    "第一页",
            "sPrevious": " 上一页 ",
            "sNext":     " 下一页 ",
            "sLast":     " 最后一页 "
          } 
      },
      "sAjaxSource":"getGroupMapping",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	oSettings.jqXHR = $.ajax({
    		  "dataType": 'json',
    		  "type": "GET",
    		  "url": sSource,
    		  "data": aoData,
    		  "success": function(data) {
    			  var ndata = {};//返回的数据需要固定格式，否则datatables无法解析，所以需要重新组装
    			  ndata.data = data;
    			  ndata.recordsTotal = data.length;
    			  ndata.recordsFiltered = ndata.recordsTotal;
    			  fnCallback(ndata);
    		  }
         });
      },
      columns:[
          { data: 'groupId'},
          { data: 'nodes'},
          { data: 'type'}
        ]
    });
}

