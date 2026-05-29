<template>
  <div class="page-container">
    <el-card>
      <div style="margin-bottom:12px">
        <el-button type="success" @click="openAdd(null)">新增根菜单</el-button>
      </div>
      <el-table :data="tableData" border stripe row-key="id" default-expand-all>
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="primary">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由" />
        <el-table-column prop="component" label="组件" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openAdd(row)">新增子菜单</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio>
            <el-radio label="C">菜单</el-radio>
            <el-radio label="B">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" required>
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" />
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, addMenu, updateMenu, deleteMenu } from '../../../api/system'

const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, parentId: 0, menuName: '', path: '', component: '', icon: '', menuType: 'M', perms: '', sortOrder: 0, status: 1 })
const formRef = ref(null)

async function fetchData() {
  try {
    const res = await getMenuList()
    tableData.value = res.data.data
  } catch (e) {}
}

function openAdd(parent) {
  isEdit.value = false
  Object.assign(form, { id: null, parentId: parent ? parent.id : 0, menuName: '', path: '', component: '', icon: '', menuType: 'M', perms: '', sortOrder: 0, status: 1 })
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
      await updateMenu({ ...form })
    } else {
      await addMenu({ ...form })
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  try {
    await deleteMenu(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

fetchData()
</script>

<style scoped>
.page-container { height: 100%; }
</style>
