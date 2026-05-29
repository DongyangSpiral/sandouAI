<template>
  <div class="cache-monitor">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">缓存信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="缓存类型">{{ info.info?.cacheType }}</el-descriptions-item>
            <el-descriptions-item label="Redis状态">{{ info.info?.redisEnabled ? '已启用' : '未启用' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">JVM内存</span></template>
          <div class="progress-wrap">
            <el-progress type="dashboard" :percentage="memPercent" color="#409eff" />
            <div class="progress-info">
              <p>最大内存：{{ formatBytes(info.memory?.max) }}</p>
              <p>已分配：{{ formatBytes(info.memory?.total) }}</p>
              <p>已使用：{{ formatBytes(info.memory?.used) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { getCacheInfo } from '../../../api/monitor'

const info = reactive({ info: {}, memory: {} })

const memPercent = computed(() => {
  const m = info.memory
  if (!m?.total || m.total === 0) return 0
  return Math.round(m.used * 100 / m.total)
})

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

async function loadCacheInfo() {
  try {
    const res = await getCacheInfo()
    Object.assign(info, res.data.data)
  } catch (e) {
    console.error('Failed to load cache info', e)
  }
}

onMounted(() => { loadCacheInfo() })
</script>

<style scoped>
.card-title { font-weight: 600; }
.progress-wrap { text-align: center; padding: 10px 0; }
.progress-info { margin-top: 10px; font-size: 13px; color: #606266; }
.progress-info p { margin: 4px 0; }
</style>
