$(document).ready(function(){
	$('#batch').datetimepicker({
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
	var dataTime = getQueryVariable("batch");
	if (dataTime == null) {
		$("#batch").datetimepicker("setDate", new Date());
	} else {
		var pattern = /(\d{4})(\d{2})(\d{2})/;
		var formatedDate = dataTime.replace(pattern, '$1-$2-$3');
		$("#batch").val(formatedDate);
	}
	if (!isReady) {
    	return;
    }
	query();
	$("#btn_supplement").click(function(){
		$.confirm("是否确定触发补录程序?", function(){
			var formData = {};
			$.post("supplement", formData ,function(data,status){
				if (data == true) {
					$("#messageBody").html("<p>补录触发成功, 请注意查看数据或日志。</p>");
				} else {
					$("#messageBody").html("<p>补录触发失败, 数据可能正在补录中。</p>");
				}
				$("#modal-message").modal();
			})
		});
	});

});

function getQueryVariable(variable){
   var query = window.location.search.substring(1);
   var vars = query.split("&");
   for (var i=0;i<vars.length;i++) {
       var pair = vars[i].split("=");
       if(pair[0] == variable){return pair[1];}
   }
   return null;
}

function query() {
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
      "sAjaxSource":"getBinLogList",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	var  batch = $("#batch").val();
    	batch = batch.replace(/-/g,"");
    	var status = $("#status").val();
    	aoData.push({"name":"batch","value":batch});
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
        	  var requestId = full.request_id;
        	  requestId = plusXing(requestId, 6, 6);
        	  var fullData = JSON.stringify(full);
        	  return "<a href='javascript:showDetail(" + fullData + ")'>" + requestId + "</a>";
          }},
          { data: 'transaction_method'},
          { "render": function (data, type, full, meta) {
        	  return getLocalTime(full.transaction_timestamp);
          }},
          { "render": function (data, type, full, meta) {
        	  if (full.status == 0) {
                  return '待上链';
        	  } else if (full.status == 1) {
        		  return "已上链";
        	  } else if (full.status == 2) {
        		  return "上链失败";
        	  } else {
        		  return "未知状态";
        	  } 
          }}
//          ,{"render": function ( data, type, full, meta) {
//        	  if (full.status == 2) {
//                  return '<button type="button" name="removeAuthBtn" onclick="reTryTransaction(this, ' + full.request_id + ')" class="btn btn-inline btn-primary btn-flat">重试</button>';
//        	  } else {
//        		  return "";
//        	  }
//          }}
        ]
    });
}
var detailDivTemplate = $("#detailDiv").html();
function showDetail(full) {
	full.transaction_time = getLocalTime(full.transaction_timestamp);
	$("#detailDiv").renderData(detailDivTemplate, full);
	$("#modal-show-detail").modal();
}

function getLocalTime(nS) {
	var  date = new Date(parseInt(nS) + 8 * 3600 * 1000);
    return date.toJSON().substr(0, 19).replace('T', ' ');
}

function plusXing (str,frontLen,endLen) {
	var len = str.length-frontLen-endLen;
	var xing = '...';
	return str.substring(0,frontLen)+xing+str.substring(str.length-endLen);
}

function reTryTransaction(thisObj, requestId) {
	var disabled = $(thisObj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
	$.confirm("是否确定重试该记录上链?", function(){
        $(thisObj).addClass("disabled");
        $(thisObj).html("重试中...");
		var formData = {};
		formData.requestId = requestId;
		$.post("reTryTransaction", formData ,function(data,status){
			if (data == true) {
				$("#messageBody").html("<p>上链成功。</p>");
				query();
			} else {
				$("#messageBody").html("<p>上链失败。</p>");
			}
			$(thisObj).removeClass("disabled");
	        $(thisObj).html("重试");
			$("#modal-message").modal();
		})
	})
}