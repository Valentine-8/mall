import request from '@/utils/request'

export function listCart() {
  return request({
    url: '/app/mall/cart/list',
    method: 'get'
  })
}

export function addToCart(productId, quantity) {
  return request({
    url: '/app/mall/cart/add',
    method: 'post',
    params: { productId, quantity }
  })
}

export function updateCartQuantity(cartId, quantity) {
  return request({
    url: '/app/mall/cart/quantity',
    method: 'put',
    params: { cartId, quantity }
  })
}

export function updateCartChecked(cartId, checked) {
  return request({
    url: '/app/mall/cart/checked',
    method: 'put',
    params: { cartId, checked }
  })
}

export function removeCart(cartIds) {
  return request({
    url: '/app/mall/cart/' + cartIds,
    method: 'delete'
  })
}
