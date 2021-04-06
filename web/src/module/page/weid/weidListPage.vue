<template>
  <section class="content app_main">
    <el-row class="app_main_header_row">
      <el-col :span="2">
        <el-button type="primary" class="btn" @click="showCreateWeId" style="width:100px">创建WeID</el-button>
      </el-col>
      <el-col :span="21" class="head-icon">
        <a target="blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/terminologies.html?highlight=简称WeID">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question">什么是WeID?</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-table :data="page.weidList" border="true" align="center">
          <el-table-column prop="weId" label="WeID" align="center">
            <template slot-scope='scope'>
              <span class='long_words link' @click="showAll(scope.row.weId, scope.row.id)" :title='scope.row.weId'>{{scope.row.weId}}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" :formatter="dateFormat" width='200'></el-table-column>
          <el-table-column label="操作" align="center" width='400'>
            <template slot-scope='scope'>
              <el-button  type="primary" class="btn" @click="showRegistIssuer(scope.row.weId)" :disabled='scope.row.issuer' style="width:180px;">注册为权威凭证发行者</el-button>
              <el-button  type="primary" class="btn" @click="showRegistWhiteList(scope.row.weId)" style="width:120px;" v-if="roleType === '1'">添加到白名单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, slot, next" :total="page.total">
          <span style="text-align: center;">{{page.pageIndex}}</span>
        </el-pagination>
    </div>

    <!-- 创建WeID -->
    <el-dialog
      title="创建WeID"
      class="dialog-view" width="25%"
      :visible.sync="dialog.dialogCreateWeIdVisible"
      :close-on-click-modal="false"
      v-dialogDrag>
      <div class="dialog-body">
        <el-row>
          <el-col>
            <el-button type="primary" @click="createBySys()" class="btn width_100">系统自动创建公私钥来创建WeID</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary" @click="showCreateWeIdByPri()" class="btn width_100">传入私钥创建WeID</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary"  @click="showCreateWeIdByPub()" class="btn width_100">传入公钥，使用代理模式创建WeID</el-button>
          </el-col>
        </el-row>
        <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=WeID 管理#weid"
          target="blank_" style='position: absolute;right:60px;top:20px;font-size:12px;color:#017CFF;'>
          <img class="icon_question" src="../../../assets/image/icon-question.svg">
          <span class="icon_question" >使用教程</span>
        </a>
      </div>
    </el-dialog>

    <!-- 传入私钥创建WeID -->
    <el-dialog
      title="传入私钥创建WeID"
      class="dialog-view" width="30%"
      :visible.sync="dialog.dialogCreateWeIdByPrivateVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.createByPrivateForm">
          <div class='file_part' style="margin-top:15px;">
            <el-form-item>
              <div class="mark-text">
                <span>备注：支持椭圆曲线的公私钥，并且为十进制私钥数据。</span>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=创建密钥#weid" target="blank_">
                  <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">如何生成私钥？</span>
                </a>
              </div>
              <div class='input_item' style="margin-bottom:-8px;">
                <el-input v-model="dialog.createByPrivateForm.privateKeyFileName" placeholder="请选择私钥文件" maxlength="30" readOnly style="width: 75%"></el-input>
                <button type="button" @click="chooseFile('privateKeyFile')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
              </div>
            </el-form-item>
            <input type="file" id ='privateKeyFile' style="display:none;" >
          </div>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="createByPri()">创 建</el-button>
      </div>
    </el-dialog>

    <!-- 传入公钥创建WeID -->
    <el-dialog
      title="传入公钥创建WeID"
      class="dialog-view" width="30%"
      :visible.sync="dialog.dialogCreateWeIdByPublicVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.createByPublicForm">
          <div class='file_part' style="margin-top:15px;">
            <el-form-item>
              <div class="mark-text">
                <div>* 只有联盟链委员会的管理员，才能作为代理，为其他机构创建WeID。</div>
                <span>* 支持椭圆曲线的公私钥，并且为十进制私钥数据。</span>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=创建密钥#weid" target="blank_">
                  <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">如何生成公钥？</span>
                </a>
              </div>
              <div class='input_item' style="margin-bottom:-8px;">
                <el-input v-model="dialog.createByPublicForm.publicKeyFileName" placeholder="请选择公钥文件" maxlength="30" readOnly style="width: 75%"></el-input>
                <button type="button" @click="chooseFile('publicKeyFile')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
              </div>
            </el-form-item>
            <input type="file" id ='publicKeyFile' style="display:none;" >
          </div>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="createByPub()">创 建</el-button>
      </div>
    </el-dialog>

    <!--注册权威凭证发行者 -->
    <el-dialog
      title="注册权威凭证发行者"
      class="dialog-view" width="31%"
      :visible.sync="dialog.dialogissuerFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.registForm">
          <el-form-item label="机构的WeID" :label-width="formLabelWidth">
            <el-input v-model="dialog.registForm.weId" disabled placeholder="填入这个机构WeID"></el-input>
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

    <!--添加到白名单 -->
    <el-dialog
      title="添加到白名单"
      class="dialog-view" width="31%"
      :visible.sync="dialog.dialogwhiteFormVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.whiteListForm">
          <el-form-item label="WeID" :label-width="formLabelWidth">
            <el-input v-model="dialog.whiteListForm.weId" disabled placeholder="填入这个机构WeID"></el-input>
          </el-form-item>
          <el-form-item label="选择待加入的白名单" :label-width="formLabelWidth">
            <div class="mark-line" v-if="dialog.hashWihteList == false">
              <span href="#" @click="toAddWihte()">前往新增白名单</span>
            </div>
            <el-select v-model="dialog.whiteListForm.issuerType" placeholder="白名单" style="width: 100%">
              <el-option v-for="val in dialog.whiteList"
              :key="val.type"
              :label="val.type"
              :value="val.type">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="addWeIdToWhiteList()">添 加</el-button>
      </div>
    </el-dialog>

    <!--显示WeID -->
    <el-dialog
      title="温馨提示"
      class="dialog-view" width="500px"
      :visible.sync="dialog.showWeIdVisible"
      :close-on-click-modal="false">
      <div class="dialog-body deployMessage" style="min-height: 100px;">
        <el-form :model="dialog.showData">
          <el-form-item label="WeID:" :label-width="formLabelWidth">
            <el-input v-model="dialog.showData.weId"  class="select_part" @focus='selectCopy($event)' readOnly></el-input>
          </el-form-item>
          <el-form-item label="该WeID密钥存放路径:" :label-width="formLabelWidth" v-if="dialog.showData.hasPath">
            <el-input v-model="dialog.showData.path" class="select_part" type="textarea" resize="none" rows='4' @focus='selectCopy($event)' readOnly ></el-input>
          </el-form-item>
          <el-form-item label="该WeID密钥存放路径:" :label-width="formLabelWidth" v-else>
            <div class='select_part mark-text'>该WeID非当前系统产生</div>
          </el-form-item>
        </el-form>
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
        weidList: [],
        pageSize: 6,
        total: 0,
        pageIndex: 1
      },
      roleType: localStorage.getItem('roleType'),
      dialog: {
        dialogCreateWeIdVisible: false,
        dialogCreateWeIdByPrivateVisible: false,
        dialogCreateWeIdByPublicVisible: false,
        dialogissuerFormVisible: false,
        dialogwhiteFormVisible: false,
        showWeIdVisible: false,
        createByPrivateForm: {
          privateKeyFileName: '',
          privateKeyFile: ''
        },
        createByPublicForm: {
          publicKeyFileName: '',
          publicKeyFile: ''
        },
        registForm: {
          name: '',
          weId: '',
          description: ''
        },
        whiteListForm: {
          issuerType: '',
          weId: ''
        },
        hashWihteList: false,
        whiteList: [],
        showData: {
          weId: '',
          hasPath: false
        }
      }
    }
  },
  methods: {
    showAll (weId, id) {
      this.dialog.showData.weId = weId
      this.dialog.showData.hasPath = false
      API.doGet('getWeIdPath').then(res => {
        if (res.data.errorCode === 0) {
          if (id !== null) {
            var data = res.data.result + '/' + weId.split(':')[3]
            this.dialog.showData.path = data
            this.dialog.showData.hasPath = true
          }
          this.dialog.showWeIdVisible = true
        }
      })
    },
    selectCopy (e) {
      e.currentTarget.select()
      document.execCommand('Copy')
      this.$message({
        message: '已复制',
        type: 'success',
        duration: 1000
      })
    },
    showCreateWeId () {
      if (check.data.nodeStatus) {
        this.dialog.dialogCreateWeIdVisible = true
      } else {
        check.checkNodeState(true)
      }
    },
    showCreateWeIdByPri () {
      this.dialog.createByPrivateForm.privateKeyFileName = ''
      this.dialog.dialogCreateWeIdByPrivateVisible = true
    },
    showCreateWeIdByPub () {
      this.dialog.createByPublicForm.publicKeyFileName = ''
      this.dialog.dialogCreateWeIdByPublicVisible = true
    },
    showRegistIssuer (weId) {
      this.dialog.registForm.weId = weId
      this.dialog.registForm.name = ''
      this.dialog.registForm.description = ''
      this.dialog.dialogissuerFormVisible = true
    },
    registerIssuer () {
      if (this.dialog.registForm.name === '') {
        this.$alert('请输入权威机构名称!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.registForm.name.indexOf('@') === 0) {
        this.$alert('权威机构名称输入非法!', '温馨提示', {}).catch(() => {})
        return
      }
      API.doPost('registerIssuer', this.dialog.registForm).then(res => { // 保存选择的角色
        if (res.data.errorCode === 0) {
          this.$alert('权威凭证发行者注册成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogissuerFormVisible = false
          }).catch(() => {})
          this.page.weidList.forEach(weidInfo => {
            if (weidInfo.weId === this.dialog.registForm.weId) {
              weidInfo.issuer = true
            }
          })
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    showRegistWhiteList (weId) {
      this.dialog.whiteListForm.weId = weId
      this.dialog.dialogwhiteFormVisible = true
    },
    addWeIdToWhiteList () {
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
    changeRoleType () {
      this.roleType = localStorage.getItem('roleType')
    },
    toAddWihte () {
      this.$router.push({name: 'whiteList'})
      this.resetSetItem('menu2', 4)
    },
    chooseFile (type) {
      var fileInput = document.getElementById(type)
      fileInput.value = ''
      fileInput.click()
      fileInput.onchange = file => {
        if (file.target.files[0]) {
          if (type === 'privateKeyFile') {
            this.dialog.createByPrivateForm.privateKeyFileName = file.target.files[0].name
            this.dialog.createByPrivateForm.privateKeyFile = file.target.files[0]
          } else if (type === 'publicKeyFile') {
            this.dialog.createByPublicForm.publicKeyFileName = file.target.files[0].name
            this.dialog.createByPublicForm.publicKeyFile = file.target.files[0]
          }
        }
      }
    },
    createBySys () {
      API.doGet('createWeId').then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('WeID创建成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogCreateWeIdVisible = false
          }).catch(() => {})
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    createByPri () {
      if (this.dialog.createByPrivateForm.privateKeyFile === '') {
        this.$alert('请选择私钥文件!', '温馨提示', {}).catch(() => {})
        return
      }
      var formData = new FormData()
      formData.append('privateKey', this.dialog.createByPrivateForm.privateKeyFile)
      API.doPostAndUploadFile('createWeIdByPrivateKey', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('WeID创建成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogCreateWeIdByPrivateVisible = false
            this.dialog.dialogCreateWeIdVisible = false
          }).catch(() => {})
          this.init()
          this.dialog.createByPrivateForm.privateKeyFile = ''
          this.dialog.createByPrivateForm.privateKeyFileName = ''
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    createByPub () {
      if (this.dialog.createByPublicForm.publicKeyFile === '') {
        this.$alert('请选择公钥文件!', '温馨提示', {}).catch(() => {})
        return
      }
      var formData = new FormData()
      formData.append('publicKey', this.dialog.createByPublicForm.publicKeyFile)
      API.doPostAndUploadFile('createWeIdByPublicKey', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('WeID创建成功!', '温馨提示', {}).then(() => {
            this.dialog.dialogCreateWeIdByPublicVisible = false
            this.dialog.dialogCreateWeIdVisible = false
          }).catch(() => {})
          this.init()
          this.dialog.createByPublicForm.publicKeyFile = ''
          this.dialog.createByPublicForm.publicKeyFileName = ''
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
      })
    },
    indexChange (currentPage) {
      var pageIndex = this.page.pageIndex
      this.page.pageIndex = currentPage
      var blockNumber, indexInBlock, direction
      if (pageIndex > this.page.pageIndex) { // 向前
        blockNumber = this.page.weidList[0].weIdPojo.currentBlockNum
        indexInBlock = this.page.weidList[0].weIdPojo.index + 1
        direction = false
      } else { // 向后
        blockNumber = this.page.weidList[this.page.weidList.length - 1].weIdPojo.currentBlockNum
        indexInBlock = this.page.weidList[this.page.weidList.length - 1].weIdPojo.index - 1
        direction = true
      }
      this.queryWeIdList(this.page.pageIndex, blockNumber, indexInBlock, direction)
    },
    queryWeIdList (pageIndex, blockNumber, indexInBlock, direction) {
      var formData = {}
      formData.blockNumber = blockNumber
      formData.pageSize = this.page.pageSize
      formData.indexInBlock = indexInBlock
      formData.direction = direction
      formData.iDisplayStart = pageIndex * this.page.pageSize
      formData.iDisplayLength = this.page.pageSize

      API.doGet('getWeIdList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.weidList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
      })
    },
    init () {
      this.page.pageIndex = 1
      this.queryWeIdList(1, 0, 0, true)
    },
    initWihteList () {
      API.doGet('getIssuerTypeList').then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.whiteList = res.data.result
          if (this.dialog.whiteList.length > 0) {
            this.dialog.hashWihteList = true
            this.dialog.whiteListForm.issuerType = this.dialog.whiteList[0].type
          }
        }
      })
    },
    dateFormat (row) {
      var date = new Date(parseInt(row.weIdPojo.created * 1000) + 8 * 3600 * 1000)
      return date.toJSON().substr(0, 19).replace('T', ' ')
    },
    async check () {
      await check.checkNodeState(true)
      if (check.data.nodeStatus) {
        this.init()
        this.initWihteList()
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
