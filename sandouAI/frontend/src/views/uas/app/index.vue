<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="search.appName" placeholder="应用名称" clearable style="width:200px" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openAdd">新增</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="appKey" label="AppKey" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="redirectUri" label="回调地址" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑应用' : '新增应用'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="回调地址">
          <el-input v-model="form.redirectUri" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppList, addApp, updateApp, deleteApp } from '../../../api/uas'

const search = reactive({ appName: '' })
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, appName: '', description: '', redirectUri: '', status: 1 })
const rules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
}
const formRef = ref(null)
const submitLoading = ref(false)

async function fetchData() {
  try {
    const res = await getAppList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, appName: '', description: '', redirectUri: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateApp({ ...form })
    } else {
      await addApp({ ...form })
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该应用吗？', '提示', { type: 'warning' })
  try {
    await deleteApp(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
