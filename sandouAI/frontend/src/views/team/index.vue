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
        <el-button type="primary" @click="createDialogVisible = true">创建新团队</el-button>
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
      <el-form :model="teamForm" label-width="80px">
        <el-form-item label="团队名称">
          <el-input v-model="teamForm.name" />
        </el-form-item>
        <el-form-item label="成员上限">
          <el-input-number v-model="teamForm.maxMember" :min="1" :max="100" />
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
import { getTeamList, createTeam } from '@/api/team'
import { ElMessage } from 'element-plus'

const teamList = ref([])
const createDialogVisible = ref(false)
const currentUserId = ref(1) // FIXME: should come from user store

const teamForm = ref({
  name: '',
  maxMember: 10
})

const fetchTeams = async () => {
  try {
    const res = await getTeamList({ pageNum: 1, pageSize: 50 })
    teamList.value = res.data.list || res.data.records || []
  } catch (error) {
    ElMessage.error('获取团队列表失败')
  }
}

const handleCreateTeam = async () => {
  if (!teamForm.value.name) {
    ElMessage.warning('请输入团队名称')
    return
  }
  try {
    await createTeam(teamForm.value)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    teamForm.value.name = ''
    fetchTeams()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  // Get user ID from local storage or pinia if possible
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
</style>
