<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="search.jobName" placeholder="任务名称" style="width:150px" clearable />
      <el-button type="primary" @click="load" style="margin-left:8px">搜索</el-button>
    </div>
    <el-table :data="filteredJobs" stripe border>
      <el-table-column prop="jobName" label="任务名称" />
      <el-table-column prop="cronExpression" label="Cron表达式" width="140" />
      <el-table-column prop="className" label="任务类名" :show-overflow-tooltip="true" />
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'info'" size="small">{{row.status===1?'运行中':'暂停'}}</el-tag></template></el-table-column>
      <el-table-column prop="nextFireTime" label="下次执行" width="160" :formatter="(r) => r.nextFireTime ? new Date(r.nextFireTime).toLocaleString() : ''" />
      <el-table-column label="操作" width="200"><template #default="{row}">
        <el-button size="small" type="primary">{{row.status===1?'暂停':'启动'}}</el-button>
        <el-button size="small" @click="handleExecute(row)">执行</el-button>
        <el-button size="small" type="info">日志</el-button>
      </template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getJobList, executeJob } from '../../../api/monitor'
const jobs = ref([])
const search = reactive({ jobName: '' })
const filteredJobs = computed(() => jobs.value.filter(j => !search.jobName || j.jobName?.includes(search.jobName)))
function handleExecute(row) { ElMessage.info('定时任务功能需要集成 Quartz 调度器') }
async function load() { try { const r = await getJobList(); jobs.value = r.data.data || [] } catch(e) { jobs.value = [] } }
onMounted(load)
</script>
