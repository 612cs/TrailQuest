<script setup lang="ts">
import { computed, onUnmounted, shallowRef, watch, useTemplateRef, nextTick } from 'vue'
import SectionHeader from '../common/SectionHeader.vue'
import { useAmapLoader } from '../../composables/useAmapLoader'

const props = defineProps<{
  center: [number, number] | null
  label: string
  city: string
  trackGeoJson?: unknown
  trackDownloadUrl?: string | null
}>()

const mapRef = useTemplateRef<HTMLDivElement>('map')
const mapInstance = shallowRef<any>(null)
const markerInstance = shallowRef<any>(null)
const mapReady = shallowRef(false)
const mapError = shallowRef<string | null>(null)
const { isReady, error, load } = useAmapLoader()

const hasCenter = computed(() => Boolean(props.center))
const hasTrack = computed(() => Boolean(props.trackGeoJson))

async function initMap() {
  if (!props.center && !props.trackGeoJson) return

  mapError.value = null
  const AMap = await load()

  if (error.value) {
    mapError.value = error.value
    return
  }

  if (!AMap) {
    mapError.value = '地图加载失败'
    return
  }

  mapReady.value = true
  await nextTick()

  if (!mapRef.value) return

  if (!mapInstance.value) {
    mapInstance.value = new AMap.Map(mapRef.value, {
      zoom: 11,
      center: props.center ?? undefined,
      resizeEnable: true,
    })
  } else if (props.center) {
    mapInstance.value.setCenter(props.center)
  }

  mapInstance.value.clearMap()

  if (props.trackGeoJson) {
    AMap.plugin('AMap.GeoJSON', () => {
      const geojsonObj = new AMap.GeoJSON({
        geoJSON: props.trackGeoJson,
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
      })

      mapInstance.value.add(geojsonObj)
      mapInstance.value.setFitView()
    })
    return
  }

  if (markerInstance.value) {
    markerInstance.value.setPosition(props.center)
  } else {
    markerInstance.value = new AMap.Marker({
      position: props.center,
      title: props.label,
    })
    mapInstance.value.add(markerInstance.value)
  }
}

watch(
  () => props.center,
  () => {
    initMap()
  },
  { immediate: true }
)

watch(
  () => props.trackGeoJson,
  () => {
    initMap()
  }
)

watch(
  () => isReady.value,
  (ready) => {
    if (ready && (props.center || props.trackGeoJson)) {
      initMap()
    }
  }
)

watch(
  () => props.label,
  () => {
    if (markerInstance.value) {
      markerInstance.value.setTitle(props.label)
    }
  }
)

onUnmounted(() => {
  if (mapInstance.value?.destroy) {
    mapInstance.value.destroy()
  }
  mapInstance.value = null
  markerInstance.value = null
})
</script>

<template>
  <section class="animate-fade-in-up stagger-1">
    <SectionHeader title="路线地图" />
    <div class="card p-4 space-y-4">
      <div class="map-shell" :class="{ 'is-empty': (!hasCenter && !hasTrack) || !mapReady }">
        <!-- 始终挂载 DOM，如果还没有准备好通过 z-index 置于下层或隐藏视觉内容 -->
        <div ref="map" class="map-canvas" :style="{ display: (mapReady && (hasCenter || hasTrack) && !mapError) ? 'block' : 'none' }"></div>
        <div v-if="!hasCenter && !hasTrack" class="map-placeholder">
          暂无法获取定位，稍后重试
        </div>
        <div v-else-if="mapError" class="map-placeholder">
          {{ mapError }}
        </div>
        <div v-else-if="!mapReady" class="map-placeholder z-10">
          地图加载中...
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.map-shell {
  position: relative;
  width: 100%;
  height: 375px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--primary-500) 18%, transparent);
  background: linear-gradient(145deg, color-mix(in srgb, var(--primary-500) 6%, transparent), transparent 60%);
}

.map-shell.is-empty {
  border-style: dashed;
}

.map-canvas {
  width: 100%;
  height: 100%;
}

.map-placeholder {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 0.85rem;
  color: var(--text-tertiary);
  background: linear-gradient(130deg, color-mix(in srgb, var(--text-tertiary) 12%, transparent), transparent 60%);
}
</style>
