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

export function productCoverPic(product) {
  if (!product) return ''
  return product.pic || (product.album ? product.album.split(',')[0] : '')
}
