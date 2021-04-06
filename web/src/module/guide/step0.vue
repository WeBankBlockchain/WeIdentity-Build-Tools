<template>
  <div class="app_view_content">
    <div class="app_view_register">
      <section class="content">
        <div class='container-fluid guild-step' style="padding-top:10px">
          <div class="box">
            <div id="AccountDiv" class="card card-primary warning_box">
              <div class="card-header">
                <h6>请选择您的角色</h6>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#id3" target="_blank">
                  <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                  <span class='guide_notice icon_question'>什么是"联盟链委员会管理员"</span>
                </a>
              </div>
              <div class="card-body role_body">
                <div :class="{'role_part role_active': roleType == 1, 'role_part': roleType !== 1}" type='1' @click="active(1)">
                  <span><img class='role_icon' src='../../assets/image/icon1-member.svg'>我是"联盟链委员会管理员"</span><img class='selected_icon' src="../../assets/image/icon-select.svg" alt="">
                </div>
                <div :class="{'role_part role_active': roleType == 2, 'role_part': roleType !== 2}" type='2' @click="active(2)">
                  <span><img class='role_icon' src="../../assets/image/icon1-not.svg" alt="">我不是"联盟链委员会管理员"</span><img class='selected_icon' src="../../assets/image/icon-select.svg" alt="">
                </div>
              </div>
              <div class="card-footer bt-part" id="role-next">
                <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                <el-button type="primary" @click='next' class="btn btn_150">下一步</el-button>
              </div>
              <div class='sql_warning' style='right:-160px'>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#role-selection" target="blank_" style='display:block'>
                  <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
<script>
import API from '../../API/resource'
export default {
  data () {
    return {
      roleType: 1
    }
  },
  methods: {
    active (type) {
      this.roleType = type
    },
    setRole () {
      API.doPost('setRole', {roleType: this.roleType}).then(res => { // 保存选择的角色
        if (res.data.errorCode === 0) {
          localStorage.setItem('step', 1)
          this.$router.push({name: 'step1'})
        }
      })
    },
    prev () {
      localStorage.setItem('step', '')
      this.$router.push({name: 'step'})
    },
    next () {
      localStorage.setItem('roleType', this.roleType)
      this.setRole()
    },
    init () {
      API.doGet('getRole').then(res => { // 获取角色
        if (res.data.result !== '') {
          this.roleType = res.data.result
        }
      })
    }
  },
  mounted () {
    let roleType = localStorage.getItem('roleType')
    if (roleType !== null && roleType !== '') {
      this.roleType = parseInt(roleType)
    }
    this.init()
  },
  created () {
    this.checkStep()
  }
}
</script>
