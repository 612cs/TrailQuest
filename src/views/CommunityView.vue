<script setup lang="ts">
import { ref } from 'vue'
import PostCard from '../components/community/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const currentPage = ref(1)
const totalPages = 12

const posts = [
  {
    id: 1,
    username: '@mountain_jess',
    time: '5 小时前 • 银溪',
    avatar: 'MJ',
    avatarBg: '#4f9a48',
    image: '/trail-lake.png',
    tags: ['AI: 湖泊', 'AI: 水域'],
    content: '今天的水质清澈得令人难以置信。在远离主干道的地方发现了一个隐秘的角落。非常适合下午冥想。',
    likes: '1.2k',
    comments: '48',
    shares: '12',
  },
  {
    id: 2,
    username: '@river_wanderer',
    time: '5 小时前 • 银溪',
    avatar: 'RW',
    avatarBg: '#2563eb',
    image: '/trail-foggy.png',
    tags: ['AI: 森林', 'AI: 雾景'],
    content: '清晨的薄雾中，阳光透过树叶间隙洒落在小径上。这种氛围让人仿佛置身于仙境之中。',
    likes: '856',
    comments: '22',
    shares: '5',
  },
]
</script>

<template>
  <main class="max-w-3xl mx-auto px-4 sm:px-6 py-8 sm:py-12">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-2xl sm:text-3xl font-bold" style="color: var(--text-primary);">社区动态</h1>
      <p class="text-sm mt-2" style="color: var(--text-secondary);">来自像你一样的探险者的实时自然发现。</p>
    </div>

    <!-- Posts -->
    <div class="space-y-5">
      <PostCard
        v-for="post in posts"
        :key="post.id"
        :post="post"
      />
    </div>

    <!-- Pagination -->
    <div class="mt-8">
      <Pagination
        v-model:current="currentPage"
        :total="totalPages"
        :max-visible="3"
      />
    </div>
  </main>
</template>
