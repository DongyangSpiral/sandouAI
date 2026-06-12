<template>
  <div class="team-container">
    <div class="top-nav glass-container">
      <div class="nav-left">
        <h2>团队协作空间</h2>
        <el-button type="primary" link @click="$router.push('/dashboard')">返回工作台</el-button>
        <el-divider direction="vertical" />
        <el-button type="primary" link @click="$router.push('/dfs')">个人文件</el-button>
      </div>
      <div class="nav-right">
        <el-button v-if="isAdmin" type="primary" @click="createDialogVisible = true">创建新团队</el-button>
      </div>
    </div>

    <!-- Invites Section -->
    <div v-if="invites.length > 0" class="invites-section glass-container" style="margin-bottom: 20px; padding: 20px;">
      <h3 style="margin-top: 0; color: #E6A23C;"><el-icon><Bell /></el-icon> 您有新的团队邀请</h3>
      <div v-for="invite in invites" :key="invite.id" class="invite-card">
        <span><strong>{{ invite.teamName }}</strong> 邀请您加入</span>
        <div style="float: right;">
          <el-button type="success" size="small" @click="handleAccept(invite.id)">同意</el-button>
          <el-button type="danger" size="small" plain @click="handleReject(invite.id)">拒绝</el-button>
        </div>
      </div>
    </div>

    <div class="team-grid">
      <div 
        v-for="team in teamList" 
        :key="team.id" 
        class="team-card glass-card"
        @click="$router.push(`/team/detail/${team.id}`)"
      >
        <div class="team-header">
          <h3>{{ team.name }}</h3>
          <el-tag size="small" :type="team.ownerId === currentUserId ? 'success' : 'info'">
            {{ team.ownerId === currentUserId ? '我创建的' : '已加入' }}
          </el-tag>
        </div>
        <div class="team-body">
          <p>成员上限: {{ team.maxMember }} 人</p>
          <p class="desc">{{ team.description || '暂无描述' }}</p>
        </div>
      </div>
    </div>

    <!-- Create Team Dialog -->
    <el-dialog v-model="createDialogVisible" title="创建新团队" width="400px" custom-class="glass-container">
      <el-form ref="teamFormRef" :model="teamForm" :rules="teamRules" label-width="80px">
        <el-form-item label="团队名称" prop="name">
          <el-input v-model="teamForm.name" placeholder="请输入团队名称" />
        </el-form-item>
        <el-form-item label="成员上限" prop="maxMember">
          <el-input-number v-model="teamForm.maxMember" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="团队描述" prop="description">
          <el-input v-model="teamForm.description" type="textarea" :rows="3" placeholder="一句话描述你的团队目标..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTeam">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeamList, createTeam, getPendingInvites, acceptInvite, rejectInvite } from '@/api/team'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'

const teamList = ref([])
const invites = ref([])
const createDialogVisible = ref(false)
const currentUserId = ref(null)
const isAdmin = ref(false)
const teamFormRef = ref(null)

const teamForm = ref({
  name: '',
  maxMember: 10,
  description: ''
})

const teamRules = {
  name: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过 200 个字符', trigger: 'blur' }
  ]
}

const fetchTeams = async () => {
  try {
    const res = await getTeamList({ pageNum: 1, pageSize: 50 })
    const payload = res.data.data || []
    teamList.value = Array.isArray(payload) ? payload : (payload.list || payload.records || [])
    
    const inviteRes = await getPendingInvites()
    invites.value = inviteRes.data.data || []
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const handleAccept = async (id) => {
  try {
    await acceptInvite(id)
    ElMessage.success('已加入团队')
    fetchTeams()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (id) => {
  try {
    await rejectInvite(id)
    ElMessage.success('已拒绝')
    fetchTeams()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleCreateTeam = async () => {
  if (!teamFormRef.value) return
  await teamFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createTeam(teamForm.value)
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        teamForm.value.name = ''
        teamForm.value.description = ''
        fetchTeams()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '创建失败')
      }
    }
  })
}

onMounted(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    currentUserId.value = userInfo.id
    isAdmin.value = userInfo.id === 1 // admin userId is 1
  } catch (e) {
    console.error(e)
  }
  fetchTeams()
})
</script>

<style scoped>
.team-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  margin-bottom: 30px;
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

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.team-card {
  padding: 20px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.team-body p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.team-body .desc {
  color: #999;
  font-size: 13px;
  margin-top: 10px;
}

.invite-card {
  padding: 15px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
