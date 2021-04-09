<template>
  <section class="content app_main">
    <el-row class="app_main_header_row">
      <el-col :span="3">
        <el-button type="primary" @click="showRegistIssuer" class="btn" style="width:170px">注册权威凭证发行者</el-button>
      </el-col>
      <el-col :span="20" class="head-icon" style="padding-left: 18px;">
        <a target="blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=权威凭证发行者#authority-issuer">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question">权威凭证发行者使用教程</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-table :data="page.issuerList" border="true" align="center">
          <el-table-column prop="name" label="权威机构名称" align="center">
            <template slot-scope='scope'>
              <span class='long_words' :title='scope.row.name'>{{scope.row.name}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="weId" label="WeID" align="center" width='260'>
            <template slot-scope='scope'>
              <span class=' link' @click="showAll(scope.row.weId)" :title='scope.row.weId'>{{scope.row.weIdShow}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="recognized" label="状态" align="center" width='100'>
            <template slot-scope='scope'>
              <img v-if="scope.row.recognized === true" class="icon_question" src="../../../assets/image/recognize.svg" widht='50' height='50' alt="">
              <img v-if="scope.row.recognized === false" class="icon_question" src="../../../assets/image/deRecognize.svg" widht='50' height='50' alt="">
            </template>
          </el-table-column>
          <el-table-column prop="description" label="备注" align="center">
            <template slot-scope='scope'>
              <span class='long_words' :title='scope.row.description'>{{scope.row.description}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" align="center" :formatter="dateFormat" width='170'></el-table-column>
          <el-table-column label="操作" align="center"  width='200'>
            <template slot-scope='scope'>
              <el-button  type="primary" @click="deRecognizeAuthorityIssuer(scope.row.weId)" v-if="scope.row.recognized == true" class="btn">撤销</el-button>
              <el-button  type="primary" @click="recognizeAuthorityIssuer(scope.row.weId)" v-else class="btn">认证</el-button>
              <el-button  type="primary" @click="deleteRow(scope.row.weId)" class="btn">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!--注册权威凭证发行者 -->
    <el-dialog
      title="注册权威凭证发行者"
      class="dialog-view" width="29%"
      :visible.sync="dialog.dialogFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.registForm">
          <el-form-item label="机构的WeID" :label-width="formLabelWidth">
            <div class="mark-text">
              <span>说明：由各机构提供其“WeID”和“权威机构名称”，给到联盟链委员会的成员，由成员完成“权威凭证发行者”的注册。</span>
            </div>
            <el-input v-model="dialog.registForm.weId" onkeyup="this.value=this.value.replace(/[^\w：:]/g,'');this.value=this.value.replace(/：/g,':');" @blur="dialog.registForm.weId = $event.target.value" placeholder="填入这个机构WeID"></el-input>
          </el-form-item>
          <el-form-item label="权威机构名称" :label-width="formLabelWidth">
            <div class="mark-text">
              <span>
                输入权威机构的名称（建议由英文或者数字组成，不建议包含类似空格字符的特殊符号，也不支持中文），
                作为类似权威机构 ID 的方式使用，例如如果机构是微众银行，则可以输入类似“webank”作为其权威机构名称
              </span>
            </div>
            <el-input v-model="dialog.registForm.name" onkeyup="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')" @blur="dialog.registForm.name = $event.target.value" placeholder="Enter issuer name"></el-input>
          </el-form-item>
          <el-form-item label="备注" :label-width="formLabelWidth">
            <el-input v-model="dialog.registForm.description" placeholder="Enter issuer description"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="registerIssuer()">注 册</el-button>
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
        issuerList: [],
        pageSize: 5,
        total: 0,
        pageIndex: 1
      },
      dialog: {
        registForm: {
          name: '',
          weId: '',
          description: ''
        },
        dialogFormVisible: false
      }
    }
  },
  methods: {
    showAll (value) {
      this.$alert(value, '温馨提示', {}).catch(() => {})
    },
    showRegistIssuer () {
      if (check.data.nodeStatus) {
        this.dialog.registForm.name = ''
        this.dialog.registForm.weId = ''
        this.dialog.registForm.description = ''
        this.dialog.dialogFormVisible = true
      } else {
        check.checkNodeState(true)
      }
    },
    registerIssuer () {
      if (this.dialog.registForm.weId === '') {
        this.$alert('请输入WeID!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.registForm.name === '') {
        this.$alert('请输入权威机构名称!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.registForm.name.indexOf('@') === 0) {
        this.$alert('权威机构名称输入非法!', '温馨提示', {}).catch(() => {})
        return
      }
      API.doPost('registerIssuer', this.dialog.registForm).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('权威凭证发行者注册成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogFormVisible = false
          }).catch(() => {})
          this.dialog.registForm.weId = ''
          this.dialog.registForm.name = ''
          this.dialog.registForm.description = ''
          this.page.pageIndex = 1
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    deleteRow (weId) {
      this.$confirm('确认删除该权威机构？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          var formData = {}
          formData.weId = weId
          API.doPost('removeIssuer', formData).then(res => {
            if (res.data.errorCode === 0) {
              this.$alert('删除成功!', '温馨提示', {}).then(() => {}).catch(() => {})
              this.init()
            } else {
              this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
            }
          })
        }).catch(() => {})
    },
    recognizeAuthorityIssuer (weId) {
      this.$confirm('是否确定认证该权威机构？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          var formData = {}
          formData.weId = weId
          API.doPost('recognizeAuthorityIssuer', formData).then(res => {
            if (res.data.errorCode === 0) {
              this.$alert('认证成功!', '温馨提示', {}).then(() => {}).catch(() => {})
              this.init()
            } else {
              this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
            }
          })
        }).catch(() => {})
    },
    deRecognizeAuthorityIssuer (weId) {
      this.$confirm('是否确定撤销该权威机构认证？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          var formData = {}
          formData.weId = weId
          API.doPost('deRecognizeAuthorityIssuer', formData).then(res => {
            if (res.data.errorCode === 0) {
              this.$alert('撤销成功!', '温馨提示', {}).then(() => {}).catch(() => {})
              this.init()
            } else {
              this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
            }
          })
        }).catch(() => {})
    },
    indexChange (currentPage) {
      if (currentPage < 1) {
        currentPage = 1
      }
      this.page.pageIndex = currentPage
      this.queryIssueList()
    },
    queryIssueList () {
      var formData = {}
      formData.iDisplayStart = (this.page.pageIndex - 1) * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize
      API.doGet('getIssuerList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.issuerList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
        if (this.page.total !== 0 && this.page.total === (this.page.pageSize * (this.page.pageIndex - 1))) {
          this.indexChange(this.page.pageIndex - 1)
        }
      })
    },
    init () {
      this.queryIssueList()
    },
    dateFormat (row) {
      var date = new Date(parseInt(row.createTime * 1000) + 8 * 3600 * 1000)
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
