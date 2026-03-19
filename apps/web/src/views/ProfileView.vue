<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import * as authApi from '../api/auth'
import BaseIcon from '../components/common/BaseIcon.vue'
import AccountSettingsModal from '../components/profile/AccountSettingsModal.vue'
import EditProfileModal from '../components/profile/EditProfileModal.vue'
import HikingProfileModal from '../components/profile/HikingProfileModal.vue'
import ProfileHeader from '../components/profile/ProfileHeader.vue'
import ProfileTrailList from '../components/profile/ProfileTrailList.vue'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import { useUserStore } from '../stores/useUserStore'
import type { ProfileTrailFeedState, UserTrailListItem } from '../types/profile'

const PAGE_SIZE = 10

const router = useRouter()
const userStore = useUserStore()
const trailInteractionStore = useTrailInteractionStore()

const activeTab = ref<'posts' | 'saved'>('posts')
const showEditModal = ref(false)
const showHikingProfileModal = ref(false)
const showSettingsModal = ref(false)

const feeds = reactive<Record<'posts' | 'saved', ProfileTrailFeedState>>({
  posts: createFeedState(),
  saved: createFeedState(),
})

const currentFeed = computed(() => feeds[activeTab.value])
const currentItems = computed(() =>
  currentFeed.value.items.map((item) => ({
    ...item,
    ...trailInteractionStore.applyToTrail(item),
  })),
)

watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (!isLoggedIn) {
      resetFeeds()
      return
    }

    void ensureFeedLoaded(activeTab.value)
  },
  { immediate: true },
)

watch(activeTab, async (tab) => {
  await ensureFeedLoaded(tab)
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    void ensureFeedLoaded(activeTab.value)
  }
})

function createFeedState(): ProfileTrailFeedState {
  return {
    items: [],
    pageNum: 1,
    hasMore: true,
    isLoading: false,
    initialized: false,
    errorMessage: '',
    total: 0,
  }
}

function resetFeeds() {
  feeds.posts = createFeedState()
  feeds.saved = createFeedState()
}

async function ensureFeedLoaded(tab: 'posts' | 'saved') {
  const feed = feeds[tab]
  if (feed.initialized || feed.isLoading) {
    return
  }
  await loadFeed(tab, true)
}

async function loadFeed(tab: 'posts' | 'saved', reset = false) {
  if (!userStore.isLoggedIn) {
    return
  }

  const feed = feeds[tab]
  if (feed.isLoading) {
    return
  }

  if (!reset && !feed.hasMore) {
    return
  }

  feed.isLoading = true
  feed.errorMessage = ''

  const targetPage = reset ? 1 : feed.pageNum

  try {
    const response = tab === 'posts'
      ? await authApi.fetchMyPublishedTrails(targetPage, PAGE_SIZE)
      : await authApi.fetchMyFavoriteTrails(targetPage, PAGE_SIZE)

    trailInteractionStore.hydrateTrails(response.list)

    feed.items = reset ? response.list : [...feed.items, ...response.list]
    feed.pageNum = response.pageNum + 1
    feed.total = response.total
    feed.hasMore = response.pageNum < response.totalPages
    feed.initialized = true
  } catch (error) {
    feed.errorMessage = error instanceof Error ? error.message : '路线加载失败'
    feed.initialized = true
  } finally {
    feed.isLoading = false
  }
}

async function handleLoadMore() {
  await loadFeed(activeTab.value)
}

function openTrail(item: UserTrailListItem) {
  void router.push(`/trail/${item.id}`)
}

async function handleToggleFavorite(item: UserTrailListItem) {
  const didToggle = await trailInteractionStore.toggleFavorite(item)
  if (!didToggle || activeTab.value !== 'saved' || !item.favoritedByCurrentUser) {
    return
  }

  const savedFeed = feeds.saved
  savedFeed.items = savedFeed.items.filter((entry) => entry.id !== item.id)
  savedFeed.total = Math.max(savedFeed.total - 1, 0)

  if (userStore.profile) {
    userStore.profile.savedCount = Math.max(userStore.profile.savedCount - 1, 0)
  }

  if (savedFeed.items.length === 0 && savedFeed.hasMore) {
    await loadFeed('saved')
  }
}
</script>

<template>
  <main class="mx-auto max-w-4xl space-y-6 px-4 py-8 sm:px-6 sm:py-12">
    <template v-if="userStore.isLoggedIn">
      <ProfileHeader
        @show-edit="showEditModal = true"
        @show-hiking-profile="showHikingProfileModal = true"
        @show-settings="showSettingsModal = true"
      />

      <EditProfileModal v-model:show="showEditModal" />
      <HikingProfileModal v-model:show="showHikingProfileModal" />
      <AccountSettingsModal v-model:show="showSettingsModal" />

      <div class="card overflow-hidden">
        <div class="flex items-center border-b" style="border-color: var(--border-default); background-color: var(--bg-tag);">
          <button
            class="flex items-center gap-2 border-b-2 px-5 py-3 text-sm font-medium transition-colors"
            :class="activeTab === 'posts' ? 'border-primary-500 text-primary-500' : 'border-transparent'"
            :style="activeTab !== 'posts' ? 'color: var(--text-secondary)' : ''"
            @click="activeTab = 'posts'"
          >
            <BaseIcon name="FileText" :size="16" />
            我的发布
          </button>
          <button
            class="flex items-center gap-2 border-b-2 px-5 py-3 text-sm font-medium transition-colors"
            :class="activeTab === 'saved' ? 'border-primary-500 text-primary-500' : 'border-transparent'"
            :style="activeTab !== 'saved' ? 'color: var(--text-secondary)' : ''"
            @click="activeTab = 'saved'"
          >
            <BaseIcon name="Bookmark" :size="16" />
            我的收藏
          </button>
        </div>

        <ProfileTrailList
          :items="currentItems"
          :tab="activeTab"
          :has-more="currentFeed.hasMore"
          :is-loading="currentFeed.isLoading"
          :error-message="currentFeed.errorMessage"
          @load-more="handleLoadMore"
          @open-trail="openTrail"
          @toggle-favorite="handleToggleFavorite"
        />
      </div>
    </template>

    <template v-else>
      <div class="card flex min-h-[50vh] flex-col items-center justify-center rounded-2xl p-8 text-center sm:p-12">
        <div class="mb-6 flex h-20 w-20 items-center justify-center rounded-full" style="background-color: var(--bg-tag); color: var(--color-primary-500);">
          <BaseIcon name="User" :size="36" />
        </div>
        <h2 class="mb-3 text-2xl font-bold" style="color: var(--text-primary);">欢迎来到 TrailQuest</h2>
        <p class="mx-auto mb-8 max-w-sm text-sm leading-relaxed sm:text-base" style="color: var(--text-secondary);">
          登录以查看您的个人资料、回味走过的路线以及管理您的收藏内容。
        </p>
        <button
          class="rounded-xl bg-primary-500 px-8 py-3 font-bold text-white shadow-lg shadow-primary-500/30 transition-all hover:bg-primary-600 hover:shadow-xl active:scale-[0.98]"
          @click="userStore.showAuthModal = true"
        >
          立即登录 / 注册
        </button>
      </div>
    </template>
  </main>
</template>
