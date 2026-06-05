const baseApi = import.meta.env.VITE_APP_BASE_API

export function resolveShopMedia(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return baseApi + url
}

export function resolveShopMediaList(csv) {
  if (!csv) return []
  return csv.split(',').filter(Boolean).map(resolveShopMedia)
}

export function buildProductMediaUrls(product) {
  if (!product) return []
  const rawList = []
  const seen = new Set()
  const addRaw = (raw) => {
    const key = (raw || '').trim()
    if (key && !seen.has(key)) {
      seen.add(key)
      rawList.push(key)
    }
  }
  addRaw(product.pic)
  if (product.album) {
    product.album.split(',').forEach(part => addRaw(part))
  }
  return rawList.map(resolveShopMedia)
}

export function productCoverPic(product) {
  if (!product) return ''
  return product.pic || (product.album ? product.album.split(',')[0].trim() : '')
}
