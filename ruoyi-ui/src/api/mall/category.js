import request from '@/utils/request'

export function listCategory(query) {
  return request({
    url: '/mall/category/list',
    method: 'get',
    params: query
  })
}

export function listCategoryAll(query) {
  return request({
    url: '/mall/category/listAll',
    method: 'get',
    params: query
  })
}

export function getCategory(categoryId) {
  return request({
    url: '/mall/category/' + categoryId,
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/mall/category',
    method: 'post',
    data: data
  })
}

export function updateCategory(data) {
  return request({
    url: '/mall/category',
    method: 'put',
    data: data
  })
}

export function delCategory(categoryId) {
  return request({
    url: '/mall/category/' + categoryId,
    method: 'delete'
  })
}
