import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const STORAGE_KEY = 'shop_chat_fab_pos'
const DRAG_THRESHOLD = 8

export function useDraggableFab(options = {}) {
  const fabRef = ref(null)
  const x = ref(0)
  const y = ref(0)
  const dragging = ref(false)

  let dragStart = null
  let didMove = false
  let suppressClick = false
  let listening = false

  function fabSize() {
    const el = fabRef.value
    return { w: el?.offsetWidth || 72, h: el?.offsetHeight || 40 }
  }

  function defaultPosition() {
    const { w, h } = fabSize()
    const margin = options.margin ?? 16
    const bottomOffset = typeof options.getBottomOffset === 'function'
      ? options.getBottomOffset()
      : (options.bottomOffset ?? 72)
    return {
      x: window.innerWidth - w - margin,
      y: window.innerHeight - h - bottomOffset
    }
  }

  function clamp(nx, ny) {
    const { w, h } = fabSize()
    const pad = options.pad ?? 8
    return {
      x: Math.min(Math.max(pad, nx), window.innerWidth - w - pad),
      y: Math.min(Math.max(pad, ny), window.innerHeight - h - pad)
    }
  }

  function loadPosition() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const parsed = JSON.parse(raw)
        if (Number.isFinite(parsed.x) && Number.isFinite(parsed.y)) {
          const next = clamp(parsed.x, parsed.y)
          x.value = next.x
          y.value = next.y
          return
        }
      }
    } catch (_) { /* ignore */ }
    const next = defaultPosition()
    x.value = next.x
    y.value = next.y
  }

  function savePosition() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ x: x.value, y: y.value }))
    } catch (_) { /* ignore */ }
  }

  function stopDrag() {
    if (!listening) return
    document.removeEventListener('pointermove', onPointerMove)
    document.removeEventListener('pointerup', onPointerUp)
    document.removeEventListener('pointercancel', onPointerUp)
    listening = false
  }

  function onPointerMove(e) {
    if (!dragStart) return
    const dx = e.clientX - dragStart.mx
    const dy = e.clientY - dragStart.my
    if (!didMove && Math.hypot(dx, dy) < DRAG_THRESHOLD) return
    didMove = true
    dragging.value = true
    const next = clamp(dragStart.x + dx, dragStart.y + dy)
    x.value = next.x
    y.value = next.y
  }

  function onPointerUp() {
    stopDrag()
    if (didMove) {
      suppressClick = true
      savePosition()
    }
    dragging.value = false
    dragStart = null
  }

  function onFabPointerDown(e) {
    if (e.pointerType === 'mouse' && e.button !== 0) return
    fabRef.value?.setPointerCapture?.(e.pointerId)
    dragStart = { mx: e.clientX, my: e.clientY, x: x.value, y: y.value }
    didMove = false
    if (!listening) {
      document.addEventListener('pointermove', onPointerMove)
      document.addEventListener('pointerup', onPointerUp)
      document.addEventListener('pointercancel', onPointerUp)
      listening = true
    }
  }

  function onFabClick(e) {
    if (suppressClick) {
      suppressClick = false
      didMove = false
      e.preventDefault()
      e.stopPropagation()
      return false
    }
    return true
  }

  function onResize() {
    const next = clamp(x.value, y.value)
    x.value = next.x
    y.value = next.y
  }

  onMounted(() => {
    requestAnimationFrame(loadPosition)
    window.addEventListener('resize', onResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
    stopDrag()
  })

  const posStyle = computed(() => ({
    left: `${x.value}px`,
    top: `${y.value}px`,
    right: 'auto',
    bottom: 'auto'
  }))

  const panelStyle = computed(() => {
    const { w } = fabSize()
    const centerX = x.value + w / 2
    if (centerX > window.innerWidth / 2) {
      return { right: '0', left: 'auto' }
    }
    return { left: '0', right: 'auto' }
  })

  return {
    fabRef,
    x,
    y,
    dragging,
    posStyle,
    panelStyle,
    onFabPointerDown,
    onFabClick
  }
}
