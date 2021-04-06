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
          <div class='guide_step_item guide_step_active'>
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
        <div class="container-fluid">
          <div class="box">
            <div class="card card-primary warning_box">
              <div class="card-header"><h3>配置数据库</h3></div>
              <el-form ref="dbForm" label-position="right" :model="dbForm" label-width="220px">
                <el-form-item label="数据库类型:" prop="persistence_type">
                  <el-select v-model="dbForm.persistence_type" placeholder="数据库类型" style="width: 72%">
                    <el-option label="mysql" value="mysql"></el-option>
                    <el-option label="redis" value="redis"></el-option>
                  </el-select>
                  <div class="mark-bottom"><span>目前暂只支持MySql与Redis数据库</span></div>
                </el-form-item>
                <!--mysql  -->
                <div id="mysqlForm" v-if="dbForm.persistence_type === 'mysql'">
                  <el-form-item label="数据库IP:PORT:" prop="mysql_address">
                    <el-input v-model="dbForm.mysql_address" placeholder="IP:PORT" style="width: 72%" onKeyUp="value=value=value.replace(/[^0-9：:。.]/g,'');value=value.replace(/[。]/g,'.');value=value.replace(/[：]/g,':');value=value.replace(/\s+/g,'');" @blur="dbForm.mysql_address = $event.target.value"></el-input>
                  </el-form-item>
                  <el-form-item label="数据库名称:" prop="mysql_database">
                    <el-input v-model="dbForm.mysql_database" placeholder="Enter DataBase Name" style="width: 72%"></el-input>
                    <div class="mark-bottom"><span>* 数据库需要提前自己搭建好，并建立好数据库，数据库名称(DataBase Name)可以自行决定</span></div>
                  </el-form-item>
                  <el-form-item label="数据库用户名:" prop="mysql_username">
                    <el-input v-model="dbForm.mysql_username" placeholder="Enter username" style="width: 72%"></el-input>
                  </el-form-item>
                  <el-form-item label="数据库密码:" prop="mysql_password">
                    <el-input v-model="dbForm.mysql_password" type="password" placeholder="Enter password" style="width: 72%"></el-input>
                  </el-form-item>
                </div>
                <!-- redis -->
                <div id="redisForm" v-if="dbForm.persistence_type === 'redis'">
                  <el-form-item label="服务器IP:PORT:" prop="redis_address">
                    <el-input v-model="dbForm.redis_address" placeholder="IP:PORT,IP:PORT" style="width: 72%" onKeyUp="value=value=value.replace(/[^0-9：:，,。.]/g,'');value=value.replace(/[。]/g,'.');value=value.replace(/[：]/g,':');value=value.replace(/\s+/g,'');value=value.replace(/[，]/g,',');" @blur="dbForm.redis_address = $event.target.value"></el-input>
                    <div class="mark-bottom"><span>如：127.0.0.1:6379 ；如果多个节点，则请用半角逗号","分割：127.0.0.1:7100,127.0.0.1:7101</span></div>
                  </el-form-item>
                  <el-form-item label="服务器密码（选填):" prop="redis_password">
                    <el-input v-model="dbForm.redis_password" type="password" placeholder="Enter password" style="width: 72%"></el-input>
                    <div class="mark-bottom"><span>如未设置密码可不填写</span></div>
                  </el-form-item>
                </div>
                <div class="bt-part">
                  <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                  <el-button type="primary" @click='jump' class="btn btn_150">跳过</el-button>
                  <el-button type="primary" @click='next($event)' class="btn btn_150">下一步</el-button>
                </div>
              </el-form>
              <div class='sql_warning'>
                <div class='warning_title'>温馨提示</div>
                <p>如果您需要使用到下列功能，则需要配置数据库</p>
                <p>1.Transportation相关组件功能</p>
                <p>2.Evidence异步存证功能</p>
                <p>3.Persistence数据存储功能(例如：存储Credential)</p>
                <p>
                  <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html?highlight=配置数据库#db-configuration" target="blank_" style='display:block'>
                    <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                    <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">配置教程</span>
                  </a>
                </p>
                <p>
                  <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/how-to-use-persistence.html" target="blank_" style='display:block'>
                    <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                    <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">使用教程</span>
                  </a>
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!--显示提交过程 -->
    <el-dialog
      title="温馨提示"
      class="dialog-view" width="400px"
      :visible.sync="dialog.checkDetailVisible"
      :close-on-click-modal="false">
      <div class="dialog-body deployMessage">
        <p v-for="val in dialog.checkMessages" :key="val.value">{{val}}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="checkOrgId" :disabled="!dialog.checkStatus">下一步</el-button>
      </div>
    </el-dialog>

  </div>
</template>
<script>
import API from '../../API/resource'
export default {
  data () {
    return {
      roleType: localStorage.getItem('roleType'),
      dbForm: {
        persistence_type: '',
        mysql_address: '',
        mysql_database: '',
        mysql_username: '',
        mysql_password: '',
        redis_address: '',
        redis_password: ''
      },
      dialog: {
        checkDetailVisible: false,
        checkMessages: [],
        nextBtn: null,
        checkStatus: false
      }
    }
  },
  methods: {
    checkInput () {
      if (this.dbForm.persistence_type === 'mysql') {
        if (this.dbForm.mysql_address === '') {
          this.$alert('请输入数据库 IP 和 PORT !', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.dbForm.mysql_database === '') {
          this.$alert('请输入数据库名称!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.dbForm.mysql_username === '') {
          this.$alert('请输入数据库用户名!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.dbForm.mysql_password === '') {
          this.$alert('请输入数据库密码!', '温馨提示', {}).catch(() => {})
          return false
        }
      } else {
        if (this.dbForm.redis_address === '') {
          this.$alert('请输入Redis的服务 IP 和 PORT !', '温馨提示', {}).catch(() => {})
          return false
        }
      }
      return true
    },
    setGuideStatus () {
      API.doPost('setGuideStatus', {step: '5'}).then(res => { // 保存选择的角色
        localStorage.setItem('step', '')
        this.$router.push({name: 'deployWeId'})
      })
    },
    checkOrgId () {
      API.doPost('checkOrgId', {}).then(res => {
        if (res.data.errorCode === 0) {
          if (res.data.result === 1) { // 转主页
            this.setGuideStatus()
          } else if (res.data.result === 0) { // 创建账户
            localStorage.setItem('step', 4)
            this.$router.push({name: 'step4'})
          } else {
            this.$alert('程序出现异常，请查看日志。', '温馨提示', {}).catch(() => {})
          }
        }
      })
    },
    checkPersistence () {
      this.dialog.checkDetailVisible = true
      this.dialog.checkMessages.push('数据库配置检测中...')
      API.doGet('checkPersistence').then(res => {
        this.dialog.checkDetailVisible = true
        this.enableBtn(this.dialog.nextBtn)
        if (res.data.result) {
          this.dialog.checkStatus = true
          this.dialog.checkMessages.push('数据库配置检测成功。')
          this.dialog.checkMessages.push('请继续操作。')
        } else {
          this.dialog.checkMessages.push('数据库配置检测失败。')
        }
      })
    },
    jump () {
      this.checkOrgId()
    },
    submitDbConfig () {
      // 1. 检查数据库可配置输入
      if (!this.checkInput()) {
        return
      }
      // 2.提交数据库配置
      this.dialog.checkMessages = []
      this.dialog.checkStatus = false
      this.dialog.checkDetailVisible = true
      this.dialog.checkMessages.push('数据库配置提交中...')
      this.disableBtn(this.dialog.nextBtn)
      API.doPost('submitDbConfig', this.dbForm).then(res => {
        this.dialog.checkDetailVisible = true
        if (res.data.errorCode === 0) {
          // 3. 检查数据库配置
          this.dialog.checkMessages.push('数据库配置提交成功。')
          this.checkPersistence()
        } else {
          this.dialog.checkMessages.push('数据库配置提交失败。')
          this.enableBtn(this.dialog.nextBtn)
        }
      })
    },
    next (e) {
      this.dialog.nextBtn = e.currentTarget
      this.submitDbConfig()
    },
    prev () {
      localStorage.setItem('step', 2)
      this.$router.push({name: 'step2'})
    },
    init () {
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.dbForm = res.data.result
          if (this.dbForm.persistence_type !== 'mysql' && this.dbForm.persistence_type !== 'redis') {
            this.dbForm.persistence_type = 'mysql'
          }
        }
      })
      this.$alert('<p>如果您需要使用到下列功能，则需要配置数据库<br/>1.Transportation相关组件功能<br/>2.Evidence异步存证功能<br/>3.Persistence数据存储功能(例如：存储Credential)</p>', '温馨提示', {dangerouslyUseHTMLString: true}).catch(() => {})
    }
  },
  mounted () {
    this.init()
  },
  created () {
    this.checkStep()
  }
}
</script>
