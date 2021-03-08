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
          <div class='guide_step_item guide_step_active'>
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
        <div class="container-fluid">
          <!-- Small boxes (Stat box) -->
          <div class="box">
            <!-- general form elements -->
            <div id="AccountDiv" class="card card-primary warning_box">
              <div class="card-header"><h3>{{title}}</h3></div>
              <div class="card-mark">
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_" style='display:block'>
                  <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">什么是管理员？</span>
               </a>
              </div>
              <div class="card-body" style="margin-top: 15px">
                <el-form ref="accountForm" :model="accountForm">
                  <div class="form-group" id="createDiv" v-if='isExistsAdmin === false'>
                    <div :class="{'key_item active_key': createType === 1, 'key_item': createType !== 1}" type='1' @click="active(1)">
                      <span class='item_out_role'><span></span></span>
                      <p>系统自动创建公私钥</p>
                    </div>
                    <div :class="{'key_item active_key': createType === 2, 'key_item': createType !== 2}" type='1' @click="active(2)">
                      <span class='item_out_role'><span></span></span>
                      <p>自行上传私钥</p>
                    </div>
                  </div>
                  <div class="form-group" id="accountDiv" v-if='isExistsAdmin === true'>
                    <el-form-item prop="account">
                      <el-input v-model="accountForm.account" style="width: 100%" readOnly></el-input>
                    </el-form-item>
                  </div>
                  <div class="bt-part" id="nextDiv">
                    <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                    <el-button type="primary" @click='next' class="btn btn_150">下一步</el-button>
                  </div>
                </el-form>
                <div class='sql_warning' style='right:-160px'>
                  <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_" style='display:block'>
                    <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                    <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 自行上传私钥 -->
        <el-dialog
          title="自行上传私钥"
          class="dialog-view" width="30%"
          :visible.sync="dialog.dialogVisible"
          :close-on-click-modal="false">
          <div class="dialog-body">
            <el-form :model="dialog.fileForm">
              <div class='file_part' style="margin-top:15px;">
                <el-form-item>
                  <div class="mark-text">
                    <span>备注：支持椭圆曲线的公私钥，并且为十进制私钥数据。</span>
                    <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=创建密钥#weid" target="blank_">
                      <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                      <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">如何生成私钥？</span>
                    </a>
                  </div>
                  <div class='input_item' style="margin-bottom:-8px;">
                    <el-input v-model="dialog.fileForm.privateKeyFileName" placeholder="请选择私钥文件" maxlength="30" readOnly style="width: 75%"></el-input>
                    <button type="button" @click="chooseFile('privateKeyFile')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                  </div>
                </el-form-item>
                <input type="file" id ='privateKeyFile' style="display:none;" >
              </div>
            </el-form>
          </div>
          <div slot="footer" class="dialog-footer width_100">
            <el-button type="primary" class="width_100" @click="createAdmin()">确 定</el-button>
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
      roleType: localStorage.getItem('roleType'),
      createType: 1,
      accountForm: {
        account: ''
      },
      dialog: {
        dialogVisible: false,
        fileForm: {
          privateKeyFile: '',
          privateKeyFileName: ''
        }
      },
      isExistsAdmin: false,
      title: '创建管理员WeID'
    }
  },
  methods: {
    active (type) {
      this.createType = type
    },
    createAdmin () {
      var formData = new FormData()
      if (this.createType === 2) {
        if (this.dialog.fileForm.privateKeyFile === '') {
          this.$alert('请选择私钥文件!', '温馨提示', {}).catch(() => {})
          return
        }
        formData.append('ecdsa', this.dialog.fileForm.privateKeyFile)
      }
      API.doPostAndUploadFile('createAdmin', formData).then(res => { // 上传配置
        if (res.data.errorCode === 0) {
          this.$alert('账户创建成功!', '温馨提示', {}).catch(() => {})
          this.init()
        } else {
          this.$alert('账户创建失败!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    setGuideStatus () {
      API.doPost('setGuideStatus', {step: '5'}).then(res => { // 保存选择的角色
        localStorage.setItem('step', 0)
        this.$router.push({name: 'deployWeId'})
      })
    },
    next () {
      // 判断是否存在账号
      if (this.isExistsAdmin) { // 表示存在
        if (this.roleType === '2') { // 到主页
          this.setGuideStatus()
        } else {
          this.$router.push({name: 'step5'})
        }
      } else { // 表示不存在
        if (this.createType === 2) { // 上传私钥方式创建
          this.dialog.dialogVisible = true
        } else { // 系统方式创建
          this.$confirm('请确认，系统将自动为管理员的 WeID 创建公私钥?', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary', dangerouslyUseHTMLString: true})
            .then(_ => {
              this.createAdmin()
            }).catch(() => {})
        }
      }
    },
    prev () {
      this.$router.push({name: 'step3'})
    },
    chooseFile (type) {
      var caCrtFileInput = document.getElementById(type)
      caCrtFileInput.value = ''
      caCrtFileInput.click()
      caCrtFileInput.onchange = file => {
        if (file.target.files[0]) {
          this.dialog.fileForm.privateKeyFileName = file.target.files[0].name
          this.dialog.fileForm.privateKeyFile = file.target.files[0]
        }
      }
    },
    init () {
      API.doGet('checkAdmin').then(res => { // 检查账户是否存证
        if (res.data.result !== '') { // 账户存在
          this.isExistsAdmin = true
          this.title = '当前管理员的 WeID 已经存在（目前不支持修改）'
          this.accountForm.account = res.data.result
        } else { // 账户不存在
          this.isExistsAdmin = false
          this.title = '创建管理员WeID'
        }
      })
      this.dialog.dialogVisible = false
    }
  },
  created () {
    localStorage.setItem('step', 4)
    this.init()
  }
}
</script>
