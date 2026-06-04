# -*- coding: utf-8 -*-
import os

ROOT = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui')
STYLES = os.path.join(ROOT, 'src', 'assets', 'styles', 'shop.scss')
VIEWS = os.path.join(ROOT, 'src', 'views', 'shop')

SHOP_SCSS = """
/* Shop responsive: mobile <=768px, PC >=769px */
:root {
  --shop-max-width: 100%;
  --shop-content-pad: 12px;
}
@media (min-width: 769px) {
  :root {
    --shop-max-width: 1200px;
    --shop-content-pad: 24px;
  }
}
.shop-page-bg {
  min-height: 100vh;
  background: #eef0f4;
}
.shop-only-mobile { display: block; }
.shop-only-pc { display: none !important; }
@media (min-width: 769px) {
  .shop-only-mobile { display: none !important; }
  .shop-only-pc { display: block !important; }
  .shop-only-pc.flex { display: flex !important; }
}
.shop-content {
  width: 100%;
  max-width: var(--shop-max-width);
  margin: 0 auto;
  padding-left: var(--shop-content-pad);
  padding-right: var(--shop-content-pad);
  box-sizing: border-box;
}
@media (min-width: 769px) {
  .shop-fixed-bar {
    left: 0 !important;
    right: 0 !important;
    transform: none !important;
    width: 100% !important;
    max-width: var(--shop-max-width) !important;
    margin-left: auto !important;
    margin-right: auto !important;
    padding-left: var(--shop-content-pad) !important;
    padding-right: var(--shop-content-pad) !important;
    box-sizing: border-box !important;
  }
}
"""

LAYOUT_VUE = r'''<template>
  <div class="shop-page-bg">
    <header class="shop-topnav shop-only-pc flex">
      <div class="shop-content topnav-inner">
        <router-link to="/shop/home" class="brand">BRAND</router-link>
        <nav class="topnav-links">
          <router-link to="/shop/home" class="topnav-item" active-class="active">N_HOME</router-link>
          <router-link to="/shop/cart" class="topnav-item" active-class="active">N_CART</router-link>
          <router-link to="/shop/orders" class="topnav-item" active-class="active">N_ORDERS</router-link>
        </nav>
        <router-link v-if="!isLogin" to="/login" class="topnav-login">N_LOGIN</router-link>
        <router-link v-else to="/index" class="topnav-login">N_ADMIN</router-link>
      </div>
    </header>

    <div class="shop-layout">
      <header class="shop-header shop-only-mobile" v-if="showSubPage">
        <el-button link @click="goBack"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="shop-title">{{ pageTitle }}</span>
        <router-link v-if="!isLogin" to="/login" class="shop-login-link">N_LOGIN</router-link>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const isLogin = computed(() => !!getToken())

const mainTabRoutes = ['/shop/home', '/shop/cart', '/shop/orders']
const showMainTab = computed(() => mainTabRoutes.includes(route.path))
const showSubPage = computed(() => !showMainTab.value)
const pageTitle = computed(() => route.meta?.title || 'N_MALL')

function goBack() {
  router.back()
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

# Chinese placeholders
CN = {
    'BRAND': '\u82e5\u4f9d\u5546\u57ce',
    'N_HOME': '\u9996\u9875',
    'N_CART': '\u8d2d\u7269\u8f66',
    'N_ORDERS': '\u6211\u7684\u8ba2\u5355',
    'N_ORDER': '\u8ba2\u5355',
    'N_LOGIN': '\u767b\u5f55',
    'N_ADMIN': '\u7ba1\u7406\u540e\u53f0',
    'N_BACK': '\u8fd4\u56de',
    'N_MALL': '\u5546\u57ce',
}

def cn_replace(s):
    for k, v in CN.items():
        s = s.replace(k, v)
    return s

HOME_VUE = r'''<template>
  <div class="shop-home">
    <div class="banner shop-only-mobile">
      <h1>BRAND</h1>
      <p>SLOGAN</p>
    </div>
    <div class="shop-content home-body">
      <aside class="category-sidebar shop-only-pc">
        <h3>N_CAT</h3>
        <ul>
          <li :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">N_ALL</li>
          <li v-for="c in categories" :key="c.categoryId"
              :class="{ active: queryParams.categoryId === c.categoryId }"
              @click="selectCategory(c.categoryId)">{{ c.categoryName }}</li>
        </ul>
      </aside>
      <div class="home-main">
        <div class="pc-banner shop-only-pc">
          <h1>BRAND</h1>
          <p>SLOGAN</p>
        </div>
        <div class="category-bar shop-only-mobile">
          <span class="cat-chip" :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">N_ALL</span>
          <span v-for="c in categories" :key="c.categoryId" class="cat-chip"
                :class="{ active: queryParams.categoryId === c.categoryId }"
                @click="selectCategory(c.categoryId)">{{ c.categoryName }}</span>
        </div>
        <div v-loading="loading" class="product-grid">
          <div v-for="p in products" :key="p.productId" class="product-card" @click="goDetail(p.productId)">
            <div class="pic">{{ p.productName?.charAt(0) }}</div>
            <div class="info">
              <div class="name">{{ p.productName }}</div>
              <div class="price">YEN{{ p.price }}</div>
            </div>
          </div>
          <el-empty v-if="!loading && products.length === 0" description="N_EMPTY" />
        </div>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize" @pagination="loadProducts"
          layout="total, prev, pager, next" class="home-pagination" />
      </div>
    </div>
  </div>
</template>

<script setup name="ShopHome">
import { listAppCategory } from '@/api/app/category'
import { listAppProduct } from '@/api/app/product'

const router = useRouter()
const categories = ref([])
const products = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 12, categoryId: undefined })

function loadCategories() {
  listAppCategory().then(res => { categories.value = res.data || [] })
}
function loadProducts() {
  loading.value = true
  listAppProduct(queryParams.value).then(res => {
    products.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}
function selectCategory(categoryId) {
  queryParams.value.categoryId = categoryId
  queryParams.value.pageNum = 1
  loadProducts()
}
function goDetail(productId) {
  router.push('/shop/product/' + productId)
}
loadCategories()
loadProducts()
</script>

<style scoped lang="scss">
.shop-home { padding-bottom: 8px; }
.banner, .pc-banner {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: #fff;
}
.banner { padding: 28px 20px; }
.banner h1, .pc-banner h1 { margin: 0; font-size: 22px; }
.banner p, .pc-banner p { margin: 8px 0 0; opacity: 0.9; font-size: 14px; }
.home-body { display: block; padding-top: 0; padding-bottom: 16px; }
.category-bar {
  display: flex; gap: 8px; padding: 12px 0; overflow-x: auto; background: #fff;
  margin-bottom: 8px; border-radius: 8px;
}
.cat-chip {
  flex-shrink: 0; padding: 6px 14px; border-radius: 20px;
  background: #f0f0f0; font-size: 13px; cursor: pointer;
}
.cat-chip.active { background: #ff6b35; color: #fff; }
.product-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px;
}
.product-card {
  background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer;
  transition: box-shadow 0.2s;
}
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.pic {
  height: 120px; background: linear-gradient(145deg, #ffe8de, #fff5f0);
  display: flex; align-items: center; justify-content: center;
  font-size: 36px; color: #ff6b35; font-weight: 700;
}
.info { padding: 10px; }
.name {
  font-size: 14px; line-height: 1.4; height: 40px; overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.price { color: #ff6b35; font-weight: 700; margin-top: 6px; font-size: 16px; }
.home-pagination { margin-top: 16px; justify-content: center; }

@media (min-width: 769px) {
  .shop-home { padding: 20px 0 40px; }
  .home-body {
    display: grid;
    grid-template-columns: 200px 1fr;
    gap: 20px;
    align-items: start;
  }
  .category-sidebar {
    background: #fff;
    border-radius: 8px;
    padding: 16px 0;
    position: sticky;
    top: 72px;
  }
  .category-sidebar h3 {
    margin: 0 16px 12px;
    font-size: 15px;
    color: #333;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }
  .category-sidebar ul { list-style: none; margin: 0; padding: 0; }
  .category-sidebar li {
    padding: 10px 20px;
    cursor: pointer;
    color: #666;
    font-size: 14px;
  }
  .category-sidebar li:hover { color: #ff6b35; background: #fff9f6; }
  .category-sidebar li.active {
    color: #ff6b35;
    background: #fff5f0;
    font-weight: 600;
    border-left: 3px solid #ff6b35;
  }
  .pc-banner {
    padding: 32px;
    border-radius: 8px;
    margin-bottom: 16px;
  }
  .pc-banner h1 { font-size: 28px; }
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
  .pic { height: 180px; font-size: 48px; }
}
</style>
'''

CN['SLOGAN'] = '\u7cbe\u9009\u597d\u7269\uff0c\u653e\u5fc3\u8d2d'
CN['N_CAT'] = '\u5546\u54c1\u5206\u7c7b'
CN['N_ALL'] = '\u5168\u90e8'
CN['N_EMPTY'] = '\u6682\u65e0\u5546\u54c1'
CN['YEN'] = '\uffe5'

CART_VUE = r'''<template>
  <div class="shop-cart shop-content">
    <h2 class="page-title shop-only-pc">N_CART</h2>
    <div v-loading="loading" class="cart-list">
      <div v-for="item in cartList" :key="item.cartId" class="cart-item">
        <el-checkbox :model-value="item.checked === '1'"
          @change="val => { item.checked = val ? '1' : '0'; onCheck(item) }" />
        <div class="item-body" @click="goProduct(item.productId)">
          <div class="pic">{{ item.productName?.charAt(0) }}</div>
          <div class="info">
            <div class="name">{{ item.productName }}</div>
            <div class="price">YEN{{ item.price }}</div>
            <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small"
              @change="qty => changeQty(item, qty)" />
          </div>
        </div>
        <el-button link type="danger" @click="removeItem(item)">N_DEL</el-button>
      </div>
      <el-empty v-if="!loading && cartList.length === 0" description="N_EMPTY_CART">
        <el-button type="primary" @click="$router.push('/shop/home')">N_GO</el-button>
      </el-empty>
    </div>
    <div class="settle-bar shop-fixed-bar shop-only-mobile" v-if="cartList.length">
      <el-checkbox :model-value="allChecked" @change="toggleAll">N_ALL</el-checkbox>
      <div class="total">N_TOTAL<span>YEN{{ totalAmount }}</span></div>
      <el-button type="danger" :disabled="checkedCount === 0" @click="goCheckout">N_SETTLE({{ checkedCount }})</el-button>
    </div>
    <div class="settle-panel shop-only-pc" v-if="cartList.length">
      <el-checkbox :model-value="allChecked" @change="toggleAll">N_ALL</el-checkbox>
      <div class="total">N_TOTAL <span class="amount">YEN{{ totalAmount }}</span></div>
      <el-button type="danger" size="large" :disabled="checkedCount === 0" @click="goCheckout">N_SETTLE({{ checkedCount }})</el-button>
    </div>
  </div>
</template>

<script setup name="ShopCart">
import { listCart, updateCartChecked, updateCartQuantity, removeCart } from '@/api/app/cart'
const router = useRouter()
const { proxy } = getCurrentInstance()
const cartList = ref([])
const loading = ref(true)
const checkedCount = computed(() => cartList.value.filter(i => i.checked === '1').length)
const totalAmount = computed(() => cartList.value.filter(i => i.checked === '1')
  .reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2))
const allChecked = computed(() => cartList.value.length > 0 && cartList.value.every(i => i.checked === '1'))
function loadCart() {
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
  removeCart(item.cartId).then(() => { proxy.$modal.msgSuccess('N_REMOVED'); loadCart() })
}
function goProduct(id) { router.push('/shop/product/' + id) }
function goCheckout() {
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
</style>
'''

CN['N_DEL'] = '\u5220\u9664'
CN['N_EMPTY_CART'] = '\u8d2d\u7269\u8f66\u662f\u7a7a\u7684'
CN['N_GO'] = '\u53bb\u901b\u901b'
CN['N_TOTAL'] = '\u5408\u8ba1\uff1a'
CN['N_SETTLE'] = '\u7ed3\u7b97'  # used as prefix N_SETTLE(
CN['N_REMOVED'] = '\u5df2\u5220\u9664'

# fix settle button text - template uses N_SETTLE( which becomes 锟斤拷锟斤拷(
def cn_replace(s):
    for k, v in CN.items():
        s = s.replace(k, v)
    return s

open(STYLES, 'w', encoding='utf-8', newline='\n').write(SHOP_SCSS.strip() + '\n')
for name, content in [('layout.vue', LAYOUT_VUE), ('home.vue', HOME_VUE), ('cart.vue', CART_VUE)]:
    open(os.path.join(VIEWS, name), 'w', encoding='utf-8', newline='\n').write(cn_replace(content))
print('wrote layout, home, cart, scss')
