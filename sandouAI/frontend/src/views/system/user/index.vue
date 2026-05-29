<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="search.username" placeholder="用户名" clearable style="width:200px" />
        <el-select v-model="search.status" placeholder="状态" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openAdd">新增</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openAssignRoles(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        style="margin-top:16px;justify-content:flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
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

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, getUserRoles, assignUserRoles, getAllRoles } from '../../../api/system'

const search = reactive({ username: '', status: null })
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, username: '', password: '', nickname: '', email: '', phone: '', status: 1 })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const formRef = ref(null)
const submitLoading = ref(false)

const roleDialogVisible = ref(false)
const assignUserId = ref(null)
const allRoles = ref([])
const selectedRoleIds = ref([])

async function fetchData() {
  try {
    const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', nickname: '', email: '', phone: '', status: 1 })
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
      await updateUser({ ...form })
    } else {
      await addUser({ ...form })
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
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

async function openAssignRoles(row) {
  assignUserId.value = row.id
  try {
    const res1 = await getAllRoles()
    allRoles.value = res1.data.data
    const res2 = await getUserRoles(row.id)
    selectedRoleIds.value = res2.data.data || []
    roleDialogVisible.value = true
  } catch (e) {}
}

async function handleSaveRoles() {
  try {
    await assignUserRoles({ userId: assignUserId.value, roleIds: selectedRoleIds.value })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
