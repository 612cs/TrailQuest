<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import type { TrailWeatherForecastDay } from '../../types/weather'

const props = defineProps<{
  forecast: TrailWeatherForecastDay[]
}>()

function getWeatherIcon(weatherDesc: string) {
  if (weatherDesc.includes('晴')) return 'Sun'
  if (weatherDesc.includes('雷')) return 'CloudLightning'
  if (weatherDesc.includes('雨')) return 'CloudRain'
  if (weatherDesc.includes('雪')) return 'CloudSnow'
  if (weatherDesc.includes('多云')) return 'Cloudy'
  if (weatherDesc.includes('阴')) return 'Cloud'
  if (weatherDesc.includes('雾') || weatherDesc.includes('霾')) return 'CloudFog'
  return 'Cloud'
}

function getWeatherColor(weatherDesc: string) {
  if (weatherDesc.includes('晴')) return 'text-yellow-400'
  if (weatherDesc.includes('雷') || weatherDesc.includes('雨')) return 'text-blue-400'
  if (weatherDesc.includes('雪')) return 'text-blue-200'
  return 'text-gray-400'
}

const formatItems = computed(() => {
  if (!props.forecast.length) return []

  // Skip index 0 (Today)
  const remainingForecast = props.forecast.slice(1)

  return remainingForecast.map((day) => {
    const minT = Math.min(day.tempMax ?? 0, day.tempMin ?? 0)
    const maxT = Math.max(day.tempMax ?? 0, day.tempMin ?? 0)
    
    // Format date string (YYYY-MM-DD -> MM-DD)
    const dateParts = day.date.split('-')
    const formattedDate = dateParts.length >= 3 ? `${dateParts[1]}-${dateParts[2]}` : day.date

    return {
      id: day.date,
      date: formattedDate,
      weekday: day.week,
      weather: day.textDay || day.textNight || '未知',
      icon: getWeatherIcon(day.textDay || day.textNight || ''),
      iconColor: getWeatherColor(day.textDay || day.textNight || ''),
      minTemp: minT,
      maxTemp: maxT,
    }
  })
})
</script>

<template>
  <div class="grid grid-cols-2 gap-3" v-if="formatItems.length > 0">
    <div 
      v-for="item in formatItems" 
      :key="item.id" 
      class="p-3.5 bg-white/3 rounded-2xl border border-primary-500/10 flex flex-col gap-3 hover:bg-white/5 transition-colors"
    >
      <div class="flex items-center justify-between">
        <div class="flex flex-col">
          <span class="text-sm font-medium" style="color: var(--text-primary);">{{ item.weekday }}</span>
          <span class="text-[10px] uppercase tracking-wider" style="color: var(--text-tertiary);">{{ item.date }}</span>
        </div>
        <div :class="['p-1.5 rounded-lg bg-white/5', item.iconColor]">
          <BaseIcon :name="item.icon" :size="18" />
        </div>
      </div>
      
      <div class="flex items-baseline gap-2">
        <span class="text-base font-bold" style="color: var(--text-primary);">{{ item.maxTemp }}°</span>
        <span class="text-xs" style="color: var(--text-secondary);">{{ item.minTemp }}°</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Remove list styles since we are now a grid */
</style>
