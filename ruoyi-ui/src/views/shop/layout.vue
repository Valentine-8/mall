<template>
  <div class="shop-page-bg">
    <header class="shop-topnav shop-only-pc flex">
      <div class="shop-content topnav-inner">
        <router-link to="/shop/home" class="brand">若依商城</router-link>
        <nav class="topnav-links">
          <router-link to="/shop/home" class="topnav-item" active-class="active">首页</router-link>
          <router-link to="/shop/cart" custom v-slot="{ navigate, isActive }">
            <span class="topnav-item" :class="{ active: isActive }" @click="shopNavClick(router, '/shop/cart', navigate)">购物车</span>
          </router-link>
          <router-link to="/shop/mine" class="topnav-item" active-class="active">我的</router-link>
        </nav>
        <div v-if="isLogin" class="topnav-user">
          <span class="user-name">{{ displayName }}</span>
          <el-button link class="topnav-login" @click="handleLogout">退出</el-button>
        </div>
        <router-link v-else :to="loginTo" class="topnav-login">登录</router-link>
      </div>
    </header>

    <div class="shop-layout">
      <header class="shop-header shop-only-mobile" v-if="showSubPage">
        <el-button link @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="shop-title">{{ pageTitle }}</span>
        <div v-if="isLogin" class="header-user">
          <span class="user-name">{{ displayName }}</span>
          <el-button link class="shop-login-link" @click="handleLogout">退出</el-button>
        </div>
        <router-link v-else :to="loginTo" class="shop-login-link">登录</router-link>
      </header>

      <div class="shop-pc-subhead shop-only-pc" v-if="showSubPage">
        <div class="shop-content">
          <el-button link type="primary" @click="goBack"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
          <span class="sub-title">{{ pageTitle }}</span>
        </div>
      </div>

      <main class="shop-main" :class="{ 'has-tabbar': showMainTab }">
        <router-view />
      </main>

      <nav class="shop-tabbar shop-only-mobile" v-if="showMainTab">
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
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import { shopNavClick } from '@/utils/shopAuth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => !!getToken())
const displayName = computed(() => userStore.nickName || userStore.name || '会员')
const loginTo = computed(() => ({ path: '/login', query: { redirect: route.fullPath } }))

const mainTabRoutes = ['/shop/home', '/shop/cart', '/shop/mine']
const showMainTab = computed(() => mainTabRoutes.includes(route.path))
const showSubPage = computed(() => !showMainTab.value)
const pageTitle = computed(() => route.meta?.title || '商城')

onMounted(() => {
  if (getToken() && !userStore.name) {
    userStore.getInfo().catch(() => {})
  }
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
  if (prev && typeof prev === 'string' && !prev.includes('/login')) {
    router.back()
    return
  }
  router.replace('/shop/mine')
}

function handleLogout() {
  userStore.logOut().then(() => router.push('/shop/mine'))
}
</script>

<style scoped lang="scss">
.shop-layout {
  width: 100%;
  max-width: var(--shop-max-width);
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f6f8;
}
.shop-topnav {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}
.topnav-inner {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px var(--shop-content-pad);
  box-sizing: border-box;
}
.brand {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b35;
  text-decoration: none;
  flex-shrink: 0;
}
.topnav-links { display: flex; gap: 4px; flex: 1; }
.topnav-item {
  padding: 8px 18px;
  border-radius: 6px;
  color: #666;
  text-decoration: none;
  font-size: 15px;
}
.topnav-item.active, .topnav-item:hover {
  color: #ff6b35;
  background: #fff5f0;
}
.topnav-login {
  color: #666;
  font-size: 14px;
  text-decoration: none;
}
.topnav-login:hover { color: #ff6b35; }
.topnav-user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.topnav-user .user-name {
  color: #333;
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.header-user .user-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shop-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}
.shop-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  text-align: center;
  margin-right: 28px;
}
.shop-login-link { color: #fff; font-size: 14px; text-decoration: none; }
.shop-pc-subhead {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 14px 0;
}
.shop-pc-subhead .shop-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sub-title { font-size: 16px; font-weight: 600; color: #333; }
.shop-main { flex: 1; }
.shop-main.has-tabbar { padding-bottom: calc(56px + env(safe-area-inset-bottom)); }
.shop-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 6px 0 calc(6px + env(safe-area-inset-bottom));
  z-index: 50;
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #999;
  font-size: 11px;
  text-decoration: none;
}
.tab-item.active { color: #ff6b35; }
@media (min-width: 769px) {
  .shop-layout { background: transparent; min-height: auto; }
  .shop-main.has-tabbar { padding-bottom: 24px; }
}
</style>
