<template>
  <div class="login-container">
    <aside class="login-showcase">
      <div class="brand"><span><el-icon><FolderOpened /></el-icon></span><div><strong>Sandou Drive</strong><small>企业文件协作中心</small></div></div>
      <div class="showcase-copy"><p>ONE WORKSPACE</p><h1>让每一份资料<br />都有清晰的归属。</h1><span>文件、团队协作与统一身份管理，集中在一个可靠的工作空间。</span></div>
      <div class="showcase-card"><div><el-icon><Lock /></el-icon><span>安全访问控制</span></div><div><el-icon><Connection /></el-icon><span>团队实时协作</span></div><div><el-icon><MagicStick /></el-icon><span>AI 文档辅助</span></div></div>
      <small class="showcase-foot">SANDOU DRIVE · WORKSPACE PLATFORM</small>
    </aside>
    <section class="login-panel">
    <div class="login-card">
      <p class="panel-kicker">WELCOME BACK</p>
      <h2 class="login-title">UAMS 统一认证管理系统</h2>
      <p class="login-subtitle">登录后继续管理你的文件与协作空间。</p>
      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="管理后台登录" name="admin" />
        <el-tab-pane label="密码登录" name="password" />
        <el-tab-pane label="短信登录" name="sms" />
        <el-tab-pane label="企业登录" name="enterprise" />
      </el-tabs>

      <el-form v-if="loginType === 'admin'" :model="adminForm" :rules="adminRules" ref="adminFormRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="adminForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="adminForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdminLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="loginType === 'password'" :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" size="large">
        <el-form-item prop="phone">
          <el-input v-model="passwordForm.phone" placeholder="手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="passwordForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePasswordLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="loginType === 'sms'" :model="smsForm" :rules="smsRules" ref="smsFormRef" size="large">
        <el-form-item prop="phone">
          <el-input v-model="smsForm.phone" placeholder="手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="smsForm.code" placeholder="验证码" prefix-icon="Message">
            <template #append>
              <el-button :disabled="smsCountdown > 0" @click="handleSendSms">
                {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSmsLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="loginType === 'enterprise'" :model="enterpriseForm" ref="enterpriseFormRef" size="large">
        <el-form-item v-if="enterpriseStep === 1" prop="phone">
          <el-input v-model="enterpriseForm.phone" placeholder="手机号" prefix-icon="Phone" />
          <el-button type="primary" @click="handleQueryCorps" :loading="loading" style="width:100%;margin-top:12px">查询企业</el-button>
        </el-form-item>
        <el-form-item v-if="enterpriseStep === 2">
          <el-select v-model="enterpriseForm.corpId" placeholder="请选择企业" style="width:100%">
            <el-option v-for="c in corpList" :key="c.id" :label="c.corpName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="enterpriseStep === 2" prop="password">
          <el-input v-model="enterpriseForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item v-if="enterpriseStep === 2">
          <el-button type="primary" @click="handleEnterpriseLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="login-footer">Copyright © 2025-2026 DeepSeek. All Rights Reserved.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Connection, FolderOpened, Lock, MagicStick } from '@element-plus/icons-vue'
import { systemLogin } from '../../api/system'
import { passwordLogin, sendSms, smsLogin, getCorps, enterpriseLogin } from '../../api/uas'
import { isDriveEntry } from '../../config/appEntry'
import { setAuth } from '../../utils/auth'

const router = useRouter()
const loginType = ref('admin')
const loading = ref(false)
const smsCountdown = ref(0)

const adminForm = reactive({ username: 'admin', password: '123456' })
const adminRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const adminFormRef = ref(null)

const passwordForm = reactive({ phone: '', password: '' })
const passwordRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const passwordFormRef = ref(null)

const smsForm = reactive({ phone: '', code: '' })
const smsRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const smsFormRef = ref(null)

const enterpriseForm = reactive({ phone: '', corpId: '', password: '' })
const enterpriseStep = ref(1)
const corpList = ref([])
const enterpriseFormRef = ref(null)

async function handleAdminLogin() {
  const valid = await adminFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await systemLogin({ username: adminForm.username, password: adminForm.password })
    const { token, userInfo } = res.data.data
    setAuth(token, userInfo)
    ElMessage.success('登录成功')
    router.push(isDriveEntry ? '/dfs' : '/system/user')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handlePasswordLogin() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await passwordLogin({ phone: passwordForm.phone, password: passwordForm.password })
    const { token, userInfo } = res.data.data
    setAuth(token, userInfo)
    router.push(isDriveEntry ? '/dfs' : '/dashboard')
    ElMessage.success('登录成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleSendSms() {
  if (!smsForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  try {
    await sendSms({ phone: smsForm.phone })
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发送失败')
  }
}

async function handleSmsLogin() {
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await smsLogin({ phone: smsForm.phone, code: smsForm.code })
    const { token, userInfo } = res.data.data
    setAuth(token, userInfo)
    router.push(isDriveEntry ? '/dfs' : '/dashboard')
    ElMessage.success('登录成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleQueryCorps() {
  if (!enterpriseForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  loading.value = true
  try {
    const res = await getCorps({ phone: enterpriseForm.phone })
    corpList.value = res.data.data
    if (corpList.value.length === 0) {
      ElMessage.warning('未找到关联企业')
    } else {
      enterpriseStep.value = 2
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function handleEnterpriseLogin() {
  if (!enterpriseForm.corpId || !enterpriseForm.password) {
    ElMessage.warning('请选择企业并输入密码')
    return
  }
  loading.value = true
  try {
    const res = await enterpriseLogin({ corpId: enterpriseForm.corpId, password: enterpriseForm.password })
    const { token, corpInfo } = res.data.data
    setAuth(token, corpInfo)
    router.push(isDriveEntry ? '/dfs' : '/dashboard')
    ElMessage.success('登录成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { display:grid; grid-template-columns:minmax(520px,1.08fr) minmax(470px,.92fr); min-height:100vh; background:#f7f8fc; }.login-showcase { position:relative; display:flex; flex-direction:column; overflow:hidden; padding:48px clamp(48px,7vw,108px); color:#fff; background:linear-gradient(135deg,#201c64,#4840d7 55%,#7168f0); }.login-showcase::before,.login-showcase::after { position:absolute; border:1px solid rgba(255,255,255,.15); border-radius:50%; content:''; }.login-showcase::before { right:-225px; bottom:-245px; width:560px; height:560px; }.login-showcase::after { right:35px; bottom:-155px; width:320px; height:320px; }.brand { z-index:1; display:flex; align-items:center; gap:11px; }.brand > span { display:grid; width:38px; height:38px; place-items:center; border-radius:11px; background:rgba(255,255,255,.16); font-size:20px; }.brand strong,.brand small { display:block; }.brand strong { font-size:15px; }.brand small { margin-top:2px; color:#c1befb; font-size:10px; }.showcase-copy { z-index:1; margin:auto 0; }.showcase-copy p { margin:0 0 13px; color:#b9b5ff; font-size:10px; font-weight:800; letter-spacing:.16em; }.showcase-copy h1 { margin:0; font-size:clamp(34px,3.5vw,51px); line-height:1.19; letter-spacing:-1.8px; }.showcase-copy > span { display:block; max-width:415px; margin-top:18px; color:#d1cffd; font-size:13px; line-height:1.8; }.showcase-card { z-index:1; display:grid; grid-template-columns:repeat(3,1fr); gap:9px; }.showcase-card div { display:flex; flex-direction:column; gap:9px; min-height:88px; padding:14px; border:1px solid rgba(255,255,255,.11); border-radius:12px; background:rgba(255,255,255,.09); }.showcase-card svg { color:#b9f5e7; font-size:19px; }.showcase-card span { color:#e7e6ff; font-size:11px; }.showcase-foot { z-index:1; margin-top:28px; color:#aaa6e8; font-family:'DM Mono',monospace; font-size:9px; letter-spacing:.08em; }.login-panel { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:40px; }.login-card { width:min(100%,390px); padding:42px; border:1px solid #ecedf3; border-radius:19px; background:#fff; box-shadow:0 22px 55px rgba(42,47,87,.09); }.panel-kicker { margin:0 0 8px; color:#7168e8; font-size:10px; font-weight:800; letter-spacing:.13em; }.login-title { margin:0; color:#263147; font-size:22px; letter-spacing:-.4px; }.login-subtitle { margin:8px 0 25px; color:#8e98aa; font-size:12px; }.login-tabs { margin-bottom:24px; }.login-btn { width:100%; height:42px; border-radius:9px; font-weight:700; }.login-footer { width:min(100%,390px); margin-top:17px; color:#a6aebe; font-size:10px; text-align:center; }:deep(.el-tabs__item) { color:#929bad; font-size:12px; }:deep(.el-tabs__item.is-active) { color:#574fe0; font-weight:700; }:deep(.el-tabs__active-bar) { background:#574fe0; }:deep(.el-input__wrapper) { min-height:42px; border-radius:9px; background:#fbfcff; }
</style>
