<template>
  <div class="team-detail-page">
    <section class="team-hero">
      <div>
        <p class="eyebrow">TEAM COLLABORATION SPACE</p>
        <h1>{{ teamInfo.name || '团队空间' }}</h1>
        <p>{{ teamInfo.description || '共享文件、明确权限，让团队协作保持在同一节奏。' }}</p>
        <div class="hero-nav"><el-button text @click="$router.push('/dashboard')"><el-icon><ArrowLeft /></el-icon>返回概览</el-button><span></span><el-button text @click="$router.push('/dfs')"><el-icon><FolderOpened /></el-icon>我的文件</el-button><span></span><el-button text @click="$router.push('/team')"><el-icon><UserFilled /></el-icon>所有团队</el-button></div>
      </div>
      <div class="hero-stats"><div><strong>{{ memberList.length }}</strong><span>成员</span></div><i></i><div><strong>{{ fileList.length }}</strong><span>文件</span></div><i></i><div><strong>{{ teamInfo.maxMember || '—' }}</strong><span>成员上限</span></div></div>
    </section>

    <section class="detail-toolbar glass-container">
      <div><span class="online-dot"></span><strong>团队工作区已连接</strong><small>成员文件会自动归档到该团队</small></div>
      <div class="toolbar-actions"><el-button v-if="isAdmin" plain type="danger" @click="handleDeleteTeam">解散团队</el-button><el-button plain @click="inviteDialogVisible = true"><el-icon><User /></el-icon>邀请成员</el-button><el-upload action="/api/team/file/upload" :data="{ teamId }" :headers="uploadHeaders" :show-file-list="false" :on-success="handleUploadSuccess"><el-button type="primary"><el-icon><UploadFilled /></el-icon>上传团队文件</el-button></el-upload></div>
    </section>

    <section class="detail-grid">
      <article class="files-panel glass-container">
        <div class="section-heading"><div><p class="eyebrow">TEAM FILES</p><h2>文件资源库</h2></div><span>{{ fileList.length }} 个文件</span></div>
        <el-table v-loading="loading" :data="fileList" class="team-file-table" empty-text="团队还没有共享文件"><el-table-column min-width="300" label="文件名称"><template #default="{ row }"><div class="file-name"><span><el-icon><Document /></el-icon></span><div><strong>{{ row.name }}</strong><small>{{ row.extension?.toUpperCase() || '文件' }}</small></div></div></template></el-table-column><el-table-column label="大小" width="110"><template #default="{ row }">{{ formatSize(row.size) }}</template></el-table-column><el-table-column label="创建时间" min-width="150"><template #default="{ row }">{{ formatDate(row.createTime) }}</template></el-table-column><el-table-column width="132" align="right"><template #default="{ row }"><el-dropdown trigger="click" @command="command => handleFileCommand(command, row)"><el-button text circle><el-icon><MoreFilled /></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="preview">预览</el-dropdown-item><el-dropdown-item command="download">下载</el-dropdown-item><el-dropdown-item v-if="isAdmin" divided command="delete">从团队移除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></template></el-table-column></el-table>
      </article>
      <aside class="members-panel glass-container">
        <div class="section-heading"><div><p class="eyebrow">MEMBERS</p><h2>团队成员</h2></div><el-button text @click="inviteDialogVisible = true"><el-icon><Plus /></el-icon></el-button></div>
        <div v-if="memberList.length" class="member-list"><div v-for="member in memberList" :key="member.id || member.userId" class="member-item"><el-avatar :size="35">{{ (member.userName || 'U').slice(0, 1).toUpperCase() }}</el-avatar><div><strong>{{ member.userName || `用户 ${member.userId}` }}</strong><small>{{ getRoleName(member.role) }}</small></div><el-dropdown v-if="isAdmin && member.userId !== currentUserId" trigger="click" @command="command => handleMemberCommand(command, member)"><el-button text circle><el-icon><MoreFilled /></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="remove">移除成员</el-dropdown-item></el-dropdown-menu></template></el-dropdown></div></div><el-empty v-else :image-size="78" description="暂无成员信息" />
        <div class="role-guide"><p>权限说明</p><span><b>创建者</b> 管理团队与成员</span><span><b>成员</b> 参与文件协作</span><span><b>访客</b> 仅查看已授权资料</span></div>
      </aside>
    </section>

    <el-dialog v-model="inviteDialogVisible" title="邀请团队成员" width="420px"><p class="dialog-subtitle">邀请会发送给指定用户，待对方确认后加入团队。</p><el-form :model="inviteForm" label-position="top"><el-form-item label="用户名"><el-input v-model="inviteForm.username" placeholder="请输入要邀请的用户名" /></el-form-item><el-form-item label="初始角色"><el-select v-model="inviteForm.role" style="width:100%"><el-option label="管理员" value="admin" /><el-option label="普通成员" value="member" /><el-option label="访客" value="guest" /></el-select></el-form-item></el-form><template #footer><el-button @click="inviteDialogVisible = false">取消</el-button><el-button type="primary" @click="handleInvite">发送邀请</el-button></template></el-dialog>
    <FilePreviewModal ref="previewModal" @download="handleDownloadFile" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document, FolderOpened, MoreFilled, Plus, UploadFilled, User, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteTeam, getTeamDetail, getTeamFiles, getTeamMembers, inviteMember, removeMember, removeTeamFile } from '@/api/team'
import { downloadFile } from '@/api/dfs'
import FilePreviewModal from '../components/FilePreviewModal.vue'

const route = useRoute(); const router = useRouter(); const teamId = route.params.id
const teamInfo = ref({}); const memberList = ref([]); const fileList = ref([]); const loading = ref(false); const previewModal = ref(null); const inviteDialogVisible = ref(false); const isAdmin = ref(false); const currentUserId = ref(null)
const inviteForm = ref({ teamId, username: '', role: 'member' }); const uploadHeaders = { Authorization: localStorage.getItem('token') }
function listOf(data) { return Array.isArray(data) ? data : data?.records || data?.list || [] }
async function fetchData() { loading.value = true; try { const [team, members, files] = await Promise.all([getTeamDetail(teamId), getTeamMembers({ teamId }), getTeamFiles({ teamId })]); teamInfo.value = team.data.data || {}; memberList.value = listOf(members.data.data); fileList.value = listOf(files.data.data) } catch (e) { ElMessage.error('获取团队信息失败') } finally { loading.value = false } }
function handleUploadSuccess(response) { if (response.code === 200) { ElMessage.success('文件已上传到团队空间'); fetchData() } else ElMessage.error(response.message || '上传失败') }
function handleFileCommand(command, row) { if (command === 'preview') previewModal.value?.open(row); if (command === 'download') handleDownloadFile(row); if (command === 'delete') handleDeleteFile(row) }
function handleMemberCommand(command, member) { if (command === 'remove') handleRemoveMember(member) }
async function handleDownloadFile(row) { try { const { data } = await downloadFile(row.id); const url = URL.createObjectURL(new Blob([data])); const link = document.createElement('a'); link.href = url; link.download = row.name; link.click(); URL.revokeObjectURL(url) } catch (e) { ElMessage.error('下载失败') } }
async function handleDeleteFile(row) { try { await ElMessageBox.confirm(`从团队空间移除“${row.name}”？`, '移除团队文件', { type: 'warning' }); await removeTeamFile(teamId, row.id); ElMessage.success('已从团队移除'); fetchData() } catch (e) {} }
async function handleRemoveMember(member) { try { await ElMessageBox.confirm(`确定移除 ${member.userName || member.userId}？`, '移除成员', { type: 'warning' }); await removeMember(member.id); ElMessage.success('成员已移除'); fetchData() } catch (e) {} }
async function handleInvite() { if (!inviteForm.value.username.trim()) return ElMessage.warning('请输入用户名'); try { await inviteMember(inviteForm.value); ElMessage.success('邀请已发送'); inviteDialogVisible.value = false; inviteForm.value.username = ''; fetchData() } catch (e) { ElMessage.error(e.response?.data?.message || '邀请失败') } }
async function handleDeleteTeam() { try { await ElMessageBox.confirm('解散团队后，成员将无法继续访问团队空间。', '解散团队', { type: 'warning', confirmButtonText: '解散团队' }); await deleteTeam(teamId); ElMessage.success('团队已解散'); router.push('/team') } catch (e) {} }
function getRoleName(role) { return ({ creator: '创建者', admin: '管理员', member: '成员', guest: '访客' })[role] || role || '成员' }
function formatSize(size) { if (!size) return '0 B'; const units = ['B', 'KB', 'MB', 'GB']; const index = Math.min(Math.floor(Math.log(size) / Math.log(1024)), 3); return `${(size / 1024 ** index).toFixed(index ? 1 : 0)} ${units[index]}` }
function formatDate(date) { if (!date) return '刚刚'; return String(date).replace('T', ' ').slice(0, 16) }
onMounted(() => { const user = JSON.parse(localStorage.getItem('userInfo') || '{}'); currentUserId.value = user.id; isAdmin.value = user.id === 1; fetchData() })
</script>

<style scoped>
.team-detail-page { max-width: 1500px; margin: 0 auto; }.team-hero { display:flex; align-items:center; justify-content:space-between; min-height:170px; padding:26px 34px; border-radius:20px; color:#fff; background:linear-gradient(110deg,#073d4a,#0b7a82 58%,#15a6a1); box-shadow:0 16px 30px rgba(14,165,164,.16); }.eyebrow { margin:0 0 7px; color:#a8eeea; font-size:10px; font-weight:800; letter-spacing:.13em; }.team-hero h1 { margin:0; font-size:28px; letter-spacing:-.7px; }.team-hero > div > p:not(.eyebrow) { margin:7px 0 0; color:rgba(255,255,255,.74); font-size:12px; }.hero-nav { display:flex; align-items:center; gap:5px; margin-top:13px; }.hero-nav span { width:1px; height:13px; margin:0 5px; background:rgba(255,255,255,.3); }.hero-nav :deep(.el-button) { height:25px; padding:0 5px; color:#fff; font-size:11px; }.hero-nav :deep(.el-button:hover) { color:#fff; background:rgba(255,255,255,.12); }.hero-stats { display:flex; align-items:center; gap:20px; padding:17px 21px; border:1px solid rgba(255,255,255,.16); border-radius:15px; background:rgba(255,255,255,.1); }.hero-stats div { text-align:center; }.hero-stats strong,.hero-stats span { display:block; }.hero-stats strong { font-family:'DM Mono',monospace; font-size:22px; }.hero-stats span { margin-top:4px; color:#c4f6f2; font-size:10px; }.hero-stats i { width:1px; height:27px; background:rgba(255,255,255,.2); }.detail-toolbar { display:flex; align-items:center; justify-content:space-between; margin:20px 0; padding:15px 19px; background:#fff; }.detail-toolbar > div:first-child { display:flex; align-items:center; gap:8px; color:#536075; font-size:12px; }.detail-toolbar small { margin-left:4px; color:#a1aaba; }.online-dot { width:7px; height:7px; border-radius:50%; background:#18b797; box-shadow:0 0 0 4px #e5faf5; }.toolbar-actions { display:flex; align-items:center; gap:9px; }.detail-grid { display:grid; grid-template-columns:minmax(0,1.65fr) minmax(295px,.75fr); gap:20px; }.files-panel,.members-panel { padding:22px; background:#fff; }.section-heading { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }.section-heading h2 { margin:0; color:#303d51; font-size:16px; }.section-heading .eyebrow { margin-bottom:4px; color:#9aa5b5; }.section-heading > span { color:#97a1b2; font-size:11px; }.team-file-table { width:100%; }.team-file-table :deep(.el-table__inner-wrapper::before) { display:none; }.file-name { display:flex; align-items:center; gap:10px; }.file-name > span { display:grid; width:34px; height:34px; place-items:center; border-radius:9px; color:#0e8e8d; background:#e4f8f6; font-size:17px; }.file-name strong,.file-name small { display:block; }.file-name strong { color:#475268; font-size:12px; }.file-name small { margin-top:3px; color:#a0a9b9; font-size:10px; }.member-list { display:grid; gap:8px; }.member-item { display:flex; align-items:center; gap:9px; padding:8px; border-radius:10px; }.member-item:hover { background:#f7fbfb; }.member-item :deep(.el-avatar) { background:#dff7f4; color:#078b89; font-size:12px; }.member-item div { flex:1; min-width:0; }.member-item strong,.member-item small { display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }.member-item strong { color:#485367; font-size:12px; }.member-item small { margin-top:3px; color:#98a3b4; font-size:10px; }.role-guide { display:grid; gap:7px; margin-top:19px; padding:14px; border-radius:11px; background:#f4fbfa; color:#7d8a9b; font-size:10px; }.role-guide p { margin:0 0 2px; color:#0a7777; font-size:11px; font-weight:800; }.role-guide b { color:#455166; }.dialog-subtitle { margin:-6px 0 17px; color:#8a95a7; font-size:12px; }
</style>
