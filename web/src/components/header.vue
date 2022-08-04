<template>
  <div class='application_header'>
    <div class='header_left_part'>
      <div class='nav_link'>
        <a><span class="nav_title_last">首页</span></a>
        <span class="nav_icon" v-if="menu1Name != ''">/</span>
        <a><span class="nav_title_last">{{menu1Name}}</span></a>
        <span class="nav_icon" v-if="menu2Name != ''">/</span>
        <a><span class="nav_title_last">{{menu2Name}}</span></a>
      </div>
    </div>
    <div class='header_right_part'>
      <div class='nav_link'>
        <a @click='openLog()'><span>查看后台日志</span></a>
        <a @click='openWebase()'><span>区块链浏览器</span></a>
      </div>
      <div class='role_select'>
        <el-form ref="navForm" label-position="right" :model="navForm" label-width="220px">
          <el-select v-model="navForm.roleType" @change="changeRoleType">
            <el-option label="联盟链委员会管理员"  :value="1"></el-option>
            <el-option label="非联盟链委员会管理员"  :value="2"></el-option>
          </el-select>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  components: {
  },
  data () {
    return {
      navForm: {
        roleType: 1
      },
      menuData: JSON.parse(localStorage.getItem('menuData')),
      menu1: 1,
      menu1Name: '',
      menu2: 1,
      menu2Name: ''
    }
  },
  methods: {
    changeNavTitle () {
      for (let i = 0; i < this.menuData.length; i++) {
        var item = this.menuData[i]
        if (item.index + '' === this.menu1 + '') {
          this.menu1Name = item.name
          for (let j = 0; j < item.subList.length; j++) {
            let subItem = item.subList[j]
            if (subItem.index + '' === this.menu2 + '') {
              this.menu2Name = subItem.name
              return
            }
          }
        }
      }
    },
    changeRoleType () {
      localStorage.setItem('roleType', this.navForm.roleType)
      this.resetSetItem('roleType', this.navForm.roleType)
      this.setRole()
    },
    setRole () {
      API.doPost('setRole', {roleType: this.navForm.roleType}).then(res => { // 保存选择的角色
      })
    },
    openWebase () {
      API.doGet('checkWebase').then(res => {
        if (res.data.result) {
          window.open('webase-browser/index.html')
        } else {
          this.$alert('<p>您还没有配置安装启动webase服务! &nbsp;<a target="_blank" href="https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-installation-by-web.html#fisco-bcos-browser">安装教程</a></p>', '温馨提示', {dangerouslyUseHTMLString: true}).catch(() => {})
        }
      })
    },
    openLog () {
      var win = window.open('', 'logWin', 'width=800,height=500,top=100,left=380')
      if (win.location.href === 'about:blank') {
        // 窗口不存在
        win = window.open('#/log/', 'logWin', 'width=800,height=500,top=100,left=380')
      } else {
        // 窗口以已经存在了
        win.focus()
      }
    }
  },
  mounted () {
    window.addEventListener('setItem', () => {
      this.menu1 = sessionStorage.getItem('menu1')
      this.menu2 = sessionStorage.getItem('menu2')
      this.changeNavTitle()
    })
    var roleType = localStorage.getItem('roleType')
    roleType = roleType === null ? 2 : roleType
    this.navForm.roleType = parseInt(roleType)
    this.changeRoleType()
  }
}
</script>
