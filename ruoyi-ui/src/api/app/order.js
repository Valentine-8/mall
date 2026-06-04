import request from '@/utils/request'

export function checkoutOrder(data) {
  return request({
    url: '/app/mall/order/checkout',
    method: 'post',
    data: data
  })
}

export function payOrder(orderId) {
  return request({
    url: '/app/mall/order/pay/' + orderId,
    method: 'put'
  })
}

export function listMyOrders(params) {
  return request({
    url: '/app/mall/order/list',
    method: 'get',
    params
  })
}

export function getMyOrder(orderId) {
  return request({
    url: '/app/mall/order/' + orderId,
    method: 'get'
  })
}

export function cancelMyOrder(orderId) {
  return request({
    url: '/app/mall/order/cancel/' + orderId,
    method: 'put'
  })
}

export function confirmReceiveOrder(orderId) {
  return request({
    url: '/app/mall/order/confirm/' + orderId,
    method: 'put'
  })
}
