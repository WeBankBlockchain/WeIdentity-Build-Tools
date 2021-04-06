<template>
  <div class='application' :class="blackTheme">
    <table class="t_table" cellpadding="0"  cellspacing="0">
      <tr>
        <td rowspan="3" class="t_sidebar"><sideBar></sideBar></td>
        <td class="t_header" ><headerBar></headerBar></td>
      </tr>
      <tr>
        <td class="t_main t_boder"><router-view></router-view></td>
      </tr>
      <tr>
        <td class="t_footer"><footBar></footBar></td>
      </tr>
    </table>
  </div>
</template>
<script>
import headerBar from '../../components/header'
import sideBar from '../../components/sideBar'
import footBar from '../../components/footer'
export default {
  components: {
    headerBar, sideBar, footBar
  }
}
</script>
