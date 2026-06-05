<template>
  <div class="chat-rich-input">
    <div class="chat-toolbar">
      <el-popover v-model:visible="emojiOpen" placement="top-start" :width="280" trigger="click">
        <div class="emoji-grid">
          <button
            v-for="(e, i) in CHAT_EMOJIS"
            :key="i"
            type="button"
            class="emoji-btn"
            @click="pickEmoji(e)"
          >{{ e }}</button>
        </div>
        <template #reference>
          <el-button link class="tool-btn" title="表情"><span class="tool-emoji">😊</span></el-button>
        </template>
      </el-popover>
      <el-button link class="tool-btn" title="图片" :loading="uploading" @click="pickImage">
        <el-icon><Picture /></el-icon>
      </el-button>
      <el-popover
        v-if="isMobile"
        v-model:visible="videoOpen"
        placement="top-start"
        :width="200"
        trigger="click"
      >
        <div class="video-picker-menu">
          <button type="button" class="video-picker-item" @click="pickVideoFromAlbum">
            <el-icon><FolderOpened /></el-icon>
            相册选视频
          </button>
          <button type="button" class="video-picker-item" @click="pickVideoFromCamera">
            <el-icon><VideoCamera /></el-icon>
            拍摄视频
          </button>
        </div>
        <template #reference>
          <el-button link class="tool-btn" title="视频" :loading="uploading">
            <el-icon><VideoCamera /></el-icon>
          </el-button>
        </template>
      </el-popover>
      <el-button v-else link class="tool-btn" title="视频" :loading="uploading" @click="pickVideoFromAlbum">
        <el-icon><VideoCamera /></el-icon>
      </el-button>
    </div>
    <div class="chat-input-row" :class="{ mobile: isMobile }">
      <input
        v-if="isMobile"
        ref="textRef"
        type="text"
        class="chat-native-input"
        :value="modelValue"
        :placeholder="placeholder"
        enterkeyhint="send"
        inputmode="text"
        autocomplete="off"
        @input="onNativeInput"
        @keydown.enter.prevent="onMobileSend"
        @focus="emit('focus')"
        @blur="emit('blur')"
      />
      <el-input
        v-else
        :model-value="modelValue"
        type="textarea"
        :rows="rows"
        resize="none"
        :placeholder="placeholder"
        @update:model-value="emit('update:modelValue', $event)"
        @keyup.enter.exact="emit('send')"
      />
      <button
        v-if="isMobile"
        type="button"
        class="chat-send-native"
        :disabled="sending"
        @click.stop="onSend"
      >{{ sending ? '…' : sendLabel }}</button>
      <el-button
        v-else
        type="primary"
        class="chat-send"
        :loading="sending"
        @click="onSend"
      >{{ sendLabel }}</el-button>
    </div>
    <!-- file inputs outside flow to avoid iOS caret glitches -->
    <input ref="imageInputRef" type="file" accept="image/*" class="hidden-file" @change="onFileChange" />
    <input ref="videoAlbumInputRef" type="file" accept="video/*" class="hidden-file" @change="onFileChange" />
    <input ref="videoCaptureInputRef" type="file" accept="video/*" capture="environment" class="hidden-file" @change="onFileChange" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { FolderOpened, Picture, VideoCamera } from '@element-plus/icons-vue'
import { CHAT_EMOJIS } from '@/utils/chatMessage'

const props = defineProps({
  modelValue: { type: String, default: '' },
  sending: { type: Boolean, default: false },
  uploading: { type: Boolean, default: false },
  placeholder: { type: String, default: '请输入您的问题' },
  sendLabel: { type: String, default: '发送' },
  rows: { type: Number, default: 2 }
})

const emit = defineEmits(['update:modelValue', 'send', 'upload', 'focus', 'blur'])

const emojiOpen = ref(false)
const videoOpen = ref(false)
const textRef = ref(null)
const imageInputRef = ref(null)
const videoAlbumInputRef = ref(null)
const videoCaptureInputRef = ref(null)

const isMobile = computed(() => /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent))

function pickEmoji(e) {
  emit('update:modelValue', (props.modelValue || '') + e)
  emojiOpen.value = false
}

function pickImage() {
  imageInputRef.value?.click()
}

function pickVideoFromAlbum() {
  videoOpen.value = false
  videoAlbumInputRef.value?.click()
}

function pickVideoFromCamera() {
  videoOpen.value = false
  videoCaptureInputRef.value?.click()
}

function onNativeInput(e) {
  emit('update:modelValue', e.target.value)
}

function onMobileSend() {
  onSend()
}

function onSend() {
  emit('send')
}

function onFileChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (file) emit('upload', file)
}
</script>

<style scoped lang="scss">
.chat-rich-input {
  position: relative;
}
.chat-toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 6px;
}
.tool-btn {
  padding: 4px 8px;
  font-size: 18px;
  color: var(--shop-text-muted, #888);
}
.tool-btn:hover { color: var(--shop-primary, #409eff); }
.tool-emoji { font-size: 18px; line-height: 1; }
.hidden-file {
  position: fixed;
  left: -9999px;
  top: 0;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}
.chat-input-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.chat-input-row.mobile {
  align-items: stretch;
}
.chat-native-input {
  flex: 1;
  min-width: 0;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--shop-border, #dcdfe6);
  border-radius: 8px;
  background: var(--shop-surface, #fff);
  color: var(--shop-text, #303133);
  font-size: 16px;
  line-height: 40px;
  outline: none;
  -webkit-appearance: none;
  appearance: none;
  box-sizing: border-box;
}
.chat-native-input:focus {
  border-color: var(--shop-primary, #409eff);
}
.chat-send-native {
  flex-shrink: 0;
  min-width: 56px;
  min-height: 40px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: var(--shop-primary, #409eff);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  align-self: flex-end;
}
.chat-send-native:disabled {
  opacity: 0.65;
}
.chat-send-native:active:not(:disabled) {
  opacity: 0.85;
}
.chat-send {
  flex-shrink: 0;
  min-width: 56px;
}
.chat-input-row.mobile .chat-send {
  align-self: flex-end;
  min-height: 40px;
}
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}
.emoji-btn {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1.2;
  padding: 4px;
  cursor: pointer;
  border-radius: 6px;
}
.emoji-btn:hover { background: rgba(0, 0, 0, 0.06); }
.video-picker-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.video-picker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  text-align: left;
}
.video-picker-item:hover,
.video-picker-item:active {
  background: #f5f7fa;
}
</style>
