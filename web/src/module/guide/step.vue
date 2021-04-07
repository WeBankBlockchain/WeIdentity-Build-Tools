<template>
  <div class="app_view_content">
    <div class="app_view_register">
      <section class="content">
        <div class='container-fluid guild-step' style="padding-top:10px">
          <div class="box">
            <div id="AccountDiv" class="card card-primary warning_box">
              <div class="card-header">
                <h6>请选择配置模式</h6>
              </div>
              <div class="card-mark">
                <div class="card-mark-text">
                  <div>1. WeID原始模式: 原WeID的引导配置步骤。</div>
                  <div>2. WeID+WeBASE集成模式: 原WeID的引导配置过程中可选择WeBASE同步的数据,如: 节点，账户等数据，且合约部署会同步到WeBASE。</div>
                </div>
              </div>
              <el-form ref="dataForm" :model="dataForm" style="margin-top:15px" label-position="right" label-width="150px">
                <div class="form-group" id="createDiv">
                  <div :class="{'key_item active_key': configMode === 1, 'key_item': configMode !== 1}" type='1' @click="active(1)">
                    <span class='item_out_role'><span></span></span>
                    <p>WeID原始模式</p>
                  </div>
                  <div :class="{'key_item active_key': configMode === 2, 'key_item': configMode !== 2}" type='1' @click="active(2)">
                    <span class='item_out_role'><span></span></span>
                    <p>WeID + WeBASE集成模式</p>
                  </div>
                </div>
                <div style="margin-top: 30px" v-if = "configMode === 2">
                  <div class="card-header card-title"><h3>WeBASE配置</h3></div>
                  <el-form-item label="粘贴区:" prop="org_id">
                    <el-input v-model="dataForm.text" placeholder="Your can copy data from WeBASE" resize='none'  @keyup.native="inputChange()"  type="textarea" style="width: 72%"></el-input>
                    <div class="mark-bottom">
                      <div>请从WeBASE管理台复制此应用的配置内容，然后粘贴此处。</div>
                    </div>
                  </el-form-item>
                  <el-form-item label="WeBASE服务地址:" prop="org_id">
                    <el-input v-model="dataForm.address" placeholder="Enter IP:PORT" maxlength="30"  style="width: 72%" onKeyUp="value=value=value.replace(/[^0-9：:。.]/g,'');value=value.replace(/[。]/g,'.');value=value.replace(/[：]/g,':');value=value.replace(/\s+/g,'');" @blur="dataForm.address = $event.target.value"></el-input>
                  </el-form-item>
                  <el-form-item label="appKey:" prop="org_id">
                    <el-input v-model="dataForm.appKey" placeholder="Enter appKey" maxlength="30"  style="width: 72%"></el-input>
                  </el-form-item>
                  <el-form-item label="appSecret:" prop="org_id">
                    <el-input v-model="dataForm.appSecret" placeholder="Enter appSecret" maxlength="30"  style="width: 72%"></el-input>
                  </el-form-item>
                </div>
              </el-form>
              <div class="card-footer bt-part" id="role-next">
                <el-button type="primary" @click='next($event)' class="btn btn_150">下一步</el-button>
              </div>
              <div class='sql_warning' style='right:-160px'>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#mode-selection" target="blank_" style='display:block'>
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
      dataForm: {
        address: '',
        appKey: '',
        appSecret: '',
        text: ''
      },
      configMode: 1
    }
  },
  methods: {
    active (type) {
      localStorage.setItem('configMode', type)
      this.configMode = type
    },
    inputChange () {
      var clear = true
      if (this.dataForm.text !== '' && this.dataForm.text !== null) {
        var dataArray = this.dataForm.text.split(',')
        if (dataArray.length >= 3) {
          this.dataForm.address = dataArray[0]
          this.dataForm.appKey = dataArray[1]
          this.dataForm.appSecret = dataArray[2]
          clear = false
          sessionStorage.setItem('weBaseText', this.dataForm.text)
        }
      }
      if (clear) {
        this.dataForm.address = ''
        this.dataForm.appKey = ''
        this.dataForm.appSecret = ''
      }
    },
    next (e) {
      if (this.configMode === 1) {
        this.setNoWeBaseMode()
      } else {
        this.setWeBaseMode(e)
      }
    },
    setNoWeBaseMode () {
      API.doPost('webase/setNoWeBaseMode').then(res => { // 非WeBase配置模式
        if (res.data.errorCode === 0) {
          localStorage.setItem('step', 0)
          this.$router.push({name: 'step0'})
        }
      })
    },
    setWeBaseMode (e) {
      var formData = {}
      formData.weBaseHost = this.dataForm.address
      formData.appKey = this.dataForm.appKey
      formData.appSecret = this.dataForm.appSecret
      formData.weIdHost = window.location.host
      if (formData.weBaseHost === '') {
        this.$alert('请输入WeBASE服务地址!', '温馨提示', {}).catch(() => {})
        return
      }
      if (formData.appKey === '') {
        this.$alert('请输入appKey!', '温馨提示', {}).catch(() => {})
        return
      }
      if (formData.appSecret === '') {
        this.$alert('请输入appSecret!', '温馨提示', {}).catch(() => {})
        return
      }
      var nextBtn = e.currentTarget
      this.disableBtn(nextBtn)
      API.doPostByJson('webase/registerApp', formData).then(res => { // WeBase配置模式
        this.enableBtn(nextBtn)
        if (res.data.errorCode === 0) {
          localStorage.setItem('step', 0)
          this.$router.push({name: 'step0'})
        } else {
          this.$alert('请求发生异常，请查看后台日志!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    init () {
      let type = localStorage.getItem('configMode')
      if (type === '' || type === null) {
        this.configMode = 1
      } else {
        this.configMode = parseInt(type)
      }
      this.dataForm.text = sessionStorage.getItem('weBaseText')
      this.inputChange()
    }
  },
  mounted () {
    this.init()
  }
}
</script>
