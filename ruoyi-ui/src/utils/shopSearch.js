const STORAGE_KEY = 'guoqing-shop-search-history'
const MAX_HISTORY = 10

function readList() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const list = raw ? JSON.parse(raw) : []
    return Array.isArray(list) ? list.filter(item => typeof item === 'string' && item.trim()) : []
  } catch {
    return []
  }
}

function writeList(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export function getShopSearchHistory() {
  return readList()
}

export function addShopSearchHistory(keyword) {
  const kw = String(keyword || '').trim()
  if (!kw) return getShopSearchHistory()
  const next = [kw, ...readList().filter(item => item !== kw)].slice(0, MAX_HISTORY)
  writeList(next)
  return next
}

export function removeShopSearchHistory(keyword) {
  const kw = String(keyword || '').trim()
  const next = readList().filter(item => item !== kw)
  writeList(next)
  return next
}

export function clearShopSearchHistory() {
  writeList([])
  return []
}
