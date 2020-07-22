$(document).ready(function(){
	bsCustomFileInput.init();
	// swiper 控制器
	var mySwiper = new Swiper ('.swiper-container', {
		  	pagination: {
		  		el: '.swiper-pagination',
		  		type: 'progressbar'
		  	},
			paginationClickable:true,
			spaceBetween:30,
			noSwiping: true,
			navigation: {
	        nextEl: '.swiper-button-next',
	        prevEl: '.swiper-button-prev',
	      },
	})
	// 上一步按钮
	$('.prevBtn').click(function(){
		$('.swiper-button-prev').trigger('click');
	})
	let role = sessionStorage.getItem('guide_role')
	// 选择角色
	$(".role_part").click(function(){
		let r = sessionStorage.getItem('guide_role')
		// 如果角色已存在则不允许切换
		if (!r) {
			$(".role_part").each(function(){
				$(this).removeClass("role_active")
			})
			$(this).addClass("role_active")
		}
	})
	if (!role) {
	//	get role and set sessionstorage
		$.get("getRole",function(value,status){
			if (value) {
				sessionStorage.setItem('guide_role', value)
				$("#messageBody").html("<p>检测到您已创建角色，无需再创建，请直接点击下一步</p>");
				$("#modal-message").modal();
				role = value
	           $(".role_part").each(function(){
						$(this).removeClass("role_active")
					})
					let part = $(".role_part")
					if (value == 1) {
						$(part[0]).addClass("role_active")
					} else if (value == 2) {
						$(part[1]).addClass("role_active")
					}
					// 转节点配置
					toNodeConfig();
				}
		})
	} else {
		$("#messageBody").html("<p>检测到您已创建角色，无需再创建，请直接点击下一步</p>");
		$("#modal-message").modal();
		let r = sessionStorage.getItem('guide_role')
		let part = $(".role_part")
		if (r == 1) {
			$(part[0]).addClass("role_active")
		} else if (r == 2) {
			$(part[1]).addClass("role_active")
		}
	}
	// 点击选中角色按钮
	$('#role-next').click(function(){
		// 如果有角色则直接跳转
		if (role) {
			$('.swiper-button-next').trigger('click');
			// 转节点配置
			toNodeConfig();
		} else {
			const val = $('.role_active').attr('type')
			role = val
			var formData = {};
			formData.roleType = val;
			$.post("setRole", formData, function(value,status){
				if (value) {
					$('.swiper-button-next').trigger('click');
					// 转节点配置
					toNodeConfig();
				}
			})	
		}
			
	})
	// 转节点配置，初始化已有的相关数据
	function toNodeConfig() {
		//获取配置
	    $.get("loadConfig",function(data,status){
	        $("#nodeForm  #orgId").val(data.org_id);
	        $("#nodeForm  #amopId").val(data.amop_id);
	        $("#nodeForm  #version").val(data.blockchain_fiscobcos_version);
	        $("#nodeForm  #cnsProFileActive").val(data.cns_profile_active);
	        $("#nodeForm  #ipPort").val(data.blockchain_address);
	        $("#nodeForm  #groupId").val(data.group_id);
	        caDisplay(data.blockchain_fiscobcos_version);
	        checkCa(data);
	    });
	}
	
	// 证书检查
	function  checkCa(data) {
    	var message = "该证书已存在，重新上传将被覆盖。";
    	if (data["ca.crt"] == "true") {
    		$("#caCrtSpan").html(message).show();
    	}
    	if (data["node.key"] == "true") {
    		$("#nodeKeySpan").html(message).show();
    	}
    	if (data["node.crt"] == "true") {
    		$("#nodeCrtSpan").html(message).show();
    	}
    	if (data["client.keystore"] == "true") {
    		$("#clientKeyStoreSpan").html(message).show();
    	}
    }
    
    function caDisplay(v) {
    	if (v == "1") {
        	$("#caV1").show();
        	$("#caV2").hide();
        } else {
        	$("#caV2").show();
        	$("#caV1").hide();
        }
    }
    
    $("#nodeForm  #version").change(function(){
    	var selected = $(this).children('option:selected').val();
    	caDisplay(selected);
    })
	
	// 点击配置节点按钮
	$('#postNodeBtn').click(function(){
		var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var message = checkInputNode();
        if (message != null) {
        	$("#messageBody").html("<p>"+message+"</p>");
    	    $("#modal-message").modal();
    	    return ;
        }
        
	    var formData = new FormData();
	    formData.append("file", $("#caCrtFile")[0].files[0]);
	    formData.append("file", $("#nodeCrtFile")[0].files[0]);
	    formData.append("file", $("#nodeKeyFile")[0].files[0]);
	    formData.append("file", $("#clientKeyStoreFile")[0].files[0]);
	    formData.append("orgId", $.trim($("#nodeForm  #orgId").val()));
	    formData.append("amopId", $.trim($("#nodeForm  #amopId").val()));
	    formData.append("version", $("#nodeForm  #version").val());
	    formData.append("cnsProFileActive", $("#nodeForm  #cnsProFileActive").val());
	    formData.append("ipPort", $.trim($("#nodeForm  #ipPort").val()));
	    formData.append("groupId", $.trim($("#nodeForm  #groupId").val()));
	    $("#checkBody").html("<p>配置提交中,请稍后...</p>");
	    $("#modal-default").modal();
	    $("#goNext").addClass("disabled");
	    $.ajax({
	        url:'nodeConfigUpload', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            if (res=="success") {
	            	//检查节点是否正确
	            	$("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='success-span'>成功</span>, 检查准备中,请稍后...</p>");
	            	setTimeout(checkNodeForTimeout,2000);
	            } else {
	            	 $("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            } 
	        }
	    })	
	});
	
	// 检查节点输入信息
	function checkInputNode() {
		var orgId = $.trim($("#nodeForm  #orgId").val());
		if (orgId.length == 0) {
			return "请输入您的机构名称";
		}
		var amopId = $.trim($("#nodeForm  #amopId").val());
		if (amopId.length == 0) {
			return "请输入您的通讯ID";
		}
		var ipPort = $.trim($("#nodeForm  #ipPort").val());
		if (ipPort.length == 0) {
			return "请输入您的节点IP与Port";
		}
		var groupId = $.trim($("#nodeForm  #groupId").val());
		if (groupId.length == 0) {
			return "请输入您的groupId";
		}
		var r = /^[1-9][0-9]*$/;
		if(!r.test(groupId)) {
			return "groupId必须为整数";
		}
		// 检查上传文件是否跟要求一致
		var caCrtFile = $("#caCrtFile")[0].files[0];
		if (caCrtFile != null && caCrtFile.name != "ca.crt") {
			return "请选择正确的ca.crt文件";
		}
		var nodeCrtFile = $("#nodeCrtFile")[0].files[0];
		if (nodeCrtFile != null && nodeCrtFile.name != "node.crt") {
			return "请选择正确的node.crt文件";
		}
		var nodeKeyFile = $("#nodeKeyFile")[0].files[0];
		if (nodeKeyFile != null && nodeKeyFile.name != "node.key") {
			return "请选择正确的node.key文件";
		}
		var clientKeyStoreFile = $("#clientKeyStoreFile")[0].files[0];
		if (clientKeyStoreFile != null && clientKeyStoreFile.name != "client.keystore") {
			return "请选择正确的client.keystore文件";
		}
		return null;
	}
	
	// 配置提交成功后进行节点检查
    function checkNodeForTimeout() {
    	$("#checkBody").html($("#checkBody").html() + "<p>配置检查中,请稍后...</p>");
    	setTimeout(checkNode,2000);
    }
    
    // 节点检查
    function checkNode() {
    	$.get("checkNode",function(data,status){
            if(data == "success") {//检查成功
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='success-span'>成功</span>。</p>");
				$("#goNext").removeClass("disabled");
				$("#goNext").addClass("nodeGoNext");
				//disabledInput(); //禁止修改操作
         	    $("#goNext").click(function(){
					let hasClass = $(this).hasClass('nodeGoNext')	
					if (hasClass) {
						$("#modal-default").modal("hide");
						$("#goNext").removeClass("nodeGoNext");
						$('.swiper-button-next').trigger('click');
						toDbConfig();
					}
         	   })
            } else if (data == "fail"){//检查失败
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
            } else {//检查失败
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>: " + data + "</p>");
            }
         });
    }
    
    function toDbConfig() {
    	$.get("loadConfig",function(data,status){
            $("#dbForm  #mysql_address").val(data.mysql_address);
            $("#dbForm  #mysql_database").val(data.mysql_database);
            $("#dbForm  #mysql_username").val(data.mysql_username);
            $("#dbForm  #mysql_password").val(data.mysql_password);
        });
    	$("#messageBody").html("<p><span class='success-span'>如果您需要使用到下列功能则需要配置数据库</span><br/>1.Transportation相关组件功能<br/>2.Evidence异步存证功能<br/>3.Persistence数据存储功能</p>");
    	$("#modal-message").modal();
    }
    
    // 提交数据库配置
    $("#postDbBtn").click(function(){
    	var disabled = $(this).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        var message = checkInputDB();
        if (message != null) {
        	$("#messageBody").html("<p>"+message+"</p>");
    	    $("#modal-message").modal();
    	    return ;
        }
	    var formData = new FormData();
	    formData.append("mysql_address", $.trim($("#dbForm  #mysql_address").val()));
	    formData.append("mysql_database", $.trim($("#dbForm  #mysql_database").val()));
	    formData.append("mysql_username", $.trim($("#dbForm  #mysql_username").val()));
	    formData.append("mysql_password", $.trim($("#dbForm  #mysql_password").val()));
	    $("#checkBody").html("<p>配置提交中,请稍后...</p>");
	    $("#modal-default").modal();
	    $("#goNext").addClass("disabled");
	    $.ajax({
	        url:'submitDbConfig', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            if (res=="success") {
	            	//检查节点是否正确
	            	$("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='success-span'>成功</span>, 检查准备中,请稍后...</p>");
	            	setTimeout(checkDbForTimeout,2000);
	            } else {
	            	 $("#checkBody").html($("#checkBody").html() + "<p>配置提交<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            }
	        }
	    })
    });
    
    function checkInputDB() {
    	var address = $.trim($("#dbForm  #mysql_address").val());
    	if (address.length == 0) {
    		return "请输入您的数据库地址";
    	}
    	var database = $.trim($("#dbForm  #mysql_database").val());
    	if (database.length == 0) {
    		return "请输入您的数据库名称";
    	}
    	var username = $.trim($("#dbForm  #mysql_username").val());
    	if (username.length == 0) {
    		return "请输入您的数据库用户名";
    	}
    	var password = $.trim($("#dbForm  #mysql_password").val());
    	if (password.length == 0) {
    		return "请输入您的数据库密码";
    	}
    	return null;
    }
    
    function checkDbForTimeout() {
    	$("#checkBody").html($("#checkBody").html() + "<p>配置检查中,请稍后...</p>");
    	setTimeout(checkdb,2000);
    }
    
    function checkdb() {
    	$.get("checkDb",function(data,status){
           if(data) {//检查成功
        	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='success-span'>成功</span>。</p>");
						 $("#goNext").removeClass("disabled");
						 //disabledInput();
						 $("#goNext").addClass("bdGoNext");
        	   $("#goNext").click(function(){
								let hasClass = $(this).hasClass('bdGoNext')	
								if (hasClass) {
									$("#modal-default").modal("hide");
									$("#goNext").removeClass("nodeGoNext");
									$("#goNext").removeClass("bdGoNext");
									$("#modal-default").modal("hide");
									var formData = {};
									$.post("checkOrgId", formData, function(value,status){
										if (value) {
											// 流程走完
											window.location.href="deploy.html";
										} else {
											$('.swiper-button-next').trigger('click');
											toAccount();
										}
									})	
								}	
          	  })
           } else {//检查失败
        	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
           }
        });
    }
    var hasAccount = false;
    // 转到账户配置
    function toAccount() {
		$("#accountDiv").hide();
		$("#createDiv").hide();
		$.get("checkAdmin",function(data,status){
			if (data != "") {
				$(".card-title").html("当前admin账户");
				$("#accountDiv").show();
				$("#nextDiv").show();
				$("#account").val(data);
				hasAccount = true;
			} else {
				$(".card-title").html("创建管理员账户的WeId");
				$("#createDiv").show();
				hasAccount = false;
			}
			$("#nextBtn").removeClass("disabled");
	    });
	}
    
    $("#craeteByPrivBtn").click(function(){
		var thisObj = this;
		var disabled = $(thisObj).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $.confirm("是否确定根据当前私钥文件创建账户?",function(){
			createAdmin(thisObj);
	    })
    });
	
	$("#createBySysBtn").click(function(){
		var thisObj = this;
		var disabled = $(thisObj).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $.confirm("确定系统自动创建 admin 账户的公私钥?",function(){
			createAdmin(thisObj);
	    })
    });
	
	function createAdmin(obj) {
		$(obj).addClass("disabled");
	    var formData = new FormData();
	    formData.append("ecdsa", $("#privateKeyFile")[0].files[0]);
	    $("#checkBody").html("<p>账户创建中,请稍后...</p>");
	    $("#goNext").addClass("disabled");
	    $("#modal-default").modal();
	    $.ajax({
	        url:'createAdmin', /*接口域名地址*/
	        type:'post',
	        data: formData,
	        contentType: false,
	        processData: false,
	        success:function(res) {
	            if (res=="fail") {
	            	$("#checkBody").html($("#checkBody").html() + "<p>账户创建<span class='fail-span'>失败</span>,请查看服务端日志。</p>");
	            } else {
	            	$("#checkBody").html($("#checkBody").html() + "<p>账户创建<span class='success-span'>成功</span>。</p>");
	            	$("#goNext").removeClass("disabled");
	            	$("#goNext").click(function(){
	            		if (role == "2") {
	            			window.location.href="deploy.html";
	            		} else {
	            			$('.swiper-button-next').trigger('click');
		            		$("#modal-default").modal("hide");
		            		$("#modal-create-pri").modal("hide");
	            		}
	            	})
	            }
	            $(obj).removeClass("disabled");
	            toAccount();
	        }
	    })
	}
	// 点击跳过	
	$('#dbPassBtn').click(function(){
		var formData = {};
		$.post("checkOrgId", formData, function(value,status){
			if (value) {
				// 流程走完
				window.location.href="deploy.html";
			} else {
				$('.swiper-button-next').trigger('click');
				toAccount();
			}
		})	
	})
	
	$("#nextBtn").click(function(){
		$('.swiper-button-next').trigger('click');
	})
})