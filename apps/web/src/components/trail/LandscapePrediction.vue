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
  glow: string
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
      glow: 'bg-primary-500',
      ...props.prediction.cloudSea,
    },
    {
      key: 'rime',
      name: '雾凇',
      icon: 'CloudSnow',
      accent: 'text-cyan-300',
      accentBg: 'bg-cyan-500/10',
      glow: 'bg-cyan-500',
      ...props.prediction.rime,
    },
    {
      key: 'icicle',
      name: '冰挂',
      icon: 'Snowflake',
      accent: 'text-blue-300',
      accentBg: 'bg-blue-500/10',
      glow: 'bg-blue-500',
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
        class="group relative flex flex-col p-4 rounded-2xl border overflow-hidden transition-all hover:bg-white/[0.02]"
        style="border-color: color-mix(in srgb, var(--primary-500) 12%, transparent); background: color-mix(in srgb, var(--primary-500) 2%, transparent);"
      >
        <!-- Top row: Icon + Name + Percentage -->
        <div class="flex items-center justify-between relative z-10 mb-2 gap-2">
          <div class="flex items-center gap-2.5 min-w-0">
            <div :class="['p-2 rounded-xl shrink-0 flex items-center justify-center', item.accentBg, item.accent]">
              <BaseIcon :name="item.icon" :size="20" />
            </div>
            <p class="text-base font-semibold whitespace-nowrap" style="color: var(--text-primary);">{{ item.name }}</p>
          </div>
          <span class="text-2xl font-bold tracking-tighter shrink-0" :class="item.accent">{{ toPercent(item.score) }}</span>
        </div>

        <!-- Bottom row: Details -->
        <div class="flex flex-col gap-2 mt-auto relative z-10 pt-2">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs uppercase tracking-wider shrink-0" style="color: var(--text-tertiary);">置信度</span>
            <span class="text-sm font-medium" style="color: var(--text-secondary);">{{ toPercent(item.confidence) }}</span>
          </div>
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs uppercase tracking-wider shrink-0" style="color: var(--text-tertiary);">时间</span>
            <span class="text-[13px] font-medium whitespace-nowrap tracking-tight" style="color: var(--text-secondary);">{{ toWindowLabel(item.bestWindow) }}</span>
          </div>
        </div>

        <!-- Decorative background elements -->
        <div :class="['absolute -right-4 -top-4 w-32 h-32 blur-3xl opacity-[0.15] mix-blend-screen pointer-events-none transition-opacity duration-300 group-hover:opacity-30 rounded-full', item.glow]"></div>
      </article>
    </div>
  </section>
</template>
