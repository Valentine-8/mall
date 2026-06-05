import { addToCart } from '@/api/app/cart'

/**
 * 立即购买跳转路径（含登录后回跳参数）
 */
export function buildBuyNowCheckoutPath(productId, quantity) {
  const qty = quantity || 1
  return `/shop/checkout?buyNow=1&productId=${productId}&quantity=${qty}`
}

/**
 * 登录后或已登录：加购并返回 cartId（立即购买覆盖数量，不再额外勾选）
 */
export async function prepareBuyNowCartIds(productId, quantity) {
  const pid = Number(productId)
  const qty = Number(quantity) || 1
  const res = await addToCart(pid, qty, { replace: true })
  const cartId = Number(res.data)
  if (!Number.isFinite(cartId) || cartId <= 0) {
    throw new Error('加入购物车失败，请重试')
  }
  return [cartId]
}

export function parseCartIdsFromQuery(cartIds) {
  if (!cartIds) return []
  return String(cartIds).split(',').map(Number).filter(Boolean)
}
