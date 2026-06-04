import request from '@/utils/request'

export function getSocialConfig() {
  return request({
    url: '/social/config',
    headers: { isToken: false },
    method: 'get'
  })
}

export function getSocialAuthorize(type, redirect) {
  return request({
    url: '/social/authorize/' + type,
    headers: { isToken: false },
    method: 'get',
    params: { redirect }
  })
}

export function socialMockLogin(type, redirect) {
  return request({
    url: '/social/mock/' + type,
    headers: { isToken: false },
    method: 'post',
    params: { redirect }
  })
}
