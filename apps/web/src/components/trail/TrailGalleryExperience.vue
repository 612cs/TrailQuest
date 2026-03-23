<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'

import { useTrailGalleryScene } from '../../composables/useTrailGalleryScene'
import type { TrailTrackDetail } from '../../types/trail'
import type { TrackViewerPoint } from '../../types/trackViewer'
import { createTrackViewerData } from '../../utils/trailTrackViewerAdapter'
import BaseIcon from '../common/BaseIcon.vue'
import TrailPhotoNodeCard from './TrailPhotoNodeCard.vue'

interface NormalizedPoint {
  x: number
  y: number
}

interface PhotoNodeLayout {
  image: string
  index: number
  x: number
  y: number
  lane: -1 | 1
  progress: number
  indexLabel: string
  segmentLabel: string
  progressLabel: string
  cameraLabel: string
  caption: string
  timeLabel: string
  size: 'hero' | 'detail' | 'glimpse'
  emphasis: number
  isActive: boolean
  style: {
    left: string
    top: string
    width: string
    transform: string
    zIndex: string
  }
}

const props = withDefaults(defineProps<{
  images: string[]
  title: string
  location?: string
  description?: string
  distance?: string
  elevation?: string
  duration?: string
  difficultyLabel?: string
  track?: TrailTrackDetail | null
}>(), {
  location: '',
  description: '',
  distance: '',
  elevation: '',
  duration: '',
  difficultyLabel: '',
  track: null,
})

const emit = defineEmits<{
  (event: 'active-change', index: number): void
}>()

const viewportRef = useTemplateRef<HTMLDivElement>('viewport')
const sceneRef = useTemplateRef<HTMLDivElement>('scene')
const viewportWidth = shallowRef(0)
const cameraX = shallowRef(0)
const targetCameraX = shallowRef(0)
const velocity = shallowRef(0)
const activeIndex = shallowRef(0)
const isDragging = shallowRef(false)
const pointerId = shallowRef<number | null>(null)
const lastPointerX = shallowRef(0)
const animationFrameRef = shallowRef<number | null>(null)

const segmentPhrases = [
  '林冠起点',
  '风口回望',
  '坡脊穿行',
  '山脊主景',
  '终点余韵',
]

const trackData = computed(() => createTrackViewerData({
  title: props.title,
  geoJson: props.track?.geoJson,
  distanceMeters: props.track?.distanceMeters ?? null,
  elevationGainMeters: props.track?.elevationGainMeters ?? null,
}))
const mainTrackPoints = computed(() => {
  const data = trackData.value
  if (!data) return []
  return data.paths[data.mainPathIndex ?? 0]?.points ?? []
})

const focusProgress = computed(() => {
  if (props.images.length <= 1) return 0
  return activeIndex.value / (props.images.length - 1)
})

useTrailGalleryScene({
  containerRef: sceneRef,
  focusProgress,
})

const stageHeight = computed(() => (viewportWidth.value < 768 ? 560 : 680))
const stagePaddingX = computed(() => (viewportWidth.value < 768 ? 100 : 220))
const stagePaddingTop = computed(() => (viewportWidth.value < 768 ? 94 : 110))
const stagePaddingBottom = computed(() => (viewportWidth.value < 768 ? 96 : 126))
const stageWidth = computed(() => {
  const imageSpacing = viewportWidth.value < 768 ? 220 : 300
  const minimum = viewportWidth.value * 1.08
  const ideal = stagePaddingX.value * 2 + Math.max(props.images.length - 1, 1) * imageSpacing + 260
  return Math.max(minimum, ideal)
})
const maxCameraX = computed(() => Math.max(stageWidth.value - viewportWidth.value, 0))

const routeGeometry = computed(() => {
  const routePoints = buildRoutePoints(mainTrackPoints.value, props.images.length)
  const usableWidth = Math.max(stageWidth.value - stagePaddingX.value * 2, 1)
  const usableHeight = Math.max(stageHeight.value - stagePaddingTop.value - stagePaddingBottom.value, 1)

  return routePoints.map((point) => ({
    x: stagePaddingX.value + point.x * usableWidth,
    y: stagePaddingTop.value + point.y * usableHeight,
  }))
})

const sampledNodeAnchors = computed(() => samplePoints(
  routeGeometry.value,
  Math.max(props.images.length, 1),
))

const routePathD = computed(() => createSvgPath(routeGeometry.value))

const photoNodes = computed<PhotoNodeLayout[]>(() => {
  const focusX = cameraX.value + viewportWidth.value * (viewportWidth.value < 768 ? 0.5 : 0.56)
  const widthHero = viewportWidth.value < 768 ? 248 : 396
  const widthDetail = viewportWidth.value < 768 ? 202 : 282
  const widthGlimpse = viewportWidth.value < 768 ? 160 : 206

  return props.images.map((image, index) => {
    const anchor = sampledNodeAnchors.value[index] ?? {
      x: stagePaddingX.value,
      y: stageHeight.value / 2,
    }
    const progress = props.images.length <= 1 ? 0 : index / (props.images.length - 1)
    const lane = index % 3 === 1 ? 1 : -1
    const distance = viewportWidth.value > 0 ? Math.abs(anchor.x - focusX) / viewportWidth.value : 0
    const clampedDistance = Math.min(distance, 1.25)
    const emphasis = Math.max(0.38, 1 - clampedDistance * 0.58)
    const isActive = index === activeIndex.value
    const size = isActive ? 'hero' : distance < 0.45 ? 'detail' : 'glimpse'
    const cardWidth = size === 'hero' ? widthHero : size === 'detail' ? widthDetail : widthGlimpse
    const rotation = clamp(((anchor.x - focusX) / Math.max(viewportWidth.value, 1)) * -7, -7, 7)
    const shiftY = lane === -1 ? -(46 + clampedDistance * 24) : 46 + clampedDistance * 18
    const scale = size === 'hero' ? 1 : size === 'detail' ? 0.94 : 0.82
    const segmentStep = Math.min(segmentPhrases.length - 1, Math.round(progress * (segmentPhrases.length - 1)))
    const percent = Math.round(progress * 100)

    return {
      image,
      index,
      x: anchor.x,
      y: anchor.y,
      lane,
      progress,
      indexLabel: String(index + 1).padStart(2, '0'),
      segmentLabel: segmentPhrases[segmentStep] ?? '路线片段',
      progressLabel: `${percent}% route`,
      cameraLabel: buildCameraLabel(progress, props.location),
      caption: buildNarrative(progress, props.description),
      timeLabel: buildTimeLabel(progress),
      size,
      emphasis,
      isActive,
      style: {
        left: `${anchor.x}px`,
        top: `${anchor.y}px`,
        width: `${cardWidth}px`,
        transform: `translate3d(-50%, ${shiftY}px, 0) scale(${scale}) rotate(${rotation}deg)`,
        zIndex: String(140 - Math.round(clampedDistance * 70) + (isActive ? 40 : 0)),
      },
    }
  })
})

const activeNode = computed(() => photoNodes.value[activeIndex.value] ?? photoNodes.value[0] ?? null)
const activeLabel = computed(() => activeNode.value ? activeNode.value.indexLabel : '00')
const statusText = computed(() => props.images.length
  ? '拖拽、滚轮或方向键沿路线穿行，每张照片都挂在一个节点上。'
  : '当前路线暂无可展示图片')
const routeStats = computed(() => [
  {
    label: 'Route',
    value: props.distance || formatDistance(trackData.value?.distanceMeters),
  },
  {
    label: 'Climb',
    value: props.elevation || formatElevation(trackData.value?.elevationGainMeters),
  },
  {
    label: 'Window',
    value: props.duration || '全日巡游',
  },
  {
    label: 'Mood',
    value: props.difficultyLabel || '沉浸体验',
  },
].filter((item) => item.value))
const progressFillStyle = computed(() => ({
  width: `${((activeIndex.value + 1) / Math.max(props.images.length, 1)) * 100}%`,
}))
const progressThumbStyle = computed(() => ({
  left: `${focusProgress.value * 100}%`,
}))
const counterLabel = computed(() => `${activeLabel.value} / ${String(props.images.length).padStart(2, '0')}`)

watch(
  () => props.images,
  () => {
    cameraX.value = 0
    targetCameraX.value = 0
    velocity.value = 0
    activeIndex.value = 0
    syncActiveIndex()
  },
  { immediate: true },
)

onMounted(() => {
  measureViewport()
  window.addEventListener('resize', measureViewport)
  window.addEventListener('keydown', handleKeydown)
  startLoop()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureViewport)
  window.removeEventListener('keydown', handleKeydown)
  stopLoop()
})

function measureViewport() {
  viewportWidth.value = viewportRef.value?.clientWidth ?? window.innerWidth
  targetCameraX.value = clamp(targetCameraX.value, 0, maxCameraX.value)
  cameraX.value = clamp(cameraX.value, 0, maxCameraX.value)
  syncActiveIndex()
}

function startLoop() {
  const tick = () => {
    animationFrameRef.value = window.requestAnimationFrame(tick)

    if (!isDragging.value && Math.abs(velocity.value) > 0.02) {
      targetCameraX.value = clamp(targetCameraX.value + velocity.value, 0, maxCameraX.value)
      velocity.value *= 0.9
    }

    cameraX.value += (targetCameraX.value - cameraX.value) * 0.1
    syncActiveIndex()
  }

  tick()
}

function stopLoop() {
  if (animationFrameRef.value != null) {
    window.cancelAnimationFrame(animationFrameRef.value)
    animationFrameRef.value = null
  }
}

function handlePointerDown(event: PointerEvent) {
  if (!props.images.length || !viewportRef.value) return

  isDragging.value = true
  pointerId.value = event.pointerId
  lastPointerX.value = event.clientX
  velocity.value = 0
  viewportRef.value.setPointerCapture(event.pointerId)
}

function handlePointerMove(event: PointerEvent) {
  if (!isDragging.value || pointerId.value !== event.pointerId) return

  const deltaX = event.clientX - lastPointerX.value
  lastPointerX.value = event.clientX
  targetCameraX.value = clamp(targetCameraX.value - deltaX, 0, maxCameraX.value)
  velocity.value = -deltaX * 0.18
}

function handlePointerUp(event: PointerEvent) {
  if (pointerId.value !== event.pointerId) return

  isDragging.value = false
  pointerId.value = null
  viewportRef.value?.releasePointerCapture(event.pointerId)
}

function handleWheel(event: WheelEvent) {
  if (!props.images.length) return

  event.preventDefault()
  const delta = Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : event.deltaY
  targetCameraX.value = clamp(targetCameraX.value + delta * 0.95, 0, maxCameraX.value)
}

function handleKeydown(event: KeyboardEvent) {
  if (!props.images.length) return

  if (event.key === 'ArrowRight' || event.key.toLowerCase() === 'd') {
    jumpTo(activeIndex.value + 1)
  }

  if (event.key === 'ArrowLeft' || event.key.toLowerCase() === 'a') {
    jumpTo(activeIndex.value - 1)
  }
}

function jumpTo(index: number) {
  const next = clamp(index, 0, props.images.length - 1)
  const node = photoNodes.value[next]
  if (!node) return

  const focusOffset = viewportWidth.value * (viewportWidth.value < 768 ? 0.5 : 0.56)
  targetCameraX.value = clamp(node.x - focusOffset, 0, maxCameraX.value)
  velocity.value = 0
}

function syncActiveIndex() {
  if (!photoNodes.value.length || viewportWidth.value <= 0) {
    emit('active-change', 0)
    return
  }

  const focusX = cameraX.value + viewportWidth.value * (viewportWidth.value < 768 ? 0.5 : 0.56)
  let nextIndex = 0
  let minDistance = Number.POSITIVE_INFINITY

  for (const node of photoNodes.value) {
    const distance = Math.abs(node.x - focusX)
    if (distance < minDistance) {
      minDistance = distance
      nextIndex = node.index
    }
  }

  if (activeIndex.value !== nextIndex) {
    activeIndex.value = nextIndex
  }

  emit('active-change', nextIndex)
}

function buildRoutePoints(points: TrackViewerPoint[], imageCount: number) {
  if (points.length >= 2) {
    return normalizeTrackPoints(points)
  }

  return buildFallbackRoute(imageCount)
}

function normalizeTrackPoints(points: TrackViewerPoint[]): NormalizedPoint[] {
  const maxLat = Math.max(...points.map((point) => point.lat))
  const minLat = Math.min(...points.map((point) => point.lat))
  const latSpan = Math.max(maxLat - minLat, 0.0001)

  return points.map((point, index) => {
    const progress = points.length <= 1 ? 0 : index / (points.length - 1)
    const normalizedLat = (point.lat - minLat) / latSpan
    const curveLift = Math.sin(progress * Math.PI * 2.6) * 0.08
    return {
      x: progress,
      y: clamp(0.18 + (1 - normalizedLat) * 0.62 + curveLift, 0.1, 0.92),
    }
  })
}

function buildFallbackRoute(imageCount: number) {
  const count = Math.max(imageCount * 4, 18)
  return Array.from({ length: count }, (_entry, index) => {
    const progress = count <= 1 ? 0 : index / (count - 1)
    const waveA = Math.sin(progress * Math.PI * 1.4) * 0.22
    const waveB = Math.sin(progress * Math.PI * 4.2 + 0.6) * 0.08
    return {
      x: progress,
      y: clamp(0.48 + waveA + waveB, 0.16, 0.88),
    }
  })
}

function samplePoints<T>(points: T[], count: number) {
  if (!points.length) return []
  if (count <= 1) return [points[0]!]

  return Array.from({ length: count }, (_entry, index) => {
    const progress = index / Math.max(count - 1, 1)
    const targetIndex = Math.round(progress * (points.length - 1))
    return points[targetIndex] ?? points[points.length - 1]!
  })
}

function createSvgPath(points: Array<{ x: number; y: number }>) {
  if (!points.length) return ''
  if (points.length === 1) return `M ${points[0]!.x} ${points[0]!.y}`

  return points.reduce((path, point, index, allPoints) => {
    if (index === 0) {
      return `M ${point.x} ${point.y}`
    }

    const previous = allPoints[index - 1]!
    const controlX = (previous.x + point.x) / 2
    return `${path} C ${controlX} ${previous.y}, ${controlX} ${point.y}, ${point.x} ${point.y}`
  }, '')
}

function buildCameraLabel(progress: number, location: string) {
  const localeLabel = location || '路线现场'
  if (progress < 0.22) return `${localeLabel} · 入口光影采样`
  if (progress < 0.48) return `${localeLabel} · 中段视差采样`
  if (progress < 0.76) return `${localeLabel} · 山脊观景窗口`
  return `${localeLabel} · 收束段氛围记录`
}

function buildNarrative(progress: number, description: string) {
  const base = description || '让路线的每一次停步，都变成一个可回看的节点。'
  if (progress < 0.25) return `从起点进入节奏，${base}`
  if (progress < 0.5) return `路径开始拉开空间层次，${base}`
  if (progress < 0.75) return `这里进入视觉高潮段，${base}`
  return `路线走向终章，${base}`
}

function buildTimeLabel(progress: number) {
  if (progress < 0.22) return '06:20'
  if (progress < 0.48) return '08:40'
  if (progress < 0.76) return '11:10'
  return '14:30'
}

function formatDistance(distanceMeters?: number | null) {
  if (typeof distanceMeters !== 'number' || Number.isNaN(distanceMeters) || distanceMeters <= 0) {
    return ''
  }
  return `${(distanceMeters / 1000).toFixed(1)} km`
}

function formatElevation(elevationGainMeters?: number | null) {
  if (typeof elevationGainMeters !== 'number' || Number.isNaN(elevationGainMeters) || elevationGainMeters <= 0) {
    return ''
  }
  return `+${Math.round(elevationGainMeters)} m`
}

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max)
}
</script>

<template>
  <div
    class="gallery-experience"
    @pointerdown="handlePointerDown"
    @pointermove="handlePointerMove"
    @pointerup="handlePointerUp"
    @pointercancel="handlePointerUp"
    @wheel="handleWheel"
  >
    <div ref="scene" class="gallery-scene"></div>

    <div class="gallery-atmosphere">
      <div class="gallery-orb gallery-orb-left"></div>
      <div class="gallery-orb gallery-orb-right"></div>
      <div class="gallery-grain"></div>
      <div class="gallery-mesh"></div>
      <p class="gallery-watermark">{{ props.location || 'TRAILQUEST' }}</p>
    </div>

    <div class="gallery-shell">
      <section class="gallery-copy">
        <p class="gallery-kicker">Route Image Atlas</p>
        <h1 class="gallery-title">{{ props.title }}</h1>
        <p class="gallery-subtitle">
          把路线拆成一连串可被回看的照片节点。背景不是装饰，而是一张会呼吸的路径地图。
        </p>

        <div class="gallery-stats">
          <div v-for="stat in routeStats" :key="stat.label" class="gallery-stat">
            <span class="gallery-stat-label">{{ stat.label }}</span>
            <span class="gallery-stat-value">{{ stat.value }}</span>
          </div>
        </div>

        <div v-if="activeNode" class="gallery-active-panel">
          <div class="gallery-active-panel-header">
            <span class="gallery-active-panel-index">{{ activeNode.indexLabel }}</span>
            <span class="gallery-active-panel-route">{{ activeNode.segmentLabel }}</span>
          </div>
          <p class="gallery-active-panel-title">{{ activeNode.cameraLabel }}</p>
          <p class="gallery-active-panel-text">{{ activeNode.caption }}</p>
          <div class="gallery-active-panel-tags">
            <span class="gallery-active-chip">
              <BaseIcon name="MapPinned" :size="14" />
              {{ props.location || '路线节点' }}
            </span>
            <span class="gallery-active-chip">
              <BaseIcon name="Clock3" :size="14" />
              {{ activeNode.timeLabel }}
            </span>
          </div>
        </div>
      </section>

      <section class="gallery-stage-panel">
        <div ref="viewport" class="gallery-stage-viewport">
          <div
            class="gallery-stage-rail"
            :style="{
              width: `${stageWidth}px`,
              height: `${stageHeight}px`,
              transform: `translate3d(${-cameraX}px, 0, 0)`,
            }"
          >
            <svg
              class="gallery-route-svg"
              :viewBox="`0 0 ${stageWidth} ${stageHeight}`"
              preserveAspectRatio="none"
            >
              <defs>
                <linearGradient id="routeGlow" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#6cffc5" stop-opacity="0.15" />
                  <stop offset="48%" stop-color="#b4ffdf" stop-opacity="0.95" />
                  <stop offset="100%" stop-color="#4cc694" stop-opacity="0.18" />
                </linearGradient>
              </defs>

              <path :d="routePathD" class="gallery-route-path-shadow" />
              <path :d="routePathD" class="gallery-route-path" />
              <circle
                v-for="(point, index) in sampledNodeAnchors"
                :key="`anchor-${index}`"
                class="gallery-route-anchor"
                :cx="point.x"
                :cy="point.y"
                r="4.8"
              />
            </svg>

            <article
              v-for="node in photoNodes"
              :key="`${node.image}-${node.index}`"
              class="gallery-node"
              :class="[
                `gallery-node-lane-${node.lane === -1 ? 'upper' : 'lower'}`,
                { 'gallery-node-active': node.isActive },
              ]"
              :style="node.style"
            >
              <div class="gallery-node-core"></div>
              <div class="gallery-node-ring"></div>
              <div class="gallery-node-stem"></div>
              <TrailPhotoNodeCard
                :title="props.title"
                :image="node.image"
                :alt="`${props.title} 图片 ${node.index + 1}`"
                :index-label="node.indexLabel"
                :segment-label="node.segmentLabel"
                :camera-label="node.cameraLabel"
                :progress-label="node.progressLabel"
                :size="node.size"
                :is-active="node.isActive"
                :emphasis="node.emphasis"
                @select="jumpTo(node.index)"
              />
            </article>
          </div>
        </div>

        <div class="gallery-controls">
          <button type="button" class="gallery-control" @click="jumpTo(activeIndex - 1)">
            <BaseIcon name="ArrowLeft" :size="15" />
            上一节点
          </button>
          <div class="gallery-counter">{{ counterLabel }}</div>
          <button type="button" class="gallery-control" @click="jumpTo(activeIndex + 1)">
            下一节点
            <BaseIcon name="ArrowRight" :size="15" />
          </button>
        </div>
      </section>
    </div>

    <div class="gallery-footer">
      <div class="gallery-hint">
        <BaseIcon name="MousePointer2" :size="14" />
        {{ statusText }}
      </div>

      <div class="gallery-progress">
        <div class="gallery-progress-track">
          <div class="gallery-progress-fill" :style="progressFillStyle"></div>
          <div class="gallery-progress-thumb" :style="progressThumbStyle"></div>
        </div>
        <div class="gallery-progress-labels">
          <span>Trail Start</span>
          <span>Photo {{ activeLabel }}</span>
          <span>Trail End</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gallery-experience {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(61, 159, 119, 0.18), transparent 34%),
    radial-gradient(circle at 85% 16%, rgba(142, 255, 218, 0.12), transparent 28%),
    linear-gradient(180deg, #02090c 0%, #031114 42%, #05191a 100%);
  color: rgba(246, 255, 250, 0.96);
}

.gallery-scene,
.gallery-atmosphere,
.gallery-shell,
.gallery-footer {
  position: relative;
  z-index: 1;
}

.gallery-scene,
.gallery-atmosphere {
  position: absolute;
  inset: 0;
}

.gallery-orb,
.gallery-grain,
.gallery-mesh,
.gallery-watermark {
  position: absolute;
}

.gallery-orb {
  border-radius: 999px;
  filter: blur(48px);
}

.gallery-orb-left {
  inset: 18% auto auto -6%;
  width: 24rem;
  height: 24rem;
  background: rgba(70, 174, 133, 0.12);
}

.gallery-orb-right {
  inset: auto -4% 14% auto;
  width: 28rem;
  height: 28rem;
  background: rgba(169, 255, 228, 0.08);
}

.gallery-grain {
  inset: 0;
  opacity: 0.12;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.38), transparent 85%);
}

.gallery-mesh {
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0%, rgba(162, 255, 218, 0.08) 48%, transparent 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), transparent 44%);
  opacity: 0.22;
}

.gallery-watermark {
  right: -2rem;
  bottom: 0.6rem;
  margin: 0;
  max-width: min(72rem, 88vw);
  font-family: 'Arial Narrow', 'Avenir Next Condensed', sans-serif;
  font-size: clamp(6rem, 14vw, 15rem);
  font-weight: 900;
  line-height: 0.82;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: rgba(234, 251, 245, 0.08);
  text-align: right;
  pointer-events: none;
}

.gallery-shell {
  display: grid;
  grid-template-columns: minmax(17rem, 29rem) minmax(0, 1fr);
  gap: 1.4rem;
  min-height: 100vh;
  padding: 5.5rem 1.5rem 7.5rem;
}

.gallery-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.25rem;
  padding-left: 0.6rem;
}

.gallery-kicker,
.gallery-stat-label,
.gallery-counter,
.gallery-progress-labels,
.gallery-active-panel-header,
.gallery-control,
.gallery-hint {
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.gallery-kicker {
  color: rgba(191, 238, 219, 0.68);
  font-size: 0.72rem;
}

.gallery-title {
  font-family: 'Iowan Old Style', 'Palatino Linotype', 'Times New Roman', serif;
  font-size: clamp(3.4rem, 7vw, 6.6rem);
  line-height: 0.92;
}

.gallery-subtitle {
  max-width: 28rem;
  font-size: 1rem;
  line-height: 1.75;
  color: rgba(224, 245, 235, 0.76);
}

.gallery-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  margin-top: 0.4rem;
}

.gallery-stat,
.gallery-active-panel,
.gallery-control,
.gallery-counter,
.gallery-hint,
.gallery-progress {
  border: 1px solid rgba(184, 255, 222, 0.12);
  background: rgba(6, 16, 17, 0.46);
  backdrop-filter: blur(16px);
}

.gallery-stat {
  display: flex;
  flex-direction: column;
  gap: 0.48rem;
  padding: 0.9rem 1rem;
  border-radius: 1rem;
}

.gallery-stat-label {
  font-size: 0.7rem;
  color: rgba(192, 235, 220, 0.58);
}

.gallery-stat-value {
  font-size: 1rem;
  color: rgba(249, 255, 252, 0.96);
}

.gallery-active-panel {
  margin-top: 0.5rem;
  padding: 1.2rem 1.2rem 1.1rem;
  border-radius: 1.45rem;
}

.gallery-active-panel-header {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  font-size: 0.72rem;
  color: rgba(189, 241, 220, 0.64);
}

.gallery-active-panel-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2.2rem;
  padding: 0.28rem 0.45rem;
  border-radius: 999px;
  background: rgba(171, 255, 219, 0.12);
  color: rgba(239, 255, 248, 0.92);
}

.gallery-active-panel-title {
  margin-top: 0.9rem;
  font-family: 'Iowan Old Style', 'Palatino Linotype', 'Times New Roman', serif;
  font-size: 1.4rem;
  line-height: 1.1;
}

.gallery-active-panel-text {
  margin-top: 0.6rem;
  font-size: 0.94rem;
  line-height: 1.72;
  color: rgba(220, 242, 231, 0.76);
}

.gallery-active-panel-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  margin-top: 1rem;
}

.gallery-active-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.5rem 0.72rem;
  border-radius: 999px;
  background: rgba(168, 255, 217, 0.08);
  color: rgba(239, 255, 248, 0.84);
  font-size: 0.82rem;
}

.gallery-stage-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1rem;
  min-width: 0;
}

.gallery-stage-viewport {
  position: relative;
  overflow: hidden;
  border-radius: 2rem;
  border: 1px solid rgba(183, 255, 221, 0.11);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 22%),
    linear-gradient(180deg, rgba(2, 8, 12, 0.24), rgba(2, 8, 12, 0.42));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 32px 80px rgba(1, 7, 8, 0.42);
  touch-action: none;
}

.gallery-stage-rail {
  position: relative;
  transition: transform 0.28s ease-out;
}

.gallery-route-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.gallery-route-path-shadow,
.gallery-route-path {
  fill: none;
  stroke-linecap: round;
}

.gallery-route-path-shadow {
  stroke: rgba(135, 255, 205, 0.12);
  stroke-width: 20;
  filter: blur(10px);
}

.gallery-route-path {
  stroke: url(#routeGlow);
  stroke-width: 4.2;
  stroke-dasharray: 7 10;
}

.gallery-route-anchor {
  fill: rgba(215, 255, 239, 0.94);
  opacity: 0.74;
}

.gallery-node {
  position: absolute;
  transform-origin: center center;
}

.gallery-node-core,
.gallery-node-ring,
.gallery-node-stem {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
}

.gallery-node-core {
  top: 0;
  width: 0.8rem;
  height: 0.8rem;
  border-radius: 999px;
  background: rgba(218, 255, 239, 0.98);
  box-shadow: 0 0 0 0.35rem rgba(140, 255, 206, 0.11);
}

.gallery-node-ring {
  top: -0.48rem;
  width: 1.7rem;
  height: 1.7rem;
  border-radius: 999px;
  border: 1px solid rgba(154, 255, 212, 0.3);
  opacity: 0.72;
}

.gallery-node-stem {
  width: 1px;
  background: linear-gradient(180deg, rgba(149, 255, 211, 0.7), rgba(149, 255, 211, 0));
}

.gallery-node-lane-upper .gallery-node-stem {
  top: -3.8rem;
  height: 3.4rem;
}

.gallery-node-lane-upper :deep(.photo-node-card) {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 4.3rem);
  transform: translateX(-50%);
}

.gallery-node-lane-lower .gallery-node-stem {
  top: 0.8rem;
  height: 3.2rem;
}

.gallery-node-lane-lower :deep(.photo-node-card) {
  position: absolute;
  left: 50%;
  top: calc(100% + 4.2rem);
  transform: translateX(-50%);
}

.gallery-node-active .gallery-node-ring {
  border-color: rgba(201, 255, 233, 0.82);
  box-shadow: 0 0 36px rgba(90, 233, 166, 0.28);
}

.gallery-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.gallery-control,
.gallery-counter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.55rem;
  min-height: 3rem;
  border-radius: 999px;
  color: rgba(244, 255, 249, 0.9);
}

.gallery-control {
  border-radius: 999px;
  padding: 0.8rem 1.05rem;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.gallery-control:hover {
  transform: translateY(-1px);
  border-color: rgba(182, 255, 222, 0.28);
  background: rgba(7, 18, 18, 0.62);
}

.gallery-counter {
  min-width: 8.5rem;
  padding: 0.8rem 1.15rem;
  font-size: 0.76rem;
}

.gallery-footer {
  position: absolute;
  inset-inline: 0;
  bottom: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.3rem 1.5rem 1.4rem;
}

.gallery-hint,
.gallery-progress {
  border-radius: 1.3rem;
}

.gallery-hint {
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.95rem 1.1rem;
  color: rgba(228, 248, 238, 0.82);
  font-size: 0.76rem;
}

.gallery-progress {
  width: min(30rem, 58vw);
  padding: 1rem 1.05rem 0.9rem;
}

.gallery-progress-track {
  position: relative;
  height: 0.35rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.gallery-progress-fill {
  position: absolute;
  inset: 0 auto 0 0;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(81, 190, 140, 0.18), rgba(192, 255, 228, 0.92));
}

.gallery-progress-thumb {
  position: absolute;
  top: 50%;
  width: 1.05rem;
  height: 1.05rem;
  border-radius: 999px;
  background: rgba(235, 255, 247, 0.96);
  box-shadow: 0 0 0 0.35rem rgba(152, 255, 208, 0.12);
  transform: translate(-50%, -50%);
}

.gallery-progress-labels {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 0.8rem;
  font-size: 0.68rem;
  color: rgba(199, 234, 220, 0.56);
}

@media (max-width: 1120px) {
  .gallery-shell {
    grid-template-columns: 1fr;
    gap: 1rem;
    padding-top: 5.2rem;
  }

  .gallery-copy {
    padding-left: 0;
    max-width: 42rem;
  }

  .gallery-subtitle {
    max-width: none;
  }
}

@media (max-width: 767px) {
  .gallery-shell {
    padding: 5rem 0.9rem 8.8rem;
  }

  .gallery-watermark {
    right: -1rem;
    font-size: clamp(4.6rem, 20vw, 8rem);
    max-width: 92vw;
  }

  .gallery-title {
    font-size: clamp(2.7rem, 14vw, 4rem);
  }

  .gallery-subtitle {
    font-size: 0.92rem;
    line-height: 1.65;
  }

  .gallery-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .gallery-stage-viewport {
    border-radius: 1.4rem;
  }

  .gallery-controls,
  .gallery-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .gallery-counter,
  .gallery-control {
    width: 100%;
  }

  .gallery-progress {
    width: 100%;
  }

  .gallery-hint {
    justify-content: center;
  }

  .gallery-node-lane-upper .gallery-node-stem {
    top: -3.1rem;
    height: 2.8rem;
  }

  .gallery-node-lane-upper :deep(.photo-node-card) {
    bottom: calc(100% + 3.5rem);
  }

  .gallery-node-lane-lower .gallery-node-stem {
    height: 2.6rem;
  }

  .gallery-node-lane-lower :deep(.photo-node-card) {
    top: calc(100% + 3.5rem);
  }
}
</style>
