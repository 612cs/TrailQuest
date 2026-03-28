<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import type { TrailWeatherForecastDay } from '../../types/weather'

const props = defineProps<{
  day: TrailWeatherForecastDay | null
  isLoading: boolean
  fallbackCity?: string
  errorMessage?: string
}>()

const subtitle = computed(() => {
  if (props.day) {
    return props.fallbackCity ? `${props.fallbackCity} · 今日概览` : '今日概览'
  }
  if (props.isLoading) {
    return '天气查询中...'
  }
  if (props.errorMessage) {
    return props.errorMessage
  }
  return props.fallbackCity ? `${props.fallbackCity} · 暂无预报数据` : '暂无预报数据'
})

</script>

<template>
  <section class="animate-fade-in-up stagger-1">
    <div class="flex items-start justify-between">
      <div>
        <h3 class="text-sm font-medium" style="color: var(--text-primary);">未来天气概览</h3>
        <div class="flex items-baseline gap-2 mt-2">
          <span class="text-5xl font-bold tracking-tighter" style="color: var(--text-primary);">{{ day?.tempMax ?? '--' }}°</span>
          <span class="text-lg font-medium" style="color: var(--text-secondary);">{{ day?.textDay || (isLoading ? '加载中' : '暂无预报') }}</span>
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
          <span>湿度 {{ day?.humidity ?? '--' }}%</span>
        </div>
        <div class="flex items-center justify-end gap-2" style="color: var(--text-secondary);">
          <BaseIcon name="Wind" :size="14" />
          <span>{{ day?.windDirDay || '' }} {{ day?.windScaleDay || '--' }}级</span>
        </div>
        <div class="flex items-center justify-end gap-2" style="color: var(--text-secondary);">
          <BaseIcon name="MoonStar" :size="14" />
          <span>夜间 {{ day?.textNight || '--' }} {{ day?.tempMin ?? '--' }}°</span>
        </div>
      </div>
    </div>
  </section>
</template>
