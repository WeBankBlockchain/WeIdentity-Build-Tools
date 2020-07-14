$(document).ready(function(){
	let has_guide = sessionStorage.getItem('has_guide')
	const role = sessionStorage.getItem('guide_role')
	const step = sessionStorage.getItem('guide_step')
	const url = window.location.href
	if (!role) {
	//	get role and set sessionstorage
		$.get("getRole",function(value,status){
			if (value) {
				sessionStorage.setItem('guide_role', value)
				if (!step) {
					sessionStorage.setItem('guide_step', '2')
					window.location.href='./nodeConfig.html'
				}
			}
    })
	} else {
		if (!step) {
			window.location.href='./nodeConfig.html' 
		}
	}
	if (!has_guide) {
		$('.breadcrumb').hide()
		$('.d-index').hide()
		if (url.indexOf('index') > -1) {
			if (!step) {
				$('.guild-step').show()
			} else {
				$('.container-fluid').show()
				$('.content-header').show()
				$('.guild-step').hide()
				$('.nav-index').show()
			}
		} else {
			switch (step){
				case '2':
					$('.nav-node').show()
					break
				case '3':
					$('.nav-sql').show()
					break
				case '4':
					$('.nav-node').show()
					break
				case '5':
					$('.nav_guide_deploy').show()
					$('.menu-title-deploy').css('display', 'block')
					const guide_set_weid = sessionStorage.getItem("guide_set_weid")
					if (role !== '1' || guide_set_weid !== '1') {
						$('#depolyBtn').hide()
					}
					break
			}
		}
	} else {
		$('.container-fluid').show()
		$('.content-header').show()
		$('.guild-step').hide()
		$('.menu-item').show()
		$('.menu-title-deploy').css('display', 'block')
		$('.menu-title-async').css('display', 'block')
	}
	// next btn
	$('#guild-next').click(function(){
		const val = $('#roleChange').children('option:selected').val()
		var formData = {};
	    formData.roleType = val;
		$.post("setRole", formData, function(value,status){
			if (value) {
				sessionStorage.setItem('guide_step', '2')
				window.location.href='./nodeConfig.html'
			}
        })		
	})
})