<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import TrailCard from '../components/TrailCard.vue'
import HeroSection from '../components/home/HeroSection.vue'
import ActivityGrid from '../components/home/ActivityGrid.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import { fetchTrails } from '../api/trails'
import type { TrailListItem } from '../types/trail'
import { toHomeTrailCard } from '../utils/trailAdapters'

const trails = ref<TrailListItem[]>([])
const isInitialLoad = ref(true)
const isLoading = ref(false)
const errorMessage = ref('')

const popularTrails = computed(() => trails.value.map(toHomeTrailCard))

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)

  void loadTrails()
})

async function loadTrails() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const data = await fetchTrails({
      sort: 'hot',
      pageNum: 1,
      pageSize: 6,
    })
    trails.value = data.list
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '热门路线加载失败'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <main>
    <HeroSection />

    <!-- Popular Trails Section -->
    <section class="py-10 sm:py-16 px-4 sm:px-6 lg:px-10 max-w-7xl mx-auto">
      <SectionHeader title="近期热门步道" subtitle="本周精选热门探险活动。">
        <template #action>
          <RouterLink to="/search" class="flex items-center gap-1 text-sm font-medium text-primary-500 hover:text-primary-600 transition-colors">
            查看全部
            <BaseIcon name="ChevronRight" :size="16" />
          </RouterLink>
        </template>
      </SectionHeader>

      <!-- Trail Cards -->
      <div v-if="isLoading" class="py-10 text-sm text-center" style="color: var(--text-secondary);">
        正在加载热门路线...
      </div>
      <div v-else-if="errorMessage" class="card p-6 text-sm text-center" style="color: var(--color-hard);">
        {{ errorMessage }}
      </div>
      <div v-else-if="popularTrails.length === 0" class="card p-6 text-sm text-center" style="color: var(--text-secondary);">
        暂无热门路线
      </div>
      <div v-else class="flex gap-4 sm:gap-6 overflow-x-auto scrollbar-hide pb-2 sm:grid sm:grid-cols-2 lg:grid-cols-3 sm:overflow-visible">
        <TrailCard
          v-for="trail in popularTrails"
          :key="trail.id"
          v-bind="trail"
          :class="isInitialLoad ? `animate-fade-in-up stagger-${trail.id}` : ''"
        />
      </div>
    </section>

    <ActivityGrid />

    <!-- AI Assistant Floating Button -->
    <div class="fixed bottom-6 right-6 z-40">
      <RouterLink to="/chat" class="w-14 h-14 rounded-full bg-primary-500 text-white flex items-center justify-center shadow-lg hover:bg-primary-600 hover:shadow-xl hover:scale-105 transition-all duration-200">
        <BaseIcon name="MessageSquare" :size="24" />
      </RouterLink>
    </div>
  </main>
</template>
