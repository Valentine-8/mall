import request from '@/utils/request'

export function listProduct(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
  })
}

export function getProduct(productId) {
  return request({
    url: '/mall/product/' + productId,
    method: 'get'
  })
}

export function addProduct(data) {
  return request({
    url: '/mall/product',
    method: 'post',
    data: data
  })
}

export function updateProduct(data) {
  return request({
    url: '/mall/product',
    method: 'put',
    data: data
  })
}

export function delProduct(productId) {
  return request({
    url: '/mall/product/' + productId,
    method: 'delete'
  })
}
