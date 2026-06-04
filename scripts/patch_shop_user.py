# -*- coding: utf-8 -*-
import os

path = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views', 'shop', 'layout.vue')

LAYOUT = r'''<template>
  <div class="shop-page-bg">
    <header class="shop-topnav shop-only-pc flex">
      <div class="shop-content topnav-inner">
        <router-link to="/shop/home" class="brand">BRAND</router-link>
        <nav class="topnav-links">
          <router-link to="/shop/home" class="topnav-item" active-class="active">N_HOME</router-link>
          <router-link to="/shop/cart" class="topnav-item" active-class="active">N_CART</router-link>
          <router-link to="/shop/orders" class="topnav-item" active-class="active">N_ORDERS</router-link>
        </nav>
        <div v-if="isLogin" class="topnav-user">
          <span class="user-name">{{ displayName }}</span>
          <el-button link class="topnav-login" @click="handleLogout">N_LOGOUT</el-button>
        </div>
        <router-link v-else :to="loginTo" class="topnav-login">N_LOGIN</router-link>
      </div>
    </header>

    <div class="shop-layout">
      <header class="shop-header shop-only-mobile" v-if="showSubPage">
        <el-button link @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="shop-title">{{ pageTitle }}</span>
        <div v-if="isLogin" class="header-user">
          <span class="user-name">{{ displayName }}</span>
          <el-button link class="shop-login-link" @click="handleLogout">N_LOGOUT</el-button>
        </div>
        <router-link v-else :to="loginTo" class="shop-login-link">N_LOGIN</router-link>
      </header>

      <div class="shop-pc-subhead shop-only-pc" v-if="showSubPage">
        <div class="shop-content">
          <el-button link type="primary" @click="goBack"><el-icon><ArrowLeft /></el-icon> N_BACK</el-button>
          <span class="sub-title">{{ pageTitle }}</span>
        </div>
      </div>

      <main class="shop-main" :class="{ 'has-tabbar': showMainTab }">
        <router-view />
      </main>

      <nav class="shop-tabbar shop-only-mobile" v-if="showMainTab">
        <router-link to="/shop/home" class="tab-item" active-class="active">
          <el-icon><HomeFilled /></el-icon><span>N_HOME</span>
        </router-link>
        <router-link to="/shop/cart" class="tab-item" active-class="active">
          <el-icon><ShoppingCart /></el-icon><span>N_CART</span>
        </router-link>
        <router-link to="/shop/orders" class="tab-item" active-class="active">
          <el-icon><List /></el-icon><span>N_ORDER</span>
        </router-link>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => !!getToken())
const displayName = computed(() => userStore.nickName || userStore.name || 'N_MEMBER')
const loginTo = computed(() => ({ path: '/login', query: { redirect: route.fullPath } }))

const mainTabRoutes = ['/shop/home', '/shop/cart', '/shop/orders']
const showMainTab = computed(() => mainTabRoutes.includes(route.path))
const showSubPage = computed(() => !showMainTab.value)
const pageTitle = computed(() => route.meta?.title || 'N_MALL')

onMounted(() => {
  if (getToken() && !userStore.name) {
    userStore.getInfo().catch(() => {})
  }
})

function goBack() {
  router.back()
}

function handleLogout() {
  userStore.logOut().then(() => router.push('/shop/home'))
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
'''

CN = {
    'BRAND': '\u82e5\u4f9d\u5546\u57ce',
    'N_HOME': '\u9996\u9875',
    'N_CART': '\u8d2d\u7269\u8f66',
    'N_ORDERS': '\u6211\u7684\u8ba2\u5355',
    'N_ORDER': '\u8ba2\u5355',
    'N_LOGIN': '\u767b\u5f55',
    'N_LOGOUT': '\u9000\u51fa',
    'N_BACK': '\u8fd4\u56de',
    'N_MALL': '\u5546\u57ce',
    'N_MEMBER': '\u4f1a\u5458',
}
for k, v in CN.items():
    LAYOUT = LAYOUT.replace(k, v)

open(path, 'w', encoding='utf-8', newline='\n').write(LAYOUT)
print('layout.vue ok')
