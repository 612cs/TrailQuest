<script setup lang="ts">
import { computed, onUnmounted, shallowRef, watch, useTemplateRef, nextTick } from 'vue'
import SectionHeader from '../common/SectionHeader.vue'
import { useAmapLoader } from '../../composables/useAmapLoader'

const props = defineProps<{
  center: [number, number] | null
  label: string
  city: string
}>()

const mapRef = useTemplateRef<HTMLDivElement>('map')
const mapInstance = shallowRef<any>(null)
const markerInstance = shallowRef<any>(null)
const mapReady = shallowRef(false)
const mapError = shallowRef<string | null>(null)
const { isReady, error, load } = useAmapLoader()

const hasCenter = computed(() => Boolean(props.center))

async function initMap() {
  if (!props.center) return

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
      center: props.center,
      resizeEnable: true,
    })
  } else {
    mapInstance.value.setCenter(props.center)
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
  () => isReady.value,
  (ready) => {
    if (ready && props.center) {
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
    <SectionHeader title="路线地图" :subtitle="city ? `${city} 定位` : '定位中...'" />
    <div class="card p-4 space-y-3">
      <div class="flex items-center justify-between text-xs" style="color: var(--text-tertiary);">
        <span>定位来源</span>
        <span>{{ hasCenter ? '高德 IP/地理解析' : '定位失败' }}</span>
      </div>
      <div class="map-shell" :class="{ 'is-empty': !hasCenter || !mapReady }">
        <!-- 始终挂载 DOM，如果还没有准备好通过 z-index 置于下层或隐藏视觉内容 -->
        <div ref="map" class="map-canvas" :style="{ display: (mapReady && hasCenter && !mapError) ? 'block' : 'none' }"></div>
        <div v-if="!hasCenter" class="map-placeholder">
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
  height: 260px;
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
