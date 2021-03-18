<template>
  <section class="content app_main">
    <el-form ref="queryForm" :model="queryForm" label-width="80px" class="item">
      <el-row style="width: 1200px">
        <el-col :span="5">
          <el-form-item prop="dataTime" label="任务日期:">
            <el-date-picker v-model="queryForm.dataTime" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width:160px;"></el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item prop="status" label="任务状态:">
            <el-select v-model="queryForm.status"  style="width: 140px">
              <el-option label="全部" value="-1"></el-option>
              <el-option label="处理中" value="1"></el-option>
              <el-option label="处理成功" value="2"></el-option>
              <el-option label="处理失败" value="3"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-button  type="primary" @click="init()" :disabled="!dbStatus" class="btn">查询</el-button>
        </el-col>
      </el-row>
    </el-form>
    <div class='app_view'>
      <el-table :data="page.evidenceAsynList" border="true" align="center">
          <el-table-column prop="data_time" label="任务日期" align="center">
            <template slot-scope='scope'>
              <span class='form_option' @click='showDetail(scope.row.data_time)'>{{scope.row.data_time}}</span>
            </template>
          </el-table-column>
          <el-table-column label="任务状态" align="center">
            <template slot-scope='scope'>
              <span v-if='scope.row.status === 1'>处理中</span>
              <span v-else-if='scope.row.status === 2'>处理成功</span>
              <span v-else-if='scope.row.status === 3'>处理失败</span>
              <span v-else>未知状态</span>
            </template>
          </el-table-column>
          <el-table-column prop="all_size" label="总记录数" align="center" :formatter="dateFormat"></el-table-column>
          <el-table-column prop="success_size" label="成功数" align="center"></el-table-column>
          <el-table-column prop="fail_size" label="失败数" align="center" ></el-table-column>
          <el-table-column label="操作" align="center">
            <template slot-scope='scope'>
              <el-button  type="primary" @click="reTry(scope.row.data_time)" v-if='scope.row.status === 3' class="btn">重试</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>
  </section>
</template>
<script>
import API from '../../../API/resource'
import check from '../../../assets/utils/checkConfig'
export default {
  data () {
    return {
      page: {
        evidenceAsynList: [],
        total: 0,
        pageIndex: 1,
        pageSize: 7
      },
      queryForm: {
        dataTime: '',
        status: '-1'
      },
      total: 100,
      dbStatus: false
    }
  },
  methods: {
    showDetail (value) {
      this.$router.push({name: 'asynEvidenceDetail', params: {dataTime: value}})
    },
    reTry (dataTime) {
      this.$confirm('确定重试该批次吗？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          var formData = {}
          formData.dataTime = dataTime
          API.doPost('reTryAsyn', formData).then(res => {
            this.$alert('异步处理中，请注意查看数据。', '温馨提示').catch(() => {})
          })
        }).catch(() => {})
    },
    indexChange (currentPage) {
      this.page.pageIndex = currentPage
      this.init()
    },
    init () {
      var formData = {}
      formData.iDisplayStart = (this.page.pageIndex - 1) * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize
      var time = this.queryForm.dataTime
      if (time !== null && time !== '') {
        time = time.replace(/-/g, '')
      } else {
        time = 0
      }
      formData.dataTime = time
      formData.status = this.queryForm.status
      API.doPost('getAsyncList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.evidenceAsynList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
      })
    },
    async check () {
      await check.checkDbState()
      if (check.data.dbStatus) {
        this.init()
        this.dbStatus = check.data.dbStatus
      }
    }
  },
  mounted () {
    // 检查数据库配置是否正确
    this.check()
  }
}
</script>
