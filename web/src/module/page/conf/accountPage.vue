<template>
  <section class="content section_main">
    <div class="box" style="width: 55%; margin-left: 200px">
      <!-- general form elements -->
      <div id="AccountDiv" class="card card-primary warning_box">
        <div class="card-header"><h3>WeID 账户</h3></div>
        <div class="card-mark">
          <div class="card-mark-text">当前管理员的 WeID</div>
          <div class="card-mark-icon">
            <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_">
              <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
              <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">什么是管理员？</span>
            </a>
          </div>
        </div>
        <div class="card-body">
          <div style="margin-top: 15px">
            <form role="form" id="AccountForm">
              <div class="form-group" id="accountDiv">
                <el-form ref="accountForm" :model="accountForm">
                  <el-form-item prop="account">
                    <el-input v-model="accountForm.account" placeholder="Enter account" readOnly></el-input>
                  </el-form-item>
                </el-form>
              </div>
            </form>
            <div class='sql_warning bg_color' style='right:-160px'>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#weid" target="blank_">
                <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
              </a>
            </div>
          </div>
          <div class="db-part">
            <el-button type="primary" @click="showUserList" class="btn btn_150" v-if="useWeBase">切换账户</el-button>
          </div>
        </div>
      </div>
    </div>

    <!--显示账户列表 -->
    <el-dialog
      title="WeBASE账户列表"
      class="dialog-view"
      width="40%"
      :visible.sync="dialog.dialogUserListVisible"
      :close-on-click-modal="false">
      <el-table :data="dialog.userListPage.userList" border="true" cellpadding="0" cellspacing="0" >
        <el-table-column label="选择" width="55">
          <template slot-scope="scope">
            <el-radio v-model="dialog.userListPage.selectedRow" :label="scope.row"><span></span></el-radio>
          </template>
        </el-table-column>
        <el-table-column label="用户名">
          <template slot-scope='scope'>
            <span class='long_words' :title='scope.row.userName'>{{scope.row.userName}}</span>
          </template>
        </el-table-column>
        <el-table-column property="address" label="用户账户" width="350"></el-table-column>
        <el-table-column property="createTime" label="创建时间" width="170"></el-table-column>
      </el-table>
      <el-pagination @current-change="indexChange" :current-page="dialog.userListPage.pageIndex"
      :page-size="dialog.userListPage.pageSize" layout="total, prev, pager, next, jumper" :total="dialog.userListPage.total"></el-pagination>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="chooseUser">确定</el-button>
      </div>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
export default {
  data () {
    return {
      accountForm: {
        account: ''
      },
      dialog: {
        dialogUserListVisible: false,
        userListPage: {
          userList: [],
          pageIndex: 1,
          pageSize: 5,
          total: 0,
          selectedRow: null
        }
      },
      useWeBase: false
    }
  },
  methods: {
    createAdminByWeBase () {
      var formData = {}
      formData.userId = this.dialog.userListPage.selectedRow.userId
      API.doPost('webase/createAdmin', formData).then(res => { // 创建账户
        if (res.data.errorCode === 0) {
          this.$alert('账户切换成功!', '温馨提示', {}).catch(() => {})
          this.dialog.dialogUserListVisible = false
          this.init()
        } else {
          this.$alert('账户切换失败!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    showUserList () {
      this.queryUserList()
    },
    chooseUser () {
      if (this.dialog.userListPage.selectedRow === null) {
        this.$alert('请选择用户!', '温馨提示', {}).catch(() => {})
        return
      }
      var user = this.dialog.userListPage.selectedRow.address
      this.$confirm('确定使用[' + user + ']作为WeID的账户吗?', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary', dangerouslyUseHTMLString: true})
        .then(_ => {
          this.createAdminByWeBase()
        }).catch(() => {})
    },
    indexChange (currentPage) {
      this.dialog.userListPage.pageIndex = currentPage
      this.queryUserList()
    },
    queryUserList () {
      var formData = {}
      formData.pageIndex = this.dialog.userListPage.pageIndex
      formData.pageSize = this.dialog.userListPage.pageSize
      API.doGet('webase/queryUserList', formData).then(res => { // 获取WeBase用户列表信息
        if (res.data.errorCode === 0) {
          this.dialog.userListPage.userList = res.data.result.dataList
          this.dialog.userListPage.total = res.data.result.allCount
          this.dialog.dialogUserListVisible = true
        }
        this.dialog.userListPage.selectedRow = null
      })
    },
    loadConfig () {
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.useWeBase = JSON.parse(res.data.result.useWeBase)
        }
      })
    },
    init () {
      API.doGet('checkAdmin').then(res => { // 检查账户是否存证
        if (res.data.result !== '') { // 账户存在
          this.accountForm.account = res.data.result
          this.loadConfig()
        }
      })
    }
  },
  mounted () {
    this.init()
  }
}
</script>
