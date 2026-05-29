<template>
  <div class="page-container">
    <div class="search-bar"><el-button type="primary" @click="handleAdd">新增公告</el-button></div>
    <el-table :data="list" stripe border>
      <el-table-column prop="notice_title" label="标题" />
      <el-table-column label="类型" width="80"><template #default="{row}"><el-tag :type="row.notice_type==='2'?'warning':''">{{row.notice_type==='2'?'公告':'通知'}}</el-tag></template></el-table-column>
      <el-table-column prop="notice_content" label="内容" :show-overflow-tooltip="true" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{row.status===1?'启用':'禁用'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}">
        <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog :title="formTitle" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.notice_title" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.notice_type"><el-radio label="1">通知</el-radio><el-radio label="2">公告</el-radio></el-radio-group></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.notice_content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, addNotice, updateNotice, deleteNotice } from '../../../api/systemExtend'
const list = ref([]); const dialogVisible = ref(false); const formTitle = ref(''); const form = ref({}); const isEdit = ref(false)
async function load() { try { const r = await getNoticeList(); list.value = r.data.data } catch(e) {} }
function handleAdd() { form.value = { notice_title: '', notice_type: '1', notice_content: '', status: 1 }; isEdit.value = false; formTitle.value = '新增公告'; dialogVisible.value = true }
function handleEdit(row) { form.value = { ...row }; isEdit.value = true; formTitle.value = '编辑公告'; dialogVisible.value = true }
async function handleSubmit() { try { isEdit.value ? await updateNotice(form.value) : await addNotice(form.value); ElMessage.success('成功'); dialogVisible.value = false; load() } catch(e) {} }
async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteNotice(row.id); ElMessage.success('删除成功'); load() } catch(e) {} }
onMounted(load)
</script>
