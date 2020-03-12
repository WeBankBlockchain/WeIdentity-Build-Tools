$(document).ready(function(){
	
	var websocket = null;
	
	if ('WebSocket' in window) {
        websocket = new WebSocket("ws://" + window.location.host + "/webSocket");
    } else {
        alert('当前浏览器不支持WebSocket');
        return;
    }

    // 连接发生错误的回调方法
    websocket.onerror = function () {
        $("#logContent").html($("#logContent").html() + "<p>日志连接发生错误</p>");
    };

    // 连接成功建立的回调方法
    websocket.onopen = function () {
        $("#logContent").html($("#logContent").html() + "<p>日志连接成功</p>");
    }

    // 接收到消息的回调方法
    websocket.onmessage = function (event) {
        $("#logContent").html($("#logContent").html() +"<div>" + event.data + "</div>");
        window.location.href="#buttom"
    }
    // 连接关闭的回调方法
    websocket.onclose = function () {
        $("#logContent").html($("#logContent").html() + "<p>日志连接关闭</p>");
    }
    // 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        closeWebSocket();
    }
    // 关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }
})