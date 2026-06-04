<template>
  <div class="shop-home">
    <div class="banner shop-only-mobile">
      <h1>若依商城</h1>
      <p>精选好物，放心购</p>
    </div>
    <div class="shop-content home-body">
      <aside class="category-sidebar shop-only-pc">
        <h3>商品分类</h3>
        <ul>
          <li :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">全部</li>
          <li v-for="c in categories" :key="c.categoryId"
              :class="{ active: queryParams.categoryId === c.categoryId }"
              @click="selectCategory(c.categoryId)">{{ c.categoryName }}</li>
        </ul>
      </aside>
      <div class="home-main">
        <div class="pc-banner shop-only-pc">
          <h1>若依商城</h1>
          <p>精选好物，放心购</p>
        </div>
        <div class="category-bar shop-only-mobile">
          <span class="cat-chip" :class="{ active: !queryParams.categoryId }" @click="selectCategory(null)">全部</span>
          <span v-for="c in categories" :key="c.categoryId" class="cat-chip"
                :class="{ active: queryParams.categoryId === c.categoryId }"
                @click="selectCategory(c.categoryId)">{{ c.categoryName }}</span>
        </div>
        <div v-loading="loading" class="product-grid">
          <div v-for="p in products" :key="p.productId" class="product-card" @click="goDetail(p.productId)">
            <div class="pic">{{ p.productName?.charAt(0) }}</div>
            <div class="info">
              <div class="name">{{ p.productName }}</div>
              <div class="price">￥{{ p.price }}</div>
            </div>
          </div>
          <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
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
