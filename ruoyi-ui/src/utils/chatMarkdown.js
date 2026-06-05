/**
 * Safe subset Markdown for chat bubbles (escape HTML first, then apply formatting).
 */
function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * @param {string} text
 * @returns {string} HTML safe for v-html in chat bubbles
 */
export function formatChatMarkdown(text) {
  if (text == null || text === '') return ''

  const normalized = String(text).replace(/\r\n/g, '\n').trim()
  if (!normalized.includes('\n\n')) {
    const lines = normalized.split('\n').map(l => l.trim()).filter(Boolean)
    if (lines.length <= 1) {
      return `<p>${inlineFormat(normalized)}</p>`
    }
    return lines.map(line => `<p>${inlineFormat(line)}</p>`).join('')
  }

  const blocks = normalized.split(/\n{2,}/)
  const htmlBlocks = blocks.map(block => {
    const lines = block.split('\n')
    const parts = []
    let listType = null
    let listItems = []

    function flushList() {
      if (!listItems.length) return
      const tag = listType === 'ol' ? 'ol' : 'ul'
      parts.push(`<${tag}>${listItems.map(li => `<li>${inlineFormat(li)}</li>`).join('')}</${tag}>`)
      listItems = []
      listType = null
    }

    for (const rawLine of lines) {
      const line = rawLine.trim()
      if (!line) continue

      const ul = line.match(/^[-*+]\s+(.+)$/)
      const ol = line.match(/^\d+[.)]\s+(.+)$/)
      if (ul) {
        if (listType && listType !== 'ul') flushList()
        listType = 'ul'
        listItems.push(ul[1])
        continue
      }
      if (ol) {
        if (listType && listType !== 'ol') flushList()
        listType = 'ol'
        listItems.push(ol[1])
        continue
      }

      flushList()
      parts.push(`<p>${inlineFormat(line)}</p>`)
    }
    flushList()
    return parts.join('')
  })

  return htmlBlocks.join('') || `<p>${inlineFormat(String(text))}</p>`
}

function inlineFormat(line) {
  let s = escapeHtml(line)
  s = s.replace(/`([^`]+)`/g, '<code>$1</code>')
  s = s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  s = s.replace(/__(.+?)__/g, '<strong>$1</strong>')
  s = s.replace(/(?<!\*)\*([^*\n]+?)\*(?!\*)/g, '<em>$1</em>')
  return s
}

export function shouldRenderMarkdown(senderType) {
  return senderType === 'ai' || senderType === 'agent' || senderType === 'system'
}
