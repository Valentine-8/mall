export const SHOP_NAME = '\u56fd\u6e05\u5546\u57ce'
export const SHOP_SLOGAN = '\u54c1\u8d28\u597d\u7269\uff0c\u5b89\u5fc3\u9009\u8d2d'

export const SHOP_THEMES = [
  { id: 'coral', name: '\u6d3b\u529b\u6a59', primary: '#ff6b35', light: '#ff8f5c', soft: '#fff5f0', muted: '#fff9f6' },
  { id: 'teal', name: '\u9752\u7eff', primary: '#0d9488', light: '#14b8a6', soft: '#ecfdf5', muted: '#f0fdfa' },
  { id: 'blue', name: '\u851a\u84dd', primary: '#2563eb', light: '#3b82f6', soft: '#eff6ff', muted: '#f0f9ff' },
  { id: 'rose', name: '\u9152\u7ea2', primary: '#e11d48', light: '#f43f5e', soft: '#fff1f2', muted: '#fef2f2' },
  { id: 'violet', name: '\u7d2b\u97f5', primary: '#7c3aed', light: '#8b5cf6', soft: '#f5f3ff', muted: '#faf5ff' }
]

const STORAGE_KEY = 'guoqing-shop-theme'
const DARK_STORAGE_KEY = 'guoqing-shop-dark'

export function getShopTheme(id) {
  return SHOP_THEMES.find(t => t.id === id) || SHOP_THEMES[0]
}

export function getSavedShopThemeId() {
  return localStorage.getItem(STORAGE_KEY) || SHOP_THEMES[0].id
}

export function isShopDarkMode() {
  return localStorage.getItem(DARK_STORAGE_KEY) === '1'
}

function hexToRgb(hex) {
  const n = hex.replace('#', '')
  const full = n.length === 3 ? n.split('').map(c => c + c).join('') : n
  const num = parseInt(full, 16)
  return `${(num >> 16) & 255}, ${(num >> 8) & 255}, ${num & 255}`
}

function refreshPrimaryTints(theme, dark) {
  const root = document.documentElement
  if (dark) {
    const rgb = hexToRgb(theme.primary)
    root.style.setProperty('--shop-primary-soft', `rgba(${rgb}, 0.2)`)
    root.style.setProperty('--shop-primary-muted', `rgba(${rgb}, 0.12)`)
  } else {
    root.style.setProperty('--shop-primary-soft', theme.soft)
    root.style.setProperty('--shop-primary-muted', theme.muted)
  }
}

function updateThemeColorMeta(dark) {
  let meta = document.querySelector('meta[name="theme-color"]')
  if (!meta) {
    meta = document.createElement('meta')
    meta.setAttribute('name', 'theme-color')
    document.head.appendChild(meta)
  }
  meta.setAttribute('content', dark ? '#0f1419' : '#ffffff')
}

export function applyShopTheme(themeId) {
  const theme = getShopTheme(themeId)
  const root = document.documentElement
  root.style.setProperty('--shop-primary', theme.primary)
  root.style.setProperty('--shop-primary-light', theme.light)
  root.style.setProperty('--shop-primary-rgb', hexToRgb(theme.primary))
  root.style.setProperty('--shop-gradient', `linear-gradient(135deg, ${theme.primary} 0%, ${theme.light} 100%)`)
  root.style.setProperty('--el-color-primary', theme.primary)
  localStorage.setItem(STORAGE_KEY, theme.id)
  refreshPrimaryTints(theme, isShopDarkMode())
  return theme
}

export function setShopDarkMode(dark) {
  const root = document.documentElement
  if (dark) {
    root.setAttribute('data-shop-mode', 'dark')
    localStorage.setItem(DARK_STORAGE_KEY, '1')
  } else {
    root.removeAttribute('data-shop-mode')
    localStorage.removeItem(DARK_STORAGE_KEY)
  }
  refreshPrimaryTints(getShopTheme(getSavedShopThemeId()), dark)
  updateThemeColorMeta(dark)
  return dark
}

export function toggleShopDarkMode() {
  return setShopDarkMode(!isShopDarkMode())
}

export function initShopTheme() {
  const dark = isShopDarkMode()
  if (dark) {
    document.documentElement.setAttribute('data-shop-mode', 'dark')
  }
  updateThemeColorMeta(dark)
  return applyShopTheme(getSavedShopThemeId())
}
