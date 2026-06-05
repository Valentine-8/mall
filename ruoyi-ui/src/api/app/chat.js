import request from '@/utils/request'

export function getChatSession() {
  return request({ url: '/app/mall/chat/session', method: 'get' })
}

export function listChatMessages(sessionId, afterId) {
  return request({
    url: '/app/mall/chat/messages',
    method: 'get',
    params: { sessionId, afterId }
  })
}

export function sendChatMessage(data) {
  return request({ url: '/app/mall/chat/send', method: 'post', data })
}

export function uploadChatMedia(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/app/mall/chat/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function transferChatHuman(sessionId) {
  return request({ url: '/app/mall/chat/transfer', method: 'post', data: { sessionId } })
}

export function closeChatSession(sessionId) {
  return request({ url: '/app/mall/chat/close', method: 'put', data: { sessionId } })
}
