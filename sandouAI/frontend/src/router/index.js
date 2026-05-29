import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue') },
      { path: 'system/user', name: 'SystemUser', component: () => import('../views/system/user/index.vue') },
      { path: 'system/role', name: 'SystemRole', component: () => import('../views/system/role/index.vue') },
      { path: 'system/menu', name: 'SystemMenu', component: () => import('../views/system/menu/index.vue') },
      { path: 'system/dept', name: 'SystemDept', component: () => import('../views/system/dept/index.vue') },
      { path: 'system/post', name: 'SystemPost', component: () => import('../views/system/post/index.vue') },
      { path: 'system/dict', name: 'SystemDict', component: () => import('../views/system/dict/index.vue') },
      { path: 'system/config', name: 'SystemConfig', component: () => import('../views/system/config/index.vue') },
      { path: 'system/notice', name: 'SystemNotice', component: () => import('../views/system/notice/index.vue') },
      { path: 'monitor/online', name: 'MonitorOnline', component: () => import('../views/monitor/online/index.vue') },
      { path: 'monitor/job', name: 'MonitorJob', component: () => import('../views/monitor/job/index.vue') },
      { path: 'monitor/server', name: 'MonitorServer', component: () => import('../views/monitor/server/index.vue') },
      { path: 'monitor/cache', name: 'MonitorCache', component: () => import('../views/monitor/cache/index.vue') },
      { path: 'monitor/operlog', name: 'MonitorOperlog', component: () => import('../views/monitor/operlog/index.vue') },
      { path: 'tool/gen', name: 'ToolGen', component: () => import('../views/tool/gen/index.vue') },
      { path: 'tool/swagger', name: 'ToolSwagger', component: () => import('../views/tool/swagger/index.vue') },
      { path: 'tool/form', name: 'ToolForm', component: () => import('../views/tool/form/index.vue') },
      { path: 'uas/user', name: 'UasUser', component: () => import('../views/uas/user/index.vue') },
      { path: 'uas/corp', name: 'UasCorp', component: () => import('../views/uas/corp/index.vue') },
      { path: 'uas/app', name: 'UasApp', component: () => import('../views/uas/app/index.vue') },
      { path: 'uas/log', name: 'UasLog', component: () => import('../views/uas/log/index.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
