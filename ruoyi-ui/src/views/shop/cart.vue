<template>
  <div class="shop-cart shop-content">
    <h2 class="page-title shop-only-pc">购物车</h2>
    <el-empty v-if="!isLogin" description="登录后查看和管理购物车" class="guest-empty">
      <el-button type="primary" @click="goLogin">去登录</el-button>
      <el-button @click="router.push('/shop/home')">去逛逛</el-button>
    </el-empty>
    <div v-else v-loading="loading" class="cart-list">
      <div v-for="item in cartList" :key="item.cartId" class="cart-item">
        <el-checkbox :model-value="item.checked === '1'"
          @change="val => { item.checked = val ? '1' : '0'; onCheck(item) }" />
        <div class="item-body" @click="goProduct(item.productId)">
          <div class="pic">{{ item.productName?.charAt(0) }}</div>
          <div class="info">
            <div class="name">{{ item.productName }}</div>
            <div class="price">￥{{ item.price }}</div>
            <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small"
              @change="qty => changeQty(item, qty)" />
          </div>
        </div>
        <el-button link type="danger" @click="removeItem(item)">删除</el-button>
      </div>
      <el-empty v-if="!loading && cartList.length === 0" description="购物车是空的">
        <el-button type="primary" @click="$router.push('/shop/home')">去逛逛</el-button>
      </el-empty>
    </div>
    <div class="settle-bar shop-fixed-bar shop-only-mobile" v-if="isLogin && cartList.length">
      <el-checkbox :model-value="allChecked" @change="toggleAll">全部</el-checkbox>
      <div class="total">合计：<span>￥{{ totalAmount }}</span></div>
      <el-button type="danger" :disabled="checkedCount === 0" @click="goCheckout">结算({{ checkedCount }})</el-button>
    </div>
    <div class="settle-panel shop-only-pc" v-if="cartList.length">
      <el-checkbox :model-value="allChecked" @change="toggleAll">全部</el-checkbox>
      <div class="total">合计： <span class="amount">￥{{ totalAmount }}</span></div>
      <el-button type="danger" size="large" :disabled="checkedCount === 0" @click="goCheckout">结算({{ checkedCount }})</el-button>
    </div>
  </div>
</template>

<script setup name="ShopCart">
import { listCart, updateCartChecked, updateCartQuantity, removeCart } from '@/api/app/cart'
import { promptShopLogin } from '@/utils/shopAuth'
import useUserStore from '@/store/modules/user'
const router = useRouter()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const cartList = ref([])
const loading = ref(true)
const isLogin = computed(() => !!userStore.token)
function goLogin() { promptShopLogin(router, '/shop/cart', { scene: 'cart' }) }
const checkedCount = computed(() => cartList.value.filter(i => i.checked === '1').length)
const totalAmount = computed(() => cartList.value.filter(i => i.checked === '1')
  .reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2))
const allChecked = computed(() => cartList.value.length > 0 && cartList.value.every(i => i.checked === '1'))
function loadCart() {
  if (!userStore.token) { loading.value = false; return }
  loading.value = true
  listCart().then(res => {
    cartList.value = (res.data || []).map(i => ({ ...i, checked: i.checked || '0' }))
    loading.value = false
  }).catch(() => { loading.value = false })
}
function onCheck(item) { updateCartChecked(item.cartId, item.checked) }
function toggleAll(val) {
  const checked = val ? '1' : '0'
  cartList.value.forEach(item => { item.checked = checked; updateCartChecked(item.cartId, checked) })
}
function changeQty(item, qty) { if (qty) updateCartQuantity(item.cartId, qty) }
function removeItem(item) {
  removeCart(item.cartId).then(() => { proxy.$modal.msgSuccess('已删除'); loadCart() })
}
function goProduct(id) { router.push('/shop/product/' + id) }
function goCheckout() {
  if (!userStore.token) { promptShopLogin(router, '/shop/checkout', { scene: 'checkout' }); return }
  const ids = cartList.value.filter(i => i.checked === '1').map(i => i.cartId)
  router.push({ path: '/shop/checkout', query: { cartIds: ids.join(',') } })
}
loadCart()
</script>

<style scoped lang="scss">
.shop-cart { padding-top: 12px; padding-bottom: 100px; min-height: 60vh; }
.page-title { margin: 0 0 20px; font-size: 22px; color: #333; }
.cart-item {
  display: flex; align-items: flex-start; gap: 12px;
  background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 12px;
}
.item-body { flex: 1; display: flex; gap: 16px; cursor: pointer; }
.pic {
  width: 72px; height: 72px; border-radius: 8px; background: #fff5f0;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: #ff6b35; flex-shrink: 0;
}
.info { flex: 1; }
.name { font-size: 14px; margin-bottom: 6px; }
.price { color: #ff6b35; font-weight: 600; margin-bottom: 8px; }
.settle-bar {
  position: fixed; bottom: 56px; left: 0; right: 0;
  display: flex; align-items: center; gap: 8px; padding: 10px 16px;
  background: #fff; box-shadow: 0 -2px 12px rgba(0,0,0,0.08); z-index: 12;
}
.settle-bar .total { flex: 1; font-size: 14px; }
.settle-bar .total span { color: #ff6b35; font-weight: 700; font-size: 18px; }
.settle-panel {
  margin-top: 24px; padding: 20px 24px; background: #fff; border-radius: 8px;
  display: flex; align-items: center; gap: 24px; justify-content: flex-end;
}
.settle-panel .total { flex: 1; text-align: right; font-size: 15px; color: #666; }
.settle-panel .amount { color: #ff6b35; font-size: 24px; font-weight: 700; }
@media (min-width: 769px) {
  .shop-cart { padding-top: 24px; padding-bottom: 40px; }
  .pic { width: 100px; height: 100px; font-size: 36px; }
  .settle-bar { display: none; }
}
.guest-empty { padding: 48px 16px; }
</style>
