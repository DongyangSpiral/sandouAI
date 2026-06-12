<template>
  <el-dialog
    v-model="visible"
    :title="`文件预览: ${file?.name}`"
    width="80%"
    custom-class="glass-container preview-modal"
    :destroy-on-close="true"
    @closed="handleClose"
  >
    <div v-loading="loading" class="preview-container">
      <template v-if="previewType === 'image'">
        <img :src="fileUrl" class="preview-image" />
      </template>
      <template v-else-if="previewType === 'pdf'">
        <iframe :src="fileUrl" class="preview-iframe"></iframe>
      </template>
      <template v-else-if="previewType === 'text'">
        <pre class="preview-text">{{ textContent }}</pre>
      </template>
      <template v-else-if="previewType === 'unsupported'">
        <div class="unsupported">
          <el-icon :size="60"><Document /></el-icon>
          <p>该文件格式 ({{ file?.extension }}) 暂不支持在线预览</p>
          <el-button type="primary" @click="$emit('download', file)">立即下载查看</el-button>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { downloadFile } from '@/api/dfs'
import { ElMessage } from 'element-plus'

const visible = ref(false)
const loading = ref(false)
const file = ref(null)
const previewType = ref('')
const fileUrl = ref('')
const textContent = ref('')

const emit = defineEmits(['download'])

const open = async (fileRow) => {
  file.value = fileRow
  visible.value = true
  loading.value = true
  fileUrl.value = ''
  textContent.value = ''
  
  const ext = (fileRow.extension || '').toLowerCase()
  
  if (['.png', '.jpg', '.jpeg', '.gif', '.webp'].includes(ext)) {
    previewType.value = 'image'
  } else if (ext === '.pdf') {
    previewType.value = 'pdf'
  } else if (['.txt', '.md', '.json', '.xml', '.csv', '.java', '.py', '.js', '.vue'].includes(ext)) {
    previewType.value = 'text'
  } else {
    previewType.value = 'unsupported'
    loading.value = false
    return
  }

  try {
    const res = await downloadFile(fileRow.id)
    if (previewType.value === 'text') {
      const text = await res.data.text()
      textContent.value = text
    } else {
      fileUrl.value = window.URL.createObjectURL(new Blob([res.data], { type: fileRow.mimeType }))
    }
  } catch (e) {
    ElMessage.error('获取文件预览失败')
    previewType.value = 'unsupported'
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  if (fileUrl.value) {
    window.URL.revokeObjectURL(fileUrl.value)
    fileUrl.value = ''
  }
}

defineExpose({ open })
</script>

<style scoped>
.preview-container {
  height: 60vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(245, 247, 250, 0.5);
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-text {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 20px;
  margin: 0;
  text-align: left;
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f8f9fa;
  font-family: monospace;
}

.unsupported {
  text-align: center;
  color: #909399;
}

.unsupported p {
  margin: 20px 0;
  font-size: 16px;
}

:deep(.preview-modal) {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
}
</style>
