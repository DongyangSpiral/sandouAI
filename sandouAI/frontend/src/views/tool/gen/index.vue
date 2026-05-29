<template>
  <div class="code-gen">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span class="card-title">数据库表</span></template>
          <div class="table-list">
            <div
              v-for="table in tables"
              :key="table.TABLE_NAME"
              :class="['table-item', { active: selectedTable === table.TABLE_NAME }]"
              @click="selectTable(table)"
            >
              <div class="table-name">{{ table.TABLE_NAME }}</div>
              <div class="table-comment">{{ table.TABLE_COMMENT || '无注释' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" v-if="selectedTable">
          <template #header>
            <span class="card-title">字段列表 - {{ selectedTable }}</span>
          </template>
          <el-table :data="columns" size="small" max-height="500">
            <el-table-column prop="columnName" label="字段名" width="140" />
            <el-table-column prop="dataType" label="类型" width="90" />
            <el-table-column prop="columnComment" label="注释" />
          </el-table>

          <div style="margin-top: 16px">
            <el-input v-model="className" placeholder="类名">
              <template #prepend>类名</template>
            </el-input>
          </div>
          <div style="margin-top: 12px">
            <el-button type="primary" @click="doGenerate" :loading="loading">生成代码</el-button>
          </div>
        </el-card>
        <el-empty v-else description="请选择左侧数据表" />
      </el-col>

      <el-col :span="8" v-if="generatedCode">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">生成结果</span>
            <el-button size="small" type="success" style="float:right" @click="copyCode">复制代码</el-button>
          </template>
          <pre class="code-block">{{ generatedCode }}</pre>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGenTables, getGenColumns, generateCode } from '../../../api/monitor'

const tables = ref([])
const selectedTable = ref('')
const columns = ref([])
const className = ref('')
const generatedCode = ref('')
const loading = ref(false)

async function loadTables() {
  try {
    const res = await getGenTables()
    tables.value = res.data.data || []
  } catch (e) {
    console.error(e)
  }
}

async function selectTable(table) {
  selectedTable.value = table.TABLE_NAME
  className.value = toClassName(table.TABLE_NAME)
  try {
    const res = await getGenColumns(table.TABLE_NAME)
    columns.value = res.data.data || []
  } catch (e) {
    console.error(e)
  }
}

function toClassName(name) {
  const cleaned = name.startsWith('sys_') || name.startsWith('u_') ? name.substring(2) : name
  return cleaned.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('')
}

async function doGenerate() {
  loading.value = true
  try {
    const res = await generateCode({
      tableName: selectedTable.value,
      columns: columns.value
    })
    generatedCode.value = res.data.data.javaCode
    ElMessage.success('代码生成成功')
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}

function copyCode() {
  navigator.clipboard.writeText(generatedCode.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

onMounted(() => { loadTables() })
</script>

<style scoped>
.card-title { font-weight: 600; }
.table-list { max-height: 550px; overflow-y: auto; }
.table-item { padding: 10px 12px; cursor: pointer; border-bottom: 1px solid #ebeef5; }
.table-item:hover { background: #f5f7fa; }
.table-item.active { background: #ecf5ff; border-left: 3px solid #409eff; }
.table-name { font-weight: 600; font-size: 14px; }
.table-comment { font-size: 12px; color: #909399; margin-top: 4px; }
.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
  max-height: 550px;
  overflow: auto;
  white-space: pre;
  margin: 0;
}
</style>
