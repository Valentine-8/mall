<template>
  <div class="shop-product-gallery">
    <div
      ref="stageRef"
      class="gallery-stage"
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
    <div v-if="urls.length > 1" class="gallery-hint">左右滑动切换，点击放大</div>
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
      <div v-if="viewerOpen" class="shop-image-viewer" @click.self="closeViewer">
        <button type="button" class="viewer-close" @click="closeViewer">&times;</button>
        <div v-if="urls.length > 1" class="viewer-counter">{{ activeIndex + 1 }} / {{ urls.length }}</div>
        <div
          ref="viewerRef"
          class="viewer-stage"
          :class="{ 'is-zoomed': viewerScale > 1.01 }"
          @touchstart.passive="onViewerTouchStart"
          @touchmove="onViewerTouchMove"
          @touchend="onViewerTouchEnd"
          @click="onViewerClick"
        >
          <div
            v-if="viewerScale <= 1.01 && urls.length > 1"
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
            :src="urls[activeIndex]"
            class="viewer-img viewer-img-zoom"
            draggable="false"
            alt=""
            :style="viewerZoomStyle"
          />
        </div>
        <div class="viewer-hint">
          {{ viewerScale > 1.01 ? '双指缩放 · 点击退出全屏' : (urls.length > 1 ? '左右滑切换 · 双指放大 · 点击退出' : '双指放大 · 点击退出') }}
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
const SWIPE_THRESHOLD = 0.22
const TRANSITION = 'transform 0.34s cubic-bezier(0.22, 1, 0.36, 1)'

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

const stageTouch = { x: 0, y: 0, t: 0, moved: false }
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

watch(() => props.urls, () => {
  activeIndex.value = 0
  stageDrag.value = 0
})

function measureSizes() {
  stageWidth.value = stageRef.value?.clientWidth || 0
  viewerWidth.value = viewerRef.value?.clientWidth || window.innerWidth
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

function onStageClick() {
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
}

function openViewer() {
  resetViewerTransform(false)
  viewerDrag.value = 0
  viewerOpen.value = true
  document.body.style.overflow = 'hidden'
  nextTick(measureSizes)
}

function closeViewer() {
  viewerOpen.value = false
  document.body.style.overflow = ''
  resetViewerTransform(false)
  viewerDrag.value = 0
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
    viewerTouch.mode = viewerScale.value > 1.01 ? 'pan' : 'swipe'
  }
}

function onViewerTouchMove(e) {
  if (e.touches.length === 2) {
    e.preventDefault()
    viewerTouch.mode = 'pinch'
    viewerTouch.moved = true
    const dist = touchDistance(e.touches)
    const nextScale = Math.min(4, Math.max(1, viewerTouch.pinchScale * (dist / viewerTouch.pinchStart)))
    viewerScale.value = nextScale
    if (nextScale <= 1.01) {
      viewerPanX.value = 0
      viewerPanY.value = 0
    }
    return
  }
  if (e.touches.length !== 1) return

  const x = e.touches[0].clientX
  const y = e.touches[0].clientY
  const dx = x - viewerTouch.startX
  const dy = y - viewerTouch.startY
  if (Math.abs(dx) > 6 || Math.abs(dy) > 6) viewerTouch.moved = true

  if (viewerTouch.mode === 'pan' && viewerScale.value > 1.01) {
    e.preventDefault()
    viewerPanX.value = viewerTouch.panStartX + dx
    viewerPanY.value = viewerTouch.panStartY + dy
    return
  }

  if (viewerTouch.mode === 'swipe' && viewerScale.value <= 1.01 && props.urls.length > 1) {
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
    if (viewerScale.value < 1.05) resetViewerTransform(true)
    viewerTouch.mode = 'none'
    return
  }

  if (viewerTouch.mode === 'pan') {
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    if (!viewerTouch.moved) handleViewerTap(dx, dy)
    viewerTouch.mode = 'none'
    return
  }

  if (viewerTouch.mode === 'swipe' && viewerScale.value <= 1.01 && props.urls.length > 1) {
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    const dir = resolveSwipe(viewerDrag.value || dx, viewerWidth.value)
    viewerAnimating.value = true
    if (dir !== 0) slideTo(activeIndex.value + dir)
    else {
      viewerDrag.value = 0
      handleViewerTap(dx, dy)
    }
    viewerTouch.mode = 'none'
    return
  }

  if (e.changedTouches.length === 1 && !viewerTouch.moved) {
    const dx = e.changedTouches[0].clientX - viewerTouch.startX
    const dy = e.changedTouches[0].clientY - viewerTouch.startY
    handleViewerTap(dx, dy)
  }
  viewerTouch.mode = 'none'
}

function handleViewerTap(dx, dy) {
  if (viewerTouch.moved && (Math.abs(dx) > 12 || Math.abs(dy) > 12)) return
  viewerTouch.suppressClick = true
  closeViewer()
  window.setTimeout(() => {
    viewerTouch.suppressClick = false
  }, 400)
}

function onViewerClick() {
  if (viewerTouch.suppressClick || viewerTouch.moved) return
  closeViewer()
}

onMounted(() => {
  measureSizes()
  window.addEventListener('resize', measureSizes)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureSizes)
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
  display: flex;
  align-items: center;
  justify-content: center;
}
.gallery-track,
.viewer-track {
  display: flex;
  height: 100%;
  will-change: transform;
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
.gallery-hint {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 6px 0 2px;
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
  &.active { border-color: #ff6b35; }
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
.shop-image-viewer .viewer-stage.is-zoomed {
  display: flex;
  align-items: center;
  justify-content: center;
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
.shop-image-viewer .viewer-hint {
  text-align: center;
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
  padding: 12px 0 calc(16px + env(safe-area-inset-bottom));
}
</style>
