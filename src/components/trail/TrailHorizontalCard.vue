<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  id: number
  image: string
  name: string
  difficulty: string
  difficultyLabel: string
  distance: string
  duration: string
  elevation: string
  description: string
  rating: number
  reviews: string
}>()

const difficultyColors: Record<string, string> = {
  easy: 'badge-easy',
  moderate: 'badge-moderate',
  hard: 'badge-hard',
}
</script>

<template>
  <RouterLink
    :to="`/trail/${id}`"
    class="card card-hover block overflow-hidden animate-fade-in-up"
  >
    <div class="flex flex-col sm:flex-row">
      <!-- Image -->
      <div class="sm:w-48 lg:w-56 shrink-0 aspect-video sm:aspect-auto overflow-hidden">
        <img :src="image" :alt="name" class="w-full h-full object-cover transition-transform duration-500 hover:scale-105" />
      </div>

      <!-- Content -->
      <div class="flex-1 p-4 sm:p-5 space-y-3">
        <!-- Title & Badge -->
        <div class="flex items-start justify-between gap-3">
          <div class="space-y-1.5">
            <span class="inline-block px-2 py-0.5 rounded text-xs font-semibold" :class="difficultyColors[difficulty]">
              {{ difficultyLabel }}
            </span>
            <h3 class="font-semibold text-base sm:text-lg leading-tight" style="color: var(--text-primary);">
              {{ name }}
            </h3>
          </div>
          <button class="p-1.5 shrink-0 hover:text-primary-500 transition-colors" @click.prevent style="color: var(--text-tertiary)">
            <BaseIcon name="Bookmark" :size="20" />
          </button>
        </div>

        <!-- Stats -->
        <div class="flex items-center gap-4 text-xs" style="color: var(--text-secondary);">
          <div class="flex items-center gap-1">
            <BaseIcon name="TrendingUp" :size="14" />
            {{ distance }}
          </div>
          <div class="flex items-center gap-1">
            <BaseIcon name="Clock" :size="14" />
            {{ duration }}
          </div>
          <div class="flex items-center gap-1">
            <BaseIcon name="Mountain" :size="14" />
            {{ elevation }}
          </div>
        </div>

        <!-- Description -->
        <p class="text-sm leading-relaxed line-clamp-2" style="color: var(--text-secondary);">
          {{ description }}
        </p>

        <!-- Footer -->
        <div class="flex items-center justify-between pt-1">
          <div class="flex items-center gap-1.5">
            <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
            <span class="text-sm font-semibold text-primary-500">{{ rating }}</span>
            <span class="text-xs" style="color: var(--text-tertiary);">{{ reviews }}</span>
          </div>
          <span class="flex items-center gap-1 text-sm font-medium text-primary-500">
            查看详情
            <BaseIcon name="ChevronRight" :size="16" />
          </span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>
