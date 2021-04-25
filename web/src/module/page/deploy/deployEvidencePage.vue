<template>
  <section class="content app_main">
    <el-row class="app_main_header_row">
      <el-col :span="3">
        <el-button type="primary" class="btn"  @click="showDeploy()" style="width:150px">部署存证智能合约</el-button>
      </el-col>
      <el-col :span="21" class="head-icon">
        <a target="blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=Evidence#evidence">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question">存证智能合约部署使用教程</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-table :data="page.deployList.slice((page.pageIndex-1)*page.pageSize, page.pageIndex*page.pageSize)" border="true" align="center" style="width: 100%;" cellpadding="0" cellspacing="0">
          <el-table-column prop="evidenceName" label="应用名字" align="center">
            <template slot-scope='scope'>
              <span class='long_words' :title='scope.row.evidenceName'>{{scope.row.evidenceName}}</span>
            </template>
          </el-table-column>
           <el-table-column label="智能合约部署ID" align="center" width='160'>
            <template slot-scope='scope'>
              <span class='long_words link' @click="showAll(scope.row.hash)" :title='scope.row.hash'>{{scope.row.hashShow}}</span>
            </template>
          </el-table-column>
          <el-table-column label="部署的机构名称" align="center" width='150'>
            <template slot-scope='scope' v-if='scope.row.issuer !== null'>
              <span class='icon_question' :title='scope.row.issuer.name'>{{scope.row.issuer.name}}</span>
              <img v-if="scope.row.issuer.recognized === true" class="icon_question" src="../../../assets/image/recognize.svg" widht='50' height='50' alt="">
              <img v-if="scope.row.issuer.recognized === false" class="icon_question" src="../../../assets/image/deRecognize.svg" widht='50' height='50' alt="">
            </template>
          </el-table-column>
          <el-table-column prop="showGroupId" label="所在群组" align="center" width='100'></el-table-column>
          <el-table-column prop="createTime" label="创建时间" align="center" width='170'></el-table-column>
          <el-table-column label="操作" align="center" width='420'>
            <template slot-scope='scope'>
              <el-button  type="primary" class="btn" v-if='scope.row.enable' disabled>已启用</el-button>
              <el-button  type="primary" class="btn" @click="enable(scope.row.hash, scope.row.groupId, $event)" v-else>启用</el-button>
              <el-button  type="primary" class="btn" @click='deleteRow(scope.row.hash, $event)' >删除</el-button>
              <el-button  type="primary" class="btn" @click="showDeployDetail(scope.row.hash, scope.row.owner)" style="width:90px;">详细信息</el-button>
              <el-button  type="primary" class="btn" @click="getUserListByHash(scope.row.hash)" style="width:120px">启用机构列表</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!--显示启用机构 -->
    <el-dialog
      title="启用的机构列表"
      class="dialog-view"
      width="48%"
      :visible.sync="dialog.dialogTableVisible">
      <el-table :data="dialog.userListPage.userList.slice((dialog.userListPage.pageIndex-1)*dialog.userListPage.pageSize, dialog.userListPage.pageIndex*dialog.userListPage.pageSize)" border="true" cellpadding="0" cellspacing="0">
        <el-table-column property="name" label="权威机构名称" width="250">
          <template slot-scope='scope'>
            {{scope.row.name}}
            <img v-if="scope.row.recognized === true" class="icon_question" src="../../../assets/image/recognize.svg" widht='50' height='50' alt="">
            <img v-if="scope.row.recognized === false" class="icon_question" src="../../../assets/image/deRecognize.svg" widht='50' height='50' alt="">
          </template>
        </el-table-column>
        <el-table-column property="weId" label="WeID"></el-table-column>
      </el-table>
      <el-pagination @current-change="indexChange2" :current-page="dialog.userListPage.pageIndex" :page-size="dialog.userListPage.pageSize" layout="total, prev, pager, next, jumper" :total="dialog.userListPage.total"></el-pagination>
      <br/>
    </el-dialog>

    <!--主群组部署存证智能合约 -->
    <el-dialog
      title="部署存证智能合约"
      class="dialog-view" width="33%"
      :visible.sync="dialog.dialogFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.deployForm">
          <el-form-item label="选择将存证合约部署到哪一个FISCO-BCOS群组ID（group ID）" :label-width="formLabelWidth">
            <div class="mark-text">
              <div>说明:</div>
              <div>1. 找不到想找要的FISCO-BCOS群组ID？请确认下是否已经完成群组的部署。</div>
              <div>2. 只能在“非主群组”上部署存证智能合约，主群组上的存证智能合约不能随意替换。</div>
            </div>
            <el-select v-model="dialog.deployForm.groupId" placeholder="群组" style="width: 100%">
              <el-option v-for="val in dialog.groupList"
              :key="val.value"
              :label="val.value"
              :value="val.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="存证应用名字" :label-width="formLabelWidth">
            <div class="mark-text">
              <span>给存证区块链应用起一个名字吧，用于不同的存证使用场景，例如“学历证书应用存证”。</span>
            </div>
            <el-input v-model="dialog.deployForm.evidenceName" placeholder="Enter evidenceName"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="deployEvidence($event)">部 署</el-button>
      </div>
    </el-dialog>

    <!--显示部署详情 -->
    <el-dialog
      title="部署信息"
      class="dialog-view" width="620px"
      :visible.sync="dialog.dialogDetailVisible"
      :close-on-click-modal="true">
      <div class="dialog-body deployDetail">
        <el-row>
          <el-col :span="4" class="title">智能合约部署ID</el-col>
          <el-col :span="20" class="value">{{dialog.deployDetail.hash}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="4" class="title">部署账户</el-col>
          <el-col :span="20" class="value">{{dialog.deployDetail.owner}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="4" class="title">群组</el-col>
          <el-col :span="20" class="value">{{dialog.deployDetail.groupId}}</el-col>
        </el-row>
        <el-row v-if="dialog.deployDetail.local">
          <el-col :span="4" class="title">WeID SDK版本</el-col>
          <el-col :span="7" class="value">{{dialog.deployDetail.weIdSdkVersion}}</el-col>
          <el-col :span="4" class="title">WeID合约版本</el-col>
          <el-col :span="9" class="value">{{dialog.deployDetail.contractVersion}}</el-col>
        </el-row>
        <el-row v-if="dialog.deployDetail.local">
          <el-col :span="4" class="title">部署来源</el-col>
          <el-col :span="7" class="value">{{dialog.deployDetail.from}}</el-col>
          <el-col :span="4" class="title">区块链节点版本</el-col>
          <el-col :span="9" class="value">{{dialog.deployDetail.nodeVerion}}</el-col>
        </el-row>
        <el-row v-if="dialog.deployDetail.local">
          <el-col :span="5" class="title">区块链节点</el-col>
          <el-col :span="19" class="value">{{dialog.deployDetail.nodeAddress}}</el-col>
        </el-row>
        <el-row class="none-border">
          <el-col :span="5" class="title">存证智能合约地址</el-col>
          <el-col :span="19" class="value">{{dialog.deployDetail.evidenceAddress}}</el-col>
        </el-row>
      </div>
      <br/>
    </el-dialog>

    <!--显示部署详情 -->
    <el-dialog
      title="存证合约部署"
      class="dialog-view" width="400px"
      :visible.sync="dialog.dialogDepolyDetailVisible"
      :close-on-click-modal="false">
      <div class="dialog-body deployMessage">
        <p v-for="val in dialog.deployMessages" :key="val.value">{{val}}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="comfirmBtn">确 定</el-button>
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
        deployList: [],
        pageSize: 5,
        total: 0,
        pageIndex: 1
      },
      dialog: {
        dialogTableVisible: false,
        dialogFormVisible: false,
        dialogDetailVisible: false,
        dialogDepolyDetailVisible: false,
        deployForm: {
          groupId: 1,
          evidenceName: ''
        },
        groupList: [],
        deployMessages: [],
        hash: '',
        groupId: '',
        deployBtn: null,
        deployDetail: {

        },
        userListPage: {
          userList: [],
          pageSize: 3,
          total: 0,
          pageIndex: 1
        }
      }
    }
  },
  methods: {
    showAll (value) {
      this.$alert(value, '温馨提示', {}).catch(() => {})
    },
    showDeploy () {
      if (check.data.nodeStatus) {
        this.dialog.deployForm.evidenceName = ''
        this.dialog.dialogFormVisible = true
      } else {
        check.checkNodeState(false)
      }
    },
    showDeployDetail (hash, owner) {
      API.doGet('getShareInfo/' + hash).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.deployDetail = res.data.result
          this.dialog.deployDetail.owner = owner
          this.dialog.dialogDetailVisible = true
        } else {
          this.$alert('部署详情查询失败!', '温馨提示').catch(() => {})
        }
      })
    },
    enable (hash, groupId, e) {
      this.dialog.deployBtn = e.currentTarget
      var message = '是否确定在此群组（group ID : ' + groupId + '）中使用这个 Evidence 合约地址? ' +
        '<br/>* 启用后将直接同步到应用环境（如果是生产环境，请特别小心）。' +
        '<br/>* 每一个群组，只能有一个启用的 Evidence 合约（调用EvidenceService的createEvidence会将Evidence写入这个 Evidence 合约）。' +
        '<br/>* 不同群组，可以启用不同的 Evidence 合约。'
      this.$confirm(message, '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary', dangerouslyUseHTMLString: true})
        .then(_ => {
          this.disableBtn(this.dialog.deployBtn)
          this.dialog.hash = hash
          this.dialog.groupId = groupId
          this.dialog.deployMessages = []
          this.enableShareCns()
        }).catch(() => {})
    },
    deleteRow (hash, e) {
      this.dialog.deployBtn = e.currentTarget
      this.$confirm('确认删除吗？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          this.disableBtn(this.dialog.deployBtn)
          this.removeHash(hash)
        }).catch(() => {})
    },
    removeHash (hash) {
      API.doPost('removeHash/' + hash + '/2').then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('删除成功!', '温馨提示').catch(() => {})
          this.getShareList()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
        this.enableBtn(this.dialog.deployBtn)
      })
    },
    comfirmBtn () {
      this.dialog.dialogDepolyDetailVisible = false
      this.dialog.dialogFormVisible = false
    },
    enableShareCns () {
      var formData = {}
      formData.hash = this.dialog.hash
      formData.groupId = this.dialog.groupId
      this.dialog.deployMessages.push('存证合约启用中...')
      this.dialog.dialogDepolyDetailVisible = true
      API.doPost('enableShareCns', formData, 15).then(res => {
        this.dialog.dialogDepolyDetailVisible = true
        if (res.data.errorCode === 0) {
          this.dialog.deployMessages.push('存证合约启用成功。')
          this.dialog.deployMessages.push('请继续操作。')
          this.getShareList()
        } else {
          if (res.data.result === 'fail') {
            this.dialog.deployMessages.push('存证合约启用失败。')
          } else {
            this.dialog.deployMessages.push('存证合约启用失败:' + res.data.errorMessage)
          }
        }
        this.enableBtn(this.dialog.deployBtn)
      })
    },
    isEnableEvidenceCns () {
      API.doGet('isEnableEvidenceCns/' + this.dialog.groupId).then(res => {
        if (res.data.result) { // 说明为首次部署，则调用启用逻辑
          this.enableShareCns()
        } else {
          this.dialog.deployMessages.push('请继续操作。')
          this.getShareList()
          this.enableBtn(this.dialog.deployBtn)
        }
      })
    },
    deployEvidence (e) {
      this.dialog.deployBtn = e.currentTarget
      if (this.dialog.deployForm.evidenceName === '') {
        this.$alert('请输入存证应用名!', '温馨提示').catch(() => {})
        return
      }
      this.disableBtn(this.dialog.deployBtn)
      this.dialog.deployMessages = []
      this.dialog.deployMessages.push('存证合约部署中...')
      this.dialog.dialogDepolyDetailVisible = true
      API.doPost('deployEvidence', this.dialog.deployForm, 15).then(res => {
        this.dialog.dialogDepolyDetailVisible = true
        if (res.data.errorCode === 0) {
          this.dialog.hash = res.data.result
          this.dialog.groupId = this.dialog.deployForm.groupId
          this.dialog.deployMessages.push('存证合约部署成功。')
          this.isEnableEvidenceCns()
        } else {
          this.dialog.deployMessages.push('存证合约部署失败! 请查看日志。')
          this.enableBtn(this.dialog.deployBtn)
        }
      })
    },
    indexChange (currentPage) {
      if (currentPage < 1) {
        currentPage = 1
      }
      this.page.pageIndex = currentPage
    },
    getShareList () {
      API.doGet('getShareList').then(res => {
        if (res.data.errorCode === 0) {
          this.page.deployList = res.data.result
          this.page.total = this.page.deployList.length
          if (this.page.total !== 0 && this.page.total === (this.page.pageSize * (this.page.pageIndex - 1))) {
            this.indexChange(this.page.pageIndex - 1)
          }
        }
      })
    },
    indexChange2 (currentPage) {
      this.dialog.userListPage.pageIndex = currentPage
    },
    getUserListByHash (hash) {
      API.doGet('getUserListByHash/' + hash).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.userListPage.userList = res.data.result
          this.dialog.userListPage.total = res.data.result.length
          this.dialog.dialogTableVisible = true
        }
      })
    },
    init () {
      // 初始化群组
      API.doGet('getAllGroup/true').then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.groupList = res.data.result
          this.dialog.deployForm.groupId = this.dialog.groupList[0].value
        } else {
          this.$alert('启用列表失败!', '温馨提示').catch(() => {})
        }
      })
      this.getShareList()
    },
    async check () {
      await check.checkNodeState(false)
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
