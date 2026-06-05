import { resolveShopMedia } from '@/utils/shopMedia'
import { formatChatMarkdown, shouldRenderMarkdown } from '@/utils/chatMarkdown'

export const MSG_TEXT = 'text'
export const MSG_IMAGE = 'image'
export const MSG_VIDEO = 'video'

export const CHAT_EMOJIS = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '🙂', '😉',
  '😍', '🥰', '😘', '😋', '😎', '🤔', '😢', '😭', '😡', '😱',
  '👍', '👎', '👌', '🙏', '👏', '💪', '🎉', '❤️', '💯', '🔥',
  '✅', '❌', '⭐', '🛒', '📦', '🚚', '💬', '📞', '⏰', '💰'
]

export function normalizeMsgType(msg) {
  const t = msg?.msgType || MSG_TEXT
  if (t === MSG_IMAGE || t === MSG_VIDEO) return t
  return MSG_TEXT
}

export function resolveChatMediaUrl(content) {
  return resolveShopMedia(content)
}

export function isMediaMessage(msg) {
  const t = normalizeMsgType(msg)
  return t === MSG_IMAGE || t === MSG_VIDEO
}

export function useMarkdownForMessage(msg) {
  if (isMediaMessage(msg)) return false
  return shouldRenderMarkdown(msg?.senderType)
}

export { formatChatMarkdown, shouldRenderMarkdown }
