webpackJsonp([11],{pnpQ:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var l=a("lC5x"),s=a.n(l),i=a("J0Oq"),o=a.n(i),n=a("/p82"),d=a("gej+"),r={data:function(){return{page:{deployList:[],pageSize:5,total:0,pageIndex:1},roleType:localStorage.getItem("roleType"),dialog:{dialogFormVisible:!1,dialogDetailVisible:!1,dialogDepolyDetailVisible:!1,deployMessages:[],deployForm:{chainId:"",applyName:""},hash:"",deployBtn:null,deployDetail:{}}}},methods:{showAll:function(t){this.$alert(t,"温馨提示",{closeOnClickModal:"true"}).catch(function(){})},showDeployForm:function(){d.a.data.nodeStatus?(this.dialog.deployForm.chainId="",this.dialog.deployForm.applyName="",this.dialog.dialogFormVisible=!0):d.a.checkNodeState(!1)},showDeployDetail:function(t,e){var a=this;n.a.doGet("getDeployInfo/"+t).then(function(t){0===t.data.errorCode?(a.dialog.dialogDetailVisible=!0,a.dialog.deployDetail=t.data.result,a.dialog.deployDetail.owner=e):a.$alert("部署详细查询失败！","温馨提示",{}).catch(function(){})})},changeRoleType:function(){this.roleType=localStorage.getItem("roleType")},deleteRow:function(t,e){var a=this,l=e.currentTarget,s=this;this.$confirm("确认删除吗？","温馨提示",{closeOnClickModal:!1,cancelButtonClass:"el-button--primary"}).then(function(e){s.disableBtn(l),a.removeHash(t,l)}).catch(function(){})},removeHash:function(t,e){var a=this;n.a.doGet("removeHash/"+t+"/1").then(function(t){0===t.data.errorCode?(a.$alert("删除成功!","温馨提示").catch(function(){}),a.init()):a.$alert(t.data.errorMessage,"温馨提示",{}).catch(function(){}),a.enableBtn(e)})},checkInput:function(){return""===this.dialog.deployForm.chainId?(this.$alert("请输入ChainId!","温馨提示",{}).catch(function(){}),!1):""!==this.dialog.deployForm.applyName||(this.$alert("请输入应用名!","温馨提示",{}).catch(function(){}),!1)},comfirmBtn:function(){this.dialog.dialogDepolyDetailVisible=!1,this.dialog.dialogFormVisible=!1},deploy:function(t){var e=this;this.dialog.deployBtn=t.currentTarget,this.checkInput()&&(this.disableBtn(this.dialog.deployBtn),this.dialog.dialogDepolyDetailVisible=!0,this.dialog.deployMessages=[],this.dialog.deployMessages.push("合约部署中..."),n.a.doPost("deploy",this.dialog.deployForm,15).then(function(t){e.dialog.dialogDepolyDetailVisible=!0,0===t.data.errorCode?(e.dialog.hash=t.data.result,e.isEnableMasterCns()):(e.dialog.deployMessages.push("合约部署失败! 请查看日志。"),e.enableBtn(e.dialog.deployBtn))}))},isEnableMasterCns:function(){var t=this;n.a.doGet("isEnableMasterCns").then(function(e){t.dialog.dialogDepolyDetailVisible=!0,e.data.result?t.enableHash(!0):(t.dialog.deployMessages.push("合约部署成功。"),t.dialog.deployMessages.push("请继续操作。"),t.enableBtn(t.dialog.deployBtn),t.init())})},enableHash:function(t){var e=this;this.dialog.deployMessages.push("合约启用中..."),n.a.doGet("enableHash/"+this.dialog.hash,null,10).then(function(a){e.dialog.dialogDepolyDetailVisible=!0,0===a.data.errorCode?(e.dialog.deployMessages.push("合约启用成功。"),t?e.deploySystemCpt():(e.dialog.deployMessages.push("请继续操作。"),e.enableBtn(e.dialog.deployBtn),e.init())):(e.dialog.deployMessages.push("合约启用失败！请查看日志。"),e.enableBtn(e.dialog.deployBtn))})},deploySystemCptBtn:function(t){this.dialog.hash=t,this.deploySystemCpt()},deploySystemCpt:function(){var t=this;this.dialog.deployMessages.push("系统CPT部署中..."),this.dialog.dialogDepolyDetailVisible=!0,n.a.doGet("deploySystemCpt/"+this.dialog.hash,null,10).then(function(e){0===e.data.errorCode?(t.dialog.deployMessages.push("系统CPT部署成功。"),t.dialog.deployMessages.push("系统CPT部署成功! 请继续操作。"),t.init()):t.dialog.deployMessages.push("系统CPT部署失败！请查看日志。"),t.dialog.dialogDepolyDetailVisible=!0,t.enableBtn(t.dialog.deployBtn)})},enable:function(t,e,a){var l=this;this.dialog.deployBtn=a.currentTarget,this.$confirm("是否确定启用该主合约？","温馨提示",{closeOnClickModal:!1,cancelButtonClass:"el-button--primary"}).then(function(a){l.dialog.hash=t,l.dialog.deployMessages=[],l.dialog.dialogDepolyDetailVisible=!0,l.disableBtn(l.dialog.deployBtn),l.enableHash(e)}).catch(function(){})},handleCurrentChange:function(t){t<1&&(t=1),this.page.pageIndex=t},init:function(){var t=this;n.a.doGet("getDeployList").then(function(e){0===e.data.errorCode&&(t.page.deployList=e.data.result,t.page.total=t.page.deployList.length,t.page.total===t.page.pageSize*(t.page.pageIndex-1)&&t.handleCurrentChange(t.page.pageIndex-1))})},check:function(){var t=this;return o()(s.a.mark(function e(){return s.a.wrap(function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,d.a.checkNodeState(!1);case 2:d.a.data.nodeStatus&&t.init();case 3:case"end":return e.stop()}},e,t)}))()}},mounted:function(){var t=this;window.addEventListener("setItem",function(){t.changeRoleType()}),this.check()}},c={render:function(){var t=this,e=t.$createElement,l=t._self._c||e;return l("section",{staticClass:"content app_main"},["1"===t.roleType?l("el-row",{staticClass:"app_main_header_row"},[l("el-col",{attrs:{span:5}},[l("el-button",{staticClass:"btn",staticStyle:{width:"250px"},attrs:{type:"primary"},on:{click:t.showDeployForm}},[t._v("主群组部署 WeIdentity 智能合约")])],1),t._v(" "),l("el-col",{staticClass:"head-icon",attrs:{span:19}},[l("a",{attrs:{href:"https://weid-doc-xml.readthedocs.io/zh/test/docs/weidentity-quick-tools-web.html?highlight=部署 WeIdentity 智能合约#id2",target:"blank_"}},[l("img",{staticClass:"icon_question",attrs:{src:a("JaJV"),alt:""}}),t._v(" "),l("span",{staticClass:"icon_question"},[t._v("多次部署合约")])])])],1):t._e(),t._v(" "),l("div",{staticClass:"app_view"},[l("el-table",{staticStyle:{width:"100%"},attrs:{data:t.page.deployList.slice((t.page.pageIndex-1)*t.page.pageSize,t.page.pageIndex*t.page.pageSize),border:"true",align:"center",cellpadding:"0",cellspacing:"0"}},[l("el-table-column",{attrs:{prop:"applyName",label:"应用名字",align:"center"},scopedSlots:t._u([{key:"default",fn:function(e){return[l("span",{staticClass:"long_words",attrs:{title:e.row.applyName}},[t._v(t._s(e.row.applyName))])]}}])}),t._v(" "),l("el-table-column",{attrs:{label:"智能合约部署ID",align:"center",width:"160"},scopedSlots:t._u([{key:"default",fn:function(e){return[l("span",{staticClass:"long_words link",attrs:{title:e.row.hash},on:{click:function(a){return t.showAll(e.row.hash)}}},[t._v(t._s(e.row.hashShow))])]}}])}),t._v(" "),l("el-table-column",{attrs:{label:"部署的机构名称",align:"center",width:"250"},scopedSlots:t._u([{key:"default",fn:function(e){return null!==e.row.issuer?[l("span",{staticClass:"icon_question",attrs:{title:e.row.issuer.name}},[t._v(t._s(e.row.issuer.name))]),t._v(" "),!0===e.row.issuer.recognized?l("img",{staticClass:"icon_question",attrs:{src:a("MeCv"),widht:"50",height:"50",alt:""}}):t._e(),t._v(" "),!1===e.row.issuer.recognized?l("img",{staticClass:"icon_question",attrs:{src:a("B7UN"),widht:"50",height:"50",alt:""}}):t._e()]:void 0}}],null,!0)}),t._v(" "),l("el-table-column",{attrs:{prop:"groupId",label:"所在群组",align:"center",width:"100"}}),t._v(" "),l("el-table-column",{attrs:{prop:"createTime",label:"创建时间",align:"center",width:"170"}}),t._v(" "),l("el-table-column",{attrs:{label:"操作",align:"center",width:"300"},scopedSlots:t._u([{key:"default",fn:function(e){return[e.row.enable?l("el-button",{staticClass:"btn",attrs:{type:"primary",disabled:""}},[t._v("已启用")]):l("el-button",{staticClass:"btn",attrs:{type:"primary"},on:{click:function(a){return t.enable(e.row.hash,e.row.needDeployCpt,a)}}},[t._v("启用")]),t._v(" "),l("el-button",{staticClass:"btn",attrs:{type:"primary"},on:{click:function(a){return t.deleteRow(e.row.hash,a)}}},[t._v("删除")]),t._v(" "),0==e.row.enable||0==e.row.needDeployCpt?l("el-button",{staticClass:"btn",staticStyle:{width:"90px"},attrs:{type:"primary"},on:{click:function(a){return t.showDeployDetail(e.row.hash,e.row.weId)}}},[t._v("详细信息")]):t._e(),t._v(" "),e.row.enable&&e.row.needDeployCpt?l("el-button",{staticClass:"btn",staticStyle:{width:"120px"},attrs:{type:"primary"},on:{click:function(a){return t.deploySystemCptBtn(e.row.hash)}}},[t._v("部署系统CPT")]):t._e()]}}])})],1),t._v(" "),"1"===t.roleType?l("el-pagination",{attrs:{"current-page":t.page.pageIndex,"page-size":t.page.pageSize,layout:"total, prev, pager, next, jumper",total:t.page.total},on:{"current-change":t.handleCurrentChange}}):t._e()],1),t._v(" "),l("el-dialog",{staticClass:"dialog-view",attrs:{title:"主群组部署 WeIdentity 智能合约",width:"29%",visible:t.dialog.dialogFormVisible,"close-on-click-modal":!1},on:{"update:visible":function(e){return t.$set(t.dialog,"dialogFormVisible",e)}}},[l("div",{staticClass:"dialog-body"},[l("el-form",{attrs:{model:t.dialog.deployForm}},[l("el-form-item",{attrs:{label:"链ID(chain_id)","label-width":t.formLabelWidth}},[l("div",{staticClass:"mark-icon"},[l("a",{attrs:{href:"https://weidentity.readthedocs.io/zh_CN/latest/docs/weidentity-spec.html?highlight=chain-id#id4",target:"blank_"}},[l("img",{staticClass:"icon_question",attrs:{src:a("JaJV"),alt:""}}),t._v(" "),l("span",{staticClass:"icon_question"},[t._v("什么是chain id?")])])]),t._v(" "),l("el-input",{attrs:{placeholder:"Enter chainId",onKeyUp:"this.value=this.value.replace(/\\D/g,'')"},on:{blur:function(e){t.dialog.deployForm.chainId=e.target.value}},model:{value:t.dialog.deployForm.chainId,callback:function(e){t.$set(t.dialog.deployForm,"chainId",e)},expression:"dialog.deployForm.chainId"}})],1),t._v(" "),l("el-form-item",{attrs:{label:"应用名字","label-width":t.formLabelWidth}},[l("div",{staticClass:"mark-text"},[l("span",{},[t._v("给自己的WeIdentity区块链应用起一个名字吧，用于在同一条链上区别不同的 WeIdentity 应用，例如“学历证书应用”，“区块链证件应用”。")])]),t._v(" "),l("el-input",{attrs:{placeholder:"Enter applyName"},model:{value:t.dialog.deployForm.applyName,callback:function(e){t.$set(t.dialog.deployForm,"applyName",e)},expression:"dialog.deployForm.applyName"}})],1)],1)],1),t._v(" "),l("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[l("el-button",{staticClass:"width_100",attrs:{type:"primary"},on:{click:function(e){return t.deploy(e)}}},[t._v("部 署")])],1)]),t._v(" "),l("el-dialog",{staticClass:"dialog-view",attrs:{title:"部署信息",width:"620px",visible:t.dialog.dialogDetailVisible,"close-on-click-modal":!0},on:{"update:visible":function(e){return t.$set(t.dialog,"dialogDetailVisible",e)}}},[l("div",{staticClass:"dialog-body deployDetail"},[l("el-row",[l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("智能合约部署ID")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:20}},[t._v(t._s(t.dialog.deployDetail.hash))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("部署账户")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:20}},[t._v(t._s(t.dialog.deployDetail.owner))])],1),t._v(" "),t.dialog.deployDetail.local?l("el-row",[l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("WeID SDK版本")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:7}},[t._v(t._s(t.dialog.deployDetail.weIdSdkVersion))]),t._v(" "),l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("WeID合约版本")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:9}},[t._v(t._s(t.dialog.deployDetail.contractVersion))])],1):t._e(),t._v(" "),t.dialog.deployDetail.local?l("el-row",[l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("部署来源")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:7}},[t._v(t._s(t.dialog.deployDetail.from))]),t._v(" "),l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("区块链节点版本")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:9}},[t._v(t._s(t.dialog.deployDetail.nodeVerion))])],1):t._e(),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:4}},[t._v("区块链节点")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:20}},[t._v(t._s(t.dialog.deployDetail.nodeAddress))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("WeID智能合约地址")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.weIdAddress))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("CPT智能合约地址")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.cptAddress))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("Authority Issuer 智能合约地址")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.authorityAddress))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("Evidence 智能合约地址")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.evidenceAddress))])],1),t._v(" "),l("el-row",[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("Specific Issuer 智能合约地址")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.specificAddress))])],1),t._v(" "),l("el-row",{staticClass:"none-border"},[l("el-col",{staticClass:"title",attrs:{span:8}},[t._v("Chain Id")]),t._v(" "),l("el-col",{staticClass:"value",attrs:{span:16}},[t._v(t._s(t.dialog.deployDetail.chainId))])],1)],1),t._v(" "),l("br")]),t._v(" "),l("el-dialog",{staticClass:"dialog-view",attrs:{title:"合约部署",width:"400px",visible:t.dialog.dialogDepolyDetailVisible,"close-on-click-modal":!1},on:{"update:visible":function(e){return t.$set(t.dialog,"dialogDepolyDetailVisible",e)}}},[l("div",{staticClass:"dialog-body deployMessage"},t._l(t.dialog.deployMessages,function(e){return l("p",{key:e.value},[t._v(t._s(e))])}),0),t._v(" "),l("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[l("el-button",{staticClass:"width_100",attrs:{type:"primary"},on:{click:t.comfirmBtn}},[t._v("确 定")])],1)])],1)},staticRenderFns:[]},p=a("C7Lr")(r,c,!1,null,null,null);e.default=p.exports}});