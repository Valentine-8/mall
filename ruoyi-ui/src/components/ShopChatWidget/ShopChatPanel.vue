<template>
  <div class="chat-panel shop-card" :style="panelStyle">
    <div class="chat-head">
      <div>
        <strong>{{ panelTitle }}</strong>
        <p class="chat-sub">{{ statusText }}</p>
      </div>
      <el-button link class="chat-close" @click="emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div ref="msgBoxRef" class="chat-messages">
      <div v-if="!isLogin" class="chat-login-tip">
        <p>登录后即可咨询客服</p>
        <el-button type="primary" size="small" @click="emit('login')">去登录</el-button>
      </div>
      <template v-else>
        <div
          v-for="m in messages"
          :key="m.messageId"
          class="chat-row"
          :class="m.senderType === 'user' ? 'mine' : 'other'"
        >
          <span class="chat-sender">{{ senderLabel(m) }}</span>
          <div class="chat-bubble-outer" :class="{ media: isMediaMessage(m) }">
            <ChatMessageBody :message="m" />
          </div>
        </div>
      </template>
    </div>
    <div v-if="isLogin" class="chat-foot">
      <div class="chat-actions">
        <button
          v-if="canTransfer"
          type="button"
          class="chat-action-btn"
          @click.stop="emit('transfer')"
        >转人工</button>
        <button
          v-if="showCloseSession"
          type="button"
          class="chat-action-btn chat-action-btn--link"
          @click.stop="emit('end-session')"
        >结束会话</button>
      </div>
      <ChatRichInput
        :model-value="draft"
        :sending="sending"
        :uploading="uploading"
        @update:model-value="emit('update:draft', $event)"
        @send="emit('send')"
        @upload="emit('upload', $event)"
        @focus="emit('input-focus')"
        @blur="emit('input-blur')"
      />
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { isMediaMessage } from '@/utils/chatMessage'
import ChatMessageBody from '@/components/ChatMessageBody/index.vue'
import ChatRichInput from '@/components/ChatRichInput/index.vue'

const props = defineProps({
  panelTitle: { type: String, default: '' },
  statusText: { type: String, default: '' },
  isLogin: { type: Boolean, default: false },
  messages: { type: Array, default: () => [] },
  draft: { type: String, default: '' },
  sending: { type: Boolean, default: false },
  uploading: { type: Boolean, default: false },
  canTransfer: { type: Boolean, default: false },
  showCloseSession: { type: Boolean, default: false },
  panelStyle: { type: Object, default: () => ({}) },
  scrollToken: { type: Number, default: 0 }
})

const emit = defineEmits([
  'close', 'login', 'transfer', 'end-session', 'send', 'upload',
  'update:draft', 'input-focus', 'input-blur'
])

const msgBoxRef = ref(null)

function senderLabel(m) {
  if (m.senderType === 'user') return '我'
  if (m.senderType === 'ai') return '智能客服'
  if (m.senderType === 'agent') return m.senderName || '客服'
  return '系统'
}

function scrollBottom() {
  const el = msgBoxRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(() => props.scrollToken, () => {
  nextTick(() => scrollBottom())
})

defineExpose({ scrollBottom })
</script>

<style scoped lang="scss">
.chat-panel {
  position: absolute;
  right: 0;
  bottom: 58px;
  width: min(360px, calc(100vw - 32px));
  height: 480px;
  max-height: calc(100dvh - 120px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shop-shadow-lg);
  min-height: 0;
}
.chat-head {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  padding: 14px 14px 10px;
  border-bottom: 1px solid var(--shop-border);
  background: var(--shop-primary-soft);
  strong { font-size: 15px; color: var(--shop-text); }
}
.chat-sub {
  margin: 4px 0 0;
  font-size: 11px;
  color: var(--shop-text-muted);
}
.chat-close {
  color: var(--shop-text-muted);
  flex-shrink: 0;
  padding: 4px;
  font-size: 20px;
  min-width: 44px;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px;
  background: var(--shop-surface-2);
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  touch-action: pan-y;
}
.chat-login-tip {
  text-align: center;
  padding: 40px 16px;
  color: var(--shop-text-muted);
  p { margin: 0 0 12px; }
}
.chat-row {
  margin-bottom: 12px;
  max-width: 88%;
}
.chat-row.mine {
  margin-left: auto;
  text-align: right;
}
.chat-sender {
  display: block;
  font-size: 10px;
  color: var(--shop-text-faint);
  margin-bottom: 4px;
}
.chat-bubble-outer:not(.media) :deep(.chat-text) {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 12px;
  box-shadow: var(--shop-shadow-sm);
}
.chat-row.mine .chat-bubble-outer:not(.media) :deep(.chat-text) {
  background: var(--shop-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.chat-row.other .chat-bubble-outer:not(.media) :deep(.chat-text) {
  background: var(--shop-surface);
  color: var(--shop-text);
  border-bottom-left-radius: 4px;
}
.chat-row.mine .chat-bubble-outer:not(.media) :deep(.chat-text--md code) {
  background: rgba(255, 255, 255, 0.2);
}
.chat-foot {
  flex-shrink: 0;
  border-top: 1px solid var(--shop-border);
  padding: 10px 12px calc(12px + env(safe-area-inset-bottom));
  background: var(--shop-surface);
}
.chat-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.chat-action-btn {
  appearance: none;
  -webkit-appearance: none;
  border: 1px solid var(--shop-border);
  border-radius: 6px;
  background: var(--shop-surface);
  color: var(--shop-text);
  font-size: 13px;
  line-height: 1.2;
  padding: 8px 12px;
  min-height: 36px;
  cursor: pointer;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}
.chat-action-btn--link {
  border-color: transparent;
  background: transparent;
  color: var(--shop-text-muted);
}
.chat-action-btn:active {
  opacity: 0.75;
}
</style>

<style scoped lang="scss">
.chat-panel.mobile-panel,
:deep(.chat-panel.mobile-panel) {
  position: relative;
  left: auto;
  right: auto;
  bottom: auto;
  top: auto;
  width: 100%;
  flex-shrink: 0;
  min-height: 0;
  border-radius: 16px 16px 0 0;
}
</style>
