webpackJsonp([13],{"CgC/":function(e,t,i){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=i("aA9S"),l=i.n(a),r=i("/p82"),o={data:function(){return{useWeBase:!1,form:{caCrtFileName:"",nodeKeyFile:"",nodeCrtFileName:"",nodeCrtFile:"",nodeKeyFileName:"",caCrtFile:"",gmCaCrtFileName:"",gmCaCrtFile:"",gmSdkCrtFileName:"",gmSdkCrtFile:"",gmSdkKeyFileName:"",gmSdkKeyFile:"",gmenSdkCrtFileName:"",gmenSdkCrtFile:"",gmenSdkKeyFileName:"",gmenSdkKeyFile:"",encrypt_type:0,org_id:"",amop_id:"",blockchain_address:"",blockchain_fiscobcos_version:""},dialog:{checkDetailVisible:!1,checkMessages:[],nextBtn:null,dialogNodeListVisible:!1,nodeListPage:{nodeList:[],selectedRows:[]}}}},methods:{getkey:function(e){return e.frontId},chooseNode:function(){var e=this;if(0!==this.dialog.nodeListPage.selectedRows.length){var t="";this.dialog.nodeListPage.selectedRows.forEach(function(i,a){t=t+i.frontIp+":"+i.channelPort,a+1<e.dialog.nodeListPage.selectedRows.length&&(t+=",")}),this.form.blockchain_address=t,this.dialog.dialogNodeListVisible=!1}else this.$alert("请选择需要配置的节点!","温馨提示",{}).catch(function(){})},chooseFile:function(e,t){var i=this,a=document.getElementById(e);a.value="",a.click(),a.onchange=function(a){if(a.target.files[0]){var l=a.target.files[0],r=l.name;if(r!==t)return void i.$alert("文件选择错误,请选择名为 "+t+" 的文件!","温馨提示",{}).catch(function(){});"caCrtFile"===e?(i.form.caCrtFileName=r,i.form.caCrtFile=l):"nodeCrtFile"===e?(i.form.nodeCrtFileName=r,i.form.nodeCrtFile=l):"nodeKeyFile"===e?(i.form.nodeKeyFileName=r,i.form.nodeKeyFile=l):"gmCaCrtFile"===e?(i.form.gmCaCrtFileName=r,i.form.gmCaCrtFile=l):"gmSdkCrtFile"===e?(i.form.gmSdkCrtFileName=r,i.form.gmSdkCrtFile=l):"gmSdkKeyFile"===e?(i.form.gmSdkKeyFileName=r,i.form.gmSdkKeyFile=l):"gmenSdkCrtFile"===e?(i.form.gmenSdkCrtFileName=r,i.form.gmenSdkCrtFile=l):"gmenSdkKeyFile"===e&&(i.form.gmenSdkKeyFileName=r,i.form.gmenSdkKeyFile=l)}}},checkNode:function(){var e=this;this.dialog.checkDetailVisible=!0,this.dialog.checkMessages.push("节点配置检测中..."),r.a.doGet("checkNode").then(function(t){e.dialog.checkDetailVisible=!0,e.enableBtn(e.dialog.nextBtn),0===t.data.errorCode?(e.dialog.checkMessages.push("节点配置检测成功。"),e.dialog.checkMessages.push("提示：目前暂不支持修改配置动态实时生效，修改配置需重启服务才能生效。")):e.dialog.checkMessages.push("节点配置检测失败。")})},checkInput:function(){if(""===this.form.org_id)return this.$alert("请输入机构名!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.amop_id)return this.$alert("请输入通信ID名!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.blockchain_address)return this.$alert("请输入区块链节点 IP 和 Channel 端口!","温馨提示",{}).catch(function(){}),!1;if("0"===this.form.encrypt_type){if(""===this.form.caCrtFile&&"false"===!this.form["ca.crt"])return this.$alert("请选择 ca.crt 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.nodeCrtFile&&"false"===!this.form["node.crt"])return this.$alert("请选择 node.crt 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.nodeKeyFile&&"false"===!this.form["node.key"])return this.$alert("请选择 node.key 证书文件!","温馨提示",{}).catch(function(){}),!1}else{if(""===this.form.gmCaCrtFile&&"false"===!this.form["gmCa.crt"])return this.$alert("请选择 gmCa.crt 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.gmSdkCrtFile&&"false"===!this.form["gmSdk.crt"])return this.$alert("请选择 gmSdk.crt 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.gmSdkKeyFile&&"false"===!this.form["gmSdk.key"])return this.$alert("请选择 gmSdk.key 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.gmenSdkCrtFile&&"false"===!this.form["gmenSdk.crt"])return this.$alert("请选择 gmenSdk.crt 证书文件!","温馨提示",{}).catch(function(){}),!1;if(""===this.form.gmenSdkKeyFile&&"false"===!this.form["gmenSdk.key"])return this.$alert("请选择 gmenSdk.key 证书文件!","温馨提示",{}).catch(function(){}),!1}return!0},nodeConfigUpload:function(e){var t=this;if(this.checkInput()){var i=new FormData;"0"===this.form.encrypt_type?(i.append("file",this.form.caCrtFile),i.append("file",this.form.nodeCrtFile),i.append("file",this.form.nodeKeyFile)):(i.append("file",this.form.gmCaCrtFile),i.append("file",this.form.gmSdkCrtFile),i.append("file",this.form.gmSdkKeyFile),i.append("file",this.form.gmenSdkCrtFile),i.append("file",this.form.gmenSdkKeyFile)),i.append("orgId",this.form.org_id),i.append("amopId",this.form.amop_id),i.append("version",this.form.blockchain_fiscobcos_version),i.append("encryptType",this.form.encrypt_type),i.append("ipPort",this.form.blockchain_address),this.dialog.nextBtn=e.currentTarget,this.dialog.checkMessages=[],this.dialog.checkDetailVisible=!0,this.dialog.checkMessages.push("节点配置提交中..."),this.disableBtn(this.dialog.nextBtn),r.a.doPostAndUploadFile("nodeConfigUpload",i,10).then(function(e){0===e.data.errorCode?(t.dialog.checkMessages.push("节点配置提交成功。"),t.checkNode()):(t.dialog.checkMessages.push("节点配置提交失败。"),t.enableBtn(t.dialog.nextBtn))})}},init:function(){var e=this;r.a.doGet("loadConfig").then(function(t){0===t.data.errorCode&&(e.form=l()(e.form,t.data.result),e.useWeBase=JSON.parse(e.form.useWeBase),e.useWeBase&&e.queryNodeType())})},queryNodeType:function(){var e=this;r.a.doGet("webase/queryNodeType").then(function(t){0===t.data.errorCode&&(e.form.encrypt_type=t.data.result+"")})},queryNodeList:function(){var e=this;r.a.doGet("webase/queryNodeList").then(function(t){0===t.data.errorCode&&(e.dialog.nodeListPage.nodeList=t.data.result,e.dialog.dialogNodeListVisible=!0)})},handleSelectionChange:function(e){this.dialog.nodeListPage.selectedRows=e}},mounted:function(){this.init()}},s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("section",{staticClass:"content section_main"},[a("div",{staticClass:"box",staticStyle:{width:"70%","margin-left":"200px"}},[a("div",{staticClass:"card card-primary warning_box"},[e._m(0),e._v(" "),a("div",{staticClass:"card-body"},[a("el-form",{ref:"form",attrs:{"label-position":"right",model:e.form,"label-width":"220px","inline-message":"true"}},[a("el-form-item",{attrs:{label:"机构名称:",prop:"org_id"}},[a("el-input",{staticStyle:{width:"72%"},attrs:{placeholder:"Enter orgId",maxlength:"30",onkeyup:"this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')"},on:{blur:function(t){e.form.org_id=t.target.value}},model:{value:e.form.org_id,callback:function(t){e.$set(e.form,"org_id",t)},expression:"form.org_id"}})],1),e._v(" "),a("el-form-item",{attrs:{label:"通讯ID:",prop:"amop_id"}},[a("el-input",{staticStyle:{width:"72%"},attrs:{placeholder:"Enter amopId",maxlength:"30",onkeyup:"this.value=this.value.replace(/[^a-zA-Z0-9]/g,'')"},on:{blur:function(t){e.form.amop_id=t.target.value}},model:{value:e.form.amop_id,callback:function(t){e.$set(e.form,"amop_id",t)},expression:"form.amop_id"}}),e._v(" "),a("div",{staticClass:"mark-bottom"},[a("div",[e._v("此ID用于链上AMOP通讯")])])],1),e._v(" "),a("el-form-item",{attrs:{label:"国密/非国密:",prop:"encrypt_type"}},[a("el-select",{staticStyle:{width:"72%"},attrs:{placeholder:"国密/非国密",disabled:e.useWeBase},model:{value:e.form.encrypt_type,callback:function(t){e.$set(e.form,"encrypt_type",t)},expression:"form.encrypt_type"}},[a("el-option",{attrs:{label:"非国密",value:"0"}}),e._v(" "),a("el-option",{attrs:{label:"国密",value:"1"}})],1)],1),e._v(" "),a("el-form-item",{attrs:{label:"区块链节点 IP 和 Channel 端口:",prop:"blockchain_address"}},[a("el-input",{staticStyle:{width:"72%"},attrs:{placeholder:"IP:PORT,IP:PORT",onKeyUp:"value=value=value.replace(/[^0-9：:，,。.]/g,'');value=value.replace(/[。]/g,'.');value=value.replace(/[：]/g,':');value=value.replace(/\\s+/g,'');value=value.replace(/[，]/g,',');"},on:{blur:function(t){e.form.blockchain_address=t.target.value}},model:{value:e.form.blockchain_address,callback:function(t){e.$set(e.form,"blockchain_address",t)},expression:"form.blockchain_address"}}),e._v(" "),e.useWeBase?a("el-button",{staticClass:"btn btn_100",staticStyle:{"margin-left":"10px"},attrs:{type:"primary"},on:{click:e.queryNodeList}},[e._v("查询")]):e._e(),e._v(" "),a("div",{staticClass:"mark-bottom"},[a("div",[e._v('例如：10.10.4.1:20200；如果多个节点，则请用半角逗号","分割：10.10.4.1:20200,10.10.4.2:20200')]),e._v(" "),a("div",[e._v('如果"运行 WeIdentity SDK 的 Server"与区块链节点部署在同一台机器，IP可以使用127.0.0.1')])])],1),e._v(" "),"0"===e.form.encrypt_type?a("div",{attrs:{id:"ecdsa"}},[a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"ca.crt证书:",prop:"caCrtFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter caCrtFile",maxlength:"30",readOnly:""},model:{value:e.form.caCrtFileName,callback:function(t){e.$set(e.form,"caCrtFileName",t)},expression:"form.caCrtFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("caCrtFile","ca.crt")}}},[e._v("选择文件")]),e._v(" "),e.form["ca.crt"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"caCrtFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"node.crt证书:",prop:"nodeCrtFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter nodeCrtFile",maxlength:"30",readOnly:""},model:{value:e.form.nodeCrtFileName,callback:function(t){e.$set(e.form,"nodeCrtFileName",t)},expression:"form.nodeCrtFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("nodeCrtFile","node.crt")}}},[e._v("选择文件")]),e._v(" "),e.form["node.crt"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"nodeCrtFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"node.key证书:",prop:"nodeKeyFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter nodeKeyFile",maxlength:"30",readOnly:""},model:{value:e.form.nodeKeyFileName,callback:function(t){e.$set(e.form,"nodeKeyFileName",t)},expression:"form.nodeKeyFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("nodeKeyFile","node.key")}}},[e._v("选择文件")]),e._v(" "),e.form["node.key"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"nodeKeyFile"}})],1)]):e._e(),e._v(" "),"1"===e.form.encrypt_type?a("div",{staticClass:"form-group",attrs:{id:"sm2"}},[a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"gmca.crt证书:",prop:"gmCaCrtFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter gmCaCrtFile",maxlength:"30",readOnly:""},model:{value:e.form.gmCaCrtFileName,callback:function(t){e.$set(e.form,"gmCaCrtFileName",t)},expression:"form.gmCaCrtFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("gmCaCrtFile","gmCa.crt")}}},[e._v("选择文件")]),e._v(" "),e.form["gmCa.crt"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"gmCaCrtFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"gmsdk.crt证书:",prop:"gmSdkCrtFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter gmSdkCrtFile",maxlength:"30",readOnly:""},model:{value:e.form.gmSdkCrtFileName,callback:function(t){e.$set(e.form,"gmSdkCrtFileName",t)},expression:"form.gmSdkCrtFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("gmSdkCrtFile","gmSdk.crt")}}},[e._v("选择文件")]),e._v(" "),e.form["gmSdk.crt"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"gmSdkCrtFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"gmsdk.key证书:",prop:"gmSdkKeyFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter gmSdkKeyFile",maxlength:"30",readOnly:""},model:{value:e.form.gmSdkKeyFileName,callback:function(t){e.$set(e.form,"gmSdkKeyFileName",t)},expression:"form.gmSdkKeyFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("gmSdkKeyFile","gmSdk.key")}}},[e._v("选择文件")]),e._v(" "),e.form["gmSdk.key"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"gmSdkKeyFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"gmensdk.crt证书:",prop:"gmenSdkCrtFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter gmenSdkCrtFile",maxlength:"30",readOnly:""},model:{value:e.form.gmenSdkCrtFileName,callback:function(t){e.$set(e.form,"gmenSdkCrtFileName",t)},expression:"form.gmenSdkCrtFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("gmenSdkCrtFile","gmenSdk.crt")}}},[e._v("选择文件")]),e._v(" "),e.form["gmenSdk.crt"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"gmenSdkCrtFile"}})],1),e._v(" "),a("div",{staticClass:"file_part"},[a("el-form-item",{attrs:{label:"gmensdk.key证书:",prop:"gmenSdkKeyFileName"}},[a("div",{staticClass:"input_item"},[a("el-input",{staticStyle:{width:"52%"},attrs:{placeholder:"Enter gmenSdkKeyFile",maxlength:"30",readOnly:""},model:{value:e.form.gmenSdkKeyFileName,callback:function(t){e.$set(e.form,"gmenSdkKeyFileName",t)},expression:"form.gmenSdkKeyFileName"}}),e._v(" "),a("button",{staticClass:"upload-btn btn btn-block btn-primary btn-flat",attrs:{type:"button"},on:{click:function(t){return e.chooseFile("gmenSdkKeyFile","gmenSdk.key")}}},[e._v("选择文件")]),e._v(" "),e.form["gmenSdk.key"]?a("div",{staticClass:"mark-bottom mark-red"},[a("div",[e._v("该证书已存在，重新上传将被覆盖。")])]):e._e()],1)]),e._v(" "),a("input",{staticStyle:{display:"none"},attrs:{type:"file",id:"gmenSdkKeyFile"}})],1)]):e._e(),e._v(" "),a("div",{staticStyle:{"text-align":"left","padding-left":"26%"}},[a("a",{attrs:{href:"https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/enterprise_tools/operation.html#get-sdk-file",target:"blank_"}},[a("img",{staticClass:"icon_question",attrs:{src:i("JaJV"),alt:""}}),e._v(" "),a("span",{staticStyle:{color:"#017CFF","font-size":"12px"},attrs:{clas:"",s:"icon_question"}},[e._v("如何获取证书?")])])])],1),e._v(" "),e._m(1),e._v(" "),a("div",{staticClass:"node_part"},[a("el-button",{staticClass:"btn btn_150",attrs:{type:"primary"},on:{click:function(t){return e.nodeConfigUpload(t)}}},[e._v("完成")])],1)],1)])]),e._v(" "),a("el-dialog",{staticClass:"dialog-view",attrs:{title:"温馨提示",width:"400px",visible:e.dialog.checkDetailVisible,"close-on-click-modal":!1},on:{"update:visible":function(t){return e.$set(e.dialog,"checkDetailVisible",t)}}},[a("div",{staticClass:"dialog-body deployMessage"},e._l(e.dialog.checkMessages,function(t){return a("p",{key:t.value},[e._v(e._s(t))])}),0),e._v(" "),a("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[a("el-button",{staticClass:"width_100",attrs:{type:"primary"},on:{click:function(t){e.dialog.checkDetailVisible=!1}}},[e._v("完成")])],1)]),e._v(" "),a("el-dialog",{staticClass:"dialog-view",attrs:{title:"前置节点列表",width:"45%",visible:e.dialog.dialogNodeListVisible,"close-on-click-modal":!1},on:{"update:visible":function(t){return e.$set(e.dialog,"dialogNodeListVisible",t)}}},[a("el-table",{attrs:{"row-key":e.getkey,data:e.dialog.nodeListPage.nodeList,border:"true",cellpadding:"0",cellspacing:"0"},on:{"selection-change":e.handleSelectionChange}},[a("el-table-column",{attrs:{type:"selection","reserve-selection":!0,width:"55",align:"center"}}),e._v(" "),a("el-table-column",{attrs:{label:"节点ID"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",{staticClass:"long_words",attrs:{title:t.row.nodeId}},[e._v(e._s(t.row.nodeId))])]}}])}),e._v(" "),a("el-table-column",{attrs:{property:"frontIp",label:"节点IP",width:"150"}}),e._v(" "),a("el-table-column",{attrs:{property:"channelPort",label:"节点端口",width:"150"}}),e._v(" "),a("el-table-column",{attrs:{label:"群组列表",width:"150"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("div",e._l(t.row.groupList,function(i,l){return a("span",{key:i},[e._v("\n                "+e._s(i)+" "),l<t.row.groupList.length-1?a("span",[e._v(",")]):e._e()])}),0)]}}])}),e._v(" "),a("el-table-column",{attrs:{property:"clientVersion",label:"节点版本",width:"150"}})],1),e._v(" "),a("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[a("el-button",{staticClass:"width_100",attrs:{type:"primary"},on:{click:e.chooseNode}},[e._v("确定")])],1)],1)],1)},staticRenderFns:[function(){var e=this.$createElement,t=this._self._c||e;return t("div",{staticClass:"card-header card-title"},[t("h3",[this._v("区块链节点配置")])])},function(){var e=this.$createElement,t=this._self._c||e;return t("div",{staticClass:"sql_warning bg_color",staticStyle:{right:"-160px"}},[t("a",{staticStyle:{display:"block"},attrs:{href:"https://weidentity.readthedocs.io/zh_CN/latest/docs/deploy-via-web.html#id4",target:"blank_"}},[t("img",{staticClass:"icon_question",attrs:{src:i("JaJV"),alt:""}}),this._v(" "),t("span",{staticClass:"icon_question",staticStyle:{color:"#017CFF","font-size":"12px",display:"inline-block"}},[this._v("点击查看配置配置教程")])])])}]},n=i("C7Lr")(o,s,!1,null,null,null);t.default=n.exports}});