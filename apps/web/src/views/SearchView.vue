<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import SearchBar from '../components/search/SearchBar.vue'
import FilterBar, { type FilterItem } from '../components/search/FilterBar.vue'
import ViewToggle from '../components/search/ViewToggle.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import TrailHorizontalCard from '../components/trail/TrailHorizontalCard.vue'
import { mockTrails } from '../mock/mockData'

const searchInput = ref('')
const currentSearchQuery = ref('')
const activeView = ref<'list' | 'map'>('list')
const isInitialLoad = ref(true)

const filterModel = ref<Record<string, string>>({
  difficulty: 'all',
  packType: 'all',
  durationType: 'all',
  distance: 'all'
})

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

const filters: FilterItem[] = [
  {
    key: 'difficulty',
    label: '难度',
    options: [
      { label: '全部难度', value: 'all' },
      { label: '简单', value: 'easy' },
      { label: '适中', value: 'moderate' },
      { label: '困难', value: 'hard' }
    ]
  },
  {
    key: 'distance',
    label: '路线长度',
    options: [
      { label: '全部长度', value: 'all' },
      { label: '5km以内', value: 'short' },
      { label: '5-10km', value: 'medium' },
      { label: '10km以上', value: 'long' }
    ]
  },
  {
    key: 'packType',
    label: '装备',
    options: [
      { label: '全部装备', value: 'all' },
      { label: '轻装', value: 'light' },
      { label: '重装', value: 'heavy' },
      { label: '轻重皆可', value: 'both' }
    ]
  },
  {
    key: 'durationType',
    label: '耗时',
    options: [
      { label: '全部时长', value: 'all' },
      { label: '单日', value: 'single_day' },
      { label: '多日', value: 'multi_day' }
    ]
  }
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
    const diffOk = filterModel.value.difficulty === 'all' || trail.difficulty === filterModel.value.difficulty
    const packOk = filterModel.value.packType === 'all' || trail.packType === filterModel.value.packType
    const durationOk = filterModel.value.durationType === 'all' || trail.durationType === filterModel.value.durationType
    
    let distanceOk = true
    if (filterModel.value.distance !== 'all') {
      const dist = parseFloat(trail.distance)
      if (!isNaN(dist)) {
        if (filterModel.value.distance === 'short') distanceOk = dist < 5
        else if (filterModel.value.distance === 'medium') distanceOk = dist >= 5 && dist <= 10
        else if (filterModel.value.distance === 'long') distanceOk = dist > 10
      }
    }

    return diffOk && packOk && durationOk && distanceOk
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

      <FilterBar :filters="filters" v-model="filterModel">
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