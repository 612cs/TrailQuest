<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import type { TrailWeather } from '../../composables/useTrailWeather'

const props = defineProps<{
  weather: TrailWeather | null
  isLoading: boolean
  fallbackCity?: string
}>()

const subtitle = computed(() => {
  if (props.weather) {
    return `${props.weather.city} · ${props.weather.reportTime}`
  }
  if (props.isLoading) {
    return '天气查询中...'
  }
  return props.fallbackCity ? `${props.fallbackCity} · 暂无实时数据` : '暂无实时数据'
})

</script>

<template>
  <section class="animate-fade-in-up stagger-1">
    <div class="flex items-start justify-between">
      <div>
        <h3 class="text-sm font-medium" style="color: var(--text-primary);">当前天气</h3>
        <div class="flex items-baseline gap-2 mt-2">
          <span class="text-5xl font-bold tracking-tighter" style="color: var(--text-primary);">{{ weather?.temperature || '--' }}°</span>
          <span class="text-lg font-medium" style="color: var(--text-secondary);">{{ weather?.weather || '加载中' }}</span>
        </div>
        <p class="text-sm mt-1 flex items-center gap-1" style="color: var(--text-tertiary);">
          <BaseIcon name="MapPin" :size="14" />
          {{ subtitle }}
        </p>
      </div>
      
      <!-- Right side compact metrics -->
      <div class="flex flex-col gap-2 text-sm text-right mt-1">
        <div class="flex items-center justify-end gap-2" style="color: var(--text-secondary);">
          <BaseIcon name="Droplets" :size="14" />
          <span>湿度 {{ weather?.humidity || '--' }}%</span>
        </div>
        <div class="flex items-center justify-end gap-2" style="color: var(--text-secondary);">
          <BaseIcon name="Wind" :size="14" />
          <span>{{ weather?.windDirection || '' }}风 {{ weather?.windPower || '--' }}级</span>
        </div>
      </div>
    </div>
  </section>
</template>
