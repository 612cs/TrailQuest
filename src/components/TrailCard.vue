<script setup lang="ts">
import BaseIcon from './common/BaseIcon.vue'

defineProps<{
  image: string
  name: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  rating: number
  reviews: string
  distance: string
  elevation: string
  duration: string
  id?: number
}>()

const difficultyClass: Record<string, string> = {
  easy: 'badge-easy',
  moderate: 'badge-moderate',
  hard: 'badge-hard',
}
</script>

<template>
  <RouterLink
    :to="`/trail/${id || 1}`"
    class="card card-hover block overflow-hidden min-w-[280px] sm:min-w-[320px] cursor-pointer"
  >
    <!-- Image -->
    <div class="relative aspect-[4/3] overflow-hidden">
      <img :src="image" :alt="name" class="w-full h-full object-cover transition-transform duration-500 hover:scale-105" />
      <!-- Favorite button -->
      <button
        class="absolute top-3 right-3 w-8 h-8 rounded-full flex items-center justify-center backdrop-blur-sm transition-all duration-200 hover:scale-110 active:scale-90"
        style="background-color: rgba(255,255,255,0.85); box-shadow: 0 2px 8px rgba(0,0,0,0.15);"
        @click.prevent
      >
        <BaseIcon name="Heart" :size="16" class="text-surface-500" />
      </button>
      <!-- Difficulty Badge -->
      <div class="absolute bottom-3 left-3">
        <span
          class="px-2.5 py-1 rounded-full text-xs font-semibold shadow-sm"
          :class="difficultyClass[difficulty]"
          style="backdrop-filter: blur(4px);"
        >
          {{ difficultyLabel }}
        </span>
      </div>
    </div>

    <!-- Content -->
    <div class="p-4 space-y-2.5">
      <!-- Title -->
      <h3 class="font-semibold text-base leading-tight line-clamp-1" style="color: var(--text-primary);">
        {{ name }}
      </h3>

      <!-- Rating -->
      <div class="flex items-center gap-1.5">
        <BaseIcon name="Star" :size="14" class="text-primary-500 fill-current" />
        <span class="text-sm font-semibold text-primary-500">{{ rating }}</span>
        <span class="text-xs" style="color: var(--text-tertiary);">{{ reviews }}</span>
      </div>

      <!-- Stats -->
      <div class="flex items-center justify-between text-xs" style="color: var(--text-secondary);">
        <div class="flex items-center gap-1">
          <BaseIcon name="TrendingUp" :size="14" />
          <span>{{ distance }}</span>
        </div>
        <div class="flex items-center gap-1">
          <BaseIcon name="Mountain" :size="14" />
          <span>{{ elevation }}</span>
        </div>
        <div class="flex items-center gap-1">
          <BaseIcon name="Clock" :size="14" />
          <span>{{ duration }}</span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>

