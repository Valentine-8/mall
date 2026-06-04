<template>
  <div class="shop-mine shop-content">
    <div class="user-card" @click="onUserCardClick">
      <div class="avatar">{{ avatarLetter }}</div>
      <div class="info">
        <div class="name">{{ isLogin ? displayName : '点击登录' }}</div>
        <div class="sub">{{ isLogin ? '账号 ' + userName : '登录后享受完整服务' }}</div>
      </div>
      <el-icon class="arrow"><ArrowRight /></el-icon>
    </div>

    <div v-if="isLogin" class="order-panel">
      <div class="panel-head" @click="goOrders()">
        <span>我的订单</span>
        <span class="link">全部订单 <el-icon><ArrowRight /></el-icon></span>
      </div>
      <div class="order-shortcuts">
        <div class="shortcut" @click="goOrders('0')">
          <el-icon><Wallet /></el-icon>
          <span>待付款</span>
        </div>
        <div class="shortcut" @click="goOrders('1')">
          <el-icon><Box /></el-icon>
          <span>待发货</span>
        </div>
        <div class="shortcut" @click="goOrders('2')">
          <el-icon><Van /></el-icon>
          <span>待收货</span>
        </div>
        <div class="shortcut" @click="goOrders('3')">
          <el-icon><CircleCheck /></el-icon>
          <span>已完成</span>
        </div>
        <div class="shortcut" @click="goOrders('afterSale')">
          <el-icon><RefreshLeft /></el-icon>
          <span>退款/取消</span>
        </div>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="goOrders()">
        <el-icon><List /></el-icon>
        <span>我的订单</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="goProfile">
        <el-icon><User /></el-icon>
        <span>我的资料</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="goCart">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <el-button v-if="isLogin" class="logout-btn" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup name="ShopMine">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import { promptShopLogin } from '@/utils/shopAuth'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => !!getToken())
const displayName = computed(() => userStore.nickName || userStore.name || '会员')
const userName = computed(() => userStore.name || '')
const avatarLetter = computed(() => (displayName.value || '客').charAt(0))

onMounted(() => {
  if (getToken() && !userStore.name) {
    userStore.getInfo().catch(() => {})
  }
})

function onUserCardClick() {
  if (!isLogin) {
    promptShopLogin(router, '/shop/mine', { scene: 'default' })
    return
  }
  goProfile()
}

function goOrders(status) {
  if (!getToken()) {
    promptShopLogin(router, status ? `/shop/orders?status=${status}` : '/shop/orders', { scene: 'orders' })
    return
  }
  router.push(status ? { path: '/shop/orders', query: { status } } : '/shop/orders')
}

function goProfile() {
  if (!getToken()) {
    promptShopLogin(router, '/shop/profile', { scene: 'default' })
    return
  }
  router.push('/shop/profile')
}

function goCart() {
  router.push('/shop/cart')
}

function handleLogout() {
  userStore.logOut().then(() => router.replace('/shop/mine'))
}
</script>

<style scoped lang="scss">
.shop-mine { padding: 12px 0 24px; }
.user-card {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-radius: 12px; padding: 20px 16px; color: #fff; margin-bottom: 12px;
}
.avatar {
  width: 52px; height: 52px; border-radius: 50%; background: rgba(255,255,255,0.25);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700;
}
.info { flex: 1; min-width: 0; }
.name { font-size: 18px; font-weight: 600; }
.sub { font-size: 12px; opacity: 0.9; margin-top: 4px; }
.arrow { opacity: 0.85; }
.order-panel {
  background: #fff; border-radius: 12px; padding: 14px 12px; margin-bottom: 12px;
}
.panel-head {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 15px; font-weight: 600; margin-bottom: 14px;
}
.panel-head .link {
  font-size: 12px; color: #999; font-weight: 400;
  display: flex; align-items: center; gap: 2px;
}
.order-shortcuts {
  display: flex; justify-content: space-between; text-align: center;
}
.shortcut {
  flex: 1; font-size: 11px; color: #666; cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: 6px;
}
.shortcut .el-icon { font-size: 22px; color: #ff6b35; }
.menu-list { background: #fff; border-radius: 12px; overflow: hidden; }
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 16px; border-bottom: 1px solid #f5f5f5; font-size: 15px; cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.menu-item .el-icon:first-child { color: #ff6b35; font-size: 20px; }
.menu-item span { flex: 1; }
.logout-btn { width: 100%; margin-top: 20px; }
@media (min-width: 769px) {
  .shop-mine { padding-top: 24px; max-width: 720px; }
}
</style>
