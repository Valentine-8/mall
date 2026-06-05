<template>
  <button
    type="button"
    class="shop-dark-toggle"
    :class="{ active: dark }"
    :title="dark ? lightTitle : darkTitle"
    @click="toggle"
  >
    <el-icon><Moon v-if="!dark" /><Sunny v-else /></el-icon>
    <span v-if="showLabel" class="shop-dark-label">{{ dark ? lightLabel : darkLabel }}</span>
  </button>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { isShopDarkMode, toggleShopDarkMode } from '@/utils/shopTheme'

defineProps({
  showLabel: { type: Boolean, default: true },
  darkTitle: { type: String, default: '\u5207\u6362\u591c\u95f4\u6a21\u5f0f' },
  lightTitle: { type: String, default: '\u5207\u6362\u65e5\u95f4\u6a21\u5f0f' },
  darkLabel: { type: String, default: '\u591c\u95f4' },
  lightLabel: { type: String, default: '\u65e5\u95f4' }
})

const dark = ref(false)

onMounted(() => {
  dark.value = isShopDarkMode()
})

function toggle() {
  dark.value = toggleShopDarkMode()
}
</script>

<style scoped lang="scss">
.shop-dark-toggle {
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
  transition: border-color 0.2s, color 0.2s, background 0.2s;
  &:hover {
    color: var(--shop-primary);
    border-color: rgba(var(--shop-primary-rgb), 0.35);
  }
  &.active {
    color: var(--shop-primary);
    background: var(--shop-primary-soft);
    border-color: rgba(var(--shop-primary-rgb), 0.4);
  }
}
.shop-dark-label {
  font-size: 12px;
  white-space: nowrap;
}
</style>
