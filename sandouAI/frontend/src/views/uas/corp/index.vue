<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="search.corpName" placeholder="企业名称" clearable style="width:200px" />
        <el-select v-model="search.status" placeholder="状态" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openAdd">新增</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>

      <el-table :data="tableData" border stripe @selection-change="handleSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="corpName" label="企业名称" />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="contactName" label="联系人" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="登录时间" width="170">
          <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑企业' : '新增企业'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
        <el-form-item label="企业名称" prop="corpName">
          <el-input v-model="form.corpName" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" />
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
import { getCorpList, addCorp, updateCorp, deleteCorp, batchDeleteCorps } from '../../../api/uas'

const search = reactive({ corpName: '', status: null })
const tableData = ref([])
const selectedIds = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, corpName: '', creditCode: '', phone: '', password: '', contactName: '', status: 1 })
const rules = {
  corpName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const formRef = ref(null)
const submitLoading = ref(false)

function handleSelection(val) {
  selectedIds.value = val.map(v => v.id)
}

async function fetchData() {
  try {
    const res = await getCorpList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, corpName: '', creditCode: '', phone: '', password: '', contactName: '', status: 1 })
  rules.password[0].required = true
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row, password: '' })
  rules.password[0].required = false
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCorp({ ...form })
    } else {
      await addCorp({ ...form })
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
  await ElMessageBox.confirm('确定删除该企业吗？', '提示', { type: 'warning' })
  try {
    await deleteCorp(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', { type: 'warning' })
  try {
    await batchDeleteCorps(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch (e) {}
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
