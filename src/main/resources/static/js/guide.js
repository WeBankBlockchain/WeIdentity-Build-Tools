$(document).ready(function(){
	let has_guide = false
	sessionStorage.setItem('guide_role', 1)
	const role = sessionStorage.getItem('guide_role')
	// 是否已部署合约 0-未部署 1 已部署
	const guide_deployment = '1'
	const step = sessionStorage.getItem('guide_step')
	const url = window.location.href
	//	let step = '4'
	if (!role) {
	//	get role and set sessionstorage
	} else {
		if (!step) {
			if (role !== '1' || guide_deployment === '1') {
				sessionStorage.setItem('guide_step', '2')
				window.location.href='./nodeConfig.html'
			}
		}
	}
	if (!has_guide) {
		$('.breadcrumb').hide()
		$('.d-index').hide()
		if (url.indexOf('index') > -1) {
			if (!step && step !== '2') {
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
					$('.nav-sql').show()
					break
			}
		}
	} else {
		$('.container-fluid').show()
		$('.content-header').show()
		$('.guild-step').hide()
		$('.menu-item').show()
	}
	// next btn
	$('#guild-next').click(function(){
		const val = $('#roleChange').children('option:selected').val()
		if (!step) {
			sessionStorage.setItem('guide_step', '2')
		}
		window.location.href='./nodeConfig.html'
	})
})