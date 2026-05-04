<script setup lang="ts">
import { computed } from 'vue'
import SectionHeader from '../common/SectionHeader.vue'
import BaseIcon from '../common/BaseIcon.vue'
import TrailMapSection from './TrailMapSection.vue'
import TrailTrackViewer from './TrailTrackViewer.vue'
import type { TrackViewerData, TrackWeatherScene } from '../../types/trackViewer'

type VisualizationMode = 'map' | 'model'

const props = defineProps<{
  modelValue: VisualizationMode
  center: [number, number] | null
  label: string
  city: string
  trackGeoJson?: unknown
  trackDownloadUrl?: string | null
  trackViewerData: TrackViewerData | null
  weatherScene?: TrackWeatherScene
}>()

const emit = defineEmits<{
  'update:modelValue': [value: VisualizationMode]
  requestFullscreen: []
}>()

const options = computed<
  Array<{
    value: VisualizationMode
    label: string
    icon: string
    disabled?: boolean
  }>
>(() => [
  { value: 'map', label: '路线地图', icon: 'Map' },
  { value: 'model', label: '路线建模', icon: 'Box', disabled: !props.trackViewerData },
])

function selectMode(value: VisualizationMode, disabled?: boolean) {
  if (disabled || props.modelValue === value) return
  emit('update:modelValue', value)
}
</script>

<template>
  <section class="space-y-4">
    <SectionHeader title="路线展示" subtitle="在地图轨迹与路线建模之间切换查看">
      <template #action>
        <div class="flex items-center rounded-xl p-1" style="background-color: var(--bg-tag)">
          <button
            v-for="option in options"
            :key="option.value"
            type="button"
            class="flex items-center gap-1.5 rounded-lg px-3 py-2 text-xs font-medium transition-all duration-200"
            :class="[
              props.modelValue === option.value ? 'shadow-sm' : '',
              option.disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer',
            ]"
            :style="
              props.modelValue === option.value
                ? 'background-color: var(--bg-card); color: var(--text-primary);'
                : 'color: var(--text-tertiary);'
            "
            :disabled="option.disabled"
            @click="selectMode(option.value, option.disabled)"
          >
            <BaseIcon :name="option.icon" :size="14" />
            {{ option.label }}
          </button>
        </div>
      </template>
    </SectionHeader>

    <div v-show="props.modelValue === 'map'">
      <TrailMapSection
        hide-header
        :active="props.modelValue === 'map'"
        :center="center"
        :label="label"
        :city="city"
        :track-geo-json="trackGeoJson"
        :track-download-url="trackDownloadUrl"
      />
    </div>

    <div
      v-if="trackViewerData"
      v-show="props.modelValue === 'model'"
      class="card h-[500px] min-h-[500px] p-4"
    >
      <TrailTrackViewer
        class="h-full overflow-hidden rounded-2xl border border-white/5 shadow-2xl"
        :active="props.modelValue === 'model'"
        :data="trackViewerData"
        :weather-scene="weatherScene"
        mode="detail"
        :show-scroll-to-content-button="false"
        @request-fullscreen="emit('requestFullscreen')"
      />
    </div>

    <div v-else class="card flex min-h-[320px] items-center justify-center p-8 text-center">
      <div class="max-w-sm space-y-3">
        <div
          class="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl"
          style="
            color: var(--text-secondary);
            background: color-mix(in srgb, var(--primary-500) 10%, transparent);
          "
        >
          <BaseIcon name="Box" :size="22" />
        </div>
        <div class="space-y-1">
          <p class="text-sm font-semibold" style="color: var(--text-primary)">暂无路线建模数据</p>
          <p class="text-sm leading-6" style="color: var(--text-secondary)">
            当前路线还没有可用于建模的轨迹文件，先查看路线地图吧。
          </p>
        </div>
      </div>
    </div>
  </section>
</template>
