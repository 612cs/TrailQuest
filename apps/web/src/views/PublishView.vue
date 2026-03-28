<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, shallowRef, useTemplateRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import toGeoJSON from '@mapbox/togeojson'

import BaseIcon from '../components/common/BaseIcon.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import TrailTrackViewer from '../components/trail/TrailTrackViewer.vue'
import { reverseGeo } from '../api/geo'
import { fetchTrailDetail } from '../api/trails'
import { useAmapLoader } from '../composables/useAmapLoader'
import { useTrailGeo } from '../composables/useTrailGeo'
import { useTrailWeather } from '../composables/useTrailWeather'
import { presetTags } from '../mock/mockData'
import { useFlashStore } from '../stores/useFlashStore'
import { usePublishUploadStore } from '../stores/usePublishUploadStore'
import type { EntityId } from '../types/id'
import type { PublishAssetStatus, PublishDraftState, PublishImageAsset, PublishTaskStage, PublishTrackAsset } from '../types/publishUpload'
import { createTrackViewerData } from '../utils/trailTrackViewerAdapter'
import { mapWeatherToTrackScene } from '../utils/trackWeatherScene'

type TrackCoordinate = [number, number, number?]

type DifficultyValue = 'easy' | 'moderate' | 'hard'
type PackValue = 'light' | 'heavy' | 'both'
type DurationValue = 'single_day' | 'multi_day'

const route = useRoute()
const router = useRouter()
const flashStore = useFlashStore()
const publishUploadStore = usePublishUploadStore()

const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)
const showTrackFullscreen = ref(false)
const isLoadingDraft = ref(false)
const hasAttemptedSubmit = ref(false)

const mapRef = useTemplateRef<HTMLDivElement>('mapContainer')
const mapInstance = shallowRef<any>(null)
const mapLoading = ref(false)
const locationResolveTimer = shallowRef<number | null>(null)
const locationAbortController = shallowRef<AbortController | null>(null)
const reverseGeoRequestId = shallowRef(0)
const trackLocationHint = ref('')
const { resolve: resolvePublishGeo } = useTrailGeo()
const { weather: publishWeather, resolve: resolvePublishWeather } = useTrailWeather()

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

const editTrailId = computed<EntityId | null>(() => {
  const rawEdit = route.query.edit
  if (Array.isArray(rawEdit)) {
    return rawEdit[0] ?? null
  }
  return rawEdit ?? null
})
const isEditMode = computed(() => !!editTrailId.value)
const currentScopeKey = computed(() => (isEditMode.value ? `edit:${String(editTrailId.value)}` : 'create'))

const currentDraft = shallowRef<PublishDraftState | null>(null)

const coverItems = computed(() => currentDraft.value?.coverItems ?? [])
const galleryItems = computed(() => currentDraft.value?.galleryItems ?? [])
const trackItem = computed(() => currentDraft.value?.trackItem ?? null)
const taskState = computed(() => currentDraft.value?.task ?? null)
const isSubmissionRunning = computed(() => (
  taskState.value ? publishUploadStore.isTaskRunning(taskState.value.stage) : false
))
const canRetrySubmission = computed(() => taskState.value?.stage === 'error')
const canAddGalleryMore = computed(() => galleryItems.value.length < 9)
const taskStatusMessage = computed(() => describeTaskStage(taskState.value?.stage ?? 'idle'))
const submitButtonLabel = computed(() => {
  if (isSubmissionRunning.value) {
    return taskStatusMessage.value
  }
  if (canRetrySubmission.value) {
    return '继续上传失败项'
  }
  return isEditMode.value ? '保存修改并后台上传' : '发布路线并后台上传'
})
const publishTrackViewerData = computed(() => createTrackViewerData({
  title: currentDraft.value?.fields.name.trim() || currentDraft.value?.fields.location.trim() || '未命名路线',
  fileName: trackItem.value?.fileName ?? null,
  distanceMeters: parseDistanceToMeters(currentDraft.value?.fields.distance ?? ''),
  elevationGainMeters: parseElevationGainToMeters(currentDraft.value?.fields.elevation ?? ''),
  geoJson: currentDraft.value?.geoJsonData,
}))
const publishWeatherScene = computed(() => mapWeatherToTrackScene(
  publishWeather.value?.weather,
  publishWeather.value?.windPower,
))
const missingRequiredFields = computed(() => {
  const draft = currentDraft.value
  if (!draft) {
    return []
  }

  const missing: string[] = []
  if (!draft.coverItems[0] || draft.coverItems.some((item) => item.status === 'missing')) {
    missing.push('封面图片')
  }
  if (!draft.fields.name.trim()) {
    missing.push('路线名称')
  }
  if (!draft.fields.location.trim()) {
    missing.push('所在位置')
  }
  return missing
})
const canSubmitDraft = computed(() => !isLoadingDraft.value && !isSubmissionRunning.value)
const selectedCustomTags = computed(() => {
  const draft = currentDraft.value
  if (!draft) {
    return []
  }

  const presetTagSet = new Set(presetTags)
  return draft.fields.selectedTags.filter((tag) => !presetTagSet.has(tag))
})

watch(
  currentScopeKey,
  async () => {
    await prepareDraft()
  },
  { immediate: true },
)

watch(
  () => currentDraft.value?.fields.location,
  (nextLocation) => {
    if (locationResolveTimer.value != null) {
      window.clearTimeout(locationResolveTimer.value)
      locationResolveTimer.value = null
    }
    locationAbortController.value?.abort()
    locationAbortController.value = null

    if (!nextLocation?.trim()) {
      return
    }

    locationResolveTimer.value = window.setTimeout(() => {
      const controller = new AbortController()
      locationAbortController.value = controller

      void (async () => {
        const geo = await resolvePublishGeo({
          locationLabel: nextLocation.trim(),
          signal: controller.signal,
        })

        if (!geo?.adcode) {
          return
        }

        await resolvePublishWeather({
          adcode: geo.adcode,
          signal: controller.signal,
        })
      })()
    }, 420)
  },
)

onBeforeUnmount(() => {
  if (locationResolveTimer.value != null) {
    window.clearTimeout(locationResolveTimer.value)
  }
  locationAbortController.value?.abort()
})

async function prepareDraft() {
  const scopeKey = currentScopeKey.value
  const mode = isEditMode.value ? 'edit' : 'create'
  const trailId = editTrailId.value
  currentDraft.value = publishUploadStore.ensureDraft(scopeKey, mode, trailId)

  if (currentDraft.value?.geoJsonData) {
    await renderMapWithGeoJSON()
  } else {
    clearMapRender()
  }

  if (!isEditMode.value || !trailId || currentDraft.value.hydratedFromServer) {
    return
  }

  isLoadingDraft.value = true
  try {
    const detail = await fetchTrailDetail(trailId)
    if (!detail.ownedByCurrentUser) {
      throw new Error('只能编辑自己发布的路线')
    }
    if (!detail.editableByCurrentUser) {
      throw new Error('路线发布超过48小时，不能再编辑')
    }

    currentDraft.value = publishUploadStore.hydrateDraftFromTrail(scopeKey, detail)
    await renderMapWithGeoJSON()
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '路线草稿加载失败')
    void router.replace(editTrailId.value ? `/trail/${editTrailId.value}` : '/profile')
  } finally {
    isLoadingDraft.value = false
  }
}

function hasFieldError(field: 'cover' | 'name' | 'location') {
  if (!hasAttemptedSubmit.value) {
    return false
  }

  switch (field) {
    case 'cover':
      return missingRequiredFields.value.includes('封面图片')
    case 'name':
      return missingRequiredFields.value.includes('路线名称')
    case 'location':
      return missingRequiredFields.value.includes('所在位置')
    default:
      return false
  }
}

function updateDifficulty(value: DifficultyValue) {
  if (currentDraft.value && !isSubmissionRunning.value) {
    currentDraft.value.fields.difficulty = value
  }
}

function updatePackType(value: PackValue) {
  if (currentDraft.value && !isSubmissionRunning.value) {
    currentDraft.value.fields.packType = value
  }
}

function updateDurationType(value: DurationValue) {
  if (currentDraft.value && !isSubmissionRunning.value) {
    currentDraft.value.fields.durationType = value
  }
}

function toggleTag(tag: string) {
  const draft = currentDraft.value
  if (!draft || isSubmissionRunning.value) {
    return
  }

  const idx = draft.fields.selectedTags.indexOf(tag)
  if (idx >= 0) {
    draft.fields.selectedTags.splice(idx, 1)
  } else {
    draft.fields.selectedTags.push(tag)
  }
}

function addCustomTag() {
  const draft = currentDraft.value
  if (!draft || isSubmissionRunning.value) {
    return
  }

  const tag = draft.fields.customTag.trim()
  if (tag && !draft.fields.selectedTags.includes(tag)) {
    draft.fields.selectedTags.push(tag)
  }
  draft.fields.customTag = ''
}

function openPreview(images: string[], index: number) {
  previewImages.value = images
  previewIndex.value = index
  showPreview.value = true
}

function getImageSources(items: { localUrl: string; remoteUrl: string }[]) {
  return items.map((item) => item.remoteUrl || item.localUrl)
}

async function renderMapWithGeoJSON() {
  if (!currentDraft.value?.geoJsonData) {
    clearMapRender()
    return
  }

  await nextTick()
  if (!mapRef.value) {
    return
  }

  const AMap = await useAmapLoader().load()
  if (!AMap) {
    publishUploadStore.setTrackPreview(currentScopeKey.value, currentDraft.value.geoJsonData, '获取高德地图服务失败')
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
      geoJSON: currentDraft.value?.geoJsonData,
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
      getMarker(geojson: { properties?: { name?: string } }, lnglats: unknown) {
        return new AMap.Marker({
          position: lnglats,
          title: geojson.properties?.name || '轨迹点',
        })
      },
    })

    mapInstance.value?.add(geojsonObj)
    mapInstance.value?.setFitView()
  })
}

function clearMapRender() {
  mapInstance.value?.clearMap?.()
}

function buildImageAsset(file: File, bizType: 'trail_cover' | 'trail_gallery'): PublishImageAsset {
  return {
    id: buildLocalId(),
    source: 'local',
    bizType,
    file,
    fileName: file.name,
    mimeType: file.type,
    localUrl: URL.createObjectURL(file),
    remoteUrl: '',
    mediaId: null,
    progress: 0,
    status: 'pending',
    errorMessage: '',
  }
}

function buildTrackAsset(file: File): PublishTrackAsset {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  return {
    id: buildLocalId(),
    source: 'local',
    file,
    fileName: file.name,
    localUrl: URL.createObjectURL(file),
    remoteUrl: '',
    mediaId: null,
    mimeType: file.type || (extension === 'kml'
      ? 'application/vnd.google-earth.kml+xml'
      : 'application/gpx+xml'),
    extension,
    progress: 0,
    status: 'pending',
    errorMessage: '',
  }
}

function buildLocalId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function revokeLocalUrl(url: string) {
  if (url.startsWith('blob:')) {
    URL.revokeObjectURL(url)
  }
}

async function handleCoverChange(event: Event) {
  const draft = currentDraft.value
  const files = (event.target as HTMLInputElement).files
  if (!draft || !files?.length || isSubmissionRunning.value) return

  draft.coverItems.forEach((item) => {
    if (item.source === 'local') {
      revokeLocalUrl(item.localUrl)
    }
  })
  draft.coverItems = [buildImageAsset(files[0]!, 'trail_cover')]
  ;(event.target as HTMLInputElement).value = ''
}

async function handleGalleryChange(event: Event) {
  const draft = currentDraft.value
  const files = (event.target as HTMLInputElement).files
  if (!draft || !files?.length || isSubmissionRunning.value) return

  const available = Math.max(9 - draft.galleryItems.length, 0)
  const nextFiles = Array.from(files).slice(0, available)
  draft.galleryItems = [...draft.galleryItems, ...nextFiles.map((file) => buildImageAsset(file, 'trail_gallery'))]
  ;(event.target as HTMLInputElement).value = ''
}

async function handleTrackChange(event: Event) {
  const draft = currentDraft.value
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!draft || !file || isSubmissionRunning.value) return

  await parseTrackPreview(file)
  if (draft.trackItem?.source === 'local') {
    revokeLocalUrl(draft.trackItem.localUrl)
  }
  draft.trackItem = buildTrackAsset(file)
  ;(event.target as HTMLInputElement).value = ''
}

async function parseTrackPreview(file: File) {
  mapLoading.value = true
  trackLocationHint.value = ''

  try {
    const text = await file.text()
    const doc = new DOMParser().parseFromString(text, 'text/xml')
    const extension = file.name.split('.').pop()?.toLowerCase()
    const geoJson = extension === 'kml' ? toGeoJSON.kml(doc) : toGeoJSON.gpx(doc)
    publishUploadStore.setTrackPreview(currentScopeKey.value, geoJson, '')
    const coordinates = extractCoordinates(geoJson)
    applyTrackSummary(coordinates)
    await autofillTrackLocation(coordinates)
    await renderMapWithGeoJSON()
  } catch (error) {
    console.error(error)
    trackLocationHint.value = ''
    publishUploadStore.setTrackPreview(currentScopeKey.value, null, '解析轨迹文件失败，请检查 GPX/KML 格式。')
    clearMapRender()
  } finally {
    mapLoading.value = false
  }
}

function applyTrackSummary(coordinates: TrackCoordinate[]) {
  const draft = currentDraft.value
  if (!draft) {
    return
  }

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

  if (!draft.fields.distance.trim()) {
    draft.fields.distance = `${(totalDistance / 1000).toFixed(1).replace(/\.0$/, '')} km`
  }
  if (!draft.fields.elevation.trim() && gain > 0) {
    draft.fields.elevation = `+${Math.round(gain)} m`
  }
}

async function autofillTrackLocation(coordinates: TrackCoordinate[]) {
  const draft = currentDraft.value
  const start = coordinates[0]
  if (!draft || !start) {
    return
  }

  const [lng, lat] = start
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) {
    return
  }

  const requestId = reverseGeoRequestId.value + 1
  reverseGeoRequestId.value = requestId

  try {
    const result = await reverseGeo({ lng, lat })
    if (reverseGeoRequestId.value !== requestId) {
      return
    }

    if (result.formattedLocation.trim()) {
      draft.fields.location = result.formattedLocation.trim()
      trackLocationHint.value = '已根据轨迹起点自动识别所在市县'
    }
  } catch (error) {
    if (reverseGeoRequestId.value !== requestId) {
      return
    }
    trackLocationHint.value = error instanceof Error
      ? `轨迹位置识别失败，请手动填写地点：${error.message}`
      : '轨迹位置识别失败，请手动填写地点'
  }
}

function extractCoordinates(geoJson: { features?: unknown[] }): TrackCoordinate[] {
  const coordinates: TrackCoordinate[] = []
  const features = Array.isArray(geoJson?.features) ? geoJson.features : []

  for (const feature of features as Array<{ geometry?: { type?: string; coordinates?: unknown } }>) {
    const geometry = feature?.geometry
    if (!geometry) continue

    if (geometry.type === 'LineString' && Array.isArray(geometry.coordinates)) {
      geometry.coordinates.forEach((coordinate) => {
        if (isTrackCoordinate(coordinate)) {
          coordinates.push(coordinate)
        }
      })
    }

    if (geometry.type === 'MultiLineString' && Array.isArray(geometry.coordinates)) {
      geometry.coordinates.forEach((line) => {
        if (!Array.isArray(line)) return
        line.forEach((coordinate) => {
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

function removeCover(itemId: string) {
  if (isSubmissionRunning.value || !currentDraft.value) {
    return
  }
  const nextItems = currentDraft.value.coverItems.filter((item) => {
    if (item.id === itemId && item.source === 'local') {
      revokeLocalUrl(item.localUrl)
    }
    return item.id !== itemId
  })
  currentDraft.value.coverItems = nextItems
}

function removeGalleryItem(itemId: string) {
  if (isSubmissionRunning.value || !currentDraft.value) {
    return
  }
  const nextItems = currentDraft.value.galleryItems.filter((item) => {
    if (item.id === itemId && item.source === 'local') {
      revokeLocalUrl(item.localUrl)
    }
    return item.id !== itemId
  })
  currentDraft.value.galleryItems = nextItems
}

function clearTrack() {
  const draft = currentDraft.value
  if (!draft || isSubmissionRunning.value) {
    return
  }
  if (draft.trackItem?.source === 'local') {
    revokeLocalUrl(draft.trackItem.localUrl)
  }
  draft.trackItem = null
  trackLocationHint.value = ''
  publishUploadStore.setTrackPreview(currentScopeKey.value, null, '')
  clearMapRender()
  showTrackFullscreen.value = false
}

function parseDistanceToMeters(value: string) {
  const normalized = value.trim().toLowerCase()
  if (!normalized) return null
  const numberValue = Number(normalized.replace(/[^0-9.]/g, ''))
  if (!Number.isFinite(numberValue)) return null
  if (normalized.includes('km')) {
    return numberValue * 1000
  }
  if (normalized.includes('m')) {
    return numberValue
  }
  return null
}

function parseElevationGainToMeters(value: string) {
  const normalized = value.trim().toLowerCase()
  if (!normalized) return null
  const numberValue = Number(normalized.replace(/[^0-9.]/g, ''))
  if (!Number.isFinite(numberValue)) return null
  return numberValue
}

function describeTaskStage(stage: PublishTaskStage) {
  switch (stage) {
    case 'queued':
      return '已进入后台上传队列'
    case 'uploading-cover':
      return '后台上传封面中...'
    case 'uploading-gallery':
      return '后台上传相册中...'
    case 'uploading-track':
      return '后台上传轨迹中...'
    case 'creating-trail':
      return '后台创建路线中...'
    case 'updating-trail':
      return '后台保存修改中...'
    case 'success':
      return isEditMode.value ? '路线已更新完成' : '路线已发布完成'
    case 'error':
      return '后台上传失败，可返回后重试'
    case 'idle':
    default:
      return '点击发布后将转为后台继续上传'
  }
}

function assetStatusText(status: PublishAssetStatus, item?: { progress: number; errorMessage: string } | null) {
  switch (status) {
    case 'existing':
      return '已保留原有文件'
    case 'pending':
      return '等待后台上传'
    case 'uploading':
      return `后台上传中 ${item?.progress ?? 0}%`
    case 'uploaded':
      return '上传完成，等待提交路线'
    case 'error':
      return item?.errorMessage || '上传失败，可稍后重试'
    case 'missing':
      return item?.errorMessage || '本地文件已失效，请重新选择'
    default:
      return ''
  }
}

async function handleSubmit() {
  if (!canSubmitDraft.value) {
    return
  }

  hasAttemptedSubmit.value = true

  if (!canRetrySubmission.value && missingRequiredFields.value.length > 0) {
    flashStore.showError(`请先补充：${missingRequiredFields.value.join('、')}`, 3200)
    return
  }

  try {
    if (canRetrySubmission.value) {
      await publishUploadStore.retryDraft(currentScopeKey.value)
      return
    }

    await publishUploadStore.submitDraft(currentScopeKey.value)
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '加入后台上传队列失败')
  }
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
        <h2 class="text-sm font-semibold" style="color: var(--text-primary);">{{ isEditMode ? '编辑路线' : '发布路线' }}</h2>
        <div class="w-12" />
      </div>
    </div>

    <div class="mx-auto max-w-3xl space-y-5 px-4 py-6 sm:px-6">
      <section v-if="isLoadingDraft" class="card flex items-center gap-3 p-4 sm:p-5">
        <BaseIcon name="Loader2" :size="18" class="animate-spin text-primary-500" />
        <p class="text-sm" style="color: var(--text-secondary);">正在加载路线信息...</p>
      </section>

      <template v-if="currentDraft">
        <section class="card animate-fade-in-up p-4 sm:p-5">
          <h3 class="mb-3 flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
            <BaseIcon name="Image" :size="16" class="text-primary-500" />
            封面图片
            <span class="text-[11px] font-medium text-red-500">必填</span>
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
                <div v-if="item.status === 'uploading'" class="absolute inset-0 flex items-center justify-center bg-black/40 text-sm font-medium text-white">
                  {{ item.progress }}%
                </div>
                <div v-else-if="item.status === 'error' || item.status === 'missing'" class="absolute inset-0 flex items-center justify-center bg-black/55 px-3 text-center text-xs text-white">
                  {{ item.errorMessage }}
                </div>
                <div v-else class="absolute inset-x-2 bottom-2 rounded-full bg-black/55 px-2 py-1 text-center text-[11px] text-white/90">
                  {{ assetStatusText(item.status, item) }}
                </div>
                <button
                  type="button"
                  class="absolute right-2 top-2 flex h-7 w-7 items-center justify-center rounded-full bg-black/60 text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="isSubmissionRunning"
                  @click="removeCover(item.id)"
                >
                  <BaseIcon name="X" :size="14" />
                </button>
              </div>
            </div>

            <label
              v-if="!coverItems.length"
              class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
              :style="hasFieldError('cover') ? 'border-color: var(--color-hard); background-color: color-mix(in srgb, var(--color-hard) 8%, transparent);' : 'border-color: var(--border-default);'"
            >
              <div class="flex h-10 w-10 items-center justify-center rounded-full" style="background-color: var(--bg-tag);">
                <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
              </div>
              <p class="text-sm font-medium" style="color: var(--text-secondary);">上传路线封面</p>
              <input type="file" accept="image/jpeg,image/png,image/webp" class="hidden" :disabled="isSubmissionRunning" @change="handleCoverChange" />
            </label>
            <p v-if="hasFieldError('cover')" class="text-xs text-red-500">请先上传封面图片</p>
          </div>
        </section>

        <section class="card animate-fade-in-up space-y-4 p-4 sm:p-5">
          <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
            <BaseIcon name="FileText" :size="16" class="text-primary-500" />
            基本信息
          </h3>

          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">路线名称 <span class="text-red-500">必填</span></label>
            <input v-model="currentDraft.fields.name" type="text" placeholder="例如：龙脊梯田精华线" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" :style="hasFieldError('name') ? 'background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--color-hard);' : 'background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);'" :disabled="isSubmissionRunning" />
            <p v-if="hasFieldError('name')" class="mt-1.5 text-xs text-red-500">请填写路线名称</p>
          </div>

          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">所在位置 <span class="text-red-500">必填</span></label>
            <input v-model="currentDraft.fields.location" type="text" placeholder="例如：广西 桂林 龙胜" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" :style="hasFieldError('location') ? 'background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--color-hard);' : 'background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);'" :disabled="isSubmissionRunning" />
            <p
              v-if="trackLocationHint"
              class="mt-1.5 text-xs"
              :style="trackLocationHint.includes('失败') ? 'color: var(--color-hard);' : 'color: var(--text-tertiary);'"
            >
              {{ trackLocationHint }}
            </p>
            <p v-if="hasFieldError('location')" class="mt-1.5 text-xs text-red-500">请填写所在位置</p>
          </div>

          <div>
            <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">难度等级</label>
            <div class="flex gap-2">
              <button
                v-for="opt in difficultyOptions"
                :key="opt.value"
                type="button"
                class="flex-1 rounded-lg border-2 py-2 text-sm font-medium transition-all duration-200"
                :disabled="isSubmissionRunning"
                :class="currentDraft.fields.difficulty === opt.value ? 'scale-[1.02]' : 'opacity-70 hover:opacity-90'"
                :style="{ borderColor: currentDraft.fields.difficulty === opt.value ? opt.color : 'var(--border-default)', backgroundColor: currentDraft.fields.difficulty === opt.value ? `${opt.color}15` : 'transparent', color: currentDraft.fields.difficulty === opt.value ? opt.color : 'var(--text-secondary)' }"
                @click="updateDifficulty(opt.value)"
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
                  :disabled="isSubmissionRunning"
                  :class="currentDraft.fields.packType === opt.value ? 'border-primary-500 bg-primary-500/10 text-primary-500' : ''"
                  :style="currentDraft.fields.packType !== opt.value ? 'border-color: var(--border-default); color: var(--text-secondary);' : ''"
                  @click="updatePackType(opt.value)"
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
                  :disabled="isSubmissionRunning"
                  :class="currentDraft.fields.durationType === opt.value ? 'border-primary-500 bg-primary-500/10 text-primary-500' : ''"
                  :style="currentDraft.fields.durationType !== opt.value ? 'border-color: var(--border-default); color: var(--text-secondary);' : ''"
                  @click="updateDurationType(opt.value)"
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
            <label class="cursor-pointer rounded-lg bg-primary-500/10 px-3 py-1.5 text-xs font-medium text-primary-500 transition-colors hover:bg-primary-500/20" :class="isSubmissionRunning ? 'pointer-events-none opacity-40' : ''">
              {{ trackItem ? '重新上传' : '上传轨迹' }}
              <input type="file" accept=".gpx,.kml,application/gpx+xml,application/vnd.google-earth.kml+xml,text/xml,application/xml" class="hidden" :disabled="isSubmissionRunning" @change="handleTrackChange" />
            </label>
          </div>

          <p class="text-xs" style="color: var(--text-secondary);">
            轨迹文件为可选项。点击发布后才会真正上传，上传前会继续保留本地预览和解析结果。
          </p>

          <div v-if="trackItem" class="rounded-xl border p-3 text-sm" style="border-color: var(--border-default); background-color: var(--bg-tag);">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <p class="truncate font-medium" style="color: var(--text-primary);">{{ trackItem.fileName }}</p>
                <p class="mt-1 text-xs" :class="trackItem.status === 'error' || trackItem.status === 'missing' ? 'text-red-500' : ''" style="color: var(--text-secondary);">
                  {{ assetStatusText(trackItem.status, trackItem) }}
                </p>
              </div>
              <button type="button" class="flex h-8 w-8 items-center justify-center rounded-full transition-colors hover:bg-primary-500/10 disabled:cursor-not-allowed disabled:opacity-40" :disabled="isSubmissionRunning" @click="clearTrack">
                <BaseIcon name="X" :size="16" style="color: var(--text-secondary);" />
              </button>
            </div>
          </div>

          <div class="relative overflow-hidden rounded-xl shadow-inner" style="height: 240px; background-color: var(--bg-input); border: 1px solid var(--border-default);">
            <div ref="mapContainer" class="h-full w-full" :style="{ display: currentDraft.geoJsonData ? 'block' : 'none' }"></div>
            <div v-if="mapLoading" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5 backdrop-blur-sm">
              <BaseIcon name="Loader2" :size="20" class="animate-spin text-primary-500" />
              <span class="text-xs font-medium" style="color: var(--text-secondary);">解析轨迹中...</span>
            </div>
            <div v-else-if="currentDraft.trackPreviewError" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5">
              <BaseIcon name="AlertCircle" :size="20" class="text-red-500" />
              <span class="px-4 text-center text-xs font-medium text-red-500">{{ currentDraft.trackPreviewError }}</span>
            </div>
            <div v-else-if="!currentDraft.geoJsonData" class="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-black/5">
              <BaseIcon name="Route" :size="24" style="color: var(--text-tertiary);" />
              <span class="text-xs font-medium" style="color: var(--text-tertiary);">请上传 GPX / KML 轨迹文件以在地图中预览</span>
            </div>
          </div>

          <TrailTrackViewer
            v-if="publishTrackViewerData"
            :data="publishTrackViewerData"
            :weather-scene="publishWeatherScene"
            mode="embedded"
            :show-fullscreen-button="true"
            @request-fullscreen="showTrackFullscreen = true"
          />
        </section>

        <section class="card animate-fade-in-up space-y-4 p-4 sm:p-5">
          <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
            <BaseIcon name="Ruler" :size="16" class="text-primary-500" />
            路线参数
          </h3>
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">距离</label>
              <input v-model="currentDraft.fields.distance" type="text" placeholder="12.5 km" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" :disabled="isSubmissionRunning" />
            </div>
            <div>
              <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">海拔</label>
              <input v-model="currentDraft.fields.elevation" type="text" placeholder="+450 m" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" :disabled="isSubmissionRunning" />
            </div>
            <div>
              <label class="mb-1.5 block text-xs font-medium" style="color: var(--text-secondary);">时长</label>
              <input v-model="currentDraft.fields.duration" type="text" placeholder="4h 30m" class="w-full rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" :disabled="isSubmissionRunning" />
            </div>
          </div>
        </section>

        <section class="card animate-fade-in-up space-y-3 p-4 sm:p-5">
          <h3 class="flex items-center gap-2 text-sm font-semibold" style="color: var(--text-primary);">
            <BaseIcon name="AlignLeft" :size="16" class="text-primary-500" />
            路线描述
          </h3>
          <textarea v-model="currentDraft.fields.description" rows="5" placeholder="描述这条路线的特色、注意事项、推荐理由..." class="w-full resize-none rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" :disabled="isSubmissionRunning" />
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
                <div v-else-if="item.status === 'error' || item.status === 'missing'" class="absolute inset-0 flex items-center justify-center bg-black/55 px-2 text-center text-[11px] text-white">
                  {{ item.errorMessage }}
                </div>
                <div v-else class="absolute inset-x-1 bottom-1 rounded-full bg-black/55 px-2 py-1 text-center text-[10px] text-white/90">
                  {{ assetStatusText(item.status, item) }}
                </div>
                <button type="button" class="absolute right-1 top-1 flex h-6 w-6 items-center justify-center rounded-full bg-black/60 text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-40" :disabled="isSubmissionRunning" @click="removeGalleryItem(item.id)">
                  <BaseIcon name="X" :size="14" />
                </button>
              </div>

              <label
                v-if="canAddGalleryMore"
                class="flex aspect-square cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed transition-colors hover:border-primary-500 hover:bg-primary-500/5"
                :class="isSubmissionRunning ? 'pointer-events-none opacity-40' : ''"
                style="border-color: var(--border-default); color: var(--text-tertiary);"
              >
                <BaseIcon name="Plus" :size="20" />
                <span class="text-xs">{{ 9 - galleryItems.length }}/9</span>
                <input type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" :disabled="isSubmissionRunning" @change="handleGalleryChange" />
              </label>
            </div>

            <label
              v-else
              class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
              :class="isSubmissionRunning ? 'pointer-events-none opacity-40' : ''"
              style="border-color: var(--border-default);"
            >
              <div class="flex h-10 w-10 items-center justify-center rounded-full" style="background-color: var(--bg-tag);">
                <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
              </div>
              <p class="text-sm font-medium" style="color: var(--text-secondary);">上传路线照片</p>
              <input type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" :disabled="isSubmissionRunning" @change="handleGalleryChange" />
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
              :disabled="isSubmissionRunning"
              :class="currentDraft.fields.selectedTags.includes(tag) ? 'border-primary-500 bg-primary-500 text-white' : 'hover:border-primary-400'"
              :style="!currentDraft.fields.selectedTags.includes(tag) ? 'color: var(--text-secondary); border-color: var(--border-default); background-color: var(--bg-tag);' : ''"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </button>
          </div>
          <div v-if="selectedCustomTags.length" class="flex flex-wrap gap-2">
            <button
              v-for="tag in selectedCustomTags"
              :key="`custom-${tag}`"
              type="button"
              class="inline-flex items-center gap-1 rounded-full border border-primary-500/30 bg-primary-500/10 px-3 py-1.5 text-xs font-medium text-primary-600 transition-colors hover:bg-primary-500/15 disabled:cursor-not-allowed disabled:opacity-40"
              :disabled="isSubmissionRunning"
              @click="toggleTag(tag)"
            >
              <span>{{ tag }}</span>
              <BaseIcon name="X" :size="12" />
            </button>
          </div>
          <div class="flex gap-2">
            <input v-model="currentDraft.fields.customTag" type="text" placeholder="自定义标签..." class="flex-1 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30" style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);" :disabled="isSubmissionRunning" @keyup.enter="addCustomTag" />
            <button class="rounded-lg border border-primary-500/30 px-3 py-2 text-sm font-medium text-primary-500 transition-colors hover:bg-primary-500/10 disabled:cursor-not-allowed disabled:opacity-40" :disabled="!currentDraft.fields.customTag.trim() || isSubmissionRunning" @click="addCustomTag">
              添加
            </button>
          </div>
        </section>

        <div class="animate-fade-in-up pb-8">
          <button
            class="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-medium text-white transition-all disabled:cursor-not-allowed disabled:opacity-40"
            :class="canSubmitDraft ? 'bg-primary-500 hover:bg-primary-600 hover:shadow-md active:scale-[0.98]' : 'bg-gray-400'"
            :disabled="!canSubmitDraft"
            @click="handleSubmit"
          >
            <BaseIcon v-if="isSubmissionRunning" name="Loader2" :size="16" class="animate-spin" />
            <BaseIcon v-else name="Send" :size="16" />
            {{ submitButtonLabel }}
          </button>
          <p class="mt-3 text-center text-xs" style="color: var(--text-secondary);">
            目前仅封面图片、路线名称、所在位置为必填。其余信息都可选，点击按钮后会提示你缺少的内容。
          </p>
        </div>
      </template>
    </div>

    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showTrackFullscreen && publishTrackViewerData" class="fixed inset-0 z-[70] bg-black/55 backdrop-blur-sm">
          <div class="flex h-full w-full flex-col p-3 sm:p-5">
            <div class="mb-3 flex items-center justify-between">
              <p class="text-sm font-semibold text-white/90">轨迹全屏浏览</p>
              <button
                type="button"
                class="flex h-10 w-10 items-center justify-center rounded-full bg-white/15 text-white transition hover:bg-white/25"
                @click="showTrackFullscreen = false"
              >
                <BaseIcon name="X" :size="18" />
              </button>
            </div>
            <TrailTrackViewer
              :data="publishTrackViewerData"
              :weather-scene="publishWeatherScene"
              mode="fullscreen"
              @exit-fullscreen="showTrackFullscreen = false"
            />
          </div>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>
