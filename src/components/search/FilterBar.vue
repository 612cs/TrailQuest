<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'

export interface FilterOption {
  label: string
  value: string
}

export interface FilterItem {
  key: string
  label: string
  options: FilterOption[]
}

const props = defineProps<{
  filters: FilterItem[]
  modelValue: Record<string, string>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, string>): void
}>()

const activeDropdown = ref<string | null>(null)

function toggleDropdown(key: string) {
  if (activeDropdown.value === key) {
    activeDropdown.value = null
  } else {
    activeDropdown.value = key
  }
}

function selectOption(filterKey: string, optionValue: string) {
  const newValue = { ...props.modelValue, [filterKey]: optionValue }
  emit('update:modelValue', newValue)
  activeDropdown.value = null
}

// 简单实现点击外部关闭
function closeDropdown() {
  activeDropdown.value = null
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-2 pb-1" style="background-color: transparent;">
    <div
      v-for="filter in filters"
      :key="filter.key"
      class="relative shrink-0"
    >
      <!-- Filter Button -->
      <button
        @click="toggleDropdown(filter.key)"
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
        :style="{
          backgroundColor: modelValue[filter.key] !== 'all' ? 'var(--color-primary-500)' : 'var(--bg-tag)',
          color: modelValue[filter.key] !== 'all' ? 'var(--text-inverse)' : 'var(--text-secondary)'
        }"
      >
        {{ filter.options.find(o => o.value === modelValue[filter.key])?.label || filter.label }}
        <BaseIcon name="ChevronDown" :size="14" />
      </button>

      <!-- Dropdown Menu -->
      <div
        v-if="activeDropdown === filter.key"
        class="absolute top-full left-0 mt-1 w-32 rounded-lg shadow-lg border z-10 overflow-hidden"
        style="border-color: var(--border-default); background-color: var(--bg-card);"
      >
        <button
          v-for="option in filter.options"
          :key="option.value"
          @click="selectOption(filter.key, option.value)"
          class="w-full text-left px-4 py-2 text-sm transition-colors"
          :style="{
             color: modelValue[filter.key] === option.value ? 'var(--color-primary-500)' : 'var(--text-primary)',
             backgroundColor: modelValue[filter.key] === option.value ? 'var(--bg-tag)' : 'transparent'
          }"
        >
          {{ option.label }}
        </button>
      </div>
      
      <!-- Click outside overlay layer (simple approach) -->
      <div 
        v-if="activeDropdown === filter.key" 
        class="fixed inset-0 z-0" 
        @click="closeDropdown"
      ></div>
    </div>

    <!-- Extra slot (for "更多筛选" or other custom buttons) -->
    <template v-if="$slots.extra">
      <div class="w-px h-5 mx-1 shrink-0" style="background-color: var(--border-default);"></div>
      <slot name="extra" />
    </template>
  </div>
</template>

<style scoped>
/* Ensure dropdowns appear above the click-outside overlay */
.relative > div:not(.fixed) {
  z-index: 20;
}
</style>