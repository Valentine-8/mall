import request from '@/utils/request'

export function getChatConfig() {
  return request({ url: '/mall/chat/config', method: 'get' })
}

export function updateChatConfig(data) {
  return request({ url: '/mall/chat/config', method: 'put', data })
}

export function listChatAgents(query) {
  return request({ url: '/mall/chat/agent/list', method: 'get', params: query })
}

export function addChatAgent(data) {
  return request({ url: '/mall/chat/agent', method: 'post', data })
}

export function updateChatAgent(data) {
  return request({ url: '/mall/chat/agent', method: 'put', data })
}

export function delChatAgent(agentId) {
  return request({ url: '/mall/chat/agent/' + agentId, method: 'delete' })
}

export function getMyChatAgent() {
  return request({ url: '/mall/chat/agent/me', method: 'get' })
}

export function setChatAgentOnline(online) {
  return request({ url: '/mall/chat/agent/online', method: 'post', params: { online } })
}

export function listChatSessions(query) {
  return request({ url: '/mall/chat/session/list', method: 'get', params: query })
}

export function listAdminChatMessages(sessionId, afterId) {
  return request({
    url: '/mall/chat/messages',
    method: 'get',
    params: { sessionId, afterId }
  })
}

export function replyChatMessage(data) {
  return request({ url: '/mall/chat/reply', method: 'post', data })
}

export function uploadAdminChatMedia(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/mall/chat/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function closeAdminChatSession(sessionId) {
  return request({ url: '/mall/chat/close/' + sessionId, method: 'put' })
}
