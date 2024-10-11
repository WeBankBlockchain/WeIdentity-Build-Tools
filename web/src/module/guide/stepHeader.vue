<template>
  <div class='application_header box'>
    <div class='header_left_part'>
      <div class='nav_link'>
        <span>WeIdentity 网页管理工具</span>
      </div>
    </div>
    <div class='header_right_part'>
      <div class='nav_link'>
        <a @click="openLog()"><span class="openLog">查看后台日志</span></a>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data () {
    return {

    }
  },
  methods: {
    openLog () {
      var win = window.open('', 'logWin', 'width=800,height=500,top=100,left=380')
      if (win.location.href === 'about:blank') {
        // 窗口不存在
        win = window.open('#/log/', 'logWin', 'width=800,height=500,top=100,left=330')
      } else {
        // 窗口以已经存在了
        win.focus()
      }
    }
  },
  created () {

  }
}
</script>
