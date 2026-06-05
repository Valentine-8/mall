<template>
  <el-popover placement="bottom-end" :width="220" trigger="click">
    <template #reference>
      <button type="button" class="shop-theme-trigger" :title="title">
        <el-icon><Brush /></el-icon>
        <span class="shop-theme-label shop-only-pc">{{ current.name }}</span>
      </button>
    </template>
    <div class="shop-theme-panel">
      <div class="shop-theme-title">{{ title }}</div>
      <div class="shop-theme-list">
        <button
          v-for="t in SHOP_THEMES"
          :key="t.id"
          type="button"
          class="shop-theme-item"
          :class="{ active: t.id === current.id }"
          @click="pick(t.id)"
        >
          <span class="swatch" :style="{ background: `linear-gradient(135deg, ${t.primary}, ${t.light})` }" />
          <span class="name">{{ t.name }}</span>
          <el-icon v-if="t.id === current.id" class="check"><Check /></el-icon>
        </button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  SHOP_THEMES,
  applyShopTheme,
  getSavedShopThemeId
} from '@/utils/shopTheme'

const props = defineProps({
  title: { type: String, default: '\u4e3b\u9898\u8272' }
})

const current = ref(SHOP_THEMES[0])

onMounted(() => {
  current.value = applyShopTheme(getSavedShopThemeId())
})

function pick(id) {
  current.value = applyShopTheme(id)
}
</script>

<style scoped lang="scss">
.shop-theme-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--shop-border, rgba(0, 0, 0, 0.08));
  background: var(--shop-surface, #fff);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 13px;
  color: var(--shop-text-muted, #666);
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
  &:hover {
    color: var(--shop-primary);
    border-color: rgba(var(--shop-primary-rgb), 0.35);
  }
}
.shop-theme-label { font-size: 12px; }
.shop-theme-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--shop-text, #333);
}
.shop-theme-list { display: flex; flex-direction: column; gap: 6px; margin-top: 8px; }
.shop-theme-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  border: 1px solid var(--shop-border, #eee);
  border-radius: 10px;
  padding: 8px 10px;
  background: var(--shop-surface-2, #fff);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  &.active {
    border-color: var(--shop-primary);
    background: var(--shop-primary-soft);
  }
  .swatch {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    flex-shrink: 0;
    box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.06);
  }
  .name { flex: 1; text-align: left; font-size: 13px; color: var(--shop-text, #333); }
  .check { color: var(--shop-primary); font-size: 16px; }
}
</style>

<style lang="scss">
.el-popover.el-popper {
  background: var(--shop-surface, #fff);
  border-color: var(--shop-border, #ebeef5);
}
:root[data-shop-mode='dark'] .el-popover.el-popper {
  background: var(--shop-surface);
  border-color: var(--shop-border);
  .el-popper__arrow::before {
    background: var(--shop-surface);
    border-color: var(--shop-border);
  }
}
</style>
