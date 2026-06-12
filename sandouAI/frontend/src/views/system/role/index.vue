<template>
  <div class="page-container">
    <el-card>
      <div class="search-bar">
        <el-input v-model="search.roleName" placeholder="角色名称" clearable style="width:200px" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openAdd">新增</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleKey" label="角色标识" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openAssignMenus(row)">分配菜单</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" required>
          <el-input v-model="form.roleKey" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <el-tree ref="menuTreeRef" :data="menuTree" show-checkbox node-key="id" :props="{ label: 'menuName' }" />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenuIds, assignRoleMenus, getMenuTree } from '../../../api/system'

const search = reactive({ roleName: '' })
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, roleName: '', roleKey: '', description: '', sortOrder: 0, status: 1 })
const formRef = ref(null)

const menuDialogVisible = ref(false)
const assignRoleId = ref(null)
const menuTree = ref([])
const menuTreeRef = ref(null)

async function fetchData() {
  try {
    const res = await getRoleList({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {}
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, roleName: '', roleKey: '', description: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateRole({ ...form })
    } else {
      await addRole({ ...form })
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  try {
    await deleteRole(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

async function openAssignMenus(row) {
  assignRoleId.value = row.id
  try {
    const res1 = await getMenuTree()
    menuTree.value = res1.data.data
    const res2 = await getRoleMenuIds(row.id)
    menuDialogVisible.value = true
    setTimeout(() => {
      if (menuTreeRef.value) {
        menuTreeRef.value.setCheckedKeys(res2.data.data || [])
      }
    }, 100)
  } catch (e) {}
}

async function handleSaveMenus() {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]
  try {
    await assignRoleMenus({ roleId: assignRoleId.value, menuIds })
    ElMessage.success('菜单分配成功')
    menuDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
