import { ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'

const SHOP_AUTH_PATHS = ['/shop/cart', '/shop/orders', '/shop/checkout', '/shop/profile']

const MESSAGES = {
  default: '请先登录账号，即可使用购物车、下单与查看订单等功能。',
  cart: '查看购物车需要先登录，登录后可管理已选商品并结算。',
  addCart: '加入购物车需要先登录，登录后可将商品保存到购物车。',
  buyNow: '立即购买需要先登录，登录后将进入订单结算页面。',
  orders: '查看订单需要先登录，登录后可查看支付与物流状态。',
  checkout: '提交订单需要先登录，请登录后继续结算。'
}

/**
 * @param {import('vue-router').Router} router
 * @param {string} [redirect] login redirect path
 * @param {{ title?: string, message?: string, scene?: keyof typeof MESSAGES }} [options]
 */
export function promptShopLogin(router, redirect, options = {}) {
  const scene = options.scene || 'default'
  const message = options.message || MESSAGES[scene] || MESSAGES.default
  const title = options.title || '需要登录'
  const target = redirect || router.currentRoute.value.fullPath

  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '去登录',
    cancelButtonText: '先看看',
    type: 'info',
    distinguishCancelAndClose: true
  }).then(() => {
    router.push({ path: '/login', query: { redirect: target } })
  }).catch(() => {})
}

/**
 * @returns {Promise<boolean>} true if already logged in
 */
export function ensureShopLogin(router, redirect, options) {
  if (getToken()) {
    return Promise.resolve(true)
  }
  return promptShopLogin(router, redirect, options).then(() => false)
}

export function isShopAuthPath(path) {
  if (!path || !path.startsWith('/shop')) return false
  if (path.startsWith('/shop/orders/')) return true
  return SHOP_AUTH_PATHS.includes(path)
}

export function shopNavClick(router, path, navigate) {
  if (!getToken() && isShopAuthPath(path)) {
    const scene = path.includes('cart') ? 'cart' : path.includes('order') ? 'orders' : 'checkout'
    promptShopLogin(router, path, { scene })
    return
  }
  navigate()
}
