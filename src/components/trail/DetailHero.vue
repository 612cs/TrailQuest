<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  image: string
  name: string
  location: string
  difficulty: string
  difficultyLabel: string
  distance: string
  elevation: string
  duration: string
  favorites?: number
  likes?: number
  rating?: number
  reviewCount?: number
}>()
</script>

<template>
  <div class="card overflow-hidden animate-fade-in-up">
    <div class="relative aspect-[16/9] overflow-hidden">
      <img :src="image" :alt="name" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"></div>
      <div class="absolute bottom-4 left-4 right-4">
        <span
          class="px-2.5 py-1 rounded-full text-xs font-semibold"
          :class="{
            'badge-easy': difficulty === 'easy',
            'badge-moderate': difficulty === 'moderate',
            'badge-hard': difficulty === 'hard'
          }"
        >
          {{ difficultyLabel }}
        </span>
        <h1 class="text-xl sm:text-2xl font-bold text-white mt-2">{{ name }}</h1>
        <p class="text-sm text-white/80 flex items-center gap-1 mt-1">
          <BaseIcon name="MapPin" :size="14" />
          {{ location }}
        </p>
      </div>
    </div>
    <!-- Rating & Favorites Row -->
    <div v-if="rating || favorites" class="flex items-center justify-between px-4 py-3 border-b" style="border-color: var(--border-default);">
      <div v-if="rating" class="flex items-center gap-2">
        <div class="flex gap-0.5">
          <BaseIcon
            v-for="i in 5"
            :key="i"
            name="Star"
            :size="14"
            :class="i <= Math.round(rating) ? 'text-yellow-400 fill-current' : 'text-gray-300'"
          />
        </div>
        <span class="text-sm font-bold text-primary-500">{{ rating }}</span>
        <span v-if="reviewCount" class="text-xs" style="color: var(--text-tertiary);">({{ reviewCount >= 1000 ? (reviewCount / 1000).toFixed(1) + 'k' : reviewCount }} 条评论)</span>
      </div>
      <div class="flex items-center gap-3">
        <div v-if="likes" class="flex items-center gap-1 text-sm" style="color: var(--text-secondary);">
          <BaseIcon name="Heart" :size="16" class="text-red-400 fill-red-400" />
          <span class="font-medium">{{ likes >= 10000 ? (likes / 10000).toFixed(1) + 'w' : likes >= 1000 ? (likes / 1000).toFixed(1) + 'k' : likes }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">喜爱</span>
        </div>
        <div v-if="favorites" class="flex items-center gap-1 text-sm" style="color: var(--text-secondary);">
          <BaseIcon name="Bookmark" :size="16" class="text-primary-500 fill-primary-500" />
          <span class="font-medium">{{ favorites >= 10000 ? (favorites / 10000).toFixed(1) + 'w' : favorites >= 1000 ? (favorites / 1000).toFixed(1) + 'k' : favorites }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">收藏</span>
        </div>
      </div>
    </div>
    <!-- Stats Bar -->
    <div class="grid grid-cols-3 divide-x p-4" style="border-color: var(--border-default);">
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ distance }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">距离</p>
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ elevation }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">海拔增益</p>
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ duration }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">预计时间</p>
      </div>
    </div>
  </div>
</template>
