<template>
  <div class="shop-product-gallery">
    <div
      ref="stageRef"
      class="gallery-stage"
      :class="{ 'is-single': urls.length <= 1 }"
      :style="{ height: stageHeight }"
      @touchstart.passive="onStageTouchStart"
      @touchmove="onStageTouchMove"
      @touchend="onStageTouchEnd"
      @mousedown="onStageMouseDown"
      @click="onStageClick"
    >
      <div
        v-if="urls.length > 1"
        class="gallery-track"
        :style="stageTrackStyle"
      >
        <div v-for="(url, idx) in urls" :key="'slide-' + idx" class="gallery-slide" :style="{ width: stageWidth + 'px' }">
          <img :src="url" class="stage-img" draggable="false" alt="" />
        </div>
      </div>
      <img
        v-else-if="urls.length === 1"
        :src="urls[0]"
        class="stage-img stage-img-single"
        draggable="false"
        alt=""
      />
      <button
        v-if="urls.length > 1"
        type="button"
        class="nav-btn nav-prev shop-only-pc"
        @click.stop="slideTo(activeIndex - 1)"
      >&lsaquo;</button>
      <button
        v-if="urls.length > 1"
        type="button"
        class="nav-btn nav-next shop-only-pc"
        @click.stop="slideTo(activeIndex + 1)"
      >&rsaquo;</button>
      <div v-if="urls.length > 1" class="gallery-counter">{{ activeIndex + 1 }} / {{ urls.length }}</div>
    </div>
    <div v-if="urls.length > 1" class="album-thumbs">
      <button
        v-for="(url, idx) in urls"
        :key="'thumb-' + idx"
        type="button"
        class="thumb-btn"
        :class="{ active: activeIndex === idx }"
        @click="slideTo(idx)"
      >
        <img :src="url" alt="" draggable="false" />
      </button>
    </div>

    <teleport to="body">
      <div v-if="viewerOpen" class="shop-image-viewer" @click.self="closeViewerAndBlockReopen">
        <button type="button" class="viewer-close" @click.stop="closeViewerAndBlockReopen">&times;</button>
        <div v-if="urls.length > 1" class="viewer-counter">{{ activeIndex + 1 }} / {{ urls.length }}</div>
        <div
          ref="viewerRef"
          class="viewer-stage"
          :class="{ 'is-transforming': !isViewerTrackMode }"
          @touchstart.passive="onViewerTouchStart"
          @touchmove="onViewerTouchMove"
          @touchend="onViewerTouchEnd"
          @wheel.prevent="onViewerWheel"
          @mousedown="onViewerMouseDown"
          @click.stop="onViewerClick"
        >
          <div
            v-if="isViewerTrackMode"
            class="viewer-track"
            :style="viewerTrackStyle"
          >
            <div
              v-for="(url, idx) in urls"
              :key="'vslide-' + idx"
              class="viewer-slide"
              :style="{ width: viewerWidth + 'px' }"
            >
              <img :src="url" class="viewer-img" draggable="false" alt="" />
            </div>
          </div>
          <img
            v-else
            ref="zoomImgRef"
            :src="urls[activeIndex]"
            class="viewer-img viewer-img-zoom"
            draggable="false"
            alt=""
            :style="viewerZoomStyle"
            @load="measureZoomImg"
          />
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
const SWIPE_THRESHOLD = 0.22
const TRANSITION = 'transform 0.34s cubic-bezier(0.22, 1, 0.36, 1)'
const VIEWER_MIN_SCALE = 0.3
const VIEWER_MAX_SCALE = 4
const VIEWER_UNIT_SCALE = 1
const VIEWER_SCALE_EPS = 0.01

const props = defineProps({
  urls: {
    type: Array,
    default: () => []
  },
  stageHeight: {
    type: String,
    default: '320px'
  }
})

const stageRef = ref(null)
const viewerRef = ref(null)
const zoomImgRef = ref(null)
const activeIndex = ref(0)
const viewerOpen = ref(false)
const stageWidth = ref(0)
const viewerWidth = ref(0)

const stageDrag = ref(0)
const stageAnimating = ref(false)
const viewerDrag = ref(0)
const viewerAnimating = ref(false)

const viewerScale = ref(1)
const viewerPanX = ref(0)
const viewerPanY = ref(0)
const zoomLayout = ref({ w: 0, h: 0, cw: 0, ch: 0 })

const stageTouch = { x: 0, y: 0, t: 0, moved: false, swiped: false, blockOpenUntil: 0 }
const viewerTouch = {
  mode: 'none',
  startX: 0,
  startY: 0,
  moved: false,
  suppressClick: false,
  pinchStart: 0,
  pinchScale: 1,
  panStartX: 0,
  panStartY: 0
}
const viewerMouse = {
  down: false,
  startX: 0,
  startY: 0,
  panStartX: 0,
  panStartY: 0
}

const stageTrackStyle = computed(() => {
  const w = stageWidth.value || 1
  const offset = -activeIndex.value * w + stageDrag.value
  return {
    width: `${props.urls.length * w}px`,
    transform: `translate3d(${offset}px, 0, 0)`,
    transition: stageAnimating.value ? TRANSITION : 'none'
  }
})

const viewerTrackStyle = computed(() => {
  const w = viewerWidth.value || 1
  const offset = -activeIndex.value * w + viewerDrag.value
  return {
    width: `${props.urls.length * w}px`,
    transform: `translate3d(${offset}px, 0, 0)`,
    transition: viewerAnimating.value ? TRANSITION : 'none'
  }
})

const viewerZoomStyle = computed(() => ({
  transform: `translate3d(${viewerPanX.value}px, ${viewerPanY.value}px, 0) scale(${viewerScale.value})`,
  transition: viewerAnimating.value ? 'transform 0.28s ease-out' : 'none'
}))

const isViewerTrackMode = computed(() =>
  props.urls.length > 1 && isNearUnitScale(viewerScale.value)
)

function isNearUnitScale(scale) {
  return Math.abs(scale - VIEWER_UNIT_SCALE) < VIEWER_SCALE_EPS
}

function clampViewerScale(scale) {
  return Math.min(VIEWER_MAX_SCALE, Math.max(VIEWER_MIN_SCALE, scale))
}

watch(() => props.urls, () => {
  activeIndex.value = 0
  stageDrag.value = 0
  nextTick(measureSizes)
})

function measureSizes() {
  stageWidth.value = stageRef.value?.clientWidth || 0
  viewerWidth.value = viewerRef.value?.clientWidth || window.innerWidth
  measureZoomImg()
}

function measureZoomImg() {
  const img = zoomImgRef.value
  const stage = viewerRef.value
  if (!img?.naturalWidth || !stage) return
  const cw = stage.clientWidth
  const ch = stage.clientHeight
  const ratio = img.naturalWidth / img.naturalHeight
  const containerRatio = cw / ch
  let w
  let h
  if (ratio > containerRatio) {
    w = cw
    h = cw / ratio
  } else {
    h = ch
    w = ch * ratio
  }
  zoomLayout.value = { w, h, cw, ch }
}

function clampViewerPan(x, y, scale) {
  const { w, h, cw, ch } = zoomLayout.value
  if (!w || !cw) return { x: 0, y: 0 }
  const sw = w * scale
  const sh = h * scale
  let minX = 0
  let maxX = 0
  let minY = 0
  let maxY = 0
  if (Math.abs(sw - cw) > 0.5) {
    const edge = Math.abs(sw - cw) / 2
    minX = -edge
    maxX = edge
  }
  if (Math.abs(sh - ch) > 0.5) {
    const edge = Math.abs(sh - ch) / 2
    minY = -edge
    maxY = edge
  }
  return {
    x: Math.min(maxX, Math.max(minX, x)),
    y: Math.min(maxY, Math.max(minY, y))
  }
}

function applyViewerPan(x, y, scale = viewerScale.value) {
  const clamped = clampViewerPan(x, y, scale)
  viewerPanX.value = clamped.x
  viewerPanY.value = clamped.y
}

function clampIndex(idx) {
  return Math.max(0, Math.min(props.urls.length - 1, idx))
}

function slideTo(idx, animate = true) {
  const next = clampIndex(idx)
  if (next === activeIndex.value) {
    stageDrag.value = 0
    viewerDrag.value = 0
    return
  }
  stageAnimating.value = animate
  viewerAnimating.value = animate
  activeIndex.value = next
  stageDrag.value = 0
  viewerDrag.value = 0
}

function rubberBand(offset, atStart, atEnd) {
  if ((atStart && offset > 0) || (atEnd && offset < 0)) {
    return offset * 0.35
  }
  return offset
}

function resolveSwipe(offset, width) {
  const threshold = width * SWIPE_THRESHOLD
  if (offset < -threshold) return 1
  if (offset > threshold) return -1
  return 0
}

function onStageTouchStart(e) {
  if (e.touches.length !== 1 || props.urls.length <= 1) return
  stageAnimating.value = false
  stageTouch.x = e.touches[0].clientX
  stageTouch.y = e.touches[0].clientY
  stageTouch.t = Date.now()
  stageTouch.moved = false
}

function onStageTouchMove(e) {
  if (e.touches.length !== 1 || props.urls.length <= 1) return
  const dx = e.touches[0].clientX - stageTouch.x
  const dy = e.touches[0].clientY - stageTouch.y
  if (Math.abs(dx) > 6 || Math.abs(dy) > 6) stageTouch.moved = true
  if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 8) {
    e.preventDefault()
    stageDrag.value = rubberBand(
      dx,
      activeIndex.value === 0,
      activeIndex.value === props.urls.length - 1
    )
  }
}

function onStageTouchEnd(e) {
  if (props.urls.length <= 1) return
  const dx = e.changedTouches[0].clientX - stageTouch.x
  const dy = e.changedTouches[0].clientY - stageTouch.y
  const dir = resolveSwipe(stageDrag.value || dx, stageWidth.value)
  stageAnimating.value = true
  if (dir !== 0) slideTo(activeIndex.value + dir)
  else stageDrag.value = 0
  stageTouch.swiped = stageTouch.moved && Math.abs(dx) > 20 && Math.abs(dx) > Math.abs(dy)
}

function onStageMouseDown(e) {
  if (props.urls.length <= 1) return
  stageTouch.x = e.clientX
  stageTouch.y = e.clientY
  stageTouch.moved = false
  const onMove = (ev) => {
    const dx = ev.clientX - stageTouch.x
    if (Math.abs(dx) > 6) stageTouch.moved = true
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function onStageClick(e) {
  if (Date.now() < stageTouch.blockOpenUntil) {
    e?.preventDefault?.()
    e?.stopPropagation?.()
    return
  }
  if (stageTouch.swiped || stageTouch.moved) {
    stageTouch.swiped = false
    stageTouch.moved = false
    return
  }
  openViewer()
}

function resetViewerTransform(animate = true) {
  viewerAnimating.value = animate
  viewerScale.value = 1
  viewerPanX.value = 0
  viewerPanY.value = 0
  zoomLayout.value = { w: 0, h: 0, cw: 0, ch: 0 }
}

function openViewer() {
  if (viewerOpen.value) return
  resetViewerTransform(false)
  viewerDrag.value = 0
  viewerOpen.value = true
  document.body.style.overflow = 'hidden'
  nextTick(() => {
    measureSizes()
    if (zoomImgRef.value?.complete) measureZoomImg()
  })
}

function closeViewer() {
  if (!viewerOpen.value) return
  viewerOpen.value = false
  document.body.style.overflow = ''
  resetViewerTransform(false)
  viewerDrag.value = 0
}

/** Close fullscreen and ignore the ghost click that would reopen it (mobile Safari). */
function closeViewerAndBlockReopen() {
  stageTouch.blockOpenUntil = Date.now() + 650
  viewerTouch.suppressClick = true
  closeViewer()
  window.setTimeout(() => {
    viewerTouch.suppressClick = false
  }, 650)
}

function applyViewerScale(nextScale) {
  const clamped = clampViewerScale(nextScale)
  viewerScale.value = clamped
  nextTick(() => {
    measureZoomImg()
    if (isNearUnitScale(clamped)) {
      viewerPanX.value = 0
      viewerPanY.value = 0
    } else {
      applyViewerPan(viewerPanX.value, viewerPanY.value, clamped)
    }
  })
}

function onViewerWheel(e) {
  viewerAnimating.value = false
  const step = e.deltaY > 0 ? -0.1 : 0.1
  applyViewerScale(viewerScale.value + step)
}

function onViewerMouseDown(e) {
  if (e.button !== 0 || isViewerTrackMode.value) return
  viewerAnimating.value = false
  viewerMouse.down = true
  viewerMouse.startX = e.clientX
  viewerMouse.startY = e.clientY
  viewerMouse.panStartX = viewerPanX.value
  viewerMouse.panStartY = viewerPanY.value
  viewerTouch.moved = false
  document.addEventListener('mousemove', onViewerMouseMove)
  document.addEventListener('mouseup', onViewerMouseUp)
}

function onViewerMouseMove(e) {
  if (!viewerMouse.down) return
  const dx = e.clientX - viewerMouse.startX
  const dy = e.clientY - viewerMouse.startY
  if (Math.abs(dx) > 6 || Math.abs(dy) > 6) viewerTouch.moved = true
  applyViewerPan(viewerMouse.panStartX + dx, viewerMouse.panStartY + dy)
}

function onViewerMouseUp() {
  if (!viewerMouse.down) return
  viewerMouse.down = false
  document.removeEventListener('mousemove', onViewerMouseMove)
  document.removeEventListener('mouseup', onViewerMouseUp)
  measureZoomImg()
  applyViewerPan(viewerPanX.value, viewerPanY.value)
}

function touchDistance(touches) {
  const [a, b] = touches
  return Math.hypot(a.clientX - b.clientX, a.clientY - b.clientY)
}

function onViewerTouchStart(e) {
  viewerAnimating.value = false
  if (e.touches.length === 2) {
    viewerTouch.mode = 'pinch'
    viewerTouch.pinchStart = touchDistance(e.touches)
    viewerTouch.pinchScale = viewerScale.value
    viewerTouch.moved = true
    return
  }
  if (e.touches.length === 1) {
    viewerTouch.startX = e.touches[0].clientX
    viewerTouch.startY = e.touches[0].clientY
    viewerTouch.moved = false
    viewerTouch.panStartX = viewerPanX.value
    viewerTouch.panStartY = viewerPanY.value
    viewerTouch.mode = isNearUnitScale(viewerScale.value) ? 'swipe' : 'pan'
  }
}

function onViewerTouchMove(e) {
  if (e.touches.length === 2) {
    e.preventDefault()
    viewerTouch.mode = 'pinch'
    viewerTouch.moved = true
    const dist = touchDistance(e.touches)
    const nextScale = clampViewerScale(viewerTouch.pinchScale * (dist / viewerTouch.pinchStart))
    applyViewerScale(nextScale)
    return
  }
  if (e.touches.length !== 1) return

  const x = e.touches[0].clientX
  const y = e.touches[0].clientY
  const dx = x - viewerTouch.startX
  const dy = y - viewerTouch.startY
  if (Math.abs(dx) > 6 || Math.abs(dy) > 6) viewerTouch.moved = true

  if (viewerTouch.mode === 'pan' && !isNearUnitScale(viewerScale.value)) {
    e.preventDefault()
    applyViewerPan(viewerTouch.panStartX + dx, viewerTouch.panStartY + dy)
    return
  }

  if (viewerTouch.mode === 'swipe' && isNearUnitScale(viewerScale.value) && props.urls.length > 1) {
    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 8) {
      e.preventDefault()
      viewerDrag.value = rubberBand(
        dx,
        activeIndex.value === 0,
        activeIndex.value === props.urls.length - 1
      )
    }
  }
}

function onViewerTouchEnd(e) {
  if (viewerTouch.mode === 'pinch') {
    measureZoomImg()
    if (isNearUnitScale(viewerScale.value)) {
      viewerPanX.value = 0
      viewerPanY.value = 0
    } else {
      applyViewerPan(viewerPanX.value, viewerPanY.value)
    }
    viewerTouch.mode = 'none'
    return
  }

  if (viewerTouch.mode === 'pan') {
    measureZoomImg()
    applyViewerPan(viewerPanX.value, viewerPanY.value)
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    if (!viewerTouch.moved) handleViewerTap(e, dx, dy)
    viewerTouch.mode = 'none'
    return
  }

  if (viewerTouch.mode === 'swipe' && isNearUnitScale(viewerScale.value) && props.urls.length > 1) {
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    const dir = resolveSwipe(viewerDrag.value || dx, viewerWidth.value)
    viewerAnimating.value = true
    if (dir !== 0) {
      slideTo(activeIndex.value + dir)
    } else {
      viewerDrag.value = 0
      if (!viewerTouch.moved) handleViewerTap(e, dx, dy)
    }
    viewerTouch.mode = 'none'
    return
  }

  if (e.changedTouches.length === 1 && !viewerTouch.moved) {
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    handleViewerTap(e, dx, dy)
  }
  viewerTouch.mode = 'none'
}

function handleViewerTap(e, dx, dy) {
  if (viewerTouch.moved && (Math.abs(dx) > 12 || Math.abs(dy) > 12)) return
  if (e?.cancelable) e.preventDefault()
  closeViewerAndBlockReopen()
}

function onViewerClick() {
  if (viewerTouch.suppressClick || viewerTouch.moved) return
  closeViewerAndBlockReopen()
}

onMounted(() => {
  measureSizes()
  window.addEventListener('resize', measureSizes)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureSizes)
  document.removeEventListener('mousemove', onViewerMouseMove)
  document.removeEventListener('mouseup', onViewerMouseUp)
  document.body.style.overflow = ''
})

defineExpose({ slideTo, activeIndex })
</script>

<style scoped lang="scss">
.shop-product-gallery {
  background: #f7f7f7;
  border-radius: 12px;
  overflow: hidden;
}
.gallery-stage {
  position: relative;
  background: #f7f7f7;
  overflow: hidden;
  touch-action: pan-y;
  user-select: none;
  -webkit-user-select: none;
}
.gallery-stage.is-single {
  display: flex;
  align-items: center;
  justify-content: center;
}
.gallery-track,
.viewer-track {
  display: flex;
  height: 100%;
  will-change: transform;
  flex-shrink: 0;
}
.gallery-slide,
.viewer-slide {
  flex-shrink: 0;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stage-img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  pointer-events: none;
}
.stage-img-single {
  display: block;
  margin: 0 auto;
  max-height: 100%;
}
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  z-index: 2;
}
.nav-prev { left: 8px; }
.nav-next { right: 8px; }
.gallery-counter {
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 12px;
  z-index: 2;
}
.album-thumbs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px 12px;
  -webkit-overflow-scrolling: touch;
}
.thumb-btn {
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 0;
  background: #fff;
  cursor: pointer;
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  overflow: hidden;
  &.active { border-color: var(--shop-primary); }
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
    background: #fff;
  }
}
</style>

<style lang="scss">
.shop-image-viewer {
  position: fixed;
  inset: 0;
  z-index: 4000;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  flex-direction: column;
  touch-action: none;
}
.shop-image-viewer .viewer-close {
  position: absolute;
  top: calc(12px + env(safe-area-inset-top));
  right: 12px;
  z-index: 2;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}
.shop-image-viewer .viewer-counter {
  position: absolute;
  top: calc(18px + env(safe-area-inset-top));
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  font-size: 14px;
  z-index: 2;
}
.shop-image-viewer .viewer-stage {
  flex: 1;
  overflow: hidden;
  touch-action: none;
}
.shop-image-viewer .viewer-stage.is-transforming {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
}
.shop-image-viewer .viewer-stage.is-transforming:active {
  cursor: grabbing;
}
.shop-image-viewer .viewer-slide {
  flex-shrink: 0;
}
.shop-image-viewer .viewer-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  pointer-events: none;
  user-select: none;
  -webkit-user-drag: none;
}
.shop-image-viewer .viewer-img-zoom {
  display: block;
  margin: auto;
  max-height: 100%;
  transform-origin: center center;
  will-change: transform;
}
</style>
