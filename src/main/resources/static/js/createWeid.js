var role = "3";
$(document).ready(function(){
	$("#showCreateBtn").click(function(){
    	if (!isReady) {
    		showMessageForNodeException();
        } else {
        	$("#modal-show-create-weid").modal();
        }
    });
	if (!isReady) {
    	return;
    }
    loadData();
    // 初始化控件
    bsCustomFileInput.init();
    role = getRole();
    $("#createBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        var btnValue = $($this).html();
        $($this).html("WeID创建中,  请稍等...");
        $.get("createWeId",function(value,status){
            if (value == "success") {
                $("#confirmMessageBody").html("<p>WeID创建<span class='success-span'>成功</span>。</p>");
                loadData();
                isClose = true;
            }  else if (value == "fail") {
            	 $("#confirmMessageBody").html("<p>WeID创建<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	 $("#confirmMessageBody").html("<p>" + value + "</p>");
            }
            $($this).html(btnValue);
            $($this).removeClass("disabled");
            $("#modal-confirm-message").modal();
        })
    });
    
    $("#craeteByPrivBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        var btnValue = $($this).html();
        $($this).html("WeID创建中,  请稍等...");
        var formData = new FormData();
        formData.append("privateKey", $("#privateKey")[0].files[0]);
		$.ajax({
	        url:'createWeIdByPrivateKey', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	        	if (res == "success") {
	                $("#confirmMessageBody").html("<p>WeID创建<span class='success-span'>成功</span>。</p>");
	                loadData();
	                isClose = true;
	            }  else if (res == "fail") {
	            	 $("#confirmMessageBody").html("<p>WeID创建<span class='fail-span'>失败</span>，请联系管理员。</p>");
	            } else {
	            	 $("#confirmMessageBody").html("<p>" + res + "</p>");
	            }
	            $($this).html(btnValue);
	            $($this).removeClass("disabled");
	            $("#modal-confirm-message").modal();
	        }
	    })
    });
    
    $("#craeteByPubBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        var btnValue = $($this).html();
        $($this).html("WeID创建中,  请稍等...");
        var formData = new FormData();
        formData.append("publicKey", $("#publicKey")[0].files[0]);
		$.ajax({
	        url:'createWeIdByPublicKey', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	        	if (res == "success") {
	                $("#confirmMessageBody").html("<p>WeID创建<span class='success-span'>成功</span>。</p>");
	                loadData();
	                isClose = true;
	            }  else if (res == "fail") {
	            	 $("#confirmMessageBody").html("<p>WeID创建<span class='fail-span'>失败</span>，请联系管理员。</p>");
	            } else {
	            	 $("#confirmMessageBody").html("<p>" + res + "</p>");
	            }
	            $($this).html(btnValue);
	            $($this).removeClass("disabled");
	            $("#modal-confirm-message").modal();
	        }
	    })
    });
    
    var isClose = false;
    $("#registerIssuerBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var weId = $("#registerIssuerWeId").val();
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
        var formData = {};
	    formData.weId = weId;
	    formData.name = name;
	    isClose = false;
        $.post("registerIssuer", formData, function(value,status){
            if (value == "success") {
                $("#confirmMessageBody").html("<p>权威凭证发行者注册<span class='success-span'>成功</span>。</p>");
                loadData();
                isClose = true;
            }  else if (value == "fail") {
            	 $("#confirmMessageBody").html("<p>权威凭证发行者注册<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	 $("#confirmMessageBody").html("<p>"+value+"</p>");
            }
            $($this).html("注册");
            $($this).removeClass("disabled");
            $("#modal-confirm-message").modal();
        })
    });
    
    //加载issuerTypeList
    $("#issuerType").loadSelect("getIssuerTypeList","type", "type",function(data){
    	if(data.length == 0) {
    		$("#registerSpecific_A").show();
    	}
    });
    
    $("#addToIssuerType").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var weId = $("#addIssuerWeId").val();
        var type= $("#issuerType").val();
        $($this).addClass("disabled");
        $($this).html("注册中,  请稍等...");
        isClose = false;
        var formData = {};
	    formData.weId = weId;
	    formData.issuerType = type;
        $.post("addIssuerIntoIssuerType", formData, function(value,status){
            if (value == "success") {
                $("#confirmMessageBody").html("<p>特定类型的发行者注册<span class='success-span'>成功</span>。</p>");
                loadData();
                isClose = true;
            }  else if (value == "fail") {
            	$("#confirmMessageBody").html("<p>特定类型的发行者注册<span class='fail-span'>失败</span>，请联系管理员。</p>");
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
			$("#modal-add-to-issueType").modal("hide");
			$("#modal-register-issue").modal("hide");
			$("#modal-show-create-weid").modal("hide");
			$("#modal-create-by-privateKey").modal("hide");
			$("#modal-create-by-publicKey").modal("hide");
		}
	})

	$('#modal-create-by-privateKey, #modal-create-by-publicKey').on('hide.bs.modal', function () {
		$("#privateKey").val("");
		$("#publicKey").val("");
		$("#privateKey-label").html("选择私钥文件...");
		$("#publicKey-label").html("选择公钥文件...");
	})
	
	$('#modal-message').on('hide.bs.modal', function () {
		if (isClose) {
			$("#modal-show-create-weid").modal("hide");
		}
    	
	})
	
//	$.get("getWeIdPath",function(value,status){
//       $("#weidDir").html("当前WeID存放路径: " + value);
//    })
});
var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$.get("getWeIdList",function(data,status){
  		if(table != null) {
  			table.destroy();
  		}
  		if (data.length > 0) {
  			for(var i = 0; i < data.length; i++) {
  				if (data[i].admin) {
  					data[i]["showAdmin"] = "是";
  				} else {
  					data[i]["showAdmin"] = "否";
  				}
  			}
  		}
  		$("#data-tbody").renderData(template,data);
  		table = $('#example2').DataTable({
  	      "paging": true,
  	      "iDisplayLength": 7,
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
  		processIssuerBtn();
  		table.on('draw', function () {
  			processIssuerBtn();
  		}); 
	})
}

function processIssuerBtn() {
	$("button[name='registerIssueBtn']").each(function(){
		if (role == "1") {
			$(this).show();
			var index = $(this).attr("class").indexOf("true");
			if(index > 0) {
				$(this).attr("disabled",true);
				$(this).html("已成为权威凭证发行者");
			}
		}
	});
	$("button[name='addToIssuerTypeBtn']").each(function(){
		if (role == "1") {
			$(this).show();
		}
	});
	$.get("isDownFile",function(data,status){
		if(data) {
			$("button[downFile='file']").each(function(){
				$(this).css("display","inline-block");
	  		})
		}
	})
}

function downEcdsaKey(address) {
	window.location.href="downWeIdEcdsaKey/" + address;
}

function downEcdsaPubKey(address) {
	window.location.href="downWeIdEcdsaPubKey/" + address;
}

function registerIssue(weId) {
	$("#registerIssuerWeId").val(weId);
	$("#registerIssuerName").val("");
    $("#modal-register-issue").modal();
}

function addToIssuerType(weId) {
	$("#addIssuerWeId").val(weId);
    $("#modal-add-to-issueType").modal();
}