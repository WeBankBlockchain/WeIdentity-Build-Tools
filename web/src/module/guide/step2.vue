<template>
  <div class="app_view_content">
    <div class="app_view_register">
      <section class="content">
        <div class='guide_step_part box'>
          <div class='bottom_line'></div>
          <div class='guide_step_item guide_step_complated'>
               <span>1</span>
               <img src="../../assets/image/icon-hook.svg" alt="">
               <p>区块链节点配置</p>
          </div>
          <div class='guide_step_item guide_step_active'>
              <span>2</span>
              <img src="../../assets/image/icon-hook.svg" alt="">
              <p>设置主群组</p>
          </div>
          <div class='guide_step_item'>
              <span>3</span>
              <img src="../../assets/image/icon-hook.svg" alt="">
              <p>数据库配置(可选)</p>
          </div>
          <div class='guide_step_item'>
               <span>4</span>
               <img src="../../assets/image/icon-hook.svg" alt="">
               <p>创建管理员WeID</p>
          </div>
          <div class='guide_step_item' v-if="roleType == '1'">
               <span>5</span>
               <img src="../../assets/image/icon-hook.svg" alt="">
               <p style='width:200px;left:-89px'>部署WeIdentity智能合约</p>
          </div>
        </div>
        <div class='container-fluid'>
          <div class="box">
            <div id="AccountDiv" class="card card-primary warning_box">
              <div class="card-header card-title"><h3>配置主群组</h3></div>
              <div class="card-mark">
                <div class="card-mark-text">选择一个FISCO-BCOS的群组，作为 WeIdentity 的主群组（即部署 WeIdentity 智能合约的群组）</div>
                <div class="card-mark-icon">
                  <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html?highlight=群组#group-selection" target="blank_" style='display:block'>
                    <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                    <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block"> 什么是群组？</span>
                  </a>
                </div>
              </div>
              <div class="card-body" style='margin-top:12px'>
                <div class="form-group" style='width:100%;'>
                  <el-form ref="form" :model="form">
                    <el-form-item prop="groupId">
                      <el-select v-model="form.groupId" placeholder="主群组" style="width: 100%">
                        <el-option v-for="val in groupList"
                        :key="val.value"
                        :label="val.value"
                        :value="val.value">
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
              <div class="bt-part">
                <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                <el-button type="primary" @click='next' class="btn btn_150">下一步</el-button>
              </div>
              <div class='sql_warning' style='right:-160px'>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#group-selection" target="blank_" style='display:block'>
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
      roleType: localStorage.getItem('roleType'),
      form: {
        groupId: ''
      },
      groupList: []
    }
  },
  methods: {
    setGroupId () {
      API.doPost('setGroupId', this.form).then(res => { // 保存选择的角色
        if (res.data.errorCode === 0) {
          this.$alert('群组配置成功!', '温馨提示', {}).then(() => {
            localStorage.setItem('step', 3)
            this.$router.push({name: 'step3'})
          }).catch(() => {})
        } else {
          this.$alert('群组配置失败!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    next () {
      this.setGroupId()
    },
    prev () {
      localStorage.setItem('step', 1)
      this.$router.push({name: 'step1'})
    },
    init () {
      // 初始化群组
      API.doGet('getAllGroup/false').then(res => {
        if (res.data.errorCode === 0) {
          this.groupList = res.data.result
          this.form.groupId = this.groupList[0].value
        }
      })
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.form.groupId = res.data.result.group_id
        }
      })
    }
  },
  mounted () {
    this.checkStep()
    this.init()
  }
}
</script>
