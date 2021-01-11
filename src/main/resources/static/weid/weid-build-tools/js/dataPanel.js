$(document).ready(function(){
	if (!isReady) {
    	return;
    }
	var template = $("#app").html();
	$.get("getDataPanel",function(value,status){
		$("#app").renderData(template, value);
	})
});