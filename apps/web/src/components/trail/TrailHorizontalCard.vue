<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'

const props = defineProps<{
  id: number
  image: string
  name: string
  difficulty: string
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  distance: string
  duration: string
  elevation: string
  description: string
  rating: number
  reviewCount: number
  likes: number
  favorites: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
  isLikePending?: boolean
  isFavoritePending?: boolean
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
            <div class="flex flex-wrap items-center gap-2">
              <TagBadge :label="difficultyLabel" />
              <TagBadge :label="packLabels[packType]" />
              <TagBadge :label="durationLabels[durationType]" />
            </div>
            <h3 class="font-semibold text-base sm:text-lg leading-tight" style="color: var(--text-primary);">
              {{ name }}
            </h3>
          </div>
          <div class="flex items-center gap-1 shrink-0">
            <button
              class="p-1.5 hover:text-primary-500 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="props.isLikePending"
              @click.prevent="$emit('toggle-like')"
              style="color: var(--text-tertiary)"
            >
              <BaseIcon name="Heart" :size="20" :class="props.likedByCurrentUser ? 'text-red-400 fill-red-400' : ''" />
            </button>
            <button
              class="p-1.5 hover:text-primary-500 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="props.isFavoritePending"
              @click.prevent="$emit('toggle-favorite')"
              style="color: var(--text-tertiary)"
            >
              <BaseIcon name="Bookmark" :size="20" :class="props.favoritedByCurrentUser ? 'text-primary-500 fill-primary-500' : ''" />
            </button>
          </div>
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
        <div class="flex items-center justify-between pt-1 gap-4">
          <div class="flex items-center gap-1.5 shrink-0">
            <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
            <span class="text-sm font-semibold text-primary-500">{{ rating }}</span>
            <span class="text-xs" style="color: var(--text-tertiary);">({{ reviewCount >= 1000 ? (reviewCount / 1000).toFixed(1) + 'k' : reviewCount }} 条评论)</span>
          </div>
          <div class="flex items-center gap-3 text-xs sm:text-sm" style="color: var(--text-secondary);">
            <button
              class="flex items-center gap-1 transition-colors hover:text-primary-500 disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="props.isLikePending"
              @click.prevent="$emit('toggle-like')"
            >
              <BaseIcon name="Heart" :size="14" :class="props.likedByCurrentUser ? 'text-red-400 fill-red-400' : ''" />
              <span>{{ likes >= 1000 ? (likes / 1000).toFixed(1) + 'k' : likes }}</span>
            </button>
            <button
              class="flex items-center gap-1 transition-colors hover:text-primary-500 disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="props.isFavoritePending"
              @click.prevent="$emit('toggle-favorite')"
            >
              <BaseIcon name="Bookmark" :size="14" :class="props.favoritedByCurrentUser ? 'text-primary-500 fill-primary-500' : ''" />
              <span>{{ favorites >= 1000 ? (favorites / 1000).toFixed(1) + 'k' : favorites }}</span>
            </button>
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
