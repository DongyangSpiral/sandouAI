<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div style="display: flex; gap: 20px;">
            <el-button type="primary" size="large" @click="$router.push('/dfs')">
              <el-icon style="margin-right: 8px;"><FolderOpened /></el-icon>
              进入个人文件管理
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/team')">
              <el-icon style="margin-right: 8px;"><Share /></el-icon>
              进入团队协作空间
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户注册趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>登录统计</span>
            </div>
          </template>
          <div ref="barChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, UserFilled, OfficeBuilding, Grid, Document, Key, FolderOpened, Share } from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardChart } from '../../api/dashboard'

const lineChartRef = ref(null)
const barChartRef = ref(null)
let lineChart = null
let barChart = null

const statCards = ref([
  { title: '系统管理员', value: 0, icon: UserFilled, color: '#409eff' },
  { title: '角色数量', value: 0, icon: Key, color: '#67c23a' },
  { title: '自然人用户', value: 0, icon: User, color: '#e6a23c' },
  { title: '企业用户', value: 0, icon: OfficeBuilding, color: '#f56c6c' },
  { title: '应用数量', value: 0, icon: Grid, color: '#909399' },
  { title: '登录日志', value: 0, icon: Document, color: '#e040fb' }
])

async function loadStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data.data
    statCards.value[0].value = data.sysUserCount || 0
    statCards.value[1].value = data.sysRoleCount || 0
    statCards.value[2].value = data.naturalUserCount || 0
    statCards.value[3].value = data.corpUserCount || 0
    statCards.value[4].value = data.appCount || 0
    statCards.value[5].value = data.loginLogCount || 0
  } catch (e) {
    console.error('Failed to load dashboard stats', e)
  }
}

async function loadCharts() {
  try {
    const res = await getDashboardChart()
    const data = res.data.data

    await nextTick()
    initLineChart(data)
    initBarChart(data)
  } catch (e) {
    console.error('Failed to load dashboard chart', e)
  }
}

function initLineChart(data) {
  if (!lineChartRef.value) return
  if (lineChart) lineChart.dispose()
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户注册'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.months },
    yAxis: { type: 'value' },
    series: [{
      name: '新增用户注册',
      type: 'line',
      smooth: true,
      data: data.userRegistration,
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
      itemStyle: { color: '#409eff' }
    }]
  })
}

function initBarChart(data) {
  if (!barChartRef.value) return
  if (barChart) barChart.dispose()
  barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['登录次数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.months },
    yAxis: { type: 'value' },
    series: [{
      name: '登录次数',
      type: 'bar',
      data: data.loginCount,
      itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] }
    }]
  })
}

function handleResize() {
  lineChart?.resize()
  barChart?.resize()
}

onMounted(() => {
  loadStats()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.stat-card {
  margin-bottom: 20px;
}
.stat-content {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
</style>
