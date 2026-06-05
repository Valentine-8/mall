<template>
  <div class="social-callback-page">
    <el-icon class="loading-icon" v-if="loading"><Loading /></el-icon>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'
import { resolveShopLoginPath } from '@/utils/authRoute'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const message = ref('正在登录...')

function shopLoginPath(redirect) {
  const r = typeof redirect === 'string' ? redirect : '/shop/home'
  if (r.startsWith('/shop')) {
    return `/shop/login?redirect=${encodeURIComponent(r)}`
  }
  return '/shop/login'
}

onMounted(async () => {
  const error = route.query.error
  const token = route.query.token
  const redirect = route.query.redirect || '/shop/home'
  if (error) {
    loading.value = false
    message.value = decodeURIComponent(error)
    setTimeout(() => router.replace(shopLoginPath(redirect)), 2000)
    return
  }
  if (!token) {
    loading.value = false
    message.value = '登录失败：未获取到令牌'
    setTimeout(() => router.replace('/shop/login'), 2000)
    return
  }
  setToken(token)
  userStore.applyToken(token)
  try {
    await userStore.getInfo()
    const target = resolveShopLoginPath(typeof redirect === 'string' ? redirect : undefined)
    router.replace(target)
  } catch (e) {
    loading.value = false
    message.value = '登录失败，请重试'
    setTimeout(() => router.replace('/shop/login'), 2000)
  }
})
</script>

<style scoped>
.social-callback-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #666;
}
.loading-icon { font-size: 40px; color: var(--shop-primary, #ff6b35); animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
