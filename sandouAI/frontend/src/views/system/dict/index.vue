<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card><template #header><span>字典类型</span><el-button type="primary" size="small" style="float:right" @click="handleAddType">新增</el-button></template>
          <div v-for="t in typeList" :key="t.id" :class="['dict-type-item', {active: activeType===t.dict_type}]" @click="selectType(t)">
            <div>{{ t.dict_name }}</div><div style="font-size:12px;color:#909399">{{ t.dict_type }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card v-if="activeType">
          <template #header><span>字典数据: {{ activeType }}</span><el-button type="primary" size="small" style="float:right" @click="handleAddData">新增</el-button></template>
          <el-table :data="dataList" stripe border size="small">
            <el-table-column prop="dict_sort" label="排序" width="60" />
            <el-table-column prop="dict_label" label="标签" />
            <el-table-column prop="dict_value" label="值" />
            <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{row.status===1?'启用':'禁用'}}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="160"><template #default="{row}">
              <el-button size="small" type="primary" @click="handleEditData(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteData(row)">删除</el-button>
            </template></el-table-column>
          </el-table>
        </el-card>
        <el-empty v-else description="请选择字典类型" />
      </el-col>
    </el-row>
    <el-dialog :title="dictDialogTitle" v-model="dictDialogVisible" width="450px">
      <el-form :model="dictForm" label-width="80px">
        <el-form-item label="字典名称"><el-input v-model="dictForm.dict_name" /></el-form-item>
        <el-form-item label="字典类型"><el-input v-model="dictForm.dict_type" :disabled="dictIsEdit" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dictForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dictDialogVisible=false">取消</el-button><el-button type="primary" @click="submitDictType">确定</el-button></template>
    </el-dialog>
    <el-dialog :title="dataDialogTitle" v-model="dataDialogVisible" width="450px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="排序"><el-input-number v-model="dataForm.dict_sort" :min="0" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="dataForm.dict_label" /></el-form-item>
        <el-form-item label="值"><el-input v-model="dataForm.dict_value" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dataDialogVisible=false">取消</el-button><el-button type="primary" @click="submitDictData">确定</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDictTypeList, addDictType, updateDictType, deleteDictType, getDictDataList, addDictData, updateDictData, deleteDictData } from '../../../api/systemExtend'
const typeList = ref([])
const activeType = ref('')
const dataList = ref([])
const dictDialogVisible = ref(false); const dictDialogTitle = ref(''); const dictForm = ref({}); const dictIsEdit = ref(false)
const dataDialogVisible = ref(false); const dataDialogTitle = ref(''); const dataForm = ref({}); const dataIsEdit = ref(false)
async function loadTypes() { try { const r = await getDictTypeList(); typeList.value = r.data.data } catch(e) {} }
async function selectType(t) { activeType.value = t.dict_type; try { const r = await getDictDataList(t.dict_type); dataList.value = r.data.data } catch(e) {} }
function handleAddType() { dictForm.value = { dict_name: '', dict_type: '', status: 1 }; dictIsEdit.value = false; dictDialogTitle.value = '新增字典类型'; dictDialogVisible.value = true }
function handleEditType(row) { dictForm.value = { ...row }; dictIsEdit.value = true; dictDialogTitle.value = '编辑字典类型'; dictDialogVisible.value = true }
async function submitDictType() { try { dictIsEdit.value ? await updateDictType(dictForm.value) : await addDictType(dictForm.value); ElMessage.success('成功'); dictDialogVisible.value = false; loadTypes() } catch(e) {} }
async function handleDeleteType(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteDictType(row.id); ElMessage.success('删除成功'); loadTypes() } catch(e) {} }
function handleAddData() { dataForm.value = { dict_sort: 0, dict_label: '', dict_value: '', dict_type: activeType.value, status: 1 }; dataIsEdit.value = false; dataDialogTitle.value = '新增字典数据'; dataDialogVisible.value = true }
function handleEditData(row) { dataForm.value = { ...row }; dataIsEdit.value = true; dataDialogTitle.value = '编辑字典数据'; dataDialogVisible.value = true }
async function submitDictData() { try { dataIsEdit.value ? await updateDictData(dataForm.value) : await addDictData(dataForm.value); ElMessage.success('成功'); dataDialogVisible.value = false; selectType({ dict_type: activeType.value }) } catch(e) {} }
async function handleDeleteData(row) { try { await ElMessageBox.confirm('确认删除?'); await deleteDictData(row.id); ElMessage.success('删除成功'); selectType({ dict_type: activeType.value }) } catch(e) {} }
onMounted(loadTypes)
</script>
<style scoped>
.dict-type-item { padding: 10px 12px; cursor: pointer; border-bottom: 1px solid #ebeef5; }
.dict-type-item:hover { background: #f5f7fa; }
.dict-type-item.active { background: #ecf5ff; border-left: 3px solid #409eff; }
</style>
