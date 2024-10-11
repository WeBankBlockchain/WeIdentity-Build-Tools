<template>
  <section class="content app_main">
    <el-row v-if="roleType === '1'" class="app_main_header_row">
      <el-col :span="5">
        <el-button type="primary" class="btn" style="width:250px" @click="showDeployForm">主群组部署 WeIdentity 智能合约</el-button>
      </el-col>
      <el-col :span="19" class="head-icon">
        <a href="https://weid-doc-xml.readthedocs.io/zh/test/docs/weidentity-quick-tools-web.html?highlight=部署 WeIdentity 智能合约#id2" target="blank_">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question" >多次部署合约</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-table
        :data="page.deployList.slice((page.pageIndex-1)*page.pageSize, page.pageIndex*page.pageSize)"
        border="true"  align="center"  style="width: 100%;"  cellpadding="0" cellspacing="0">
          <el-table-column prop="applyName" label="应用名字" align="center">
            <template slot-scope='scope'>
              <span class='long_words' :title='scope.row.applyName'>{{scope.row.applyName}}</span>
            </template>
          </el-table-column>
           <el-table-column label="智能合约部署ID" align="center" width='160'>
            <template slot-scope='scope'>
              <span class='long_words link' @click="showAll(scope.row.hash)" :title='scope.row.hash'>{{scope.row.hashShow}}</span>
            </template>
          </el-table-column>
          <!-- <el-table-column prop="deployOwner" label="部署账户" align="center" :formatter="dateFormat"></el-table-column> -->
          <el-table-column label="部署的机构名称" align="center" width='250'>
            <template slot-scope='scope' v-if='scope.row.issuer !== null'>
              <span class='icon_question' :title='scope.row.issuer.name'>{{scope.row.issuer.name}}</span>
              <img v-if="scope.row.issuer.recognized === true" class="icon_question" src="../../../assets/image/recognize.svg" widht='50' height='50' alt="">
              <img v-if="scope.row.issuer.recognized === false" class="icon_question" src="../../../assets/image/deRecognize.svg" widht='50' height='50' alt="">
            </template>
          </el-table-column>
          <el-table-column prop="groupId" label="所在群组" align="center" width='100'></el-table-column>
          <el-table-column prop="createTime" label="创建时间" align="center" width='170'></el-table-column>
          <el-table-column label="操作" align="center" width='300' >
            <template slot-scope='scope'>
              <el-button  type="primary" class="btn" v-if='scope.row.enable' disabled>已启用</el-button>
              <el-button  type="primary" class="btn" @click="enable(scope.row.hash, scope.row.needDeployCpt, $event)" v-else>启用</el-button>
              <el-button  type="primary" @click="deleteRow(scope.row.hash, $event)" class="btn" >删除</el-button>
              <el-button  type="primary" class="btn" @click="showDeployDetail(scope.row.hash, scope.row.weId)" v-if='scope.row.enable == false || scope.row.needDeployCpt == false' style="width:90px;">详细信息</el-button>
              <el-button  type="primary" class="btn" @click="deploySystemCptBtn(scope.row.hash)" v-if='scope.row.enable && scope.row.needDeployCpt' style="width:120px;">部署系统CPT</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="roleType === '1'" @current-change="handleCurrentChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!--主群组部署 WeIdentity 智能合约 -->
    <el-dialog
      title="主群组部署 WeIdentity 智能合约"
      class="dialog-view" width="29%"
      :visible.sync="dialog.dialogFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.deployForm">
          <el-form-item label="链ID(chain_id)" :label-width="formLabelWidth">
            <div class="mark-icon">
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-spec.html?highlight=chain-id#id4" target="blank_" >
                <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                <span class="icon_question">什么是chain id?</span>
              </a>
            </div>
            <el-input v-model="dialog.deployForm.chainId" placeholder="Enter chainId" onKeyUp="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')" @blur="dialog.deployForm.chainId = $event.target.value"></el-input>
          </el-form-item>
          <el-form-item label="应用名字" :label-width="formLabelWidth">
            <div class="mark-text">
              <span class="">给自己的WeIdentity区块链应用起一个名字吧，用于在同一条链上区别不同的 WeIdentity 应用，例如“学历证书应用”，“区块链证件应用”。</span>
            </div>
            <el-input v-model="dialog.deployForm.applyName" placeholder="Enter applyName"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="deploy($event)">部 署</el-button>
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
          <el-col :span="4" class="title">区块链节点</el-col>
          <el-col :span="20" class="value">{{dialog.deployDetail.nodeAddress}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8" class="title">WeID智能合约地址</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.weIdAddress}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8" class="title">CPT智能合约地址</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.cptAddress}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8" class="title">Authority Issuer 智能合约地址</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.authorityAddress}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8" class="title">Evidence 智能合约地址</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.evidenceAddress}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8" class="title">Specific Issuer 智能合约地址</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.specificAddress}}</el-col>
        </el-row>
        <el-row class="none-border">
          <el-col :span="8" class="title">Chain Id</el-col>
          <el-col :span="16" class="value">{{dialog.deployDetail.chainId}}</el-col>
        </el-row>
      </div>
      <br/>
    </el-dialog>

    <!--显示部署详情 -->
    <el-dialog
      title="合约部署"
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
      roleType: localStorage.getItem('roleType'),
      dialog: {
        dialogFormVisible: false,
        dialogDetailVisible: false,
        dialogDepolyDetailVisible: false,
        deployMessages: [],
        deployForm: {
          chainId: '',
          applyName: ''
        },
        hash: '',
        deployBtn: null,
        deployDetail: {}
      }
    }
  },
  methods: {
    showAll (value) {
      this.$alert(value, '温馨提示', {closeOnClickModal: 'true'}).catch(() => {})
    },
    showDeployForm () {
      if (check.data.nodeStatus) {
        this.dialog.deployForm.chainId = ''
        this.dialog.deployForm.applyName = ''
        this.dialog.dialogFormVisible = true
      } else {
        check.checkNodeState(false)
      }
    },
    showDeployDetail (hash, weId) {
      API.doGet('getDeployInfo/' + hash).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.dialogDetailVisible = true
          this.dialog.deployDetail = res.data.result
          this.dialog.deployDetail.owner = weId
        } else {
          this.$alert('部署详细查询失败！', '温馨提示', {}).catch(() => {})
        }
      })
    },
    changeRoleType () {
      this.roleType = localStorage.getItem('roleType')
    },
    deleteRow (hash, e) {
      var target = e.currentTarget
      let thiObj = this
      this.$confirm('确认删除吗？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          thiObj.disableBtn(target)
          this.removeHash(hash, target)
        }).catch(() => {})
    },
    removeHash (hash, target) {
      API.doPost('removeHash/' + hash + '/1').then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('删除成功!', '温馨提示').catch(() => {})
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
        this.enableBtn(target)
      })
    },
    checkInput () {
      if (this.dialog.deployForm.chainId === '') {
        this.$alert('请输入ChainId!', '温馨提示', {}).catch(() => {})
        return false
      }
      if (this.dialog.deployForm.applyName === '') {
        this.$alert('请输入应用名!', '温馨提示', {}).catch(() => {})
        return false
      }
      return true
    },
    comfirmBtn () {
      this.dialog.dialogDepolyDetailVisible = false
      this.dialog.dialogFormVisible = false
    },
    deploy (e) {
      this.dialog.deployBtn = e.currentTarget
      if (!this.checkInput()) {
        return
      }
      this.disableBtn(this.dialog.deployBtn)
      this.dialog.dialogDepolyDetailVisible = true
      this.dialog.deployMessages = []
      this.dialog.deployMessages.push('合约部署中...')
      API.doPost('deploy', this.dialog.deployForm, 120).then(res => {
        this.dialog.dialogDepolyDetailVisible = true
        if (res.data.errorCode === 0) {
          this.dialog.hash = res.data.result
          this.isEnableMasterCns()
        } else {
          this.dialog.deployMessages.push('合约部署失败! 请查看日志。')
          this.enableBtn(this.dialog.deployBtn)
        }
      })
    },
    isEnableMasterCns () { // 检查是否已启用合约
      API.doGet('isEnableMasterCns').then(res => {
        this.dialog.dialogDepolyDetailVisible = true
        if (res.data.result) { // 说明为首次部署，则调用启用逻辑
          this.enableHash(true)
        } else { // 非首次部署，部署流程完成
          this.dialog.deployMessages.push('合约部署成功。')
          this.dialog.deployMessages.push('请继续操作。')
          this.enableBtn(this.dialog.deployBtn)
          this.init()
        }
      })
    },
    enableHash (deployCpt) { // 启用Hash
      this.dialog.deployMessages.push('合约启用中...')
      API.doGet('enableHash/' + this.dialog.hash, null, 10).then(res => {
        this.dialog.dialogDepolyDetailVisible = true
        if (res.data.errorCode === 0) { // 启用成功，执行系统cpt部署
          this.dialog.deployMessages.push('合约启用成功。')
          if (deployCpt) {
            this.deploySystemCpt()
          } else {
            this.dialog.deployMessages.push('请继续操作。')
            this.enableBtn(this.dialog.deployBtn)
            this.init()
          }
        } else {
          this.dialog.deployMessages.push('合约启用失败！请查看日志。')
          this.enableBtn(this.dialog.deployBtn)
        }
      })
    },
    deploySystemCptBtn (hash) {
      this.dialog.hash = hash
      this.deploySystemCpt()
    },
    deploySystemCpt () { // 部署系统cpt
      this.dialog.deployMessages.push('系统CPT部署中...')
      this.dialog.dialogDepolyDetailVisible = true
      API.doGet('deploySystemCpt/' + this.dialog.hash, null, 10).then(res => {
        if (res.data.errorCode === 0) { // 启用成功，执行系统cpt部署
          this.dialog.deployMessages.push('系统CPT部署成功。')
          this.dialog.deployMessages.push('系统CPT部署成功! 请继续操作。')
          this.init()
        } else {
          this.dialog.deployMessages.push('系统CPT部署失败！请查看日志。')
        }
        this.dialog.dialogDepolyDetailVisible = true
        this.enableBtn(this.dialog.deployBtn)
      })
    },
    enable (hash, deployCpt, e) {
      this.dialog.deployBtn = e.currentTarget
      this.$confirm('是否确定启用该主合约？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          this.dialog.hash = hash
          this.dialog.deployMessages = []
          this.dialog.dialogDepolyDetailVisible = true
          this.disableBtn(this.dialog.deployBtn)
          this.enableHash(deployCpt)
        }).catch(() => {})
    },
    handleCurrentChange (currentPage) {
      if (currentPage < 1) {
        currentPage = 1
      }
      this.page.pageIndex = currentPage
    },
    init () {
      API.doGet('getDeployList').then(res => {
        if (res.data.errorCode === 0) {
          this.page.deployList = res.data.result
          this.page.total = this.page.deployList.length
          if (this.page.total !== 0 && this.page.total === (this.page.pageSize * (this.page.pageIndex - 1))) {
            this.handleCurrentChange(this.page.pageIndex - 1)
          }
        }
      })
    },
    async check () {
      await check.checkNodeState(false)
      if (check.data.nodeStatus) {
        this.init()
      }
    }
  },
  mounted () {
    window.addEventListener('setItem', () => {
      this.changeRoleType()
    })
    this.check()
  }
}
</script>
