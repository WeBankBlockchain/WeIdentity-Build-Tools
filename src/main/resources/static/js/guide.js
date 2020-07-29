$(document).ready(function(){
	bsCustomFileInput.init();
	// swiper 控制器
	var step = sessionStorage.getItem('guide_step')
	var role = sessionStorage.getItem('guide_role')
	if (step) {
		step = Number(step)
	} else {
		step = 0
	}
	checkStep(step)
	var mySwiper = new Swiper ('.swiper-container', {
		  	initialSlide: step,
			paginationClickable:true,
			spaceBetween:30,
			noSwiping: true,
			navigation: {
		        nextEl: '.swiper-button-next',
		        prevEl: '.swiper-button-prev',
			},
			mousewheelEventsTarged : '.swiper-slide',
			mousewheelControl: true,
	        observer:true,
			observeParents:true
	})
	
	// 上一步按钮
	$('.prevBtn').click(function(){
		let s = Number(sessionStorage.getItem('guide_step'))
		s--
		checkStep(s)
		sessionStorage.setItem('guide_step', s)
		$('.swiper-button-prev').trigger('click');
	})
	
	// 选择角色
	$(".role_part").click(function(){
		let r = sessionStorage.getItem('guide_role')
		$(".role_part").each(function(){
			$(this).removeClass("role_active")
		})
		$(this).addClass("role_active")
	})
	if (!role) {
	//	get role and set sessionstorage
		$.get("getRole",function(value,status){
			if (value) {
				sessionStorage.setItem('guide_role', value)
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
		let r = sessionStorage.getItem('guide_role')
		$(".role_part").each(function(){
			$(this).removeClass("role_active")
	    })
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
		sessionStorage.setItem('guide_step', '1')
		const val = $('.role_active').attr('type')
		role = val
		sessionStorage.setItem('guide_role', role)
		var formData = {};
		formData.roleType = val;
		$.post("setRole", formData, function(value,status){
			if (value) {
				nav();
				$('.swiper-button-next').trigger('click');
				// 转节点配置
				toNodeConfig();
			}
		})
	})
	function checkStep(e) {
		if (e != 0) {
			nav();
		}
		switch (e) {
		 	case 0:
		 		break;
		 	case 1:
		 		toNodeConfig();
		 		break;
		 	case 2:
		 		getIdList();
		 		break;
		 	case 3:
		 		toDbConfig();
		 		break;
		 	case 4:
		 		toAccount();;
	 			break;		
		}
	}
	
	function nav() {
		$(".guide_step_part > .guide_step_item > span").each(function(){
			var $step = $(this).html();
			if ($step == 5 && role == 2) {
				$(this).parent().hide();
			} else {
				$(this).parent().show();
			}
		})
	}
	
	// 转节点配置，初始化已有的相关数据
	function toNodeConfig() {
		//获取配置
	    $.get("loadConfig",function(data,status){
	        $("#nodeForm  #orgId").val(data.org_id);
	        $("#nodeForm  #amopId").val(data.amop_id);
	        $("#nodeForm  #version").val(data.blockchain_fiscobcos_version);
	        $("#nodeForm  #cnsProFileActive").val(data.cns_profile_active);
	        $("#nodeForm  #ipPort").val(data.blockchain_address);
//	        $("#nodeForm  #groupId").val(data.group_id);
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
//	    formData.append("groupId", $.trim($("#nodeForm  #groupId").val()));
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
//		var groupId = $.trim($("#nodeForm  #groupId").val());
//		if (groupId.length == 0) {
//			return "请输入您的groupId";
//		}
//		var r = /^[1-9][0-9]*$/;
//		if(!r.test(groupId)) {
//			return "groupId必须为整数";
//		}
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
						sessionStorage.setItem('guide_step', '2')
						$("#modal-default").modal("hide");
						$("#goNext").removeClass("nodeGoNext");
						$('.swiper-button-next').trigger('click');
//						toDbConfig();
						getIdList()
					}
         	   })
            } else if (data == "fail"){//检查失败
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
            } else {//检查失败
         	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>: " + data + "</p>");
            }
         });
    }
    // 获取群主ID
    function getIdList() {
		$("#guide_groupID").loadSelect("getAllGroup/false","value", "value",function(data){
	    	$.get("loadConfig",function(data,status){
	    		//获取设置的groupId
	    		const id = data.group_id
	    		const str = "option[value='"+ id +"']"
	    		$("#guide_groupID").find(str).prop("selected",true);
		    })
		});
    	
    }
    // 点击群组ID下一步
    $('#setGroupId').click(function(){
    	let val = $('#guide_groupID').find('option:selected').val()
    	const formData = {}
    	formData.groupId = val
    	$.post("setGroupId", formData, function(value,status){
			if (value) {
		    	sessionStorage.setItem('guide_step', '3')
				$('.swiper-button-next').trigger('click');
				toDbConfig();
			} else {
				$("#checkBody").html($("#checkBody").html() + "<p>设置主群组ID<span class='fail-span'>失败</span></p>");
			}
		})

    })
    
    function toDbConfig() {
    	$.get("loadConfig",function(data,status){
            $("#dbForm  #mysql_address").val(data.mysql_address);
            $("#dbForm  #mysql_database").val(data.mysql_database);
            $("#dbForm  #mysql_username").val(data.mysql_username);
            $("#dbForm  #mysql_password").val(data.mysql_password);
        });
    	$("#messageBody").html("<p><span class='success-span'>如果您需要使用到下列功能，则需要配置数据库</span><br/>1.Transportation相关组件功能<br/>2.Evidence异步存证功能<br/>3.Persistence数据存储功能(例如：存储Credential)</p>");
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
        				  if (value == 1) {
        					  // 流程走完
        					  sessionStorage.removeItem('guide_step')
        					  toIndex();
        				  } else if (value == 0){
        					  $('.swiper-button-next').trigger('click');
        					  sessionStorage.setItem('guide_step', '4')
        					  toAccount();
        				  } else {
        					  $("#messageBody").html("<p><span class='fail-span'>程序出现异常，请查看日志</span></p>");
        			    	  $("#modal-message").modal();
        				  }
        			   })	
        		   }	
          	  })
           } else {//检查失败
        	   $("#checkBody").html($("#checkBody").html() + "<p>配置检查<span class='fail-span'>失败</span>，请确认配置是否正确。</p>");
           }
        });
    }
    // 点击选择生成秘钥
    $('.key_item').click(function(){
    	$('.key_item').removeClass('active_key')
    	$(this).addClass('active_key')
    })
    // 选择完秘钥方式点击下一步
    $('#caretKeyBtn').click(function(){
    	let type = $('.active_key').attr('type')
    	if (type == 1) {
    		// 系统自动创建公钥
    		var thisObj = this;
			var disabled = $(thisObj).attr("class").indexOf("disabled");
	        if(disabled > 0) return;
	        $.confirm("请确认，系统将自动为管理员的 WeID 创建公私钥?",function(){
				createAdmin(thisObj);
		    })
    	} else {
    		// 选择私钥
    		$('#modal-create-pri').modal()
    	}
    })
    
    
    var hasAccount = false;
    // 转到账户配置
    function toAccount() {
		$("#accountDiv").hide();
		$("#createDiv").hide();
		$.get("checkAdmin",function(data,status){
			if (data != "") {
				$(".card-title").html("当前管理员的 WeID 已经存在（目前不支持修改）");
				$("#accountDiv").show();
				$("#nextDiv").show();
				$("#account").val(data);
				$('#key-part').hide()
				hasAccount = true;
			} else {
				$(".card-title").html("创建管理员账户的WeId");
				$("#createDiv").show();
				$('#key-part').show()
				hasAccount = false;
			}
			$("#nextBtn").removeClass("disabled");
	    });
	}
    
    $("#craeteByPrivBtn").click(function(){
		var thisObj = this;
		var disabled = $(thisObj).attr("class").indexOf("disabled");
        if(disabled > 0) return;
        $.confirm("请确认，是否使用当前私钥文件为管理员的 WeID 创建公私钥?",function(){
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
//	            	$("#postBtn").removeClass("disabled");
	            	$(obj).removeClass("disabled");
	            } else {
	            	$("#checkBody").html($("#checkBody").html() + "<p>账户创建<span class='success-span'>成功</span>。</p>");
	            	// 新增的内容开始
	            	$(".card-title").html("当前admin账户");
	            	$("#createDiv").hide();
					$("#accountDiv").show();
					$("#nextDiv").show();
					$("#account").val(res);
					$('#key-part').hide()
					hasAccount = true;
					$("#nextBtn").removeClass("disabled");
					// 新增的内容结束
//	            	$("#configBtn").removeClass("disabled");
	            	$("#goNext").removeClass("disabled");
	            	$("#goNext").click(function(){
	            		if (role == "2") {
	            			sessionStorage.removeItem('guide_step')
	            			toIndex();
	            		} else {
	            			$('.swiper-button-next').trigger('click');
		            		$("#modal-default").modal("hide");
		            		$("#modal-create-pri").modal("hide");
		            		sessionStorage.setItem('guide_step', '5')
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
			if (value == 1) {
				// 流程走完
				sessionStorage.removeItem('guide_step')
				toIndex();
			} else if (value == 0){
				$('.swiper-button-next').trigger('click');
				sessionStorage.setItem('guide_step', '4')
				toAccount();
			} else {
				$("#messageBody").html("<p><span class='fail-span'>程序出现异常，请查看日志</span></p>");
	    	    $("#modal-message").modal();
			}
		})	
	})
	
	$("#nextBtn").click(function(){
		if(role == "2") {
			toIndex();
		} else {
			$('.swiper-button-next').trigger('click');
			sessionStorage.setItem('guide_step', '5')
		}
	})
	
	function toIndex() {
		var formData = {};
		formData.step = "5";
		$.post("setGuideStatus", formData, function(value,status){
			window.location.href="index.html";
		})	
	}
})