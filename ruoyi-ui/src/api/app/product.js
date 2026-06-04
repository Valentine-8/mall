import request from '@/utils/request'

export function listAppProduct(query) {
  return request({
    url: '/app/mall/product/list',
    method: 'get',
    params: query,
    headers: { isToken: false }
  })
}

export function getAppProduct(productId) {
  return request({
    url: '/app/mall/product/' + productId,
    method: 'get',
    headers: { isToken: false }
  })
}
