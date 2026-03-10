<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import SearchBar from '../components/search/SearchBar.vue'
import FilterBar from '../components/search/FilterBar.vue'
import ViewToggle from '../components/search/ViewToggle.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import TrailHorizontalCard from '../components/trail/TrailHorizontalCard.vue'
import { mockTrails } from '../mock/mockData'

const searchInput = ref('')
const currentSearchQuery = ref('')
const activeView = ref<'list' | 'map'>('list')
const isInitialLoad = ref(true)
const packTypeFilter = ref<'all' | 'light' | 'heavy' | 'both'>('all')
const durationTypeFilter = ref<'all' | 'single_day' | 'multi_day'>('all')

const route = useRoute()

watch(
  () => route.query.q,
  (newQ) => {
    if (newQ !== undefined) {
      const q = String(newQ)
      searchInput.value = q
      currentSearchQuery.value = q
    }
  },
  { immediate: true }
)

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)
})

const filters = ['难度', '路线长度', '海拔增益', '评分', '轻装/重装', '单日/多日']

const packTypeOptions = [
  { value: 'all', label: '全部' },
  { value: 'light', label: '轻装' },
  { value: 'heavy', label: '重装' },
  { value: 'both', label: '轻重皆可' },
]

const durationTypeOptions = [
  { value: 'all', label: '全部' },
  { value: 'single_day', label: '单日' },
  { value: 'multi_day', label: '多日' },
]

const handleSearch = () => {
  currentSearchQuery.value = searchInput.value
}

const filteredTrails = computed(() => {
  const base = currentSearchQuery.value
    ? mockTrails.filter(trail =>
        trail.name.toLowerCase().includes(currentSearchQuery.value.toLowerCase()) ||
        trail.location.toLowerCase().includes(currentSearchQuery.value.toLowerCase()) ||
        trail.description.toLowerCase().includes(currentSearchQuery.value.toLowerCase())
      )
    : mockTrails
  return base.filter((trail) => {
    const packOk = packTypeFilter.value === 'all' || trail.packType === packTypeFilter.value
    const durationOk = durationTypeFilter.value === 'all' || trail.durationType === durationTypeFilter.value
    return packOk && durationOk
  })
})
</script>

<template>
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-10 py-6 sm:py-10">
    <!-- Search & Filters -->
    <div class="space-y-4 mb-6">
      <div class="flex gap-3">
        <SearchBar 
          v-model="searchInput" 
          placeholder="搜索路线、地点或描述..." 
          class="flex-1"
          @keydown.enter="handleSearch"
        />
        <button 
          @click="handleSearch"
          class="px-5 py-3 bg-primary-500 text-white rounded-xl text-sm font-medium hover:bg-primary-600 active:bg-primary-700 transition-colors shrink-0 flex items-center justify-center gap-1"
        >
          <BaseIcon name="Search" :size="16" />
          搜索
        </button>
      </div>

      <FilterBar :filters="filters">
        <template #extra>
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium shrink-0 border transition-colors hover:border-primary-500/30"
            style="border-color: var(--border-default); color: var(--text-secondary);"
          >
            <BaseIcon name="Filter" :size="14" />
            更多筛选
          </button>
        </template>
      </FilterBar>
      <div class="grid gap-3 sm:grid-cols-2">
        <div class="card p-3 flex flex-wrap items-center gap-2">
          <span class="text-xs font-medium" style="color: var(--text-secondary);">装备类型</span>
          <button
            v-for="option in packTypeOptions"
            :key="option.value"
            class="px-2.5 py-1 rounded-full text-xs font-semibold border transition-colors"
            :style="packTypeFilter === option.value ? 'border-color: var(--primary-500); color: var(--primary-500);' : 'border-color: var(--border-default); color: var(--text-secondary);'"
            @click="packTypeFilter = option.value"
          >
            {{ option.label }}
          </button>
        </div>
        <div class="card p-3 flex flex-wrap items-center gap-2">
          <span class="text-xs font-medium" style="color: var(--text-secondary);">行程天数</span>
          <button
            v-for="option in durationTypeOptions"
            :key="option.value"
            class="px-2.5 py-1 rounded-full text-xs font-semibold border transition-colors"
            :style="durationTypeFilter === option.value ? 'border-color: var(--primary-500); color: var(--primary-500);' : 'border-color: var(--border-default); color: var(--text-secondary);'"
            @click="durationTypeFilter = option.value"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- Results Header -->
    <div class="flex items-center justify-between py-3 mb-4 border-b" style="border-color: var(--border-default);">
      <h3 class="text-sm font-medium" style="color: var(--text-secondary);">找到 {{ filteredTrails.length }} 条符合条件的路线</h3>
      <ViewToggle v-model="activeView" />
    </div>

    <!-- Trail List -->
    <div class="space-y-4 sm:space-y-5">
      <TrailHorizontalCard
        v-for="trail in filteredTrails"
        :key="trail.id"
        v-bind="trail"
        :class="isInitialLoad ? `animate-fade-in-up stagger-${trail.id}` : ''"
      />
    </div>
  </main>
</template>
