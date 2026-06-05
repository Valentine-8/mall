import { ref } from 'vue'
import { isMobileViewport } from '@/utils/usePageScrollLock'

/** 商城手机端软键盘是否打开（供 Tab 栏、悬浮客服等隐藏） */
export const shopKeyboardOpen = ref(false)

let installed = false
let blurTimer = null
let baseViewportHeight = 0

function syncViewportMetrics() {
  /* metrics reserved for future use */
}

export function resetShopKeyboardState() {
  shopKeyboardOpen.value = false
  baseViewportHeight = window.visualViewport?.height || window.innerHeight
}

function isTextField(el) {
  if (!el || !(el instanceof HTMLElement)) return false
  const tag = el.tagName
  if (tag === 'TEXTAREA') return true
  if (tag === 'INPUT') {
    const t = (el.getAttribute('type') || 'text').toLowerCase()
    return !['checkbox', 'radio', 'button', 'submit', 'reset', 'file', 'hidden', 'image'].includes(t)
  }
  return el.isContentEditable
}

function isInsideChatPanel(el) {
  return !!el?.closest?.('.shop-chat-mobile-layer, .shop-chat-widget.is-open')
}

function syncFromViewport() {
  if (!isMobileViewport()) {
    shopKeyboardOpen.value = false
    return
  }
  const vv = window.visualViewport
  if (!vv) return
  if (!baseViewportHeight) baseViewportHeight = vv.height
  const keyboardLikely = baseViewportHeight - vv.height > 120
  const active = document.activeElement
  if (keyboardLikely && isTextField(active) && !isInsideChatPanel(active)) {
    shopKeyboardOpen.value = true
  } else if (!isTextField(active)) {
    shopKeyboardOpen.value = false
    baseViewportHeight = vv.height
  }
}

function onFocusIn(e) {
  if (!isMobileViewport()) return
  if (isTextField(e.target) && !isInsideChatPanel(e.target)) {
    shopKeyboardOpen.value = true
  }
}

function onFocusOut() {
  if (!isMobileViewport()) return
  clearTimeout(blurTimer)
  blurTimer = setTimeout(syncFromViewport, 120)
}

function onViewportChange() {
  syncFromViewport()
}

export function installShopKeyboardListener() {
  if (installed || typeof window === 'undefined') return
  installed = true
  baseViewportHeight = window.visualViewport?.height || window.innerHeight
  window.addEventListener('focusin', onFocusIn, true)
  window.addEventListener('focusout', onFocusOut, true)
  window.visualViewport?.addEventListener('resize', onViewportChange)
  window.visualViewport?.addEventListener('scroll', onViewportChange)
}

export function uninstallShopKeyboardListener() {
  if (!installed || typeof window === 'undefined') return
  installed = false
  clearTimeout(blurTimer)
  shopKeyboardOpen.value = false
  window.removeEventListener('focusin', onFocusIn, true)
  window.removeEventListener('focusout', onFocusOut, true)
  window.visualViewport?.removeEventListener('resize', onViewportChange)
  window.visualViewport?.removeEventListener('scroll', onViewportChange)
}
