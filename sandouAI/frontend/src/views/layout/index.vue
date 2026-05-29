<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">UAMS 管理平台</div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>首页</el-menu-item>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
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
          <template #title><el-icon><Monitor /></el-icon>系统监测</template>
          <el-menu-item index="/monitor/online">在线用户</el-menu-item>
          <el-menu-item index="/monitor/job">定时任务</el-menu-item>
          <el-menu-item index="/monitor/server">服务监控</el-menu-item>
          <el-menu-item index="/monitor/cache">缓存监控</el-menu-item>
          <el-menu-item index="/monitor/operlog">操作日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="tool">
          <template #title><el-icon><Tools /></el-icon>系统工具</template>
          <el-menu-item index="/tool/gen">代码生成</el-menu-item>
          <el-menu-item index="/tool/swagger">系统接口</el-menu-item>
          <el-menu-item index="/tool/form">表单构建</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="uas">
          <template #title><el-icon><Key /></el-icon>统一认证</template>
          <el-menu-item index="/uas/user">自然人用户</el-menu-item>
          <el-menu-item index="/uas/corp">企业用户</el-menu-item>
          <el-menu-item index="/uas/app">应用管理</el-menu-item>
          <el-menu-item index="/uas/log">登录日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span>{{ userInfo?.nickname || userInfo?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; overflow-y: auto; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,.1); }
.header { display: flex; align-items: center; justify-content: flex-end; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.main { background: #f0f2f5; padding: 20px; }
</style>
