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
}

function toPercent(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) return '--'
  return `${Math.round(value * 100)}%`
}

function toWindowLabel(value?: string | null) {
  if (typeof value !== 'string' || !value.trim()) return '--'
  return value.trim()
}

const cards = computed<LandscapeItem[]>(() => {
  if (!props.prediction) return []

  return [
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
    <div class="flex items-center justify-between mb-4 gap-3">
      <div>
        <h4 class="text-sm font-semibold" style="color: var(--text-primary);">景观预测</h4>
        <p class="text-xs mt-1" style="color: var(--text-tertiary);">
          基于天气、天文与实验性随机森林评分生成
        </p>
      </div>
      <div
        v-if="prediction"
        class="text-[11px] px-2.5 py-1 rounded-full border shrink-0"
        style="color: var(--text-secondary); border-color: color-mix(in srgb, var(--primary-500) 18%, transparent); background: color-mix(in srgb, var(--primary-500) 8%, transparent);"
      >
        {{ prediction.source.provider }}
      </div>
    </div>

    <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div
        v-for="index in 3"
        :key="index"
        class="rounded-2xl border p-5 animate-pulse"
        style="border-color: color-mix(in srgb, var(--primary-500) 14%, transparent); background: color-mix(in srgb, white 4%, transparent);"
      >
        <div class="h-5 w-24 rounded bg-white/10 mb-6"></div>
        <div class="h-10 w-20 rounded bg-white/10 mb-5"></div>
        <div class="grid grid-cols-2 gap-3">
          <div class="h-16 rounded bg-white/10"></div>
          <div class="h-16 rounded bg-white/10"></div>
        </div>
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

    <div v-else-if="cards.length" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <article
        v-for="item in cards"
        :key="item.key"
        class="rounded-2xl border p-5 flex flex-col gap-5 min-h-[220px]"
        style="border-color: color-mix(in srgb, var(--primary-500) 14%, transparent); background: color-mix(in srgb, white 4%, transparent);"
      >
        <div class="flex items-center justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div :class="['p-2 rounded-xl shrink-0', item.accentBg, item.accent]">
              <BaseIcon :name="item.icon" :size="18" />
            </div>
            <p class="text-base font-semibold truncate" style="color: var(--text-primary);">{{ item.name }}</p>
          </div>
          <span
            v-if="item.experimental"
            class="text-[11px] px-2 py-0.5 rounded-full shrink-0"
            style="background: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--text-secondary);"
          >
            实验中
          </span>
        </div>

        <div>
          <p class="text-xs mb-1.5" style="color: var(--text-tertiary);">概率</p>
          <p class="text-4xl font-bold tracking-tight leading-none" style="color: var(--text-primary);">{{ toPercent(item.score) }}</p>
        </div>

        <div class="grid grid-cols-2 gap-3 mt-auto">
          <div class="rounded-xl px-3 py-3" style="background: color-mix(in srgb, var(--primary-500) 6%, transparent);">
            <p class="text-[11px]" style="color: var(--text-tertiary);">置信度</p>
            <p class="text-base font-semibold mt-1" style="color: var(--text-primary);">{{ toPercent(item.confidence) }}</p>
          </div>
          <div class="rounded-xl px-3 py-3" style="background: color-mix(in srgb, var(--primary-500) 6%, transparent);">
            <p class="text-[11px]" style="color: var(--text-tertiary);">时间</p>
            <p class="text-base font-semibold mt-1 truncate" style="color: var(--text-primary);">{{ toWindowLabel(item.bestWindow) }}</p>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>
