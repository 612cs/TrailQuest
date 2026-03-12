<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import TrailCard from '../components/TrailCard.vue'
import HeroSection from '../components/home/HeroSection.vue'
import ActivityGrid from '../components/home/ActivityGrid.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import { mockTrails } from '../mock/mockData'

const popularTrails = computed(() =>
  mockTrails.map((t) => ({
    id: t.id,
    image: t.image,
    name: t.name,
    difficulty: t.difficulty,
    difficultyLabel: t.difficultyLabel,
    packType: t.packType,
    durationType: t.durationType,
    rating: t.rating,
    reviews: `(${t.reviewCount >= 1000 ? (t.reviewCount / 1000).toFixed(1) + 'k' : t.reviewCount} 条评论)`,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
  }))
)
const isInitialLoad = ref(true)

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)
})
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
      <div class="flex gap-4 sm:gap-6 overflow-x-auto scrollbar-hide pb-2 sm:grid sm:grid-cols-2 lg:grid-cols-3 sm:overflow-visible">
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
