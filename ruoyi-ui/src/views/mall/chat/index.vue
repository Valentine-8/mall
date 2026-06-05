<template>
  <div class="app-container chat-workbench">
    <div class="workbench-toolbar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户">
          <el-input v-model="queryParams.userName" placeholder="用户账号" clearable style="width: 140px" @keyup.enter="loadSessions" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadSessions">搜索</el-button>
        </el-form-item>
      </el-form>
      <div v-if="myAgent" class="agent-online">
        <span>坐席：{{ myAgent.nickName || myAgent.userName }}</span>
        <el-switch v-model="online" active-text="在线" inactive-text="离线" @change="toggleOnline" />
        <span class="load-tip">接待 {{ myAgent.activeSessions || 0 }}/{{ myAgent.maxSessions || 5 }}</span>
      </div>
    </div>
    <div class="workbench-body">
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: current?.sessionId === s.sessionId }"
          @click="selectSession(s)"
        >
          <div class="session-top">
            <strong>{{ s.userName }}</strong>
            <el-tag size="small" :type="statusTag(s.status)">{{ statusLabel(s.status) }}</el-tag>
          </div>
          <p class="session-preview">{{ s.lastMessage || '暂无消息' }}</p>
          <p class="session-time">{{ parseTime(s.lastMessageTime || s.createTime) }}</p>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="64" />
      </div>
      <div class="chat-main" v-if="current">
        <div class="chat-main-head">
          <span>{{ current.userName }} · {{ statusLabel(current.status) }}</span>
          <el-button v-if="current.status !== '3'" link type="danger" @click="closeSession" v-hasPermi="['mall:chat:close']">关闭会话</el-button>
        </div>
        <div ref="msgRef" class="chat-main-msgs">
          <div
            v-for="m in messages"
            :key="m.messageId"
            class="msg-line"
            :class="msgLineClass(m)"
          >
            <div class="msg-head">
              <span v-if="m.senderType === 'ai'" class="sender-badge badge-ai">
                <el-icon class="badge-icon"><Cpu /></el-icon>
                AI 智能客服
              </span>
              <span v-else-if="m.senderType === 'agent'" class="sender-badge badge-agent">人工客服</span>
              <span v-else-if="m.senderType === 'user'" class="sender-badge badge-user">买家</span>
              <span v-else-if="m.senderType === 'system'" class="sender-badge badge-system">系统</span>
              <span class="msg-time">{{ parseTime(m.createTime) }}</span>
            </div>
            <div class="msg-content-wrap" :class="{ media: isMediaMessage(m) }">
              <ChatMessageBody :message="m" />
            </div>
          </div>
        </div>
        <div class="chat-main-input" v-if="current.status !== '3'">
          <ChatRichInput
            v-model="replyText"
            :sending="replying"
            :uploading="uploading"
            placeholder="输入回复内容"
            :rows="3"
            @send="sendReply"
            @upload="uploadReplyMedia"
          />
        </div>
      </div>
      <div v-else class="chat-empty">请选择左侧会话</div>
    </div>
  </div>
</template>

<script setup name="MallChat">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { Cpu } from '@element-plus/icons-vue'
import {
  closeAdminChatSession,
  getMyChatAgent,
  listAdminChatMessages,
  listChatSessions,
  replyChatMessage,
  setChatAgentOnline,
  uploadAdminChatMedia
} from '@/api/mall/chat'
import { isMediaMessage } from '@/utils/chatMessage'
import ChatMessageBody from '@/components/ChatMessageBody/index.vue'
import ChatRichInput from '@/components/ChatRichInput/index.vue'

const queryParams = ref({ pageNum: 1, pageSize: 50, userName: undefined, status: undefined })
const sessions = ref([])
const current = ref(null)
const messages = ref([])
const replyText = ref('')
const replying = ref(false)
const uploading = ref(false)
const myAgent = ref(null)
const online = ref(false)
const lastId = ref(null)
const msgRef = ref(null)
let pollTimer = null

const statusOptions = [
  { value: '0', label: 'AI接待' },
  { value: '1', label: '等待分配' },
  { value: '2', label: '人工接待' },
  { value: '3', label: '已关闭' }
]

function statusLabel(status) {
  return statusOptions.find(s => s.value === status)?.label || status
}
function statusTag(status) {
  if (status === '1') return 'warning'
  if (status === '2') return 'success'
  if (status === '3') return 'info'
  return ''
}
function msgLineClass(m) {
  const t = m?.senderType
  if (t === 'ai') return 'is-ai'
  if (t === 'agent') return 'is-agent'
  if (t === 'user') return 'is-user'
  if (t === 'system') return 'is-system'
  return ''
}
function loadSessions() {
  listChatSessions(queryParams.value).then(res => {
    sessions.value = res.rows || []
  })
}
function selectSession(s) {
  current.value = s
  lastId.value = null
  loadMessages(true)
  startPoll()
}
function loadMessages(scroll) {
  if (!current.value) return
  listAdminChatMessages(current.value.sessionId, lastId.value).then(res => {
    const rows = res.data || []
    if (rows.length) {
      messages.value = lastId.value ? messages.value.concat(rows) : rows
      lastId.value = rows[rows.length - 1].messageId
      if (scroll) nextTick(() => {
        if (msgRef.value) msgRef.value.scrollTop = msgRef.value.scrollHeight
      })
    } else if (!lastId.value) {
      messages.value = []
    }
  })
}
function sendReply() {
  const text = replyText.value.trim()
  if (!text || !current.value) return
  replying.value = true
  replyChatMessage({ sessionId: current.value.sessionId, content: text, msgType: 'text' }).then(() => {
    replyText.value = ''
    loadMessages(true)
    loadSessions()
  }).finally(() => { replying.value = false })
}
function uploadReplyMedia(file) {
  if (uploading.value || !current.value) return
  uploading.value = true
  uploadAdminChatMedia(file).then(res => {
    return replyChatMessage({
      sessionId: current.value.sessionId,
      content: res.fileName,
      msgType: res.msgType || 'image'
    })
  }).then(() => {
    loadMessages(true)
    loadSessions()
  }).finally(() => { uploading.value = false })
}
function closeSession() {
  closeAdminChatSession(current.value.sessionId).then(() => {
    loadSessions()
    selectSession({ ...current.value, status: '3' })
  })
}
function loadMyAgent() {
  getMyChatAgent().then(res => {
    myAgent.value = res.data
    online.value = myAgent.value?.status === '1'
  }).catch(() => { myAgent.value = null })
}
function toggleOnline(val) {
  setChatAgentOnline(val).then(() => loadMyAgent())
}
function startPoll() {
  stopPoll()
  pollTimer = setInterval(() => {
    loadMessages(true)
    loadSessions()
  }, 3000)
}
function stopPoll() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}
onMounted(() => {
  loadSessions()
  loadMyAgent()
})
onBeforeUnmount(() => stopPoll())
</script>

<style scoped lang="scss">
.chat-workbench { display: flex; flex-direction: column; height: calc(100vh - 120px); }
.workbench-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
.agent-online {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.load-tip { color: #909399; font-size: 12px; }
.workbench-body {
  flex: 1;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 12px;
  min-height: 0;
}
.session-list {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow-y: auto;
  background: #fff;
}
.session-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  &:hover, &.active { background: #f5f9ff; }
}
.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.session-preview {
  margin: 6px 0 0;
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-time { margin: 4px 0 0; font-size: 11px; color: #aaa; }
.chat-main {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: #fff;
}
.chat-main-head {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-main-msgs {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f6f8;
}
.msg-line {
  margin-bottom: 16px;
  max-width: 78%;
  display: flex;
  flex-direction: column;
}
.msg-line.is-user {
  margin-right: auto;
  align-items: flex-start;
}
.msg-line.is-ai,
.msg-line.is-agent {
  margin-left: auto;
  align-items: flex-end;
}
.msg-line.is-system {
  margin-left: auto;
  margin-right: auto;
  max-width: 92%;
  align-items: center;
}
.msg-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.msg-line.is-ai .msg-head,
.msg-line.is-agent .msg-head {
  flex-direction: row-reverse;
}
.msg-line.is-system .msg-head {
  justify-content: center;
}
.sender-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.4;
}
.badge-icon { font-size: 13px; }
.badge-ai {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.35);
}
.badge-agent {
  background: #ecf5ff;
  color: #409eff;
  border: 1px solid #b3d8ff;
}
.badge-user {
  background: #f0f2f5;
  color: #606266;
}
.badge-system {
  background: #fdf6ec;
  color: #e6a23c;
}
.msg-time {
  font-size: 11px;
  color: #aaa;
}
.msg-content-wrap:not(.media) :deep(.chat-text) {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 12px;
  text-align: left;
  line-height: 1.55;
  font-size: 13px;
  word-break: break-word;
}
.msg-line.is-user .msg-content-wrap:not(.media) :deep(.chat-text) {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
}
.msg-line.is-ai .msg-content-wrap:not(.media) :deep(.chat-text) {
  background: linear-gradient(145deg, #f5f0ff 0%, #ede9fe 100%);
  color: #3b0764;
  border: 1px solid #c4b5fd;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 10px rgba(124, 58, 237, 0.1);
}
.msg-line.is-ai .msg-content-wrap:not(.media) :deep(.chat-text--md strong) {
  color: #6d28d9;
  font-weight: 700;
}
.msg-line.is-ai .msg-content-wrap:not(.media) :deep(.chat-text--md code) {
  background: rgba(124, 58, 237, 0.12);
  color: #5b21b6;
}
.msg-line.is-agent .msg-content-wrap:not(.media) :deep(.chat-text) {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-line.is-agent .msg-content-wrap:not(.media) :deep(.chat-text--md code) {
  background: rgba(255, 255, 255, 0.2);
}
.msg-line.is-system .msg-content-wrap:not(.media) :deep(.chat-text) {
  background: #fdf6ec;
  color: #b88230;
  border: 1px dashed #f5dab1;
  font-size: 12px;
}
.chat-main-input {
  padding: 12px;
  border-top: 1px solid #eee;
}
.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
}
</style>
