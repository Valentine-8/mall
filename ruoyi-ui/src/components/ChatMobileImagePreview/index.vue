<template>
  <teleport to="body">
    <transition name="preview-fade">
      <div
        v-if="modelValue"
        class="chat-mobile-preview"
        @click.self="close"
      >
        <button type="button" class="preview-close" aria-label="关闭" @click="close">×</button>
        <div
          ref="stageRef"
          class="preview-stage"
          @touchstart="onTouchStart"
          @touchmove.prevent="onTouchMove"
          @touchend="onTouchEnd"
          @touchcancel="onTouchEnd"
        >
          <img
            ref="imgRef"
            class="preview-img"
            :src="src"
            :style="imgStyle"
            alt="预览"
            draggable="false"
            @click.stop
          />
        </div>
        <p class="preview-hint">双指缩放 · 拖动查看 · 点击空白关闭</p>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { resumePageScrollLock, suspendPageScrollLock } from '@/utils/usePageScrollLock'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  src: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const stageRef = ref(null)
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)

let pinchStartDist = 0
let pinchStartScale = 1
let panStartX = 0
let panStartY = 0
let panBaseX = 0
let panBaseY = 0
let panning = false

const imgStyle = computed(() => ({
  transform: `translate3d(${translateX.value}px, ${translateY.value}px, 0) scale(${scale.value})`
}))

function resetTransform() {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

function close() {
  emit('update:modelValue', false)
}

function getTouchDistance(touches) {
  const dx = touches[0].clientX - touches[1].clientX
  const dy = touches[0].clientY - touches[1].clientY
  return Math.hypot(dx, dy)
}

function onTouchStart(e) {
  if (e.touches.length === 2) {
    pinchStartDist = getTouchDistance(e.touches)
    pinchStartScale = scale.value
    panning = false
  } else if (e.touches.length === 1 && scale.value > 1) {
    panning = true
    panStartX = e.touches[0].clientX
    panStartY = e.touches[0].clientY
    panBaseX = translateX.value
    panBaseY = translateY.value
  }
}

function onTouchMove(e) {
  if (e.touches.length === 2 && pinchStartDist > 0) {
    e.preventDefault()
    const dist = getTouchDistance(e.touches)
    const next = pinchStartScale * (dist / pinchStartDist)
    scale.value = Math.min(4, Math.max(1, next))
    if (scale.value <= 1) {
      translateX.value = 0
      translateY.value = 0
    }
  } else if (panning && e.touches.length === 1 && scale.value > 1) {
    e.preventDefault()
    translateX.value = panBaseX + (e.touches[0].clientX - panStartX)
    translateY.value = panBaseY + (e.touches[0].clientY - panStartY)
  }
}

function onTouchEnd() {
  pinchStartDist = 0
  panning = false
  if (scale.value < 1) {
    resetTransform()
  }
}

watch(() => props.modelValue, (visible) => {
  if (visible) {
    resetTransform()
    suspendPageScrollLock()
    document.documentElement.style.overflow = 'hidden'
  } else {
    document.documentElement.style.overflow = ''
    resumePageScrollLock()
  }
})
</script>

<style scoped lang="scss">
.chat-mobile-preview {
  position: fixed;
  inset: 0;
  z-index: 5000;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  flex-direction: column;
  touch-action: none;
}
.preview-close {
  position: absolute;
  top: calc(12px + env(safe-area-inset-top));
  right: 12px;
  z-index: 2;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}
.preview-stage {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  touch-action: none;
}
.preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
  will-change: transform;
  transition: transform 0.05s linear;
}
.preview-hint {
  flex-shrink: 0;
  margin: 0;
  padding: 8px 12px calc(12px + env(safe-area-inset-bottom));
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}
.preview-fade-enter-active,
.preview-fade-leave-active {
  transition: opacity 0.2s ease;
}
.preview-fade-enter-from,
.preview-fade-leave-to {
  opacity: 0;
}
</style>
