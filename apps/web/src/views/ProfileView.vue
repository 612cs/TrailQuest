<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import * as authApi from '../api/auth'
import { deleteTrail } from '../api/trails'
import BaseIcon from '../components/common/BaseIcon.vue'
import ConfirmDialog from '../components/common/ConfirmDialog.vue'
import AccountSettingsModal from '../components/profile/AccountSettingsModal.vue'
import EditProfileModal from '../components/profile/EditProfileModal.vue'
import HikingProfileModal from '../components/profile/HikingProfileModal.vue'
import ProfileHeader from '../components/profile/ProfileHeader.vue'
import ProfileTrailList from '../components/profile/ProfileTrailList.vue'
import { useTrailInteractionStore } from '../stores/useTrailInteractionStore'
import { useUserStore } from '../stores/useUserStore'
import type { ProfileTrailFeedState, UserTrailListItem } from '../types/profile'

const PAGE_SIZE = 10

type ProfileTab = 'posts' | 'saved'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const trailInteractionStore = useTrailInteractionStore()

const activeTab = ref<ProfileTab>(resolveTab(route.query.tab))
const showEditModal = ref(false)
const showHikingProfileModal = ref(false)
const showSettingsModal = ref(false)
const pendingDeleteTrail = ref<UserTrailListItem | null>(null)
const isDeletingTrail = ref(false)

const feeds = reactive<Record<ProfileTab, ProfileTrailFeedState>>({
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

watch(
  () => route.query.tab,
  (tab) => {
    const normalizedTab = resolveTab(tab)
    if (activeTab.value !== normalizedTab) {
      activeTab.value = normalizedTab
    }
  },
)

watch(activeTab, async (tab) => {
  syncTabQuery(tab)
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

async function ensureFeedLoaded(tab: ProfileTab) {
  const feed = feeds[tab]
  if (feed.initialized || feed.isLoading) {
    return
  }
  await loadFeed(tab, true)
}

async function loadFeed(tab: ProfileTab, reset = false) {
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
  void router.push({
    name: 'TrailDetail',
    params: { id: item.id },
    query: { from: route.fullPath },
  })
}

function handleEditTrail(item: UserTrailListItem) {
  if (!item.editableByCurrentUser) {
    return
  }
  void router.push({
    name: 'Publish',
    query: { edit: String(item.id) },
  })
}

function handleDeleteTrail(item: UserTrailListItem) {
  pendingDeleteTrail.value = item
}

async function confirmDeleteTrail() {
  if (!pendingDeleteTrail.value || isDeletingTrail.value) {
    return
  }

  isDeletingTrail.value = true

  try {
    await deleteTrail(pendingDeleteTrail.value.id)
    feeds.posts.items = feeds.posts.items.filter((item) => item.id !== pendingDeleteTrail.value?.id)
    feeds.posts.total = Math.max(feeds.posts.total - 1, 0)

    if (userStore.profile) {
      userStore.profile.postCount = Math.max(userStore.profile.postCount - 1, 0)
    }
  } catch (error) {
    feeds.posts.errorMessage = error instanceof Error ? error.message : '路线删除失败'
  } finally {
    isDeletingTrail.value = false
    pendingDeleteTrail.value = null
  }
}

function closeDeleteTrailDialog() {
  if (isDeletingTrail.value) {
    return
  }
  pendingDeleteTrail.value = null
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

function setActiveTab(tab: ProfileTab) {
  activeTab.value = tab
}

function resolveTab(tab: unknown): ProfileTab {
  return tab === 'saved' ? 'saved' : 'posts'
}

function syncTabQuery(tab: ProfileTab) {
  const nextTab = tab === 'posts' ? undefined : tab
  const currentTab = typeof route.query.tab === 'string' ? route.query.tab : undefined

  if (currentTab === nextTab || (!currentTab && !nextTab)) {
    return
  }

  void router.replace({
    name: 'Profile',
    query: {
      ...route.query,
      tab: nextTab,
    },
  })
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
            @click="setActiveTab('posts')"
          >
            <BaseIcon name="FileText" :size="16" />
            我的发布
          </button>
          <button
            class="flex items-center gap-2 border-b-2 px-5 py-3 text-sm font-medium transition-colors"
            :class="activeTab === 'saved' ? 'border-primary-500 text-primary-500' : 'border-transparent'"
            :style="activeTab !== 'saved' ? 'color: var(--text-secondary)' : ''"
            @click="setActiveTab('saved')"
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
          @edit-trail="handleEditTrail"
          @delete-trail="handleDeleteTrail"
        />
      </div>

      <ConfirmDialog
        :show="!!pendingDeleteTrail"
        title="删除路线"
        message="确认删除这条路线吗？删除后将不会再出现在前台列表和详情页。"
        confirm-text="确认删除"
        cancel-text="暂不删除"
        tone="danger"
        :confirm-loading="isDeletingTrail"
        @update:show="(value) => { if (!value) closeDeleteTrailDialog() }"
        @cancel="closeDeleteTrailDialog"
        @confirm="confirmDeleteTrail"
      />
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
