$(document).ready(function(){

    $("#createBtn").click(function(){
        var $this = this;
        var disabled = $($this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $($this).addClass("disabled");
        $($this).html("weId创建中,  请稍等...");
        $("#messageBody").html("<p>weId创建中，请稍等...</p>");
        $("#modal-message").modal();
        $.get("createWeId",function(value,status){
            if (value) {
                $("#messageBody").html("<p>weId创建<span class='success-span'>成功</span>。</p>");
                loadData();
            } else {
                $("#messageBody").html("<p>weId创建<span class='fail-span'>失败</span>，请联系管理员。</p>");
            }
            $($this).html("创建weId");
            $($this).removeClass("disabled");
            $("#modal-message").modal();
        })
    });
    
    if (isReady) {
    	loadData();
    }
    
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
        $.post("registerIssuer", formData, function(value,status){
            if (value == "success") {
                $("#messageBody").html("<p>权威凭证发行者注册<span class='success-span'>成功</span>。</p>");
                loadData();
            }  else if (value == "fail") {
            	 $("#messageBody").html("<p>权威凭证发行者注册<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	 $("#messageBody").html("<p>"+value+"</p>");
            }
            $($this).html("注册");
            $($this).removeClass("disabled");
            $("#modal-message").modal();
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
        var formData = {};
	    formData.weId = weId;
	    formData.issuerType = type;
        $.post("addIssuerIntoIssuerType", formData, function(value,status){
            if (value == "success") {
                $("#messageBody").html("<p>特定类型的发行者注册<span class='success-span'>成功</span>。</p>");
                loadData();
            }  else if (value == "fail") {
            	 $("#messageBody").html("<p>特定类型的发行者注册<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	 $("#messageBody").html("<p>"+value+"</p>");
            }
            $($this).html("注册");
            $($this).removeClass("disabled");
            $("#modal-message").modal();
        })
    });
});
var template = $("#data-tbody").html();
var  table;
function loadData() {
	 //加载部署数据
	$.get("getWeIdList",function(data,status){
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
  		processIssuerBtn();
  		table.on('draw', function () {
  			processIssuerBtn();
  		}); 
	})
}

function processIssuerBtn() {
	$("button[name='registerIssueBtn']").each(function(){
		var index = $(this).attr("class").indexOf("true");
		if(index > 0) {
			$(this).attr("disabled",true);
			$(this).html("已成为权威凭证发行者");
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
    $("#modal-register-issue").modal();
}

function addToIssuerType(weId) {
	$("#addIssuerWeId").val(weId);
    $("#modal-add-to-issueType").modal();
}