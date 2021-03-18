<template>
  <section class="content app_main">
    <el-row class="panle">
      <el-col :span="8">
        <div class="panle-box" style="cursor:default">
          <div class="left">
            <div class="title">当前块高</div>
            <div class="data">{{panleData.blockNumber}}</div>
          </div>
          <div class="right">
            <img src="../../../assets/image/block.png" alt="">
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panle-box" @click="toPage('weidList', 2)">
          <div class="left">
            <div class="title">WeID总数</div>
            <div class="data">{{panleData.weIdCount}}</div>
          </div>
          <div class="right">
            <img src="../../../assets/image/weid.png" alt="">
          </div>
        </div>
      </el-col>
      <el-col :span="8" >
        <div class="panle-box" @click="toPage('cptList', 5)">
          <div class="left">
            <div class="title">凭证模板(CPT)总数</div>
            <div class="data">{{panleData.cptCount}}</div>
          </div>
          <div class="right">
            <img src="../../../assets/image/cpt.png" alt="">
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row class="panle">
      <el-col :span="8">
        <div class="panle-box" @click="toPage('issuerList', 3)">
          <div class="left">
            <div class="title">权威凭证发行者(Issuer)总数</div>
            <div class="data">{{panleData.issuerCount}}</div>
          </div>
          <div class="right">
            <img src="../../../assets/image/issuer.png" alt="">
          </div>
        </div>
      </el-col>
      <el-col :span="8" >
        <div class="panle-box" @click="toPage('policyList', 6)">
          <div class="left">
            <div class="title">披露策略(Policy)总数</div>
            <div class="data">{{panleData.policyCount}}</div>
          </div>
          <div class="right">
            <img src="../../../assets/image/issuer-1.png" alt="">
          </div>
        </div>
      </el-col>
    </el-row>
  </section>
</template>
<script>
import API from '../../../API/resource'
import check from '../../../assets/utils/checkConfig'
export default {
  data () {
    return {
      panleData: {
        blockNumber: 0,
        weIdCount: 0,
        cptCount: 0,
        issuerCount: 0,
        policyCount: 0
      }
    }
  },
  methods: {
    toPage (name, index) {
      this.$router.push({name: name})
      this.resetSetItem('menu2', index)
    },
    init () {
      API.doGet('getDataPanel').then(res => {
        if (res.data.errorCode === 0) {
          this.panleData = res.data.result
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
