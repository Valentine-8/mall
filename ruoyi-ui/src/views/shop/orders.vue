<template>
  <div class="shop-orders shop-content">
    <h2 class="page-title shop-only-pc">我的订单</h2>
    <el-empty v-if="!isLogin" description="登录后查看订单与物流状态" class="guest-empty">
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </el-empty>
    <template v-else>
      <div class="status-tabs shop-only-mobile">
        <span
          v-for="tab in tabs"
          :key="tab.value"
          class="tab"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >{{ tab.label }}</span>
      </div>
      <div class="status-tabs-pc shop-only-pc">
        <el-radio-group v-model="activeTab" @change="loadOrders">
          <el-radio-button v-for="tab in tabs" :key="tab.value" :value="tab.value">{{ tab.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <div v-loading="loading" class="order-list">
        <div v-for="o in orders" :key="o.orderId" class="order-card" @click="goDetail(o.orderId)">
          <div class="head">
            <span class="sn">{{ o.orderSn }}</span>
            <dict-tag :options="mall_order_status" :value="o.status" />
          </div>
          <div class="amount">应付 ￥{{ o.payAmount }}</div>
          <div class="meta">
            <span>{{ parseTime(o.createTime) }}</span>
            <span v-if="o.deliveryTime" class="ship-tag">已发货</span>
          </div>
          <div class="actions" @click.stop>
            <el-button v-if="o.status === '0'" type="danger" size="small" @click="doPay(o)">去支付</el-button>
            <el-button v-if="o.status === '0'" size="small" @click="doCancel(o)">取消</el-button>
            <el-button v-if="o.status === '2'" type="primary" size="small" @click="doConfirm(o)">确认收货</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && orders.length === 0" description="暂无相关订单" />
      </div>
    </template>
  </div>
</template>

<script setup name="ShopOrders">
import { listMyOrders, payOrder, cancelMyOrder, confirmReceiveOrder } from '@/api/app/order'
import { promptShopLogin } from '@/utils/shopAuth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const { mall_order_status } = useDict('mall_order_status')

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '已完成', value: '3' },
  { label: '退款/取消', value: 'afterSale' }
]

const activeTab = ref(route.query.status != null ? String(route.query.status) : '')
const orders = ref([])
const loading = ref(true)
const isLogin = computed(() => !!userStore.token)

function goLogin() {
  const q = activeTab.value ? `?status=${activeTab.value}` : ''
  promptShopLogin(router, '/shop/orders' + q, { scene: 'orders' })
}

function loadOrders() {
  if (!userStore.token) {
    loading.value = false
    return
  }
  loading.value = true
  const params = activeTab.value ? { status: activeTab.value } : {}
  listMyOrders(params).then(res => {
    orders.value = res.data || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

function switchTab(value) {
  activeTab.value = value
  router.replace({ path: '/shop/orders', query: value ? { status: value } : {} })
  loadOrders()
}

function goDetail(orderId) {
  router.push('/shop/orders/' + orderId)
}

function doPay(o) {
  payOrder(o.orderId).then(() => {
    proxy.$modal.msgSuccess('支付成功')
    loadOrders()
  })
}

function doCancel(o) {
  proxy.$modal.confirm('确认取消该订单？').then(() => cancelMyOrder(o.orderId)).then(() => {
    proxy.$modal.msgSuccess('已取消')
    loadOrders()
  }).catch(() => {})
}

function doConfirm(o) {
  proxy.$modal.confirm('确认已收到商品？').then(() => confirmReceiveOrder(o.orderId)).then(() => {
    proxy.$modal.msgSuccess('已确认收货')
    loadOrders()
  }).catch(() => {})
}

watch(() => route.query.status, (val) => {
  activeTab.value = val != null ? String(val) : ''
  loadOrders()
})

onMounted(loadOrders)
</script>

<style scoped lang="scss">
.shop-orders { padding: 0 0 24px; }
.page-title { margin: 0 0 16px; font-size: 22px; color: #333; }
.status-tabs {
  display: flex; gap: 8px; overflow-x: auto; padding: 10px 0 12px;
  -webkit-overflow-scrolling: touch;
}
.status-tabs .tab {
  flex-shrink: 0; padding: 6px 12px; border-radius: 16px;
  font-size: 13px; background: #fff; color: #666;
}
.status-tabs .tab.active { background: var(--shop-primary-soft); color: var(--shop-primary); font-weight: 600; }
.status-tabs-pc { margin-bottom: 16px; }
.order-card {
  background: #fff; border-radius: 10px; padding: 14px 16px; margin-bottom: 10px; cursor: pointer;
}
.head { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.sn { color: #666; }
.amount { font-size: 18px; font-weight: 700; color: var(--shop-primary); margin: 8px 0 4px; }
.meta { font-size: 12px; color: #999; display: flex; gap: 8px; align-items: center; }
.ship-tag { color: #409eff; }
.actions { margin-top: 10px; display: flex; gap: 8px; justify-content: flex-end; }
.guest-empty { padding: 48px 16px; }
@media (min-width: 769px) {
  .shop-orders { padding-top: 24px; }
  .status-tabs { display: none; }
}
</style>
