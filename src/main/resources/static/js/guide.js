$(document).ready(function(){
	// swiper 控制器
	var mySwiper = new Swiper ('.swiper-container', {
		  	pagination: {
		  		el: '.swiper-pagination',
		  		type: 'progressbar'
		  	},
			paginationClickable:true,
			spaceBetween:30,
			noSwiping: false,
			navigation: {
	        nextEl: '.swiper-button-next',
	        prevEl: '.swiper-button-prev',
	      },
	})
	const role = sessionStorage.getItem('guide_role')
	if (!role) {
	//	get role and set sessionstorage
		$.get("getRole",function(value,status){
			if (value) {
			} else {
			}
		})
	} else {
	}
	// next btn
	$('#guild-next').click(function(){
		const val = $('#roleChange').children('option:selected').val()
		$('.swiper-button-next').trigger('click')
//		var formData = {};
//	    formData.roleType = val;
//		$.post("setRole", formData, function(value,status){
//			if (value) {
//				sessionStorage.setItem('guide_step', '2')
//				window.location.href='./nodeConfig.html'
//			}
//        })		
	})
})