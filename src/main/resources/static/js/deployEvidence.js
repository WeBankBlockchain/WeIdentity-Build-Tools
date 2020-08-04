$(document).ready(function(){
	var hashChildGroup = false;
	$("#depolyBtn").click(function(){
    	if (!isReady) {
    		showMessageForNodeException();
        } else {
        	//判断是否有子群组
        	if (hashChildGroup) {
        		$("#modal-evidence-deploy").modal();
        	} else {
        		$("#messageBody").html("<p>当前“WeIdentity 部署工具”连接的区块链节点只加入了一个区块链群组，且这个群组是主群组。我们不支持在主群组上额外部署 Evidence 智能合约。</p>");
    	       	$("#modal-message").modal();
        	}
        }
    });
	if (!isReady) {
    	return;
    }
    loadData();
    //加载issuerTypeList
    $("#groupId").loadSelect("getAllGroup/true","value", "value",function(data){
    	if (data.length > 0) {
    		hashChildGroup = true;
    	}
    });
    
    var isClose = false;
    $("#evidenceDeployBtn").click(function(){
    	var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        var btnValue = $($this).html();
        $($this).html("部署中,  请稍等...");
        isClose = false;
    	var formData = {};
	    formData.groupId = $("#groupId").val();
	    $("#confirmMessage1Body").html("<p>存证部署中...</p>");
	    $("#confirmMessage1Btn").addClass("disabled");
	    $("#modal-confirm-message1").modal();
    	$.post("deployEvidence", formData, function(value,status){
    		// 部署成功
           if (value != "") {
        	   $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>存证部署<span class='success-span'>成功</span>。</p>");
        	   checkFirstDeploy(value, formData.groupId);
        	   isClose = true;
           } else {
        	   $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>存证部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
        	   $("#confirmMessage1Btn").removeClass("disabled");
           }
           $($this).html(btnValue);
           $($this).removeClass("disabled");
           $("#modal-confirm-message1").modal();
        })
    });
    //关闭对话框
    $('#modal-confirm-message1').on('hide.bs.modal', function () {
		if (isClose) {
			$("#modal-evidence-deploy").modal("hide");
		}
	})
	$("#confirmMessage1Btn").click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
		$("#modal-confirm-message1").modal("hide");
	})
});

var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$('#example2').DataTable({
	      "paging": true,
	      "lengthChange": false,
	      "searching": true,
	      "serverSide": false,
	      "ordering": true,
	      "info": false,
	      "destroy": true,
	      "autoWidth": false,
	      "iDisplayLength": 7,
	      "order": [[ 5, "desc" ], [ 4, "desc" ]],
	      "oLanguage": {
	    	  "sZeroRecords": "对不起，查询不到任何相关数据",
	    	  "oPaginate": {
	            "sFirst":    "第一页",
	            "sPrevious": " 上一页 ",
	            "sNext":     " 下一页 ",
	            "sLast":     " 最后一页 "
	          } 
	      },
	      "sAjaxSource":"getShareList",
	      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
	    	oSettings.jqXHR = $.ajax({
	    		  "dataType": 'json',
	    		  "type": "GET",
	    		  "url": sSource,
	    		  "data": aoData,
	    		  "success": function(data) {
	    			  var ndata = {};//返回的数据需要固定格式，否则datatables无法解析，所以需要重新组装
	    			  ndata.data = data;
	    			  ndata.recordsTotal = data.lenght;
	    			  ndata.recordsFiltered = ndata.lenght;
	    			  fnCallback(ndata);
	    		  }
	         });
	      },
	      columns:[
	          {"render": function ( data, type, full, meta) {
	        	  return "<a href='javascript:showDeploy(\"" + full.hash +"\", \"" + full.owner + "\")'>" + full.hashShow + "</a>"
	          }},
	          {"render": function ( data, type, full, meta) {
	        	  return "<a href='javascript:showWeId(\"" + full.owner + "\")'>" + full.ownerShow + "</a>"
	          }},
	          {"render": function ( data, type, full, meta) {
	        	  if (full.issuer != null) {
	        		  var name = full.issuer.name;
	        		  if (full.issuer.recognized) {
	        			  name += "&nbsp;<image src='dist/img/recognize.svg' widht='50' height='50'/>"
	        		  } else {
	        			  name += "&nbsp;<image src='dist/img/deRecognize.svg' widht='50' height='50'/>" 
	        		  }
	        		  return name
	        	  }
	        	  return ""
	          }},
	          {"render": function ( data, type, full, meta) {
	        	  return full.showGroupId
	          }},
	          {"render": function ( data, type, full, meta) {
	        	  return full.createTime
	          }},
	          {"render": function ( data, type, full, meta) {
	        	  var btnHtml = "";
	        	  if (full.enable) {
	        		  btnHtml += "<button type='button' name='cnsEnableBtn'  class='btn btn-inline btn-primary btn-flat ' disabled>已启用</button>&nbsp;&nbsp;";
	        	  } else {
	        		  btnHtml += "<button type='button' name='cnsEnableBtn' onclick='enableHash(\"" + full.hash + "\" , \"" + full.groupId + "\")' class='btn btn-inline btn-primary btn-flat'>启用</button>&nbsp;&nbsp;";
	        	  }
	        	  btnHtml += "<button type='button' name='cnsRemoveBtn' onclick='removeHash(\"" + full.hash + "\", this)'   class='btn btn-inline btn-primary btn-flat'>删除</button>&nbsp;&nbsp;";
	        	  return btnHtml
	          }}
	        ]
	    });
}

function checkFirstDeploy(hash, groupId) {
	$.get("isEnableEvidenceCns/" + groupId ,function(data,status){
		if (data == true) {//说明为首次部署，则调用启用逻辑
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用中，请稍等...</p>");
			enableEvidenHash(hash, groupId);
		} else {
			loadData();
			$("#confirmMessage1Btn").removeClass("disabled");
		}
	});
}

function processCnsBtn() {
	$("button[name='cnsEnableBtn']").each(function(){
		var index = $(this).attr("class").indexOf("true");
		if(index > 0) {
			$(this).attr("disabled",true);
			$(this).html("已启用");
		}
	})
}

function removeHash(hash, obj) {
	$.confirm("是否确定删除该CNS数据?",function() {
		var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    $(obj).html("删除中...");
		$("#confirmMessage1Body").html("<p>CNS删除中，请稍等...</p>");
		$("#confirmMessage1Btn").addClass("disabled");
		$("#modal-confirm-message1").modal();
		$.get("removeHash/" + hash + "/2", function(value,status){ 
			if (value == "success") {
				$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS删除<span class='success-span'>成功</span>。</p>");
				loadData();
			} else if (value == "fail") {
	        	 $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS删除<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        } else {
	        	 $("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>"+value+"</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        }
			$("#confirmMessage1Btn").removeClass("disabled");
			$("#modal-confirm-message1").modal();
		});
    })
}

function enableHash(hash, groupId) {
	var showHash = "..." + hash.substring(hash.length - 6);
	var message = "是否确定在此群组（group ID : " + groupId + "）中使用这个 Evidence 合约地址? "
			+"<br/>* 启用后将直接同步到应用环境（如果是生产环境，请特别小心）。"
			+"<br/>* 每一个群组，只能有一个启用的 Evidence 合约（调用EvidenceService的createEvidence会将Evidence写入这个 Evidence 合约）。"
			+"<br/>* 不同群组，可以启用不同的 Evidence 合约。";
	$("#modal-confirm .modal-dialog").addClass("modal-lg");
	$.confirm(message,function() {
		$("#confirmMessage1Body").html("<p>CNS启用中，请稍等...</p>");
		$("#confirmMessage1Btn").addClass("disabled");
		$("#modal-confirm-message1").modal();
		enableEvidenHash(hash, groupId);
	});
}

function enableEvidenHash(hash, groupId) {
	var formData = {};
    formData.hash = hash;
    formData.groupId = groupId;
	$.post("enableShareCns", formData, function(value,status){
		if (value == "success") {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用<span class='success-span'>成功</span>。</p>");
			loadData();
		} else if (value == "fail") {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用<span class='fail-span'>失败</span>，请联系管理员。</p>");
		} else {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用<span class='fail-span'>失败</span>，原因：" + value + "</p>");
		}
		$("#confirmMessage1Btn").removeClass("disabled");
		$("#modal-confirm-message1").modal();
	});
}


var deployDivTemplate = $("#deployDiv").html();
function showDeploy(hash, weId) {
	 $.get("getShareInfo/" + hash, function(data,status){
		 data["owner"] = weId;
		 $("#deployDiv").renderData(deployDivTemplate, data);
		 var show = $("#otherInfo").attr("show");
		 if (show == "true") {
			 $("#otherInfo").show();
		 }
		 $("#modal-show-deploy").modal();
	 });
}