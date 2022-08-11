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
          <div class='guide_step_item guide_step_complated'>
            <span>2</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>设置主群组</p>
          </div>
          <div class='guide_step_item guide_step_complated'>
            <span>3</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>数据库配置(可选)</p>
          </div>
          <div class='guide_step_item guide_step_complated'>
            <span>4</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>创建管理员WeID</p>
          </div>
          <div class='guide_step_item guide_step_active'>
            <span>5</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p style='width:200px;left:-89px'>部署WeIdentity智能合约</p>
          </div>
        </div>
        <div class='container-fluid guild-step'>
          <div class="box">
            <div id="AccountDiv" class="card card-primary warning_box">
              <div class="card-header"><h3>为WeIdentity DID 设置链ID （chain-id）</h3></div>
              <el-form ref="deployForm" :model="deployForm">
                <div class="card-body role_body">
                  <div class="form-group" style='width:100%;'><a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-spec.html?highlight=chain-id#id4" target="blank_">
                    <span style="color: #017CFF;"><img class="icon_question" src="../../assets/image/icon-question.svg" alt=""> 什么是chain id?</span></a>
                    <el-form-item prop="chainId" :rules="{required: true, message: '请输入ChainId', trigger: 'blur'}">
                      <el-input v-model="deployForm.chainId" placeholder="Enter chainId" maxlength="30" style="width: 100%" onKeyUp="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')" @blur="deployForm.chainId = $event.target.value"></el-input>
                    </el-form-item>
                  </div>
                </div>
                <div class="card-header" style="margin-top: -5px;"><h3>为应用设置名字</h3></div>
                <div class="card-body role_body">
                  <div class="form-group" style='width:100%;'>
                    <span style="display: block;color: gray;padding-bottom: 10px;">给自己的WeIdentity区块链应用起一个名字吧，用于在同一条链上区别不同的 WeIdentity 应用，例如“学历证书应用”，“区块链证件应用”。</span>
                    <el-form-item prop="applyName" :rules="{required: true, message: '请输入应用名', trigger: 'blur'}">
                      <el-input v-model="deployForm.applyName" placeholder="Enter applyName" maxlength="30" style="width: 100%"></el-input>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
              <div class="bt-part">
                <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                <el-button type="primary" @click='toIndex($event)' class="btn btn_150">下一步</el-button>
              </div>
              <div class='sql_warning' style='right:-160px'>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid-deploy" target="blank_" style='display:block'>
                  <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        <!--显示部署详情 -->
        <el-dialog
          title="合约部署"
          class="dialog-view" width="400px"
          :visible.sync="dialog.dialogDetailVisible"
          :close-on-click-modal="false">
          <div class="dialog-body deployMessage">
            <p v-for="val in dialog.deployMessages" :key="val.value">{{val}}</p>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" class="width_100" @click="comfirmBtn">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </div>
  </div>
</template>
<script>
import API from '../../API/resource'
export default {
  data () {
    return {
      deployForm: {
        applyName: '',
        chainId: ''
      },
      deployed: false,
      hash: '',
      dialog: {
        dialogDetailVisible: false,
        deployMessages: []
      },
      deployBtn: null
    }
  },
  methods: {
    checkInput () {
      if (this.deployForm.chainId === '') {
        this.$alert('请输入ChainId!', '温馨提示', {}).catch(() => {})
        return false
      }
      if (this.deployForm.applyName === '') {
        this.$alert('请输入应用名!', '温馨提示', {}).catch(() => {})
        return false
      }
      return true
    },
    comfirmBtn () {
      this.dialog.dialogDetailVisible = false
      if (this.deployed) {
        this.setGuideStatus()
      }
    },
    toIndex (e) {
      this.deployBtn = e.currentTarget
      if (this.deployed) {
        this.setGuideStatus()
      } else {
        this.deploy()
      }
    },
    deploy () {
      if (!this.checkInput()) {
        return
      }
      this.deployed = false
      this.disableBtn(this.deployBtn)
      this.dialog.dialogDetailVisible = true
      this.dialog.deployMessages = []
      this.dialog.deployMessages.push('合约部署中...')
      API.doPost('deploy', this.deployForm, 120).then(res => { // 保存选择的角色
        this.dialog.dialogDetailVisible = true
        if (res.data.errorCode === 0) {
          this.hash = res.data.result
          this.isEnableMasterCns()
        } else {
          this.dialog.deployMessages.push('合约部署失败! 请查看日志。')
          this.enableBtn(this.deployBtn)
        }
      })
    },
    isEnableMasterCns () { // 检查是否已启用合约
      API.doGet('isEnableMasterCns').then(res => {
        this.dialog.dialogDetailVisible = true
        if (res.data.result) { // 说明为首次部署，则调用启用逻辑
          this.enableHash()
        } else { // 非首次部署，部署流程完成
          this.dialog.deployMessages.push('合约部署成功! 请继续操作。')
          this.deployed = true
          this.enableBtn(this.deployBtn)
        }
      })
    },
    enableHash () { // 启用Hash
      this.dialog.deployMessages.push('合约启用中...')
      API.doGet('enableHash/' + this.hash, null, 10).then(res => {
        this.dialog.dialogDetailVisible = true
        if (res.data.errorCode === 0) { // 启用成功，执行系统cpt部署
          this.dialog.deployMessages.push('合约启用成功。')
          this.deploySystemCpt()
        } else {
          this.dialog.deployMessages.push('合约启用失败！请查看日志。')
          this.enableBtn(this.deployBtn)
        }
      })
    },
    deploySystemCpt () { // 部署系统cpt
      this.dialog.deployMessages.push('系统CPT部署中...')
      API.doGet('deploySystemCpt/' + this.hash, null, 10).then(res => {
        if (res.data.errorCode === 0) { // 启用成功，执行系统cpt部署
          this.deployed = true
          this.dialog.deployMessages.push('系统CPT部署成功。')
          this.dialog.deployMessages.push('系统CP部署成功! 请继续操作。')
        } else {
          this.dialog.deployMessages.push('系统CPT部署失败！请查看日志。')
        }
        this.dialog.dialogDetailVisible = true
        this.enableBtn(this.deployBtn)
      })
    },
    prev () {
      localStorage.setItem('step', 4)
      this.$router.push({name: 'step4'})
    },
    setGuideStatus () {
      API.doPost('setGuideStatus', {step: '5'}).then(res => { // 保存选择的角色
        localStorage.setItem('step', '')
        this.$router.push({name: 'deployWeId'})
      })
    }
  },
  mounted () {
    this.checkStep()
  }
}
</script>
