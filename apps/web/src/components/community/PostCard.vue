<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'
import type { TrailWithAuthor } from '../../mock/mockData'

defineProps<{
  post: TrailWithAuthor
  isInitialLoad?: boolean
}>()
</script>

<template>
  <article 
    class="card overflow-hidden" 
    :class="isInitialLoad !== false ? 'animate-fade-in-up' : ''"
  >
    <!-- Post Header -->
    <div class="flex items-center justify-between p-4 pb-3">
      <div class="flex items-center gap-3">
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-bold shrink-0"
          :style="`background-color: ${post.author.avatarBg}`"
        >
          {{ post.author.avatar }}
        </div>
        <div>
          <p class="text-sm font-semibold" style="color: var(--text-primary);">{{ post.author.username }}</p>
          <p class="text-xs" style="color: var(--text-tertiary);">{{ post.publishTime }}</p>
        </div>
      </div>
      <button class="p-1 hover:text-primary-500 transition-colors" style="color: var(--text-tertiary);">
        <BaseIcon name="MoreHorizontal" :size="20" />
      </button>
    </div>

    <!-- Post Image with Link to Trail -->
    <RouterLink :to="`/trail/${post.id}`" class="block relative aspect-[16/10] overflow-hidden group cursor-pointer">
      <img :src="post.image" :alt="post.description" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
      <!-- AI Tags -->
      <div class="absolute bottom-3 left-3 flex gap-2">
        <TagBadge
          v-for="tag in post.tags"
          :key="tag"
          :label="tag"
          icon="Sparkles"
        />
      </div>
    </RouterLink>

    <!-- Post Content -->
    <div class="p-4 space-y-3">
      <p class="text-sm leading-relaxed" style="color: var(--text-primary);">
        {{ post.description }}
      </p>

      <!-- Actions -->
      <div class="flex items-center justify-between pt-2 border-t" style="border-color: var(--border-default);">
        <div class="flex items-center gap-5">
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="Heart" :size="16" />
            {{ post.likes >= 1000 ? (post.likes / 1000).toFixed(1) + 'k' : post.likes }}
          </button>
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="MessageCircle" :size="16" />
            {{ post.reviewCount >= 1000 ? (post.reviewCount / 1000).toFixed(1) + 'k' : post.reviewCount }}
          </button>
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="Share2" :size="16" />
            <span v-if="post.favorites < 1000">{{ post.favorites }}</span>
            <span v-else>{{ (post.favorites / 1000).toFixed(1) }}k</span>
          </button>
        </div>
        <button class="hover:text-primary-500 transition-colors" style="color: var(--text-tertiary);">
          <BaseIcon name="Bookmark" :size="18" />
        </button>
      </div>
    </div>
  </article>
</template>
