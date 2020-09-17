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
    
    $("#downPolicy").click(function(){
    	var pojoId = $("#pojoId").val();
    	var fromType = $("#fromType").val();
    	var cptIds = [];
        $('input[name="cptId"]:checked').each(function(){
        	cptIds.push($(this).val());
        });
        if (cptIds.length == 0) {
        	$("#messageBody").html("<p>请选择需要包含的CPT</p>");
    		$("#modal-message").modal();
    		return;
        }
        var policyType= $("#policyType").val();
        var policyId = $("#policyId").val();
        var param = "pojoId="+pojoId +"&policyType="+policyType+"&policyId="+policyId;
        var cptId_value = "";
        for (var i = 0; i < cptIds.length; i++) {
        	cptId_value += cptIds[i]+",";
		}
        cptId_value = cptId_value.substring(0, cptId_value.length - 1);
        param += "&cptIds=" + cptId_value;
        param += "&fromType=" + fromType;
        window.location.href="downPolicy?" + param;
        $("#modal-down-policy").modal("hide");
    })
    //json编辑器
    var options = {
		mode: 'code',
		modes: ['code', 'tree'], // allowed modes
		onError: function (err) {
			alert(err.toString());
		}
	};
	var editor = new JSONEditor(jQuery("#jsonContent").get(0), options);
	editor.setText("");
    $("#policyJsonFile").change(function(){
    	var file = $("#policyJsonFile")[0].files[0];
    	if (file == null || file == undefined) {
    		editor.setText("");
    	} else {
    		let reader = new FileReader();
            reader.readAsText(file, 'utf-8');
            reader.onload = function(e, rs) {
              editor.setText(e.target.result);
            };
    	}
    })
    var isClose = false;
    $("#policyToPojo").click(function(){
    	try {
    		var thisObj = this;
    		var disabled = $(thisObj).attr("class").indexOf("disabled");
            if(disabled > 0) return;
    		var policy = editor.getText();
    		var formData = new FormData();
		    formData.append("policy", policy);
		    var btnValue = $(thisObj).html();
		    $(thisObj).html("转换中，请稍后...");
		    isClose = false;
		    $(thisObj).addClass("disabled");
    		$.ajax({
    	        url:'policyToPojo', /*接口域名地址*/
    	        type:'post',
    	        data: formData,
    	        contentType: false,
    	        processData: false,
    	        success:function(res) {
    	        	if (res=="success") {
    	            	$("#confirmMessageBody").html("<p>披露策略转Jar包<span class='success-span'>成功</span>。</p>");
    	            	loadData();
    	            	isClose = true;
    	            } else if (res=="fail") {
    	            	$("#confirmMessageBody").html("<p>披露策略转Jar包<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
    	            } else {
    	            	$("#confirmMessageBody").html("<p>"+res+"</p>");
    	            }
    	        	$("#modal-confirm-message").modal();
    	        	$(thisObj).html(btnValue);
    	            $(thisObj).removeClass("disabled");
    	        }
    	    })
    	} catch (e) {
    		$("#messageBody").html("<pre>Error：" + e.message + "</pre>");
    		$("#modal-message").modal();
    	}
    })
    
    $('#modal-confirm-message').on('hide.bs.modal', function () {
		if (isClose) {
			$("#modal-policy-to-pojo").modal("hide");
		}
	})
	$('#modal-policy-to-pojo').on('hide.bs.modal', function () {
    	editor.setText("");
    	$("#policyJsonFile").val("");
		$(".custom-file-label").html("选择策略文件...");
	})
});
var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$.get("getPojoList",function(data,status){
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
	})
}

function downCptJar(pojoId) {
	$.confirm("确定下载该Jar包吗?",function(){
		window.location.href="downPojoJar/" + pojoId;
    })
}
var cptId_checkbox_template = $("#cptId_Div").html();
function downPolicy(pojoId,cptIdstr,fromType) {
	$("#pojoId").val(pojoId);
	$("#fromType").val(fromType);
	var cptIds = cptIdstr.split(",");
	var data = [];
	for (var i = 0; i < cptIds.length; i++) {
		var param = {};
		param["cptId"] = cptIds[i];
		data.push(param);
	}
	$("#cptId_Div").renderData(cptId_checkbox_template,data);
    $("#modal-down-policy").modal();
}

