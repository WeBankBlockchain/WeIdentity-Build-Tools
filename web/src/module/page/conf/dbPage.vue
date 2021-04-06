<template>
  <section class="content section_main">
    <div class="box" style="width: 55%; margin-left: 200px">
      <div class="card card-primary warning_box">
        <div class="card-header"><h3>数据库配置</h3></div>
        <div class="card-body">
          <el-form ref="dbForm" label-position="right" :model="dbForm" label-width="150px" inline-message="true">
            <el-form-item label="数据库类型:" prop="persistence_type">
              <el-select v-model="dbForm.persistence_type" placeholder="数据库类型" @change="resetValidate" style="width: 72%">
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
            <div class="db-part">
              <el-button  type="primary" @click="submitDbConfig($event)" class="btn btn_150">完成</el-button>
            </div>
          </el-form>
          <div class='sql_warning bg_color'>
            <div class='warning_title'>温馨提示</div>
            <p>如果您需要使用到下列功能，则需要配置数据库</p>
            <p>1.Transportation相关组件功能</p>
            <p>2.Evidence异步存证功能</p>
            <p>3.Persistence数据存储功能(例如：存储Credential)</p>
            <p>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html?highlight=配置数据库#db-configuration" target="blank_">
                <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">配置教程</span>
              </a>
            </p>
            <p>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/how-to-use-persistence.html" target="blank_">
                <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">使用教程</span>
              </a>
            </p>
          </div>
        </div>
      </div>
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
        <el-button type="primary" class="width_100" @click="dialog.checkDetailVisible = false">完成</el-button>
      </div>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
export default {
  data () {
    return {
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
        nextBtn: null
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
    checkPersistence () {
      this.dialog.checkDetailVisible = true
      this.dialog.checkMessages.push('数据库配置检测中...')
      API.doGet('checkPersistence').then(res => {
        this.dialog.checkDetailVisible = true
        this.enableBtn(this.dialog.nextBtn)
        if (res.data.result) {
          this.dialog.checkMessages.push('数据库配置检测成功。')
          this.dialog.checkMessages.push('提示：目前暂不支持修改配置动态实时生效，修改配置需重启服务才能生效')
        } else {
          this.dialog.checkMessages.push('数据库配置检测失败。')
        }
      })
    },
    submitDbConfig (e) {
      // 1. 检查数据库可配置输入
      if (!this.checkInput()) {
        return
      }
      // 2.提交数据库配置
      this.dialog.nextBtn = e.currentTarget
      this.dialog.checkMessages = []
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
    init () {
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.dbForm = res.data.result
          if (this.dbForm.persistence_type !== 'mysql' && this.dbForm.persistence_type !== 'redis') {
            this.dbForm.persistence_type = 'mysql'
          }
        }
      })
    }
  },
  mounted () {
    this.init()
  }
}
</script>
