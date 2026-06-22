<template>
  <div class="dashboard">
    <section class="welcome-panel">
      <div class="welcome-copy">
        <p class="eyebrow">SANDOU WORKSPACE · {{ today }}</p>
        <h1>早上好，{{ displayName }}<span>。</span></h1>
        <p>文件、协作与权限管理都在一个清晰的工作空间里。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/dfs')"><el-icon><FolderOpened /></el-icon>进入文件库</el-button>
          <el-button size="large" plain @click="$router.push('/team')"><el-icon><UserFilled /></el-icon>打开团队空间</el-button>
        </div>
      </div>
      <div class="hero-art" aria-hidden="true"><div class="orbit orbit-a"></div><div class="orbit orbit-b"></div><div class="hero-folder"><el-icon><FolderOpened /></el-icon></div><div class="float-card card-one"><el-icon><Document /></el-icon><span>项目资料</span></div><div class="float-card card-two"><el-icon><Connection /></el-icon><span>实时协作</span></div></div>
    </section>

    <section class="metrics-grid">
      <article v-for="card in statCards" :key="card.title" class="metric-card glass-card">
        <div class="metric-top"><div class="metric-icon" :style="{ background: card.tint, color: card.color }"><el-icon><component :is="card.icon" /></el-icon></div><span class="metric-trend">{{ card.hint }}</span></div>
        <strong>{{ card.value }}</strong><span>{{ card.title }}</span>
      </article>
    </section>

    <section class="content-grid">
      <article class="workspace-card glass-container">
        <div class="section-heading"><div><p class="eyebrow">QUICK ACCESS</p><h2>常用工作区</h2></div><el-button text @click="$router.push('/dfs')">查看全部 <el-icon><ArrowRight /></el-icon></el-button></div>
        <div class="shortcut-grid">
          <button v-for="item in shortcuts" :key="item.title" class="shortcut" @click="$router.push(item.path)"><span :style="{ background: item.tint, color: item.color }"><el-icon><component :is="item.icon" /></el-icon></span><div><strong>{{ item.title }}</strong><small>{{ item.desc }}</small></div><el-icon class="shortcut-arrow"><ArrowRight /></el-icon></button>
        </div>
      </article>
      <article class="activity-card glass-container">
        <div class="section-heading"><div><p class="eyebrow">ACTIVITY</p><h2>工作动态</h2></div><span class="live-dot">实时</span></div>
        <div class="activity-list"><div v-for="activity in activities" :key="activity.title" class="activity-row"><span class="activity-icon" :style="{ color: activity.color, background: activity.tint }"><el-icon><component :is="activity.icon" /></el-icon></span><div><strong>{{ activity.title }}</strong><small>{{ activity.text }}</small></div><time>{{ activity.time }}</time></div></div>
      </article>
    </section>

    <section class="charts-grid">
      <article class="chart-card glass-container"><div class="section-heading"><div><p class="eyebrow">USER GROWTH</p><h2>用户注册趋势</h2></div><span class="chart-badge">近 6 个月</span></div><div ref="lineChartRef" class="chart"></div></article>
      <article class="chart-card glass-container"><div class="section-heading"><div><p class="eyebrow">LOGIN ACTIVITY</p><h2>登录活跃度</h2></div><span class="chart-badge">近 6 个月</span></div><div ref="barChartRef" class="chart"></div></article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ArrowRight, Connection, Document, Files, FolderOpened, Grid, Key, OfficeBuilding, Share, User, UserFilled } from '@element-plus/icons-vue'
import { getDashboardChart, getDashboardStats } from '../../api/dashboard'

const lineChartRef = ref(null); const barChartRef = ref(null)
let lineChart; let barChart
const displayName = computed(() => JSON.parse(localStorage.getItem('userInfo') || '{}').nickname || JSON.parse(localStorage.getItem('userInfo') || '{}').username || '管理员')
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const statCards = ref([
  { title: '系统管理员', value: 0, icon: UserFilled, color: '#635bff', tint: '#eeedff', hint: '权限与账号' },
  { title: '角色数量', value: 0, icon: Key, color: '#f59e0b', tint: '#fff5df', hint: '安全策略' },
  { title: '自然人用户', value: 0, icon: User, color: '#0ea5a4', tint: '#e3fbf8', hint: '用户资产' },
  { title: '企业用户', value: 0, icon: OfficeBuilding, color: '#ec6a70', tint: '#fff0f1', hint: '组织协作' },
  { title: '应用数量', value: 0, icon: Grid, color: '#3378f6', tint: '#eaf2ff', hint: '已接入服务' },
  { title: '登录日志', value: 0, icon: Document, color: '#8b5cf6', tint: '#f1edff', hint: '安全审计' }
])
const shortcuts = [{ title: '我的文件', desc: '管理个人资料与文件夹', path: '/dfs', icon: FolderOpened, color: '#635bff', tint: '#eeedff' }, { title: '团队空间', desc: '和成员共同推进项目', path: '/team', icon: UserFilled, color: '#0ea5a4', tint: '#e2f9f6' }, { title: '共享资料', desc: '统一查看已分享内容', path: '/dfs', icon: Share, color: '#ef8b4b', tint: '#fff0e6' }, { title: '应用中心', desc: '配置接入与授权服务', path: '/uas/app', icon: Files, color: '#2879e8', tint: '#e9f2ff' }]
const activities = [{ icon: FolderOpened, title: '文件协作空间已就绪', text: '上传、预览、分享和 AI 分析均可从文件库进入。', time: '刚刚', color: '#635bff', tint: '#eeedff' }, { icon: UserFilled, title: '团队成员权限已启用', text: '可按创建者、管理员、成员和访客分配权限。', time: '今天', color: '#0ea5a4', tint: '#e2f9f6' }, { icon: Connection, title: '统一认证服务在线', text: '登录、角色和访问记录集中管理。', time: '稳定运行', color: '#ef8b4b', tint: '#fff0e6' }]

async function loadStats() { try { const { data } = await getDashboardStats(); const d = data.data || {}; const keys = ['sysUserCount', 'sysRoleCount', 'naturalUserCount', 'corpUserCount', 'appCount', 'loginLogCount']; statCards.value.forEach((card, index) => { card.value = d[keys[index]] || 0 }) } catch (e) { console.error(e) } }
async function loadCharts() { try { const { data } = await getDashboardChart(); await nextTick(); initCharts(data.data || {}) } catch (e) { initCharts({ months: [], userRegistration: [], loginCount: [] }) } }
function initCharts(data) { const common = { grid: { left: 6, right: 10, top: 16, bottom: 6, containLabel: true }, xAxis: { type: 'category', data: data.months || [], boundaryGap: false, axisLine: { lineStyle: { color: '#e7ebf2' } }, axisTick: { show: false }, axisLabel: { color: '#9aa3b4' } }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eff2f6', type: 'dashed' } }, axisLabel: { color: '#9aa3b4' } }, tooltip: { trigger: 'axis', borderWidth: 0, backgroundColor: '#20263a', textStyle: { color: '#fff' } } }; lineChart?.dispose(); barChart?.dispose(); if (lineChartRef.value) { lineChart = echarts.init(lineChartRef.value); lineChart.setOption({ ...common, series: [{ type: 'line', data: data.userRegistration || [], smooth: true, showSymbol: false, lineStyle: { width: 3, color: '#635bff' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(99,91,255,.28)' }, { offset: 1, color: 'rgba(99,91,255,0)' }]) } }] }) } if (barChartRef.value) { barChart = echarts.init(barChartRef.value); barChart.setOption({ ...common, xAxis: { ...common.xAxis, boundaryGap: true }, series: [{ type: 'bar', data: data.loginCount || [], barWidth: 18, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#28c3bd' }, { offset: 1, color: '#0ea5a4' }]), borderRadius: [8, 8, 2, 2] } }] }) } }
function resizeCharts() { lineChart?.resize(); barChart?.resize() }
onMounted(() => { loadStats(); loadCharts(); window.addEventListener('resize', resizeCharts) }); onUnmounted(() => { window.removeEventListener('resize', resizeCharts); lineChart?.dispose(); barChart?.dispose() })
</script>

<style scoped>
.dashboard { max-width: 1500px; margin: 0 auto; }.welcome-panel { position: relative; display: flex; min-height: 248px; overflow: hidden; padding: 39px 50px; border-radius: 22px; color: #fff; background: linear-gradient(115deg, #302c83 0%, #4f46e5 52%, #7770f5 100%); box-shadow: 0 18px 34px rgba(79,70,229,.22); }.welcome-panel::after { position: absolute; right: 0; bottom: 0; width: 44%; height: 100%; background: linear-gradient(130deg, transparent, rgba(255,255,255,.08)); content: ''; }.welcome-copy { z-index: 1; }.eyebrow { margin: 0 0 8px; color: #9994ff; font-size: 10px; font-weight: 800; letter-spacing: .13em; }.welcome-panel .eyebrow { color: #c9c6ff; }.welcome-panel h1 { margin: 0; font-size: 30px; letter-spacing: -.8px; }.welcome-panel h1 span { color: #a8f2df; }.welcome-copy > p:not(.eyebrow) { margin: 10px 0 22px; color: #d7d6ff; font-size: 13px; }.hero-actions { display: flex; gap: 10px; }.hero-actions :deep(.el-button) { border: 0; }.hero-actions :deep(.el-button--primary) { background: #fff; color: #4c46dc; }.hero-actions :deep(.el-button--default) { color: #fff; background: rgba(255,255,255,.13); }.hero-art { position: absolute; right: 8%; bottom: 0; width: 350px; height: 248px; }.hero-folder { position: absolute; right: 104px; bottom: 51px; display: grid; width: 115px; height: 83px; place-items: center; border: 10px solid rgba(255,255,255,.12); border-radius: 18px 22px 22px 22px; color: #fff; background: linear-gradient(140deg, #b8b5ff, #7770f5); box-shadow: 0 22px 30px rgba(25,22,106,.25); font-size: 42px; transform: rotate(-6deg); }.hero-folder::before { position: absolute; top: -25px; left: -8px; width: 52px; height: 25px; border-radius: 13px 13px 0 0; background: #aaa6ff; content: ''; }.orbit { position: absolute; border: 1px solid rgba(255,255,255,.23); border-radius: 50%; }.orbit-a { right: 24px; bottom: -150px; width: 350px; height: 350px; }.orbit-b { right: 81px; bottom: -84px; width: 225px; height: 225px; }.float-card { position: absolute; z-index: 2; display: flex; align-items: center; gap: 8px; padding: 9px 12px; border: 1px solid rgba(255,255,255,.22); border-radius: 10px; color: #49419e; background: rgba(255,255,255,.86); box-shadow: 0 9px 16px rgba(32,29,122,.18); font-size: 11px; font-weight: 800; }.float-card svg { color: #6159e8; }.card-one { top: 47px; right: 30px; }.card-two { right: 172px; bottom: 23px; }.metrics-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 14px; margin: 20px 0; }.metric-card { min-height: 135px; padding: 16px; background: #fff; }.metric-top { display: flex; align-items: center; justify-content: space-between; }.metric-icon { display: grid; width: 35px; height: 35px; place-items: center; border-radius: 10px; font-size: 18px; }.metric-trend { color: #9ca6b8; font-size: 10px; }.metric-card > strong { display: block; margin: 17px 0 2px; color: #283146; font-family: 'DM Mono', monospace; font-size: 25px; letter-spacing: -.8px; }.metric-card > span { color: #7b8599; font-size: 11px; }.content-grid, .charts-grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 20px; }.workspace-card, .activity-card, .chart-card { padding: 22px; background: #fff; }.section-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }.section-heading h2 { margin: 0; color: #293247; font-size: 16px; letter-spacing: -.3px; }.section-heading .eyebrow { margin-bottom: 4px; color: #9ba5b8; }.section-heading :deep(.el-button) { color: #635bff; font-size: 11px; }.shortcut-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 9px; }.shortcut { display: flex; align-items: center; gap: 11px; width: 100%; padding: 12px; border: 1px solid #edf0f5; border-radius: 12px; background: #fff; text-align: left; cursor: pointer; transition: all .2s; }.shortcut:hover { border-color: #dcd9ff; background: #fafaff; transform: translateY(-1px); }.shortcut > span { display: grid; width: 35px; height: 35px; place-items: center; border-radius: 10px; }.shortcut div { flex: 1; }.shortcut strong, .shortcut small { display: block; }.shortcut strong { color: #394359; font-size: 12px; }.shortcut small { margin-top: 3px; color: #9aa3b3; font-size: 10px; }.shortcut-arrow { color: #bec5d1; font-size: 13px; }.live-dot { display: inline-flex; align-items: center; gap: 5px; color: #16a085; font-size: 10px; font-weight: 700; }.live-dot::before { width: 6px; height: 6px; border-radius: 50%; background: #1bc9a4; content: ''; box-shadow: 0 0 0 4px #e4fbf4; }.activity-list { display: grid; gap: 13px; }.activity-row { display: flex; align-items: center; gap: 10px; }.activity-icon { display: grid; width: 31px; height: 31px; place-items: center; border-radius: 9px; font-size: 15px; }.activity-row div { flex: 1; min-width: 0; }.activity-row strong, .activity-row small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.activity-row strong { color: #3d465b; font-size: 11px; }.activity-row small { margin-top: 3px; color: #9da6b6; font-size: 10px; }.activity-row time { color: #a8afbd; font-size: 9px; }.charts-grid { margin-top: 20px; }.chart-card { min-height: 300px; }.chart-badge { padding: 5px 8px; border-radius: 7px; color: #8892a6; background: #f5f7fb; font-size: 10px; }.chart { height: 220px; }
</style>
