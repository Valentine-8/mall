<template>
  <div class="shop-mine shop-content">
    <h2 class="page-title shop-only-pc">个人中心</h2>
    <div class="mine-body">
      <aside class="mine-sidebar shop-only-pc">
        <div class="sidebar-user" @click="onUserCardClick">
          <div class="avatar-lg">{{ avatarLetter }}</div>
          <div class="name">{{ isLogin ? displayName : '点击登录' }}</div>
          <div class="sub">{{ isLogin ? '账号 ' + userName : '登录后享受完整服务' }}</div>
        </div>
        <nav class="sidebar-nav">
          <div class="nav-item" :class="{ active: isOrdersActive }" @click="goOrders()">我的订单</div>
          <div class="nav-item" :class="{ active: isProfileActive }" @click="goProfile">我的资料</div>
          <div class="nav-item" @click="goCart">购物车</div>
        </nav>
        <el-button v-if="isLogin" class="sidebar-logout" @click="handleLogout">退出登录</el-button>
      </aside>

      <div class="mine-main">
        <div class="user-card shop-only-mobile" @click="onUserCardClick">
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

        <div class="menu-list shop-only-mobile">
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
          <div class="menu-item theme-row">
            <el-icon><Brush /></el-icon>
            <span>主题色</span>
            <shop-theme-picker title="选择主题色" />
          </div>
        </div>

        <div v-if="!isLogin" class="guest-panel shop-only-pc">
          <el-empty description="登录后查看订单、管理资料与购物车">
            <el-button type="primary" @click="onUserCardClick">去登录</el-button>
          </el-empty>
        </div>

        <el-button v-if="isLogin" class="logout-btn shop-only-mobile" @click="handleLogout">退出登录</el-button>

        <div class="theme-panel shop-only-pc shop-card">
          <div class="theme-panel-head">外观设置</div>
          <shop-theme-picker title="商城主题色" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ShopMine">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ShopThemePicker from '@/components/ShopThemePicker/index.vue'
import { promptShopLogin } from '@/utils/shopAuth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => !!userStore.token)
const displayName = computed(() => userStore.nickName || userStore.name || '会员')
const userName = computed(() => userStore.name || '')
const avatarLetter = computed(() => (displayName.value || '客').charAt(0))
const isOrdersActive = computed(() => route.path.startsWith('/shop/orders'))
const isProfileActive = computed(() => route.path === '/shop/profile')

onMounted(() => {
  if (userStore.token && !userStore.name) {
    userStore.getInfo().catch(() => {})
  }
})

function onUserCardClick() {
  if (!isLogin.value) {
    promptShopLogin(router, '/shop/mine', { scene: 'default' })
    return
  }
  goProfile()
}

function goOrders(status) {
  if (!userStore.token) {
    promptShopLogin(router, status ? `/shop/orders?status=${status}` : '/shop/orders', { scene: 'orders' })
    return
  }
  router.push(status ? { path: '/shop/orders', query: { status } } : '/shop/orders')
}

function goProfile() {
  if (!userStore.token) {
    promptShopLogin(router, '/shop/profile', { scene: 'default' })
    return
  }
  router.push('/shop/profile')
}

function goCart() {
  router.push('/shop/cart')
}

function handleLogout() {
  userStore.logOut().then(() => {
    ElMessage.success('已退出登录')
  })
}
</script>

<style scoped lang="scss">
.shop-mine { padding: 12px 0 24px; }
.page-title { margin: 0 0 16px; font-size: 22px; color: #333; }
.mine-body { display: block; }

.user-card {
  display: flex; align-items: center; gap: 14px;
  background: var(--shop-gradient);
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
  font-size: 15px; font-weight: 600; margin-bottom: 14px; cursor: pointer;
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
.shortcut .el-icon { font-size: 22px; color: var(--shop-primary); }

.menu-list { background: #fff; border-radius: 12px; overflow: hidden; }
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 16px; border-bottom: 1px solid #f5f5f5; font-size: 15px; cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.menu-item .el-icon:first-child { color: var(--shop-primary); font-size: 20px; }
.menu-item span { flex: 1; }
.menu-item.theme-row {
  cursor: default;
  .arrow { display: none; }
}
.theme-panel {
  margin-top: 16px;
  padding: 20px 24px;
}
.theme-panel-head {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 14px;
}
.logout-btn { width: 100%; margin-top: 20px; }

.guest-panel {
  background: #fff;
  border-radius: 8px;
  padding: 48px 24px;
}

@media (min-width: 769px) {
  .shop-mine { padding: 24px 0 40px; }
  .mine-body {
    display: grid;
    grid-template-columns: 220px 1fr;
    gap: 20px;
    align-items: start;
  }
  .mine-sidebar {
    background: #fff;
    border-radius: 8px;
    padding: 24px 16px;
    position: sticky;
    top: 72px;
  }
  .sidebar-user {
    text-align: center;
    cursor: pointer;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
  }
  .avatar-lg {
    width: 72px;
    height: 72px;
    margin: 0 auto 12px;
    border-radius: 50%;
    background: var(--shop-gradient);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 700;
  }
  .sidebar-user .name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  .sidebar-user .sub {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
  .sidebar-nav { margin-top: 8px; }
  .nav-item {
    padding: 12px 16px;
    border-radius: 6px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
  }
  .nav-item:hover {
    color: var(--shop-primary);
    background: var(--shop-primary-muted);
  }
  .nav-item.active {
    color: var(--shop-primary);
    background: var(--shop-primary-soft);
    font-weight: 600;
  }
  .sidebar-logout {
    width: 100%;
    margin-top: 16px;
  }
  .mine-main { min-width: 0; }
  .order-panel {
    border-radius: 8px;
    padding: 20px 24px;
    margin-bottom: 0;
  }
  .panel-head { font-size: 16px; margin-bottom: 20px; }
  .order-shortcuts { gap: 8px; }
  .shortcut {
    font-size: 13px;
    padding: 12px 8px;
    border-radius: 8px;
    transition: background 0.2s;
  }
  .shortcut:hover { background: var(--shop-primary-muted); }
  .shortcut .el-icon { font-size: 28px; }
}
</style>
