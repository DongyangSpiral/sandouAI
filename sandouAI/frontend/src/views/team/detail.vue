<template>
  <div class="team-detail-container">
    <div class="top-nav glass-container">
      <div class="nav-left">
        <el-button type="primary" link @click="$router.push('/team')">← 返回团队列表</el-button>
        <el-divider direction="vertical" />
        <h2>{{ teamInfo.name }} <span class="tag">团队空间</span></h2>
      </div>
      <div class="nav-right">
        <el-button v-if="isAdmin" type="danger" plain @click="handleDeleteTeam">解散团队</el-button>
        <el-button type="primary" plain @click="inviteDialogVisible = true">邀请成员</el-button>
        <el-upload
          class="upload-btn"
          action="/api/team/file/upload"
          :data="{ teamId: route.params.id }"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          style="display: inline-block; margin-left: 10px;"
        >
          <el-button type="primary">上传团队文件</el-button>
        </el-upload>
      </div>
    </div>

    <div class="main-layout">
      <!-- Left: File List -->
      <div class="left-panel glass-container">
        <h3 class="panel-title">文件资源库</h3>
        <el-table :data="fileList" style="width: 100%; background: transparent" row-class-name="transparent-row">
          <el-table-column prop="name" label="文件名" min-width="150">
            <template #default="{ row }">
              <el-icon class="file-icon"><Document /></el-icon>
              <span style="margin-left: 8px">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="大小" width="100">
            <template #default="{ row }">
              {{ formatSize(row.size) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
              <el-button link type="primary" size="small" @click="handleDownloadFile(row)">下载</el-button>
              <el-button v-if="isAdmin" link type="danger" size="small" @click="handleDeleteFile(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Right: Members -->
      <div class="right-panel glass-container">
        <h3 class="panel-title">团队成员 ({{ memberList.length }}/{{ teamInfo.maxMember || '-' }})</h3>
        <div class="member-list">
          <div v-for="member in memberList" :key="member.userId" class="member-item">
            <div class="member-info">
              <el-avatar :size="32">{{ member.userName ? member.userName.charAt(0) : 'U' }}</el-avatar>
              <span>{{ member.userName || ('用户' + member.userId) }}</span>
            </div>
            <div style="display: flex; gap: 8px; align-items: center;">
              <el-tag size="small" :type="getRoleType(member.role)">{{ getRoleName(member.role) }}</el-tag>
              <el-button v-if="isAdmin && member.userId !== currentUserId" link type="danger" size="small" @click="handleRemoveMember(member)">移除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Invite Member Dialog -->
    <el-dialog v-model="inviteDialogVisible" title="邀请新成员" width="400px" custom-class="glass-container">
      <el-form :model="inviteForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="inviteForm.username" placeholder="请输入要邀请的用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="inviteForm.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="普通成员" value="member" />
            <el-option label="访客" value="guest" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="inviteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleInvite">发送邀请</el-button>
        </span>
      </template>
    </el-dialog>

    <FilePreviewModal ref="previewModal" @download="handleDownloadFile" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeamDetail, getTeamMembers, getTeamFiles, inviteMember, deleteTeam, removeTeamFile, removeMember } from '@/api/team'
import { downloadFile } from '@/api/dfs'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import FilePreviewModal from '../components/FilePreviewModal.vue'

const route = useRoute()
const router = useRouter()
const teamId = route.params.id

const previewModal = ref(null)
const teamInfo = ref({})
const memberList = ref([])
const fileList = ref([])
const inviteDialogVisible = ref(false)
const isAdmin = ref(false)
const currentUserId = ref(null)

const inviteForm = ref({
  teamId: teamId,
  username: '',
  role: 'member'
})

const uploadHeaders = {
  Authorization: localStorage.getItem('token')
}

const fetchData = async () => {
  try {
    const infoRes = await getTeamDetail(teamId)
    if (infoRes.data && infoRes.data.data) teamInfo.value = infoRes.data.data

    const membersRes = await getTeamMembers({ teamId, pageNum: 1, pageSize: 100 })
    const membersPayload = membersRes.data.data || []
    memberList.value = Array.isArray(membersPayload) ? membersPayload : (membersPayload.list || membersPayload.records || [])

    const filesRes = await getTeamFiles({ teamId, pageNum: 1, pageSize: 50 })
    const filesPayload = filesRes.data.data || []
    fileList.value = Array.isArray(filesPayload) ? filesPayload : (filesPayload.list || filesPayload.records || [])
  } catch (error) {
    ElMessage.error('获取团队详情数据失败')
  }
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchData()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handlePreview = (row) => {
  previewModal.value.open(row)
}

const handleDownloadFile = async (row) => {
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

const handleDeleteFile = (row) => {
  ElMessageBox.confirm('确定要删除这个团队文件吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeTeamFile(teamId, row.id)
      ElMessage.success('文件已删除')
      fetchData()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleRemoveMember = (member) => {
  ElMessageBox.confirm(`确定要移除成员 ${member.userName || member.userId} 吗？`, '警告', {
    confirmButtonText: '确定移除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeMember(member.id)
      ElMessage.success('成员已移除')
      fetchData()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '移除失败')
    }
  }).catch(() => {})
}

const handleInvite = async () => {
  if (!inviteForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    await inviteMember(inviteForm.value)
    ElMessage.success('邀请已发送，等待对方同意')
    inviteDialogVisible.value = false
    inviteForm.value.username = ''
    fetchData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '邀请失败')
  }
}

import { ElMessageBox } from 'element-plus'
const handleDeleteTeam = () => {
  ElMessageBox.confirm('确定要解散并删除这个团队吗？所有数据将无法恢复！', '警告', {
    confirmButtonText: '确定解散',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTeam(teamId)
      ElMessage.success('团队已解散')
      router.push('/team')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '解散失败')
    }
  }).catch(() => {})
}

const getRoleName = (role) => {
  const map = { creator: '创建者', admin: '管理员', member: '成员', guest: '访客' }
  return map[role] || role
}

const getRoleType = (role) => {
  const map = { creator: 'danger', admin: 'warning', member: 'primary', guest: 'info' }
  return map[role] || ''
}

const formatSize = (size) => {
  if (!size) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return (size / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i]
}

onMounted(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    isAdmin.value = userInfo.id === 1
    currentUserId.value = userInfo.id
  } catch (e) { console.error(e) }
  fetchData()
})
</script>

<style scoped>
.team-detail-container {
  padding: 20px;
  max-width: 1400px;
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
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-left .tag {
  font-size: 12px;
  background: #e6f1fc;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 12px;
}

.main-layout {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.left-panel {
  flex: 3;
  padding: 20px;
}

.right-panel {
  flex: 1;
  padding: 20px;
  min-width: 300px;
}

.panel-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding-bottom: 10px;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
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
