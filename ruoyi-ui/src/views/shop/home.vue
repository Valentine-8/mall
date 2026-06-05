<template>
  <div class="shop-home">
    <div class="hero shop-only-mobile">
      <div class="hero-inner">
        <p class="hero-kicker">{{ shopSlogan }}</p>
        <h1>{{ shopName }}</h1>
        <p class="hero-desc">发现心仪好物，一键下单到家</p>
        <div class="hero-stats">
          <span><strong>{{ total }}</strong> 件商品</span>
          <span><strong>{{ categories.length }}</strong> 个分类</span>
        </div>
      </div>
    </div>

    <div class="shop-content home-body">
      <aside class="category-sidebar shop-only-pc shop-card">
        <h3><el-icon><Menu /></el-icon> 商品分类</h3>
        <ul>
          <li :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">
            <el-icon class="cat-icon"><Grid /></el-icon>
            <span>全部商品</span>
            <em v-if="!queryParams.categoryId" class="cat-dot" />
          </li>
          <li v-for="(c, idx) in categories" :key="c.categoryId"
              :class="{ active: queryParams.categoryId === c.categoryId }"
              @click="selectCategory(c.categoryId)">
            <el-icon class="cat-icon"><component :is="pickCategoryIcon(idx)" /></el-icon>
            <span>{{ c.categoryName }}</span>
            <em v-if="queryParams.categoryId === c.categoryId" class="cat-dot" />
          </li>
        </ul>
      </aside>

      <div class="home-main">
        <div class="hero pc-hero shop-only-pc">
          <div class="hero-glow hero-glow-a" />
          <div class="hero-glow hero-glow-b" />
          <div class="hero-grid">
            <div class="hero-copy">
              <p class="hero-kicker">{{ shopSlogan }}</p>
              <h1>{{ shopName }}</h1>
              <p class="hero-desc">精选商品，品质保障，购物更省心</p>
              <div class="hero-stats">
                <div class="stat-pill">
                  <span class="stat-num">{{ total }}</span>
                  <span class="stat-label">在售商品</span>
                </div>
                <div class="stat-pill">
                  <span class="stat-num">{{ categories.length }}</span>
                  <span class="stat-label">商品分类</span>
                </div>
              </div>
              <div v-if="heroCategories.length" class="hero-cats">
                <button
                  v-for="(c, idx) in heroCategories"
                  :key="c.categoryId"
                  type="button"
                  class="hero-cat-btn"
                  @click="selectCategory(c.categoryId)"
                >
                  <el-icon><component :is="pickCategoryIcon(idx)" /></el-icon>
                  {{ c.categoryName }}
                </button>
              </div>
            </div>
            <div class="hero-visual">
              <div class="visual-ring" />
              <div class="visual-card">
                <el-icon class="visual-icon"><ShoppingBag /></el-icon>
                <p>今日精选</p>
                <span>好物不断上新</span>
              </div>
            </div>
          </div>
        </div>

        <div class="service-strip shop-card">
          <div v-for="item in serviceTags" :key="item.title" class="service-item">
            <span class="service-icon"><el-icon><component :is="item.icon" /></el-icon></span>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>

        <div class="category-bar shop-only-mobile">
          <span class="cat-chip" :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">全部</span>
          <span v-for="c in categories" :key="c.categoryId" class="cat-chip"
                :class="{ active: queryParams.categoryId === c.categoryId }"
                @click="selectCategory(c.categoryId)">{{ c.categoryName }}</span>
        </div>

        <div class="list-section shop-card">
          <div class="section-head">
            <div class="section-title">
              <h2>{{ activeCategoryName }}</h2>
              <p v-if="!loading">共 {{ total }} 件商品<span v-if="searchKeyword.trim()">，搜索「{{ searchKeyword.trim() }}」</span></p>
            </div>
            <div class="sort-chips shop-only-pc">
              <button
                v-for="opt in sortOptions"
                :key="opt.value"
                type="button"
                class="sort-chip"
                :class="{ active: sortValue === opt.value }"
                @click="sortValue = opt.value; applySort(opt.value)"
              >{{ opt.label }}</button>
            </div>
          </div>

          <div class="search-wrap" ref="searchWrapRef">
            <div class="product-toolbar">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索商品名称"
                clearable
                class="search-input"
                @focus="onSearchFocus"
                @blur="onSearchBlur"
                @keyup.enter="handleSearch"
                @clear="onSearchClear"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button class="search-btn" @click="handleSearch">搜索</el-button>
              <el-select v-model="sortValue" class="sort-select shop-only-mobile" @change="applySort">
                <el-option v-for="opt in sortOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </div>

            <div
              v-show="searchPanelOpen && (searchHistory.length || searchRecommendations.length)"
              class="search-panel"
              @mousedown.prevent
            >
              <div v-if="searchHistory.length" class="search-block">
                <div class="search-block-head">
                  <span class="search-block-title"><el-icon><Clock /></el-icon> 搜索历史</span>
                  <button type="button" class="search-block-action" @click="clearHistory">清空</button>
                </div>
                <div class="search-tags">
                  <button
                    v-for="kw in searchHistory"
                    :key="'h-' + kw"
                    type="button"
                    class="search-tag history"
                    @click="pickSearch(kw)"
                  >
                    {{ kw }}
                    <el-icon class="tag-remove" @click.stop="removeHistoryItem(kw)"><Close /></el-icon>
                  </button>
                </div>
              </div>
              <div v-if="searchRecommendations.length" class="search-block">
                <div class="search-block-head">
                  <span class="search-block-title"><el-icon><Star /></el-icon> 搜索推荐</span>
                </div>
                <div class="search-tags">
                  <button
                    v-for="(kw, idx) in searchRecommendations"
                    :key="'r-' + kw"
                    type="button"
                    class="search-tag recommend"
                    @click="pickSearch(kw)"
                  >
                    <em v-if="idx < 3" class="hot-rank">{{ idx + 1 }}</em>
                    {{ kw }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-loading="loading" class="product-grid">
            <div
              v-for="(p, idx) in products"
              :key="p.productId"
              class="product-card"
              :style="{ animationDelay: `${Math.min(idx, 11) * 0.04}s` }"
              @click="goDetail(p.productId)"
            >
              <div class="pic">
                <span v-if="p.saleCount > 0" class="badge hot">热卖</span>
                <span v-if="p.categoryName" class="badge cat">{{ p.categoryName }}</span>
                <img v-if="productThumb(p)" :src="productThumb(p)" :alt="p.productName" />
                <span v-else class="pic-fallback">{{ p.productName?.charAt(0) }}</span>
                <div class="pic-overlay">查看详情</div>
              </div>
              <div class="info">
                <div class="name">{{ p.productName }}</div>
                <div class="price-row">
                  <span class="price"><small>￥</small>{{ formatPrice(p.price) }}</span>
                  <span class="sales" :class="{ muted: !p.saleCount }">{{ p.saleCount ? `已售 ${p.saleCount}` : '新品上架' }}</span>
                </div>
              </div>
            </div>
            <div v-if="!loading && products.length === 0" class="grid-empty">
              <el-empty description="暂无相关商品">
                <el-button type="primary" plain @click="resetFilters">查看全部商品</el-button>
              </el-empty>
            </div>
          </div>

          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="loadProducts"
            layout="total, prev, pager, next"
            class="home-pagination"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ShopHome">
import {
  Box,
  CircleCheck,
  Clock,
  Close,
  Grid,
  Headset,
  Iphone,
  Menu,
  Monitor,
  Search,
  ShoppingBag,
  ShoppingCart,
  Star,
  Van
} from '@element-plus/icons-vue'
import { listAppCategory } from '@/api/app/category'
import { listAppProduct } from '@/api/app/product'
import { productCoverPic, resolveShopMedia } from '@/utils/shopMedia'
import {
  addShopSearchHistory,
  clearShopSearchHistory,
  getShopSearchHistory,
  removeShopSearchHistory
} from '@/utils/shopSearch'
import { SHOP_NAME, SHOP_SLOGAN } from '@/utils/shopTheme'

const shopName = SHOP_NAME
const shopSlogan = SHOP_SLOGAN
const router = useRouter()
const categories = ref([])
const products = ref([])
const loading = ref(false)
const total = ref(0)
const searchKeyword = ref('')
const sortValue = ref('default')
const searchPanelOpen = ref(false)
const searchWrapRef = ref(null)
const searchHistory = ref([])
const searchRecommendations = ref([])
const categoryIconList = [ShoppingBag, Monitor, Box, Iphone, ShoppingCart, Headset]
const sortOptions = [
  { label: '综合排序', value: 'default' },
  { label: '价格低', value: 'price_asc' },
  { label: '价格高', value: 'price_desc' },
  { label: '销量', value: 'saleCount_desc' },
  { label: '最新', value: 'createTime_desc' }
]
const serviceTags = [
  { icon: CircleCheck, title: '正品保障', desc: '品质甄选' },
  { icon: Van, title: '快速配送', desc: '高效履约' },
  { icon: Box, title: '安心购物', desc: '售后无忧' },
  { icon: Headset, title: '专属客服', desc: '随时咨询' }
]
const queryParams = ref({
  pageNum: 1,
  pageSize: 12,
  categoryId: undefined,
  productName: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

const heroCategories = computed(() => categories.value.slice(0, 4))
const activeCategoryName = computed(() => {
  if (!queryParams.value.categoryId) return '全部商品'
  const match = categories.value.find(c => c.categoryId === queryParams.value.categoryId)
  return match?.categoryName || '全部商品'
})

function pickCategoryIcon(index) {
  return categoryIconList[index % categoryIconList.length]
}
function formatPrice(price) {
  if (price == null || price === '') return '0'
  return Number(price).toFixed(Number(price) % 1 === 0 ? 0 : 2)
}
function loadCategories() {
  listAppCategory().then(res => {
    categories.value = res.data || []
    loadSearchRecommendations()
  })
}
function refreshSearchHistory() {
  searchHistory.value = getShopSearchHistory()
}
function loadSearchRecommendations() {
  listAppProduct({
    pageNum: 1,
    pageSize: 8,
    orderByColumn: 'saleCount',
    isAsc: 'desc'
  }).then(res => {
    const names = []
    const seen = new Set()
    const addName = (text) => {
      const name = String(text || '').trim()
      if (!name || seen.has(name)) return
      seen.add(name)
      names.push(name)
    }
    ;(res.rows || []).forEach(p => addName(p.productName))
    categories.value.forEach(c => addName(c.categoryName))
    searchRecommendations.value = names.slice(0, 10)
  }).catch(() => {})
}
function pickSearch(keyword) {
  searchKeyword.value = keyword
  searchPanelOpen.value = false
  handleSearch()
}
function onSearchClear() {
  searchKeyword.value = ''
  queryParams.value.productName = undefined
  queryParams.value.pageNum = 1
  loadProducts()
}
function removeHistoryItem(keyword) {
  searchHistory.value = removeShopSearchHistory(keyword)
}
function clearHistory() {
  searchHistory.value = clearShopSearchHistory()
}
function onSearchFocus() {
  searchPanelOpen.value = true
}
function onSearchBlur() {
  searchPanelOpen.value = false
}
function handleDocumentClick(e) {
  if (!searchWrapRef.value?.contains(e.target)) {
    searchPanelOpen.value = false
  }
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
function handleSearch() {
  const keyword = searchKeyword.value.trim()
  queryParams.value.productName = keyword || undefined
  queryParams.value.pageNum = 1
  if (keyword) {
    searchHistory.value = addShopSearchHistory(keyword)
  }
  searchPanelOpen.value = false
  loadProducts()
}
function applySort(value) {
  if (!value || value === 'default') {
    queryParams.value.orderByColumn = undefined
    queryParams.value.isAsc = undefined
  } else {
    const sep = value.lastIndexOf('_')
    queryParams.value.orderByColumn = value.slice(0, sep)
    queryParams.value.isAsc = value.slice(sep + 1)
  }
  queryParams.value.pageNum = 1
  loadProducts()
}
function resetFilters() {
  searchKeyword.value = ''
  sortValue.value = 'default'
  queryParams.value = {
    pageNum: 1,
    pageSize: 12,
    categoryId: undefined,
    productName: undefined,
    orderByColumn: undefined,
    isAsc: undefined
  }
  loadProducts()
}
function goDetail(productId) {
  router.push('/shop/product/' + productId)
}
function productThumb(p) {
  return resolveShopMedia(productCoverPic(p))
}
onMounted(() => {
  refreshSearchHistory()
  document.addEventListener('click', handleDocumentClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
loadCategories()
loadProducts()
</script>

<style scoped lang="scss">
.shop-home { padding-bottom: 8px; }

.hero {
  position: relative;
  overflow: hidden;
  background: var(--shop-gradient);
  color: #fff;
}
.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.22), transparent 45%),
    radial-gradient(circle at 80% 0%, rgba(255, 255, 255, 0.12), transparent 40%);
  pointer-events: none;
}
.hero-inner {
  position: relative;
  padding: 28px 20px 32px;
}
.hero-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.92;
}
.hero h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.02em;
}
.hero-desc {
  margin: 10px 0 0;
  font-size: 14px;
  opacity: 0.92;
  line-height: 1.5;
}
.hero-stats {
  display: flex;
  gap: 16px;
  margin-top: 14px;
  font-size: 13px;
  opacity: 0.95;
  strong { font-size: 18px; margin-right: 4px; }
}

.home-body {
  display: block;
  padding-top: 12px;
  padding-bottom: 16px;
}

.category-sidebar h3 {
  display: flex;
  align-items: center;
  gap: 6px;
}
.category-sidebar ul { list-style: none; margin: 0; padding: 0; }
.category-sidebar li {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  position: relative;
}
.cat-icon {
  font-size: 16px;
  opacity: 0.75;
  flex-shrink: 0;
}
.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--shop-primary);
  margin-left: auto;
  flex-shrink: 0;
}

.service-strip {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.service-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  strong {
    display: block;
    font-size: 13px;
    color: var(--shop-text);
    line-height: 1.3;
  }
  p {
    margin: 2px 0 0;
    font-size: 11px;
    color: var(--shop-text-faint);
    line-height: 1.3;
  }
}
.service-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--shop-primary-soft);
  color: var(--shop-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.category-bar {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
  background: var(--shop-surface);
  margin-bottom: 12px;
  border-radius: var(--shop-radius);
  box-shadow: var(--shop-shadow-sm);
  border: 1px solid var(--shop-border);
  -webkit-overflow-scrolling: touch;
}
.cat-chip {
  flex-shrink: 0;
  padding: 7px 16px;
  border-radius: 999px;
  background: var(--shop-surface-3);
  font-size: 13px;
  color: var(--shop-text-muted);
  cursor: pointer;
  transition: all 0.2s;
}
.cat-chip.active {
  background: var(--shop-gradient);
  color: #fff;
  box-shadow: 0 4px 12px rgba(var(--shop-primary-rgb), 0.35);
}

.list-section {
  padding: 16px;
  overflow: hidden;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--shop-border);
}
.section-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--shop-text);
}
.section-title p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--shop-text-faint);
}
.sort-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}
.sort-chip {
  border: 1px solid var(--shop-border);
  background: var(--shop-surface-2);
  color: var(--shop-text-muted);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}
.sort-chip:hover {
  color: var(--shop-primary);
  border-color: rgba(var(--shop-primary-rgb), 0.35);
}
.sort-chip.active {
  background: var(--shop-primary-soft);
  border-color: rgba(var(--shop-primary-rgb), 0.45);
  color: var(--shop-primary);
  font-weight: 600;
}

.product-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.search-wrap {
  position: relative;
  margin-bottom: 16px;
}
.search-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 6px);
  z-index: 20;
  padding: 12px 14px;
  border-radius: var(--shop-radius-sm);
  background: var(--shop-surface);
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-shadow-md);
}
.search-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.search-block-title {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 700;
  color: var(--shop-text-secondary);
}
.search-block-action {
  border: none;
  background: none;
  padding: 0;
  font-size: 12px;
  color: var(--shop-text-faint);
  cursor: pointer;
}
.search-block-action:hover { color: var(--shop-primary); }
.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.search-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid var(--shop-border);
  background: var(--shop-surface);
  color: var(--shop-text-secondary);
  font-size: 12px;
  line-height: 1.3;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
  max-width: 100%;
}
.search-tag:hover {
  color: var(--shop-primary);
  border-color: rgba(var(--shop-primary-rgb), 0.4);
  background: var(--shop-primary-soft);
}
.search-tag.history {
  padding-right: 8px;
}
.search-tag.recommend {
  background: var(--shop-surface-2);
}
.search-tag .tag-remove {
  font-size: 12px;
  color: var(--shop-text-faint);
  margin-left: 2px;
}
.search-tag .tag-remove:hover { color: var(--shop-primary); }
.hot-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  border-radius: 4px;
  font-size: 10px;
  font-style: normal;
  font-weight: 800;
  color: #fff;
  background: var(--shop-gradient);
}
.search-input { flex: 1; min-width: 0; }
:deep(.search-btn.el-button) {
  flex-shrink: 0;
  background: var(--shop-primary);
  border-color: var(--shop-primary);
  color: #fff;
  transition: filter 0.2s ease, box-shadow 0.2s ease;
  &:hover,
  &:focus {
    background: var(--shop-primary);
    border-color: var(--shop-primary);
    color: #fff;
    filter: brightness(0.9);
  }
  &:active {
    filter: brightness(0.82);
  }
}
.sort-select { width: 148px; flex-shrink: 0; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.product-card {
  background: var(--shop-surface-2);
  border-radius: var(--shop-radius);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  animation: card-in 0.45s ease both;
}
@keyframes card-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shop-shadow-md);
  border-color: rgba(var(--shop-primary-rgb), 0.25);
}
.pic {
  position: relative;
  height: 156px;
  background: var(--shop-pic-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  overflow: hidden;
  img {
    max-width: 100%;
    max-height: 100%;
    width: auto;
    height: auto;
    object-fit: contain;
    display: block;
    transition: transform 0.35s ease;
  }
}
.badge {
  position: absolute;
  z-index: 2;
  font-size: 10px;
  line-height: 1;
  padding: 4px 8px;
  border-radius: 999px;
  font-weight: 700;
}
.badge.hot {
  top: 8px;
  left: 8px;
  background: var(--shop-gradient);
  color: #fff;
  box-shadow: 0 2px 8px rgba(var(--shop-primary-rgb), 0.35);
}
.badge.cat {
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--shop-text-muted);
  max-width: 42%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pic-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.06em;
  opacity: 0;
  transition: opacity 0.25s ease;
}
.product-card:hover .pic img { transform: scale(1.06); }
.product-card:hover .pic-overlay { opacity: 1; }
.pic-fallback {
  font-size: 36px;
  color: var(--shop-primary);
  font-weight: 700;
}
.info { padding: 12px 12px 14px; background: var(--shop-surface); }
.name {
  font-size: 14px;
  line-height: 1.45;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: var(--shop-text);
}
.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
}
.price {
  color: var(--shop-primary);
  font-weight: 800;
  font-size: 18px;
  small { font-size: 12px; font-weight: 700; margin-right: 1px; }
}
.sales {
  font-size: 11px;
  color: var(--shop-text-faint);
  &.muted { color: var(--shop-primary); opacity: 0.85; }
}
.grid-empty { grid-column: 1 / -1; padding: 24px 0 8px; }
.home-pagination { margin-top: 20px; justify-content: center; }

@media (min-width: 769px) {
  .shop-home { padding: 20px 0 40px; }
  .home-body {
    display: grid;
    grid-template-columns: 232px 1fr;
    gap: 20px;
    align-items: start;
    padding-top: 20px;
  }
  .category-sidebar {
    padding: 8px 0 16px;
    position: sticky;
    top: 80px;
  }
  .category-sidebar h3 {
    margin: 0 16px 12px;
    font-size: 15px;
    font-weight: 700;
    color: var(--shop-text);
    padding-bottom: 10px;
    border-bottom: 1px solid var(--shop-border);
  }
  .category-sidebar li {
    padding: 11px 16px 11px 18px;
    color: var(--shop-text-muted);
    font-size: 14px;
    border-radius: 0 999px 999px 0;
    margin-right: 8px;
    transition: all 0.2s;
  }
  .category-sidebar li:hover {
    color: var(--shop-primary);
    background: var(--shop-primary-muted);
  }
  .category-sidebar li.active {
    color: var(--shop-primary);
    background: var(--shop-primary-soft);
    font-weight: 600;
    border-left: 3px solid var(--shop-primary);
  }

  .pc-hero {
    position: relative;
    border-radius: var(--shop-radius);
    margin-bottom: 16px;
    box-shadow: var(--shop-shadow-md);
    overflow: hidden;
  }
  .hero-glow {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
  }
  .hero-glow-a {
    width: 280px;
    height: 280px;
    top: -80px;
    right: 10%;
    background: rgba(255, 255, 255, 0.14);
  }
  .hero-glow-b {
    width: 160px;
    height: 160px;
    bottom: -40px;
    left: 55%;
    background: rgba(255, 255, 255, 0.08);
  }
  .hero-grid {
    position: relative;
    display: grid;
    grid-template-columns: 1.2fr 0.8fr;
    gap: 20px;
    align-items: center;
    padding: 36px 32px;
  }
  .pc-hero h1 { font-size: 36px; }
  .hero-stats {
    display: flex;
    gap: 12px;
    margin-top: 18px;
  }
  .stat-pill {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.22);
    border-radius: 12px;
    padding: 10px 16px;
    min-width: 96px;
  }
  .stat-num {
    display: block;
    font-size: 22px;
    font-weight: 800;
    line-height: 1.1;
  }
  .stat-label {
    display: block;
    margin-top: 4px;
    font-size: 11px;
    opacity: 0.88;
  }
  .hero-cats {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 18px;
  }
  .hero-cat-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    border: 1px solid rgba(255, 255, 255, 0.35);
    background: rgba(255, 255, 255, 0.12);
    color: #fff;
    font-size: 12px;
    padding: 7px 14px;
    border-radius: 999px;
    cursor: pointer;
    transition: all 0.2s;
  }
  .hero-cat-btn:hover {
    background: rgba(255, 255, 255, 0.22);
    transform: translateY(-1px);
  }
  .hero-visual {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 180px;
  }
  .visual-ring {
    position: absolute;
    width: 180px;
    height: 180px;
    border-radius: 50%;
    border: 1px dashed rgba(255, 255, 255, 0.35);
    animation: spin-slow 18s linear infinite;
  }
  @keyframes spin-slow {
    to { transform: rotate(360deg); }
  }
  .visual-card {
    position: relative;
    z-index: 1;
    width: 148px;
    padding: 22px 16px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.18);
    border: 1px solid rgba(255, 255, 255, 0.28);
    backdrop-filter: blur(8px);
    text-align: center;
    color: #fff;
  }
  .visual-icon { font-size: 34px; margin-bottom: 8px; }
  .visual-card p {
    margin: 0;
    font-size: 15px;
    font-weight: 700;
  }
  .visual-card span {
    display: block;
    margin-top: 6px;
    font-size: 11px;
    opacity: 0.88;
  }

  .service-strip {
    grid-template-columns: repeat(4, 1fr);
    padding: 16px 20px;
    margin-bottom: 16px;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  .pic { height: 200px; }
}

@media (max-width: 480px) {
  .product-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-btn { width: 100%; }
  .sort-select { width: 100%; }
}
</style>
