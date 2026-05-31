<template>
  <div class="team-detail-container">
    <div class="top-nav glass-container">
      <div class="nav-left">
        <el-button type="primary" link @click="$router.push('/team')">← 返回团队列表</el-button>
        <el-divider direction="vertical" />
        <h2>{{ teamInfo.name }} <span class="tag">团队空间</span></h2>
      </div>
      <div class="nav-right">
        <el-button type="primary" plain @click="inviteDialogVisible = true">邀请成员</el-button>
        <el-upload
          class="upload-btn"
          action="/api/team/file"
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
              <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
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
            <el-tag size="small" :type="getRoleType(member.role)">{{ getRoleName(member.role) }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- Invite Member Dialog -->
    <el-dialog v-model="inviteDialogVisible" title="邀请新成员" width="400px" custom-class="glass-container">
      <el-form :model="inviteForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="inviteForm.userId" placeholder="请输入要邀请的用户ID" />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeamDetail, getTeamMembers, getTeamFiles, inviteMember } from '@/api/team'
import { downloadFile } from '@/api/dfs'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const teamId = route.params.id

const teamInfo = ref({})
const memberList = ref([])
const fileList = ref([])
const inviteDialogVisible = ref(false)

const inviteForm = ref({
  teamId: teamId,
  userId: '',
  role: 'member'
})

const uploadHeaders = {
  Authorization: localStorage.getItem('token')
}

const fetchData = async () => {
  try {
    const infoRes = await getTeamDetail(teamId)
    if (infoRes.data) teamInfo.value = infoRes.data

    const membersRes = await getTeamMembers({ teamId, pageNum: 1, pageSize: 100 })
    memberList.value = membersRes.data.list || membersRes.data.records || []

    const filesRes = await getTeamFiles({ teamId, pageNum: 1, pageSize: 50 })
    fileList.value = filesRes.data.list || filesRes.data.records || []
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

const handleDownload = async (row) => {
  try {
    const res = await downloadFile(row.id) // Fallback to dfs download, assuming team file id matches
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

const handleInvite = async () => {
  if (!inviteForm.value.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  try {
    await inviteMember(inviteForm.value)
    ElMessage.success('邀请成功')
    inviteDialogVisible.value = false
    inviteForm.value.userId = ''
    fetchData()
  } catch (error) {
    ElMessage.error('邀请失败')
  }
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
