<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import TrailCard from '../components/TrailCard.vue'
import HeroSection from '../components/home/HeroSection.vue'
import ActivityGrid from '../components/home/ActivityGrid.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import { fetchTrails } from '../api/trails'
import { useTrailFeedRefreshStore } from '../stores/useTrailFeedRefreshStore'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import type { TrailListItem } from '../types/trail'
import { toHomeTrailCard } from '../utils/trailAdapters'

const trails = ref<TrailListItem[]>([])
const isInitialLoad = ref(true)
const isLoading = ref(false)
const errorMessage = ref('')
const trailInteractionStore = useTrailInteractionStore()
const trailFeedRefreshStore = useTrailFeedRefreshStore()

const popularTrails = computed(() =>
  trails.value.map((trail) => trailInteractionStore.applyToTrail(trail)).map(toHomeTrailCard),
)

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)

  void loadTrails()
})

watch(
  () => trailFeedRefreshStore.version,
  () => {
    void loadTrails()
  },
)

async function loadTrails() {
  if (isLoading.value) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const data = await fetchTrails({
      sort: 'hot',
      pageNum: 1,
      pageSize: 6,
    })
    trails.value = data.list
    trailInteractionStore.hydrateTrails(data.list)
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
    <section class="mx-auto max-w-7xl px-4 py-10 sm:px-6 sm:py-16 lg:px-10">
      <SectionHeader title="近期热门步道" subtitle="本周精选热门探险活动。">
        <template #action>
          <RouterLink
            to="/search"
            class="text-primary-500 hover:text-primary-600 flex items-center gap-1 text-sm font-medium transition-colors"
          >
            查看全部
            <BaseIcon name="ChevronRight" :size="16" />
          </RouterLink>
        </template>
      </SectionHeader>

      <!-- Trail Cards -->
      <div v-if="isLoading" class="py-10 text-center text-sm" style="color: var(--text-secondary)">
        正在加载热门路线...
      </div>
      <div
        v-else-if="errorMessage"
        class="card p-6 text-center text-sm"
        style="color: var(--color-hard)"
      >
        {{ errorMessage }}
      </div>
      <div
        v-else-if="popularTrails.length === 0"
        class="card p-6 text-center text-sm"
        style="color: var(--text-secondary)"
      >
        暂无热门路线
      </div>
      <div
        v-else
        class="scrollbar-hide flex gap-4 overflow-x-auto pb-2 sm:grid sm:grid-cols-2 sm:gap-6 sm:overflow-visible lg:grid-cols-3"
      >
        <TrailCard
          v-for="trail in popularTrails"
          :key="trail.id"
          v-bind="trail"
          :is-like-pending="trailInteractionStore.isLikePending(trail.id)"
          :is-favorite-pending="trailInteractionStore.isFavoritePending(trail.id)"
          :class="isInitialLoad ? `animate-fade-in-up stagger-${trail.id}` : ''"
          @toggle-like="trailInteractionStore.toggleLike(trail)"
          @toggle-favorite="trailInteractionStore.toggleFavorite(trail)"
        />
      </div>
    </section>

    <ActivityGrid />
  </main>
</template>
