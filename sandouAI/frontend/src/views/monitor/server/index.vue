<template>
  <div class="server-monitor">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">CPU</span></template>
          <div class="progress-wrap">
            <el-progress type="dashboard" :percentage="info.cpu?.usage || 0" :color="cpuColor" />
            <div class="progress-info">
              <p>核心数：{{ info.cpu?.cpuNum || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">内存</span></template>
          <div class="progress-wrap">
            <el-progress type="dashboard" :percentage="info.mem?.usage || 0" :color="memColor" />
            <div class="progress-info">
              <p>总内存：{{ formatBytes(info.mem?.total) }}</p>
              <p>已用：{{ formatBytes(info.mem?.used) }}</p>
              <p>剩余：{{ formatBytes(info.mem?.free) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">JVM</span></template>
          <div class="jvm-info">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="JVM版本">{{ info.jvm?.version }}</el-descriptions-item>
              <el-descriptions-item label="最大内存">{{ formatBytes(info.jvm?.max) }}</el-descriptions-item>
              <el-descriptions-item label="已分配">{{ formatBytes(info.jvm?.total) }}</el-descriptions-item>
              <el-descriptions-item label="已使用">{{ formatBytes(info.jvm?.used) }}</el-descriptions-item>
              <el-descriptions-item label="使用率">{{ info.jvm?.usage }}%</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">系统信息</span></template>
          <div class="sys-info">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="操作系统">{{ info.sys?.osName }}</el-descriptions-item>
              <el-descriptions-item label="系统架构">{{ info.sys?.osArch }}</el-descriptions-item>
              <el-descriptions-item label="工作目录">{{ info.sys?.userDir }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px" v-if="info.sysFiles?.length">
      <template #header><span class="card-title">磁盘信息</span></template>
      <el-table :data="info.sysFiles" stripe size="small">
        <el-table-column prop="dirName" label="盘符" />
        <el-table-column prop="typeName" label="文件系统" />
        <el-table-column label="总大小" :formatter="(r) => formatBytes(r.total)" />
        <el-table-column label="已用" :formatter="(r) => formatBytes(r.used)" />
        <el-table-column label="可用" :formatter="(r) => formatBytes(r.free)" />
        <el-table-column label="使用率">
          <template #default="{ row }">
            <el-progress :percentage="row.usage" :color="row.usage > 80 ? '#f56c6c' : '#409eff'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getServerInfo } from '../../../api/monitor'

const info = reactive({
  cpu: {}, mem: {}, jvm: {}, sys: {}, sysFiles: []
})

const cpuColor = computed(() => {
  const v = info.cpu?.usage || 0
  if (v > 80) return '#f56c6c'
  if (v > 60) return '#e6a23c'
  return '#409eff'
})

const memColor = computed(() => {
  const v = info.mem?.usage || 0
  if (v > 80) return '#f56c6c'
  if (v > 60) return '#e6a23c'
  return '#67c23a'
})

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

async function loadServerInfo() {
  try {
    const res = await getServerInfo()
    Object.assign(info, res.data.data)
  } catch (e) {
    console.error('Failed to load server info', e)
  }
}

onMounted(() => { loadServerInfo() })
</script>

<style scoped>
.card-title { font-weight: 600; }
.progress-wrap { text-align: center; padding: 10px 0; }
.progress-info { margin-top: 10px; font-size: 13px; color: #606266; }
.progress-info p { margin: 4px 0; }
</style>
