import request from '@/utils/request'

export function getMallAuthConfig() {
  return request({ url: '/mall/auth/config', method: 'get' })
}

export function updateMallAuthConfig(data) {
  return request({ url: '/mall/auth/config', method: 'put', data })
}
