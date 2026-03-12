<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'
import { useUserStore } from '../../stores/useUserStore'

import type { User } from '../../mock/mockData'

const userStore = useUserStore()

const props = defineProps<{
  image: string
  name: string
  location: string
  difficulty: string
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  distance: string
  elevation: string
  duration: string
  favorites?: number
  likes?: number
  rating?: number
  reviewCount?: number
  author: User
  publishTime: string
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
  <div class="card overflow-hidden animate-fade-in-up">
    <div class="relative aspect-[16/9] overflow-hidden">
      <img :src="image" :alt="name" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"></div>
      <div class="absolute bottom-4 left-4 right-4">
        <div class="flex flex-wrap items-center gap-2">
          <TagBadge :label="difficultyLabel" />
          <TagBadge :label="packLabels[packType]" />
          <TagBadge :label="durationLabels[durationType]" />
        </div>
        <h1 class="text-xl sm:text-2xl font-bold text-white mt-2">{{ name }}</h1>
        <p class="text-sm text-white/80 flex items-center gap-1 mt-1">
          <BaseIcon name="MapPin" :size="14" />
          {{ location }}
        </p>
      </div>
    </div>
    <!-- Author Info Row -->
    <div class="flex items-center justify-between px-4 py-3 border-b" style="border-color: var(--border-default); background-color: var(--bg-card);">
      <div class="flex items-center gap-3">
        <div
          class="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold"
          :style="`background-color: ${author.avatarBg}`"
        >
          {{ author.avatar }}
        </div>
        <div>
          <p class="text-sm font-semibold" style="color: var(--text-primary);">{{ author.username }}</p>
          <p class="text-xs" style="color: var(--text-tertiary);">发布于 {{ publishTime }}</p>
        </div>
      </div>
      <button 
        @click="userStore.requireAuth(() => {})"
        class="px-3 py-1 rounded-full text-xs font-medium bg-primary-50 text-primary-600 hover:bg-primary-100 transition-colors"
      >
        关注
      </button>
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
        <button v-if="likes !== undefined" @click="userStore.requireAuth(() => {})" class="flex items-center gap-1 text-sm transition-opacity hover:opacity-80" style="color: var(--text-secondary);">
          <BaseIcon name="Heart" :size="16" class="text-red-400 fill-red-400" />
          <span class="font-medium">{{ likes >= 10000 ? (likes / 10000).toFixed(1) + 'w' : likes >= 1000 ? (likes / 1000).toFixed(1) + 'k' : likes }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">喜爱</span>
        </button>
        <button v-if="favorites !== undefined" @click="userStore.requireAuth(() => {})" class="flex items-center gap-1 text-sm transition-opacity hover:opacity-80" style="color: var(--text-secondary);">
          <BaseIcon name="Bookmark" :size="16" class="text-primary-500 fill-primary-500" />
          <span class="font-medium">{{ favorites >= 10000 ? (favorites / 10000).toFixed(1) + 'w' : favorites >= 1000 ? (favorites / 1000).toFixed(1) + 'k' : favorites }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">收藏</span>
        </button>
      </div>
    </div>
    <!-- Stats Bar -->
    <div class="grid grid-cols-3 divide-x p-4" style="border-color: var(--border-default);">
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ distance }}</p>
        <BaseIcon name="TrendingUp" :size="14" /> 
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">距离</p> -->
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ elevation }}</p>
         <BaseIcon name="Mountain" :size="14" />
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">海拔增益</p> -->
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ duration }}</p>
         <BaseIcon name="Clock" :size="14" />
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">预计时间</p> -->
      </div>
    </div>
  </div>
</template>
