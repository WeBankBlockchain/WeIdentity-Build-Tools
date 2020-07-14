$(document).ready(function(){
	bsCustomFileInput.init();
    var hasAccount = false;
    var first = true;
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
	
	$("#nextBtn").click(function(){
//		goTo(this, "nodeConfig.html");
		sessionStorage.setItem('guide_step', '5')
		goTo(this, "deploy.html");
    });
	
	function createAdmin(obj) {
		$(obj).addClass("disabled");
	    var formData = new FormData();
	    formData.append("ecdsa", $("#privateKeyFile")[0].files[0]);
	    $("#checkBody").html("<p>账户创建中,请稍后...</p>");
	    $("#configBtn").addClass("disabled");
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
	            	$("#postBtn").removeClass("disabled");
	            } else {
	            	$("#checkBody").html($("#checkBody").html() + "<p>账户创建<span class='success-span'>成功</span>。</p>");
	            	first = false;
	            	load();
	            	$("#configBtn").removeClass("disabled");
	            	step2();
	            }
	        }
	    })
	}
	load();
	function load() {
		$("#accountDiv").hide();
		$("#createDiv").hide();
		$.get("checkAdmin",function(data,status){
			if (data != "") {
				$(".card-title").html("当前admin账户");
				$("#accountDiv").show();
				$("#nextDiv").show();
				$("#account").val(data);
				$("#i-account").removeClass("fa-circle");
	            $("#i-account").addClass("fa-check-circle");
				hasAccount = true;
				$("#nextBtn").removeClass("disabled");
				if (first) {
					step2_1();
				}
			} else {
				$(".card-title").html("创建管理员账户的WeId");
				$("#createDiv").show();
				hasAccount = false;
				$("#nextBtn").removeClass("disabled");
				step1();
			}
	    });
	}
	function step1() {
		if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({
			onSkip:function(){
				$.cookie("skip",true);
			}
		});
		var enjoyhint_script_steps = [{
		    'click #createDiv': "请点击【系统自动创建公私钥】或 【自行上传公私钥1】"
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
	
	function step2() {
		if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({});
		var enjoyhint_script_steps = [{
		    'click #configBtn': '下一步。',
		    'showSkip': false
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
	
	function step2_1() {
		if($.cookie("skip")){
			return;
		}
		var enjoyhint_instance = new EnjoyHint({
			onSkip:function(){
				$.cookie("skip",true);
			}
		});
		var enjoyhint_script_steps = [{
		    'next #nextBtn': '下一步。',
		    'nextButton': {
		        text: "确定"
		    },
		    'showSkip': true
		}];
		enjoyhint_instance.set(enjoyhint_script_steps);
		enjoyhint_instance.run();
	}
});



