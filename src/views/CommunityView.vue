<script setup lang="ts">
import { ref, computed } from 'vue'
import PostCard from '../components/community/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'
import { mockPosts } from '../mock/mockData'

const pageSize = 2
const currentPage = ref(1)
const totalPages = Math.ceil(mockPosts.length / pageSize)

const currentPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return mockPosts.slice(start, end)
})
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
        v-for="post in currentPosts"
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
