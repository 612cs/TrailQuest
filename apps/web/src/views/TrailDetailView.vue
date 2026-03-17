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
import { fetchTrailDetail } from '../api/trails'
import { createReview, fetchTrailReviews } from '../api/reviews'
import { useFlashStore } from '../stores/useFlashStore'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import type { TrailListItem } from '../types/trail'
import type { CreateReviewPayload, ReviewItem } from '../types/review'

const router = useRouter()
const route = useRoute()
const flashStore = useFlashStore()
const trailInteractionStore = useTrailInteractionStore()

const reviews = ref<ReviewItem[]>([])
const trailData = ref<TrailListItem | null>(null)
const isLoading = ref(false)
const isReviewsLoading = ref(false)
const isSubmittingReview = ref(false)
const errorMessage = ref('')
const reviewsErrorMessage = ref('')
const reviewFormResetKey = ref(0)
const replyFormResetKey = ref(0)
const { geo, isLoading: geoLoading, resolve: resolveGeo } = useTrailGeo()
const { weather, isLoading: weatherLoading, resolve: resolveWeather } = useTrailWeather()

const trailId = computed(() => Number(route.params.id))
const displayTrail = computed(() => {
  if (!trailData.value) return null
  return trailInteractionStore.applyToTrail(trailData.value)
})

watch(trailId, async (newId) => {
  if (!newId) {
    reviews.value = []
    return
  }

  await loadReviews(newId)
}, { immediate: true })

watch(trailId, async (newId) => {
  if (!newId) {
    trailData.value = null
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const data = await fetchTrailDetail(newId)
    trailData.value = data
    trailInteractionStore.hydrateTrail(data)
  } catch (error) {
    trailData.value = null
    errorMessage.value = error instanceof Error ? error.message : '路线详情加载失败'
  } finally {
    isLoading.value = false
  }
}, { immediate: true })

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
  const t = displayTrail.value!
  return {
    id: t.id,
    image: t.image,
    name: t.name,
    location: t.location,
    difficulty: t.difficulty,
    difficultyLabel: t.difficultyLabel,
    packType: t.packType,
    durationType: t.durationType,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
    rating: t.rating,
    reviewCount: t.reviewCount,
    likes: t.likes,
    favorites: t.favorites,
    likedByCurrentUser: t.likedByCurrentUser,
    favoritedByCurrentUser: t.favoritedByCurrentUser,
    isLikePending: trailInteractionStore.isLikePending(t.id),
    isFavoritePending: trailInteractionStore.isFavoritePending(t.id),
    author: t.author,
    publishTime: t.publishTime,
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

async function handleCreateReview(payload: { rating?: number; text: string; images: string[] }) {
  if (!trailData.value || isSubmittingReview.value) return

  await submitReview({
    trailId: trailData.value.id,
    rating: payload.rating,
    text: payload.text,
    images: payload.images,
  })
}

async function handleShare() {
  if (!displayTrail.value) return

  const shareUrl = window.location.href
  const canUseNativeShare = typeof navigator.share === 'function'

  try {
    if (canUseNativeShare) {
      await navigator.share({
        title: displayTrail.value.name,
        text: `${displayTrail.value.name} · ${displayTrail.value.location}`,
        url: shareUrl,
      })
    } else {
      await navigator.clipboard.writeText(shareUrl)
    }
    flashStore.showSuccess(canUseNativeShare ? '已打开系统分享' : '路线链接已复制')
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return
    }
    flashStore.showError('分享失败，请稍后重试')
  }
}

async function handleCreateReply(payload: { parentId: string; text: string; replyTo?: string; images: string[] }) {
  if (!trailData.value || isSubmittingReview.value) return

  await submitReview({
    trailId: trailData.value.id,
    parentId: payload.parentId,
    text: payload.text,
    replyTo: payload.replyTo,
    images: payload.images,
  })
}

async function submitReview(payload: CreateReviewPayload) {
  isSubmittingReview.value = true
  reviewsErrorMessage.value = ''

  try {
    const result = await createReview(payload)
    trailData.value = trailData.value
      ? {
        ...trailData.value,
        rating: result.trailRating,
        reviewCount: result.trailReviewCount,
      }
      : trailData.value
    reviewsErrorMessage.value = ''
    const inserted = insertReview(result.review)
    if (!inserted) {
      await loadReviews(payload.trailId, { replaceOnError: false })
    }
    if (payload.parentId) {
      replyFormResetKey.value += 1
    } else {
      reviewFormResetKey.value += 1
    }
  } catch (error) {
    reviewsErrorMessage.value = error instanceof Error ? error.message : '评论提交失败'
  } finally {
    isSubmittingReview.value = false
  }
}

async function loadReviews(targetTrailId: number, options: { replaceOnError?: boolean } = {}) {
  const { replaceOnError = true } = options
  isReviewsLoading.value = true
  reviewsErrorMessage.value = ''

  try {
    reviews.value = await fetchTrailReviews(targetTrailId)
    return true
  } catch (error) {
    if (replaceOnError) {
      reviews.value = []
    }
    reviewsErrorMessage.value = error instanceof Error ? error.message : '评论加载失败'
    return false
  } finally {
    isReviewsLoading.value = false
  }
}

function insertReview(review: ReviewItem) {
  const normalizedReview: ReviewItem = {
    ...review,
    images: review.images ?? [],
    replies: review.replies ?? [],
  }

  if (!normalizedReview.parentId) {
    reviews.value = [normalizedReview, ...reviews.value]
    return true
  }

  return insertReply(reviews.value, normalizedReview)
}

function insertReply(items: ReviewItem[], review: ReviewItem): boolean {
  for (const item of items) {
    if (item.id === review.parentId) {
      item.replies = [...item.replies, review]
      return true
    }
    if (insertReply(item.replies, review)) {
      return true
    }
  }
  return false
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
        <button class="p-1.5" style="color: var(--text-secondary);" @click="handleShare">
          <BaseIcon name="Share" :size="20" />
        </button>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      <!-- Hero Card -->
      <DetailHero
        v-bind="heroProps"
        @toggle-like="displayTrail && trailInteractionStore.toggleLike(displayTrail)"
        @toggle-favorite="displayTrail && trailInteractionStore.toggleFavorite(displayTrail)"
        @share="handleShare"
      />

      <!-- Description Section -->
      <div class="card p-5 space-y-3">
        <h3 class="text-base font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Info" :size="18" class="text-primary-500" />
          路线介绍
        </h3>
        <p class="text-sm leading-relaxed whitespace-pre-line" style="color: var(--text-secondary);">
          {{ displayTrail?.description ?? trailData.description }}
        </p>
      </div>

      <TrailMapSection
        :center="mapCenter"
        :label="displayTrail?.name ?? trailData.name"
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
        :is-submitting="isSubmittingReview || isReviewsLoading"
        :error-message="reviewsErrorMessage"
        :review-form-reset-key="reviewFormResetKey"
        :reply-form-reset-key="replyFormResetKey"
        @submit-review="handleCreateReview"
        @submit-reply="handleCreateReply"
      />
    </div>
  </main>
  <main v-else class="max-w-4xl mx-auto px-4 sm:px-6 py-10">
    <div class="card p-8 text-center">
      <p v-if="isLoading" class="text-sm" style="color: var(--text-secondary);">正在加载路线详情...</p>
      <p v-else-if="errorMessage" class="text-sm" style="color: var(--color-hard);">{{ errorMessage }}</p>
      <p v-else class="text-sm" style="color: var(--text-secondary);">未找到该路线</p>
    </div>
  </main>
</template>
