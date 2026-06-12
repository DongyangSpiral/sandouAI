<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="search.operName" placeholder="操作用户" style="width:150px" clearable /><span style="margin:0 8px"></span>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button type="danger" @click="handleClean">清空日志</el-button>
    </div>
    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="title" label="操作模块" width="120" />
      <el-table-column prop="operName" label="操作人员" width="100" />
      <el-table-column prop="operIp" label="IP地址" width="130" />
      <el-table-column prop="operUrl" label="请求URL" :show-overflow-tooltip="true" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===0?'success':'danger'" size="small">{{row.status===0?'成功':'失败'}}</el-tag></template></el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="160" />
      <el-table-column label="操作" width="80"><template #default="{row}">
        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px;justify-content:flex-end" background layout="total,prev,pager,next" :total="total" :page-size="10" v-model:current-page="pageNum" @current-change="load" />
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOperlogList, deleteOperlog, cleanOperlog } from '../../../api/systemExtend'
const list = ref([]); const loading = ref(false); const total = ref(0); const pageNum = ref(1)
const search = reactive({ operName: '' })
async function load() { loading.value = true; try { const r = await getOperlogList({ pageNum: pageNum.value, pageSize: 10, operName: search.operName }); list.value = r.data.data.records; total.value = r.data.data.total } catch(e) {} finally { loading.value = false } }
async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteOperlog(row.id); ElMessage.success('删除成功'); load() } catch(e) {} }
async function handleClean() { try { await ElMessageBox.confirm('清空所有操作日志?'); await cleanOperlog(); ElMessage.success('已清空'); load() } catch(e) {} }
onMounted(load)
</script>
