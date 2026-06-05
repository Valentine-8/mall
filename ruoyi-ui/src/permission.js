import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { isBackendUser, resolveAdminLoginPath, resolveShopLoginPath } from '@/utils/authRoute'

NProgress.configure({ showSpinner: false })

const whiteList = [
  '/login', '/register', '/social/callback',
  '/shop/login',
  '/shop/home', '/shop/product/*',
  '/shop/cart', '/shop/mine', '/shop/orders', '/shop/checkout', '/shop/orders/*', '/shop/profile'
]

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isShopRoute = (path) => path && path.startsWith('/shop')

function applyRouteTitle(to) {
  const settingsStore = useSettingsStore()
  if (isShopRoute(to.path)) {
    settingsStore.setTitle(to.meta?.title || '')
  } else if (to.meta?.title) {
    settingsStore.setTitle(to.meta.title)
  }
}

async function ensureUserInfo() {
  if (useUserStore().roles.length === 0) {
    await useUserStore().getInfo()
  }
}

router.beforeEach(async (to, from) => {
  NProgress.start()
  if (isShopRoute(to.path)) {
    applyRouteTitle(to)
  } else if (getToken()) {
    applyRouteTitle(to)
  }

  if (getToken()) {
    const isLock = useLockStore().isLock

    if (to.path === '/shop/login') {
      NProgress.done()
      try {
        await ensureUserInfo()
      } catch (e) {
        return true
      }
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : undefined
      const path = resolveShopLoginPath(redirect)
      return path.includes('?') ? path : { path }
    }

    if (to.path === '/login') {
      NProgress.done()
      try {
        await ensureUserInfo()
      } catch (e) {
        return true
      }
      if (!isBackendUser()) {
        const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : undefined
        return { path: resolveShopLoginPath(redirect) }
      }
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : undefined
      const path = resolveAdminLoginPath(redirect)
      return path.includes('?') ? path : { path }
    }

    if (useUserStore().roles.length > 0 && !isBackendUser() && !isShopRoute(to.path) && !isWhiteList(to.path)) {
      NProgress.done()
      return { path: '/shop/home' }
    }

    if (isWhiteList(to.path)) {
      return true
    }

    if (isLock && to.path !== '/lock') {
      NProgress.done()
      return { path: '/lock' }
    }
    if (!isLock && to.path === '/lock') {
      NProgress.done()
      return { path: '/' }
    }

    if (usePermissionStore().sidebarRouters.length === 0) {
      isRelogin.show = true
      try {
        if (useUserStore().roles.length === 0) {
          await useUserStore().getInfo()
        }
        isRelogin.show = false
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
          }
        })
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/login' }
      }
    }
    return true
  }

  if (isWhiteList(to.path)) {
    return true
  }

  NProgress.done()
  if (isShopRoute(to.path)) {
    return `/shop/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  return `/login?redirect=${encodeURIComponent(to.fullPath)}`
})

router.afterEach((to) => {
  if (isShopRoute(to.path)) {
    useDynamicTitle()
  }
  NProgress.done()
})
