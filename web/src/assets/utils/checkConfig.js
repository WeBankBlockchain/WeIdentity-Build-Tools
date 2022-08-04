import API from '../../API/resource'
import { MessageBox } from 'Element-ui'

export default {
  data: {
    dbStatus: false,
    nodeStatus: false,
    isEnableMasterCns: false
  },
  async checkDbState () {
    this.data.dbStatus = false
    await API.doGet('dbCheckState').then(res => {
      if (!res.data.result) {
        MessageBox.alert('您数据库配置异常, 请检查数据库配置。', '温馨提示').catch(() => {})
      } else {
        this.data.dbStatus = true
      }
    })
  },
  async checkNodeState (ischeckMaster) {
    this.data.nodeStatus = false
    this.data.isEnableMasterCns = false
    var nodeStatus = false
    await API.doGet('nodeCheckState').then(res => {
      if (!res.data.result) {
        MessageBox.alert('您区块链节点异常，请配置正确的区块链节点。', '温馨提示').catch(() => {})
      } else {
        if (!ischeckMaster) {
          this.data.nodeStatus = true
        }
        nodeStatus = true
      }
    })
    if (nodeStatus && ischeckMaster) {
      await API.doGet('isEnableMasterCns').then(res => {
        if (res.data.result) {
          MessageBox.alert('您未启用主合约，请前往启用主合约。', '温馨提示').catch(() => {})
        } else {
          this.data.nodeStatus = true
          this.data.isEnableMasterCns = true
        }
      })
    }
  }
}
