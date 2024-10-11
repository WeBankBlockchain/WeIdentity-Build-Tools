<template>
  <div id='sideBar'>
    <div class="logo">
      <a id="a_index">
        <img src="../assets/image/logo.svg" alt="AdminLTE Logo" class="logo_image icon_question">
        <span class="logo_title icon_question">网页管理工具</span>
      </a>
    </div>
    <el-menu
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"
      background-color="#545c64"
      text-color="#fff"
      :default-openeds="openeds"
      :default-active="actives"
      ref="kzMenu"
      active-text-color="#ffd04b">
      <el-submenu :index="item.index" :key="item.name" v-for="(item) in menuData">
        <template slot="title">
          <i :class="item.icon"></i>
          <span>{{item.name}}</span>
        </template>
        <el-menu-item
          :index="item.index + '-' + subItem.index"
          @click="link(item.index, subItem.index, subItem.linkName)"
          :key="subItem.name"
          v-for="(subItem) in item.subList" v-if="subItem.isShow === true">{{subItem.name}}</el-menu-item>
      </el-submenu>
      <br/><br/>
    </el-menu>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  data () {
    return {
      menuData: [
        {
          index: 1,
          name: '配置管理',
          icon: 'el-icon-menu-self el-icon-menu-1',
          subList: [
            {index: 1, name: '配置区块链节点', linkName: 'nodeConfig', isShow: true},
            {index: 2, name: '配置主群组', linkName: 'groupConfig', isShow: true},
            {index: 3, name: '配置数据库', linkName: 'dbConfig', isShow: true},
            {index: 4, name: '配置WeID账户', linkName: 'accountConfig', isShow: true}
          ]
        }, {
          index: 2,
          name: '智能合约管理',
          icon: 'el-icon-menu-self el-icon-menu-2',
          subList: [
            {index: 1, name: '部署WeIdentity智能合约', linkName: 'deployWeId', isShow: true},
            {index: 2, name: '部署存证智能合约', linkName: 'deployEvidence', isShow: true}
          ]
        }, {
          index: 3,
          name: '功能管理',
          icon: 'el-icon-menu-self el-icon-menu-3',
          subList: [
            {index: 1, name: '数据概览', linkName: 'dataPanle', isShow: true},
            {index: 2, name: 'WeID管理', linkName: 'weidList', isShow: true},
            {index: 3, name: '权威凭证发行者', linkName: 'issuerList', isShow: true},
            {index: 4, name: '白名单功能管理', linkName: 'whiteList', isShow: true},
            {index: 5, name: '凭证模板(CPT)管理', linkName: 'cptList', isShow: true},
            {index: 6, name: '披露策略管理', linkName: 'policyList', isShow: true}
          ]
        }, {
          index: 4,
          name: '异步上链管理',
          icon: 'el-icon-menu-self el-icon-menu-4',
          subList: [
            {index: 1, name: '存证异步上链管理', linkName: 'asynEvidenceList', isShow: true}
          ]
        }, {
          index: 5,
          name: '配置检查管理',
          icon: 'el-icon-menu-self el-icon-menu-5',
          subList: [
            {index: 1, name: '检查数据库配置', linkName: 'dbCheck', isShow: true}
          ]
        }
      ],
      openeds: [0],
      actives: '',
      roleType: localStorage.getItem('roleType')
    }
  },
  methods: {
    link (v1, v2, linkName) {
      this.resetSetItem('menu1', v1)
      this.resetSetItem('menu2', v2)
      this.$router.push({name: linkName})
      localStorage.setItem('linkName', linkName)
    },
    activeMenu () {
      this.openeds[0] = parseInt(sessionStorage.getItem('menu1'))
      this.actives = this.openeds[0] + '-' + sessionStorage.getItem('menu2')
      this.roleType = localStorage.getItem('roleType')
      this.menuData[2].subList[3].isShow = this.roleType === '1'
    },
    checkGuideStatus () {
      API.doGet('getGuideStatus').then(res => {
        if (res.data.result === '') {
          this.$router.push({name: 'step'})
        }
      })
    }
  },
  computed: {

  },
  watch: {

  },
  created () {
    // 检查是否需要引导
    this.checkGuideStatus()
    localStorage.setItem('menuData', JSON.stringify(this.menuData))
    window.addEventListener('setItem', () => {
      this.activeMenu()
    })
  },
  mounted () {
    this.resetSetItem('menu1', sessionStorage.getItem('menu1'))
    this.resetSetItem('menu2', sessionStorage.getItem('menu2'))
    if (sessionStorage.getItem('menu2') === 'null' || this.$route.path.endsWith('deployWeId')) {
      this.link(2, 1, 'deployWeId')
      this.activeMenu()
    }
  }

}
</script>
