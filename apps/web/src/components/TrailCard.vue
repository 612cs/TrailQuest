<script setup lang="ts">
import BaseIcon from './common/BaseIcon.vue'
import TagBadge from './common/TagBadge.vue'
import type { EntityId } from '../types/id'

const props = defineProps<{
  image: string
  name: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  rating: number
  reviews: string
  distance: string
  elevation: string
  duration: string
  likes: number
  favorites: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
  isLikePending?: boolean
  isFavoritePending?: boolean
  id?: EntityId
}>()

defineEmits<{
  (event: 'toggle-like'): void
  (event: 'toggle-favorite'): void
}>()

const packLabels: Record<'light' | 'heavy' | 'both', string> = {
  light: '轻装',
  heavy: '重装',
  both: '轻重皆可',
}

const durationLabels: Record<'single_day' | 'multi_day', string> = {
  single_day: '单日',
  multi_day: '多日',
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
      <div class="absolute top-3 right-3 flex items-center gap-2">
        <button
          class="w-8 h-8 rounded-full flex items-center justify-center backdrop-blur-sm transition-all duration-200 hover:scale-110 active:scale-90 disabled:opacity-60 disabled:cursor-not-allowed"
          style="background-color: rgba(255,255,255,0.88); box-shadow: 0 2px 8px rgba(0,0,0,0.15);"
          :disabled="props.isLikePending"
          @click.prevent="$emit('toggle-like')"
        >
          <BaseIcon
            name="Heart"
            :size="16"
            :class="props.likedByCurrentUser ? 'text-red-400 fill-red-400' : 'text-surface-500'"
          />
        </button>
        <button
          class="w-8 h-8 rounded-full flex items-center justify-center backdrop-blur-sm transition-all duration-200 hover:scale-110 active:scale-90 disabled:opacity-60 disabled:cursor-not-allowed"
          style="background-color: rgba(255,255,255,0.88); box-shadow: 0 2px 8px rgba(0,0,0,0.15);"
          :disabled="props.isFavoritePending"
          @click.prevent="$emit('toggle-favorite')"
        >
          <BaseIcon
            name="Bookmark"
            :size="16"
            :class="props.favoritedByCurrentUser ? 'text-primary-500 fill-primary-500' : 'text-surface-500'"
          />
        </button>
      </div>
      <!-- Difficulty Badge -->
      <div class="absolute bottom-3 left-3 flex flex-wrap gap-2">
        <TagBadge :label="difficultyLabel" />
        <TagBadge :label="packLabels[packType]" />
        <TagBadge :label="durationLabels[durationType]" />
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

      <div class="flex items-center gap-4 text-xs" style="color: var(--text-secondary);">
        <button
          class="flex items-center gap-1.5 transition-colors hover:text-primary-500 disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="props.isLikePending"
          @click.prevent="$emit('toggle-like')"
        >
          <BaseIcon name="Heart" :size="14" :class="props.likedByCurrentUser ? 'text-red-400 fill-red-400' : ''" />
          <span>{{ likes >= 1000 ? (likes / 1000).toFixed(1) + 'k' : likes }}</span>
        </button>
        <button
          class="flex items-center gap-1.5 transition-colors hover:text-primary-500 disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="props.isFavoritePending"
          @click.prevent="$emit('toggle-favorite')"
        >
          <BaseIcon name="Bookmark" :size="14" :class="props.favoritedByCurrentUser ? 'text-primary-500 fill-primary-500' : ''" />
          <span>{{ favorites >= 1000 ? (favorites / 1000).toFixed(1) + 'k' : favorites }}</span>
        </button>
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
