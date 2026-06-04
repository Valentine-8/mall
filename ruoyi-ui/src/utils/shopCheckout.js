import { addToCart, listCart, updateCartChecked } from '@/api/app/cart'

/**
 * 立即购买跳转路径（含登录后回跳参数）
 */
export function buildBuyNowCheckoutPath(productId, quantity) {
  const qty = quantity || 1
  return `/shop/checkout?buyNow=1&productId=${productId}&quantity=${qty}`
}

/**
 * 登录后或已登录：加购并解析 cartId，返回结算页 query
 */
export async function prepareBuyNowCartIds(productId, quantity) {
  const pid = Number(productId)
  const qty = Number(quantity) || 1
  await addToCart(pid, qty)
  const res = await listCart()
  const item = (res.data || []).find(i => Number(i.productId) === pid)
  if (!item) {
    throw new Error('加入购物车失败，请重试')
  }
  await updateCartChecked(item.cartId, '1')
  return [item.cartId]
}

export function parseCartIdsFromQuery(cartIds) {
  if (!cartIds) return []
  return String(cartIds).split(',').map(Number).filter(Boolean)
}
