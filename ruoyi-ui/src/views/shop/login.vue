<template>
  <div class="shop-login">
    <div class="shop-login-card">
      <div class="brand">
        <h1>{{ shopTitle }}</h1>
        <p>登录后继续购物、下单与咨询客服</p>
      </div>
      <el-tabs v-model="loginMode" class="login-tabs" stretch>
        <el-tab-pane v-if="otpConfig.emailEnabled" label="邮箱验证码" name="email" />
        <el-tab-pane label="账号密码" name="account" />
        <el-tab-pane v-if="otpConfig.phoneEnabled" label="手机验证码" name="phone" />
      </el-tabs>
      <template v-if="loginMode === 'account'">
        <el-form ref="loginRef" :model="loginForm" :rules="loginRules" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" size="large" placeholder="账号" auto-complete="off">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" size="large" placeholder="密码" auto-complete="off" @keyup.enter="handleLogin">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code" v-if="captchaEnabled">
            <el-input v-model="loginForm.code" size="large" placeholder="验证码" style="width: 63%" @keyup.enter="handleLogin">
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <img :src="codeUrl" class="captcha-img" alt="验证码" @click="getCode" />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form>
      </template>
      <template v-else>
        <el-form @submit.prevent>
          <el-form-item>
            <el-input
              v-model="otpForm.target"
              size="large"
              :placeholder="loginMode === 'email' ? '邮箱地址' : '手机号'"
              auto-complete="off"
            >
              <template #prefix><el-icon><Message v-if="loginMode === 'email'" /><Iphone v-else /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="otpForm.code" size="large" placeholder="验证码" style="width: 63%" @keyup.enter="handleOtpLogin">
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <el-button class="otp-send-btn" size="large" :disabled="otpCountdown > 0 || otpSending" :loading="otpSending" @click="handleSendOtp">
              {{ otpCountdown > 0 ? otpCountdown + 's' : '获取验证码' }}
            </el-button>
          </el-form-item>
          <p v-if="otpMockHint" class="otp-mock-tip">{{ otpMockHint }}</p>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleOtpLogin">登录 / 注册</el-button>
        </el-form>
      </template>
      <div v-if="socialEnabled" class="social-block">
        <div class="social-divider"><span>其他方式</span></div>
        <div class="social-btns">
          <el-button v-if="socialWechat" class="social-btn wechat" :loading="socialLoading === 'wechat'" @click="handleSocialLogin('wechat')">微信</el-button>
          <el-button v-if="socialAlipay" class="social-btn alipay" :loading="socialLoading === 'alipay'" @click="handleSocialLogin('alipay')">支付宝</el-button>
        </div>
        <p v-if="socialMock" class="social-tip">开发模式：未配置 AppId 时使用模拟登录</p>
      </div>
      <router-link class="back-home" to="/shop/home">← 返回商城首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { User, Lock, Key, Message, Iphone } from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import { getOtpConfig, sendOtpCode, loginByOtp } from '@/api/otp'
import { getSocialAuthorize, getSocialConfig, socialMockLogin } from '@/api/social'
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'
import { resolveShopLoginPath } from '@/utils/authRoute'

const shopTitle = import.meta.env.VITE_APP_SHOP_TITLE || '\u56fd\u6e05\u5546\u57ce'
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({ username: '', password: '', code: '', uuid: '' })
const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}
const codeUrl = ref('')
const loading = ref(false)
const captchaEnabled = ref(true)
const redirect = ref(undefined)
const socialEnabled = ref(false)
const socialWechat = ref(false)
const socialAlipay = ref(false)
const socialMock = ref(false)
const socialLoading = ref('')
const loginMode = ref('email')
const otpConfig = ref({ emailEnabled: true, phoneEnabled: false, emailMock: false, phoneMock: false })
const otpForm = ref({ target: '', code: '' })
const otpSending = ref(false)
const otpCountdown = ref(0)
let otpTimer = null

const otpMockHint = computed(() => {
  if (loginMode.value === 'email' && otpConfig.value.emailMock) {
    return '开发模式：未配置 SMTP 时验证码在后端日志中查看'
  }
  if (loginMode.value === 'phone' && otpConfig.value.phoneMock) {
    return '开发模式：验证码在后端日志中查看'
  }
  return ''
})

watch(route, (r) => {
  redirect.value = r.query?.redirect
}, { immediate: true })

function afterLoginSuccess() {
  const target = resolveShopLoginPath(typeof redirect.value === 'string' ? redirect.value : undefined)
  router.push(target.includes('?') ? target : { path: target })
}

function loadOtpConfig() {
  getOtpConfig().then(res => {
    const d = res.data || {}
    otpConfig.value = {
      emailEnabled: !!d.emailEnabled,
      phoneEnabled: !!d.phoneEnabled,
      emailMock: !!d.emailMock,
      phoneMock: !!d.phoneMock
    }
    if (!otpConfig.value.emailEnabled) {
      loginMode.value = 'account'
    }
  }).catch(() => { loginMode.value = 'account' })
}

function handleSendOtp() {
  const target = otpForm.value.target.trim()
  if (!target) {
    proxy.$modal.msgError(loginMode.value === 'email' ? '请输入邮箱' : '请输入手机号')
    return
  }
  otpSending.value = true
  sendOtpCode({ type: loginMode.value, target }).then(() => {
    proxy.$modal.msgSuccess('验证码已发送')
    otpCountdown.value = 60
    otpTimer = setInterval(() => {
      otpCountdown.value -= 1
      if (otpCountdown.value <= 0 && otpTimer) {
        clearInterval(otpTimer)
        otpTimer = null
      }
    }, 1000)
  }).finally(() => { otpSending.value = false })
}

function handleOtpLogin() {
  const target = otpForm.value.target.trim()
  const code = otpForm.value.code.trim()
  if (!target || !code) {
    proxy.$modal.msgError('请输入邮箱/手机号和验证码')
    return
  }
  loading.value = true
  loginByOtp({ type: loginMode.value, target, code }).then(res => {
    userStore.applyToken(res.token)
    return userStore.getInfo()
  }).then(() => afterLoginSuccess()).finally(() => { loading.value = false })
}

function loadSocialConfig() {
  getSocialConfig().then(res => {
    const d = res.data || {}
    socialEnabled.value = !!d.enabled
    socialWechat.value = !!d.wechatEnabled
    socialAlipay.value = !!d.alipayEnabled
    socialMock.value = !!d.mockEnabled && (!d.wechatConfigured || !d.alipayConfigured)
  }).catch(() => {})
}

function handleSocialLogin(type) {
  const target = resolveShopLoginPath(typeof redirect.value === 'string' ? redirect.value : undefined)
  socialLoading.value = type
  getSocialAuthorize(type, target).then(res => {
    const d = res.data || {}
    if (d.mock) {
      return socialMockLogin(type, target).then(r => {
        userStore.applyToken(r.token)
        return userStore.getInfo().then(() => router.push({ path: target }))
      })
    }
    if (d.url) {
      window.location.href = d.url
      return
    }
    throw new Error('无法获取授权地址')
  }).catch(err => {
    proxy.$modal.msgError(err.msg || err.message || '登录失败')
  }).finally(() => { socialLoading.value = '' })
}

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (!valid) return
    loading.value = true
    Cookies.set('shop_username', loginForm.value.username, { expires: 30 })
    Cookies.set('shop_password', encrypt(loginForm.value.password), { expires: 30 })
    userStore.login(loginForm.value).then(() => userStore.getInfo()).then(() => {
      afterLoginSuccess()
    }).catch(() => {
      if (captchaEnabled.value) getCode()
    }).finally(() => { loading.value = false })
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get('shop_username')
  const password = Cookies.get('shop_password')
  if (username) loginForm.value.username = username
  if (password) loginForm.value.password = decrypt(password)
}

onMounted(() => {
  document.title = `\u767b\u5f55 - ${shopTitle}`
  getCode()
  getCookie()
  loadSocialConfig()
  loadOtpConfig()
})

onBeforeUnmount(() => {
  if (otpTimer) clearInterval(otpTimer)
})
</script>

<style scoped lang="scss">
.shop-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: linear-gradient(145deg, #fff5f0 0%, #ffe8dc 45%, #ffd4c2 100%);
}
.shop-login-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 28px 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 40px rgba(255, 107, 53, 0.15);
}
.brand {
  text-align: center;
  margin-bottom: 20px;
  h1 {
    margin: 0 0 8px;
    font-size: 26px;
    color: #ff6b35;
    font-weight: 700;
  }
  p {
    margin: 0;
    font-size: 13px;
    color: #909399;
  }
}
.login-tabs { margin-bottom: 16px; }
.captcha-img {
  width: 33%;
  height: 40px;
  float: right;
  cursor: pointer;
  border-radius: 4px;
}
.otp-send-btn {
  width: 33%;
  height: 40px;
  float: right;
  padding: 0 6px;
}
.otp-mock-tip {
  margin: -8px 0 12px;
  font-size: 12px;
  color: #e6a23c;
  text-align: center;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
  background: #ff6b35;
  border-color: #ff6b35;
  &:hover { background: #ff8555; border-color: #ff8555; }
}
.social-block { margin-top: 20px; }
.social-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 12px;
  margin-bottom: 12px;
  &::before, &::after { content: ''; flex: 1; height: 1px; background: #eee; }
}
.social-btns { display: flex; gap: 10px; }
.social-btn { flex: 1; border: none; color: #fff; }
.social-btn.wechat { background: #07c160; }
.social-btn.alipay { background: #1677ff; }
.social-tip { text-align: center; font-size: 11px; color: #aaa; margin-top: 8px; }
.back-home {
  display: block;
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #909399;
  text-decoration: none;
  &:hover { color: #ff6b35; }
}
</style>
