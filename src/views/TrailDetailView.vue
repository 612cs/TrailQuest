<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BaseIcon from '../components/common/BaseIcon.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import TrailMapSection from '../components/trail/TrailMapSection.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherAlert from '../components/trail/WeatherAlert.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import ReviewList from '../components/trail/ReviewList.vue'
import { useTrailGeo } from '../composables/useTrailGeo'
import { useTrailWeather } from '../composables/useTrailWeather'
import { getTrailsWithAuthor, getReviewsByTrailId } from '../mock/mockData'
import type { TrailWithAuthor, ReviewWithAuthor } from '../mock/mockData'

const router = useRouter()
const route = useRoute()

const reviews = ref<ReviewWithAuthor[]>([])
const { geo, isLoading: geoLoading, resolve: resolveGeo } = useTrailGeo()
const { weather, isLoading: weatherLoading, resolve: resolveWeather } = useTrailWeather()

// Get trail data based on route ID with Author Context
const trailId = computed(() => Number(route.params.id))
const allTrails = getTrailsWithAuthor()
const trailData = computed<TrailWithAuthor | undefined>(() => {
  return allTrails.find(t => t.id === trailId.value)
})

// Load reviews when trailData changes
watch(trailId, (newId) => {
  if (newId) {
    const fetchedReviews = getReviewsByTrailId(newId)
    // Deep clone reviews so mutations don't affect the mock source
    reviews.value = JSON.parse(JSON.stringify(fetchedReviews))
  } else {
    reviews.value = []
  }
}, { immediate: true }) // immediate: true to run on initial component mount

watch(trailData, async (trail, _prev, onCleanup) => {
  if (!trail) return
  const controller = new AbortController()
  onCleanup(() => controller.abort())

  await resolveGeo({
    ip: trail.ip,
    locationLabel: `${trail.location} ${trail.name}`,
    signal: controller.signal,
  })

  if (geo.value?.adcode) {
    await resolveWeather({
      adcode: geo.value.adcode,
      signal: controller.signal,
    })
  }
}, { immediate: true })

const heroProps = computed(() => {
  const t = trailData.value!
  return {
    image: t.image,
    name: t.name,
    location: t.location,
    difficulty: t.difficulty,
    difficultyLabel: t.difficultyLabel,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
    rating: t.rating,
    reviewCount: t.reviewCount,
    likes: t.likes,
    favorites: t.favorites,
    author: t.author,
    publishTime: t.publishTime
  }
})

const mapCenter = computed(() => geo.value?.center ?? null)
const locationCity = computed(() => geo.value?.city ?? trailData.value?.location ?? '')
const weatherAlertMessage = computed(() => {
  if (!weather.value) {
    return geoLoading.value || weatherLoading.value
      ? '正在获取实时天气，请稍候。'
      : '暂时无法获取实时天气信息。'
  }
  if (weather.value.weather.includes('雨')) {
    return '当前有降雨可能，建议携带防雨装备。'
  }
  const windPower = Number(weather.value.windPower.match(/\d+/)?.[0] ?? '0')
  if (windPower >= 4) {
    return '当前风力较大，注意保暖并选择安全路线。'
  }
  return '天气条件良好，注意补水与防晒。'
})

function handleAddReview(review: ReviewWithAuthor) {
  reviews.value.unshift(review)
}
</script>

<template>
  <main v-if="trailData">
    <!-- Custom Header -->
    <div class="glass-header sticky top-0 z-40 px-4 py-3">
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

      <!-- Description Section -->
      <div class="card p-5 space-y-3">
        <h3 class="text-base font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Info" :size="18" class="text-primary-500" />
          路线介绍
        </h3>
        <p class="text-sm leading-relaxed whitespace-pre-line" style="color: var(--text-secondary);">
          {{ trailData.description }}
        </p>
      </div>

      <TrailMapSection
        :center="mapCenter"
        :label="trailData.name"
        :city="locationCity"
      />

      <!-- Weather Section -->
      <WeatherSection
        :weather="weather"
        :is-loading="weatherLoading"
        :fallback-city="locationCity"
      />

      <!-- Weather Alert -->
      <WeatherAlert
        title="天气提醒"
        :message="weatherAlertMessage"
      />

      <!-- Landscape Prediction -->
      <LandscapePrediction />

      <!-- User Reviews -->
      <ReviewList
        :reviews="reviews"
        :average-rating="trailData.rating"
        :total-reviews="trailData.reviewCount"
        @add-review="handleAddReview"
      />
    </div>
  </main>
</template>
