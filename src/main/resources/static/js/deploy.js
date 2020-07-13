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
    if (!isReady) {
    	return;
    }
    loadData();
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
        $("#messageBody").html("<p>合约部署中，请稍等...</p>");
        $("#modal-message").modal();
        $.get("deploy/" + chainId,function(value,status){
            if (value == "fail") {
            	$("#messageBody").html($("#messageBody").html() + "<p>合约部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
            	showBtn($this);
            } else {
            	$("#messageBody").html($("#messageBody").html() + "<p>合约部署<span class='success-span'>成功</span>。</p>");
            	checkFirstDeploy(value, $this);
            }
            $("#modal-message").modal();
        })
    });
    $('#modal-message').on('hide.bs.modal', function () {
    	if (isClose) {
    		$("#modal-deploy").modal("hide");
    	}
	})
});

var template = $("#data-tbody").html();
var  table;
function loadData() {
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
	$("#messageBody").html($("#messageBody").html() + "<p>CNS启用中，请稍等...</p>");
	$.get("enableHash/" + hash,function(value,status){
		if (value == "success") {
			$("#messageBody").html($("#messageBody").html() + "<p>CNS启用<span class='success-span'>成功</span>。</p>");
			if (deployCpt) {
				deploySystemCpt(hash, btnObj, enableBtn);
			} else {
				loadData();
			}
		} else {
			if (value == "fail") {
				$("#messageBody").html($("#messageBody").html() + "<p>CNS启用<span class='fail-span'>失败</span>，请联系管理员。</p>");
			} else {
				$("#messageBody").html($("#messageBody").html() + "<p>CNS启用<span class='fail-span'>失败</span>，原因:" + value + "。</p>");
			}
			showBtn(btnObj);
			showEnableBtn(enableBtn);
		}
		$("#modal-message").modal();
	});
}

function checkFirstDeploy(value, obj) {
	$.get("isEnableMasterCns",function(data,status){
		if (data == true) {//说明为首次部署，则调用启用逻辑
			enableHash(value, obj, true, null);
		} else {
			$("#messageBody").html($("#messageBody").html() + "<p><span class='success-span'>合约部署成功,请继续操作。</span></p>");
			guideSuccess();
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
		$("#messageBody").html("<p>CNS删除中，请稍等...</p>");
		$("#modal-message").modal();
		$.get("removeHash/" + hash + "/1", function(value,status){ 
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

function enable(hash, deployCpt, obj) {
	$.confirm("是否确定启用该主合约？",function() {
		var disabled = $(obj).attr("class").indexOf("disabled");
	    if(disabled > 0) return;
	    $(obj).addClass("disabled");
	    $(obj).html("启用中...");
	    $("#messageBody").html("");
		$("#modal-message").modal();
		enableHash(hash, null, deployCpt, obj);
	})
}

function deploySystemCpt(hash, deployBtn, enableBtn) {
	$("#messageBody").html($("#messageBody").html() + "<p>系统CPT部署中，请稍等...</p>");
	$.get("deploySystemCpt/" + hash,function(value,status){
		if (value) {
			$("#messageBody").html($("#messageBody").html() + "<p>系统CPT部署<span class='success-span'>成功</span>。</p>");
			$("#messageBody").html($("#messageBody").html() + "<p><span class='success-span'>合约部署成功,请继续操作。</span></p>");
			guideSuccess(); 
			loadData();
		} else {
			$("#messageBody").html($("#messageBody").html() + "<p>系统CPT部署<span class='fail-span'>失败</span>，请联系管理员。</p>");
			showEnableBtn(enableBtn);
		}
		showBtn(deployBtn);
	});
}

function deployCpt(hash, obj) {
	var disabled = $(obj).attr("class").indexOf("disabled");
    if(disabled > 0) return;
    $(obj).addClass("disabled");
    $(obj).html("部署中...");
    $("#messageBody").html("");
	$("#modal-message").modal();
	deploySystemCpt(hash, null, obj);
}

function showBtn(btnObj) {
	if (btnObj != null) {
		$(btnObj).html("主群组部署合约");
	    $(btnObj).removeClass("disabled");
	    $("#modal-message").modal();
	}
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

function guideSuccess () {
	sessionStorage.setItem('has_guide', '1')
	$('.container-fluid').show()
	$('.content-header').show()
	$('.guild-step').hide()
	$('.menu-item').show()
	$('.menu-title-deploy').css('display', 'block')
	$('.menu-title-async').css('display', 'block')
}
