<template>
  <section class="content section_main">
    <div class="box" style="width: 55%; margin-left: 200px">
      <div id="AccountDiv" class="card card-primary warning_box">
        <div class="card-header card-title">
          <h3>主群组配置</h3>
          <div>
            <a @click="dialog.dialogTableVisible = true" style="color:#017CFF;font-size:12px;display:inline-block;cursor: pointer">查看群组列表</a>
          </div>
        </div>
        <div class="card-mark">
          <div class="card-mark-text">选择一个FISCO-BCOS的群组，作为 WeIdentity 的主群组（即部署 WeIdentity 智能合约的群组）</div>
          <div class="card-mark-icon">
            <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html?highlight=群组#group-selection" target="blank_">
              <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
              <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block"> 什么是群组？</span>
            </a>
          </div>
        </div>
        <div class="card-body">
          <div style='margin-top:12px'>
            <div class="form-group" style='width:100%;'>
              <el-form ref="form" :model="form">
                <el-form-item prop="groupId">
                  <el-select v-model="form.groupId" placeholder="主群组" style="width: 100%">
                    <el-option v-for="val in groupList"
                    :key="val.value"
                    :label="val.value"
                    :value="val.value">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="db-part">
            <el-button type="primary" @click='setGroupId' class="btn btn_150">完成</el-button>
          </div>
          <div class='sql_warning bg_color' style='right:-160px'>
            <a href="https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#group-selection" target="blank_" style='display:block'>
              <img class="icon_question" src="../../../assets/image/icon-question.svg" alt="">
              <span class="icon_question" style="color:#017CFF;font-size:12px;display:inline-block">点击查看配置配置教程</span>
            </a>
          </div>
        </div>
      </div>
    </div>

    <!--显示群组列表-->
    <el-dialog
      title="群组列表"
      class="dialog-view"
      width="55%"
      :visible.sync="dialog.dialogTableVisible"
      :close-on-click-modal="false">
      <el-table :data="dialog.groupNodeList" border="true" align="center">
        <el-table-column prop="groupId" label="群组编号" align="center" width='120'></el-table-column>
        <el-table-column prop="nodes" label="节点列表" align="center"></el-table-column>
        <el-table-column prop="type" label="群主类型" align="center" width='120'></el-table-column>
      </el-table>
      <br/>
    </el-dialog>
  </section>
</template>
<script>
import API from '../../../API/resource'
export default {
  data () {
    return {
      form: {
        groupId: ''
      },
      groupList: [],
      dialog: {
        groupNodeList: [],
        dialogTableVisible: false
      }
    }
  },
  methods: {
    setGroupId () {
      API.doPost('setGroupId', this.form).then(res => { // 保存选择的角色
        if (res.data.errorCode === 0) {
          this.$alert('群组配置成功!<br/>提示：目前暂不支持修改配置动态实时生效，修改配置需重启服务才能生效。', '温馨提示', {dangerouslyUseHTMLString: true}).catch(() => {})
        } else {
          this.$alert('群组配置失败!', '温馨提示', {}).catch(() => {})
        }
      })
    },
    init () {
      // 初始化群组
      API.doGet('getAllGroup/false').then(res => {
        if (res.data.errorCode === 0) {
          this.groupList = res.data.result
          this.form.groupId = this.groupList[0].value
        }
      })
      API.doGet('loadConfig').then(res => { // 获取配置信息
        if (res.data.errorCode === 0) {
          this.form.groupId = res.data.result.group_id
        }
      })
      API.doGet('getGroupMapping').then(res => {
        if (res.data.errorCode === 0) {
          this.dialog.groupNodeList = res.data.result
        }
      })
    }
  },
  mounted () {
    this.init()
  }
}
</script>
