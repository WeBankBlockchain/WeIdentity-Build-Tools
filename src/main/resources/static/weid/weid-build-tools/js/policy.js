$(document).ready(function(){
	bsCustomFileInput.init();
	$("#policyToPojoBtn").click(function(){
		if (!isReady) {
			showMessageForNodeException();
	    } else {
	    	$("#modal-policy-to-pojo").modal();
	    }
    })
    
	if (!isReady) {
    	return;
    }
    loadData();
});

function loadData() {
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
      "iDisplayLength": 7,
      "oLanguage": {
    	  "sZeroRecords": "对不起，查询不到任何相关数据",
    	  "oPaginate": {
            "sFirst":    "第一页",
            "sPrevious": " 上一页 ",
            "sNext":     " 下一页 ",
            "sLast":     " 最后一页 "
          } 
      },
      "sAjaxSource":"getPolicyList",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	oSettings.jqXHR = $.ajax({
		  "dataType": 'json',
		  "type": "GET",
		  "url": sSource,
		  "data": aoData,
		  "success": function(data) {
			var ndata = {};//返回的数据需要固定格式，否则datatables无法解析，所以需要重新组装
			console.log(data)
			ndata.data = data.dataList;
			ndata.recordsTotal = data.allCount;
			ndata.recordsFiltered = ndata.recordsTotal;
			fnCallback(ndata);
		  }
        });
      },
      columns:[
          {data: 'id'},
          {"render": function ( data, type, full, meta) {
        	  var op = ""
        	  op += "<button type='button' onclick='queryPolicy(\"" + full.id + "\")' class='btn btn-inline btn-primary btn-flat'>预览Policy</button>&nbsp;";
        	  return op;
          }}
        ]
    });
}

function queryPolicy(cptId) {
	$.get("queryPolicy/"+cptId,function(data,status){
		$("#messageBody").html("<textarea class='form-control' rows='25' readOnly='readOnly' >"+data+"</textarea>");
		$("#modal-message .modal-dialog").addClass("modal-lg");
		$("#modal-message").modal();
	})
}
