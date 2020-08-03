$(document).ready(function(){
	
	if (!isReady) {
    	return;
    }
    loadData();
	
	var isClose = false;
    $("#registerIssuerBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var weId = $.trim($("#registerIssuerWeId").val());
        if (weId.length == 0) {
            $("#messageBody").html("<p>请输入WeID。</p>");
            $("#modal-message").modal();
            return;
        }
        var name = $.trim($("#registerIssuerName").val());
        if (name.length == 0) {
        	 $("#messageBody").html("<p>请输入权威机构名称。</p>");
        	 $("#modal-message").modal();
    		return;
    	}
        if (name.indexOf("@") == 0) {
        	$("#messageBody").html("<p>权威机构名称输入非法。</p>");
	       	$("#modal-message").modal();
	   		return;
        }
        $($this).addClass("disabled");
        $($this).html("注册中,  请稍等...");
        isClose = false;
        var formData = {};
	    formData.weId = weId;
	    formData.name = name;
        $.post("registerIssuer", formData ,function(value,status){
        	if (value == "success") {
                $("#confirmMessageBody").html("<p>注册<span class='success-span'>成功</span>。</p>");
                loadData();
                isClose = true;
            }  else if (value == "fail") {
            	 $("#confirmMessageBody").html("<p>注册<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	 $("#confirmMessageBody").html("<p>"+value+"</p>");
            }
            $($this).html("注册");
            $($this).removeClass("disabled");
            $("#modal-confirm-message").modal();
        })
    });
    
    $('#modal-confirm-message').on('hide.bs.modal', function () {
		if (isClose) {
			$("#modal-register-issue").modal("hide");
		}
	})
});
var template = $("#data-tbody").html();
var  table;
function loadData1() {
	 //加载部署数据
  $.get("getIssuerList",function(data,status){
		if(table != null) {
			table.destroy();
		}
		$("#data-tbody").renderData(template,data);
		table = $('#example2').DataTable({
	      "paging": true,
	      "lengthChange": false,
	      "searching": true,
	      "ordering": true,
	      "info": false,
	      "autoWidth": false,
	      "oLanguage": {
	    	  "sZeroRecords": "对不起，查询不到任何相关数据",
  	    	  "oPaginate": {
	            "sFirst":    "第一页",
	            "sPrevious": " 上一页 ",
	            "sNext":     " 下一页 ",
	            "sLast":     " 最后一页 "
	          }
	      }
	    });
		$("button[name='removeAuthBtn']").each(function(){
			var index = $(this).attr("class").indexOf("true");
			if(index < 0) $(this).attr("disabled",true);
		})
  })
}

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
      "sAjaxSource":"getIssuerList",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	oSettings.jqXHR = $.ajax({
    		  "dataType": 'json',
    		  "type": "GET",
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
          {"render": function ( data, type, full, meta) {
        	  return "<a href='javascript:showWeId(\""+ full.weId + "\")'>" + full.weIdShow + "</a>"
          }},
          { data: 'name'},
          {"render": function ( data, type, full, meta) {
        	  if(full.recognized) {
        		  return "<image src='dist/img/recognize.svg' widht='50' height='50'/>";
        	  }
        	  return "<image src='dist/img/deRecognize.svg' widht='50' height='50' />";
          }},
          { data: 'hashShow'},
          { "render": function (data, type, full, meta) {
        	  return getLocalTime(full.createTime * 1000);
          }},
          {"render": function ( data, type, full, meta) {
        	  var op = ""
        	  if(full.recognized) {
        		  op = "<button type='button' name='confirmAuthBtn' onclick='deRecognizeAuthorityIssuer(this,\"" + full.weId + "\")' class='btn btn-inline btn-primary btn-flat'>撤销认证</button>&nbsp;&nbsp;";
        	  } else {
        		  op = "<button type='button' name='confirmAuthBtn' onclick='recognizeAuthorityIssuer(this,\"" + full.weId + "\")' class='btn btn-inline btn-primary btn-flat'>认证</button>&nbsp;&nbsp;";
        	  }
        	  op += "<button type='button' name='removeAuthBtn' onclick='removeIssuer(this,\"" + full.weId + "\")' class='btn btn-inline btn-primary btn-flat'>删除</button>";
        	  
        	  return op;
          }}
        ]
    });
}

function registerIssuer() {
	if (!isReady) {
		showMessageForNodeException();
    } else {
    	$("#registerIssuerWeId").val("");
    	$("#registerIssuerName").val("");
        $("#modal-register-issue").modal();
    }
}

function removeIssuer(obj, weId) {
	$.confirm("是否确定删除该数据?",function(){
	    var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    $(obj).html("删除中,  请稍等...");
	    var formData = {};
	    formData.weId = weId;
		$.post("removeIssuer", formData, function(value,status){
			if (value == "success") {
	            $("#messageBody").html("<p>删除<span class='success-span'>成功</span>。</p>");
	            loadData();
	        }  else if (value == "fail") {
	        	 $("#messageBody").html("<p>删除<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        } else {
	        	 $("#messageBody").html("<p>"+value+"</p>");
	        }
	        $(obj).html("删除");
	        $(obj).removeClass("disabled");
	        $("#modal-message").modal();
	    });
	});
}

function deRecognizeAuthorityIssuer(obj, weId) {
	$.confirm("是否确定撤销该权威机构认证?",function(){
	    var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    var btnValue = $(obj).html();
	    $(obj).html("认证撤销中,  请稍等...");
	    var formData = {};
	    formData.weId = weId;
		$.post("deRecognizeAuthorityIssuer", formData, function(value,status){
			if (value == "success") {
	            $("#messageBody").html("<p>认证撤销<span class='success-span'>成功</span>。</p>");
	            loadData();
	        }  else if (value == "fail") {
	        	 $("#messageBody").html("<p>认证撤销<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        } else {
	        	 $("#messageBody").html("<p>"+value+"</p>");
	        }
	        $(obj).html(btnValue);
	        $(obj).removeClass("disabled");
	        $("#modal-message").modal();
	    });
	});
}

function  recognizeAuthorityIssuer(obj, weId) {
	$.confirm("是否确定认证该权威机构?",function(){
	    var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    var btnValue = $(obj).html();
	    $(obj).html("认证中,  请稍等...");
	    var formData = {};
	    formData.weId = weId;
		$.post("recognizeAuthorityIssuer", formData, function(value,status){
			if (value == "success") {
	            $("#messageBody").html("<p>认证<span class='success-span'>成功</span>。</p>");
	            loadData();
	        }  else if (value == "fail") {
	        	 $("#messageBody").html("<p>认证<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        } else {
	        	 $("#messageBody").html("<p>"+value+"</p>");
	        }
	        $(obj).html(btnValue);
	        $(obj).removeClass("disabled");
	        $("#modal-message").modal();
	    });
	});
}