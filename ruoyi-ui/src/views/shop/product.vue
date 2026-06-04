<template>
  <div class="shop-product shop-content" v-loading="loading">
    <template v-if="product">
      <div class="product-layout">
        <div class="hero-pic">{{ product.productName?.charAt(0) }}</div>
        <div class="detail-card">
          <div class="price">￥{{ product.price }}</div>
          <h2>{{ product.productName }}</h2>
          <p class="meta">库存 {{ product.stock }} &middot; 销量 {{ product.saleCount || 0 }}</p>
          <p class="desc">{{ product.description || '暂无详细描述' }}</p>
          <div class="qty-row"><span>数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" /></div>
          <div class="action-row shop-only-pc">
            <el-button type="warning" plain size="large" @click="addCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>
      <div class="bottom-bar shop-fixed-bar shop-only-mobile">
        <el-button type="warning" plain @click="addCart">加入购物车</el-button>
        <el-button type="danger" @click="buyNow">立即购买</el-button>
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
    proxy.$modal.msgError('商品不存在或已下架')
    router.replace('/shop/home')
  })
}
async function addCart() {
  if (!(await ensureShopLogin(router, route.fullPath, { scene: 'addCart' }))) return
  addToCart(product.value.productId, quantity.value).then(() => proxy.$modal.msgSuccess('已加入购物车'))
}
async function buyNow() {
  const checkoutPath = buildBuyNowCheckoutPath(product.value.productId, quantity.value)
  if (!(await ensureShopLogin(router, checkoutPath, { scene: 'buyNow' }))) return
  try {
    const cartIds = await prepareBuyNowCartIds(product.value.productId, quantity.value)
    router.push({ path: '/shop/checkout', query: { cartIds: cartIds.join(',') } })
  } catch (e) {
    proxy.$modal.msgError(e.message || '无法进入结算')
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
