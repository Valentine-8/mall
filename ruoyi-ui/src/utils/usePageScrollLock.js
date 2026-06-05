let lockedScrollY = 0
let chatPanelLockActive = false

export function isMobileViewport() {
  return typeof window !== 'undefined' && window.innerWidth < 769
}

function applyBodyLockStyles() {
  document.documentElement.style.overflow = 'hidden'
  document.documentElement.style.height = '100%'
  document.body.style.overflow = 'hidden'
  document.body.style.height = '100%'
  document.body.style.overscrollBehavior = 'none'
}

function clearBodyLockStyles() {
  document.documentElement.style.overflow = ''
  document.documentElement.style.height = ''
  document.body.style.overflow = ''
  document.body.style.height = ''
  document.body.style.overscrollBehavior = ''
}

export function lockPageScrollForChat() {
  if (!isMobileViewport()) return
  chatPanelLockActive = true
  lockedScrollY = window.scrollY || document.documentElement.scrollTop || 0
  applyBodyLockStyles()
  window.scrollTo(0, lockedScrollY)
}

export function unlockPageScrollForChat() {
  if (!isMobileViewport()) return
  chatPanelLockActive = false
  const y = lockedScrollY
  lockedScrollY = 0
  clearBodyLockStyles()
  requestAnimationFrame(() => window.scrollTo(0, y))
}

/** 图片预览等全屏层打开时暂时解除锁定，避免缩放错位 */
export function suspendPageScrollLock() {
  if (!isMobileViewport() || !chatPanelLockActive) return
  clearBodyLockStyles()
}

export function resumePageScrollLock() {
  if (!isMobileViewport() || !chatPanelLockActive) return
  applyBodyLockStyles()
  window.scrollTo(0, lockedScrollY)
}

/** Safari 键盘收起后刷新锁定，修复点击假死 */
export function refreshPageScrollLock() {
  if (!chatPanelLockActive || !isMobileViewport()) return
  const y = lockedScrollY
  clearBodyLockStyles()
  requestAnimationFrame(() => {
    lockedScrollY = y
    applyBodyLockStyles()
    window.scrollTo(0, y)
  })
}
