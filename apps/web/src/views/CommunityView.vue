<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PostCard from '../components/community/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'
import { fetchTrails } from '../api/trails'
import { useTrailShare } from '../composables/useTrailShare'
import { useTrailFeedRefreshStore } from '../stores/useTrailFeedRefreshStore'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import type { TrailListItem } from '../types/trail'
import { toCommunityPost } from '../utils/trailAdapters'

const pageSize = 2
const currentPage = ref(1)
const totalPages = ref(1)
const allPosts = ref<TrailListItem[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const trailInteractionStore = useTrailInteractionStore()
const trailFeedRefreshStore = useTrailFeedRefreshStore()
const { shareTrail } = useTrailShare()

const currentPosts = computed(() => {
  return allPosts.value
    .map((trail) => trailInteractionStore.applyToTrail(trail))
    .map(toCommunityPost)
})

const isInitialLoad = ref(true)

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)

  void loadPosts()
})

watch(currentPage, () => {
  void loadPosts()
})

watch(
  () => trailFeedRefreshStore.version,
  () => {
    void loadPosts()
  },
)

async function loadPosts() {
  if (isLoading.value) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const data = await fetchTrails({
      sort: 'latest',
      pageNum: currentPage.value,
      pageSize,
    })
    allPosts.value = data.list
    trailInteractionStore.hydrateTrails(data.list)
    totalPages.value = data.totalPages || 1
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '社区动态加载失败'
  } finally {
    isLoading.value = false
  }
}

async function handleShare(post: TrailListItem) {
  await shareTrail({
    id: post.id,
    name: post.name,
    location: post.location,
  })
}
</script>

<template>
  <main class="max-w-3xl mx-auto px-4 sm:px-6 py-8 sm:py-12">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-2xl sm:text-3xl font-bold" style="color: var(--text-primary);">社区动态</h1>
      <p class="text-sm mt-2" style="color: var(--text-secondary);">来自像你一样的探险者的实时自然发现。</p>
    </div>

    <!-- Posts -->
    <div v-if="isLoading" class="card p-6 text-sm text-center" style="color: var(--text-secondary);">
      正在加载社区动态...
    </div>
    <div v-else-if="errorMessage" class="card p-6 text-sm text-center" style="color: var(--color-hard);">
      {{ errorMessage }}
    </div>
    <div v-else-if="currentPosts.length === 0" class="card p-6 text-sm text-center" style="color: var(--text-secondary);">
      暂无社区动态
    </div>
    <div v-else class="space-y-5">
      <PostCard
        v-for="post in currentPosts"
        :key="post.id"
        :post="post"
        :is-initial-load="isInitialLoad"
        :is-like-pending="trailInteractionStore.isLikePending(post.id)"
        :is-favorite-pending="trailInteractionStore.isFavoritePending(post.id)"
        @toggle-like="trailInteractionStore.toggleLike(post)"
        @toggle-favorite="trailInteractionStore.toggleFavorite(post)"
        @share="handleShare(post)"
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
