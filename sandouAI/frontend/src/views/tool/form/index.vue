<template>
  <div class="page-container">
    <el-alert title="表单构建 - 可视化拖拽表单设计器" type="info" :closable="false" show-icon style="margin-bottom:16px" />
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card><template #header><span style="font-weight:600">组件库</span></template>
          <div v-for="comp in components" :key="comp.type" class="comp-item" draggable="true" @dragstart="onDragStart($event,comp)">
            <el-icon><component :is="comp.icon" /></el-icon>
            <span style="margin-left:8px">{{ comp.label }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span style="font-weight:600">表单设计区</span><el-button size="small" style="float:right" type="success" @click="exportForm">导出代码</el-button></template>
          <div class="form-area" @drop.prevent="onDrop" @dragover.prevent>
            <div v-if="formItems.length===0" style="text-align:center;color:#909399;padding:40px">将左侧组件拖拽到此处</div>
            <div v-for="(item,idx) in formItems" :key="idx" class="form-row">
              <el-input :placeholder="item.label" :type="item.type" style="flex:1" />
              <el-button size="small" type="danger" :icon="Delete" @click="formItems.splice(idx,1)" style="margin-left:8px" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card v-if="generatedHtml">
          <template #header><span style="font-weight:600">生成代码</span><el-button size="small" style="float:right" @click="copyHtml">复制</el-button></template>
          <pre class="code-preview">{{ generatedHtml }}</pre>
        </el-card>
        <el-empty v-else description="拖拽组件后点击导出" />
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const components = [
  { label:'单行输入', type:'text', icon:'Edit' },
  { label:'密码输入', type:'password', icon:'Lock' },
  { label:'数字输入', type:'number', icon:'Odometer' },
  { label:'文本域', type:'textarea', icon:'Document' },
  { label:'日期选择', type:'date', icon:'Calendar' },
  { label:'邮箱输入', type:'email', icon:'Message' }
]
const formItems = ref([])
const generatedHtml = ref('')

let dragComp = null
function onDragStart(e, comp) { dragComp = comp }
function onDrop(e) { if (dragComp) { formItems.value.push({ ...dragComp }); dragComp = null } }

function exportForm() {
  let html = '<el-form :model="form" label-width="100px">\n'
  for (const item of formItems.value) {
    if (item.type === 'textarea') html += `  <el-form-item label="${item.label}"><el-input type="textarea" v-model="form.${item.type}" /></el-form-item>\n`
    else if (item.type === 'date') html += `  <el-form-item label="${item.label}"><el-date-picker v-model="form.${item.type}" type="date" /></el-form-item>\n`
    else html += `  <el-form-item label="${item.label}"><el-input v-model="form.${item.type}" type="${item.type}" /></el-form-item>\n`
  }
  html += '  <el-form-item><el-button type="primary" @click="submit">提交</el-button></el-form-item>\n'
  html += '</el-form>'
  generatedHtml.value = html
  ElMessage.success('代码已生成')
}

function copyHtml() { navigator.clipboard.writeText(generatedHtml.value); ElMessage.success('已复制') }
</script>
<style scoped>
.comp-item { padding: 10px 12px; cursor: grab; border:1px dashed #ccc; margin-bottom:8px; border-radius:4px; display:flex; align-items:center; }
.comp-item:hover { border-color:#409eff; background:#ecf5ff; }
.form-area { min-height:300px; border:2px dashed #dcdfe6; border-radius:8px; padding:12px; }
.form-row { display:flex; align-items:center; margin-bottom:12px; }
.code-preview { background:#1e1e1e;color:#d4d4d4;padding:12px;border-radius:4px;font-size:12px;line-height:1.5;max-height:400px;overflow:auto;white-space:pre;margin:0; }
</style>
