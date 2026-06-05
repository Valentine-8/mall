<template>
  <div class="chat-message-body" :class="{ 'is-media': isMedia }">
    <template v-if="msgType === MSG_IMAGE">
      <img
        v-if="isMobile"
        class="chat-media-img"
        :src="mediaUrl"
        alt="图片"
        loading="lazy"
        @click="previewOpen = true"
      />
      <el-image
        v-else
        class="chat-media-img"
        :src="mediaUrl"
        :preview-src-list="[mediaUrl]"
        fit="cover"
        preview-teleported
      />
      <ChatMobileImagePreview v-model="previewOpen" :src="mediaUrl" />
    </template>
    <template v-else-if="msgType === MSG_VIDEO">
      <video class="chat-media-video" :src="mediaUrl" controls playsinline preload="metadata" />
    </template>
    <div
      v-else-if="useMarkdown"
      class="chat-text chat-text--md"
      v-html="formatChatMarkdown(message.content)"
    />
    <div v-else class="chat-text">{{ message.content }}</div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  MSG_IMAGE,
  MSG_VIDEO,
  formatChatMarkdown,
  normalizeMsgType,
  resolveChatMediaUrl,
  useMarkdownForMessage
} from '@/utils/chatMessage'
import { isMobileViewport } from '@/utils/usePageScrollLock'
import ChatMobileImagePreview from '@/components/ChatMobileImagePreview/index.vue'

const props = defineProps({
  message: { type: Object, required: true }
})

const previewOpen = ref(false)
const isMobile = computed(() => isMobileViewport())

const msgType = computed(() => normalizeMsgType(props.message))
const isMedia = computed(() => msgType.value === MSG_IMAGE || msgType.value === MSG_VIDEO)
const mediaUrl = computed(() => resolveChatMediaUrl(props.message?.content))
const useMarkdown = computed(() => useMarkdownForMessage(props.message))
</script>

<style scoped lang="scss">
.chat-message-body {
  display: inline-block;
  max-width: 100%;
  text-align: left;
}
.chat-message-body.is-media {
  padding: 4px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--chat-media-bg, rgba(255, 255, 255, 0.95));
}
.chat-media-img {
  display: block;
  max-width: 220px;
  max-height: 220px;
  border-radius: 8px;
  cursor: zoom-in;
  object-fit: cover;
}
.chat-media-video {
  display: block;
  max-width: 240px;
  max-height: 180px;
  border-radius: 8px;
  background: #000;
}
.chat-text {
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}
.chat-text--md {
  :deep(p) {
    margin: 0 0 8px;
    &:last-child { margin-bottom: 0; }
  }
  :deep(strong) { font-weight: 600; }
  :deep(em) { font-style: italic; }
  :deep(ul),
  :deep(ol) {
    margin: 4px 0 8px;
    padding-left: 18px;
  }
  :deep(li) { margin: 2px 0; }
  :deep(code) {
    padding: 1px 4px;
    border-radius: 4px;
    font-size: 12px;
    background: rgba(0, 0, 0, 0.06);
  }
}
</style>
