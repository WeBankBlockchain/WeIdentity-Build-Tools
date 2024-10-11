import BaseModel from './BaseModel'
class ResoruceService extends BaseModel {
  constructor () {
    super()
    this.headers_post = {
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
      'X-Requested-With': 'XMLHttpRequest'
    }
  }
  ajax (type, url, param, headers, timeout) {
    if (type === 'upload') {
      return this.request({'method': 'post', 'url': url, 'timeout': timeout * 1000}, param)
    } else {
      return this.request({'method': type, 'url': url, headers: headers, 'timeout': timeout * 1000}, param)
    }
  }
  doPost (url, param, timeout) {
    return this.ajax('post', url, param, this.headers_post, timeout)
  }
  doPostAndUploadFile (url, param, timeout) {
    return this.doPostByJson(url, param, timeout)
  }
  doPostByJson (url, param, timeout) {
    return this.ajax('upload', url, param, null, timeout)
  }
  doGet (url, param, timeout) {
    return this.ajax('get', url, param, null, timeout)
  }
}

export default new ResoruceService()
