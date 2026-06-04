<template>
  <div class="shop-checkout shop-content">
    <h2 class="page-title shop-only-pc">提交订单</h2>
    <el-empty v-if="!isLogin" description="请先登录后再结算订单" class="guest-empty">
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </el-empty>
    <div v-else-if="preparing" class="preparing" v-loading="true" element-loading-text="正在准备订单..." />
    <el-empty v-else-if="!ready" description="没有可结算的商品">
      <el-button type="primary" @click="goCart">去购物车</el-button>
      <el-button @click="goHome">去逛逛</el-button>
    </el-empty>
    <template v-else>
    <div v-if="settleItems.length" class="order-preview">
      <h3>商品清单</h3>
      <div v-for="item in settleItems" :key="item.cartId" class="preview-item">
        <span class="name">{{ item.productName }}</span>
        <span class="meta">x{{ item.quantity }} · ￥{{ item.price }}</span>
      </div>
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="checkout-form">
      <div class="section">
        <h3>收货信息</h3>
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="详细地址" prop="receiverAddress">
          <el-input v-model="form.receiverAddress" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="选填" />
        </el-form-item>
      </div>
    </el-form>
    <div class="submit-bar shop-fixed-bar shop-only-mobile">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder" style="width:100%">提交订单</el-button>
    </div>
    <div class="submit-panel shop-only-pc">
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">提交订单</el-button>
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
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '请输入地址', trigger: 'blur' }]
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
    proxy.$modal.msgError(e.message || '加载结算信息失败')
  } finally {
    preparing.value = false
  }
}

function submitOrder() {
  if (!form.value.cartIds.length) {
    proxy.$modal.msgWarning('请先选择要结算的商品')
    return
  }
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return
    submitting.value = true
    checkoutOrder(form.value).then(res => {
      const order = res.data
      proxy.$modal.confirm('订单已创建，是否立即支付？').then(() => payOrder(order.orderId)).then(() => {
        proxy.$modal.msgSuccess('支付成功')
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
