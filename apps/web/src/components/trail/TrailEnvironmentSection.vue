<script setup lang="ts">
import SectionHeader from '../common/SectionHeader.vue'
import LandscapePrediction from './LandscapePrediction.vue'
import WeatherForecast from './WeatherForecast.vue'
import WeatherSection from './WeatherSection.vue'
import type { TrailLandscapePredictionResponse } from '../../types/landscape'
import type { TrailWeatherForecastDay } from '../../types/weather'

defineProps<{
  day: TrailWeatherForecastDay | null
  forecast: TrailWeatherForecastDay[]
  isWeatherLoading: boolean
  weatherErrorMessage?: string
  fallbackCity?: string
  landscapePrediction: TrailLandscapePredictionResponse | null
  isLandscapeLoading: boolean
  landscapeErrorMessage?: string
}>()
</script>

<template>
  <section class="space-y-4">
    <SectionHeader title="天气与环境" subtitle="独立查看天气概览、景观预测与未来预报" />

    <div class="card flex flex-col gap-6 p-6">
      <WeatherSection
        :day="day"
        :is-loading="isWeatherLoading"
        :fallback-city="fallbackCity"
        :error-message="weatherErrorMessage"
      />

      <LandscapePrediction
        :prediction="landscapePrediction"
        :is-loading="isLandscapeLoading"
        :error-message="landscapeErrorMessage"
      />

      <div class="border-t border-white/5 pt-6">
        <div class="mb-4 flex items-center justify-between gap-3">
          <div>
            <h4 class="text-sm font-semibold" style="color: var(--text-primary)">未来七天预报</h4>
            <p class="mt-1 text-xs" style="color: var(--text-tertiary)">
              今日之外的天气趋势，方便安排行程窗口
            </p>
          </div>
        </div>
        <WeatherForecast :forecast="forecast" />
      </div>
    </div>
  </section>
</template>
