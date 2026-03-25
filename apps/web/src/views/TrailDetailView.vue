<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  fetchTrailDetail,
  fetchTrailReviews,
  createReview,
  deleteReview,
  fetchUserCard,
  deleteTrail,
} from '../mock/mockData'
import type { TrailDetail, ReviewItem, UserCard, CreateReviewPayload } from '../mock/mockData'
import type { EntityId } from '../mock/mockData'
import { useTrailWeather } from '../composables/useTrailWeather'
import { useTrailInteractionStore } from '../stores/trailInteraction'
import { useUserStore } from '../stores/useUserStore'
import { useFlashStore } from '../stores/flash'
import { shareTrail } from '../utils/share'
import BaseIcon from '../components/common/BaseIcon.vue'
import DetailHero from '../components/trail/DetailHero.vue'
import TrailMapSection from '../components/trail/TrailMapSection.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherForecast from '../components/trail/WeatherForecast.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import TrailTrackViewer from '../components/trail/TrailTrackViewer.vue'
import ReviewList from '../components/trail/ReviewList.vue'
import ConfirmDialog from '../components/common/ConfirmDialog.vue'

const route = useRoute()
const router = useRouter()
const trailInteractionStore = useTrailInteractionStore()
const userStore = useUserStore()
const flashStore = useFlashStore()

const trailId = computed(() => route.params.id as string)
const trailData = ref<TrailDetail | null>(null)
const isLoading = ref(true)
const errorMessage = ref('')

const isViewerFullscreen = ref(false)

const reviews = ref<ReviewItem[]>([])
const isLoadingMoreReviews = ref(false)
const hasMoreReviews = ref(false)
const nextReviewCursor = ref<string | null>(null)
const REVIEWS_PAGE_SIZE = 10
const reviewsErrorMessage = ref('')
const loadMoreErrorMessage = ref('')
const isSubmittingReview = ref(false)

const reviewFormResetKey = ref(0)
const replyFormResetKey = ref(0)

const isUserCardVisible = ref(false)
const activeUserCardId = ref<string | null>(null)
const isUserCardLoading = ref(false)
const userCardErrorMessage = ref('')
const userCardCache = ref<Record<string, UserCard>>({})

const pendingDeleteReview = ref<ReviewItem | null>(null)
const deletingIds = ref<string[]>([])

const pendingDeleteTrail = ref(false)
const isDeletingTrail = ref(false)

const detailFrom = computed(() => normalizeInternalPath(route.query.from))

const {
  weather,
  forecast,
  isLoading: weatherLoading,
  resolveGeo,
  resolveWeather,
  geo,
} = useTrailWeather()

const activeUserCard = computed(() => {
  if (!activeUserCardId.value) return null
  return userCardCache.value[activeUserCardId.value] || null
})

function createTrackViewerData(t: TrailDetail) {
  return {
    id: t.id,
    name: t.name,
    difficulty: t.difficulty,
    distance: t.distance,
    elevation: t.elevation,
    track: t.track,
  }
}

const displayTrail = computed(() => {
  const t = trailData.value
  if (!t) return null

  return {
    id: t.id,
    name: t.name,
    title: t.title,
    description: t.description,
    image: t.image,
    location: t.location,
    difficulty: t.difficulty,
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
const detailTrackViewerData = computed(() => trailData.value ? createTrackViewerData(trailData.value) : null)

const detailWeatherScene = computed(() => {
  if (!weather.value) return 'partly_cloudy'
  // Simplified mapping for demo
  const desc = weather.value.weather
  if (desc.includes('雨')) return 'rainy'
  if (desc.includes('雪')) return 'snowy'
  if (desc.includes('晴')) return 'sunny'
  return 'partly_cloudy'
})

const canManageTrail = computed(() => !!displayTrail.value?.ownedByCurrentUser)
const canEditTrail = computed(() => !!displayTrail.value?.editableByCurrentUser)

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

function handleEditTrail() {
  if (!displayTrail.value || !canEditTrail.value) {
    flashStore.showError('路线发布超过48小时，不能再编辑')
    return
  }

  void router.push({
    name: 'Publish',
    query: { edit: String(displayTrail.value.id) },
  })
}

function handleDeleteTrail() {
  pendingDeleteTrail.value = true
}

async function confirmDeleteTrail() {
  if (!displayTrail.value || isDeletingTrail.value) {
    return
  }

  isDeletingTrail.value = true

  try {
    await deleteTrail(displayTrail.value.id)
    flashStore.showSuccess('路线删除成功')

    if (userStore.profile) {
      userStore.profile.postCount = Math.max(userStore.profile.postCount - 1, 0)
    }

    void router.replace({
      name: 'Profile',
      query: { tab: 'posts' },
    })
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '路线删除失败')
  } finally {
    isDeletingTrail.value = false
    pendingDeleteTrail.value = false
  }
}

function closeDeleteTrailDialog() {
  if (isDeletingTrail.value) {
    return
  }
  pendingDeleteTrail.value = false
}



function normalizeInternalPath(value: unknown) {
  if (typeof value !== 'string' || !value.startsWith('/')) {
    return null
  }

  return value
}

function handleBack() {
  if (detailFrom.value) {
    void router.push(detailFrom.value)
    return
  }

  void router.back()
}

function openGalleryExperience() {
  if (!detailImages.value.length || !trailData.value) {
    return
  }

  const detailPath = router.resolve({
    name: 'TrailDetail',
    params: { id: trailData.value.id },
    query: detailFrom.value ? { from: detailFrom.value } : {},
  }).fullPath

  void router.push({
    name: 'TrailGallery',
    params: { id: trailData.value.id },
    query: {
      from: detailPath,
      ...(detailFrom.value ? { detailFrom: detailFrom.value } : {}),
    },
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

const heroProps = computed(() => {
  const t = displayTrail.value
  if (!t) return null
  return {
    id: t.id,
    title: t.title,
    author: t.author,
    publishTime: t.publishTime,
    location: t.location,
    difficulty: t.difficulty,
    distance: t.distance,
    elevation: t.elevation,
    duration: t.duration,
    rating: t.rating,
    reviewCount: t.reviewCount,
    likes: t.likes,
    likedByCurrentUser: t.likedByCurrentUser,
    favorites: t.favorites,
    favoritedByCurrentUser: t.favoritedByCurrentUser,
    isLikePending: t.isLikePending,
    isFavoritePending: t.isFavoritePending,
    image: t.image,
  }
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

  await resolveWeather({
    city: geo.value?.city || trail.location,
    signal: controller.signal,
  })
}, { immediate: true })

</script>

<template>
  <main v-if="trailData">
    <div class="glass-header sticky top-0 z-40 px-4 py-3">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
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
          <button class="p-1.5 text-secondary transition-colors hover:text-primary-500 cursor-pointer" @click="handleShare">
            <BaseIcon name="Share" :size="20" />
          </button>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 py-6 pb-24 space-y-8">
      
      <!-- Top Section: Hero & Stats -->
      <DetailHero
        v-if="heroProps"
        v-bind="heroProps"
        :gallery-count="detailImages.length"
        @toggle-like="displayTrail && trailInteractionStore.toggleLike(displayTrail)"
        @toggle-favorite="displayTrail && trailInteractionStore.toggleFavorite(displayTrail)"
        @share="handleShare"
        @open-gallery="openGalleryExperience"
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

      <!-- Map Section -->
      <TrailMapSection
        :center="mapCenter"
        :label="displayTrail?.name ?? trailData.name"
        :city="locationCity"
        :track-geo-json="trailData.track?.geoJson"
        :track-download-url="trailData.track?.downloadUrl"
      />

      <!-- Conditions & 3D Viewer Section (Side-by-Side on Desktop) -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
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
        <div class="h-[500px] lg:h-auto min-h-[500px]">
          <TrailTrackViewer
            v-if="detailTrackViewerData"
            class="h-full rounded-2xl overflow-hidden shadow-2xl border border-white/5"
            :data="detailTrackViewerData"
            :weather-scene="detailWeatherScene"
            mode="detail"
            :show-scroll-to-content-button="false"
            @request-fullscreen="isViewerFullscreen = true"
          />
        </div>
      </div>

      <!-- Reviews Section (Full Width) -->
      <div class="pt-8 border-t border-white/5">
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

    <ConfirmDialog
      :show="pendingDeleteTrail"
      title="删除路线"
      message="确认删除这条路线吗？删除后将不会再出现在前台列表和详情页。"
      confirm-text="确认删除"
      cancel-text="暂不删除"
      tone="danger"
      :confirm-loading="isDeletingTrail"
      @update:show="(value) => { if (!value) closeDeleteTrailDialog() }"
      @cancel="closeDeleteTrailDialog"
      @confirm="confirmDeleteTrail"
    />

  </main>
  <main v-else class="max-w-4xl mx-auto px-4 sm:px-6 py-10">
    <div class="card p-8 text-center">
      <p v-if="isLoading" class="text-sm" style="color: var(--text-secondary);">正在加载路线详情...</p>
      <p v-else-if="errorMessage" class="text-sm" style="color: var(--color-hard);">{{ errorMessage }}</p>
      <p v-else class="text-sm" style="color: var(--text-secondary);">未找到该路线</p>
    </div>
  </main>

  <!-- Fullscreen Track Viewer -->
  <div
    v-if="isViewerFullscreen"
    class="fixed inset-0 z-[100] bg-black animate-fade-in"
  >
    <TrailTrackViewer
      v-if="detailTrackViewerData"
      class="h-full w-full"
      mode="fullscreen"
      :data="detailTrackViewerData"
      :weather-scene="detailWeatherScene"
      @exit-fullscreen="isViewerFullscreen = false"
    />
  </div>
</template>
