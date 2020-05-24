$(document).ready(function(){
//    if (isReady) {
//    	loadData();
//    }
    loadData();
    $("#depolyBtn").click(function(){
    	$("#modal-evidence-deploy").modal();
    });
    
    //加载issuerTypeList
    $("#groupId").loadSelect("getAllGroup","value", "value",function(data){
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
    	$.post("deployEvidence", formData, function(value,status){
    		// 部署成功
           if (value) {
        	   $("#confirmMessageBody").html("<p>存证部署<span class='success-span'>成功</span>。</p>");
        	   loadData();
        	   isClose = true;
           } else {
        	   $("#confirmMessageBody").html("<p>存证部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
           }
           $($this).html(btnValue);
           $($this).removeClass("disabled");
           $("#modal-confirm-message").modal();
        })
    });
    //关闭对话框
    $('#modal-confirm-message').on('hide.bs.modal', function () {
		if (isClose) {
			$("#modal-evidence-deploy").modal("hide");
		}
	})
});

var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$.get("getShareList",function(data,status){
  		if(table != null) {
 			table.destroy();
 		}
 		$("#data-tbody").renderData(template,data);
 		table = $('#example2').DataTable({
 	      "paging": true,
 	      "iDisplayLength": 10,
 	      "lengthChange": false,
 	      "searching": true,
 	      "ordering": true,
 	      "info": false,
 	      "autoWidth": false,
 	      "order": [[ 2, "desc" ], [ 3, "desc" ]],
	  	  "aoColumnDefs": [
	          { "sType": "operation-column", "aTargets": [4] },    //指定列号使用自定义排序
	      ],
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
 		processCnsBtn();
 		table.on('draw', function () {
 			processCnsBtn();
 		}); 
  })
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
		$("#messageBody").html("<p>CNS删除中，请稍等...</p>");
		$("#modal-message").modal();
		$.get("removeHash/" + hash + "/2", function(value,status){ 
			if (value == "success") {
				$("#messageBody").html($("#messageBody").html() + "<p>CNS删除<span class='success-span'>成功</span>。</p>");
				loadData();
			} else if (value == "fail") {
	        	 $("#messageBody").html("<p>CNS删除<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        } else {
	        	 $("#messageBody").html("<p>"+value+"</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        }
			$("#modal-message").modal();
		});
    })
}

function enableHash(hash, groupId) {
	var showHash = "..." + hash.substring(hash.length - 6);
	var message = "是否确定启用群组" + groupId + "中的CNS[" + showHash +"]吗?<br />启用后将同步对应应用环境";
	$.confirm(message,function() {
		$("#messageBody").html("<p>CNS启用中，请稍等...</p>");
		$("#modal-message").modal();
		var formData = {};
	    formData.hash = hash;
	    formData.groupId = groupId;
		$.post("enableHash", formData, function(value,status){
			if (value) {
				$("#messageBody").html($("#messageBody").html() + "<p>CNS启用<span class='success-span'>成功</span>。</p>");
				loadData();
			} else {
				$("#messageBody").html($("#messageBody").html() + "<p>CNS启用<span class='fail-span'>失败</span>，请联系管理员。</p>");
			}
			$("#modal-message").modal();
		});
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