<template>
  <div id='footBar' class="footBar">
    <span class="copyright">
      <strong>Copyright &copy; 2019-2020 <a href="https://github.com/WeBankFinTech" target="_blank">WeBankFinTech</a>..</strong> All rights reserved.
    </span>
    <span class="version">
      <b>Version</b> <span>{{version}}</span>
    </span>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  data () {
    return {
      version: '1.0.0'
    }
  },
  methods: {
    getVersion () {
      API.doGet('getVersion').then(res => {
        if (res.data.errorCode === 0) {
          this.version = res.data.result
        }
      })
    }
  },
  mounted () {
    this.getVersion()
  }
}
</script>
