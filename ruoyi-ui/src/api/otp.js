import request from '@/utils/request'

export function getOtpConfig() {
  return request({
    url: '/otp/config',
    headers: { isToken: false },
    method: 'get'
  })
}

export function sendOtpCode(data) {
  return request({
    url: '/otp/send',
    headers: { isToken: false, repeatSubmit: false },
    method: 'post',
    data
  })
}

export function loginByOtp(data) {
  return request({
    url: '/otp/login',
    headers: { isToken: false, repeatSubmit: false },
    method: 'post',
    data
  })
}
