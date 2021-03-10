<template>
  <div class='application' :class="blackTheme">
    <table cellpadding="0"  cellspacing="0">
      <tr>
        <td class="t_header s_header"><headerBar></headerBar></td>
      </tr>
      <tr>
        <td class="t_main s_main"><router-view></router-view></td>
      </tr>
      <tr>
        <td class="t_footer"><footBar></footBar></td>
      </tr>
    </table>
  </div>
</template>
<script>
import headerBar from './guide/stepHeader'
import footBar from '../components/footer'
import API from '../API/resource'
export default {
  components: {
    headerBar, footBar
  },
  data () {
    return {
      step: ''
    }
  },
  methods: {
    initStep () {
      let step = localStorage.getItem('step')
      if (step === null) {
        this.step = ''
      } else {
        this.step = step
      }
    },
    checkGuideStatus () {
      API.doGet('getGuideStatus').then(res => {
        if (res.data.result !== '') {
          this.$router.push({name: 'deployWeId'})
        } else {
          this.initStep()
          this.$router.push({name: 'step' + this.step})
        }
      })
    }
  },
  mounted () {
    this.checkGuideStatus()
  }
}
</script>
