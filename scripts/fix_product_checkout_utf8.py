# -*- coding: utf-8 -*-
"""Rewrite shop product.vue and checkout.vue with correct UTF-8 Chinese."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / 'ruoyi-ui' / 'src' / 'views' / 'shop'


def write_product() -> None:
    p = ROOT / 'product.vue'
    p.write_text(
        '''<template>
  <div class="shop-product shop-content" v-loading="loading">
    <template v-if="product">
      <div class="product-layout">
        <div class="hero-pic">{{ product.productName?.charAt(0) }}</div>
        <div class="detail-card">
          <div class="price">\uFFE5{{ product.price }}</div>
          <h2>{{ product.productName }}</h2>
          <p class="meta">\u5E93\u5B58 {{ product.stock }} &middot; \u9500\u91CF {{ product.saleCount || 0 }}</p>
          <p class="desc">{{ product.description || '\u6682\u65E0\u8BE6\u7EC6\u63CF\u8FF0' }}</p>
          <div class="qty-row"><span>\u6570\u91CF</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" /></div>
          <div class="action-row shop-only-pc">
            <el-button type="warning" plain size="large" @click="addCart">\u52A0\u5165\u8D2D\u7269\u8F66</el-button>
            <el-button type="danger" size="large" @click="buyNow">\u7ACB\u5373\u8D2D\u4E70</el-button>
          </div>
        </div>
      </div>
      <div class="bottom-bar shop-fixed-bar shop-only-mobile">
        <el-button type="warning" plain @click="addCart">\u52A0\u5165\u8D2D\u7269\u8F66</el-button>
        <el-button type="danger" @click="buyNow">\u7ACB\u5373\u8D2D\u4E70</el-button>
      </div>
    </template>
  </div>
</template>
<script setup name="ShopProduct">
import { getAppProduct } from '@/api/app/product'
import { addToCart } from '@/api/app/cart'
import { ensureShopLogin } from '@/utils/shopAuth'
import { buildBuyNowCheckoutPath, prepareBuyNowCartIds } from '@/utils/shopCheckout'
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
    proxy.$modal.msgError('\u5546\u54C1\u4E0D\u5B58\u5728\u6216\u5DF2\u4E0B\u67B6')
    router.replace('/shop/home')
  })
}
async function addCart() {
  if (!(await ensureShopLogin(router, route.fullPath, { scene: 'addCart' }))) return
  addToCart(product.value.productId, quantity.value).then(() => proxy.$modal.msgSuccess('\u5DF2\u52A0\u5165\u8D2D\u7269\u8F66'))
}
async function buyNow() {
  const checkoutPath = buildBuyNowCheckoutPath(product.value.productId, quantity.value)
  if (!(await ensureShopLogin(router, checkoutPath, { scene: 'buyNow' }))) return
  try {
    const cartIds = await prepareBuyNowCartIds(product.value.productId, quantity.value)
    router.push({ path: '/shop/checkout', query: { cartIds: cartIds.join(',') } })
  } catch (e) {
    proxy.$modal.msgError(e.message || '\u65E0\u6CD5\u8FDB\u5165\u7ED3\u7B97')
  }
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
''',
        encoding='utf-8',
        newline='\n',
    )


def write_checkout() -> None:
    p = ROOT / 'checkout.vue'
    p.write_text(
        '''<template>
  <div class="shop-checkout shop-content">
    <h2 class="page-title shop-only-pc">\u63D0\u4EA4\u8BA2\u5355</h2>
    <el-empty v-if="!isLogin" description="\u8BF7\u5148\u767B\u5F55\u540E\u518D\u7ED3\u7B97\u8BA2\u5355" class="guest-empty">
      <el-button type="primary" @click="goLogin">\u53BB\u767B\u5F55</el-button>
    </el-empty>
    <div v-else-if="preparing" class="preparing" v-loading="true" element-loading-text="\u6B63\u5728\u51C6\u5907\u8BA2\u5355..." />
    <el-empty v-else-if="!ready" description="\u6CA1\u6709\u53EF\u7ED3\u7B97\u7684\u5546\u54C1">
      <el-button type="primary" @click="goCart">\u53BB\u8D2D\u7269\u8F66</el-button>
      <el-button @click="goHome">\u53BB\u901B\u901B</el-button>
    </el-empty>
    <template v-else>
    <div v-if="settleItems.length" class="order-preview">
      <h3>\u5546\u54C1\u6E05\u5355</h3>
      <div v-for="item in settleItems" :key="item.cartId" class="preview-item">
        <span class="name">{{ item.productName }}</span>
        <span class="meta">x{{ item.quantity }} \u00B7 \uFFE5{{ item.price }}</span>
      </div>
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="checkout-form">
      <div class="section">
        <h3>\u6536\u8D27\u4FE1\u606F</h3>
        <el-form-item label="\u6536\u8D27\u4EBA" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="\u8BF7\u8F93\u5165\u6536\u8D27\u4EBA" />
        </el-form-item>
        <el-form-item label="\u624B\u673A\u53F7" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="\u8BF7\u8F93\u5165\u624B\u673A\u53F7" />
        </el-form-item>
        <el-form-item label="\u8BE6\u7EC6\u5730\u5740" prop="receiverAddress">
          <el-input v-model="form.receiverAddress" type="textarea" :rows="2" placeholder="\u8BF7\u8F93\u5165\u5730\u5740" />
        </el-form-item>
        <el-form-item label="\u5907\u6CE8">
          <el-input v-model="form.remark" placeholder="\u9009\u586B" />
        </el-form-item>
      </div>
    </el-form>
    <div class="submit-bar shop-fixed-bar shop-only-mobile">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder" style="width:100%">\u63D0\u4EA4\u8BA2\u5355</el-button>
    </div>
    <div class="submit-panel shop-only-pc">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">\u63D0\u4EA4\u8BA2\u5355</el-button>
    </div>
    </template>
  </div>
</template>
<script setup name="ShopCheckout">
import { checkoutOrder, payOrder } from '@/api/app/order'
import { listCart } from '@/api/app/cart'
import { getToken } from '@/utils/auth'
import { promptShopLogin } from '@/utils/shopAuth'
import { prepareBuyNowCartIds, parseCartIdsFromQuery, buildBuyNowCheckoutPath } from '@/utils/shopCheckout'
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const submitting = ref(false)
const preparing = ref(false)
const ready = ref(false)
const settleItems = ref([])
const isLogin = computed(() => !!getToken())
function goLogin() {
  const redirect = route.query.buyNow === '1' && route.query.productId
    ? buildBuyNowCheckoutPath(route.query.productId, route.query.quantity)
    : route.fullPath
  promptShopLogin(router, redirect, { scene: 'checkout' })
}
function goCart() { router.replace('/shop/cart') }
function goHome() { router.replace('/shop/home') }
const form = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '', cartIds: [] })
const rules = {
  receiverName: [{ required: true, message: '\u8BF7\u8F93\u5165\u6536\u8D27\u4EBA', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '\u8BF7\u8F93\u5165\u624B\u673A\u53F7', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '\u8BF7\u8F93\u5165\u5730\u5740', trigger: 'blur' }]
}

async function loadSettlePreview() {
  const res = await listCart()
  const all = res.data || []
  const idSet = new Set(form.value.cartIds)
  settleItems.value = all.filter(i => idSet.has(i.cartId))
}

async function initCheckout() {
  if (!getToken()) {
    ready.value = false
    return
  }
  preparing.value = true
  ready.value = false
  try {
    if (route.query.buyNow === '1' && route.query.productId) {
      form.value.cartIds = await prepareBuyNowCartIds(route.query.productId, route.query.quantity)
      router.replace({ path: '/shop/checkout', query: { cartIds: form.value.cartIds.join(',') } })
    } else {
      form.value.cartIds = parseCartIdsFromQuery(route.query.cartIds)
      if (!form.value.cartIds.length) {
        const res = await listCart()
        form.value.cartIds = (res.data || []).filter(i => i.checked === '1').map(i => i.cartId)
      }
    }
    if (!form.value.cartIds.length) {
      ready.value = false
      return
    }
    await loadSettlePreview()
    ready.value = settleItems.value.length > 0
  } catch (e) {
    ready.value = false
    proxy.$modal.msgError(e.message || '\u52A0\u8F7D\u7ED3\u7B97\u4FE1\u606F\u5931\u8D25')
  } finally {
    preparing.value = false
  }
}

function submitOrder() {
  if (!form.value.cartIds.length) {
    proxy.$modal.msgWarning('\u8BF7\u5148\u9009\u62E9\u8981\u7ED3\u7B97\u7684\u5546\u54C1')
    return
  }
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    submitting.value = true
    checkoutOrder(form.value).then(res => {
      const order = res.data
      proxy.$modal.confirm('\u8BA2\u5355\u5DF2\u521B\u5EFA\uFF0C\u662F\u5426\u7ACB\u5373\u652F\u4ED8\uFF1F').then(() => payOrder(order.orderId)).then(() => {
        proxy.$modal.msgSuccess('\u652F\u4ED8\u6210\u529F')
        router.replace('/shop/orders/' + order.orderId)
      }).catch(() => router.replace('/shop/orders/' + order.orderId))
        .finally(() => { submitting.value = false })
    }).catch(() => { submitting.value = false })
  })
}

watch(() => route.fullPath, () => initCheckout(), { immediate: false })
onMounted(() => initCheckout())
</script>
<style scoped lang="scss">
.shop-checkout { padding: 12px 0 100px; }
.page-title { margin: 0 0 20px; font-size: 22px; }
.order-preview {
  background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 12px;
}
.order-preview h3 { margin: 0 0 12px; font-size: 16px; }
.preview-item {
  display: flex; justify-content: space-between; gap: 12px;
  padding: 8px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px;
}
.preview-item:last-child { border-bottom: none; }
.preview-item .name { flex: 1; color: #333; }
.preview-item .meta { color: #ff6b35; flex-shrink: 0; }
.section { background: #fff; border-radius: 10px; padding: 16px; }
.section h3 { margin: 0 0 12px; font-size: 16px; }
.submit-bar { padding: 12px 16px; background: #fff; }
.submit-panel { margin-top: 20px; padding: 20px; background: #fff; border-radius: 8px; text-align: right; }
.preparing { min-height: 200px; }
@media (min-width: 769px) {
  .shop-checkout { padding: 24px 0 40px; }
  .checkout-form { max-width: 640px; }
}
.guest-empty { padding: 48px 16px; }
</style>
''',
        encoding='utf-8',
        newline='\n',
    )


def main() -> None:
    write_product()
    write_checkout()
    product = (ROOT / 'product.vue').read_text(encoding='utf-8')
    checkout = (ROOT / 'checkout.vue').read_text(encoding='utf-8')
    assert '加入购物车' in product, product[200:400]
    assert '提交订单' in checkout
    print('ok')


if __name__ == '__main__':
    main()
