<template>
  <div class="social-callback-page">
    <el-icon class="loading-icon" v-if="loading"><Loading /></el-icon>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const message = ref('正在登录...')

onMounted(async () => {
  const error = route.query.error
  const token = route.query.token
  const redirect = route.query.redirect || '/shop/home'
  if (error) {
    loading.value = false
    message.value = decodeURIComponent(error)
    setTimeout(() => router.replace('/login?redirect=' + encodeURIComponent(redirect)), 2000)
    return
  }
  if (!token) {
    loading.value = false
    message.value = '登录失败：未获取到令牌'
    setTimeout(() => router.replace('/login'), 2000)
    return
  }
  setToken(token)
  try {
    await userStore.getInfo()
    router.replace(redirect)
  } catch (e) {
    loading.value = false
    message.value = '登录失败，请重试'
    setTimeout(() => router.replace('/login'), 2000)
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
.loading-icon { font-size: 40px; color: #ff6b35; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
