<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">UAMS 统一认证管理系统</h2>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { systemLogin } from '../../api/system'
import { passwordLogin, sendSms, smsLogin, getCorps, enterpriseLogin } from '../../api/uas'

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
    localStorage.setItem('token', token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    ElMessage.success('登录成功')
    router.push('/system/user')
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
    ElMessage.success('登录成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
}
.login-title {
  text-align: center;
  color: #303133;
  margin-bottom: 24px;
  font-size: 22px;
}
.login-tabs {
  margin-bottom: 20px;
}
.login-btn {
  width: 100%;
}
.login-footer {
  margin-top: 16px;
  color: rgba(255,255,255,0.7);
  font-size: 13px;
}
</style>
