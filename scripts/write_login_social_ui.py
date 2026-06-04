# -*- coding: utf-8 -*-
import os

path = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views', 'login.vue')
with open(path, encoding='utf-8') as f:
    t = f.read()

social_block = '''
      <div class="social-login" v-if="socialEnabled">
        <div class="social-divider"><span>其他登录方式</span></div>
        <div class="social-btns">
          <el-button v-if="socialWechat" class="social-btn wechat" size="large" :loading="socialLoading === 'wechat'" @click="handleSocialLogin('wechat')">
            微信登录
          </el-button>
          <el-button v-if="socialAlipay" class="social-btn alipay" size="large" :loading="socialLoading === 'alipay'" @click="handleSocialLogin('alipay')">
            支付宝登录
          </el-button>
        </div>
        <p class="social-tip" v-if="socialMock">开发模式：未配置 AppId 时将使用模拟登录</p>
      </div>
'''

if 'social-login' not in t:
    t = t.replace(
        '        <div style="text-align: center; margin-top: 12px;">\n          <router-link class="link-type" to="/shop/home">',
        social_block + '\n        <div style="text-align: center; margin-top: 12px;">\n          <router-link class="link-type" to="/shop/home">'
    )

if 'getSocialConfig' not in t:
    t = t.replace(
        "import { getCodeImg } from \"@/api/login\"",
        "import { getCodeImg } from \"@/api/login\"\nimport { getSocialAuthorize, getSocialConfig, socialMockLogin } from \"@/api/social\""
    )

if 'socialEnabled' not in t:
    insert_after = "const redirect = ref(undefined)\n"
    social_script = """
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

"""
    t = t.replace(insert_after, insert_after + social_script)
    t = t.replace(
        'import useUserStore from \'@/store/modules/user\'',
        "import useUserStore from '@/store/modules/user'\nimport { setToken } from '@/utils/auth'"
    )
    t = t.replace('getCode()\ngetCookie()', 'getCode()\ngetCookie()\nloadSocialConfig()')

if '.social-login' not in t:
    t = t.replace(
        'html.dark .login {',
        """
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
"""
    )

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(t)
print('login.vue updated')
