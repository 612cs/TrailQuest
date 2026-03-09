<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BaseIcon from '../components/common/BaseIcon.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherAlert from '../components/trail/WeatherAlert.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import ReviewList from '../components/trail/ReviewList.vue'
import { getTrailById } from '../mock/mockData'
import type { Review, TrailDetail } from '../mock/mockData'

const router = useRouter()
const route = useRoute()

const trailDetail = ref<TrailDetail | null>(null)
const reviews = ref<Review[]>([])

// Load trail data based on route param
function loadTrail() {
  const id = Number(route.params.id)
  const detail = getTrailById(id)
  if (detail) {
    trailDetail.value = detail
    // Deep clone reviews so mutations don't affect the mock source
    reviews.value = JSON.parse(JSON.stringify(detail.reviews))
  } else {
    // Fallback: if ID not found, show first trail
    const fallback = getTrailById(1)!
    trailDetail.value = fallback
    reviews.value = JSON.parse(JSON.stringify(fallback.reviews))
  }
}

loadTrail()

// Reload when route param changes (e.g. navigating between trails)
watch(() => route.params.id, loadTrail)

const heroProps = computed(() => {
  const t = trailDetail.value!
  return {
    image: t.image,
    name: t.name,
    location: t.location,
    difficulty: t.difficulty,
    difficultyLabel: t.difficultyLabel,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
    favorites: t.favorites,
    rating: t.rating,
    reviewCount: t.reviewCount,
  }
})

function handleAddReview(review: Review) {
  reviews.value.unshift(review)
}
</script>

<template>
  <main v-if="trailDetail">
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
      <DetailHero v-bind="heroProps" />

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
      <ReviewList
        :reviews="reviews"
        :average-rating="trailDetail.rating"
        :total-reviews="trailDetail.reviewCount"
        @add-review="handleAddReview"
      />
    </div>
  </main>
</template>
