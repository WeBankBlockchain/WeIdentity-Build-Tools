$(document).ready(function(){
	$('#dataTime').datetimepicker({
		    language:'zh-CN',
	        weekStart: 1,
	        todayBtn:  1,
			autoclose: 1,
			todayHighlight: 1,
			startView: 2,
			minView: 2,
			forceParse: 0,
			format:"yyyy-mm-dd"
	});
	if (!isReady || !isReadyForDb) {
		return;
    }
	query();
	queryAsyncStatus(false);
});

function query() {
	if (!isReadyForDb) {
		showConfigMessage();
		return;
    }
	if (!isReady) {
		showMessageForNodeException();
		return;
    }
	//加载部署数据
	$('#example2').DataTable({
      "paging": true,
      "lengthChange": false,
      "searching": false,
      "serverSide": true,
      "ordering": false,
      "info": false,
      "destroy": true,
      "autoWidth": false,
      "oLanguage": {
    	  "sZeroRecords": "对不起，查询不到任何相关数据",
    	  "oPaginate": {
            "sFirst":    "第一页",
            "sPrevious": " 上一页 ",
            "sNext":     " 下一页 ",
            "sLast":     " 最后一页 "
          } 
      },
      "sAjaxSource":"getAsyncList",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	var  time = $("#dataTime").val();
      	time = time.replace(/-/g,"");
      	if (time == "") {
      		time = 0;
      	}
      	var status = $("#status").val();
      	aoData.push({"name":"dataTime","value":time});
      	aoData.push({"name":"status","value":status});
    	oSettings.jqXHR = $.ajax({
    		  "dataType": 'json',
    		  "type": "POST",
    		  "url": sSource,
    		  "data": aoData,
    		  "success": function(data) {
    			  var ndata = {};//返回的数据需要固定格式，否则datatables无法解析，所以需要重新组装
    			  ndata.data = data.dataList;
    			  ndata.recordsTotal = data.allCount;
    			  ndata.recordsFiltered = ndata.recordsTotal;
    			  fnCallback(ndata);
    		  }
         });
      },
      columns:[
          { "render": function (data, type, full, meta) {
        	  return "<a href='asyncDetail.html?batch=" + full.data_time + "'>" + full.data_time+ "</a>";
          }},
          { "render": function (data, type, full, meta) {
        	  if (full.status == 1) {
                  return '处理中';
        	  } else if (full.status == 2) {
        		  return "处理成功";
        	  } else if (full.status == 3) {
        		  return "处理失败";
        	  } else {
        		  return "未知状态";
        	  } 
          }},
          { data: 'all_size' },
          { data: 'success_size' },
          { data: 'fail_size' },
          {"render": function ( data, type, full, meta) {
        	  if (full.status == 3) {
                  return '<button type="button" name="removeAuthBtn" onclick="reTry(this, ' + full.data_time + ')" class="btn btn-inline btn-primary btn-flat">重试</button>';
        	  } else {
        		  return "";
        	  }
          }}
        ]
    });
}

function reTry(thisObj, dataTime) {
	var disabled = $(thisObj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
	$.confirm("是否确定异步重试该批次?", function(){
        $(thisObj).addClass("disabled");
        $(thisObj).html("重试中...");
		var formData = {};
		formData.dataTime = dataTime;
		$.post("reTryAsyn", formData ,function(data,status){
			$("#messageBody").html("<p>异步处理中，请注意查看数据。</p>");
			$(thisObj).removeClass("disabled");
	        $(thisObj).html("重试");
			$("#modal-message").modal();
			query();
		})
	})
}

function queryAsyncStatus(show) {
	//检查是否开启异步上链t
	$.post("chekEnableAsync", {} ,function(data,staus){
		$("#asyncStatus").val(data);
		var message = ""
		if (data == true) {
			$("#doEnableAsyncBtn").html("禁用异步上链");
			message = "启用成功。";
		} else {
			$("#doEnableAsyncBtn").html("启用异步上链");
			message = "禁用成功。";
		}
		if (show) {
			$("#messageBody").html("<p>" + message + "</p>");
			$("#modal-message").modal();
		}
	});
}

function doEnableAsync(thisObj) {
	var disabled = $(thisObj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
    var status = $("#asyncStatus").val();
    var message = "是否确定启用异步上链?";
    var enable = true;
    if (status == "true") {
    	message = "是否确定禁用异步上链?";
    	enable = false;
    }
	$.confirm(message, function(){
        $(thisObj).addClass("disabled");
        $(thisObj).html("处理中...");
		var formData = {};
		formData.enable = enable;
		$.post("doEnableAsync", formData ,function(data,status){
			queryAsyncStatus(true);
			$(thisObj).removeClass("disabled");
		})
	})
}