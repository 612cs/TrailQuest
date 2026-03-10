<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import SectionHeader from '../common/SectionHeader.vue'
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

const weatherData = computed(() => {
  if (!props.weather) {
    const placeholder = props.isLoading ? '加载中' : '—'
    return [
      { label: '温度', value: placeholder, desc: '实时气温', icon: 'ThermometerSun' },
      { label: '湿度', value: placeholder, desc: '空气湿度', icon: 'Droplets' },
      { label: '风向', value: placeholder, desc: '风向风力', icon: 'Wind' },
    ]
  }

  const temp = `${props.weather.temperature}°C`
  const humidity = `${props.weather.humidity}%`
  const wind = `${props.weather.windDirection}${props.weather.windPower}`

  return [
    {
      label: '温度',
      value: temp,
      desc: `天气：${props.weather.weather}`,
      icon: 'ThermometerSun',
    },
    {
      label: '湿度',
      value: humidity,
      desc: '相对湿度',
      icon: 'Droplets',
    },
    {
      label: '风向',
      value: wind,
      desc: '实时风况',
      icon: 'Wind',
    },
  ]
})
</script>

<template>
  <section class="animate-fade-in-up stagger-1">
    <SectionHeader title="当前天气状况" :subtitle="subtitle" />
    <div class="grid grid-cols-3 gap-3">
      <div v-for="w in weatherData" :key="w.label" class="card p-4 text-center group hover:border-primary-500/30 transition-colors">
        <div class="flex justify-center text-primary-500 group-hover:scale-110 transition-transform">
          <BaseIcon :name="w.icon" :size="28" />
        </div>
        <p class="text-lg font-bold mt-2" style="color: var(--text-primary);">{{ w.value }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">{{ w.label }}</p>
        <p class="text-xs mt-1" style="color: var(--text-secondary);">{{ w.desc }}</p>
      </div>
    </div>
  </section>
</template>
