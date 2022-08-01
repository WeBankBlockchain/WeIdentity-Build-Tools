// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import App from './App'
import router from './router'
import store from './store/index'
import VueClipboard from 'vue-clipboard2'
import './assets/utils/dialog'
const Vue = require('vue')

Vue.use(VueClipboard)

Vue.config.productionTip = false
Vue.config.devtools = true;


/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})

Vue.prototype.resetSetItem = function (key, newVal) {
  if (key === 'menu1' || key === 'menu2' || key === 'roleType') {
    // 创建一个StorageEvent事件
    var newStorageEvent = document.createEvent('StorageEvent')
    const storage = {
      setItem: function (k, val) {
        sessionStorage.setItem(k, val)
        // 初始化创建的事件
        newStorageEvent.initStorageEvent('setItem', false, false, k, null, val, null, null)
        // 派发对象
        window.dispatchEvent(newStorageEvent)
      }
    }
    return storage.setItem(key, newVal)
  }
}

var disableClass = 'is-disabled'
// 按钮禁用
Vue.prototype.disableBtn = function (target) {
  var classNameArray = target.className.split(' ')
  if (classNameArray.indexOf(disableClass) === -1) {
    target.className = target.className + ' ' + disableClass
    target.disabled = true
  }
}
// 按钮启用
Vue.prototype.enableBtn = function (target) {
  var classNameArray = target.className.split(' ')
  if (classNameArray.indexOf(disableClass) !== -1) {
    classNameArray.splice(classNameArray.indexOf(disableClass), 1)
    var classNames = classNameArray.join(' ')
    target.disabled = false
    target.className = classNames
  }
}

Vue.prototype.checkStep = function () {
  var path = this.$route.path
  let step = path.substring(11, 12)
  let currentStep = localStorage.getItem('step')
  if (step !== currentStep) {
    this.$router.push({name: 'step' + currentStep})
  }
}
