<template>
  <div class="team-container">
    <div class="top-nav glass-container">
      <div class="nav-left">
        <p class="eyebrow">TEAM COLLABORATION SPACE</p>
        <h2>团队空间</h2>
        <p class="hero-description">与成员共享文件、协作推进项目。</p>
        <div class="hero-nav">
          <el-button text @click="$router.push('/dashboard')"><el-icon><ArrowLeft /></el-icon>返回概览</el-button>
          <span></span>
          <el-button text @click="$router.push('/dfs')"><el-icon><FolderOpened /></el-icon>我的文件</el-button>
        </div>
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
import { ArrowLeft, Bell, FolderOpened } from '@element-plus/icons-vue'

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
  max-width: 1500px;
  margin: 0 auto;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 142px;
  padding: 28px 32px;
  margin-bottom: 20px;
  color: #fff;
  background: linear-gradient(110deg, #073d4a, #0b7a82 58%, #15a6a1);
  box-shadow: 0 16px 30px rgba(14, 165, 164, .16);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-left h2 {
  margin: 0;
  font-size: 27px;
  color: #fff;
  letter-spacing: -.5px;
}

.eyebrow { margin: 0 0 7px; color: #a8eeea; font-size: 10px; font-weight: 800; letter-spacing: .13em; }
.hero-description { margin: 7px 0 0; color: rgba(255,255,255,.74); font-size: 12px; }
.hero-nav { display: flex; align-items: center; gap: 5px; margin-top: 13px; }
.hero-nav span { width: 1px; height: 13px; margin: 0 5px; background: rgba(255,255,255,.3); }
.hero-nav :deep(.el-button) { height: 25px; padding: 0 5px; color: #fff; font-size: 11px; }
.hero-nav :deep(.el-button:hover) { color: #fff; background: rgba(255,255,255,.12); }
.top-nav .nav-right :deep(.el-button) { border: 0; background: #fff; color: #087b7d; font-weight: 700; }

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(275px, 1fr));
  gap: 15px;
}

.team-card {
  position: relative;
  min-height: 168px;
  padding: 22px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fff;
  border: 1px solid #e7edf1;
  box-shadow: none;
}

.team-card::before { position: absolute; top: 0; left: 0; width: 5px; height: 100%; border-radius: 14px 0 0 14px; background: #17a6a2; content: ''; }

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-header h3 {
  margin: 0;
  font-size: 16px;
  color: #304254;
}

.team-body p {
  margin: 5px 0;
  font-size: 12px;
  color: #718096;
}

.team-body .desc {
  margin-top: 14px;
  color: #9aa5b4;
  font-size: 11px;
}

.invite-card {
  padding: 15px;
  background: #fffdf5;
  border: 1px solid #ffedba;
  border-radius: 10px;
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
