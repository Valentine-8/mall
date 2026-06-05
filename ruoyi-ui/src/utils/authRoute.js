import useUserStore from '@/store/modules/user'

/** 仅 C 端买家使用的角色标识 */
const SHOP_ONLY_ROLES = new Set(['common', 'ROLE_DEFAULT'])

/**
 * 是否可访问后台管理（admin、客服坐席等有后台菜单权限的账号）
 */
export function isBackendUser(store) {
  const s = store || useUserStore()
  const roles = s.roles || []
  const perms = s.permissions || []

  if (roles.includes('admin')) return true
  if (perms.includes('*:*:*')) return true

  const hasBackendPerm = perms.some(p =>
    p && p !== '*:*:*' && (
      p.startsWith('mall:') ||
      p.startsWith('system:') ||
      p.startsWith('monitor:') ||
      p.startsWith('tool:')
    )
  )
  if (hasBackendPerm) return true

  if (!roles.length) return false
  return roles.some(r => !SHOP_ONLY_ROLES.has(r))
}

/** C 端商城登录成功后的跳转（仅限商城路径） */
export function resolveShopLoginPath(redirect) {
  if (redirect && typeof redirect === 'string' && redirect.startsWith('/shop') && !redirect.startsWith('/shop/login')) {
    return redirect
  }
  return '/shop/mine'
}

/** 管理后台登录成功后的跳转（禁止落到商城路径） */
export function resolveAdminLoginPath(redirect) {
  if (redirect && typeof redirect === 'string' && !redirect.startsWith('/shop') && !redirect.startsWith('/login')) {
    return redirect
  }
  return '/'
}

/** @deprecated 请使用 resolveShopLoginPath / resolveAdminLoginPath */
export function resolveLoginPath(redirect) {
  if (isBackendUser()) {
    return resolveAdminLoginPath(redirect)
  }
  return resolveShopLoginPath(redirect)
}
