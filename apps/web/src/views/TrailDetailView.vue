<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import BaseIcon from '../components/common/BaseIcon.vue'
import ConfirmDialog from '../components/common/ConfirmDialog.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import ReviewList from '../components/trail/ReviewList.vue'
import TrailMapSection from '../components/trail/TrailMapSection.vue'
import TrailTrackViewer from '../components/trail/TrailTrackViewer.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherForecast from '../components/trail/WeatherForecast.vue'
import { createReview, deleteReview, fetchTrailReviews, fetchUserCard } from '../api/reviews'
import { deleteTrail, fetchTrailDetail } from '../api/trails'
import { useTrailShare } from '../composables/useTrailShare'
import { useTrailGeo } from '../composables/useTrailGeo'
import { useTrailWeather } from '../composables/useTrailWeather'
import { useFlashStore } from '../stores/useFlashStore'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import { useUserStore } from '../stores/useUserStore'
import type { EntityId } from '../types/id'
import type { TrailListItem } from '../types/trail'
import { createTrackViewerData } from '../utils/trailTrackViewerAdapter'
import { mapWeatherToTrackScene } from '../utils/trackWeatherScene'
import type {
  CreateReviewPayload,
  ReviewItem,
  UserCard,
} from '../types/review'

const router = useRouter()
const route = useRoute()
const flashStore = useFlashStore()
const interactionStore = useTrailInteractionStore()
const userStore = useUserStore()
const { shareTrail } = useTrailShare()

const trailId = computed<EntityId>(() => {
  const rawId = route.params.id
  return Array.isArray(rawId) ? rawId[0] ?? '' : String(rawId ?? '')
})

const detailFrom = computed(() => {
  const raw = route.query.from
  return Array.isArray(raw) ? raw[0] : raw
})

const trailData = ref<TrailListItem | null>(null)
const isLoading = ref(true)
const isDeleting = ref(false)
const showDeleteConfirm = ref(false)

const canEditTrail = computed(() => {
  if (!trailData.value || !userStore.isAuthenticated) return false
  return trailData.value.authorId === userStore.user?.id
})

// 地理与天气
const { locationCity, locationAdcode } = useTrailGeo(computed(() => trailData.value?.location))
const { weather, forecast, isLoading: weatherLoading } = useTrailWeather(locationAdcode)

const detailImages = computed(() => {
  if (!trailData.value) return []
  return [trailData.value.image, ...(trailData.value.gallery?.map(i => i.url) || [])].filter(Boolean)
})

const displayTrail = computed(() => {
  if (!trailData.value) return null
  const t = trailData.value
  return {
    id: t.id,
    authorId: t.authorId,
    ownedByCurrentUser: userStore.isAuthenticated && t.authorId === userStore.user?.id,
    editableByCurrentUser: userStore.isAuthenticated && t.authorId === userStore.user?.id,
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
    isLikePending: interactionStore.isLikePending(t.id),
    isFavoritePending: interactionStore.isFavoritePending(t.id),
    author: t.author,
    publishTime: t.publishTime,
  }
})

const geo = useTrailGeo(computed(() => trailData.value?.location))
const mapCenter = computed(() => geo.location.value?.center ?? null)
const detailTrackViewerData = computed(() => {
  if (!trailData.value?.track) return null
  return createTrackViewerData(trailData.value)
})

const detailWeatherScene = computed(() => mapWeatherToTrackScene(weather.value))

const isViewerFullscreen = ref(false)

const heroProps = computed(() => {
  if (!displayTrail.value) return {}
  const t = displayTrail.value
  return {
    image: t.image,
    name: t.name,
    location: t.location,
    difficultyLabel: t.difficultyLabel,
    packType: t.packType,
    durationType: t.durationType,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
    rating: t.rating,
    likes: t.likes,
    favorites: t.favorites,
    isLiked: t.likedByCurrentUser,
    isFavorited: t.favoritedByCurrentUser,
    author: t.author,
    publishTime: t.publishTime,
  }
})

const reviews = ref<ReviewItem[]>([])
const nextReviewCursor = ref<string | null>(null)
const hasMoreReviews = ref(false)
const REVIEWS_PAGE_SIZE = 5

const isSubmittingReview = ref(false)
const reviewsErrorMessage = ref('')
const loadMoreErrorMessage = ref('')
const reviewFormResetKey = ref(0)
const replyFormResetKey = ref(0)

async function fetchDetail() {
  if (!trailId.value) return

  isLoading.value = true
  try {
    const data = await fetchTrailDetail(trailId.value)
    trailData.value = data
    if (data) {
      await loadInitialReviews(data.id)
    }
  } catch (error) {
    flashStore.showError('路线详情加载失败')
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

function handleBack() {
  if (detailFrom.value) {
    router.push(detailFrom.value)
  } else {
    router.push('/')
  }
}

async function handleShare() {
  if (!trailData.value) return
  await shareTrail({
    id: trailData.value.id,
    name: trailData.value.name,
    location: trailData.value.location,
  })
}

function handleDownloadTrack() {
  if (trailData.value?.track?.downloadUrl) {
    window.open(trailData.value.track.downloadUrl, '_blank')
  }
}

function handleEditTrail() {
  if (!trailData.value || !canEditTrail.value) {
    flashStore.showError('路线发布超过48小时，不能再编辑')
    return
  }
  router.push({ name: 'Publish', query: { id: trailData.value.id, from: route.fullPath } })
}

const isDeleting = ref(false)
async function handleDeleteTrail() {
  if (!trailData.value) return
  isDeleting.value = true
  try {
    await deleteTrail(trailData.value.id)
    flashStore.showSuccess('路线已成功删除')
    router.push('/')
  } catch (error) {
    flashStore.showError('路线删除失败')
  } finally {
    isDeleting.value = false
  }
}

function openGalleryExperience() {
  if (!detailImages.value.length || !trailData.value) {
    return
  }

  void router.push({
    name: 'TrailGallery',
    params: { id: trailData.value.id },
    query: {
      from: route.fullPath,
    },
  })
}

async function handleCreateReview(payload: { rating?: number; text: string; images: string[] }) {
  if (!trailData.value || isSubmittingReview.value) return

  await submitReview({
    trailId: trailData.value.id,
    rating: payload.rating,
    text: payload.text,
    images: payload.images,
  })
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

    const inserted = insertReview(result.review)
    if (!inserted && trailData.value) {
      await loadInitialReviews(trailData.value.id)
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

async function loadInitialReviews(targetTrailId: EntityId) {
  reviews.value = []
  nextReviewCursor.value = null
  hasMoreReviews.value = false
  reviewsErrorMessage.value = ''
  loadMoreErrorMessage.value = ''

  try {
    const result = await fetchTrailReviews(targetTrailId, { limit: REVIEWS_PAGE_SIZE })
    reviews.value = result.list
    nextReviewCursor.value = result.nextCursor ?? null
    hasMoreReviews.value = result.hasMore
    return true
  } catch (error) {
    reviewsErrorMessage.value = '加载评论失败'
    return false
  }
}

async function loadMoreReviews() {
  if (!trailData.value || !hasMoreReviews.value || !nextReviewCursor.value) return

  try {
    const result = await fetchTrailReviews(trailData.value.id, {
      cursor: nextReviewCursor.value,
      limit: REVIEWS_PAGE_SIZE,
    })
    reviews.value = [...reviews.value, ...result.list]
    nextReviewCursor.value = result.nextCursor ?? null
    hasMoreReviews.value = result.hasMore
  } catch (error) {
    loadMoreErrorMessage.value = '加载更多评论失败'
  }
}

function insertReview(normalizedReview: ReviewItem): boolean {
  if (!reviews.value.length) {
    reviews.value = [normalizedReview]
    return true
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

function removeReviewNode(items: ReviewItem[], reviewId: EntityId): ReviewItem[] {
  return items
    .filter((item) => String(item.id) !== String(reviewId))
    .map((item) => ({
      ...item,
      replies: removeReviewNode(item.replies, reviewId),
    }))
}

watch(trailId, () => {
  fetchDetail()
}, { immediate: true })
</script>

<template>
  <main v-if="trailData">
    <div class="glass-header sticky top-0 left-0 w-full z-40 px-4 py-3">
      <div class="max-w-6xl mx-auto flex items-center justify-between">
        <button @click="handleBack" class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500 cursor-pointer" style="color: var(--text-secondary);">
          <BaseIcon name="ChevronLeft" :size="20" />
          返回
        </button>
        <h2 class="text-sm font-semibold" style="color: var(--text-primary);">路线详情</h2>
        <div class="flex items-center gap-1">
          <button
            v-if="canManageTrail"
            type="button"
            class="p-1.5 transition-colors disabled:cursor-not-allowed disabled:opacity-45 cursor-pointer"
            :class="canEditTrail ? 'text-secondary hover:text-primary-500' : 'text-tertiary'"
            :disabled="!canEditTrail"
            :title="canEditTrail ? '编辑路线' : '发布超过48小时后不能再编辑'"
            @click="handleEditTrail"
          >
            <BaseIcon name="Pencil" :size="20" />
          </button>
          <button
            v-if="canManageTrail"
            type="button"
            class="p-1.5 text-secondary transition-colors hover:text-primary-500 cursor-pointer"
            title="删除路线"
            @click="handleDeleteTrail"
          >
            <BaseIcon name="Trash2" :size="20" />
          </button>
          <button 
            v-if="trailData?.track?.downloadUrl"
            class="p-1.5 text-secondary transition-colors hover:text-primary-500 cursor-pointer" 
            title="下载轨迹"
            @click="handleDownloadTrack"
          >
            <BaseIcon name="Download" :size="20" />
          </button>
          <button class="p-1.5 text-secondary transition-colors hover:text-primary-500 cursor-pointer" @click="handleShare" title="分享路线">
            <BaseIcon name="Share" :size="20" />
          </button>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 py-6 pb-24 space-y-8">
      
      <!-- Top Section: Hero & Stats -->
      <DetailHero
        v-bind="heroProps"
        :gallery-count="detailImages.length"
        @toggle-like="displayTrail && interactionStore.toggleLike(displayTrail)"
        @toggle-favorite="displayTrail && interactionStore.toggleFavorite(displayTrail)"
        @share="handleShare"
        @open-gallery="openGalleryExperience"
      />

      <!-- Description Section -->
      <section class="card p-6 space-y-4">
        <div class="flex items-center gap-2 mb-2">
          <BaseIcon name="Info" :size="22" class="text-primary-500" />
          <h3 class="text-lg font-bold" style="color: var(--text-primary);">路线概览</h3>
        </div>
        <p class="text-sm leading-relaxed" style="color: var(--text-secondary);">
          {{ trailData.description }}
        </p>
        <div class="flex flex-wrap gap-2 pt-2">
          <span v-for="tag in trailData.tags" :key="tag" class="badge">
            {{ tag }}
          </span>
        </div>
      </section>

      <!-- Weather & Track Grid -->
      <div :class="['grid grid-cols-1 gap-6 items-stretch', detailTrackViewerData ? 'lg:grid-cols-2' : '']">
        <!-- Left: Weather Dashboard -->
        <div class="card p-6 flex flex-col h-full">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold flex items-center gap-2" style="color: var(--text-primary);">
              <BaseIcon name="CloudSun" :size="22" class="text-primary-500" />
              路线环境与天气
            </h3>
          </div>

          <div class="space-y-6 flex-1">
            <!-- Current Weather -->
            <WeatherSection
              :weather="weather"
              :is-loading="weatherLoading"
              :fallback-city="locationCity"
            />

            <!-- Landscape Predictions -->
            <LandscapePrediction />

            <!-- Forecast List -->
            <div class="mt-6 border-t border-white/5">
              <h4 class="text-sm font-medium mb-4" style="color: var(--text-secondary);">未来七天预报</h4>
              <WeatherForecast :forecast="forecast" />
            </div>
          </div>
        </div>

        <!-- Right: 3D Track Viewer -->
        <div v-if="detailTrackViewerData" class="h-[500px] lg:h-auto min-h-[500px]">
          <TrailTrackViewer
            class="h-full rounded-2xl overflow-hidden shadow-2xl border border-white/5"
            :data="detailTrackViewerData"
            :weather-scene="detailWeatherScene"
            mode="detail"
            :show-scroll-to-content-button="false"
          />
        </div>
      </div>

      <!-- Map Detail -->
       <TrailMapSection
        :center="mapCenter"
        :label="trailData.name"
        :city="locationCity"
        :track-geo-json="trailData.track?.geoJson"
        :track-download-url="trailData.track?.downloadUrl"
      />

      <!-- Reviews Section -->
      <section class="space-y-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <BaseIcon name="MessageSquare" :size="22" class="text-primary-500" />
            <h3 class="text-lg font-bold" style="color: var(--text-primary);">路线动态 ({{ trailData.reviewCount }})</h3>
          </div>
        </div>

        <ReviewList
          :trail-id="trailData.id"
          :initial-reviews="reviews"
          :has-more="hasMoreReviews"
          :is-loading="isLoading"
          :error-message="reviewsErrorMessage"
          :load-more-error="loadMoreErrorMessage"
          :reset-key="reviewFormResetKey"
          :reply-reset-key="replyFormResetKey"
          @submit-review="handleCreateReview"
          @submit-reply="handleCreateReply"
          @load-more="loadMoreReviews"
        />
      </section>
    </div>
  </main>
  
  <div v-else-if="isLoading" class="flex flex-col items-center justify-center min-h-[60vh] gap-4">
    <BaseIcon name="Loader2" :size="32" class="animate-spin text-primary-500" />
    <p class="text-sm font-medium text-secondary">加载路线详情...</p>
  </div>
  
  <div v-else class="flex flex-col items-center justify-center min-h-[60vh] gap-4 px-6 text-center">
    <div class="p-4 rounded-full bg-red-500/10">
      <BaseIcon name="AlertCircle" :size="32" class="text-red-500" />
    </div>
    <h2 class="text-lg font-bold" style="color: var(--text-primary);">未找到路线</h2>
    <p class="text-sm text-secondary max-w-xs">
      该路线可能已被删除或地址输入有误。
    </p>
    <button class="btn btn-primary mt-4" @click="handleBack">
      返回上一页
    </button>
  </div>
</template>

<style scoped>
.glass-header {
  background: color-mix(in srgb, var(--surface) 80%, transparent);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-subtle);
}

.card {
  background: var(--surface);
  border-radius: 1.5rem;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05);
}

.badge {
  background: var(--surface-50);
  color: var(--text-secondary);
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
  border: 1px solid var(--border-subtle);
}
</style>
