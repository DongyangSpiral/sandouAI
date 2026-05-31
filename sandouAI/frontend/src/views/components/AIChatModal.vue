<template>
  <el-dialog v-model="visible" title="AI 文档智能分析" width="60%" custom-class="glass-container" :destroy-on-close="true">
    <div v-if="file" class="ai-container">
      <div class="file-info">
        <h3>当前文件: {{ file.name }}</h3>
        <el-button size="small" type="primary" :loading="summarizing" @click="getSummary">
          生成全文摘要
        </el-button>
      </div>
      
      <div v-if="summary" class="summary-box glass-card">
        <h4>文档摘要:</h4>
        <p>{{ summary }}</p>
      </div>

      <div class="chat-area">
        <div class="messages" ref="msgList">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </div>
        
        <div class="input-area">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            placeholder="向 AI 提问关于本文档的内容..."
            @keyup.enter.prevent="askQuestion"
          />
          <el-button type="primary" :loading="asking" @click="askQuestion">发送</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiSummarize, aiAnalyze } from '@/api/ai'
import { ElMessage } from 'element-plus'

const visible = ref(false)
const file = ref(null)
const summary = ref('')
const summarizing = ref(false)
const asking = ref(false)
const question = ref('')
const messages = ref([])
const msgList = ref(null)

const open = (fileRow) => {
  file.value = fileRow
  summary.value = ''
  messages.value = []
  question.value = ''
  visible.value = true
}

const getSummary = async () => {
  if (!file.value) return
  summarizing.value = true
  try {
    const res = await aiSummarize({ fileId: file.value.id })
    summary.value = res.data
  } catch (error) {
    ElMessage.error('摘要生成失败')
  } finally {
    summarizing.value = false
  }
}

const askQuestion = async () => {
  if (!question.value.trim() || asking.value) return
  
  const q = question.value.trim()
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  asking.value = true
  
  scrollToBottom()
  
  try {
    const res = await aiAnalyze({ fileId: file.value.id, question: q })
    messages.value.push({ role: 'ai', content: res.data })
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，分析出错。请稍后重试。' })
  } finally {
    asking.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgList.value) {
      msgList.value.scrollTop = msgList.value.scrollHeight
    }
  })
}

defineExpose({ open })
</script>

<style scoped>
.ai-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-box {
  padding: 15px;
  margin-bottom: 10px;
}

.summary-box p {
  line-height: 1.6;
  color: #444;
}

.chat-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 350px;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 8px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.4);
}

.messages {
  flex-grow: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message {
  max-width: 80%;
  padding: 10px 15px;
  border-radius: 15px;
  line-height: 1.5;
}

.message.user {
  align-self: flex-end;
  background-color: #409EFF;
  color: white;
  border-bottom-right-radius: 0;
}

.message.ai {
  align-self: flex-start;
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
  border: 1px solid #eee;
  border-bottom-left-radius: 0;
}

.input-area {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

:deep(.el-dialog.glass-container) {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(15px);
}
</style>
