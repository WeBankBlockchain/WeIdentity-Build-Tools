<template>
  <section class="content app_main">
    <el-row>
      <el-button type="primary" class="btn" @click="goBack()">返回</el-button>
    </el-row>
    <div class='app_view'>
      <el-table :data="page.dataList" border="true" style="width: 100%;margin-top: 10px;" align="center">
          <el-table-column prop="request_id" label="RequestId" align="center">
            <template slot-scope='scope'>
              <span class='form_option' @click='showDetail(scope.row)'>{{plusXing(scope.row.request_id, 6 , 6)}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="transaction_method" label="Method" align="center"></el-table-column>
          <el-table-column prop="transaction_timestamp" label="TimeStamp" :formatter="dateFormat" align="center"></el-table-column>
          <el-table-column prop="status" label="Status" align="center" >
            <template slot-scope='scope'>
              <span v-if='scope.row.status === 0'>待上链</span>
              <span v-else-if='scope.row.status === 1'>已上链</span>
              <span v-else-if='scope.row.status === 2'>上链失败</span>
              <span v-else>未知状态</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!--显示交易详情 -->
    <el-dialog
      title="交易详情"
      class="dialog-view" width="620px"
      :visible.sync="dialog.dialogDetailVisible"
      :close-on-click-modal="true">
      <div class="dialog-body deployDetail">
        <el-row>
          <el-col :span="4" class="title bold">Batch</el-col>
          <el-col :span="20" class="value">{{dialog.row.batch}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="4" class="title bold">Timestamp</el-col>
          <el-col :span="20" class="value">{{dialog.row.transaction_timestamp}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="4" class="title bold">Method</el-col>
          <el-col :span="20" class="value">{{dialog.row.transaction_method}}</el-col>
        </el-row>
        <el-row>
          <el-col class="title bold">Request Id</el-col>
        </el-row>
        <el-row>
          <el-col class="value">{{dialog.row.request_id}}</el-col>
        </el-row>
        <el-row>
          <el-col class="title bold">Transaction Args</el-col>
        </el-row>
        <el-row>
          <el-col class="value line-height-24">{{dialog.row.transaction_args}}</el-col>
        </el-row>
        <el-row>
          <el-col class="title bold">Extra Data</el-col>
        </el-row>
        <el-row class="none-border">
          <el-col class="value">{{dialog.row.extra}}</el-col>
        </el-row>
      </div>
      <br/>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
export default {
  data () {
    return {
      page: {
        dataList: [],
        total: 0,
        pageIndex: 1,
        pageSize: 8
      },
      dataTime: '',
      dialog: {
        dialogDetailVisible: false,
        row: {}
      }
    }
  },
  methods: {
    goBack () {
      this.$router.push({name: 'asynEvidenceList'})
    },
    showDetail (row) {
      this.dialog.row = row
      this.dialog.dialogDetailVisible = true
    },
    indexChange (currentPage) {
      this.page.pageIndex = currentPage
      this.init()
    },
    init () {
      var formData = {}
      formData.iDisplayStart = (this.page.pageIndex - 1) * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize
      formData.batch = this.dataTime
      formData.status = -1
      API.doPost('getBinLogList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.dataList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
      })
    },
    plusXing (str, frontLen, endLen) {
      var xing = '...'
      return str.substring(0, frontLen) + xing + str.substring(str.length - endLen)
    },
    dateFormat (row) {
      var date = new Date(parseInt(row.transaction_timestamp) + 8 * 3600 * 1000)
      return date.toJSON().substr(0, 19).replace('T', ' ')
    }
  },
  mounted () {
    var dataTime = this.$route.params.dataTime
    if (dataTime === '' || dataTime === undefined) {
      this.dataTime = sessionStorage.getItem('dataTime')
    } else {
      this.dataTime = dataTime
      sessionStorage.setItem('dataTime', this.dataTime)
    }
    this.init()
  }
}
</script>
