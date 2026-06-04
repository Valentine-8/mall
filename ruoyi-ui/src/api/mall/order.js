import request from '@/utils/request'

export function listOrder(query) {
  return request({
    url: '/mall/order/list',
    method: 'get',
    params: query
  })
}

export function getOrder(orderId) {
  return request({
    url: '/mall/order/' + orderId,
    method: 'get'
  })
}

export function shipOrder(orderId) {
  return request({
    url: '/mall/order/ship/' + orderId,
    method: 'put'
  })
}

export function finishOrder(orderId) {
  return request({
    url: '/mall/order/finish/' + orderId,
    method: 'put'
  })
}

export function cancelOrder(orderId) {
  return request({
    url: '/mall/order/cancel/' + orderId,
    method: 'put'
  })
}

export function refundOrder(orderId) {
  return request({
    url: '/mall/order/refund/' + orderId,
    method: 'put'
  })
}

export function delOrder(orderId) {
  return request({
    url: '/mall/order/' + orderId,
    method: 'delete'
  })
}
