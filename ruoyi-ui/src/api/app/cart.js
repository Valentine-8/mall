import request from '@/utils/request'
import { tansParams } from '@/utils/ruoyi'

function withQuery(url, params) {
  const query = tansParams(params)
  return query ? `${url}?${query.slice(0, -1)}` : url
}

function putWithQuery(url, params) {
  return request({
    url: withQuery(url, params),
    method: 'put'
  })
}

export function listCart() {
  return request({
    url: '/app/mall/cart/list',
    method: 'get'
  })
}

export function addToCart(productId, quantity, options = {}) {
  const params = { productId, quantity }
  if (options.replace) {
    params.replace = true
  }
  return request({
    url: withQuery('/app/mall/cart/add', params),
    method: 'post'
  })
}

export function updateCartQuantity(cartId, quantity) {
  return putWithQuery('/app/mall/cart/quantity', { cartId, quantity })
}

export function updateCartChecked(cartId, checked) {
  return putWithQuery('/app/mall/cart/checked', { cartId, checked })
}

export function removeCart(cartIds) {
  return request({
    url: '/app/mall/cart/' + cartIds,
    method: 'delete'
  })
}
