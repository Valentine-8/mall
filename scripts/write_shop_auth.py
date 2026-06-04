# -*- coding: utf-8 -*-
import os

path = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'utils', 'shopAuth.js')

content = """
import { ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'

const SHOP_AUTH_PATHS = ['/shop/cart', '/shop/orders', '/shop/checkout']

const MESSAGES = {
  default: '\u8bf7\u5148\u767b\u5f55\u8d26\u53f7\uff0c\u5373\u53ef\u4f7f\u7528\u8d2d\u7269\u8f66\u3001\u4e0b\u5355\u4e0e\u67e5\u770b\u8ba2\u5355\u7b49\u529f\u80fd\u3002',
  cart: '\u67e5\u770b\u8d2d\u7269\u8f66\u9700\u8981\u5148\u767b\u5f55\uff0c\u767b\u5f55\u540e\u53ef\u7ba1\u7406\u5df2\u9009\u5546\u54c1\u5e76\u7ed3\u7b97\u3002',
  addCart: '\u52a0\u5165\u8d2d\u7269\u8f66\u9700\u8981\u5148\u767b\u5f55\uff0c\u767b\u5f55\u540e\u53ef\u5c06\u5546\u54c1\u4fdd\u5b58\u5230\u8d2d\u7269\u8f66\u3002',
  buyNow: '\u7acb\u5373\u8d2d\u4e70\u9700\u8981\u5148\u767b\u5f55\uff0c\u767b\u5f55\u540e\u5c06\u8fdb\u5165\u8ba2\u5355\u7ed3\u7b97\u9875\u9762\u3002',
  orders: '\u67e5\u770b\u8ba2\u5355\u9700\u8981\u5148\u767b\u5f55\uff0c\u767b\u5f55\u540e\u53ef\u67e5\u770b\u652f\u4ed8\u4e0e\u7269\u6d41\u72b6\u6001\u3002',
  checkout: '\u63d0\u4ea4\u8ba2\u5355\u9700\u8981\u5148\u767b\u5f55\uff0c\u8bf7\u767b\u5f55\u540e\u7ee7\u7eed\u7ed3\u7b97\u3002'
}

/**
 * @param {import('vue-router').Router} router
 * @param {string} [redirect] login redirect path
 * @param {{ title?: string, message?: string, scene?: keyof typeof MESSAGES }} [options]
 */
export function promptShopLogin(router, redirect, options = {}) {
  const scene = options.scene || 'default'
  const message = options.message || MESSAGES[scene] || MESSAGES.default
  const title = options.title || '\u9700\u8981\u767b\u5f55'
  const target = redirect || router.currentRoute.value.fullPath

  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '\u53bb\u767b\u5f55',
    cancelButtonText: '\u5148\u770b\u770b',
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
"""

open(path, 'w', encoding='utf-8', newline='\n').write(content.strip() + '\n')
print('shopAuth.js ok')
# verify
t = open(path, encoding='utf-8').read()
assert '\u53bb\u767b\u5f55' in t and '\u9700\u8981\u767b\u5f55' in t
print('verified utf-8')
