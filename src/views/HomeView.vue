<script setup lang="ts">
import TrailCard from '../components/TrailCard.vue'
import HeroSection from '../components/home/HeroSection.vue'
import ActivityGrid from '../components/home/ActivityGrid.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import SectionHeader from '../components/common/SectionHeader.vue'

const popularTrails = [
  {
    id: 1,
    image: '/trail-pine.png',
    name: '老鹰峰顶',
    difficulty: 'moderate' as const,
    difficultyLabel: '中等',
    rating: 4.9,
    reviews: '(1.2k 条评论)',
    distance: '6.4 mi',
    elevation: '1,420 ft',
    duration: '3h 15m',
  },
  {
    id: 2,
    image: '/trail-lake.png',
    name: '镜湖环线',
    difficulty: 'easy' as const,
    difficultyLabel: '简单',
    rating: 4.7,
    reviews: '(856 条评论)',
    distance: '2.1 mi',
    elevation: '150 ft',
    duration: '45m',
  },
  {
    id: 3,
    image: '/trail-foggy.png',
    name: '布莱克伍德峡谷',
    difficulty: 'hard' as const,
    difficultyLabel: '困难',
    rating: 4.8,
    reviews: '(2.4k 条评论)',
    distance: '11.2 mi',
    elevation: '3,100 ft',
    duration: '6h 20m',
  },
]
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
          class="animate-fade-in-up"
          :class="`stagger-${trail.id}`"
        />
      </div>
    </section>

    <ActivityGrid />

    <!-- AI Assistant Floating Button -->
    <div class="fixed bottom-6 right-6 z-40">
      <button class="w-14 h-14 rounded-full bg-primary-500 text-white flex items-center justify-center shadow-lg hover:bg-primary-600 hover:shadow-xl hover:scale-105 transition-all duration-200">
        <BaseIcon name="MessageSquare" :size="24" />
      </button>
    </div>
  </main>
</template>
