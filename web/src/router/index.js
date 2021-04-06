const Vue = require('vue')
const Router = require('vue-router')

Vue.use(Router)

export default new Router({
  routes: [
    /* --配置引导-- */
    {
      path: '/',
      meta: { requireAuth: true },
      name: 'index',
      component: resolve => { import('@/module/index').then(module => resolve(module)) },
      children: [
        {
          path: '/guide/step',
          meta: { requireAuth: true },
          name: 'step',
          component: resolve => { import('@/module/guide/step.vue').then(module => resolve(module)) }
        },
        {
          path: '/guide/step0',
          meta: { requireAuth: true },
          name: 'step0',
          component: resolve => { import('@/module/guide/step0.vue').then(module => resolve(module)) }
        }, {
          path: '/guide/step1',
          meta: { requireAuth: true },
          name: 'step1',
          component: resolve => { import('@/module/guide/step1').then(module => resolve(module)) }
        }, {
          path: '/guide/step2',
          meta: { requireAuth: true },
          name: 'step2',
          component: resolve => { import('@/module/guide/step2').then(module => resolve(module)) }
        }, {
          path: '/guide/step3',
          meta: { requireAuth: true },
          name: 'step3',
          component: resolve => { import('@/module/guide/step3').then(module => resolve(module)) }
        }, {
          path: '/guide/step4',
          meta: { requireAuth: true },
          name: 'step4',
          component: resolve => { import('@/module/guide/step4').then(module => resolve(module)) }
        }, {
          path: '/guide/step5',
          meta: { requireAuth: true },
          name: 'step5',
          component: resolve => { import('@/module/guide/step5').then(module => resolve(module)) }
        }
      ]
    },
    /* --日志窗口-- */
    {
      path: '/log',
      meta: { requireAuth: true },
      name: 'log',
      component: resolve => { import('@/module/log.vue').then(module => resolve(module)) }
    },
    /* ---主功能--- */
    {
      path: '/index/',
      meta: { requireAuth: true },
      name: 'pageIndex',
      component: resolve => { import('@/module/page/index').then(module => resolve(module)) },
      children: [
        /* --部署管理-- */
        {
          path: '/index/deploy/deployWeId',
          meta: { requireAuth: true },
          name: 'deployWeId',
          component: resolve => { import('@/module/page/deploy/deployWeIdPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/deploy/deployEvidence',
          meta: { requireAuth: true },
          name: 'deployEvidence',
          component: resolve => { import('@/module/page/deploy/deployEvidencePage.vue').then(module => resolve(module)) }
        },
        /* --功能管理-- */
        {
          path: '/index/weid/dataPanle',
          meta: { requireAuth: true },
          name: 'dataPanle',
          component: resolve => { import('@/module/page/weid/dataPanlePage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/weid/cptList',
          meta: { requireAuth: true },
          name: 'cptList',
          component: resolve => { import('@/module/page/weid/cptListPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/weid/weidList',
          meta: { requireAuth: true },
          name: 'weidList',
          component: resolve => { import('@/module/page/weid/weidListPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/weid/issuerList',
          meta: { requireAuth: true },
          name: 'issuerList',
          component: resolve => { import('@/module/page/weid/issuerListPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/weid/whiteList',
          meta: { requireAuth: true },
          name: 'whiteList',
          component: resolve => { import('@/module/page/weid/whiteListPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/weid/policyList',
          meta: { requireAuth: true },
          name: 'policyList',
          component: resolve => { import('@/module/page/weid/policyListPage.vue').then(module => resolve(module)) }
        },
        /* --配置管理-- */
        {
          path: '/index/conf/nodeConfig',
          meta: { requireAuth: true },
          name: 'nodeConfig',
          component: resolve => { import('@/module/page/conf/nodePage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/conf/groupConfig',
          meta: { requireAuth: true },
          name: 'groupConfig',
          component: resolve => { import('@/module/page/conf/groupPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/conf/dbConfig',
          meta: { requireAuth: true },
          name: 'dbConfig',
          component: resolve => { import('@/module/page/conf/dbPage.vue').then(module => resolve(module)) }
        }, {
          path: '/index/account/accountConfig',
          meta: { requireAuth: true },
          name: 'accountConfig',
          component: resolve => { import('@/module/page/conf/accountPage.vue').then(module => resolve(module)) }
        },
        /* --配置检查管理-- */
        {
          path: '/index/check/dbCheck',
          meta: { requireAuth: true },
          name: 'dbCheck',
          component: resolve => { import('@/module/page/check/dbCheckPage.vue').then(module => resolve(module)) }
        },
        /* --异步存证管理-- */
        {
          path: '/index/asyn/asynEvidenceList',
          meta: { requireAuth: true },
          name: 'asynEvidenceList',
          component: resolve => { import('@/module/page/asyn/evidenceAsynListPage.vue').then(module => resolve(module)) }
        },
        {
          path: '/index/asyn/asynEvidenceDetail',
          meta: { requireAuth: true },
          name: 'asynEvidenceDetail',
          component: resolve => { import('@/module/page/asyn/evidenceAsynDetailPage.vue').then(module => resolve(module)) }
        }
      ]
    }
  ]
})
