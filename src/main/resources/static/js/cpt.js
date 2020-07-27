$(document).ready(function(){
	bsCustomFileInput.init();
    $("#openRegisterCpt").click(function(){
    	if (!isReady) {
    		showMessageForNodeException();
        } else {
        	$("#modal-register-cpt").modal();
        }
    });
    
    $("#cptToPojoBtn").click(function(){
    	if (!isReady) {
    		showMessageForNodeException();
    		return;
        }
    	var $this = this;
    	var cptIds = getCptIds();
        if (cptIds.length == 0) {
        	$("#messageBody").html("<p>请选择需要转换的凭证类型(CPT)</p>");
    		$("#modal-message").modal();
    		return;
        }
        
        $.confirm("确定将["+cptIds+"]转成Jar包吗?",function(){
            var disabled = $($this).attr("class").indexOf("disabled");
            if(disabled > 0) return;
            $($this).addClass("disabled");
            var btnValue = $($this).html();
            $($this).html("转换中,  请稍等...");
        	$.get("cptToPojo",{cptIds:cptIds},function(data,status){
        		if (data == "success") {
        			$("#messageBody").html("<p>凭证类型(CPT)转Jar包<span class='success-span'>成功</span>。</p>");
        		} else {
        			$("#messageBody").html("<p>凭证类型(CPT)转Jar包<span class='fail-span'>失败</span>，请查看后台日志。</p>");
        		}
        		$("#modal-message").modal();
        		$($this).html(btnValue);
        		$($this).removeClass("disabled");
        	})
        })
    });
    
    var registerCptResult = false;
    
    $('#modal-message').on('hide.bs.modal', function () {
    	$("#modal-message .modal-dialog").removeClass("modal-lg");
	})
	
	$('#modal-confirm-message').on('hide.bs.modal', function () {
		if (registerCptResult) {
			$("#modal-register-cpt").modal("hide");
		}
	})

	if (!isReady) {
    	return;
    }
    loadData();

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
    //注册CPT
    $("#registerCpt").click(function(){
    	var thisObj = this;
    	var disabled = $(thisObj).attr("class").indexOf("disabled");
        if(disabled > 0) return;
	    var cptJson = null;
	    try {
	    	cptJson = JSON.stringify(editor.get());
	    } catch (e) {
	    	$("#messageBody").html("<pre>Error：" + e.message + "</pre>");
    		$("#modal-message").modal();
    		return;
	    }
	    if (cptJson == null) {
	    	$("#messageBody").html("<p>凭证类型(CPT)不能为空</p>");
	    	$("#modal-message").modal();
	    	return;
	    }
	    var cptId = $("#nodeForm  #registerCptId").val();
	    var formData = new FormData();
	    formData.append("cptJson", cptJson);
	    formData.append("cptId", cptId);
	    var btnValue = $(thisObj).html();
	    $(thisObj).html("凭证类型(CPT)注册中，请稍后...");
	    $(thisObj).addClass("disabled");
	    $.ajax({
	        url:'registerCpt', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            console.log(res);
	            registerCptResult = false;
	            if (res=="success") {
	            	$("#confirmMessageBody").html("<p>凭证类型(CPT)注册<span class='success-span'>成功</span>。</p>");
	            	registerCptResult = true;
	            	loadData();
	            } else if (res=="fail") {
	            	$("#confirmMessageBody").html("<p>凭证类型(CPT)注册<span class='fail-span'>失败</span>，请查看服务端日志。</p>");
	            } else {
	            	$("#confirmMessageBody").html("<p>"+res+"</p>");
	            }
	            $(thisObj).html(btnValue);
	            $(thisObj).removeClass("disabled");
	            $("#modal-confirm-message").modal();
	        }
	    })
    })
    
    $('.template_btn').click(function(){
    	let cptJson = editor.getText()
    	try {
    		var $this = this;
    		var orginCode = $($this).next().find('pre').text();
	    	if (!cptJson || cptJson == '{}') {
	    		editor.setText(orginCode);
	    	} else {
	    		let code = JSON.stringify(JSON.parse(orginCode))
	    		cptJson = JSON.stringify(JSON.parse(cptJson))
	    		if (cptJson != code) {
	    			$.confirm("确认要覆盖下列代码片段?",function() {
		    			editor.setText(orginCode);
		    		})
	    		}
	    	}
    	} catch (e) {
	    	$("#messageBody").html("<pre>Error：" + e.message + "</pre>");
    		$("#modal-message").modal();
    		return;
	    }
		
	})
    
    
    function vaildFileName(fileName) {
    	var v = fileName.substring(fileName.lastIndexOf("."));
    	if (v != ".json" && v != ".JSON") {
    		return false;
    	}
    	return true;
    }
	$("#cptJsonFile").change(function(){
    	var file = $("#cptJsonFile")[0].files[0];
    	if (file == null || file == undefined) {
    		editor.setText("");
    	} else {
    		let reader = new FileReader();
            reader.readAsText(file, 'utf-8');
            reader.onload = function(e, rs) {
              editor.set(JSON.parse(e.target.result));
            };
    	}
    })
    
    $('#modal-register-cpt').on('hide.bs.modal', function () {
    	editor.setText("");
    	$("#cptJsonFile").val("");
		$(".custom-file-label").html("选择CPT文件...");
	})
	
	$("#selectAll").click(function(){
		var selectAllObj = $(this);
    	$("input[name=cptId]").each(function(){
			$(this).get(0).checked = $(selectAllObj).get(0).checked;
			clickCptId($(this).get(0), false);
		});
    	fixSelectAll();
    });
});
var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$.get("getCptInfoList",function(data,status){
  		if(table != null) {
  			table.destroy();
  		}
  		cptIds = new Array();
  		$("#data-tbody").renderData(template,data);
  		table = $('#example2').DataTable({
  	      "paging": true,
  	      "lengthChange": false,
  	      "searching": true,
  	      "ordering": true,
  	      "info": false,
  	      "autoWidth": false,
  	      "aoColumnDefs": [{ "bSortable": false, "aTargets": [ 0 , 6] }],
  	      "aaSorting": [[1, "asc"]],
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
  		processTable();
  		table.on('draw', function () {
  			processTable();
  		}); 
	})
}

function processTable() {
	$("#selectAll").get(0).checked = false;
	fixSelectAll();
}

function fixSelectAll() {
	var allCheck = true;
	if ($("input[name=cptId]").length == 0) {
		$("#selectAll").get(0).checked = false;
		return;
	}
	$("input[name=cptId]").each(function(){ 
		if (!$(this).get(0).checked) {
			allCheck = false;
		}
	})
	if (allCheck) {
		$("#selectAll").get(0).checked = true;
	} else {
		$("#selectAll").get(0).checked = false;
	}
}

function queryCptSchema(cptId) {
	$.get("queryCptSchema/"+cptId,function(data,status){
		$("#messageBody").html("<textarea class='form-control' rows='25' readOnly='readOnly' >"+data+"</textarea>");
		$("#modal-message .modal-dialog").addClass("modal-lg");
		$("#modal-message").modal();
	})
}

function downCpt(cptId) {
	$.confirm("确定下载该CPT吗?",function(){
		window.location.href="downCpt/" + cptId;
    })
}
Array.prototype.indexOf = function(val) { 
	for (var i = 0; i < this.length; i++) { 
		if (this[i] == val) return i; 
	} 
	return -1; 
}
Array.prototype.remove = function(val) { 
	var index = this.indexOf(val); 
	if (index > -1) { 
		this.splice(index, 1); 
	} 
};

var cptIds = new Array();
function clickCptId(thisObj, fix) {
	if(thisObj.checked){
		if (cptIds.indexOf(thisObj.value) == -1) {
			cptIds.push(thisObj.value);
		}
	} else {
		cptIds.remove(thisObj.value);
	}
	if (fix) {
		fixSelectAll();
	}
}
function getCptIds() {
	return cptIds;
}

