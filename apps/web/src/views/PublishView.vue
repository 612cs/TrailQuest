<script setup lang="ts">
import { computed, nextTick, ref, shallowRef, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import toGeoJSON from '@mapbox/togeojson'

import BaseIcon from '../components/common/BaseIcon.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import { createTrail } from '../api/trails'
import { useAmapLoader } from '../composables/useAmapLoader'
import { useOssImageUploader } from '../composables/useOssImageUploader'
import { useOssTrackUploader } from '../composables/useOssTrackUploader'
import { useFlashStore } from '../stores/useFlashStore'
import { presetTags } from '../mock/mockData'

type TrackCoordinate = [number, number, number?]

const router = useRouter()
const flashStore = useFlashStore()

const name = ref('')
const location = ref('')
const difficulty = ref<'easy' | 'moderate' | 'hard'>('moderate')
const packType = ref<'light' | 'heavy' | 'both'>('light')
const durationType = ref<'single_day' | 'multi_day'>('single_day')
const distance = ref('')
const elevation = ref('')
const duration = ref('')
const description = ref('')
const selectedTags = ref<string[]>([])
const customTag = ref('')

const isSubmitting = ref(false)
const showSuccess = ref(false)
const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)

const mapRef = useTemplateRef<HTMLDivElement>('mapContainer')
const mapInstance = shallowRef<any>(null)
const geoJsonData = ref<any>(null)
const mapError = ref('')
const mapLoading = ref(false)

const coverUploader = useOssImageUploader({ bizType: 'trail_cover', max: 1 })
const galleryUploader = useOssImageUploader({ bizType: 'trail_gallery', max: 9 })
const trackUploader = useOssTrackUploader()

const difficultyOptions = [
  { value: 'easy' as const, label: '简单', color: 'var(--color-easy)' },
  { value: 'moderate' as const, label: '适中', color: 'var(--color-primary-500)' },
  { value: 'hard' as const, label: '困难', color: 'var(--color-hard)' },
]

const packOptions = [
  { value: 'light' as const, label: '轻装' },
  { value: 'heavy' as const, label: '重装' },
  { value: 'both' as const, label: '轻重皆可' },
]

const durationOptions = [
  { value: 'single_day' as const, label: '单日' },
  { value: 'multi_day' as const, label: '多日' },
]

const difficultyLabelMap = {
  easy: '简单',
  moderate: '适中',
  hard: '困难',
} as const

const coverItems = computed(() => coverUploader.items.value)
const galleryItems = computed(() => galleryUploader.items.value)
const trackItem = computed(() => trackUploader.item.value)

const isAnyUploading = computed(() =>
  coverUploader.isUploading.value || galleryUploader.isUploading.value || trackUploader.isUploading.value,
)

const isFormValid = computed(() => {
  return !!name.value.trim()
    && !!location.value.trim()
    && !!description.value.trim()
    && selectedTags.value.length > 0
    && !!coverUploader.items.value[0]?.mediaId
    && !isAnyUploading.value
})

function toggleTag(tag: string) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

function addCustomTag() {
  const tag = customTag.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
  customTag.value = ''
}

function openPreview(images: string[], index: number) {
  previewImages.value = images
  previewIndex.value = index
  showPreview.value = true
}

async function renderMapWithGeoJSON() {
  if (!geoJsonData.value) return

  await nextTick()
  if (!mapRef.value) return

  const AMap = await useAmapLoader().load()
  if (!AMap) {
    mapError.value = '获取高德地图服务失败'
    return
  }

  if (!mapInstance.value) {
    mapInstance.value = new AMap.Map(mapRef.value, {
      zoom: 11,
      resizeEnable: true,
    })
  }

  mapInstance.value.clearMap()

  AMap.plugin('AMap.GeoJSON', () => {
    const geojsonObj = new AMap.GeoJSON({
      geoJSON: geoJsonData.value,
      getPolyline(_geojson: unknown, lnglats: unknown) {
        return new AMap.Polyline({
          path: lnglats,
          strokeColor: '#2f6f36',
          strokeWeight: 6,
          strokeOpacity: 0.85,
          lineJoin: 'round',
          lineCap: 'round',
          showDir: true,
        })
      },
      getMarker(geojson: any, lnglats: unknown) {
        return new AMap.Marker({
          position: lnglats,
          title: geojson.properties?.name || '轨迹点',
        })
      },
    })

    mapInstance.value.add(geojsonObj)
    mapInstance.value.setFitView()
  })
}

async function handleCoverChange(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (!files?.length) return
  await coverUploader.addFiles(files)
  ;(event.target as HTMLInputElement).value = ''
}

async function handleGalleryChange(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (!files?.length) return
  await galleryUploader.addFiles(files)
  ;(event.target as HTMLInputElement).value = ''
}

async function handleTrackChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  await parseTrackPreview(file)
  await trackUploader.setFile(file)
  ;(event.target as HTMLInputElement).value = ''
}

async function parseTrackPreview(file: File) {
  mapLoading.value = true
  mapError.value = ''

  try {
    const text = await file.text()
    const doc = new DOMParser().parseFromString(text, 'text/xml')
    const extension = file.name.split('.').pop()?.toLowerCase()
    geoJsonData.value = extension === 'kml' ? toGeoJSON.kml(doc) : toGeoJSON.gpx(doc)
    applyTrackSummary(geoJsonData.value)
    await renderMapWithGeoJSON()
  } catch (error) {
    console.error(error)
    geoJsonData.value = null
    mapError.value = '解析轨迹文件失败，请检查 GPX/KML 格式。'
  } finally {
    mapLoading.value = false
  }
}

function applyTrackSummary(geoJson: any) {
  const coordinates = extractCoordinates(geoJson)
  if (coordinates.length < 2) {
    return
  }

  const totalDistance = coordinates.reduce((sum, current, index) => {
    if (index === 0) return sum
    const previous = coordinates[index - 1]
    return previous ? sum + haversine(previous, current) : sum
  }, 0)

  let gain = 0
  for (let i = 1; i < coordinates.length; i += 1) {
    const previous = coordinates[i - 1]
    const current = coordinates[i]
    if (!previous || !current) continue
    const prevEle = previous[2]
    const nextEle = current[2]
    if (typeof prevEle === 'number' && typeof nextEle === 'number' && nextEle > prevEle) {
      gain += nextEle - prevEle
    }
  }

  if (!distance.value.trim()) {
    distance.value = `${(totalDistance / 1000).toFixed(1).replace(/\.0$/, '')} km`
  }
  if (!elevation.value.trim() && gain > 0) {
    elevation.value = `+${Math.round(gain)} m`
  }
}

function extractCoordinates(geoJson: any): TrackCoordinate[] {
  const coordinates: TrackCoordinate[] = []
  const features = Array.isArray(geoJson?.features) ? geoJson.features : []

  for (const feature of features) {
    const geometry = feature?.geometry
    if (!geometry) continue

    if (geometry.type === 'LineString' && Array.isArray(geometry.coordinates)) {
      geometry.coordinates.forEach((coordinate: unknown) => {
        if (isTrackCoordinate(coordinate)) {
          coordinates.push(coordinate)
        }
      })
    }

    if (geometry.type === 'MultiLineString' && Array.isArray(geometry.coordinates)) {
      geometry.coordinates.forEach((line: unknown) => {
        if (!Array.isArray(line)) return
        line.forEach((coordinate: unknown) => {
          if (isTrackCoordinate(coordinate)) {
            coordinates.push(coordinate)
          }
        })
      })
    }
  }

  return coordinates
}

function isTrackCoordinate(value: unknown): value is TrackCoordinate {
  return Array.isArray(value)
    && typeof value[0] === 'number'
    && typeof value[1] === 'number'
}

function haversine(start: TrackCoordinate, end: TrackCoordinate) {
  const toRad = (value: number) => (value * Math.PI) / 180
  const lat1 = toRad(start[1])
  const lat2 = toRad(end[1])
  const deltaLat = lat2 - lat1
  const deltaLon = toRad(end[0] - start[0])

  const a = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) ** 2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return 6371000 * c
}

function clearTrack() {
  trackUploader.clear()
  geoJsonData.value = null
  mapError.value = ''
}

async function handleSubmit() {
  if (!isFormValid.value) return

  isSubmitting.value = true

  try {
    const coverMediaId = coverItems.value[0]?.mediaId ? Number(coverItems.value[0].mediaId) : null
    if (!coverMediaId) {
      throw new Error('请先上传路线封面')
    }

    const galleryMediaIds = galleryItems.value
      .filter((item) => item.mediaId)
      .map((item) => Number(item.mediaId))
      .filter((mediaId) => Number.isFinite(mediaId))

    const trackMediaId = trackItem.value?.mediaId ? Number(trackItem.value.mediaId) : null

    const created = await createTrail({
      name: name.value.trim(),
      location: location.value.trim(),
      difficulty: difficulty.value,
      difficultyLabel: difficultyLabelMap[difficulty.value],
      packType: packType.value,
      durationType: durationType.value,
      distance: distance.value.trim() || undefined,
      elevation: elevation.value.trim() || undefined,
      duration: duration.value.trim() || undefined,
      description: description.value.trim(),
      coverMediaId,
      galleryMediaIds,
      trackMediaId,
      tags: selectedTags.value,
    })

    showSuccess.value = true
    flashStore.showSuccess('路线发布成功')
    setTimeout(() => {
      showSuccess.value = false
      void router.push(`/trail/${created.id}`)
    }, 1200)
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '路线发布失败')
  } finally {
    isSubmitting.value = false
  }
}

function getImageSources(items: { localUrl: string; remoteUrl: string }[]) {
  return items.map((item) => item.remoteUrl || item.localUrl)
}
</script>

<template>
  <main>
    <div class="glass-header sticky top-14 z-40 px-4 py-3 sm:top-16">
      <div class="mx-auto flex max-w-3xl items-center justify-between">
        <button
          class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500"
          style="color: var(--text-secondary);"
          @click="router.back()"
        >
          <BaseIcon name="ChevronLeft" :size="20" />
          返回
        </button>
        <h2 class="text-sm font-semibold" style="color: var(--text-primary);">发布路线</h2>
        <div class="w-12" />
      </div>
    </div>

    <div class="mx-auto max-w-3xl space-y-5 px-4 py-6 sm:px-6">
      <section class="card animate-fade-in-up p-4 sm:p-5">
        <h3 class="mb-3 flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="Image" :size="16" class="text-primary-500" />
          封面图片
        </h3>
        <div class="space-y-3">
          <div v-if="coverItems.length" class="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div
              v-for="(item, index) in coverItems"
              :key="item.id"
              class="group relative aspect-[4/3] overflow-hidden rounded-xl border"
              style="border-color: var(--border-default);"
            >
              <img :src="item.remoteUrl || item.localUrl" :alt="item.id" class="h-full w-full object-cover" @click="openPreview(getImageSources(coverItems), index)" />
              <div v-if="item.status === 'uploading'" class="absolute inset-0 flex items-center justify-center bg-black/40 text-white text-sm font-medium">
                {{ item.progress }}%
              </div>
              <div v-else-if="item.status === 'error'" class="absolute inset-0 flex items-center justify-center bg-black/55 px-3 text-center text-xs text-white">
                {{ item.errorMessage }}
              </div>
              <button
                class="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-black/60 text-white transition hover:bg-red-500"
                @click="coverUploader.removeItem(item.id)"
              >
                <BaseIcon name="X" :size="14" />
              </button>
            </div>
          </div>

          <label
            v-if="!coverItems.length"
            class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
            style="border-color: var(--border-default);"
          >
            <div class="flex h-10 w-10 items-center justify-center rounded-full" style="background-color: var(--bg-tag);">
              <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
            </div>
            <p class="text-sm font-medium" style="color: var(--text-secondary);">上传路线封面</p>
            <input type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="handleCoverChange" />
          </label>
        </div>
      </section>

      <section class="card animate-fade-in-up space-y-4 p-4 sm:p-5">
        <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="FileText" :size="16" class="text-primary-500" />
          基本信息
        </h3>

        <div>
          <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">路线名称</label>
          <input v-model="name" type="text" placeholder="例如：龙脊梯田精华线" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
        </div>

        <div>
          <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">所在位置</label>
          <input v-model="location" type="text" placeholder="例如：广西 桂林 龙胜" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
        </div>

        <div>
          <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">难度等级</label>
          <div class="flex gap-2">
            <button
              v-for="opt in difficultyOptions"
              :key="opt.value"
              type="button"
              class="flex-1 rounded-lg border-2 py-2 text-sm font-medium transition-all duration-200"
              :class="difficulty === opt.value ? 'scale-[1.02]' : 'opacity-70 hover:opacity-90'"
              :style="{ borderColor: difficulty === opt.value ? opt.color : 'var(--border-default)', backgroundColor: difficulty === opt.value ? `${opt.color}15` : 'transparent', color: difficulty === opt.value ? opt.color : 'var(--text-secondary)' }"
              @click="difficulty = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">负重类型</label>
            <div class="flex gap-2">
              <button
                v-for="opt in packOptions"
                :key="opt.value"
                type="button"
                class="flex-1 rounded-lg border px-3 py-2 text-xs font-medium transition-colors"
                :class="packType === opt.value ? 'border-primary-500 bg-primary-500/10 text-primary-500' : ''"
                :style="packType !== opt.value ? 'border-color: var(--border-default); color: var(--text-secondary);' : ''"
                @click="packType = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>
          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">行程类型</label>
            <div class="flex gap-2">
              <button
                v-for="opt in durationOptions"
                :key="opt.value"
                type="button"
                class="flex-1 rounded-lg border px-3 py-2 text-xs font-medium transition-colors"
                :class="durationType === opt.value ? 'border-primary-500 bg-primary-500/10 text-primary-500' : ''"
                :style="durationType !== opt.value ? 'border-color: var(--border-default); color: var(--text-secondary);' : ''"
                @click="durationType = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="card animate-fade-in-up space-y-4 p-4 sm:p-5">
        <div class="flex items-center justify-between">
          <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
            <BaseIcon name="Map" :size="16" class="text-primary-500" />
            路线轨迹 (GPX / KML)
          </h3>
          <label class="cursor-pointer rounded-lg bg-primary-500/10 px-3 py-1.5 text-xs font-medium text-primary-500 transition-colors hover:bg-primary-500/20">
            {{ trackItem ? '重新上传' : '上传轨迹' }}
            <input type="file" accept=".gpx,.kml,application/gpx+xml,application/vnd.google-earth.kml+xml,text/xml,application/xml" class="hidden" @change="handleTrackChange" />
          </label>
        </div>

        <p class="text-xs" style="color: var(--text-secondary);">
          轨迹文件为可选项。上传后会自动预览路线并尽量补全距离、海拔和时长，后续详情页也可直接下载原始轨迹。
        </p>

        <div v-if="trackItem" class="rounded-xl border p-3 text-sm" style="border-color: var(--border-default); background-color: var(--bg-tag);">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="truncate font-medium" style="color: var(--text-primary);">{{ trackItem.fileName }}</p>
              <p class="mt-1 text-xs" style="color: var(--text-secondary);">
                <span v-if="trackItem.status === 'uploading'">上传中 {{ trackItem.progress }}%</span>
                <span v-else-if="trackItem.status === 'success'">轨迹文件已上传，发布时将以后端解析结果为准</span>
                <span v-else-if="trackItem.status === 'error'">{{ trackItem.errorMessage }}</span>
              </p>
            </div>
            <button class="flex h-8 w-8 items-center justify-center rounded-full transition-colors hover:bg-primary-500/10" @click="clearTrack">
              <BaseIcon name="X" :size="16" style="color: var(--text-secondary);" />
            </button>
          </div>
        </div>

        <div class="relative overflow-hidden rounded-xl shadow-inner" style="height: 240px; background-color: var(--bg-input); border: 1px solid var(--border-default);">
          <div ref="mapContainer" class="h-full w-full" :style="{ display: geoJsonData ? 'block' : 'none' }"></div>
          <div v-if="mapLoading" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5 backdrop-blur-sm">
            <BaseIcon name="Loader2" :size="20" class="animate-spin text-primary-500" />
            <span class="text-xs font-medium" style="color: var(--text-secondary);">解析轨迹中...</span>
          </div>
          <div v-else-if="mapError" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5">
            <BaseIcon name="AlertCircle" :size="20" class="text-red-500" />
            <span class="px-4 text-center text-xs font-medium text-red-500">{{ mapError }}</span>
          </div>
          <div v-else-if="!geoJsonData" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5">
            <BaseIcon name="Route" :size="24" style="color: var(--text-tertiary);" />
            <span class="text-xs font-medium" style="color: var(--text-tertiary);">请上传 GPX / KML 轨迹文件以在地图中预览</span>
          </div>
        </div>
      </section>

      <section class="card animate-fade-in-up space-y-4 p-4 sm:p-5">
        <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="Ruler" :size="16" class="text-primary-500" />
          路线参数
        </h3>
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">距离</label>
            <input v-model="distance" type="text" placeholder="12.5 km" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
          </div>
          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">海拔</label>
            <input v-model="elevation" type="text" placeholder="+450 m" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
          </div>
          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">时长</label>
            <input v-model="duration" type="text" placeholder="4h 30m" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
          </div>
        </div>
      </section>

      <section class="card animate-fade-in-up space-y-3 p-4 sm:p-5">
        <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="AlignLeft" :size="16" class="text-primary-500" />
          路线描述
        </h3>
        <textarea v-model="description" rows="5" placeholder="描述这条路线的特色、注意事项、推荐理由..." class="w-full resize-none rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" />
      </section>

      <section class="card animate-fade-in-up p-4 sm:p-5">
        <h3 class="mb-3 flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="Camera" :size="16" class="text-primary-500" />
          路线照片
        </h3>
        <div class="space-y-3">
          <div v-if="galleryItems.length" class="grid grid-cols-3 gap-2 sm:grid-cols-4">
            <div
              v-for="(item, index) in galleryItems"
              :key="item.id"
              class="group relative aspect-square overflow-hidden rounded-lg"
            >
              <img :src="item.remoteUrl || item.localUrl" :alt="item.id" class="h-full w-full object-cover" @click="openPreview(getImageSources(galleryItems), index)" />
              <div v-if="item.status === 'uploading'" class="absolute inset-0 flex items-center justify-center bg-black/40 text-xs font-medium text-white">
                {{ item.progress }}%
              </div>
              <div v-else-if="item.status === 'error'" class="absolute inset-0 flex items-center justify-center bg-black/55 px-2 text-center text-[11px] text-white">
                {{ item.errorMessage }}
              </div>
              <button class="absolute right-1 top-1 flex h-6 w-6 items-center justify-center rounded-full bg-black/60 text-white transition hover:bg-red-500" @click="galleryUploader.removeItem(item.id)">
                <BaseIcon name="X" :size="14" />
              </button>
            </div>

            <label
              v-if="galleryUploader.canAddMore.value"
              class="flex aspect-square cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed transition-colors hover:border-primary-500 hover:bg-primary-500/5"
              style="border-color: var(--border-default); color: var(--text-tertiary);"
            >
              <BaseIcon name="Plus" :size="20" />
              <span class="text-xs">{{ 9 - galleryItems.length }}/9</span>
              <input type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="handleGalleryChange" />
            </label>
          </div>

          <label
            v-else
            class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
            style="border-color: var(--border-default);"
          >
            <div class="flex h-10 w-10 items-center justify-center rounded-full" style="background-color: var(--bg-tag);">
              <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
            </div>
            <p class="text-sm font-medium" style="color: var(--text-secondary);">上传路线照片</p>
            <input type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="handleGalleryChange" />
          </label>
        </div>
      </section>

      <section class="card animate-fade-in-up space-y-3 p-4 sm:p-5">
        <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
          <BaseIcon name="Tag" :size="16" class="text-primary-500" />
          路线标签
        </h3>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in presetTags"
            :key="tag"
            type="button"
            class="rounded-full border px-3 py-1.5 text-xs font-medium transition-all duration-200"
            :class="selectedTags.includes(tag) ? 'border-primary-500 bg-primary-500 text-white' : 'hover:border-primary-400'"
            :style="!selectedTags.includes(tag) ? 'color: var(--text-secondary); border-color: var(--border-default); background-color: var(--bg-tag);' : ''"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </button>
        </div>
        <div class="flex gap-2">
          <input v-model="customTag" type="text" placeholder="自定义标签..." class="flex-1 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" @keyup.enter="addCustomTag" />
          <button class="rounded-lg border border-primary-500/30 px-3 py-2 text-sm font-medium text-primary-500 transition-colors hover:bg-primary-500/10 disabled:cursor-not-allowed disabled:opacity-40" :disabled="!customTag.trim()" @click="addCustomTag">
            添加
          </button>
        </div>
      </section>

      <div class="animate-fade-in-up pb-8">
        <button
          class="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-medium text-white transition-all disabled:cursor-not-allowed disabled:opacity-40"
          :class="isFormValid && !isSubmitting ? 'bg-primary-500 hover:bg-primary-600 hover:shadow-md active:scale-[0.98]' : 'bg-gray-400'"
          :disabled="!isFormValid || isSubmitting"
          @click="handleSubmit"
        >
          <BaseIcon v-if="isSubmitting" name="Loader2" :size="16" class="animate-spin" />
          <BaseIcon v-else name="Send" :size="16" />
          {{ isSubmitting ? '发布中...' : '发布路线' }}
        </button>
      </div>
    </div>

    <transition name="toast">
      <div v-if="showSuccess" class="fixed left-1/2 top-20 z-50 flex -translate-x-1/2 items-center gap-2 rounded-xl bg-primary-500 px-5 py-3 text-sm font-medium text-white shadow-lg">
        <BaseIcon name="CheckCircle" :size="18" />
        发布成功！即将跳转...
      </div>
    </transition>

    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />
  </main>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -12px);
}
</style>
