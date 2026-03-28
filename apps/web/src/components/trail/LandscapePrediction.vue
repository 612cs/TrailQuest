<script setup lang="ts">
import { computed } from 'vue'

import BaseIcon from '../common/BaseIcon.vue'
import type {
  LandscapePredictionCard,
  TrailLandscapePredictionResponse,
} from '../../types/landscape'

const props = defineProps<{
  prediction: TrailLandscapePredictionResponse | null
  isLoading: boolean
  errorMessage?: string
}>()

type LandscapeItem = LandscapePredictionCard & {
  key: string
  name: string
  icon: string
  accent: string
  accentBg: string
  meta?: string
}

function toPercent(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) return '--'
  return `${Math.round(value * 100)}%`
}

function toLevelLabel(level?: string | null) {
  switch (level) {
    case 'excellent':
      return '极佳'
    case 'good':
      return '良好'
    case 'medium':
      return '一般'
    case 'poor':
      return '较低'
    default:
      return '待评估'
  }
}

const cards = computed<LandscapeItem[]>(() => {
  if (!props.prediction) return []

  return [
    {
      key: 'sunriseSunset',
      name: '日出日落',
      icon: 'Sunrise',
      accent: 'text-orange-400',
      accentBg: 'bg-orange-500/10',
      meta: `${props.prediction.sunriseSunset.sunriseTime ?? '--'} / ${props.prediction.sunriseSunset.sunsetTime ?? '--'}`,
      ...props.prediction.sunriseSunset,
    },
    {
      key: 'milkyWay',
      name: '银河',
      icon: 'Stars',
      accent: 'text-sky-300',
      accentBg: 'bg-sky-500/10',
      meta: props.prediction.source.lightPollutionReady ? '已校准光污染' : '光污染保守估计',
      ...props.prediction.milkyWay,
    },
    {
      key: 'cloudSea',
      name: '云海',
      icon: 'Cloud',
      accent: 'text-primary-400',
      accentBg: 'bg-primary-500/10',
      ...props.prediction.cloudSea,
    },
    {
      key: 'rime',
      name: '雾凇',
      icon: 'CloudSnow',
      accent: 'text-cyan-300',
      accentBg: 'bg-cyan-500/10',
      ...props.prediction.rime,
    },
    {
      key: 'icicle',
      name: '冰挂',
      icon: 'Snowflake',
      accent: 'text-blue-300',
      accentBg: 'bg-blue-500/10',
      ...props.prediction.icicle,
    },
  ]
})
</script>

<template>
  <section class="animate-fade-in-up stagger-3 mt-4">
    <div class="flex items-center justify-between mb-4">
      <div>
        <h4 class="text-sm font-semibold" style="color: var(--text-primary);">景观预测</h4>
        <p class="text-xs mt-1" style="color: var(--text-tertiary);">
          基于天气、天文与实验性随机森林评分生成
        </p>
      </div>
      <div
        v-if="prediction"
        class="text-[11px] px-2.5 py-1 rounded-full border"
        style="color: var(--text-secondary); border-color: color-mix(in srgb, var(--primary-500) 18%, transparent); background: color-mix(in srgb, var(--primary-500) 8%, transparent);"
      >
        {{ prediction.source.provider }}
      </div>
    </div>

    <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-4">
      <div
        v-for="index in 5"
        :key="index"
        class="rounded-2xl border p-4 animate-pulse"
        style="border-color: color-mix(in srgb, var(--primary-500) 14%, transparent); background: color-mix(in srgb, white 4%, transparent);"
      >
        <div class="h-5 w-20 rounded bg-white/10 mb-4"></div>
        <div class="h-8 w-16 rounded bg-white/10 mb-3"></div>
        <div class="h-4 w-full rounded bg-white/10 mb-2"></div>
        <div class="h-4 w-3/4 rounded bg-white/10"></div>
      </div>
    </div>

    <div
      v-else-if="errorMessage"
      class="rounded-2xl border p-4 flex items-start gap-3"
      style="border-color: color-mix(in srgb, var(--color-hard) 35%, transparent); background: color-mix(in srgb, var(--color-hard) 8%, transparent);"
    >
      <BaseIcon name="TriangleAlert" :size="18" class="mt-0.5 text-red-400" />
      <div>
        <p class="text-sm font-medium" style="color: var(--text-primary);">景观预测暂时不可用</p>
        <p class="text-xs mt-1" style="color: var(--text-secondary);">{{ errorMessage }}</p>
      </div>
    </div>

    <div v-else-if="cards.length" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-4">
      <article
        v-for="item in cards"
        :key="item.key"
        class="rounded-2xl border p-4 flex flex-col gap-3 min-h-[220px]"
        style="border-color: color-mix(in srgb, var(--primary-500) 14%, transparent); background: color-mix(in srgb, white 4%, transparent);"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-center gap-2">
            <div :class="['p-2 rounded-xl', item.accentBg, item.accent]">
              <BaseIcon :name="item.icon" :size="18" />
            </div>
            <div>
              <p class="text-sm font-semibold" style="color: var(--text-primary);">{{ item.name }}</p>
              <p class="text-[11px]" style="color: var(--text-tertiary);">{{ item.meta || item.bestWindow || '待计算' }}</p>
            </div>
          </div>
          <span
            class="text-[11px] px-2 py-0.5 rounded-full border"
            style="color: var(--text-secondary); border-color: color-mix(in srgb, var(--primary-500) 18%, transparent);"
          >
            {{ toLevelLabel(item.level) }}
          </span>
        </div>

        <div class="flex items-end justify-between gap-3">
          <div>
            <p class="text-2xl font-bold tracking-tight" style="color: var(--text-primary);">{{ toPercent(item.score) }}</p>
            <p class="text-xs mt-1" style="color: var(--text-tertiary);">置信度 {{ toPercent(item.confidence) }}</p>
          </div>
          <span
            v-if="item.experimental"
            class="text-[11px] px-2 py-0.5 rounded-full"
            style="background: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--text-secondary);"
          >
            实验中
          </span>
        </div>

        <p class="text-xs leading-6 flex-1" style="color: var(--text-secondary);">
          {{ item.summary || '当前没有足够数据生成解释。' }}
        </p>

        <div class="space-y-2">
          <p class="text-[11px] font-medium" style="color: var(--text-tertiary);">风险提示</p>
          <ul v-if="item.risks?.length" class="space-y-1.5">
            <li
              v-for="risk in item.risks.slice(0, 2)"
              :key="risk"
              class="text-[11px] leading-5 flex items-start gap-1.5"
              style="color: var(--text-secondary);"
            >
              <BaseIcon name="Dot" :size="14" class="mt-0.5 shrink-0" />
              <span>{{ risk }}</span>
            </li>
          </ul>
          <p v-else class="text-[11px]" style="color: var(--text-secondary);">暂无明显风险提示</p>
        </div>
      </article>
    </div>
  </section>
</template>
