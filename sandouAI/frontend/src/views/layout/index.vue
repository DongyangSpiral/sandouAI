<template>
  <el-container class="app-shell">
    <el-aside width="252px" class="aside">
      <div class="brand" @click="router.push('/dashboard')">
        <div class="brand-mark"><el-icon><FolderOpened /></el-icon></div>
        <div><strong>Sandou Drive</strong><span>企业文件协作中心</span></div>
      </div>

      <el-menu :default-active="activeMenu" class="nav-menu" @select="handleMenuSelect">
        <p class="nav-label">工作空间</p>
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>概览</span></el-menu-item>
        <el-menu-item index="/dfs"><el-icon><Files /></el-icon><span>我的文件</span></el-menu-item>
        <el-menu-item index="/team"><el-icon><UserFilled /></el-icon><span>团队空间</span></el-menu-item>

        <p class="nav-label">管理控制台</p>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/post">岗位管理</el-menu-item>
          <el-menu-item index="/system/dict">字典管理</el-menu-item>
          <el-menu-item index="/system/config">参数设置</el-menu-item>
          <el-menu-item index="/system/notice">通知公告</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="monitor">
          <template #title><el-icon><Monitor /></el-icon><span>运行监测</span></template>
          <el-menu-item index="/monitor/online">在线用户</el-menu-item>
          <el-menu-item index="/monitor/job">定时任务</el-menu-item>
          <el-menu-item index="/monitor/server">服务监控</el-menu-item>
          <el-menu-item index="/monitor/cache">缓存监控</el-menu-item>
          <el-menu-item index="/monitor/operlog">操作日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="tool">
          <template #title><el-icon><Tools /></el-icon><span>开发工具</span></template>
          <el-menu-item index="/tool/gen">代码生成</el-menu-item>
          <el-menu-item index="/tool/swagger">系统接口</el-menu-item>
          <el-menu-item index="/tool/form">表单构建</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="uas">
          <template #title><el-icon><Key /></el-icon><span>统一认证</span></template>
          <el-menu-item index="/uas/user">自然人用户</el-menu-item>
          <el-menu-item index="/uas/corp">企业用户</el-menu-item>
          <el-menu-item index="/uas/app">应用管理</el-menu-item>
          <el-menu-item index="/uas/log">登录日志</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="storage-card">
        <div><span>存储空间</span><strong>已用 38%</strong></div>
        <el-progress :percentage="38" :show-text="false" :stroke-width="6" color="#7c75ff" />
        <small>38.4 GB / 100 GB</small>
      </div>
    </el-aside>
    <el-container class="content-shell">
      <el-header class="header">
        <div class="header-crumb"><span class="header-dot"></span><span>工作空间</span><i>/</i><strong>{{ route.meta.title || pageTitle }}</strong></div>
        <div class="header-right">
          <el-button circle text><el-icon :size="19"><Bell /></el-icon></el-button>
          <div class="user-chip"><el-avatar :size="32">{{ userInitial }}</el-avatar><span>{{ userInfo?.nickname || userInfo?.username || '管理员' }}</span></div>
          <el-button text class="logout" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, DataAnalysis, Files, FolderOpened, Key, Monitor, Setting, Tools, UserFilled } from '@element-plus/icons-vue'
import { clearAuth } from '@/utils/auth'
import { goDrive } from '@/config/appEntry'

const route = useRoute()
const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => ({ '/dashboard': '概览', '/dfs': '我的文件', '/team': '团队空间' }[route.path] || '管理控制台'))
const userInitial = computed(() => (userInfo.value?.nickname || userInfo.value?.username || 'A').slice(0, 1).toUpperCase())

function handleMenuSelect(index) {
  if (index.startsWith('/dfs') || index.startsWith('/team')) {
    goDrive(index)
    return
  }
  router.push(index)
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.app-shell { min-height: 100vh; background: #f6f7fb; }
.aside { position: relative; display: flex; flex-direction: column; overflow: hidden; padding: 22px 14px 18px; background: #171d35; }
.aside::before { position: absolute; top: -160px; right: -90px; width: 300px; height: 300px; border-radius: 50%; background: #4f46e5; content: ''; filter: blur(4px); opacity: .38; }
.brand { z-index: 1; display: flex; align-items: center; gap: 11px; padding: 0 11px 30px; color: #fff; cursor: pointer; }
.brand-mark { display: grid; width: 35px; height: 35px; place-items: center; border-radius: 11px; background: linear-gradient(135deg, #8b85ff, #5c56e8); box-shadow: 0 8px 18px rgba(79, 70, 229, .38); }
.brand strong, .brand span { display: block; }.brand strong { font-size: 15px; letter-spacing: .1px; }.brand span { margin-top: 2px; color: #939ab5; font-size: 10px; }
.nav-menu { z-index: 1; flex: 1; overflow-y: auto; border-right: 0; background: transparent; --el-menu-bg-color: transparent; --el-menu-text-color: #a8b0c9; --el-menu-hover-bg-color: rgba(255,255,255,.065); --el-menu-active-color: #fff; }
.nav-label { margin: 17px 12px 8px; color: #68718e; font-size: 10px; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
:deep(.el-menu-item), :deep(.el-sub-menu__title) { height: 44px; margin: 2px 0; border-radius: 10px; color: #aeb5cc; font-size: 13px; line-height: 44px; }
:deep(.el-menu-item.is-active) { position: relative; background: linear-gradient(90deg, rgba(112, 105, 255, .96), rgba(91, 84, 225, .84)); color: #fff; box-shadow: 0 8px 18px rgba(52, 47, 168, .23); }
:deep(.el-menu-item.is-active)::after { position: absolute; right: 13px; width: 5px; height: 5px; border-radius: 50%; background: #fff; content: ''; }
:deep(.el-sub-menu .el-menu-item) { min-width: 0; height: 37px; padding-left: 52px !important; line-height: 37px; }
.storage-card { z-index: 1; padding: 15px; border: 1px solid rgba(255,255,255,.08); border-radius: 14px; background: rgba(255,255,255,.055); color: #c3c9dc; }.storage-card div { display: flex; justify-content: space-between; margin-bottom: 11px; font-size: 11px; }.storage-card strong { color: #fff; font-weight: 600; }.storage-card small { display: block; margin-top: 9px; color: #8991ab; font-family: 'DM Mono', monospace; font-size: 10px; }
.content-shell { min-width: 0; }.header { display: flex; align-items: center; justify-content: space-between; height: 72px; padding: 0 34px; background: rgba(255,255,255,.88); border-bottom: 1px solid #edf0f5; }.header-crumb { display: flex; align-items: center; gap: 9px; color: #929bb0; font-size: 12px; }.header-crumb i { color: #c2c8d6; font-style: normal; }.header-crumb strong { color: #3c465d; }.header-dot { width: 8px; height: 8px; border-radius: 50%; background: #6c63e9; box-shadow: 0 0 0 4px #eeedff; }.header-right, .user-chip { display: flex; align-items: center; }.header-right { gap: 12px; }.user-chip { gap: 9px; padding-right: 12px; border-right: 1px solid #e9edf3; color: #3c465d; font-size: 12px; font-weight: 700; }.user-chip :deep(.el-avatar) { background: linear-gradient(135deg, #6259eb, #978dff); font-size: 12px; }.logout { color: #7b8499; }.main { padding: 28px 32px 38px; overflow: auto; }
@media (max-width: 960px) { .aside { width:72px !important; padding:18px 10px; }.brand { justify-content:center; padding:0 0 22px; }.brand > div,.nav-label,.storage-card { display:none; }.nav-menu :deep(.el-menu-item),.nav-menu :deep(.el-sub-menu__title) { justify-content:center; padding:0 !important; }.nav-menu :deep(.el-menu-item span),.nav-menu :deep(.el-sub-menu__title span),.nav-menu :deep(.el-sub-menu__icon-arrow) { display:none; }.nav-menu :deep(.el-sub-menu .el-menu-item) { display:none; }.header { padding:0 20px; }.main { padding:20px; } }
@media (max-width: 640px) { .app-shell { display:block; }.aside { position:fixed; z-index:20; bottom:0; left:0; display:block; width:100% !important; height:62px; padding:5px 7px; overflow:visible; }.brand,.nav-label,.nav-menu :deep(.el-sub-menu),.nav-menu :deep(.el-menu-item:nth-of-type(n+4)) { display:none; }.nav-menu { display:flex; justify-content:space-around; overflow:visible; }.nav-menu :deep(.el-menu-item) { flex:1; height:50px; margin:0; line-height:50px; }.nav-menu :deep(.el-menu-item:nth-of-type(-n+3)) { display:flex; }.content-shell { min-height:100vh; }.header { height:60px; padding:0 16px; }.header-crumb span:not(.header-dot),.header-crumb i { display:none; }.user-chip span,.logout { display:none; }.main { padding:14px 12px 82px; } }
</style>
