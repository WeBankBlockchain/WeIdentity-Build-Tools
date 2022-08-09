import { Message } from 'Element-ui'
import store from '../store'
const axios = require('axios')
const qs = require('qs')
const con = require('../../config/config.js')

class BaseModule {
  constructor () {
    this.default_time = 5 * 1000
    this.$http = axios.create({
      timeout: this.default_time,
      baseURL: con.URL
    })

    this.dataMethodDefaults = {
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest'
      }
    }
    this.$http.interceptors.request.use(config => {
      if (config.url.indexOf('doLogin') > -1 || config.url.indexOf('generateVerifCode') > -1) {
        localStorage.setItem('token', '')
      } else {
        config.headers.token = localStorage.getItem('token')
      }
      return config
    })

    this.$http.interceptors.response.use(config => {
      return new Promise((resolve, reject) => {
        let data = config.data
        let status = config.status

        // 临时处理模拟后台返回数据结构，后续删除//////////////////////////////////
        // let res = { errorCode: 0,   errorMessage: 'success',   result: data }
        // config.data = res
        // 临时处理模拟后台返回数据结构，后续删除//////////////////////////////////

        if (status === 200 && data) {
          if (data.code === 10007) {
            Message({
              type: 'error',
              message: '当前会话已失效,请重新登录'
            })
            store.commit('back', true)
          } else {
            resolve(config)
          }
        } else {
          reject(config)
        }
      }).catch((e) => {
        return e
      })
    }, error => {
      console.log(error)
      if (error.message.includes('timeout')) {
        return Promise.resolve({data: this.responseTimeout()})
      }
      return error
    })
  }

  request (config, data = undefined) {
    if (config.method && config.method.toLowerCase() === 'post') {
      if (config.headers) {
        return this.$http({
          url: config.url,
          method: 'post',
          data: qs.stringify(data),
          headers: config.headers,
          timeout: this.getTimeout(config)
        })
      } else {
        return this.post(config.url, data, config)
      }
    } else {
      return this.$http({
        url: config.url,
        method: 'get',
        params: data,
        timeout: this.getTimeout(config)
      })
    }
  }

  get (url, config = {}) {
    return this.$http.get(url.config)
  }

  post (url, data = undefined, config = {}) {
    return this.$http.post(url, data, { ...this.dataMethodDefaults, ...config })
  }

  getTimeout (config) {
    return config.timeout === undefined ? this.default_time : config.timeout
  }

  responseTimeout () {
    return {errorCode: -1, errorMessage: 'timeout', result: null}
  }
}

export default BaseModule
