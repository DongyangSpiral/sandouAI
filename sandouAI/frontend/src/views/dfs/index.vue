<template>
  <div class="dfs-container">
    <!-- Top Navigation -->
    <div class="top-nav glass-container">
      <div class="nav-left">
        <h2>文件协作平台</h2>
        <el-button type="primary" link @click="$router.push('/dashboard')">返回工作台</el-button>
        <el-divider direction="vertical" />
        <el-button type="primary" link @click="$router.push('/team')">团队空间</el-button>
      </div>
      <div class="nav-right">
        <el-upload
          class="upload-btn"
          action="/api/file/upload"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-button type="primary" :icon="Upload">上传文件</el-button>
        </el-upload>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content glass-container">
      <el-table :data="fileList" style="width: 100%; background: transparent" row-class-name="transparent-row">
        <el-table-column prop="name" label="文件名" min-width="200">
          <template #default="{ row }">
            <el-icon class="file-icon"><Document /></el-icon>
            <span style="margin-left: 8px">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
            <el-button link type="primary" @click="handleShare(row)">分享</el-button>
            <el-button link type="success" @click="openAI(row)">AI分析</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <AIChatModal ref="aiModal" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Upload } from '@element-plus/icons-vue'
import { getFileList, deleteFile, downloadFile } from '@/api/dfs'
import { ElMessage, ElMessageBox } from 'element-plus'
import AIChatModal from '../components/AIChatModal.vue'

const fileList = ref([])
const aiModal = ref(null)

const uploadHeaders = {
  Authorization: localStorage.getItem('token')
}

const fetchFiles = async () => {
  try {
    const res = await getFileList({ pageNum: 1, pageSize: 50 })
    fileList.value = res.data.list || res.data.records || []
  } catch (error) {
    console.error('获取文件列表失败', error)
  }
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchFiles()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleDownload = async (row) => {
  try {
    const res = await downloadFile(row.id)
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', row.name)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该文件吗?', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteFile(row.id)
      ElMessage.success('删除成功')
      fetchFiles()
    } catch (error) {}
  }).catch(() => {})
}

const handleShare = (row) => {
  // Share logic mockup
  ElMessage.success('分享链接已生成并复制到剪贴板')
}

const openAI = (row) => {
  aiModal.value.open(row)
}

const formatSize = (size) => {
  if (!size) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return (size / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i]
}

onMounted(() => {
  fetchFiles()
})
</script>

<style scoped>
.dfs-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  margin-bottom: 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.main-content {
  padding: 20px;
  min-height: 600px;
}

.file-icon {
  color: #409EFF;
  vertical-align: middle;
  font-size: 18px;
}

:deep(.transparent-row) {
  background-color: transparent !important;
}

:deep(.el-table th), :deep(.el-table tr), :deep(.el-table td) {
  background-color: transparent !important;
}
</style>
