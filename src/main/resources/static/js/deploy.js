$(document).ready(function(){
    $("#depolyBtn").click(function(){
        var $this = this;
        $.get("isReady",function(data,status){
            if(data) {
            	$("#modal-deploy").modal();
            } else {
                $("#messageBody").html("<p>配置未准备完成，不可部署</p>");
                $("#modal-message").modal();
            }
        });
    });
    var url = window.location.pathname;
    url = url.substring(1);
    if (url == "deploy.html") {
    	if (!isReady) {
        	return;
        }
        loadData();
    }
    
    var isClose = true;
    $("#mainDeploy").click(function(){
    	var chainId = $("#chainId").val();
    	if (chainId == "") {
    		isClose = false;
    		$("#messageBody").html("<p>请输入chainId!</p>");
            $("#modal-message").modal();
            return;
    	}
    	isClose = true;
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        $($this).html("合约部署中,  请稍等...");
        $("#confirmMessage1Body").html("<p>合约部署中，请稍等...</p>");
        $("#confirmMessage1Btn").addClass("disabled");
        $("#modal-confirm-message1").modal();
        $.get("deploy/" + chainId,function(value,status){
            if (value == "fail") {
            	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>合约部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
            	showBtn($this);
            } else {
            	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>合约部署<span class='success-span'>成功</span>。</p>");
            	checkFirstDeploy(value, $this);
            }
            $("#modal-confirm-message1").modal();
        })
    });
    $('#modal-confirm-message1').on('hide.bs.modal', function () {
    	if (isClose) {
    		$("#modal-deploy").modal("hide");
    	}
	})
	$("#confirmMessage1Btn").click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
		if (url != "deploy.html") {
			window.location.href="deploy.html";
    	}
		$("#modal-confirm-message1").modal("hide");
	})
});

var template = $("#data-tbody").html();
var  table;
function loadData() {
	var url = window.location.pathname;
    url = url.substring(1);
    if (url == "guide.html") {
       return;
    }
	 //加载部署数据
	$.get("getDeployList",function(data,status){
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
  	      "order": [[ 3, "desc" ], [ 4, "desc" ]],
	  	  "aoColumnDefs": [
	          { "sType": "operation-column", "aTargets": [6] },    //指定列号使用自定义排序
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
   		
   		$.get("isDownFile",function(data,status){
			if(data) {
				$("button[downFile='file']").each(function(){
					$(this).css("display","inline-block");
		  		})
			}
		})
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
	$("button[name='cnsRemoveBtn']").each(function(){
		var index = $(this).attr("class").indexOf("true");
		if(index > 0) {
			$(this).attr("disabled",true);
		}
	})
	
	$("button[name='cnsDeploSystemCptBtn']").each(function(){
		var index = $(this).attr("class").indexOf("true");
		if(index > 0) {
			$(this).show();
		}
	})
}

function downEcdsaKey(id) {
	window.location.href="downEcdsaKey/" + id;
}

function enableHash(hash, btnObj, deployCpt, enableBtn) {
	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用中，请稍等...</p>");
	$.get("enableHash/" + hash,function(value,status){
		if (value == "success") {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用<span class='success-span'>成功</span>。</p>");
			if (deployCpt) {
				deploySystemCpt(hash, btnObj, enableBtn);
			} else {
				loadData();
				$("#confirmMessage1Btn").removeClass("disabled");
			}
		} else {
			if (value == "fail") {
				$("#confirmMessage1Body").html($("confirmMessage1Body").html() + "<p>CNS启用<span class='fail-span'>失败</span>，请联系管理员。</p>");
			} else {
				$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS启用<span class='fail-span'>失败</span>，原因:" + value + "。</p>");
			}
			showBtn(btnObj);
			showEnableBtn(enableBtn);
			$("#confirmMessage1Btn").removeClass("disabled");
		}
		$("#modal-confirm-message1").modal();
	});
}

function checkFirstDeploy(value, obj) {
	$.get("isEnableMasterCns",function(data,status){
		if (data == true) {//说明为首次部署，则调用启用逻辑
			enableHash(value, obj, true, null);
		} else {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p><span class='success-span'>合约部署成功,请继续操作。</span></p>");
			loadData();
			showBtn(obj);
		}
	});
}

function removeHash(hash, obj) {
	$.confirm("是否确定删除该CNS数据?",function(){
		var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    $(obj).html("删除中...");
	    $("#confirmMessage1Btn").addClass("disabled");
		$("#confirmMessage1Body").html("<p>CNS删除中，请稍等...</p>");
		$("#modal-confirm-message1").modal();
		$.get("removeHash/" + hash + "/1", function(value,status){ 
			if (value == "success") {
				$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>CNS删除<span class='success-span'>成功</span>。</p>");
				loadData();
			} else if (value == "fail") {
	        	 $("#confirmMessage1Body").html("<p>CNS删除<span class='fail-span'>失败</span>，请联系管理员。</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        } else {
	        	 $("#confirmMessage1Body").html("<p>"+value+"</p>");
	        	 $(obj).removeClass("disabled");
	     	     $(obj).html("删除");
	        }
			$("#confirmMessage1Btn").removeClass("disabled");
			$("#modal-confirm-message1").modal();
		});
    })
}

function enable(hash, deployCpt, obj) {
	$.confirm("是否确定启用该主合约？",function() {
		var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    $(obj).html("启用中...");
	    $("#confirmMessage1Body").html("");
	    $("#confirmMessage1Btn").addClass("disabled");
		$("#modal-confirm-message1").modal();
		enableHash(hash, null, deployCpt, obj);
	})
}

function deploySystemCpt(hash, deployBtn, enableBtn) {
	$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>系统CPT部署中，请稍等...</p>");
	$.get("deploySystemCpt/" + hash,function(value,status){
		if (value) {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>系统CPT部署<span class='success-span'>成功</span>。</p>");
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p><span class='success-span'>合约部署成功,请继续操作。</span></p>");
			loadData();
		} else {
			$("#confirmMessage1Body").html($("#confirmMessage1Body").html() + "<p>系统CPT部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
			showEnableBtn(enableBtn);
		}
		$("#confirmMessage1Btn").removeClass("disabled");
		showBtn(deployBtn);
	});
}

function deployCpt(hash, obj) {
	var disabled = $(obj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
    $(obj).addClass("disabled");
    $(obj).html("部署中...");
    $("#confirmMessage1Body").html("");
	$("#modal-confirm-message1").modal();
	deploySystemCpt(hash, null, obj);
}

function showBtn(btnObj) {
	if (btnObj != null) {
		$(btnObj).html("主群组部署合约");
	    $(btnObj).removeClass("disabled");
	    $("#modal-confirm-message1").modal();
	}
	 $("#confirmMessage1Btn").removeClass("disabled");
}

function showEnableBtn(btnObj) {
	if (btnObj != null) {
		$(btnObj).html("启用");
	    $(btnObj).removeClass("disabled");
	}
}
var deployDivTemplate = $("#deployDiv").html();
function showDeploy(hash, weId) {
	 $.get("getDeployInfo/" + hash, function(data,status){
		 data["owner"] = weId;
		 $("#deployDiv").renderData(deployDivTemplate, data);
		 var show = $("#otherInfo").attr("show");
		 if (show == "true") {
			 $("#otherInfo").show();
		 }
		 $.get("isDownFile",function(data,status){
			if(data) {
				$("button[downFile='file']").each(function(){
					var show = $(this).attr("show");
					if (show == "true") {
						$(this).css("display","inline-block");
					}
		  		})
			}
		})
		 $("#modal-show-deploy").modal();
	 });
}

function downResources() {
	$.confirm("确定下载该当前配置吗?",function(){
		window.location.href="downConfig";
    })
}
