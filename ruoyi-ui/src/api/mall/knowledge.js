import request from '@/utils/request'

export function listKnowledgeDocs(query) {
  return request({ url: '/mall/knowledge/list', method: 'get', params: query })
}

export function getKnowledgeDoc(docId) {
  return request({ url: '/mall/knowledge/' + docId, method: 'get' })
}

export function uploadKnowledgeDoc(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/mall/knowledge/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function delKnowledgeDoc(docId) {
  return request({ url: '/mall/knowledge/' + docId, method: 'delete' })
}

export function reindexKnowledgeDoc(docId) {
  return request({ url: '/mall/knowledge/reindex/' + docId, method: 'post', timeout: 120000 })
}
