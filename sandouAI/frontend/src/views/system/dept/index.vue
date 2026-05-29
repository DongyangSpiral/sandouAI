<template>
  <div class="page-container">
    <div class="search-bar"><el-button type="primary" @click="handleAdd">新增部门</el-button></div>
    <el-table :data="list" stripe border>
      <el-table-column prop="dept_name" label="部门名称" />
      <el-table-column prop="order_num" label="排序" width="80" />
      <el-table-column prop="leader" label="负责人" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{row.status===1?'启用':'禁用'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}">
        <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog :title="formTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="部门名称"><el-input v-model="form.dept_name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.order_num" :min="0" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptList, addDept, updateDept, deleteDept } from '../../../api/systemExtend'
const list = ref([])
const dialogVisible = ref(false)
const formTitle = ref('')
const form = ref({})
const isEdit = ref(false)
async function load() { try { const r = await getDeptList(); list.value = r.data.data } catch(e) {} }
function handleAdd() { form.value = { dept_name: '', order_num: 0, leader: '', phone: '', email: '', status: 1 }; isEdit.value = false; formTitle.value = '新增部门'; dialogVisible.value = true }
function handleEdit(row) { form.value = { ...row }; isEdit.value = true; formTitle.value = '编辑部门'; dialogVisible.value = true }
async function handleSubmit() { try { isEdit.value ? await updateDept(form.value) : await addDept(form.value); ElMessage.success('成功'); dialogVisible.value = false; load() } catch(e) {} }
async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteDept(row.id); ElMessage.success('删除成功'); load() } catch(e) {} }
onMounted(load)
</script>
