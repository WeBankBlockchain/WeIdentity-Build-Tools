<template>
  <div class="log-view">
    <pre v-html = 'data'>
    </pre>
  </div>
</template>
<script>
export default {
  data () {
    return {
      server: 'ws://' + window.location.host + '/webSocket',
      scket: null,
      data: ''
    }
  },
  methods: {
    // 初始化websocket
    initConn () {
      this.data += '日志连接中...</br>'
      let socket = new WebSocket(this.server) // 创建Socket实例 (ReconnectingWebSocket)
      this.socket = socket
      this.socket.onmessage = this.OnMessage
      this.socket.onopen = this.OnOpen
      this.socket.onerror = this.OnError
    },
    OnOpen () {
      this.data += '日志连接成功</br>'
    },
    OnError () {
      this.data += '日志连接发生错误</br>'
    },
    OnMessage (e) {
      this.data += e.data + '</br>'
      window.scrollTo({top: document.body.scrollHeight})
    }
  },
  mounted () {
    this.initConn()
  },
  destroyed () {
    this.socket.close()
  }
}
</script>
<style>
html{
  background: black;
}
</style>
