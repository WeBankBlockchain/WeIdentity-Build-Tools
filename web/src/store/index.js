const Vue = require('vue')
const Vuex = require('vuex')
Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    actIndex: '1',
    goLogin: false
  },
  mutations: {
    set_Index (state, index) {
      state.actIndex = index
    },
    back (state, goLogin) {
      state.goLogin = goLogin
    }
  }
})
