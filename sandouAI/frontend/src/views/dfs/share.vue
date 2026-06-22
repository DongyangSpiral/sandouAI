<template>
  <div class="share-page">
    <section class="share-hero">
      <div><p class="eyebrow">SHARED CONTENT</p><h1>我的分享</h1><p>集中查看、复制和管理已创建的文件分享链接。</p><div class="hero-nav"><el-button text @click="$router.push('/dashboard')"><el-icon><ArrowLeft /></el-icon>返回概览</el-button><span></span><el-button text @click="$router.push('/dfs')"><el-icon><FolderOpened /></el-icon>我的文件</el-button></div></div>
      <div class="share-count"><el-icon><Share /></el-icon><strong>{{ shares.length }}</strong><span>个有效分享</span></div>
    </section>
    <section class="share-card glass-container"><div class="section-heading"><div><p class="eyebrow">SHARE MANAGEMENT</p><h2>分享链接</h2></div><el-button plain @click="$router.push('/dfs')"><el-icon><Plus /></el-icon>创建新分享</el-button></div><el-table v-loading="loading" :data="shares" empty-text="还没有创建分享"><el-table-column min-width="250" label="分享内容"><template #default="{ row }"><div class="share-item"><span><el-icon><Document /></el-icon></span><div><strong>{{ row.fileName || row.name || `文件 #${row.fileId || row.folderId}` }}</strong><small>分享码：{{ row.code }}</small></div></div></template></el-table-column><el-table-column label="访问设置" min-width="140"><template #default="{ row }"><el-tag size="small" :type="row.password ? 'warning' : 'success'">{{ row.password ? '密码保护' : '无需密码' }}</el-tag><span class="download-text">{{ row.allowDownload ? '允许下载' : '仅预览' }}</span></template></el-table-column><el-table-column label="有效期" min-width="160"><template #default="{ row }">{{ row.expireTime ? formatDate(row.expireTime) : '永久有效' }}</template></el-table-column><el-table-column width="170" align="right"><template #default="{ row }"><el-button text type="primary" @click="copyShare(row)">复制链接</el-button><el-button text type="danger" @click="cancel(row)">取消分享</el-button></template></el-table-column></el-table><el-empty v-if="!loading && !shares.length" :image-size="100" description="还没有分享内容"><el-button type="primary" @click="$router.push('/dfs')">去文件库创建分享</el-button></el-empty></section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ArrowLeft, Document, FolderOpened, Plus, Share } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelShare, getShareList } from '@/api/dfs'

const shares = ref([]); const loading = ref(false)
function listOf(data) { return Array.isArray(data) ? data : data?.records || data?.list || [] }
async function fetchShares() { loading.value = true; try { const { data } = await getShareList({ pageNum: 1, pageSize: 100 }); shares.value = listOf(data.data) } catch (e) { shares.value = [] } finally { loading.value = false } }
async function copyShare(row) { const link = `${location.origin}/share/${row.code}`; try { await navigator.clipboard.writeText(link); ElMessage.success('分享链接已复制') } catch (e) { ElMessage.info(link) } }
async function cancel(row) { try { await ElMessageBox.confirm('取消后，原分享链接将立即失效。', '取消分享', { type: 'warning' }); await cancelShare(row.id); ElMessage.success('分享已取消'); fetchShares() } catch (e) {} }
function formatDate(date) { return String(date).replace('T', ' ').slice(0, 16) }
onMounted(fetchShares)
</script>

<style scoped>
.share-page { max-width:1500px; margin:0 auto; }.share-hero { display:flex; align-items:center; justify-content:space-between; min-height:170px; padding:26px 34px; border-radius:20px; color:#fff; background:linear-gradient(110deg,#5a2777,#7c3bb3 58%,#b15fd1); box-shadow:0 16px 30px rgba(124,59,179,.17); }.eyebrow { margin:0 0 7px; color:#eacbfb; font-size:10px; font-weight:800; letter-spacing:.13em; }.share-hero h1 { margin:0; font-size:28px; letter-spacing:-.7px; }.share-hero > div > p:not(.eyebrow) { margin:7px 0 0; color:#f1ddfb; font-size:12px; }.hero-nav { display:flex; align-items:center; gap:5px; margin-top:13px; }.hero-nav span { width:1px; height:13px; margin:0 5px; background:rgba(255,255,255,.3); }.hero-nav :deep(.el-button) { height:25px; padding:0 5px; color:#fff; font-size:11px; }.hero-nav :deep(.el-button:hover) { color:#fff; background:rgba(255,255,255,.12); }.share-count { display:grid; min-width:155px; gap:3px; padding:17px 21px; border:1px solid rgba(255,255,255,.17); border-radius:15px; background:rgba(255,255,255,.1); text-align:center; }.share-count svg { margin:auto; color:#f4dcff; font-size:23px; }.share-count strong { font-family:'DM Mono',monospace; font-size:25px; }.share-count span { color:#f1dfff; font-size:10px; }.share-card { position:relative; margin-top:20px; padding:22px; background:#fff; }.section-heading { display:flex; align-items:center; justify-content:space-between; margin-bottom:15px; }.section-heading h2 { margin:0; color:#303c50; font-size:17px; }.section-heading .eyebrow { margin-bottom:4px; color:#a0a9ba; }.share-item { display:flex; align-items:center; gap:10px; }.share-item > span { display:grid; width:35px; height:35px; place-items:center; border-radius:9px; color:#843eb9; background:#f7eafa; }.share-item strong,.share-item small { display:block; }.share-item strong { color:#485267; font-size:12px; }.share-item small { margin-top:3px; color:#9da6b6; font-family:'DM Mono',monospace; font-size:10px; }.download-text { display:block; margin-top:6px; color:#9aa4b5; font-size:10px; }.share-card :deep(.el-empty) { position:absolute; top:116px; right:0; left:0; }
</style>
