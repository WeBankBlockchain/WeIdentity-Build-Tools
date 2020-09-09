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
        var btnValue = $($this).html();
        $($this).html("添加中,  请稍等...");
        isClose = false;
        var formData = {};
	    formData.weId = weId;
	    formData.issuerType = type;
        $.post("addIssuerIntoIssuerType", formData, function(value,status){
            if (value == "success") {
                $("#confirmMessageBody").html("<p>添加白名单<span class='success-span'>成功</span>。</p>");
                loadData();
                isClose = true;
            }  else if (value == "fail") {
            	$("#confirmMessageBody").html("<p>添加白名单<span class='fail-span'>失败</span>，请联系管理员。</p>");
            } else {
            	$("#confirmMessageBody").html("<p>"+value+"</p>");
            }
            $($this).html(btnValue);
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

});

function loadData() {
	var cData = {};//记录当前查询数据
	var cStartIndex = 0;//记录当前开始位置
	var pageBtnType = "first";
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
      "pagingType":"full",
      "oLanguage": {
    	  "sZeroRecords": "对不起，查询不到任何相关数据",
    	  "oPaginate": {
            "sFirst":    "第一页",
            "sPrevious": " 上一页 ",
            "sNext":     " 下一页 ",
            "sLast":     " 最后一页 "
          } 
      },
      "sAjaxSource":"getWeIdList",
      "fnServerData" : function(sSource, aoData, fnCallback, oSettings) {
    	var iDisplayStart = oSettings["_iDisplayStart"];
    	var pageSize = oSettings["_iDisplayLength"];
    	var blockNumber;
    	var indexInBlock;
    	var direction;
    	if (pageBtnType == "first") {
    		blockNumber = 0;//0表示后台要取最新块高
    		indexInBlock = 9999; //表示从块最末开始
    		direction = true;
    	} else if(pageBtnType == "previous") {
    		blockNumber = cData.dataList[0].weIdPojo.currentBlockNum;
    		indexInBlock = cData.dataList[0].weIdPojo.index + 1;
    		direction = false;
    	} else if(pageBtnType == "next") {
    		blockNumber = cData.dataList[cData.dataList.length - 1].weIdPojo.currentBlockNum;
    		indexInBlock = cData.dataList[cData.dataList.length - 1].weIdPojo.index - 1;
    		direction = true;
    	} else if(pageBtnType == "last") {
    		blockNumber = -1
    		indexInBlock = 0;
    		direction = false;
    		oSettings["_iDisplayStart"] = 99999999;
    	}
    	aoData.push({"name":"blockNumber","value":blockNumber});
        aoData.push({"name":"pageSize","value":pageSize});
        aoData.push({"name":"indexInBlock","value":indexInBlock});
        aoData.push({"name":"direction","value":direction});
        cStartIndex = oSettings["_iDisplayStart"];
        console.log(cStartIndex)
    	oSettings.jqXHR = $.ajax({
    		  "dataType": 'json',
    		  "type": "GET",
    		  "url": sSource,
    		  "data": aoData,
    		  "success": function(data) {
    			  var ndata = {};//返回的数据需要固定格式，否则datatables无法解析，所以需要重新组装
    			  if (data.dataList.length > 0) {
    				cData = data; 
    			  } else {
    				data = cData;
    				if (pageBtnType == "previous") {
  					  oSettings["_iDisplayStart"] = 0;
  				    }
    			  }
    			  ndata.data = data.dataList;
    			  if (data.dataList.length < pageSize) {
    				  if (pageBtnType == "previous") {
    					  oSettings["_iDisplayStart"] = 0;
    				  }
    				  if (pageBtnType == "next") {
    					  oSettings["_iDisplayStart"] = oSettings["_iDisplayStart"] + pageSize;
    				  }
    			  }
    			  ndata.recordsTotal = data.allCount;
    			  ndata.recordsFiltered = ndata.recordsTotal;
    			  fnCallback(ndata);
    		  }
         });
      },
      "fnDrawCallback":function(){
    	 $(".pagination > .last > a").click(function(){
			 pageBtnType = "last";
		 })
		 $(".pagination > .previous > a").click(function(){
			 pageBtnType = "previous";
		 })
		 $(".pagination > .next > a").click(function(){
			 pageBtnType = "next";
		 })
		 $(".pagination > .first > a").click(function(){
			 pageBtnType = "first";
		 })
      },
      columns:[
          {"render": function ( data, type, full, meta) {
        	var op = ""
            op += "<a href='javascript:showDId(\""+ full.weId + "\",\""+ full.id + "\")'>" + full.weIdShow + "</a>";
        	return op;
          }},
          { "render": function (data, type, full, meta) {
        	  if (!full.from) {
        		  return "--";
        	  }
        	  return full.from;
          }},
          { data: 'hashShow'},
          { "render": function (data, type, full, meta) {
        	  return getLocalTime(full.weIdPojo.created * 1000);
          }},
          {"render": function ( data, type, full, meta) {
        	  var op = ""
        	  //op += "<button type='button' onclick='downEcdsaPubKey(\"" + full.id + "\")' class='btn btn-inline btn-primary btn-flat'>下载公钥文件</button> &nbsp;";
        	  //op += "<button type='button' onclick='downEcdsaKey(\"" + full.id + "\")' class='btn btn-inline btn-primary btn-flat'>下载私钥文件</button> &nbsp;";
        	  var disable = "";
        	  if (full.issuer) {//权威机构
        		  disable = "disabled = 'true'"
        	  }
        	  op += "<button type='button' name='registerIssueBtn' onclick='registerIssue(\"" + full.weId + "\")' title='注册为Authority Issuer' class='btn btn-inline btn-primary btn-flat'" + disable + ">注册为权威凭证发行者</button> &nbsp;";
        	  if (role == "1") {
        		op += "<button type='button' name='addToIssuerTypeBtn' onclick='addToIssuerType(\"" + full.weId + "\")' title='注册为Specific Issuer' class='btn btn-inline btn-primary btn-flat' >添加到白名单</button>";
      		  }
        	  return op;
          }}
        ]
    });
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