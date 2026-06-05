<template>
  <div class="shop-page-bg">
    <header class="shop-topnav shop-only-pc flex">
      <div class="shop-content topnav-inner">
        <router-link to="/shop/home" class="brand">
          <span class="brand-name">{{ shopName }}</span>
          <span class="brand-tag">{{ shopSlogan }}</span>
        </router-link>
        <nav class="topnav-links">
          <router-link to="/shop/home" class="topnav-item" active-class="active">首页</router-link>
          <router-link to="/shop/cart" custom v-slot="{ navigate, isActive }">
            <span class="topnav-item" :class="{ active: isActive }" @click="shopNavClick(router, '/shop/cart', navigate)">购物车</span>
          </router-link>
          <router-link to="/shop/mine" class="topnav-item" active-class="active">我的</router-link>
        </nav>
        <div class="topnav-actions">
          <shop-dark-toggle />
          <shop-theme-picker />
          <div v-if="isLogin" class="topnav-user">
            <span class="user-name">{{ displayName }}</span>
            <el-button link class="topnav-login" @click="handleLogout">退出</el-button>
          </div>
          <router-link v-else :to="loginTo" class="topnav-login">登录</router-link>
        </div>
      </div>
    </header>

    <div class="shop-layout">
      <div v-if="showMainTab" class="shop-mobile-bar shop-only-mobile">
        <span class="mobile-bar-brand">{{ shopName }}</span>
        <div class="mobile-bar-actions">
          <shop-dark-toggle :show-label="false" />
          <shop-theme-picker />
        </div>
      </div>

      <header class="shop-header shop-only-mobile" v-if="showSubPage">
        <el-button link class="header-back" @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="shop-title">{{ pageTitle }}</span>
        <div class="header-actions">
          <shop-dark-toggle :show-label="false" />
          <shop-theme-picker />
          <div v-if="isLogin" class="header-user">
            <span class="user-name">{{ displayName }}</span>
            <el-button link class="shop-login-link" @click="handleLogout">退出</el-button>
          </div>
          <router-link v-else :to="loginTo" class="shop-login-link">登录</router-link>
        </div>
      </header>

      <div class="shop-pc-subhead shop-only-pc" v-if="showSubPage">
        <div class="shop-content">
          <el-button link type="primary" @click="goBack"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
          <span class="sub-title">{{ pageTitle }}</span>
        </div>
      </div>

      <main class="shop-main" :class="{ 'has-tabbar': showMainTab && !keyboardOpen }">
        <router-view />
      </main>

      <nav
        class="shop-tabbar shop-only-mobile"
        :class="{ 'tabbar-hidden': keyboardOpen }"
        v-show="showMainTab && !keyboardOpen"
      >
        <router-link to="/shop/home" class="tab-item" active-class="active">
          <el-icon><HomeFilled /></el-icon><span>首页</span>
        </router-link>
        <router-link to="/shop/cart" custom v-slot="{ navigate, isActive }">
          <span class="tab-item" :class="{ active: isActive }" @click="shopNavClick(router, '/shop/cart', navigate)">
            <el-icon><ShoppingCart /></el-icon><span>购物车</span>
          </span>
        </router-link>
        <router-link to="/shop/mine" class="tab-item" active-class="active">
          <el-icon><User /></el-icon><span>我的</span>
        </router-link>
      </nav>
    </div>
    <shop-chat-widget />
  </div>
</template>

<script setup>
import ShopChatWidget from '@/components/ShopChatWidget/index.vue'
import { computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ShopDarkToggle from '@/components/ShopDarkToggle/index.vue'
import ShopThemePicker from '@/components/ShopThemePicker/index.vue'
import { shopNavClick } from '@/utils/shopAuth'
import { SHOP_NAME, SHOP_SLOGAN, initShopTheme } from '@/utils/shopTheme'
import useUserStore from '@/store/modules/user'
import {
  installShopKeyboardListener,
  shopKeyboardOpen,
  uninstallShopKeyboardListener
} from '@/utils/useMobileKeyboard'

const keyboardOpen = shopKeyboardOpen

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const shopName = SHOP_NAME
const shopSlogan = SHOP_SLOGAN
const isLogin = computed(() => !!userStore.token)
const displayName = computed(() => userStore.nickName || userStore.name || '\u4f1a\u5458')
const loginTo = computed(() => ({ path: '/shop/login', query: { redirect: route.fullPath } }))

const mainTabRoutes = ['/shop/home', '/shop/cart', '/shop/mine']
const showMainTab = computed(() => mainTabRoutes.includes(route.path))
const showSubPage = computed(() => !showMainTab.value)
const pageTitle = computed(() => route.meta?.title || '\u5546\u57ce')

onMounted(() => {
  initShopTheme()
  installShopKeyboardListener()
  if (userStore.token && !userStore.name) {
    userStore.getInfo().catch(() => {})
  }
})

onBeforeUnmount(() => {
  uninstallShopKeyboardListener()
})

function goBack() {
  const path = route.path
  if (path === '/shop/checkout') {
    if (route.query.productId) {
      router.replace('/shop/product/' + route.query.productId)
      return
    }
    if (route.query.cartIds) {
      router.replace('/shop/cart')
      return
    }
    router.replace('/shop/home')
    return
  }
  if (path === '/shop/profile' || path === '/shop/orders') {
    router.replace('/shop/mine')
    return
  }
  if (path.startsWith('/shop/product/')) {
    router.replace('/shop/home')
    return
  }
  if (path.startsWith('/shop/orders/')) {
    router.replace(route.query.from === 'mine' ? '/shop/mine' : '/shop/orders')
    return
  }
  const prev = window.history.state?.back
  if (prev && typeof prev === 'string' && !prev.includes('/shop/login')) {
    router.back()
    return
  }
  router.replace('/shop/mine')
}

function handleLogout() {
  userStore.logOut().then(() => {
    ElMessage.success('\u5df2\u9000\u51fa\u767b\u5f55')
    if (route.path !== '/shop/mine') {
      router.push('/shop/mine')
    }
  })
}
</script>

<style scoped lang="scss">
.shop-layout {
  width: 100%;
  max-width: var(--shop-max-width);
  margin: 0 auto;
  min-height: 100vh;
}
.shop-topnav {
  background: var(--shop-nav-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--shop-border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shop-shadow-sm);
}
.topnav-inner {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 14px var(--shop-content-pad);
  box-sizing: border-box;
}
.brand {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  flex-shrink: 0;
  gap: 2px;
}
.brand-name {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.02em;
  background: var(--shop-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand-tag {
  font-size: 11px;
  color: var(--shop-text-faint);
  letter-spacing: 0.04em;
}
.topnav-links { display: flex; gap: 6px; flex: 1; }
.topnav-item {
  padding: 8px 18px;
  border-radius: 999px;
  color: var(--shop-text-muted);
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s, background 0.2s;
}
.topnav-item.active, .topnav-item:hover {
  color: var(--shop-primary);
  background: var(--shop-primary-soft);
}
.topnav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.topnav-login {
  color: var(--shop-text-muted);
  font-size: 14px;
  text-decoration: none;
}
.topnav-login:hover { color: var(--shop-primary); }
.topnav-user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.topnav-user .user-name {
  color: var(--shop-text-secondary);
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.header-user .user-name {
  max-width: 64px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shop-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--shop-gradient);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 4px 16px rgba(var(--shop-primary-rgb), 0.25);
}
.header-back { color: #fff !important; }
.shop-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  text-align: center;
  min-width: 0;
}
.shop-login-link { color: #fff; font-size: 13px; text-decoration: none; white-space: nowrap; }
.shop-pc-subhead {
  background: var(--shop-surface);
  border-bottom: 1px solid var(--shop-border);
  padding: 14px 0;
}
.shop-pc-subhead .shop-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sub-title { font-size: 16px; font-weight: 600; color: var(--shop-text); }
.shop-main { flex: 1; }
.shop-main.has-tabbar { padding-bottom: calc(56px + env(safe-area-inset-bottom)); }
.shop-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: var(--shop-tabbar-bg);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--shop-border);
  padding: 6px 0 calc(6px + env(safe-area-inset-bottom));
  z-index: 50;
  box-shadow: 0 -4px 20px rgba(15, 23, 42, 0.06);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: var(--shop-text-faint);
  font-size: 11px;
  text-decoration: none;
  transition: color 0.2s;
}
.tab-item.active { color: var(--shop-primary); }
.shop-tabbar.tabbar-hidden {
  transform: translateY(100%);
  pointer-events: none;
}
:deep(.shop-header .shop-theme-trigger),
:deep(.shop-header .shop-dark-toggle) {
  border-color: rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  padding: 5px 8px;
}
:deep(.shop-header .shop-dark-toggle.active) {
  background: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff;
}
:deep(.shop-header .shop-theme-label) { display: none; }
.shop-mobile-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  background: var(--shop-nav-bg);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--shop-border);
  position: sticky;
  top: 0;
  z-index: 90;
}
.mobile-bar-brand {
  font-size: 17px;
  font-weight: 800;
  background: var(--shop-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.mobile-bar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
:deep(.shop-mobile-bar .shop-dark-toggle),
:deep(.shop-mobile-bar .shop-theme-trigger) {
  padding: 6px 8px;
}
:deep(.shop-mobile-bar .shop-theme-label) { display: none; }
@media (min-width: 769px) {
  .shop-layout { min-height: auto; }
  .shop-main.has-tabbar { padding-bottom: 24px; }
}
</style>
