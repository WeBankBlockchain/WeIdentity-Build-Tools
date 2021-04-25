<template>
  <section class="content app_main">
    <el-row class="app_main_header_row">
      <el-col :span="4">
        <el-button type="primary" class="btn" @click="showRegistCpt" style="width:190px">注册新的凭证模板(CPT)</el-button>
      </el-col>
      <el-col :span="20" class="head-icon">
        <a target="blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-quick-tools-web.html?highlight=CPT#cpt">
          <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
          <span class="icon_question">凭证模板(CPT)使用教程</span>
        </a>
      </el-col>
    </el-row>
    <div class='app_view'>
      <el-form ref="queryForm" :model="queryForm" class="item">
        <el-form-item prop="cptType" label="凭证模板(CPT)类型:">
          <el-select v-model="queryForm.cptType" @change="changeCptType()" style="width: 120px">
            <el-option label="全部" value=""></el-option>
            <el-option label="系统CPT" value="sys"></el-option>
            <el-option label="用户CPT" value="user"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="page.cptList" border="true" align="center">
          <el-table-column prop="cptId" label="凭证模板ID(CPT ID)" align="center" width='170'></el-table-column>
          <el-table-column label="凭证模板名称(CPT Name)" align="center">
            <template slot-scope='scope'>
              <span class='long_words' :title='scope.row.cptTitle'>{{scope.row.cptTitle}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="publish" label="发布者WeID" align="center" width='270'>
            <template slot-scope='scope'>
              <span class='long_words link' @click="showAll(scope.row.weId)" :title='scope.row.weId'>{{scope.row.weIdShow}}</span>
            </template>
          </el-table-column>
          <!--<el-table-column prop="description" label="描述" align="center"></el-table-column>-->
          <el-table-column label="创建时间" align="center" :formatter="dateFormat" width='170'></el-table-column>
          <el-table-column label="操作" align="center" width="340">
            <template slot-scope='scope'>
              <el-button  type="primary" @click="showCpt(scope.row.cptId)" class="btn" style="width:90px">预览模板</el-button>
              <el-button  type="primary" @click="downCpt(scope.row.cptId)" class="btn" style="width:90px">下载模板</el-button>
              <el-button  type="primary" @click="toRegistPolicy(scope.row.cptId)" class="btn" style="width:90px" v-if="scope.row.cptType === 'user'" >注册Policy</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination @current-change="indexChange" :current-page="page.pageIndex" :page-size="page.pageSize" layout="total, prev, pager, next, jumper" :total="page.total"></el-pagination>
    </div>

    <!-- 创建凭证模板(CPT) -->
    <el-dialog
      title="创建凭证模板(CPT)"
      class="dialog-view" width="25%" style="min-width: 900px;"
      :visible.sync="dialog.dialogCreateCptVisible"
      :close-on-click-modal="false"
      v-dialogDrag>
      <div class="dialog-body">
        <el-row>
          <el-col>
            <el-button type="primary" @click="showCreateCpt1()" class="btn width_100" >创建只有一级数据结构的凭证模板</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary" @click="showCreateCpt2()" class="btn width_100">创建多级数据结构的凭证模板</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary" @click="showCreateCpt3()" class="btn width_100">上传CPT模板文件创建凭证模板</el-button>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 创建只有一级数据结构的凭证模板 -->
    <el-dialog
      title="注册新的凭证模板(CPT)"
      class="dialog-view" width="50%"
      :visible.sync="dialog.dialogCreateCpt1Visible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form ref="dialog.oneLeveFrom" :model="dialog.oneLeveFrom" class="item" inline-message="true" label-position="left" label-width="110px">
            <el-form-item label="CPT标题:" prop="cptTitle" :rules="{required: true, message: 'CPT标题不能为空', trigger: 'blur'}" style="margin-bottom: 12px">
              <el-input v-model="dialog.oneLeveFrom.cptTitle" placeholder="Enter cpt title" maxlength="30" style="width:70%"></el-input>
            </el-form-item>
            <el-form-item label="CPT描述:" prop="cptDesc" :rules="{required: true, message: 'CPT描述不能为空', trigger: 'blur'}" style="margin-bottom: 12px">
              <el-input v-model="dialog.oneLeveFrom.cptDesc" placeholder="Enter cpt description" maxlength="30" style="width:70%"></el-input>
            </el-form-item>
            <el-form-item label="CPT ID (选填):" prop="registerCptId" style="margin-bottom: 12px">
              <el-input v-model="dialog.oneLeveFrom.cptId" maxlength="30" style="width:70%" onkeyup="this.value=this.value.replace(/\D/g,'')" @blur="dialog.oneLeveFrom.cptId = $event.target.value" placeholder="Enter cptId"></el-input>
              <div class="mark-bottom">
                <span>只允许填入数字；如果不填，系统将自动按累加规则帮您生成CPT ID</span>
              </div>
            </el-form-item>
            <div class="mark-icon">
              <strong style="font-size:14px">数据结构定义（claim结构定义）</strong>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-spec.html?highlight=claim#claim-protocol-type-cpt" target="blank_" >
               <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
               <span class="icon_question" >什么是claim</span>
              </a>
            </div>
            <div>
              <el-table :data="dialog.tableData" border style="width: 100%;margin-top: 10px;" max-height="300">
                <el-table-column label="序号" width="70" align="center">
                  <template slot-scope="scope">{{scope.$index + 1}}</template>
                </el-table-column>
                <el-table-column prop="name" label="字段名" width="180" align="center">
                  <template slot-scope="scope"><el-input v-model="scope.row.name" /></template>
                </el-table-column>
                <el-table-column prop="type" label="字段类型" width="130" align="center">
                  <template slot-scope="scope">
                    <el-select v-model="scope.row.type" >
                      <el-option label="string" value="string" selected></el-option>
                      <el-option label="integer" value="integer"></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="字段描述"  align="center">
                  <template slot-scope="scope"><el-input  v-model="scope.row.description" /></template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="100">
                  <template slot-scope="scope"><el-button type="primary" @click="deleteRow(scope.$index, dialog.tableData)">删除</el-button></template>
                </el-table-column>
              </el-table>
              <div class="mark-icon" style="margin-top: 5px;">
                <span class="icon_question">claim中的属性需要以字母开头</span>
              </div>
              <div style="margin-top: 10px">
                <el-button type="primary" @click="addDomain()" >新增字段</el-button>
              </div>
            </div>
          </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="registerCpt1($event)">注 册</el-button>
      </div>
    </el-dialog>

    <!-- 创建多级数据结构的凭证模板 -->
    <el-dialog
      title="注册新的凭证模板(CPT)"
      class="dialog-view" width="40%"
      :visible.sync="dialog.dialogCreateCpt2Visible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form ref="dialog.multilevelFrom" :model="dialog.multilevelFrom" class="item" label-position="left" inline-message="true" label-width="110px">
            <el-form-item label="CPT标题:" prop="cptTitle" :rules="{required: true, message: 'CPT标题不能为空', trigger: 'blur'}" style="margin-bottom: 12px" >
              <el-input v-model="dialog.multilevelFrom.cptTitle" placeholder="Enter cpt title" maxlength="30" style="width:70%"></el-input>
            </el-form-item>
            <el-form-item label="CPT描述:" prop="cptDesc" :rules="{required: true, message: 'CPT描述不能为空', trigger: 'blur'}" style="margin-bottom: 12px">
              <el-input v-model="dialog.multilevelFrom.cptDesc" placeholder="Enter cpt description" maxlength="30" style="width:70%"></el-input>
            </el-form-item>
            <el-form-item label="CPT ID (选填):" prop="cptId" style="margin-bottom: 12px">
              <el-input v-model="dialog.multilevelFrom.cptId" maxlength="30" style="width:70%" onkeyup="this.value=this.value.replace(/\D/g,'')" @blur="dialog.multilevelFrom.cptId = $event.target.value" placeholder="Enter cptId"></el-input>
              <div class="mark-bottom">
                <span>只允许填入数字；如果不填，系统将自动按累加规则帮您生成CPT ID</span>
              </div>
            </el-form-item>
            <div class="mark-icon">
              <strong style="font-size:14px">数据结构定义（claim结构定义）</strong>
              <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-spec.html?highlight=claim#claim-protocol-type-cpt" target="blank_" >
               <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
               <span class="icon_question" >什么是claim</span>
              </a>
            </div>
            <div class="mark-bottom" v-if="dialog.create2">
              <el-button type="primary" @click="selectJson(1)" title='模板1为 Claim 只有一级数据结构的模板' >模板1</el-button>
              <el-button type="primary" @click="selectJson(2)" title='模板2为 Claim 是两级数据结构的模板' >模板2</el-button>
              <div style="padding-top: 5px; padding-bottom: 5px;">说明: 点击模板按钮可以自动填充相应内容到下列代码输入框中</div>
            </div>
            <div>
              <v-jsoneditor ref="jsoneditorCpt" v-model="dialog.multilevelFrom.cptClaim" :options="{mode: 'code'}" :plus="false" height="400px" @error="onError"></v-jsoneditor>
            </div>
            <div class="mark-icon" style="margin-top: 5px;">
              <span class="icon_question">claim中的属性需要以字母开头</span>
            </div>
          </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="registerCpt2($event)">注 册</el-button>
      </div>
    </el-dialog>

    <!-- 根据CPT文件注册CPT -->
    <el-dialog
      title="注册新的凭证模板(CPT)"
      class="dialog-view" width="40%" style="min-width: 40%;"
      :visible.sync="dialog.dialogCreateCpt3Visible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-form :model="dialog.fileForm">
          <div class='file_part' style="margin-top:15px;">
            <el-form-item label="CPT JSON文件:" >
              <div class='input_item' style="margin-bottom:-8px;">
                <el-input v-model="dialog.fileForm.cptFileName" placeholder="请选择CPT文件" maxlength="30" readOnly style="width: 60%"></el-input>
                <button type="button" @click="chooseFile('cptFile')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
              </div>
            </el-form-item>
            <input type="file" id ='cptFile' style="display:none;" accept=".json" >
          </div>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="toCreateCpt2()">下 一 步</el-button>
      </div>
    </el-dialog>

    <!-- 预览cpt模板 -->
    <el-dialog
      title="预览cpt模板"
      class="dialog-view" width="40%"
      :visible.sync="dialog.dialogShowCptVisible">
      <div class="dialog-body">
        <el-row>
          <el-col>
            <el-input type="textarea" readOnly :rows="20" resize=none v-model="dialog.cptSchema"></el-input>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 注册policy -->
    <el-dialog
      title="注册Policy"
      class="dialog-view" width="30%"
      :visible.sync="dialog.dialogRegistPolicyVisible"
      :close-on-click-modal="false">
      <div class="dialog-body">
        <el-row>
          <el-col>
            <strong>Policy结构如下:</strong>
            <p><span style="font-size:12px;display:inline-block">0: 表示不披露，1: 表示披露</span></p>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <v-jsoneditor ref="jsoneditorPolicy" v-model="dialog.policyInfo.policy" :options="{mode: 'code'}" :plus="true" height="400px" @error="onError"></v-jsoneditor>
          </el-col>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="registerClaimPolicy($event)">注 册</el-button>
      </div>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
import config from '../../../../config/config'
import check from '../../../assets/utils/checkConfig'
import VJsoneditor from 'v-jsoneditor'
export default {
  components: { VJsoneditor },
  data () {
    return {
      page: {
        cptList: [],
        total: 0,
        pageIndex: 1,
        pageSize: 5
      },
      queryForm: {
        cptType: ''
      },
      dialog: {
        dialogCreateCpt1Visible: false,
        dialogCreateCpt2Visible: false,
        dialogCreateCpt3Visible: false,
        dialogCreateCptVisible: false,
        dialogShowCptVisible: false,
        dialogRegistPolicyVisible: false,
        policyInfo: {
          cptId: '',
          policy: ''
        },
        cptSchema: '',
        oneLeveFrom: {
          cptTitle: '',
          cptDesc: '',
          cptId: ''
        },
        multilevelFrom: {
          cptTitle: '',
          cptDesc: '',
          cptId: '',
          cptClaim: ''
        },
        tableData: [],
        fileForm: {
          cptFileName: '',
          cptFile: ''
        },
        create2: true
      }
    }
  },
  methods: {
    showRegistCpt () {
      if (check.data.nodeStatus) {
        this.dialog.dialogCreateCptVisible = true
      } else {
        check.checkNodeState(true)
      }
    },
    showAll (value) { // 显示weid数据
      this.$alert(value, '温馨提示', {}).catch(() => {})
    },
    showCpt (cptId) { // 显示CPT
      API.doGet('queryCptSchema/' + cptId).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.cptSchema = res.data.result // JSON.stringify(res.data.result, '\r\n', 4)
          this.dialog.dialogShowCptVisible = true
        } else {
          this.$alert('CPT查询异常!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    changeCptType () {
      this.page.pageIndex = 1
      this.init()
    },
    downCpt (cptId) {
      this.$confirm('确定下载该CPT吗？', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary'})
        .then(_ => {
          window.location.href = config.URL + '/downCpt/' + cptId
        }).catch(() => {})
    },
    toRegistPolicy (cptId) {
      var formData = {}
      formData.cptId = cptId
      API.doGet('cptToPolicy', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.policyInfo.policy = JSON.parse(res.data.result)
          this.dialog.policyInfo.cptId = cptId
          this.dialog.dialogRegistPolicyVisible = true
        } else {
          this.$alert('获取policy失败!', '温馨提示').catch(() => {})
        }
      })
    },
    onError (value) {
      // alert('json错误')
    },
    addDomain () {
      this.dialog.tableData.push({
        name: '',
        type: 'string',
        description: ''
      })
    },
    deleteRow (index, rows) {
      rows.splice(index, 1)
    },
    showCreateCpt1 () {
      this.dialog.dialogCreateCpt1Visible = true
      this.dialog.oneLeveFrom.cptTitle = ''
      this.dialog.oneLeveFrom.cptDesc = ''
      this.dialog.oneLeveFrom.cptClaim = ''
      this.dialog.oneLeveFrom.cptId = ''
      this.dialog.tableData = []
      this.addDomain()
    },
    showCreateCpt2 () {
      this.dialog.create2 = true
      this.dialog.dialogCreateCpt2Visible = true
      let $this = this
      setTimeout(function () { $this.setDefault() }, 50)
      this.dialog.multilevelFrom.cptTitle = ''
      this.dialog.multilevelFrom.cptDesc = ''
      this.dialog.multilevelFrom.cptClaim = ''
      this.dialog.multilevelFrom.cptId = ''
    },
    showCreateCpt3 () {
      this.dialog.create2 = false
      this.dialog.dialogCreateCpt3Visible = true
      this.dialog.multilevelFrom.cptTitle = ''
      this.dialog.multilevelFrom.cptDesc = ''
      this.dialog.multilevelFrom.cptClaim = ''
      this.dialog.multilevelFrom.cptId = ''
      this.dialog.fileForm.cptFileName = ''
    },
    selectJson (v) {
      let jsonData
      if (v === 1) {
        jsonData = json1
      } else if (v === 2) {
        jsonData = json2
      }
      if (this.dialog.multilevelFrom.cptClaim === '') {
        this.dialog.multilevelFrom.cptClaim = jsonData
      } else if (this.dialog.multilevelFrom.cptClaim !== jsonData) {
        this.$confirm('确认要覆盖下列代码片段?', '温馨提示', {closeOnClickModal: false, cancelButtonClass: 'el-button--primary', dangerouslyUseHTMLString: true})
          .then(_ => {
            this.dialog.multilevelFrom.cptClaim = jsonData
          }).catch(() => {})
      }
    },
    setDefault () {
      this.$refs.jsoneditorCpt.editor.setText('')
    },
    chooseFile () {
      var caCrtFileInput = document.getElementById('cptFile')
      caCrtFileInput.value = ''
      caCrtFileInput.click()
      caCrtFileInput.onchange = file => {
        if (file.target.files[0]) {
          this.dialog.fileForm.cptFileName = file.target.files[0].name
          this.dialog.fileForm.cptFile = file.target.files[0]
        }
      }
    },
    toCreateCpt2 () {
      if (this.dialog.fileForm.cptFileName === '') {
        this.$alert('请选择CPT JSON文件。', '温馨提示', {closeOnClickModal: 'true'}).catch(() => {})
        return
      }
      this.dialog.dialogCreateCpt3Visible = false
      this.dialog.dialogCreateCpt2Visible = true
      let reader = new FileReader()
      if (typeof FileReader === 'undefined') {
        this.$alert('您的浏览器不支持文件读取。', '温馨提示', {closeOnClickModal: 'true'}).catch(() => {})
        return
      }
      reader.readAsText(this.dialog.fileForm.cptFile, 'utf-8')
      var _this = this
      reader.onload = function (e) {
        var data = JSON.parse(e.target.result)
        _this.dialog.multilevelFrom.cptTitle = data.title
        _this.dialog.multilevelFrom.cptDesc = data.description
        _this.dialog.multilevelFrom.cptClaim = data.properties
      }
    },
    registerCpt1 (e) {
      // 校验
      if (this.dialog.oneLeveFrom.cptTitle === '' || this.dialog.oneLeveFrom.cptTitle === undefined) {
        this.$alert('请输入凭证模板标题!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.oneLeveFrom.cptDesc === '' || this.dialog.oneLeveFrom.cptDesc === undefined) {
        this.$alert('请输入凭证模板描述!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.tableData.length === 0) {
        this.$alert('请新增凭证模板字段!', '温馨提示', {}).catch(() => {})
        return
      }
      var claimData = {}
      for (var i = 0; i < this.dialog.tableData.length; i++) {
        var item = {}
        item.type = this.dialog.tableData[i].type
        item.description = this.dialog.tableData[i].description
        if (this.dialog.tableData[i].name.length === 0) {
          this.$alert('请输入字段名!', '温馨提示', {}).catch(() => {})
          return
        }
        claimData[this.dialog.tableData[i].name] = item
      }
      var cptId = this.dialog.oneLeveFrom.cptId
      var cptTitle = this.dialog.oneLeveFrom.cptTitle
      var cptDesc = this.dialog.oneLeveFrom.cptDesc
      var target = e.currentTarget
      this.disableBtn(target)
      this.registerCptInner(cptId, cptTitle, cptDesc, claimData, target)
    },
    registerCpt2 (e) {
      // 校验
      if (this.dialog.multilevelFrom.cptTitle === '' || this.dialog.multilevelFrom.cptTitle === undefined) {
        this.$alert('请输入凭证模板标题!', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.multilevelFrom.cptDesc === '' || this.dialog.multilevelFrom.cptDesc === undefined) {
        this.$alert('请输入凭证模板描述!', '温馨提示', {}).catch(() => {})
        return
      }
      var claimStr = this.$refs.jsoneditorCpt.editor.getText()
      if (claimStr.trim() === '') {
        this.$alert('请输入凭证模板Claim!！', '温馨提示', {}).catch(() => {})
        return
      }
      if (this.dialog.multilevelFrom.cptClaim === '' || this.dialog.multilevelFrom.cptClaim === undefined) {
        this.$alert('请输入凭证模板Claim!', '温馨提示', {}).catch(() => {})
        return
      }
      var cptId = this.dialog.multilevelFrom.cptId
      var cptTitle = this.dialog.multilevelFrom.cptTitle
      var cptDesc = this.dialog.multilevelFrom.cptDesc
      var cptClaim = this.dialog.multilevelFrom.cptClaim
      var target = e.currentTarget
      this.disableBtn(target)
      this.registerCptInner(cptId, cptTitle, cptDesc, cptClaim, target)
    },
    registerCptInner (cptId, cptTitle, cptDesc, cptClaim, target) {
      var cptJsonData = {}
      cptJsonData.title = cptTitle
      cptJsonData.description = cptDesc
      cptJsonData.properties = cptClaim
      var params = {
        cptJson: JSON.stringify(cptJsonData),
        cptId: cptId
      }
      API.doPost('registerCpt', params).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('凭证模板(CPT)注册成功！', '温馨提示', {}).catch(() => {})
          this.dialog.dialogCreateCpt2Visible = false
          this.dialog.dialogCreateCptVisible = false
          this.dialog.dialogCreateCpt1Visible = false
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
        this.enableBtn(target)
      })
    },
    registerClaimPolicy (e) {
      var policy = this.$refs.jsoneditorPolicy.editor.getText()
      if (policy.trim() === '') {
        this.$alert('请填写policy！', '温馨提示').catch(() => {})
        return
      }
      var target = e.currentTarget
      this.disableBtn(target)
      var formData = {}
      formData.cptId = this.dialog.policyInfo.cptId
      formData.policy = JSON.stringify(this.dialog.policyInfo.policy)
      API.doPost('registerClaimPolicy', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.$alert('Policy注册成功！', '温馨提示', {}).catch(() => {})
          this.dialog.dialogRegistPolicyVisible = false
          this.init()
        } else {
          this.$alert(res.data.errorMessage, '温馨提示', {}).catch(() => {})
        }
        this.enableBtn(target)
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
      formData.cptType = this.queryForm.cptType
      API.doGet('getCptInfoList', formData).then(res => {
        if (res.data.errorCode === 0) {
          this.page.cptList = res.data.result.dataList
          this.page.total = res.data.result.allCount
        }
      })
    },
    dateFormat (row) {
      var date = new Date(parseInt(row.time * 1000) + 8 * 3600 * 1000)
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
var json1 = {
  'id': {
    'type': 'string',
    'description': 'user weId'
  },
  'age': {
    'description': 'the age of certificate owner',
    'type': 'integer'
  },
  'gender': {
    'description': 'the gender of certificate owner',
    'enum': [
      'F',
      'M'
    ],
    'type': 'string'
  },
  'name': {
    'description': 'the name of certificate owner',
    'type': 'string'
  }
}
var json2 = {
  'id': {
    'type': 'string',
    'description': 'the WeID of the owner'
  },
  'name': {
    'type': 'string',
    'description': 'the name of object'
  },
  'meta': {
    'type': 'object',
    'properties': {
      'id': {
        'type': 'string',
        'description': 'code'
      },
      'context': {
        'type': 'string',
        'description': 'error'
      },
      'date': {
        'type': 'string',
        'description': 'info'
      }
    }
  },
  'data': {
    'type': 'object',
    'properties': {
      'id': {
        'type': 'string',
        'description': 'id'
      },
      'address': {
        'type': 'string',
        'description': 'sipTellAddress'
      },
      'userlevel': {
        'type': 'integer',
        'description': 'userlevel'
      }
    }
  }
}
</script>
