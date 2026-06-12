<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="search.phone" placeholder="手机号" clearable style="width:200px" />
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
        <el-table-column prop="phoneMasked" label="手机号" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="idCardType" label="证件类型" width="100">
          <template #default="{ row }">
            {{ row.idCardType === 'ID_CARD' ? '身份证' : row.idCardType === 'PASSPORT' ? '护照' : row.idCardType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="idCardNoMasked" label="证件号码" />
        <el-table-column prop="certLevel" label="认证等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.certLevel === 2" type="success">高级</el-tag>
            <el-tag v-else-if="row.certLevel === 1" type="primary">实名</el-tag>
            <el-tag v-else type="info">未认证</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="认证等级">
          <el-select v-model="form.certLevel">
            <el-option label="高级" :value="2" />
            <el-option label="实名" :value="1" />
            <el-option label="未认证" :value="0" />
          </el-select>
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
import { getUasUserList, addUasUser, updateUasUser, deleteUasUser, batchDeleteUasUsers } from '../../../api/uas'

const search = reactive({ phone: '', status: null })
const tableData = ref([])
const selectedIds = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, phone: '', password: '', realName: '', nickname: '', certLevel: 0, status: 1 })
const rules = {
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
    const res = await getUasUserList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, phone: '', password: '', realName: '', nickname: '', certLevel: 0, status: 1 })
  rules.password[0].required = true
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, phone: row.phone, password: '', realName: row.realName, nickname: row.nickname, certLevel: row.certLevel, status: row.status })
  rules.password[0].required = false
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUasUser({ ...form })
    } else {
      await addUasUser({ ...form })
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
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  try {
    await deleteUasUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', { type: 'warning' })
  try {
    await batchDeleteUasUsers(selectedIds.value)
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
