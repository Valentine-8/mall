<template>
  <div class="shop-order-detail shop-content" v-loading="loading">
    <el-empty v-if="!isLogin" description="登录后查看订单详情" class="guest-empty">
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </el-empty>
    <template v-else-if="order">
      <div class="status-banner">
        <dict-tag :options="mall_order_status" :value="order.status" />
        <p class="status-tip">{{ statusTip }}</p>
      </div>
      <div class="card timeline-card" v-if="timeline.length">
        <h4>订单进度</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(node, idx) in timeline"
            :key="idx"
            :timestamp="node.time"
            :type="idx === 0 ? 'primary' : ''"
          >{{ node.label }}</el-timeline-item>
        </el-timeline>
      </div>
      <div class="detail-grid">
        <div class="card">
          <div class="sn">订单号 {{ order.orderSn }}</div>
          <div class="amount">￥{{ order.payAmount }}</div>
        </div>
        <div class="card">
          <h4>收货信息</h4>
          <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
          <p class="addr">{{ order.receiverAddress }}</p>
        </div>
      </div>
      <div class="card items-card">
        <h4>商品明细</h4>
        <div v-for="item in order.items" :key="item.itemId" class="line">
          <span>{{ item.productName }} x{{ item.quantity }}</span>
          <span>￥{{ item.totalAmount }}</span>
        </div>
      </div>
      <div class="actions shop-fixed-bar shop-only-mobile" v-if="showActions">
        <el-button v-if="order.status === '0'" type="danger" size="large" @click="doPay">立即支付</el-button>
        <el-button v-if="order.status === '0'" size="large" @click="doCancel">取消订单</el-button>
        <el-button v-if="order.status === '2'" type="primary" size="large" @click="doConfirm">确认收货</el-button>
      </div>
      <div class="actions-pc shop-only-pc" v-if="showActions">
        <el-button v-if="order.status === '0'" type="danger" size="large" @click="doPay">立即支付</el-button>
        <el-button v-if="order.status === '0'" size="large" @click="doCancel">取消订单</el-button>
        <el-button v-if="order.status === '2'" type="primary" size="large" @click="doConfirm">确认收货</el-button>
      </div>
    </template>
  </div>
</template>

<script setup name="ShopOrderDetail">
import { getMyOrder, payOrder, cancelMyOrder, confirmReceiveOrder } from '@/api/app/order'
import { promptShopLogin } from '@/utils/shopAuth'
import { parseTime } from '@/utils/ruoyi'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const { mall_order_status } = useDict('mall_order_status')
const order = ref(null)
const loading = ref(true)
const isLogin = computed(() => !!userStore.token)

const statusTips = {
  '0': '请尽快完成支付',
  '1': '商家正在准备发货',
  '2': '商品已发出，请注意查收',
  '3': '交易已完成',
  '4': '订单已取消',
  '5': '订单已退款，款项将原路退回（模拟）'
}

const statusTip = computed(() => statusTips[order.value?.status] || '')
const showActions = computed(() => order.value && ['0', '2'].includes(order.value.status))

const timeline = computed(() => {
  if (!order.value) return []
  const o = order.value
  const nodes = [{ label: '提交订单', time: parseTime(o.createTime) }]
  if (o.payTime) nodes.unshift({ label: '支付成功', time: parseTime(o.payTime) })
  if (o.deliveryTime) nodes.unshift({ label: '商家已发货', time: parseTime(o.deliveryTime) })
  if (o.finishTime) nodes.unshift({ label: '交易完成', time: parseTime(o.finishTime) })
  if (o.status === '4' && o.cancelTime) nodes.unshift({ label: '订单已取消', time: parseTime(o.cancelTime) })
  if (o.status === '5' && o.cancelTime) nodes.unshift({ label: '已退款', time: parseTime(o.cancelTime) })
  return nodes
})

function goLogin() {
  promptShopLogin(router, route.fullPath, { scene: 'orders' })
}

function load() {
  if (!userStore.token) {
    loading.value = false
    return
  }
  getMyOrder(route.params.orderId).then(res => {
    order.value = res.data
    loading.value = false
  }).catch(() => {
    loading.value = false
    router.replace('/shop/orders')
  })
}

function doPay() {
  payOrder(order.value.orderId).then(() => {
    proxy.$modal.msgSuccess('支付成功')
    load()
  })
}

function doCancel() {
  proxy.$modal.confirm('确认取消该订单？').then(() => cancelMyOrder(order.value.orderId)).then(() => {
    proxy.$modal.msgSuccess('已取消')
    load()
  }).catch(() => {})
}

function doConfirm() {
  proxy.$modal.confirm('确认已收到商品？').then(() => confirmReceiveOrder(order.value.orderId)).then(() => {
    proxy.$modal.msgSuccess('已确认收货')
    load()
  }).catch(() => {})
}

load()
</script>

<style scoped lang="scss">
.shop-order-detail { padding: 12px 0 100px; }
.status-banner {
  background: var(--shop-gradient);
  border-radius: 12px; padding: 16px; color: #fff; margin-bottom: 12px;
}
.status-tip { margin: 8px 0 0; font-size: 13px; opacity: 0.95; }
.card { background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.sn { font-size: 13px; color: #666; margin-bottom: 8px; }
.amount { font-size: 22px; font-weight: 700; color: var(--shop-primary); }
.addr { color: #666; font-size: 14px; }
.line {
  display: flex; justify-content: space-between; padding: 10px 0;
  border-bottom: 1px solid #f5f5f5; font-size: 14px;
}
.timeline-card h4 { margin: 0 0 12px; font-size: 15px; }
.actions {
  display: flex; gap: 10px; padding: 12px 16px; background: #fff;
}
.actions .el-button { flex: 1; }
.actions-pc { display: flex; gap: 12px; padding: 8px 0 24px; }
@media (min-width: 769px) {
  .shop-order-detail { padding-top: 24px; padding-bottom: 40px; }
  .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .detail-grid .card { margin-bottom: 0; }
}
.guest-empty { padding: 48px 16px; }
</style>
