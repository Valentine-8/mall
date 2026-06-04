# -*- coding: utf-8 -*-
import os

VIEWS = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views', 'shop')

CN = {
    'YEN': '\uffe5',
    'N_STOCK': '\u5e93\u5b58',
    'N_SALE': '\u9500\u91cf',
    'N_DESC': '\u6682\u65e0\u63cf\u8ff0',
    'N_QTY': '\u6570\u91cf',
    'N_ADD_CART': '\u52a0\u5165\u8d2d\u7269\u8f66',
    'N_BUY': '\u7acb\u5373\u8d2d\u4e70',
    'N_NO_PRODUCT': '\u5546\u54c1\u4e0d\u5b58\u5728',
    'N_ADDED': '\u5df2\u52a0\u5165\u8d2d\u7269\u8f66',
    'N_RECV': '\u6536\u8d27\u4fe1\u606f',
    'N_RECV_NAME': '\u6536\u8d27\u4eba',
    'N_PHONE': '\u624b\u673a\u53f7',
    'N_ADDR': '\u8be6\u7ec6\u5730\u5740',
    'N_REMARK': '\u5907\u6ce8',
    'N_OPT': '\u9009\u586b',
    'N_SUBMIT': '\u63d0\u4ea4\u8ba2\u5355',
    'N_PAY_ASK': '\u8ba2\u5355\u5df2\u521b\u5efa\uff0c\u662f\u5426\u7acb\u5373\u652f\u4ed8\uff1f',
    'N_PAY_OK': '\u652f\u4ed8\u6210\u529f',
    'N_MY_ORDERS': '\u6211\u7684\u8ba2\u5355',
    'N_PAYABLE': '\u5e94\u4ed8',
    'N_PAY': '\u53bb\u652f\u4ed8',
    'N_CANCEL': '\u53d6\u6d88',
    'N_CANCEL_ASK': '\u786e\u8ba4\u53d6\u6d88\u8be5\u8ba2\u5355\uff1f',
    'N_CANCELED': '\u5df2\u53d6\u6d88',
    'N_EMPTY_ORD': '\u6682\u65e0\u8ba2\u5355',
    'N_ORD_SN': '\u8ba2\u5355\u53f7',
    'N_RECV_INFO': '\u6536\u8d27\u4fe1\u606f',
    'N_ITEMS': '\u5546\u54c1\u660e\u7ec6',
    'N_PAY_NOW': '\u7acb\u5373\u652f\u4ed8',
    'N_CANCEL_ORD': '\u53d6\u6d88\u8ba2\u5355',
    'PH_NAME': '\u8bf7\u8f93\u5165\u6536\u8d27\u4eba',
    'PH_PHONE': '\u8bf7\u8f93\u5165\u624b\u673a\u53f7',
    'PH_ADDR': '\u8bf7\u8f93\u5165\u5730\u5740',
    'MSG_NAME': '\u8bf7\u8f93\u5165\u6536\u8d27\u4eba',
    'MSG_PHONE': '\u8bf7\u8f93\u5165\u624b\u673a\u53f7',
    'MSG_ADDR': '\u8bf7\u8f93\u5165\u5730\u5740',
}

def r(s):
    for k, v in sorted(CN.items(), key=lambda x: -len(x[0])):
        s = s.replace(k, v)
    return s

PRODUCT = r(r'''<template>
  <div class="shop-product shop-content" v-loading="loading">
    <template v-if="product">
      <div class="product-layout">
        <div class="hero-pic">{{ product.productName?.charAt(0) }}</div>
        <div class="detail-card">
          <div class="price">YEN{{ product.price }}</div>
          <h2>{{ product.productName }}</h2>
          <p class="meta">N_STOCK {{ product.stock }} &middot; N_SALE {{ product.saleCount || 0 }}</p>
          <p class="desc">{{ product.description || 'N_DESC' }}</p>
          <div class="qty-row"><span>N_QTY</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" /></div>
          <div class="action-row shop-only-pc">
            <el-button type="warning" plain size="large" @click="addCart">N_ADD_CART</el-button>
            <el-button type="danger" size="large" @click="buyNow">N_BUY</el-button>
          </div>
        </div>
      </div>
      <div class="bottom-bar shop-fixed-bar shop-only-mobile">
        <el-button type="warning" plain @click="addCart">N_ADD_CART</el-button>
        <el-button type="danger" @click="buyNow">N_BUY</el-button>
      </div>
    </template>
  </div>
</template>
<script setup name="ShopProduct">
import { getAppProduct } from '@/api/app/product'
import { addToCart } from '@/api/app/cart'
import { ensureShopLogin } from '@/utils/shopAuth'
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
function loadProduct() {
  loading.value = true
  getAppProduct(route.params.productId).then(res => {
    product.value = res.data
    loading.value = false
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError('N_NO_PRODUCT')
    router.replace('/shop/home')
  })
}
async function addCart() {
  if (!(await ensureShopLogin(router, route.fullPath, { scene: 'addCart' }))) return
  addToCart(product.value.productId, quantity.value).then(() => proxy.$modal.msgSuccess('N_ADDED'))
}
async function buyNow() {
  if (!(await ensureShopLogin(router, '/shop/checkout', { scene: 'buyNow' }))) return
  addToCart(product.value.productId, quantity.value).then(() => router.push('/shop/checkout'))
}
loadProduct()
</script>
<style scoped lang="scss">
.shop-product { padding: 12px 0 80px; }
.product-layout { display: block; }
.hero-pic {
  height: 260px; background: linear-gradient(145deg, #ffe8de, #fff5f0);
  display: flex; align-items: center; justify-content: center;
  font-size: 72px; color: #ff6b35; font-weight: 700; border-radius: 12px;
}
.detail-card { margin: -16px 0 0; background: #fff; border-radius: 12px; padding: 16px; }
.price { color: #ff6b35; font-size: 24px; font-weight: 700; }
.detail-card h2 { margin: 8px 0; font-size: 18px; }
.meta { color: #999; font-size: 13px; }
.desc { color: #666; font-size: 14px; line-height: 1.6; margin-top: 12px; }
.qty-row {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;
}
.action-row { display: flex; gap: 12px; margin-top: 24px; }
.action-row .el-button { min-width: 140px; }
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: 10px; padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff; box-shadow: 0 -2px 12px rgba(0,0,0,0.06); z-index: 15;
}
.bottom-bar .el-button { flex: 1; }
@media (min-width: 769px) {
  .shop-product { padding: 24px 0 40px; }
  .product-layout {
    display: grid; grid-template-columns: 420px 1fr; gap: 32px;
    background: #fff; border-radius: 8px; padding: 24px;
  }
  .hero-pic { height: 420px; margin: 0; }
  .detail-card { margin: 0; padding: 0; }
}
</style>
''')

CHECKOUT = r(r'''<template>
  <div class="shop-checkout shop-content">
    <h2 class="page-title shop-only-pc">N_SUBMIT</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="checkout-form">
      <div class="section">
        <h3>N_RECV</h3>
        <el-form-item label="N_RECV_NAME" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="PH_NAME" />
        </el-form-item>
        <el-form-item label="N_PHONE" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="PH_PHONE" />
        </el-form-item>
        <el-form-item label="N_ADDR" prop="receiverAddress">
          <el-input v-model="form.receiverAddress" type="textarea" :rows="2" placeholder="PH_ADDR" />
        </el-form-item>
        <el-form-item label="N_REMARK">
          <el-input v-model="form.remark" placeholder="N_OPT" />
        </el-form-item>
      </div>
    </el-form>
    <div class="submit-bar shop-fixed-bar shop-only-mobile">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder" style="width:100%">N_SUBMIT</el-button>
    </div>
    <div class="submit-panel shop-only-pc">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">N_SUBMIT</el-button>
    </div>
  </div>
</template>
<script setup name="ShopCheckout">
import { checkoutOrder, payOrder } from '@/api/app/order'
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const submitting = ref(false)
const form = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '', cartIds: [] })
const rules = {
  receiverName: [{ required: true, message: 'MSG_NAME', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: 'MSG_PHONE', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: 'MSG_ADDR', trigger: 'blur' }]
}
if (route.query.cartIds) {
  form.value.cartIds = route.query.cartIds.split(',').map(Number).filter(Boolean)
}
function submitOrder() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    submitting.value = true
    checkoutOrder(form.value).then(res => {
      const order = res.data
      proxy.$modal.confirm('N_PAY_ASK').then(() => payOrder(order.orderId)).then(() => {
        proxy.$modal.msgSuccess('N_PAY_OK')
        router.replace('/shop/orders/' + order.orderId)
      }).catch(() => router.replace('/shop/orders/' + order.orderId))
        .finally(() => { submitting.value = false })
    }).catch(() => { submitting.value = false })
  })
}
</script>
<style scoped lang="scss">
.shop-checkout { padding: 12px 0 100px; }
.page-title { margin: 0 0 20px; font-size: 22px; }
.section { background: #fff; border-radius: 10px; padding: 16px; }
.section h3 { margin: 0 0 12px; font-size: 16px; }
.submit-bar { padding: 12px 16px; background: #fff; }
.submit-panel { margin-top: 20px; padding: 20px; background: #fff; border-radius: 8px; text-align: right; }
@media (min-width: 769px) {
  .shop-checkout { padding: 24px 0 40px; }
  .checkout-form { max-width: 640px; }
}
</style>
''')

ORDERS = r(r'''<template>
  <div class="shop-orders shop-content">
    <h2 class="page-title shop-only-pc">N_MY_ORDERS</h2>
    <div v-loading="loading">
      <div v-for="o in orders" :key="o.orderId" class="order-card" @click="goDetail(o.orderId)">
        <div class="head"><span>{{ o.orderSn }}</span><dict-tag :options="mall_order_status" :value="o.status" /></div>
        <div class="amount">N_PAYABLE YEN{{ o.payAmount }}</div>
        <div class="time">{{ parseTime(o.createTime) }}</div>
        <div class="actions" @click.stop>
          <el-button v-if="o.status === '0'" type="danger" size="small" @click="doPay(o)">N_PAY</el-button>
          <el-button v-if="o.status === '0'" size="small" @click="doCancel(o)">N_CANCEL</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && orders.length === 0" description="N_EMPTY_ORD" />
    </div>
  </div>
</template>
<script setup name="ShopOrders">
import { listMyOrders, payOrder, cancelMyOrder } from '@/api/app/order'
const router = useRouter()
const { proxy } = getCurrentInstance()
const { mall_order_status } = useDict('mall_order_status')
const orders = ref([])
const loading = ref(true)
function loadOrders() {
  loading.value = true
  listMyOrders().then(res => { orders.value = res.data || []; loading.value = false })
    .catch(() => { loading.value = false })
}
function goDetail(orderId) { router.push('/shop/orders/' + orderId) }
function doPay(o) {
  payOrder(o.orderId).then(() => { proxy.$modal.msgSuccess('N_PAY_OK'); loadOrders() })
}
function doCancel(o) {
  proxy.$modal.confirm('N_CANCEL_ASK').then(() => cancelMyOrder(o.orderId)).then(() => {
    proxy.$modal.msgSuccess('N_CANCELED'); loadOrders()
  }).catch(() => {})
}
loadOrders()
</script>
<style scoped lang="scss">
.shop-orders { padding: 12px 0 24px; }
.page-title { margin: 0 0 20px; font-size: 22px; color: #333; }
.order-card {
  background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 12px; cursor: pointer;
}
.order-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.head { display: flex; justify-content: space-between; font-size: 13px; color: #666; }
.amount { font-size: 18px; font-weight: 700; color: #ff6b35; margin: 8px 0; }
.time { font-size: 12px; color: #999; }
.actions { margin-top: 10px; display: flex; gap: 8px; justify-content: flex-end; }
@media (min-width: 769px) {
  .shop-orders { padding-top: 24px; }
  .order-card { padding: 20px 24px; }
}
</style>
''')

ORDER_DETAIL = r(r'''<template>
  <div class="shop-order-detail shop-content" v-loading="loading">
    <template v-if="order">
      <div class="detail-grid">
        <div class="card">
          <div class="sn">N_ORD_SN {{ order.orderSn }}</div>
          <dict-tag :options="mall_order_status" :value="order.status" />
          <div class="amount">YEN{{ order.payAmount }}</div>
        </div>
        <div class="card">
          <h4>N_RECV_INFO</h4>
          <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
          <p class="addr">{{ order.receiverAddress }}</p>
        </div>
      </div>
      <div class="card items-card">
        <h4>N_ITEMS</h4>
        <div v-for="item in order.items" :key="item.itemId" class="line">
          <span>{{ item.productName }} x{{ item.quantity }}</span>
          <span>YEN{{ item.totalAmount }}</span>
        </div>
      </div>
      <div class="actions" v-if="order.status === '0'">
        <el-button type="danger" size="large" @click="doPay">N_PAY_NOW</el-button>
        <el-button size="large" @click="doCancel">N_CANCEL_ORD</el-button>
      </div>
    </template>
  </div>
</template>
<script setup name="ShopOrderDetail">
import { getMyOrder, payOrder, cancelMyOrder } from '@/api/app/order'
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const { mall_order_status } = useDict('mall_order_status')
const order = ref(null)
const loading = ref(true)
function load() {
  getMyOrder(route.params.orderId).then(res => { order.value = res.data; loading.value = false })
    .catch(() => { loading.value = false; router.replace('/shop/orders') })
}
function doPay() {
  payOrder(order.value.orderId).then(() => { proxy.$modal.msgSuccess('N_PAY_OK'); load() })
}
function doCancel() {
  proxy.$modal.confirm('N_CANCEL_ASK').then(() => cancelMyOrder(order.value.orderId)).then(() => {
    proxy.$modal.msgSuccess('N_CANCELED'); load()
  }).catch(() => {})
}
load()
</script>
<style scoped lang="scss">
.shop-order-detail { padding: 12px 0 24px; }
.card { background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.sn { font-size: 13px; color: #666; margin-bottom: 8px; }
.amount { font-size: 22px; font-weight: 700; color: #ff6b35; margin-top: 8px; }
.addr { color: #666; font-size: 14px; }
.line {
  display: flex; justify-content: space-between; padding: 10px 0;
  border-bottom: 1px solid #f5f5f5; font-size: 14px;
}
.actions { display: flex; gap: 12px; padding: 16px 0; }
.actions .el-button { flex: 1; }
@media (min-width: 769px) {
  .shop-order-detail { padding-top: 24px; }
  .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .detail-grid .card { margin-bottom: 0; }
  .items-card { margin-top: 16px; }
  .actions { max-width: 400px; margin-left: auto; }
  .actions .el-button { flex: none; min-width: 120px; }
}
</style>
''')

for name, content in [
    ('product.vue', PRODUCT),
    ('checkout.vue', CHECKOUT),
    ('orders.vue', ORDERS),
    ('order-detail.vue', ORDER_DETAIL),
]:
    open(os.path.join(VIEWS, name), 'w', encoding='utf-8', newline='\n').write(content)
print('part2 ok')
