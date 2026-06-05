<template>
  <div
    class="shop-chat-widget"
    :class="{ 'is-open': open && !isMobile, 'is-dragging': dragging }"
    :style="widgetStyle"
  >
    <Teleport to="body">
      <div
        v-if="open && isMobile"
        class="shop-chat-mobile-layer"
        :style="mobileLayerStyle"
        @click.self="closePanel"
      >
        <div class="chat-panel-shell" @click.stop>
          <ShopChatPanel
            ref="mobilePanelRef"
            class="mobile-panel"
            :panel-style="mobilePanelStyle"
            :panel-title="panelTitle"
            :status-text="statusText"
            :is-login="isLogin"
            :messages="messages"
            v-model:draft="draft"
            :sending="sending"
            :uploading="uploading"
            :can-transfer="canTransfer"
            :show-close-session="!!session && session.status !== '3'"
            :scroll-token="scrollToken"
            @close="closePanel"
            @login="goLogin"
            @transfer="doTransfer"
            @end-session="doClose"
            @send="sendMsg"
            @upload="uploadMedia"
            @input-focus="handleInputFocus"
            @input-blur="handleInputBlur"
          />
        </div>
      </div>
    </Teleport>

    <template v-if="open && !isMobile">
      <div class="chat-backdrop desktop-backdrop" @click="onBackdropClose" />
      <ShopChatPanel
        ref="desktopPanelRef"
        :panel-title="panelTitle"
        :status-text="statusText"
        :is-login="isLogin"
        :messages="messages"
        v-model:draft="draft"
        :sending="sending"
        :uploading="uploading"
        :can-transfer="canTransfer"
        :show-close-session="!!session && session.status !== '3'"
        :panel-style="panelStyle"
        :scroll-token="scrollToken"
        @close="closePanel"
        @login="goLogin"
        @transfer="doTransfer"
        @end-session="doClose"
        @send="sendMsg"
        @upload="uploadMedia"
      />
    </template>

    <button
      ref="fabRef"
      v-show="showFab"
      type="button"
      class="chat-fab"
      :class="{ active: open, dragging }"
      @pointerdown="onFabPointerDown"
      @click="handleFabClick"
    >
      <el-icon><Service /></el-icon>
      <span v-if="!open" class="fab-label">客服</span>
    </button>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Service } from '@element-plus/icons-vue'
import {
  closeChatSession,
  getChatSession,
  listChatMessages,
  sendChatMessage,
  transferChatHuman,
  uploadChatMedia
} from '@/api/app/chat'
import useUserStore from '@/store/modules/user'
import ShopChatPanel from './ShopChatPanel.vue'
import {
  lockPageScrollForChat,
  unlockPageScrollForChat,
  isMobileViewport
} from '@/utils/usePageScrollLock'
import { shopKeyboardOpen, resetShopKeyboardState } from '@/utils/useMobileKeyboard'
import { useChatKeyboardLayout } from '@/utils/useChatKeyboardLayout'
import { useDraggableFab } from '@/utils/useDraggableFab'

const keyboardOpen = shopKeyboardOpen
const isMobile = ref(isMobileViewport())
const open = ref(false)
const scrollToken = ref(0)

const {
  mobileLayerStyle,
  mobilePanelStyle,
  onInputFocus: chatInputFocus,
  onInputBlur: chatInputBlur,
  resetLayout: resetChatLayout,
  syncViewport: syncChatViewport
} = useChatKeyboardLayout(open)

const showFab = computed(() => {
  if (open.value && isMobile.value) return false
  return !keyboardOpen.value
})

const {
  fabRef,
  dragging,
  posStyle,
  panelStyle,
  onFabPointerDown,
  onFabClick
} = useDraggableFab({
  margin: 16,
  getBottomOffset: () => (window.innerWidth >= 769 ? 24 : 72)
})

const widgetStyle = computed(() => {
  if (open.value && isMobile.value) return {}
  return posStyle.value
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const session = ref(null)
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const uploading = ref(false)
const lastId = ref(null)
const mobilePanelRef = ref(null)
const desktopPanelRef = ref(null)
let pollTimer = null

function bumpScroll() {
  scrollToken.value += 1
}

function onResize() {
  isMobile.value = isMobileViewport()
}

function blurActiveInput() {
  const el = document.activeElement
  if (el && typeof el.blur === 'function') {
    el.blur()
  }
}

function closePanel() {
  blurActiveInput()
  resetChatLayout()
  resetShopKeyboardState()
  open.value = false
  stopPoll()
  unlockPageScrollForChat()
}

async function doTransfer() {
  if (!session.value) return
  await transferChatHuman(session.value.sessionId)
  await loadMessages(true)
}

const isLogin = computed(() => !!userStore.token)
const panelTitle = computed(() => {
  if (!session.value) return '在线客服'
  if (session.value.status === '2' && session.value.agentName) return session.value.agentName
  if (session.value.status === '1') return '等待客服'
  return '智能客服'
})
const statusText = computed(() => {
  const s = session.value?.status
  if (s === '0') return 'AI 为您服务，可回复「转人工」'
  if (s === '1') return '正在分配人工客服…'
  if (s === '2') return '人工客服接待中'
  if (s === '3') return '会话已结束'
  return '有问题随时咨询'
})
const canTransfer = computed(() => session.value && (session.value.status === '0'))

function goLogin() {
  router.push({ path: '/shop/login', query: { redirect: route.fullPath } })
}

function onBackdropClose(e) {
  if (e.target === e.currentTarget) {
    closePanel()
  }
}

async function toggleOpen() {
  if (open.value) {
    closePanel()
    return
  }
  resetChatLayout()
  resetShopKeyboardState()
  open.value = true
  if (!isMobile.value) {
    lockPageScrollForChat()
  } else {
    nextTick(() => syncChatViewport())
  }
  if (isLogin.value) {
    await initSession()
    startPoll()
  }
}

function handleFabClick(e) {
  if (!onFabClick(e)) return
  toggleOpen()
}

function handleInputFocus() {
  chatInputFocus()
  bumpScroll()
}

function handleInputBlur() {
  chatInputBlur()
  resetShopKeyboardState()
  bumpScroll()
  if (open.value && isMobile.value) {
    setTimeout(() => syncChatViewport(), 120)
    setTimeout(() => bumpScroll(), 150)
  }
}

async function initSession() {
  const res = await getChatSession()
  session.value = res.data
  lastId.value = null
  await loadMessages(true)
}

async function loadMessages(scroll) {
  if (!session.value?.sessionId) return
  const res = await listChatMessages(session.value.sessionId, lastId.value)
  const rows = res.data || []
  if (rows.length) {
    messages.value = lastId.value ? messages.value.concat(rows) : rows
    lastId.value = rows[rows.length - 1].messageId
    if (scroll) bumpScroll()
  } else if (!lastId.value) {
    messages.value = []
  }
  const sessRes = await getChatSession()
  session.value = sessRes.data
}

function startPoll() {
  stopPoll()
  pollTimer = setInterval(() => loadMessages(true), 3000)
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function sendMsg() {
  const text = draft.value.trim()
  if (!text || sending.value || !session.value) return
  sending.value = true
  try {
    await sendChatMessage({ sessionId: session.value.sessionId, content: text, msgType: 'text' })
    draft.value = ''
    blurActiveInput()
    resetShopKeyboardState()
    chatInputBlur()
    if (isMobile.value) {
      setTimeout(() => syncChatViewport(), 120)
      setTimeout(() => syncChatViewport(), 300)
    }
    await loadMessages(true)
  } finally {
    sending.value = false
  }
}

async function uploadMedia(file) {
  if (uploading.value || !session.value) return
  uploading.value = true
  try {
    const res = await uploadChatMedia(file)
    await sendChatMessage({
      sessionId: session.value.sessionId,
      content: res.fileName,
      msgType: res.msgType || 'image'
    })
    await loadMessages(true)
  } finally {
    uploading.value = false
  }
}

async function doClose() {
  if (!session.value) return
  await closeChatSession(session.value.sessionId)
  await initSession()
}

watch(open, (val) => {
  if (val && !isMobile.value) {
    lockPageScrollForChat()
  }
  if (!val) {
    resetChatLayout()
    resetShopKeyboardState()
    unlockPageScrollForChat()
  } else if (val && isMobile.value) {
    nextTick(() => syncChatViewport())
  }
})

watch(isLogin, (val) => {
  if (!val) {
    session.value = null
    messages.value = []
    stopPoll()
  } else if (open.value) {
    initSession()
    startPoll()
  }
})

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  stopPoll()
  resetChatLayout()
  resetShopKeyboardState()
  unlockPageScrollForChat()
})
</script>

<style scoped lang="scss">
.shop-chat-widget {
  position: fixed;
  z-index: 120;
}
.shop-chat-mobile-layer {
  position: fixed;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: rgba(0, 0, 0, 0.45);
  overflow: hidden;
  overscroll-behavior: none;
  -webkit-overflow-scrolling: auto;
}
.chat-panel-shell {
  flex: 0 1 100%;
  min-height: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.desktop-backdrop {
  display: block;
}
.shop-chat-widget.is-open .desktop-backdrop {
  display: block;
  position: fixed;
  inset: 0;
  z-index: 0;
  background: rgba(0, 0, 0, 0.45);
}
.chat-backdrop {
  display: none;
}
.chat-fab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  border-radius: 999px;
  padding: 12px 16px;
  background: var(--shop-gradient);
  color: #fff;
  box-shadow: 0 8px 24px rgba(var(--shop-primary-rgb), 0.45);
  cursor: grab;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
  touch-action: none;
  user-select: none;
  -webkit-user-select: none;
}
.chat-fab:hover:not(.dragging) { transform: translateY(-2px); }
.chat-fab.dragging {
  cursor: grabbing;
  transform: scale(1.04);
  box-shadow: 0 12px 28px rgba(var(--shop-primary-rgb), 0.55);
}
.chat-fab.active { border-radius: 50%; padding: 14px; }
.fab-label { white-space: nowrap; }
@media (max-width: 768px) {
  .shop-chat-widget.is-open {
    inset: 0;
    z-index: 3000;
  }
}
</style>
