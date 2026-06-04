<template>
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
        <div class="social-login" v-if="socialEnabled">
          <div class="social-divider"><span>其他登录方式</span></div>
          <div class="social-btns">
            <el-button
              v-if="socialWechat"
              class="social-btn wechat"
              size="large"
              :loading="socialLoading === 'wechat'"
              @click="handleSocialLogin('wechat')"
            >微信登录</el-button>
            <el-button
              v-if="socialAlipay"
              class="social-btn alipay"
              size="large"
              :loading="socialLoading === 'alipay'"
              @click="handleSocialLogin('alipay')"
            >支付宝登录</el-button>
          </div>
          <p class="social-tip" v-if="socialMock">开发模式：未配置 AppId 时将使用模拟登录</p>
        </div>
        <div style="text-align: center; margin-top: 12px;">
          <router-link class="link-type" to="/shop/home">进入商城</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import { getSocialAuthorize, getSocialConfig, socialMockLogin } from "@/api/social"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import { setToken } from "@/utils/auth"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)
const socialEnabled = ref(false)
const socialWechat = ref(false)
const socialAlipay = ref(false)
const socialMock = ref(false)
const socialLoading = ref('')

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
  const target = redirect.value || '/shop/home'
  socialLoading.value = type
  getSocialAuthorize(type, target).then(res => {
    const d = res.data || {}
    if (d.mock) {
      return socialMockLogin(type, target).then(r => {
        setToken(r.token)
        return userStore.getInfo().then(() => {
          router.push({ path: target })
        })
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

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
    if (redirect.value && String(redirect.value).startsWith('/shop')) {
      loginForm.value.username = ''
      loginForm.value.password = ''
    }
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        const target = redirect.value || '/'
        if (typeof target === 'string' && target.includes('?')) {
          router.push(target)
        } else {
          router.push({ path: target, query: otherQueryParams })
        }
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
loadSocialConfig()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}

.social-login { margin-top: 8px; }
.social-divider {
  display: flex; align-items: center; gap: 12px; color: #999; font-size: 13px; margin: 16px 0 12px;
}
.social-divider::before, .social-divider::after {
  content: ''; flex: 1; height: 1px; background: #e8e8e8;
}
.social-btns { display: flex; gap: 12px; }
.social-btn { flex: 1; border: none; color: #fff; }
.social-btn.wechat { background: #07c160; }
.social-btn.wechat:hover { background: #06ad56; color: #fff; }
.social-btn.alipay { background: #1677ff; }
.social-btn.alipay:hover { background: #4096ff; color: #fff; }
.social-tip { text-align: center; font-size: 12px; color: #999; margin-top: 10px; }
html.dark .login {
  background-image: linear-gradient(rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.55)), url("../assets/images/login-background.jpg");
  .login-form {
    background: var(--el-bg-color-overlay) !important;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  }
}
</style>
