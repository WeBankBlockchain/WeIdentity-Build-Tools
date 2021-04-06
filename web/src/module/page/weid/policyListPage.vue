<template>
  <section class="content app_main">
    <div class='app_view'>
      <el-table :data="page.policyList" border="true" align="center">
          <el-table-column prop="id" label="披露策略ID" align="center"></el-table-column>
          <el-table-column label="操作" align="center">
            <template slot-scope='scope'>
              <el-button  type="primary" @click="showCpt(scope.row.id)" class="btn" style="width:90px">预览Policy</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!-- 预览policy -->
    <el-dialog
      title="预览policy"
      class="dialog-view" width="40%"
      :visible.sync="dialog.dialogShowPolicyVisible">
      <div class="dialog-body">
        <el-row>
          <el-col>
            <el-input type="textarea" readOnly :rows="15" resize=none v-model="dialog.policy"></el-input>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
import check from '../../../assets/utils/checkConfig'
export default {
  data () {
    return {
      page: {
        policyList: [],
        total: 0,
        pageIndex: 1,
        pageSize: 6
      },
      dialog: {
        policy: '',
        dialogShowPolicyVisible: false
      }
    }
  },
  methods: {
    showCpt (policyId) {
      API.doGet('queryPolicy/' + policyId).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.policy = res.data.result
          this.dialog.dialogShowPolicyVisible = true
        } else {
          this.$alert('Policy查询异常!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    indexChange (currentPage) {
      this.page.pageIndex = currentPage
      this.init()
    },
    init () {
      var formData = {}
      formData.iDisplayStart = (this.page.pageIndex - 1) * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize
      API.doGet('getPolicyList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.policyList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
      })
    },
    async check () {
      await check.checkNodeState(true)
      if (check.data.nodeStatus) {
        this.init()
      }
    }
  },
  mounted () {
    this.check()
  }
}
</script>
