<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'
import { useUserStore } from '../../stores/useUserStore'
import type { EntityId } from '../../types/id'
import type { TrailAuthor } from '../../types/trail'

const userStore = useUserStore()

const props = defineProps<{
  id: EntityId
  image: string
  galleryCount?: number
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
  likedByCurrentUser?: boolean
  favoritedByCurrentUser?: boolean
  isLikePending?: boolean
  isFavoritePending?: boolean
  rating?: number
  reviewCount?: number
  author: TrailAuthor
  publishTime: string
}>()

defineEmits<{
  (event: 'toggle-like'): void
  (event: 'toggle-favorite'): void
  (event: 'share'): void
  (event: 'open-gallery'): void
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
  <div class="card animate-fade-in-up overflow-hidden">
    <div class="relative aspect-[16/9] overflow-hidden">
      <img :src="image" :alt="name" class="h-full w-full object-cover" />
      <div
        class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"
      ></div>
      <button
        v-if="(galleryCount ?? 0) > 0"
        type="button"
        class="absolute right-4 bottom-4 z-[1] flex items-center gap-2 rounded-full border border-white/20 bg-black/35 px-3 py-2 text-sm font-medium text-white backdrop-blur-md transition hover:bg-black/50"
        @click="$emit('open-gallery')"
      >
        <BaseIcon name="Play" :size="16" />
        图片漫游
      </button>
      <div class="absolute right-4 bottom-4 left-4 hidden sm:block">
        <div class="flex flex-wrap items-center gap-2">
          <TagBadge :label="difficultyLabel" />
          <TagBadge :label="packLabels[packType]" />
          <TagBadge :label="durationLabels[durationType]" />
        </div>
        <h1 class="mt-2 text-xl font-bold text-white sm:text-2xl">{{ name }}</h1>
        <p class="mt-1 flex items-center gap-1 text-sm text-white/80">
          <BaseIcon name="MapPin" :size="14" />
          {{ location }}
        </p>
      </div>
    </div>
    <!-- Author Info Row -->
    <div
      class="flex items-center justify-between border-b px-4 py-3"
      style="border-color: var(--border-default); background-color: var(--bg-card)"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold text-white"
          :style="`background-color: ${author.avatarBg}`"
        >
          {{ author.avatar }}
        </div>
        <div>
          <p class="text-sm font-semibold" style="color: var(--text-primary)">
            {{ author.username }}
          </p>
          <p class="text-xs" style="color: var(--text-tertiary)">发布于 {{ publishTime }}</p>
        </div>
      </div>
      <button
        @click="userStore.requireAuth(() => {})"
        class="bg-primary-50 text-primary-600 hover:bg-primary-100 rounded-full px-3 py-1 text-xs font-medium transition-colors"
      >
        关注
      </button>
    </div>
    <div
      class="space-y-3 border-b px-4 py-4 sm:hidden"
      style="border-color: var(--border-default); background-color: var(--bg-card)"
    >
      <div class="flex flex-wrap items-center gap-2">
        <TagBadge :label="difficultyLabel" />
        <TagBadge :label="packLabels[packType]" />
        <TagBadge :label="durationLabels[durationType]" />
      </div>
      <div class="flex items-center justify-between gap-3">
        <h1
          class="min-w-0 truncate text-xl leading-tight font-bold"
          style="color: var(--text-primary)"
        >
          {{ name }}
        </h1>
        <p
          class="flex shrink-0 items-center gap-1 text-sm whitespace-nowrap"
          style="color: var(--text-secondary)"
        >
          <BaseIcon name="MapPin" :size="14" />
          {{ location }}
        </p>
      </div>
    </div>
    <!-- Rating & Favorites Row -->
    <div
      v-if="rating || favorites"
      class="flex items-center justify-between border-b px-4 py-3"
      style="border-color: var(--border-default)"
    >
      <div v-if="rating" class="flex items-center gap-2">
        <div class="flex gap-0.5">
          <BaseIcon
            v-for="i in 5"
            :key="i"
            name="Star"
            :size="14"
            :class="i <= Math.round(rating) ? 'fill-current text-yellow-400' : 'text-gray-300'"
          />
        </div>
        <span class="text-primary-500 text-sm font-bold">{{ rating }}</span>
        <span v-if="reviewCount" class="text-xs" style="color: var(--text-tertiary)"
          >({{
            reviewCount >= 1000 ? (reviewCount / 1000).toFixed(1) + 'k' : reviewCount
          }}
          条评论)</span
        >
      </div>
      <div class="flex flex-wrap items-center justify-end gap-3">
        <!-- <button
          class="flex items-center gap-1 text-sm transition-opacity hover:opacity-80 disabled:opacity-60 disabled:cursor-not-allowed"
          @click="$emit('share')"
          style="color: var(--text-secondary);"
        >
          <BaseIcon name="Share2" :size="16" />
          <span class="font-medium">分享</span>
        </button> -->
        <button
          v-if="likes !== undefined"
          class="group flex items-center gap-1 rounded-md text-sm transition-colors focus-visible:ring-2 focus-visible:ring-red-300/60 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="props.isLikePending"
          @click="$emit('toggle-like')"
          style="color: var(--text-secondary)"
        >
          <BaseIcon
            name="Heart"
            :size="16"
            :class="
              props.likedByCurrentUser ? 'fill-red-400 text-red-400' : 'group-hover:text-red-400'
            "
          />
          <span class="font-medium group-hover:text-red-400">{{
            likes >= 10000
              ? (likes / 10000).toFixed(1) + 'w'
              : likes >= 1000
                ? (likes / 1000).toFixed(1) + 'k'
                : likes
          }}</span>
          <span class="text-xs group-hover:text-red-400/80" style="color: var(--text-tertiary)"
            >喜爱</span
          >
        </button>
        <button
          v-if="favorites !== undefined"
          class="group focus-visible:ring-primary-300/60 flex items-center gap-1 rounded-md text-sm transition-colors focus-visible:ring-2 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="props.isFavoritePending"
          @click="$emit('toggle-favorite')"
          style="color: var(--text-secondary)"
        >
          <BaseIcon
            name="Bookmark"
            :size="16"
            :class="
              props.favoritedByCurrentUser
                ? 'text-primary-500 fill-primary-500'
                : 'group-hover:text-primary-500'
            "
          />
          <span class="group-hover:text-primary-500 font-medium">{{
            favorites >= 10000
              ? (favorites / 10000).toFixed(1) + 'w'
              : favorites >= 1000
                ? (favorites / 1000).toFixed(1) + 'k'
                : favorites
          }}</span>
          <span class="group-hover:text-primary-500/80 text-xs" style="color: var(--text-tertiary)"
            >收藏</span
          >
        </button>
      </div>
    </div>
    <!-- Stats Bar -->
    <div class="grid grid-cols-3 divide-x p-4" style="border-color: var(--border-default)">
      <div class="text-center">
        <p class="text-primary-500 text-sm font-bold">{{ distance }}</p>
        <BaseIcon name="TrendingUp" :size="14" />
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">距离</p> -->
      </div>
      <div class="text-center">
        <p class="text-primary-500 text-sm font-bold">{{ elevation }}</p>
        <BaseIcon name="Mountain" :size="14" />
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">海拔增益</p> -->
      </div>
      <div class="text-center">
        <p class="text-primary-500 text-sm font-bold">{{ duration }}</p>
        <BaseIcon name="Clock" :size="14" />
        <!-- <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">预计时间</p> -->
      </div>
    </div>
  </div>
</template>
