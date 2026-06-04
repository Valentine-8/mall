import defaultSettings from '@/settings'
import useSettingsStore from '@/store/modules/settings'
import router from '@/router'

const shopSiteTitle = import.meta.env.VITE_APP_SHOP_TITLE || '若依商城'

function isShopPath(path) {
  return path && path.startsWith('/shop')
}

/**
 * 动态修改标题（C 端 /shop 使用商城名，管理端使用若依管理系统）
 */
export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  const path = router.currentRoute.value?.path || ''

  if (isShopPath(path)) {
    const page = settingsStore.title
    document.title = page ? `${page} - ${shopSiteTitle}` : shopSiteTitle
    return
  }

  if (settingsStore.dynamicTitle) {
    document.title = settingsStore.title + ' - ' + defaultSettings.title
  } else {
    document.title = defaultSettings.title
  }
}