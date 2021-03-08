<template>
  <section class="content section_main">
    <div class="box" style="width: 55%; margin-left: 200px">
      <!-- general form elements -->
      <div id="AccountDiv" class="card card-primary warning_box">
        <div class="card-header"><h3>WeID 账户</h3></div>
        <div class="card-mark">
          <div class="card-mark-text">当前管理员的 WeID（目前不支持修改）</div>
          <div class="card-mark-icon">
            <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_">
              <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
              <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">什么是管理员？</span>
            </a>
          </div>
        </div>
        <div class="card-body">
          <div style="margin-top: 15px">
            <form role="form" id="AccountForm">
              <div class="form-group" id="accountDiv">
                <el-form ref="accountForm" :model="accountForm">
                  <el-form-item prop="account">
                    <el-input v-model="accountForm.account" placeholder="Enter account" readOnly></el-input>
                  </el-form-item>
                </el-form>
              </div>
            </form>
            <div class='sql_warning bg_color' style='right:-160px'>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_">
                <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
<script>
import API from '../../../API/resource'
export default {
  data () {
    return {
      accountForm: {
        account: ''
      }
    }
  },
  methods: {
    init () {
      API.doGet('checkAdmin').then(res => { // 检查账户是否存证
        if (res.data.result !== '') { // 账户存在
          this.accountForm.account = res.data.result
        }
      })
    }
  },
  created () {
    this.init()
  }
}
</script>
