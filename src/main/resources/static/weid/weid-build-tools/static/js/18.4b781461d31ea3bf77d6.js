(window.webpackJsonp=window.webpackJsonp||[]).push([[18,17,25,26,27],{Mt0T:function(t,e,n){"use strict";var r=n("yXPU"),o=n.n(r),a=n("o0o1"),s=n.n(a),u=n("NVG9"),i=n("QDv8");e.a={data:{dbStatus:!1,nodeStatus:!1,isEnableMasterCns:!1},checkDbState:function(){var t=this;return o()(s.a.mark((function e(){return s.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return t.data.dbStatus=!1,e.next=3,u.a.doGet("dbCheckState").then((function(e){e.data.result?t.data.dbStatus=!0:i.MessageBox.alert("您数据库配置异常, 请检查数据库配置。","温馨提示").catch((function(){}))}));case 3:case"end":return e.stop()}}),e)})))()},checkNodeState:function(t){var e=this;return o()(s.a.mark((function n(){var r;return s.a.wrap((function(n){for(;;)switch(n.prev=n.next){case 0:return e.data.nodeStatus=!1,e.data.isEnableMasterCns=!1,r=!1,n.next=5,u.a.doGet("nodeCheckState").then((function(n){n.data.result?(t||(e.data.nodeStatus=!0),r=!0):i.MessageBox.alert("您区块链节点异常，请配置正确的区块链节点。","温馨提示").catch((function(){}))}));case 5:if(!r||!t){n.next=8;break}return n.next=8,u.a.doGet("isEnableMasterCns").then((function(t){t.data.result?i.MessageBox.alert("您未启用主合约，请前往启用主合约。","温馨提示").catch((function(){})):(e.data.nodeStatus=!0,e.data.isEnableMasterCns=!0)}));case 8:case"end":return n.stop()}}),n)})))()}}},NVG9:function(t,e,n){"use strict";var r=n("lwsE"),o=n.n(r),a=n("W8MJ"),s=n.n(a),u=n("7W2i"),i=n.n(u),c=n("a1gu"),l=n.n(c),d=n("Nsbk"),h=n.n(d),f=n("lSNA"),p=n.n(f),v=n("QDv8"),m=n("Q2AE");function y(t,e){var n=Object.keys(t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(t);e&&(r=r.filter((function(e){return Object.getOwnPropertyDescriptor(t,e).enumerable}))),n.push.apply(n,r)}return n}function g(t){for(var e=1;e<arguments.length;e++){var n=null!=arguments[e]?arguments[e]:{};e%2?y(Object(n),!0).forEach((function(e){p()(t,e,n[e])})):Object.getOwnPropertyDescriptors?Object.defineProperties(t,Object.getOwnPropertyDescriptors(n)):y(Object(n)).forEach((function(e){Object.defineProperty(t,e,Object.getOwnPropertyDescriptor(n,e))}))}return t}var b=n("zr5I"),k=n("eW3l"),w=n("OcYQ");function O(t){var e=function(){if("undefined"==typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"==typeof Proxy)return!0;try{return Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],(function(){}))),!0}catch(t){return!1}}();return function(){var n,r=h()(t);if(e){var o=h()(this).constructor;n=Reflect.construct(r,arguments,o)}else n=r.apply(this,arguments);return l()(this,n)}}var j=function(t){i()(n,t);var e=O(n);function n(){var t;return o()(this,n),(t=e.call(this)).headers_post={"Content-Type":"application/x-www-form-urlencoded;charset=UTF-8","X-Requested-With":"XMLHttpRequest"},t}return s()(n,[{key:"ajax",value:function(t,e,n,r,o){return"upload"===t?this.request({method:"post",url:e,timeout:1e3*o},n):this.request({method:t,url:e,headers:r,timeout:1e3*o},n)}},{key:"doPost",value:function(t,e,n){return this.ajax("post",t,e,this.headers_post,n)}},{key:"doPostAndUploadFile",value:function(t,e,n){return this.doPostByJson(t,e,n)}},{key:"doPostByJson",value:function(t,e,n){return this.ajax("upload",t,e,null,n)}},{key:"doGet",value:function(t,e,n){return this.ajax("get",t,e,null,n)}}]),n}(function(){function t(){var e=this;o()(this,t),this.default_time=5e3,this.$http=b.create({timeout:this.default_time,baseURL:w.URL}),this.dataMethodDefaults={headers:{"Content-Type":"application/json;charset=UTF-8","X-Requested-With":"XMLHttpRequest"}},this.$http.interceptors.request.use((function(t){return t.url.indexOf("doLogin")>-1||t.url.indexOf("generateVerifCode")>-1?localStorage.setItem("token",""):t.headers.token=localStorage.getItem("token"),t})),this.$http.interceptors.response.use((function(t){return new Promise((function(e,n){var r=t.data;200===t.status&&r?10007===r.code?(Object(v.Message)({type:"error",message:"当前会话已失效,请重新登录"}),m.a.commit("back",!0)):e(t):n(t)})).catch((function(t){return t}))}),(function(t){return console.log(t),t.message.includes("timeout")?Promise.resolve({data:e.responseTimeout()}):t}))}return s()(t,[{key:"request",value:function(t){var e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:void 0;return t.method&&"post"===t.method.toLowerCase()?t.headers?this.$http({url:t.url,method:"post",data:k.stringify(e),headers:t.headers,timeout:this.getTimeout(t)}):this.post(t.url,e,t):this.$http({url:t.url,method:"get",params:e,timeout:this.getTimeout(t)})}},{key:"get",value:function(t){return this.$http.get(t.config)}},{key:"post",value:function(t){var e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:void 0,n=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{};return this.$http.post(t,e,g(g({},this.dataMethodDefaults),n))}},{key:"getTimeout",value:function(t){return void 0===t.timeout?this.default_time:t.timeout}},{key:"responseTimeout",value:function(){return{errorCode:-1,errorMessage:"timeout",result:null}}}]),t}());e.a=new j},OcYQ:function(t,e){e.URL="/weid/weid-build-tools/"}}]);