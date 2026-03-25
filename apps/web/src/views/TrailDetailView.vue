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

const detailTrackViewerData = computed(() => {
  if (!trailData.value?.track) return null
  return createTrackViewerData(trailData.value)
})

const detailWeatherScene = computed(() => mapWeatherToTrackScene(weather.value))

const isViewerFullscreen = ref(false)

async function fetchDetail() {
  if (!trailId.value) return

  isLoading.value = true
  try {
    const data = await fetchTrailDetail(trailId.value)
    trailData.value = data
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

async function confirmDelete() {
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
    showDeleteConfirm.value = false
  }
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

watch(trailId, () => {
  fetchDetail()
}, { immediate: true })
</script>

<template>
  <div class="min-h-screen pb-20">
    <!-- Header -->
    <header class="glass-header sticky top-0 z-50 animate-fade-in">
      <div class="mx-auto max-w-7xl px-4 py-3">
        <div class="flex items-center justify-between">
          <button class="gallery-action" @click="handleBack">
            <BaseIcon name="ChevronLeft" :size="18" />
            返回
          </button>
          <div class="text-base font-bold text-primary-900 dark:text-white">路线详情</div>
          <div class="flex items-center gap-1.5 overflow-hidden">
            <template v-if="canEditTrail">
              <button 
                class="p-1.5 text-secondary transition-colors hover:text-primary-500 cursor-pointer" 
                title="编辑详情"
                @click="handleEditTrail"
              >
                <BaseIcon name="Edit3" :size="20" />
              </button>
              <button 
                class="p-1.5 text-secondary transition-colors hover:text-red-500 cursor-pointer" 
                title="删除路线"
                @click="showDeleteConfirm = true"
              >
                <BaseIcon name="Trash2" :size="20" />
              </button>
            </template>
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
    </header>

    <div v-if="isLoading" class="flex min-h-[60vh] flex-col items-center justify-center gap-4">
      <BaseIcon name="Loader2" :size="32" class="animate-spin text-primary-500" />
      <p class="text-sm font-medium text-secondary">加载路线详情...</p>
    </div>

    <div v-else-if="!trailData" class="mx-auto flex max-w-md flex-col items-center justify-center p-12 text-center">
      <div class="mb-4 rounded-full bg-red-500/10 p-4">
        <BaseIcon name="AlertCircle" :size="32" class="text-red-500" />
      </div>
      <h2 class="text-lg font-bold text-primary-900 dark:text-white">未找到路线</h2>
      <p class="mt-2 text-sm text-secondary">该路线可能已被删除或地址输入有误。</p>
      <button class="btn btn-primary mt-6" @click="router.push('/')">回到首页</button>
    </div>

    <main v-else class="mx-auto max-w-7xl px-4 pt-6">
      <DetailHero :trail="trailData" />

      <div class="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-12 lg:items-start">
        <!-- Main Content (Left) -->
        <article class="space-y-10 lg:col-span-8">
          <!-- Overview Section -->
          <section class="animate-fade-in-up stagger-1">
            <div class="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
              <div class="flex-1">
                <h2 class="text-xl font-bold text-primary-900 dark:text-white">路线概览</h2>
                <p class="mt-3 leading-relaxed text-secondary">{{ trailData.description }}</p>
                <div class="mt-4 flex flex-wrap gap-2">
                  <span
                    v-for="tag in trailData.tags"
                    :key="tag"
                    class="rounded-full bg-surface-50 px-3 py-1 text-xs font-medium text-secondary dark:bg-zinc-800"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>

              <!-- Media Showcase Button -->
              <div class="flex shrink-0 gap-3">
                <button 
                  class="flex items-center gap-2 rounded-full border border-primary-500/20 bg-primary-500/5 px-4 py-2 text-sm font-medium text-primary-500 transition-all hover:bg-primary-500 hover:text-white"
                  @click="openGalleryExperience"
                >
                  <BaseIcon name="Image" :size="18" />
                  图片漫游
                </button>
              </div>
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
                @request-fullscreen="isViewerFullscreen = true"
              />
            </div>
          </div>

          <!-- Map Section -->
          <TrailMapSection
            :center="null"
            :label="trailData.name"
            :city="locationCity"
            :track-geo-json="trailData.track?.geoJson"
            :track-download-url="trailData.track?.downloadUrl"
          />

          <!-- Reviews Section -->
          <section class="animate-fade-in-up stagger-4">
            <h2 class="mb-4 text-xl font-bold text-primary-900 dark:text-white">
              路线动态 ({{ trailData.reviewCount }})
            </h2>
            <ReviewList :trail-id="trailData.id" />
          </section>
        </article>

        <!-- Sidebar (Right) -->
        <aside class="space-y-6 lg:col-span-4 lg:sticky lg:top-24">
          <!-- Sidebar components could go here -->
        </aside>
      </div>
    </main>

    <!-- Fullscreen 3D Viewer Portal -->
    <Teleport to="body">
      <div 
        v-if="isViewerFullscreen && detailTrackViewerData" 
        class="fixed inset-0 z-[100] bg-black animate-fade-in flex flex-col"
      >
        <div class="relative flex-1">
           <TrailTrackViewer
            class="w-full h-full"
            :data="detailTrackViewerData"
            :weather-scene="detailWeatherScene"
            mode="fullscreen"
           />
           <button 
            class="absolute top-6 left-6 z-[101] flex items-center gap-2 rounded-full border border-white/20 bg-black/40 px-4 py-2 text-sm font-medium text-white backdrop-blur-md transition-all hover:bg-black/60"
            @click="isViewerFullscreen = false"
          >
            <BaseIcon name="Minimize2" :size="18" />
            退出全屏
          </button>
        </div>
      </div>
    </Teleport>

    <!-- Delete Confirmation -->
    <ConfirmDialog
      v-model="showDeleteConfirm"
      title="删除路线"
      message="确定要删除这条路线吗？此操作不可撤销。"
      confirm-text="确认删除"
      confirm-variant="danger"
      :loading="isDeleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.glass-header {
  background: color-mix(in srgb, var(--surface) 80%, transparent);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-subtle);
}

.gallery-action {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 999px;
  background: var(--surface-50);
  color: var(--text-primary);
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
  cursor: pointer;
}

.gallery-action:hover {
  background: var(--surface-100);
  transform: translateX(-2px);
}

.card {
  background: var(--surface);
  border-radius: 1.5rem;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.05);
}
</style>
