import { onBeforeUnmount, ref, watch } from 'vue'
import { isMobileViewport } from '@/utils/usePageScrollLock'

/** 手机客服：跟随 visualViewport，避免 iOS 键盘顶起后大片空白与点击错位 */
export function useChatKeyboardLayout(openRef) {
  const inputFocused = ref(false)
  const mobileLayerStyle = ref({})
  const mobilePanelStyle = ref({})
  let cleanup = null

  function syncViewport() {
    if (!isMobileViewport() || !openRef.value) {
      mobileLayerStyle.value = {}
      return
    }
    const vv = window.visualViewport
    if (!vv) return
    mobileLayerStyle.value = {
      top: `${vv.offsetTop}px`,
      left: `${vv.offsetLeft}px`,
      width: `${vv.width}px`,
      height: `${vv.height}px`,
      bottom: 'auto',
      right: 'auto'
    }
    const panelH = inputFocused.value
      ? vv.height
      : Math.round(Math.min(vv.height * 0.82, vv.height - 8))
    mobilePanelStyle.value = {
      maxHeight: `${panelH}px`,
      height: `${panelH}px`
    }
  }

  function bindViewportListeners() {
    unbindViewportListeners()
    syncViewport()
    const vv = window.visualViewport
    const handler = () => syncViewport()
    if (vv) {
      vv.addEventListener('resize', handler)
      vv.addEventListener('scroll', handler)
    }
    window.addEventListener('resize', handler)
    cleanup = () => {
      vv?.removeEventListener('resize', handler)
      vv?.removeEventListener('scroll', handler)
      window.removeEventListener('resize', handler)
    }
  }

  function unbindViewportListeners() {
    cleanup?.()
    cleanup = null
  }

  function onInputFocus() {
    inputFocused.value = true
    syncViewport()
    requestAnimationFrame(syncViewport)
    setTimeout(syncViewport, 80)
    setTimeout(syncViewport, 200)
  }

  function onInputBlur() {
    inputFocused.value = false
    setTimeout(syncViewport, 120)
  }

  function resetLayout() {
    inputFocused.value = false
    if (!openRef.value) {
      mobileLayerStyle.value = {}
      mobilePanelStyle.value = {}
      unbindViewportListeners()
    } else {
      syncViewport()
    }
  }

  watch(openRef, (val) => {
    if (val && isMobileViewport()) {
      bindViewportListeners()
      requestAnimationFrame(syncViewport)
    } else {
      resetLayout()
    }
  })

  onBeforeUnmount(() => {
    resetLayout()
  })

  return {
    inputFocused,
    mobileLayerStyle,
    mobilePanelStyle,
    onInputFocus,
    onInputBlur,
    resetLayout,
    syncViewport
  }
}
