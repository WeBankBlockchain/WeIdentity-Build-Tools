<template>
  <section class="content app_main">
    <el-row class="app_main_header_row">
      <el-col :span="2">
        <el-button type="primary" @click="showRegistWhiteList" class="btn" style="width:100px">新增白名单</el-button>
      </el-col>
      <el-col :span="22" class="head-icon">
        <a target="blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=白名单#id4">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question">白名单使用教程</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-table :data="page.whiteList" border="true" align="center">
        <el-table-column prop="type" label="白名单名称" align="center">
          <template slot-scope='scope'>
            <span class='long_words' :title='scope.row.typeName'>{{scope.row.typeName}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="created" label="创建时间" :formatter="dateFormat" align="center" ></el-table-column>
        <el-table-column label="操作" align="center" width='450'>
          <template slot-scope='scope'>
            <el-button  type="primary" class="btn" @click="deleteWhiteList(scope.row.typeName, $event)" style="width:100px;">删除白名单</el-button>
            <el-button  type="primary" class="btn" @click="showAllIssuer(scope.row.typeName)" style="width:100px;">查看白名单</el-button>
            <el-button  type="primary" class="btn" @click="showAddIssuer(scope.row.typeName)" style="width:180px;">添加WeID到这个白名单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!--新增白名单 -->
    <el-dialog
      title="新增白名单"
      class="dialog-view" width="29%"
      :visible.sync="dialog.dialogaddwhiteListVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.addwhiteListForm">
          <el-form-item label="新增白名单名称" :label-width="formLabelWidth">
            <el-input v-model="dialog.addwhiteListForm.issuerType" onkeyup="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')"  @blur="dialog.addwhiteListForm.issuerType = $event.target.value" placeholder="Enter new whitelist name"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="registerWhiteList()">新 增</el-button>
      </div>
    </el-dialog>

    <!--添加到白名单 -->
    <el-dialog
      title="添加到白名单"
      class="dialog-view" width="29%"
      :visible.sync="dialog.dialogwhiteFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.whiteListForm">
          <el-form-item label="WeID" :label-width="formLabelWidth">
            <el-input v-model="dialog.whiteListForm.weId" onkeyup="this.value=this.value.replace(/[^\w：:]/g,'');this.value=this.value.replace(/：/g,':');" @blur="dialog.whiteListForm.weId = $event.target.value"  placeholder="Enter WeID"></el-input>
          </el-form-item>
          <el-form-item label="白名单名称" :label-width="formLabelWidth">
            <el-input v-model="dialog.whiteListForm.issuerType" disabled></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="addWeIdToWhiteList()">添 加</el-button>
      </div>
    </el-dialog>

    <!--显示白名单列表 -->
    <el-dialog
      title="白名单用户列表"
      class="dialog-view"
      width="55%"
      :visible.sync="dialog.dialogTableVisible"
      :close-on-click-modal="false">
      <el-table :data="dialog.whiteWeIdListPage.whiteWeIdList" border="true" cellpadding="0" cellspacing="0">
        <el-table-column property="weId" label="WeID"></el-table-column>
        <el-table-column property="name" label="权威机构名称" width="250">
          <template slot-scope='scope' v-if="scope.row.name !== ''">
            {{scope.row.name}}
            <img v-if="scope.row.recognized === true" class="icon_question" src="../../../assets/image/recognize.svg" widht='50' height='50' alt="">
            <img v-if="scope.row.recognized === false" class="icon_question" src="../../../assets/image/deRecognize.svg" widht='50' height='50' alt="">
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope='scope'>
            <el-button  type="primary" @click="deleteRow(scope.row.weId)" class="btn">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination @current-change="indexChange2" :current-page="dialog.whiteWeIdListPage.pageIndex" :page-size="dialog.whiteWeIdListPage.pageSize" layout="total, prev, pager, next, jumper" :total="dialog.whiteWeIdListPage.total"></el-pagination>
      <br/>
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
        whiteList: [],
        pageIndex: 1,
        pageSize: 6,
        total: 0,
        issuerCount: 0
      },
      dialog: {
        addwhiteListForm: {
          issuerType: ''
        },
        dialogaddwhiteListVisible: false,
        whiteListForm: {
          issuerType: '',
          weId: ''
        },
        dialogwhiteFormVisible: false,
        whiteWeIdListPage: {
          whiteWeIdList: [],
          pageIndex: 1,
          pageSize: 5,
          total: 0
        },
        deleteType: '',
        dialogTableVisible: false
      }
    }
  },
  methods: {
    showRegistWhiteList () {
      if (check.data.nodeStatus) {
        this.dialog.dialogaddwhiteListVisible = true
      } else {
        check.checkNodeState(true)
      }
    },
    deleteRow (weId) {
      this.$confirm('确定删除该白名单用户？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          var formData = {}
          formData.issuerType = this.dialog.deleteType
          formData.weId = weId
          API.doPost('removeIssuerFromIssuerType', formData).then(res => {
            if (res.data.errorCode === 0) {
              this.$alert('删除成功!', '温馨提示', {closeOnClickModal: 'true'}).catch(() => {})
              this.getAllIssuerInType(this.dialog.deleteType)
            } else {
              this.$alert('删除失败!', '温馨提示', {closeOnClickModal: 'true'}).catch(() => {})
            }
          })
        }).catch((err) => {
          console.log(err)
        })
    },
    showAddIssuer (issuerType) {
      this.dialog.whiteListForm.issuerType = issuerType
      this.dialog.whiteListForm.weId = ''
      this.dialog.dialogwhiteFormVisible = true
    },
    showAllIssuer (issuerType) {
      this.dialog.whiteWeIdListPage.whiteWeIdList = []
      this.dialog.whiteWeIdListPage.pageIndex = 1
      this.getAllIssuerInType(issuerType)
    },
    getAllIssuerInType (issuerType) {
      var formData = {}
      formData.issuerType = issuerType
      formData.iDisplayStart = (this.dialog.whiteWeIdListPage.pageIndex - 1) * this.dialog.whiteWeIdListPage.pageSize
      formData.iDisplayLength = this.dialog.whiteWeIdListPage.pageSize
      this.dialog.deleteType = issuerType
      API.doPost('getIssuerListInType', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.whiteWeIdListPage.whiteWeIdList = res.data.result.dataList
          this.dialog.whiteWeIdListPage.total = res.data.result.allCount
        }
        if (this.dialog.whiteWeIdListPage.total !== 0 && this.dialog.whiteWeIdListPage.total === (this.dialog.whiteWeIdListPage.pageSize * (this.dialog.whiteWeIdListPage.pageIndex - 1))) {
          this.indexChange2(this.dialog.whiteWeIdListPage.pageIndex - 1)
        }
        this.dialog.dialogTableVisible = true
      })
    },
    addWeIdToWhiteList () {
      if (this.dialog.whiteListForm.weId === '') {
        this.$alert('请输入WeID!', '温馨提示', {}).catch(() => {})
        return
      }
      API.doPost('addIssuerIntoIssuerType', this.dialog.whiteListForm).then(res => { // 保存选择的角色
        if (res.data.errorCode === 0) {
          this.$alert('添加白名单成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogwhiteFormVisible = false
          }).catch(() => {})
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    registerWhiteList () {
      if (this.dialog.addwhiteListForm.issuerType === '') {
        this.$alert('请输入白名单名称!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.addwhiteListForm.issuerType.indexOf('@') === 0) {
        this.$alert('白名单名称输入非法!', '温馨提示', {}).catch(() => {})
        return
      }
      API.doPost('registerIssuerType', this.dialog.addwhiteListForm).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('白名单新增成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogaddwhiteListVisible = false
          }).catch(() => {})
          this.dialog.addwhiteListForm.issuerType = ''
          this.page.pageIndex = 1
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    async deleteWhiteList (typeName, e) {
      var target = e.currentTarget
      let thiObj = this
      await this.queryIssuerCountByType(typeName)
      if (this.page.issuerCount !== 0) {
        this.$alert('目前此白名单【' + typeName + '】下面拥有' + this.page.issuerCount + '个白名单用户,暂时无法删除!', '温馨提示', {}).catch(() => {})
        return
      }
      this.$confirm('确认删除白名单【' + typeName + '】吗?', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary', dangerouslyUseHTMLString: true})
        .then(_ => {
          thiObj.disableBtn(target)
          this.removeWhiteList(typeName, target)
        }).catch(() => {})
    },
    removeWhiteList (typeName, target) {
      API.doPost('removeIssuerType', {issuerType: typeName}).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('删除成功!', '温馨提示').catch(() => {})
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
        this.enableBtn(target)
      })
    },
    async queryIssuerCountByType (typeName) {
      await API.doGet('getSpecificTypeIssuerSize', {issuerType: typeName}).then(res => {
        this.page.issuerCount = res.data.result
      })
    },
    indexChange2 (currentPage) {
      if (currentPage < 1) {
        currentPage = 1
      }
      this.dialog.whiteWeIdListPage.pageIndex = currentPage
      this.getAllIssuerInType(this.dialog.deleteType)
    },
    indexChange (currentPage) {
      this.page.pageIndex = currentPage
      this.queryIssueTypeList()
    },
    queryIssueTypeList () {
      var formData = {}
      formData.iDisplayStart = (this.page.pageIndex - 1) * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize
      API.doGet('getIssuerTypeList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.whiteList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
        if (this.page.total !== 0 && this.page.total === (this.page.pageSize * (this.page.pageIndex - 1))) {
          this.indexChange(this.page.pageIndex - 1)
        }
      })
    },
    init () {
      this.queryIssueTypeList()
    },
    dateFormat (row) {
      var date = new Date(parseInt(row.created) + 8 * 3600 * 1000)
      return date.toJSON().substr(0, 19).replace('T', ' ')
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
