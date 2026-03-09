<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

interface Post {
  id: number
  username: string
  time: string
  avatar: string
  avatarBg: string
  image: string
  tags: string[]
  content: string
  likes: string | number
  comments: string | number
  shares: string | number
}

defineProps<{
  post: Post
}>()
</script>

<template>
  <article class="card overflow-hidden animate-fade-in-up">
    <!-- Post Header -->
    <div class="flex items-center justify-between p-4 pb-3">
      <div class="flex items-center gap-3">
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-bold shrink-0"
          :style="`background-color: ${post.avatarBg}`"
        >
          {{ post.avatar }}
        </div>
        <div>
          <p class="text-sm font-semibold" style="color: var(--text-primary);">{{ post.username }}</p>
          <p class="text-xs" style="color: var(--text-tertiary);">{{ post.time }}</p>
        </div>
      </div>
      <button class="p-1 hover:text-primary-500 transition-colors" style="color: var(--text-tertiary);">
        <BaseIcon name="MoreHorizontal" :size="20" />
      </button>
    </div>

    <!-- Post Image -->
    <div class="relative aspect-[16/10] overflow-hidden">
      <img :src="post.image" :alt="post.content" class="w-full h-full object-cover" />
      <!-- AI Tags -->
      <div class="absolute bottom-3 left-3 flex gap-2">
        <span
          v-for="tag in post.tags"
          :key="tag"
          class="px-2.5 py-1 rounded-lg text-xs font-medium backdrop-blur-md flex items-center gap-1"
          style="background-color: rgba(45, 89, 39, 0.7); color: white;"
        >
          <BaseIcon name="Sparkles" :size="10" />
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- Post Content -->
    <div class="p-4 space-y-3">
      <p class="text-sm leading-relaxed" style="color: var(--text-primary);">
        {{ post.content }}
      </p>

      <!-- Actions -->
      <div class="flex items-center justify-between pt-2 border-t" style="border-color: var(--border-default);">
        <div class="flex items-center gap-5">
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="Heart" :size="16" />
            {{ post.likes }}
          </button>
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="MessageCircle" :size="16" />
            {{ post.comments }}
          </button>
          <button class="flex items-center gap-1.5 text-xs transition-colors hover:text-primary-500" style="color: var(--text-secondary);">
            <BaseIcon name="Share2" :size="16" />
            {{ post.shares }}
          </button>
        </div>
        <button class="hover:text-primary-500 transition-colors" style="color: var(--text-tertiary);">
          <BaseIcon name="Bookmark" :size="18" />
        </button>
      </div>
    </div>
  </article>
</template>
