<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useRoute } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'
import type { EntityId } from '../../types/id'

const route = useRoute()

const props = defineProps<{
  id: EntityId
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

const detailLink = computed(() => ({
  name: 'TrailDetail',
  params: { id: props.id },
  query: { from: route.fullPath },
}))
</script>

<template>
  <RouterLink :to="detailLink" class="card card-hover animate-fade-in-up block overflow-hidden">
    <div class="flex flex-col sm:flex-row">
      <!-- Image -->
      <div class="aspect-video shrink-0 overflow-hidden sm:aspect-auto sm:w-48 lg:w-56">
        <img
          :src="image"
          :alt="name"
          class="h-full w-full object-cover transition-transform duration-500 hover:scale-105"
        />
      </div>

      <!-- Content -->
      <div class="flex flex-1 flex-col justify-between space-y-3 p-4 sm:p-5">
        <!-- Title & Badge -->
        <div class="flex items-start justify-between gap-3">
          <div class="space-y-1.5">
            <div class="flex flex-wrap items-center gap-2">
              <TagBadge :label="difficultyLabel" />
              <TagBadge :label="packLabels[packType]" />
              <TagBadge :label="durationLabels[durationType]" />
            </div>
            <h3
              class="text-base leading-tight font-semibold sm:text-lg"
              style="color: var(--text-primary)"
            >
              {{ name }}
            </h3>
          </div>
          <div class="flex shrink-0 items-center gap-1">
            <button
              class="group rounded-md p-1.5 transition-colors focus-visible:ring-2 focus-visible:ring-red-300/60 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="props.isLikePending"
              @click.prevent="$emit('toggle-like')"
              style="color: var(--text-tertiary)"
            >
              <BaseIcon
                name="Heart"
                :size="20"
                :class="
                  props.likedByCurrentUser
                    ? 'fill-red-400 text-red-400'
                    : 'group-hover:text-red-400'
                "
              />
            </button>
            <button
              class="group focus-visible:ring-primary-300/60 rounded-md p-1.5 transition-colors focus-visible:ring-2 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="props.isFavoritePending"
              @click.prevent="$emit('toggle-favorite')"
              style="color: var(--text-tertiary)"
            >
              <BaseIcon
                name="Bookmark"
                :size="20"
                :class="
                  props.favoritedByCurrentUser
                    ? 'text-primary-500 fill-primary-500'
                    : 'group-hover:text-primary-500'
                "
              />
            </button>
          </div>
        </div>

        <!-- Stats -->
        <div class="flex items-center gap-4 text-xs" style="color: var(--text-secondary)">
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
        <p class="line-clamp-2 text-sm leading-relaxed" style="color: var(--text-secondary)">
          {{ description }}
        </p>

        <!-- Footer -->
        <div class="flex items-center justify-between gap-4 pt-1">
          <div class="flex shrink-0 items-center gap-1.5">
            <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
            <span class="text-primary-500 text-sm font-semibold">{{ rating }}</span>
            <span class="text-xs" style="color: var(--text-tertiary)"
              >({{
                reviewCount >= 1000 ? (reviewCount / 1000).toFixed(1) + 'k' : reviewCount
              }}
              条评论)</span
            >
          </div>
          <div
            class="flex items-center gap-3 text-xs sm:text-sm"
            style="color: var(--text-secondary)"
          >
            <button
              class="group flex items-center gap-1 rounded-md transition-colors focus-visible:ring-2 focus-visible:ring-red-300/60 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="props.isLikePending"
              @click.prevent="$emit('toggle-like')"
            >
              <BaseIcon
                name="Heart"
                :size="14"
                :class="
                  props.likedByCurrentUser
                    ? 'fill-red-400 text-red-400'
                    : 'group-hover:text-red-400'
                "
              />
              <span class="group-hover:text-red-400">{{
                likes >= 1000 ? (likes / 1000).toFixed(1) + 'k' : likes
              }}</span>
            </button>
            <button
              class="group focus-visible:ring-primary-300/60 flex items-center gap-1 rounded-md transition-colors focus-visible:ring-2 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="props.isFavoritePending"
              @click.prevent="$emit('toggle-favorite')"
            >
              <BaseIcon
                name="Bookmark"
                :size="14"
                :class="
                  props.favoritedByCurrentUser
                    ? 'text-primary-500 fill-primary-500'
                    : 'group-hover:text-primary-500'
                "
              />
              <span class="group-hover:text-primary-500">{{
                favorites >= 1000 ? (favorites / 1000).toFixed(1) + 'k' : favorites
              }}</span>
            </button>
          </div>
          <span class="text-primary-500 flex items-center gap-1 text-sm font-medium">
            查看详情
            <BaseIcon name="ChevronRight" :size="16" />
          </span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>
