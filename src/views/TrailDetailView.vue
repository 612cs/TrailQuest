<script setup lang="ts">
import { useRouter } from 'vue-router'
import BaseIcon from '../components/common/BaseIcon.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherAlert from '../components/trail/WeatherAlert.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import ReviewList from '../components/trail/ReviewList.vue'

const router = useRouter()

const trailData = {
  image: '/hero-mountain.png',
  name: '龙脊梯田精华线',
  location: '广西 桂林 龙胜',
  difficulty: 'moderate',
  difficultyLabel: '适中',
  distance: '12.5 km',
  elevation: '+450 m',
  duration: '4.5 小时',
}
</script>

<template>
  <main>
    <!-- Custom Header -->
    <div class="glass-header sticky top-14 sm:top-16 z-40 px-4 py-3">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
        <button @click="router.back()" class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
          <BaseIcon name="ChevronLeft" :size="20" />
          返回
        </button>
        <h2 class="text-sm font-semibold" style="color: var(--text-primary);">路线详情</h2>
        <button class="p-1.5" style="color: var(--text-secondary);">
          <BaseIcon name="Share" :size="20" />
        </button>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      <!-- Hero Card -->
      <DetailHero v-bind="trailData" />

      <!-- Weather Section -->
      <WeatherSection />

      <!-- Weather Alert -->
      <WeatherAlert
        title="天气提醒"
        message="下午可能有零星阵雨（概率 30%）。建议携带防雨装备。"
      />

      <!-- Landscape Prediction -->
      <LandscapePrediction />

      <!-- User Reviews -->
      <ReviewList />
    </div>
  </main>
</template>
