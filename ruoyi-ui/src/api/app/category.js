import request from '@/utils/request'

export function listAppCategory(query) {
  return request({
    url: '/app/mall/category/list',
    method: 'get',
    params: query,
    headers: { isToken: false }
  })
}
