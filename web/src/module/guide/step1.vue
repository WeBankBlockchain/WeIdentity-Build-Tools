<template>
  <div class="app_view_content">
    <div class="app_view_register">
      <section class="content">
        <div class='guide_step_part box'>
          <div class='bottom_line'></div>
          <div class='guide_step_item guide_step_active'>
            <span>1</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>区块链节点配置</p>
          </div>
          <div class='guide_step_item'>
            <span>2</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>设置主群组</p>
          </div>
          <div class='guide_step_item'>
            <span>3</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>数据库配置(可选)</p>
          </div>
          <div class='guide_step_item'>
            <span>4</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p>创建管理员WeID</p>
          </div>
          <div class='guide_step_item' v-if="roleType == '1'">
            <span>5</span>
            <img src="../../assets/image/icon-hook.svg" alt="">
            <p style='width:200px;left:-89px'>部署WeIdentity智能合约</p>
          </div>
        </div>
        <div class="container-fluid">
          <div class="box">
            <div class="card card-primary warning_box">
              <div class="card-header card-title"><h3>配置区块链节点</h3></div>
              <el-form ref="form" label-position="right" :model="form" label-width="220px">
                <el-form-item label="机构名称:" prop="org_id">
                  <el-input v-model="form.org_id" placeholder="Enter orgId" maxlength="30"  style="width: 72%" onkeyup="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')" @blur="form.org_id = $event.target.value"></el-input>
                </el-form-item>
                <el-form-item label="通讯ID:" prop="amop_id">
                  <el-input v-model="form.amop_id" placeholder="Enter amopId" maxlength="30" style="width: 72%" onkeyup="this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')" @blur="form.amop_id = $event.target.value"></el-input>
                  <div class="mark-bottom"><div>此ID用于链上AMOP通讯</div></div>
                </el-form-item>
                <el-form-item label="非国密/国密SSL:" prop="encrypt_type">
                  <el-select v-model="form.encrypt_type" placeholder="非国密/国密SSL" style="width: 72%" :disabled="useWeBase">
                    <el-option label="非国密" value="0"></el-option>
                    <el-option label="国密" value="1"></el-option>
                  </el-select>
                  <div class="mark-bottom">
                    <div>国密链采用SSL通讯，非国密链采用非SSL通讯。</div>
                  </div>
                </el-form-item>
                <el-form-item label="链版本:" prop="chainVersion">
              <el-select v-model="form.chainVersion" placeholder="链版本" style="width: 72%">
                <el-option label="2.0" value="2"></el-option>
                <el-option label="3.0" value="3"></el-option>
              </el-select>
              <!-- <div class="mark-bottom">
                <div>国密链采用SSL通讯，非国密链采用非SSL通讯。</div>
              </div> -->
            </el-form-item>
                <el-form-item label="区块链节点 IP 和 Channel 端口:" prop="blockchain_address">
                  <el-input v-model="form.blockchain_address" placeholder="IP:PORT,IP:PORT" style="width: 72%" onKeyUp="value=value=value.replace(/[^0-9：:，,。.]/g,'');value=value.replace(/[。]/g,'.');value=value.replace(/[：]/g,':');value=value.replace(/\s+/g,'');value=value.replace(/[，]/g,',');" @blur="form.blockchain_address = $event.target.value"></el-input>
                  <el-button type="primary" @click='queryNodeList' class="btn btn_100" style="margin-left:10px" v-if="useWeBase">查询</el-button>
                  <div class="mark-bottom">
                    <div>例如：10.10.4.1:20200；如果多个节点，则请用半角逗号","分割：10.10.4.1:20200,10.10.4.2:20200</div>
                    <div>如果"运行 WeIdentity SDK 的 Server"与区块链节点部署在同一台机器，IP可以使用127.0.0.1</div>
                    <div>如果"运行 WeIdentity SDK 的 Server"与区块链节点部署不在同一台机器，需要根据实际情况进行修改</div>
                  </div>
                </el-form-item>
                <div id="ecdsa"  class="card-body" v-if="form.encrypt_type == 0">
                  <div class='file_part'>
                    <el-form-item label="ca.crt证书:" prop="caCrtFileName">
                      <div class='input_item'>
                        <el-input v-model="form.caCrtFileName" placeholder="Enter caCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('caCrtFile', 'ca.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['ca.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='caCrtFile' style="display:none;">
                  </div>
                  <div class='file_part'>
                    <el-form-item label="sdk.crt证书:" prop="nodeCrtFileName">
                      <div class='input_item'>
                        <el-input v-model="form.nodeCrtFileName" placeholder="Enter sdkCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('nodeCrtFile', 'sdk.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['node.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='nodeCrtFile' style="display:none;" >
                  </div>
                  <div class='file_part'>
                    <el-form-item label="sdk.key证书:" prop="nodeKeyFileName">
                      <div class='input_item'>
                        <el-input v-model="form.nodeKeyFileName" placeholder="Enter sdkKeyFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('nodeKeyFile', 'sdk.key')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['node.key'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='nodeKeyFile' style="display:none;">
                  </div>
                </div>
                <div id="sm2" class="form-group" v-if="form.encrypt_type == 1&&form.chainVersion==='2'">
                  <div class='file_part'>
                    <el-form-item label="gmca.crt证书:" prop="gmCaCrtFileName">
                      <div class='input_item'>
                        <el-input v-model="form.gmCaCrtFileName" placeholder="Enter gmCaCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('gmCaCrtFile', 'gmca.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['gmca.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='gmCaCrtFile' style="display:none;">
                  </div>
                  <div class='file_part'>
                    <el-form-item label="gmsdk.crt证书:" prop="gmSdkCrtFileName">
                      <div class='input_item'>
                        <el-input v-model="form.gmSdkCrtFileName" placeholder="Enter gmSdkCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('gmSdkCrtFile', 'gmsdk.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['gmsdk.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='gmSdkCrtFile' style="display:none;">
                  </div>
                  <div class='file_part'>
                    <el-form-item label="gmsdk.key证书:" prop="gmSdkKeyFileName">
                      <div class='input_item'>
                        <el-input v-model="form.gmSdkKeyFileName" placeholder="Enter gmSdkKeyFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('gmSdkKeyFile', 'gmsdk.key')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['gmsdk.key'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='gmSdkKeyFile' style="display:none;">
                  </div>
                  <div class='file_part'>
                    <el-form-item label="gmensdk.crt证书:" prop="gmenSdkCrtFileName">
                      <div class='input_item'>
                        <el-input v-model="form.gmenSdkCrtFileName" placeholder="Enter gmenSdkCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('gmenSdkCrtFile', 'gmensdk.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['gmensdk.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='gmenSdkCrtFile' style="display:none;">
                  </div>
                  <div class='file_part'>
                    <el-form-item label="gmensdk.key证书:" prop="gmenSdkKeyFileName">
                      <div class='input_item'>
                        <el-input v-model="form.gmenSdkKeyFileName" placeholder="Enter gmenSdkKeyFile" maxlength="30" readOnly style="width: 52%"></el-input>
                        <button type="button" @click="chooseFile('gmenSdkKeyFile', 'gmensdk.key')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                      </div>
                      <div class="mark-bottom mark-red" v-if="form['gmensdk.key'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                    </el-form-item>
                    <input type="file" id ='gmenSdkKeyFile' style="display:none;">
                  </div>
                </div>
                 <div id="sm3" class="form-group" v-if="form.encrypt_type === '1'&&form.chainVersion==='3'">
              <div class='file_part'>
                <el-form-item label="sm_ca.crt证书:" prop="smCaCrtFileName">
                  <div class='input_item'>
                    <el-input v-model="form.smCaCrtFileName" placeholder="Enter smCaCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                    <button type="button" @click="chooseFile('smCaCrtFile', 'sm_ca.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                    <div class="mark-bottom mark-red" v-if="form['smCa.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                  </div>
                </el-form-item>
                <input type="file" id ='smCaCrtFile' style="display:none;">
              </div>
              <div class='file_part'>
                <el-form-item label="sm_sdk.crt证书:" prop="smSdkCrtFileName">
                  <div class='input_item'>
                    <el-input v-model="form.smSdkCrtFileName" placeholder="Enter smSdkCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                    <button type="button" @click="chooseFile('smSdkCrtFile', 'sm_sdk.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                    <div class="mark-bottom mark-red" v-if="form['smSdk.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                  </div>
                </el-form-item>
                <input type="file" id ='smSdkCrtFile' style="display:none;">
              </div>
              <div class='file_part'>
                <el-form-item label="sm_sdk.key证书:" prop="smSdkKeyFileName">
                  <div class='input_item'>
                    <el-input v-model="form.smSdkKeyFileName" placeholder="Enter smSdkKeyFile" maxlength="30" readOnly style="width: 52%"></el-input>
                    <button type="button" @click="chooseFile('smSdkKeyFile', 'sm_sdk.key')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                    <div class="mark-bottom mark-red" v-if="form['smSdk.key'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                  </div>
                </el-form-item>
                <input type="file" id ='smSdkKeyFile' style="display:none;">
              </div>
              <div class='file_part'>
                <el-form-item label="sm_ensdk.crt证书:" prop="smenSdkCrtFileName">
                  <div class='input_item'>
                    <el-input v-model="form.smenSdkCrtFileName" placeholder="Enter smenSdkCrtFile" maxlength="30" readOnly style="width: 52%"></el-input>
                    <button type="button" @click="chooseFile('smenSdkCrtFile', 'sm_ensdk.crt')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                    <div class="mark-bottom mark-red" v-if="form['smenSdk.crt'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                  </div>
                </el-form-item>
                <input type="file" id ='smenSdkCrtFile' style="display:none;">
              </div>
              <div class='file_part'>
                <el-form-item label="sm_ensdk.key证书:" prop="smenSdkKeyFileName">
                  <div class='input_item'>
                    <el-input v-model="form.smenSdkKeyFileName" placeholder="Enter smenSdkKeyFile" maxlength="30" readOnly style="width: 52%"></el-input>
                    <button type="button" @click="chooseFile('smenSdkKeyFile', 'sm_ensdk.key')" class="upload-btn btn btn-block btn-primary btn-flat">选择文件</button>
                    <div class="mark-bottom mark-red" v-if="form['smenSdk.key'] === 'true'"><div>该证书已存在，重新上传将被覆盖。</div></div>
                  </div>
                </el-form-item>
                <input type="file" id ='smenSdkKeyFile' style="display:none;">
              </div>
            </div>
                <div style="text-align: left; padding-left: 28%;">
                  <a href="https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/enterprise_tools/operation.html#get-sdk-file" target="blank_">
                    <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                    <span clas s="icon_question" style="color:#017CFF;font-size:12px;">如何获取证书?</span>
                  </a>
                </div>
              </el-form>
              <div class='sql_warning' style='right:-160px'>
                <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#blockchain-configuration" target="blank_" style='display:block'>
                  <img class="icon_question" src="../../assets/image/icon-question.svg" alt="">
                  <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
                </a>
              </div>
              <div class="bt-part">
                <el-button type="primary" @click='prev' class="btn btn_150">上一步</el-button>
                <el-button type="primary" @click='next($event)' class="btn btn_150">下一步</el-button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
    <br><br><br>

    <!--显示提交过程 -->
    <el-dialog
      title="温馨提示"
      class="dialog-view" width="400px"
      :visible.sync="dialog.checkDetailVisible"
      :close-on-click-modal="false">
      <div class="dialog-body deployMessage">
        <p v-for="val in dialog.checkMessages" :key="val.value">{{val}}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="nextStep" :disabled="!dialog.checkStatus">下一步</el-button>
      </div>
    </el-dialog>

    <!--显示节点列表 -->
    <el-dialog
      title="前置节点列表"
      class="dialog-view"
      width="45%"
      :visible.sync="dialog.dialogNodeListVisible"
      :close-on-click-modal="false">
      <el-table :row-key="getkey" :data="dialog.nodeListPage.nodeList" border="true" cellpadding="0" cellspacing="0" @selection-change="handleSelectionChange">
        <el-table-column type="selection" :reserve-selection="true" width="55" align="center"></el-table-column>
        <el-table-column label="节点ID">
          <template slot-scope='scope'>
            <span class='long_words' :title='scope.row.nodeId'>{{scope.row.nodeId}}</span>
          </template>
        </el-table-column>
        <el-table-column property="frontIp" label="节点IP" width="150"></el-table-column>
        <el-table-column property="channelPort" label="节点端口" width="150"></el-table-column>
        <el-table-column label="群组列表" width="150">
          <template slot-scope="scope">
            <div>
                <span v-for="(item, index) in scope.row.groupList" :key="item">
                  {{item}} <span v-if="index < (scope.row.groupList.length - 1)">,</span>
                </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column property="clientVersion" label="节点版本" width="150"></el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="width_100" @click="chooseNode">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from '../../API/resource'
export default {
  data () {
    return {
      roleType: localStorage.getItem('roleType'),
      useWeBase: false,
      form: {
        caCrtFileName: '',
        nodeKeyFile: '',
        nodeCrtFileName: '',
        nodeCrtFile: '',
        nodeKeyFileName: '',
        caCrtFile: '',
        gmCaCrtFileName: '',
        gmCaCrtFile: '',
        gmSdkCrtFileName: '',
        gmSdkCrtFile: '',
        gmSdkKeyFileName: '',
        gmSdkKeyFile: '',
        gmenSdkCrtFileName: '',
        gmenSdkCrtFile: '',
        gmenSdkKeyFileName: '',
        gmenSdkKeyFile: '',
        smCaCrtFileName: '',
        smCaCrtFile: '',
        smSdkCrtFileName: '',
        smSdkCrtFile: '',
        smSdkKeyFileName: '',
        smSdkKeyFile: '',
        smenSdkCrtFileName: '',
        smenSdkCrtFile: '',
        smenSdkKeyFileName: '',
        smenSdkKeyFile: '',
        encrypt_type: '1',
        org_id: '',
        amop_id: '',
        blockchain_address: '',
        blockchain_fiscobcos_version: '',
        chainVersion:'2'
      },
      dialog: {
        checkDetailVisible: false,
        dialogNodeListVisible: false,
        nodeListPage: {
          nodeList: [],
          selectedRows: []
        },
        checkMessages: [],
        checkStatus: false,
        nextBtn: null
      }
    }
  },
  methods: {
    getkey (row) {
      return row.frontId
    },
    chooseNode () {
      if (this.dialog.nodeListPage.selectedRows.length === 0) {
        this.$alert('请选择需要配置的节点!', '温馨提示', {}).catch(() => {})
        return
      }
      var nodeAddress = ''
      this.dialog.nodeListPage.selectedRows.forEach((item, i) => {
        nodeAddress = nodeAddress + item.frontIp + ':' + item.channelPort
        if ((i + 1) < this.dialog.nodeListPage.selectedRows.length) {
          nodeAddress = nodeAddress + ','
        }
      })
      this.form.blockchain_address = nodeAddress
      this.dialog.dialogNodeListVisible = false
    },
    chooseFile (type, expectFileName) {
      var caCrtFileInput = document.getElementById(type)
      caCrtFileInput.value = ''
      caCrtFileInput.click()
      caCrtFileInput.onchange = file => {
        if (file.target.files[0]) {
          let $file = file.target.files[0]
          let $fileName = $file.name
          if ($fileName !== expectFileName) {
            this.$alert('文件选择错误,请选择名为 ' + expectFileName + ' 的文件!', '温馨提示', {}).catch(() => {})
            return
          }
          if (type === 'caCrtFile') {
            this.form.caCrtFileName = $fileName
            this.form.caCrtFile = $file
          } else if (type === 'nodeCrtFile') {
            this.form.nodeCrtFileName = $fileName
            this.form.nodeCrtFile = $file
          } else if (type === 'nodeKeyFile') {
            this.form.nodeKeyFileName = $fileName
            this.form.nodeKeyFile = $file
          } else if (type === 'gmCaCrtFile') {
            this.form.gmCaCrtFileName = $fileName
            this.form.gmCaCrtFile = $file
          } else if (type === 'gmSdkCrtFile') {
            this.form.gmSdkCrtFileName = $fileName
            this.form.gmSdkCrtFile = $file
          } else if (type === 'gmSdkKeyFile') {
            this.form.gmSdkKeyFileName = $fileName
            this.form.gmSdkKeyFile = $file
          } else if (type === 'gmenSdkCrtFile') {
            this.form.gmenSdkCrtFileName = $fileName
            this.form.gmenSdkCrtFile = $file
          } else if (type === 'gmenSdkKeyFile') {
            this.form.gmenSdkKeyFileName = $fileName
            this.form.gmenSdkKeyFile = $file
          }
           else if (type === 'smCaCrtFile') {
            this.form.smCaCrtFileName = $fileName
            this.form.smCaCrtFile = $file
          }
          else if (type === 'smSdkCrtFile') {
            this.form.smSdkCrtFileName = $fileName
            this.form.smSdkCrtFile = $file
          }
          else if (type === 'smSdkKeyFile') {
            this.form.smSdkKeyFileName = $fileName
            this.form.smSdkKeyFile = $file
          }
          else if (type === 'smenSdkCrtFile') {
            this.form.smenSdkCrtFileName = $fileName
            this.form.smenSdkCrtFile = $file
          }
          else if (type === 'smenSdkKeyFile') {
            this.form.smenSdkKeyFileName = $fileName
            this.form.smenSdkKeyFile = $file
          }
        }
      }
    },
    checkNode () {
      this.dialog.checkDetailVisible = true
      this.dialog.checkMessages.push('节点配置检测中...')
      API.doGet('checkNode').then(res => { // 3. 检查节点配置结果
        this.dialog.checkDetailVisible = true
        this.enableBtn(this.dialog.nextBtn)
        if (res.data.errorCode === 0) {
          this.dialog.checkStatus = true
          this.dialog.checkMessages.push('节点配置检测成功。')
          this.dialog.checkMessages.push('请继续操作。')
        } else {
          this.dialog.checkMessages.push('节点配置检测失败。')
        }
      })
    },
    checkInput () {
      // 1. 校验输入
      if (this.form.org_id === '') {
        this.$alert('请输入机构名!', '温馨提示', {}).catch(() => {})
        return false
      }
      if (this.form.amop_id === '') {
        this.$alert('请输入通信ID名!', '温馨提示', {}).catch(() => {})
        return false
      }
      if (this.form.blockchain_address === '') {
        this.$alert('请输入区块链节点 IP 和 Channel 端口!', '温馨提示', {}).catch(() => {})
        return false
      }
      if (this.form.encrypt_type === '0') {
        if (this.form.caCrtFile === '' && this.form['ca.crt'] === 'false') {
          this.$alert('请选择 ca.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.nodeCrtFile === '' && !this.form['node.crt'] === 'false') {
          this.$alert('请选择 sdk.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.nodeKeyFile === '' && !this.form['node.key'] === 'false') {
          this.$alert('请选择 sdk.key 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
      } else if(this.chainVersion==='2') {
        if (this.form.gmCaCrtFile === '' && !this.form['gmCa.crt'] === 'false') {
          this.$alert('请选择 gmCa.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.gmSdkCrtFile === '' && !this.form['gmSdk.crt'] === 'false') {
          this.$alert('请选择 gmSdk.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.gmSdkKeyFile === '' && !this.form['gmSdk.key'] === 'false') {
          this.$alert('请选择 gmSdk.key 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.gmenSdkCrtFile === '' && !this.form['gmenSdk.crt'] === 'false') {
          this.$alert('请选择 gmenSdk.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.gmenSdkKeyFile === '' && !this.form['gmenSdk.key'] === 'false') {
          this.$alert('请选择 gmenSdk.key 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
      }else{
         if (this.form.smCaCrtFile === '' && !this.form['smCa.crt'] === 'false') {
          this.$alert('请选择 smCa.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.smSdkCrtFile === '' && !this.form['smSdk.crt'] === 'false') {
          this.$alert('请选择 smSdk.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.smSdkKeyFile === '' && !this.form['smSdk.key'] === 'false') {
          this.$alert('请选择 smSdk.key 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.smenSdkCrtFile === '' && !this.form['smenSdk.crt'] === 'false') {
          this.$alert('请选择 smenSdk.crt 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
        if (this.form.smenSdkKeyFile === '' && !this.form['smenSdk.key'] === 'false') {
          this.$alert('请选择 smenSdk.key 证书文件!', '温馨提示', {}).catch(() => {})
          return false
        }
      }
      return true
    },
    nodeConfigUpload () {
      // 1. 检查输入
      if (!this.checkInput()) {
        return
      }
      // 2. 上传配置文件及证书
      var formData = new FormData()
      if (this.form.encrypt_type === '0') {
        formData.append('file', this.form.caCrtFile)
        formData.append('file', this.form.nodeCrtFile)
        formData.append('file', this.form.nodeKeyFile)
      } else if(this.form.chainVersion==='2'){
        formData.append('file', this.form.gmCaCrtFile)
        formData.append('file', this.form.gmSdkCrtFile)
        formData.append('file', this.form.gmSdkKeyFile)
        formData.append('file', this.form.gmenSdkCrtFile)
        formData.append('file', this.form.gmenSdkKeyFile)
      }else{
        formData.append('file', this.form.smCaCrtFile)
        formData.append('file', this.form.smSdkCrtFile)
        formData.append('file', this.form.smSdkKeyFile)
        formData.append('file', this.form.smenSdkCrtFile)
        formData.append('file', this.form.smenSdkKeyFile)
      }
      formData.append('orgId', this.form.org_id)
      formData.append('amopId', this.form.amop_id)
      formData.append('version', this.form.chainVersion)
      formData.append('useSmCrypto', this.form.encrypt_type)
      formData.append('ipPort', this.form.blockchain_address)

      this.dialog.checkMessages = []
      this.dialog.checkDetailVisible = true
      this.dialog.checkMessages.push('节点配置提交中...')
      this.dialog.checkStatus = false
      this.disableBtn(this.dialog.nextBtn)
      API.doPostAndUploadFile('nodeConfigUpload', formData, 10).then(res => { // 上传配置
        this.dialog.checkDetailVisible = true
        if (res.data.errorCode === 0) {
          this.dialog.checkMessages.push('节点配置提交成功。')
          this.checkNode()
        } else {
          this.dialog.checkMessages.push('节点配置提交失败。')
          this.enableBtn(this.dialog.nextBtn)
        }
      })
    },
    next (e) {
      this.dialog.nextBtn = e.currentTarget
      this.nodeConfigUpload()
    },
    nextStep () {
      localStorage.setItem('step', 2)
      this.$router.push({name: 'step2'})
    },
    prev () {
      localStorage.setItem('step', 0)
      this.$router.push({name: 'step0'})
    },
    init () {
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.form = Object.assign(this.form, res.data.result)
          this.useWeBase = JSON.parse(this.form.useWeBase)
          if (this.useWeBase) {
            this.queryNodeType()
          }
        }
      })
    },
    queryNodeType () {
      API.doGet('webase/queryNodeType').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.form.encrypt_type = res.data.result + ''
        }
      })
    },
    queryNodeList () {
      API.doGet('webase/queryNodeList').then(res => { // 获取前置节点列表信息
        if (res.data.errorCode === 0) {
          this.dialog.nodeListPage.nodeList = res.data.result
          this.dialog.dialogNodeListVisible = true
        }
      })
    },
    handleSelectionChange (rows) {
      this.dialog.nodeListPage.selectedRows = rows
    }
  },
  mounted () {
    this.checkStep()
    // 初始化配置信息
    this.init()
  }
}
</script>
