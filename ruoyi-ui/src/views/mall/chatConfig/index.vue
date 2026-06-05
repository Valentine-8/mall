<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="AI 配置" name="ai">
        <el-form ref="configRef" :model="configForm" label-width="120px" style="max-width: 720px">
          <el-form-item label="启用 AI">
            <el-switch v-model="aiEnabled" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="API 地址">
            <el-input v-model="configForm.aiApiUrl" placeholder="https://api.openai.com/v1" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="configForm.aiApiKey" type="password" show-password placeholder="留空则不修改" />
          </el-form-item>
          <el-form-item label="模型">
            <el-input v-model="configForm.aiModel" placeholder="gpt-4o-mini" />
          </el-form-item>
          <el-form-item label="系统提示词">
            <el-input v-model="configForm.systemPrompt" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="欢迎语">
            <el-input v-model="configForm.welcomeMessage" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="转人工关键词">
            <el-input v-model="configForm.transferKeywords" placeholder="人工,转人工,客服" />
          </el-form-item>
          <el-divider content-position="left">知识库 RAG</el-divider>
          <el-form-item label="启用知识库">
            <el-switch v-model="kbEnabled" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="Embedding 模型">
            <el-input v-model="configForm.embeddingModel" placeholder="text-embedding-v3（DashScope 兼容模式）" />
          </el-form-item>
          <el-form-item label="检索 Top K">
            <el-input-number v-model="configForm.ragTopK" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="切块大小">
            <el-input-number v-model="configForm.chunkSize" :min="200" :max="2000" :step="50" />
          </el-form-item>
          <el-form-item label="切块重叠">
            <el-input-number v-model="configForm.chunkOverlap" :min="0" :max="500" :step="20" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig" v-hasPermi="['mall:chat:config']">保存配置</el-button>
          </el-form-item>
          <el-alert type="info" show-icon :closable="false" title="未配置 API 时将使用内置规则回复；配置 OpenAI 兼容接口后可启用真实 AI。知识库文档在「商城管理 → 知识库」上传，与客服共用同一 API Key。" />
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="坐席管理" name="agent">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="openAgentDialog" v-hasPermi="['mall:chat:agent']">新增坐席</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="agentLoading" :data="agentList">
          <el-table-column label="坐席名称" prop="nickName" />
          <el-table-column label="系统账号" prop="userName" width="120" />
          <el-table-column label="状态" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">{{ scope.row.status === '1' ? '在线' : '离线' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="接待中" prop="activeSessions" width="80" />
          <el-table-column label="最大接待" prop="maxSessions" width="90" />
          <el-table-column label="优先级" prop="sortOrder" width="80" />
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button link type="primary" @click="editAgent(scope.row)" v-hasPermi="['mall:chat:agent']">编辑</el-button>
              <el-button link type="danger" @click="removeAgent(scope.row)" v-hasPermi="['mall:chat:agent']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-alert class="mt12" type="warning" show-icon :closable="false" title="坐席需在「客服会话」页切换在线后才会参与自动分配；分配策略为接待数最少优先。" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="agentTitle" v-model="agentOpen" width="480px" append-to-body>
      <el-form ref="agentRef" :model="agentForm" :rules="agentRules" label-width="100px">
        <el-form-item label="系统用户" prop="userId">
          <el-select v-model="agentForm.userId" filterable placeholder="选择后台用户" style="width: 100%" :disabled="!!agentForm.agentId">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.userName + ' / ' + (u.nickName || '')" :value="u.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="坐席昵称" prop="nickName">
          <el-input v-model="agentForm.nickName" placeholder="对外显示名称" />
        </el-form-item>
        <el-form-item label="最大接待">
          <el-input-number v-model="agentForm.maxSessions" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="分配优先级">
          <el-input-number v-model="agentForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="agentOpen = false">取消</el-button>
        <el-button type="primary" @click="submitAgent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MallChatConfig">
import { computed, onMounted, ref } from 'vue'
import { listUser } from '@/api/system/user'
import {
  addChatAgent,
  delChatAgent,
  getChatConfig,
  listChatAgents,
  updateChatAgent,
  updateChatConfig
} from '@/api/mall/chat'

const activeTab = ref('ai')
const configForm = ref({})
const aiEnabled = ref('1')
const kbEnabled = ref('1')
const agentList = ref([])
const agentLoading = ref(false)
const agentOpen = ref(false)
const agentForm = ref({})
const agentTitle = ref('')
const userOptions = ref([])
const agentRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  nickName: [{ required: true, message: '请输入坐席昵称', trigger: 'blur' }]
}

function loadConfig() {
  getChatConfig().then(res => {
    configForm.value = { ...res.data }
    aiEnabled.value = res.data?.aiEnabled || '1'
    kbEnabled.value = res.data?.kbEnabled || '1'
    if (configForm.value.aiApiKey === '****') configForm.value.aiApiKey = ''
  })
}
function saveConfig() {
  const data = { ...configForm.value, aiEnabled: aiEnabled.value, kbEnabled: kbEnabled.value }
  if (!data.aiApiKey) delete data.aiApiKey
  updateChatConfig(data).then(() => {
    loadConfig()
  })
}
function loadAgents() {
  agentLoading.value = true
  listChatAgents({ pageNum: 1, pageSize: 100 }).then(res => {
    agentList.value = res.rows || []
    agentLoading.value = false
  }).catch(() => { agentLoading.value = false })
}
function loadUsers() {
  listUser({ pageNum: 1, pageSize: 200, status: '0' }).then(res => {
    userOptions.value = res.rows || []
  })
}
function openAgentDialog() {
  agentForm.value = { maxSessions: 5, sortOrder: 0 }
  agentTitle.value = '新增坐席'
  agentOpen.value = true
}
function editAgent(row) {
  agentForm.value = { ...row }
  agentTitle.value = '编辑坐席'
  agentOpen.value = true
}
function submitAgent() {
  const api = agentForm.value.agentId ? updateChatAgent : addChatAgent
  api(agentForm.value).then(() => {
    agentOpen.value = false
    loadAgents()
  })
}
function removeAgent(row) {
  delChatAgent(row.agentId).then(() => loadAgents())
}
onMounted(() => {
  loadConfig()
  loadAgents()
  loadUsers()
})
</script>

<style scoped>
.mt12 { margin-top: 12px; }
.mb8 { margin-bottom: 8px; }
</style>
