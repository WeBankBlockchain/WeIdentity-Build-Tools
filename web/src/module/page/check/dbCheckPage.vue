<template>
  <section class="content section_main">
    <div class="box" style="width: 55%; margin-left: 200px">
      <div class="card card-primary warning_box" >
        <div class="card-header"><h3>检查数据库是否配置成功</h3></div>
        <div class="card-mark">
          <div class="card-mark-text">* 此操作会通过一些简单的Weid测试样例测试数据库是否正常使用，请注意，需要先配置相关数据库。</div>
        </div>
        <div class="card-body">
          <el-form ref="dbForm" :model="dbForm" style="margin-top:20px;">
            <el-form-item prop="persistenceType">
              <el-select v-model="dbForm.persistenceType" placeholder="数据库类型" :disabled="true" style="width: 100%">
                <el-option label="mysql" value="mysql"></el-option>
                <el-option label="redis" value="redis"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <div class="db-part">
          <el-button  type="primary" @click="verifyPersistence" class="btn btn_150">确认</el-button>
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
      dbForm: {
        persistenceType: ''
      }
    }
  },
  methods: {
    verifyPersistence () {
      API.doPost('verifyPersistence', this.dbForm).then(res => {
        if (res.data.result) {
          this.$alert('检查数据库配置成功！', '温馨提示').catch(() => {})
        } else {
          this.$alert('检查数据库配置失败,请核实配置信息！', '温馨提示').catch(() => {})
        }
      })
    },
    init () {
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.dbForm.persistenceType = res.data.result.persistence_type
        }
      })
    }
  },
  mounted () {
    this.init()
  }
}
</script>
