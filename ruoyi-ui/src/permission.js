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

NProgress.configure({ showSpinner: false })

const whiteList = [
  '/login', '/register', '/social/callback',
  '/shop/home', '/shop/product/*',
  '/shop/cart', '/shop/mine', '/shop/orders', '/shop/checkout', '/shop/orders/*', '/shop/profile'
]

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isShopRoute = (path) => path && path.startsWith('/shop')

const isAdminUser = () => {
  const roles = useUserStore().roles || []
  return roles.includes('admin')
}

function applyRouteTitle(to) {
  const settingsStore = useSettingsStore()
  if (isShopRoute(to.path)) {
    settingsStore.setTitle(to.meta?.title || '')
  } else if (to.meta?.title) {
    settingsStore.setTitle(to.meta.title)
  }
}

router.beforeEach(async (to, from) => {
  NProgress.start()
  if (isShopRoute(to.path)) {
    applyRouteTitle(to)
  } else if (getToken()) {
    applyRouteTitle(to)
    const isLock = useLockStore().isLock
    if (to.path === '/login') {
      NProgress.done()
      const redirect = to.query.redirect
      if (redirect && typeof redirect === 'string') {
        return redirect.includes('?') ? redirect : { path: redirect }
      }
      if (useUserStore().roles.length === 0) {
        try {
          await useUserStore().getInfo()
        } catch (e) {
          return true
        }
      }
      return { path: isAdminUser() ? '/' : '/shop/mine' }
    }
    if (useUserStore().roles.length > 0 && !isAdminUser() && !isShopRoute(to.path) && !isWhiteList(to.path)) {
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
    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      try {
        // 拉取user_info信息
        await useUserStore().getInfo()
        isRelogin.show = false
        // 根据roles权限生成可访问的路由
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
          }
        })
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/' }
      }
    }
    return true
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      return true
    }
    NProgress.done()
    return `/login?redirect=${to.fullPath}` // 否则全部重定向到登录页
  }
})

router.afterEach((to) => {
  if (isShopRoute(to.path)) {
    useDynamicTitle()
  }
  NProgress.done()
})
