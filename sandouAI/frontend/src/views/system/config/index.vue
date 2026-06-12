<template>
  <div class="page-container">
    <div class="search-bar"><el-button type="primary" @click="handleAdd">新增参数</el-button></div>
    <el-table :data="list" stripe border>
      <el-table-column prop="config_name" label="参数名称" />
      <el-table-column prop="config_key" label="参数键名" />
      <el-table-column prop="config_value" label="参数值" width="200" />
      <el-table-column label="类型" width="80"><template #default="{row}"><el-tag>{{row.config_type==='Y'?'系统':'自定义'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}">
        <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog :title="formTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="参数名称"><el-input v-model="form.config_name" /></el-form-item>
        <el-form-item label="参数键名"><el-input v-model="form.config_key" :disabled="isEdit" /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="form.config_value" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.config_type"><el-radio label="Y">系统内置</el-radio><el-radio label="N">自定义</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigList, addConfig, updateConfig, deleteConfig } from '../../../api/systemExtend'
const list = ref([]); const dialogVisible = ref(false); const formTitle = ref(''); const form = ref({}); const isEdit = ref(false)
async function load() { try { const r = await getConfigList(); list.value = r.data.data } catch(e) {} }
function handleAdd() { form.value = { config_name: '', config_key: '', config_value: '', config_type: 'N' }; isEdit.value = false; formTitle.value = '新增参数'; dialogVisible.value = true }
function handleEdit(row) { form.value = { ...row }; isEdit.value = true; formTitle.value = '编辑参数'; dialogVisible.value = true }
async function handleSubmit() { try { isEdit.value ? await updateConfig(form.value) : await addConfig(form.value); ElMessage.success('成功'); dialogVisible.value = false; load() } catch(e) {} }
async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteConfig(row.id); ElMessage.success('删除成功'); load() } catch(e) {} }
onMounted(load)
</script>
