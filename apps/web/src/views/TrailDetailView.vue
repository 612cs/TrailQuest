<script setup lang="ts">
import { computed, ref, useTemplateRef, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import BaseIcon from '../components/common/BaseIcon.vue'
import ConfirmDialog from '../components/common/ConfirmDialog.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import ReviewList from '../components/trail/ReviewList.vue'
import TrailMapSection from '../components/trail/TrailMapSection.vue'
import TrailTrackViewer from '../components/trail/TrailTrackViewer.vue'
import WeatherAlert from '../components/trail/WeatherAlert.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import { createReview, deleteReview, fetchTrailReviews, fetchUserCard } from '../api/reviews'
import { fetchTrailDetail } from '../api/trails'
import { useTrailShare } from '../composables/useTrailShare'
import { useTrailGeo } from '../composables/useTrailGeo'
import { useTrailWeather } from '../composables/useTrailWeather'
import { useFlashStore } from '../stores/useFlashStore'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import type { EntityId } from '../types/id'
import type { TrailListItem } from '../types/trail'
import { createTrackViewerData } from '../utils/trailTrackViewerAdapter'
import { mapWeatherToTrackScene } from '../utils/trackWeatherScene'
import type {
  CreateReviewPayload,
  ReviewItem,
  UserCard,
} from '../types/review'

const REVIEWS_PAGE_SIZE = 10

const router = useRouter()
const route = useRoute()
const flashStore = useFlashStore()
const trailInteractionStore = useTrailInteractionStore()
const { shareTrail } = useTrailShare()

const reviews = ref<ReviewItem[]>([])
const trailData = ref<TrailListItem | null>(null)
const isLoading = ref(false)
const isLoadingMoreReviews = ref(false)
const isSubmittingReview = ref(false)
const errorMessage = ref('')
const reviewsErrorMessage = ref('')
const loadMoreErrorMessage = ref('')
const reviewFormResetKey = ref(0)
const replyFormResetKey = ref(0)
const nextReviewCursor = ref<string | null>(null)
const hasMoreReviews = ref(false)
const deletingIds = ref<string[]>([])
const pendingDeleteReview = ref<ReviewItem | null>(null)
const userCardCache = ref<Record<string, UserCard>>({})
const activeUserCardId = ref<string | null>(null)
const isUserCardVisible = ref(false)
const isUserCardLoading = ref(false)
const userCardErrorMessage = ref('')
const detailContentAnchorRef = useTemplateRef<HTMLDivElement>('detailContentAnchor')
const { geo, isLoading: geoLoading, resolve: resolveGeo } = useTrailGeo()
const { weather, isLoading: weatherLoading, resolve: resolveWeather } = useTrailWeather()

const trailId = computed<EntityId>(() => {
  const rawId = route.params.id
  return Array.isArray(rawId) ? rawId[0] ?? '' : String(rawId ?? '')
})
const displayTrail = computed(() => {
  if (!trailData.value) return null
  return trailInteractionStore.applyToTrail(trailData.value)
})
const activeUserCard = computed(() => {
  if (!activeUserCardId.value) return null
  return userCardCache.value[activeUserCardId.value] ?? null
})

watch(trailId, async (newId) => {
  if (!newId) {
    reviews.value = []
    nextReviewCursor.value = null
    hasMoreReviews.value = false
    return
  }

  await loadInitialReviews(newId)
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
const detailTrackViewerData = computed(() => createTrackViewerData({
  title: trailData.value?.name ?? null,
  fileName: trailData.value?.track?.originalFileName ?? null,
  distanceMeters: typeof trailData.value?.track?.distanceMeters === 'number'
    ? trailData.value.track.distanceMeters
    : null,
  elevationGainMeters: typeof trailData.value?.track?.elevationGainMeters === 'number'
    ? trailData.value.track.elevationGainMeters
    : null,
  geoJson: trailData.value?.track?.geoJson,
}))
const detailWeatherScene = computed(() => mapWeatherToTrackScene(
  weather.value?.weather,
  weather.value?.windPower,
))
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
    reviews.value = []
    reviewsErrorMessage.value = error instanceof Error ? error.message : '评论加载失败'
    return false
  }
}

async function loadMoreReviews() {
  if (!trailData.value || !hasMoreReviews.value || isLoadingMoreReviews.value || !nextReviewCursor.value) return

  isLoadingMoreReviews.value = true
  loadMoreErrorMessage.value = ''

  try {
    const result = await fetchTrailReviews(trailData.value.id, {
      limit: REVIEWS_PAGE_SIZE,
      cursor: nextReviewCursor.value,
    })
    reviews.value = [...reviews.value, ...result.list]
    nextReviewCursor.value = result.nextCursor ?? null
    hasMoreReviews.value = result.hasMore
  } catch (error) {
    loadMoreErrorMessage.value = error instanceof Error ? error.message : '加载更多评论失败'
  } finally {
    isLoadingMoreReviews.value = false
  }
}

async function retryLoadMoreReviews() {
  await loadMoreReviews()
}

async function handleDeleteReview(review: ReviewItem) {
  if (!trailData.value) return
  pendingDeleteReview.value = review
}

async function confirmDeleteReview() {
  if (!trailData.value || !pendingDeleteReview.value) return

  const review = pendingDeleteReview.value

  deletingIds.value = [...deletingIds.value, String(review.id)]

  try {
    const result = await deleteReview(review.id)
    reviews.value = removeReviewNode(reviews.value, result.deletedReviewId)
    trailData.value = {
      ...trailData.value,
      reviewCount: result.trailReviewCount,
      rating: result.trailRating,
    }
    pendingDeleteReview.value = null
    flashStore.showSuccess('评论已删除')
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '评论删除失败')
  } finally {
    deletingIds.value = deletingIds.value.filter((id) => id !== String(review.id))
  }
}

function closeDeleteConfirm() {
  if (pendingDeleteReview.value && deletingIds.value.includes(String(pendingDeleteReview.value.id))) {
    return
  }
  pendingDeleteReview.value = null
}

async function openUserCard(userId: EntityId) {
  const normalizedUserId = String(userId)
  activeUserCardId.value = normalizedUserId
  isUserCardVisible.value = true
  userCardErrorMessage.value = ''

  if (userCardCache.value[normalizedUserId]) {
    return
  }

  isUserCardLoading.value = true
  try {
    const card = await fetchUserCard(normalizedUserId)
    userCardCache.value = {
      ...userCardCache.value,
      [normalizedUserId]: card,
    }
  } catch (error) {
    userCardErrorMessage.value = error instanceof Error ? error.message : '用户卡片加载失败'
  } finally {
    isUserCardLoading.value = false
  }
}

function closeUserCard() {
  isUserCardVisible.value = false
  userCardErrorMessage.value = ''
}

async function handleShare() {
  if (!displayTrail.value) return
  await shareTrail({
    id: displayTrail.value.id,
    name: displayTrail.value.name,
    location: displayTrail.value.location,
  })
}

function scrollToDetailContent() {
  detailContentAnchorRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
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

function removeReviewNode(items: ReviewItem[], reviewId: EntityId): ReviewItem[] {
  return items
    .filter((item) => String(item.id) !== String(reviewId))
    .map((item) => ({
      ...item,
      replies: removeReviewNode(item.replies, reviewId),
    }))
}
</script>

<template>
  <main v-if="trailData">
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
      <TrailTrackViewer
        v-if="detailTrackViewerData"
        :data="detailTrackViewerData"
        :weather-scene="detailWeatherScene"
        mode="detail"
        :show-scroll-to-content-button="true"
        @scroll-to-content="scrollToDetailContent"
      />

      <div ref="detailContentAnchor" class="sr-only" aria-hidden="true"></div>

      <DetailHero
        v-bind="heroProps"
        @toggle-like="displayTrail && trailInteractionStore.toggleLike(displayTrail)"
        @toggle-favorite="displayTrail && trailInteractionStore.toggleFavorite(displayTrail)"
        @share="handleShare"
      />

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
        :track-geo-json="trailData.track?.geoJson"
        :track-download-url="trailData.track?.downloadUrl"
      />

      <WeatherSection
        :weather="weather"
        :is-loading="weatherLoading"
        :fallback-city="locationCity"
      />

      <WeatherAlert
        title="天气提醒"
        :message="weatherAlertMessage"
      />

      <LandscapePrediction />

      <ReviewList
        :reviews="reviews"
        :average-rating="trailData.rating"
        :total-reviews="trailData.reviewCount"
        :is-submitting="isSubmittingReview"
        :is-loading-more="isLoadingMoreReviews"
        :has-more="hasMoreReviews"
        :error-message="reviewsErrorMessage"
        :load-more-error="loadMoreErrorMessage"
        :review-form-reset-key="reviewFormResetKey"
        :reply-form-reset-key="replyFormResetKey"
        :active-user-card="activeUserCard"
        :is-user-card-loading="isUserCardLoading"
        :user-card-error-message="userCardErrorMessage"
        :deleting-ids="deletingIds"
        :user-card-visible="isUserCardVisible"
        @submit-review="handleCreateReview"
        @submit-reply="handleCreateReply"
        @load-more="loadMoreReviews"
        @retry-load-more="retryLoadMoreReviews"
        @delete-review="handleDeleteReview"
        @open-user-card="openUserCard"
        @close-user-card="closeUserCard"
      />
    </div>

    <ConfirmDialog
      :show="!!pendingDeleteReview"
      title="删除评论"
      message="确认删除这条评论吗？删除后无法恢复。"
      confirm-text="确认删除"
      cancel-text="暂不删除"
      tone="danger"
      :confirm-loading="!!pendingDeleteReview && deletingIds.includes(String(pendingDeleteReview.id))"
      @update:show="(value) => { if (!value) closeDeleteConfirm() }"
      @cancel="closeDeleteConfirm"
      @confirm="confirmDeleteReview"
    />
  </main>
  <main v-else class="max-w-4xl mx-auto px-4 sm:px-6 py-10">
    <div class="card p-8 text-center">
      <p v-if="isLoading" class="text-sm" style="color: var(--text-secondary);">正在加载路线详情...</p>
      <p v-else-if="errorMessage" class="text-sm" style="color: var(--color-hard);">{{ errorMessage }}</p>
      <p v-else class="text-sm" style="color: var(--text-secondary);">未找到该路线</p>
    </div>
  </main>
</template>
