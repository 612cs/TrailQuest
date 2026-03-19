<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import BaseIcon from '../common/BaseIcon.vue'
import type { UserTrailListItem } from '../../types/profile'

const props = defineProps<{
  items: UserTrailListItem[]
  tab: 'posts' | 'saved'
  hasMore: boolean
  isLoading: boolean
  errorMessage: string
}>()

const emit = defineEmits<{
  (event: 'load-more'): void
  (event: 'open-trail', item: UserTrailListItem): void
  (event: 'toggle-favorite', item: UserTrailListItem): void
}>()

const ROW_HEIGHT = 96
const OVERSCAN = 4
const STATUS_HEIGHT = 72

const scrollContainer = ref<HTMLElement | null>(null)
const scrollTop = ref(0)
const viewportHeight = ref(520)

let resizeObserver: ResizeObserver | null = null

const startIndex = computed(() => Math.max(Math.floor(scrollTop.value / ROW_HEIGHT) - OVERSCAN, 0))
const visibleCount = computed(() => Math.ceil(viewportHeight.value / ROW_HEIGHT) + OVERSCAN * 2)
const endIndex = computed(() => Math.min(startIndex.value + visibleCount.value, props.items.length))
const totalHeight = computed(() => props.items.length * ROW_HEIGHT)
const innerHeight = computed(() => totalHeight.value + STATUS_HEIGHT)
const isInitialLoading = computed(() => props.isLoading && props.items.length === 0)
const isEmpty = computed(() => !props.isLoading && !props.errorMessage && props.items.length === 0)

const virtualItems = computed(() =>
  props.items.slice(startIndex.value, endIndex.value).map((item, index) => ({
    item,
    top: (startIndex.value + index) * ROW_HEIGHT,
  })),
)

watch(
  () => props.items.length,
  async () => {
    await nextTick()
    updateViewportHeight()
  },
)

onMounted(() => {
  updateViewportHeight()

  if (typeof ResizeObserver !== 'undefined' && scrollContainer.value) {
    resizeObserver = new ResizeObserver(() => {
      updateViewportHeight()
      maybeLoadMore()
    })
    resizeObserver.observe(scrollContainer.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})

function updateViewportHeight() {
  viewportHeight.value = scrollContainer.value?.clientHeight || 520
}

function handleScroll(event: Event) {
  scrollTop.value = (event.target as HTMLElement).scrollTop
  maybeLoadMore()
}

function maybeLoadMore() {
  const container = scrollContainer.value
  if (!container || props.isLoading || !props.hasMore) {
    return
  }

  if (container.scrollTop + container.clientHeight >= container.scrollHeight - 160) {
    emit('load-more')
  }
}

function getSubtitle(item: UserTrailListItem) {
  return props.tab === 'posts'
    ? `${item.publishTime} • ${item.location || '未知地区'}`
    : `${item.authorUsername || '未知作者'} • ${item.location || '未知地区'}`
}
</script>

<template>
  <div class="h-[460px] overflow-y-auto sm:h-[560px]" ref="scrollContainer" @scroll="handleScroll">
    <div v-if="isInitialLoading" class="flex h-full items-center justify-center text-sm" style="color: var(--text-secondary);">
      正在加载路线...
    </div>

    <div v-else-if="errorMessage && items.length === 0" class="flex h-full flex-col items-center justify-center gap-3 px-6 text-center">
      <BaseIcon name="AlertCircle" :size="28" style="color: var(--color-hard);" />
      <p class="text-sm" style="color: var(--color-hard);">{{ errorMessage }}</p>
    </div>

    <div v-else-if="isEmpty" class="flex h-full flex-col items-center justify-center gap-3 px-6 text-center">
      <BaseIcon :name="tab === 'posts' ? 'FileText' : 'Bookmark'" :size="28" style="color: var(--text-tertiary);" />
      <p class="text-sm font-medium" style="color: var(--text-primary);">
        {{ tab === 'posts' ? '还没有发布路线' : '还没有收藏路线' }}
      </p>
      <p class="text-sm" style="color: var(--text-secondary);">
        {{ tab === 'posts' ? '发布后的路线会出现在这里。' : '去社区或详情页收藏喜欢的路线吧。' }}
      </p>
    </div>

    <div v-else class="relative" :style="{ height: `${innerHeight}px` }">
      <div
        v-for="{ item, top } in virtualItems"
        :key="item.id"
        class="absolute left-0 right-0 flex items-center gap-4 border-b px-4 text-left transition-colors hover:bg-primary-500/5"
        :style="{ top: `${top}px`, height: `${ROW_HEIGHT}px`, borderColor: 'var(--border-default)' }"
        @click="$emit('open-trail', item)"
      >
        <div class="h-14 w-14 shrink-0 overflow-hidden rounded-lg">
          <img :src="item.image" :alt="item.name" class="h-full w-full object-cover" />
        </div>
        <div class="min-w-0 flex-1">
          <h3 class="truncate text-sm font-medium" style="color: var(--text-primary);">{{ item.name }}</h3>
          <p class="mt-1 text-xs" style="color: var(--text-secondary);">{{ getSubtitle(item) }}</p>
        </div>
        <button
          v-if="tab === 'saved'"
          type="button"
          class="flex h-9 w-9 items-center justify-center rounded-full transition-colors hover:bg-primary-500/10"
          style="color: var(--primary-500);"
          @click.stop="$emit('toggle-favorite', item)"
        >
          <BaseIcon name="Bookmark" :size="18" class="fill-current" />
        </button>
        <BaseIcon v-else name="ChevronRight" :size="16" style="color: var(--text-tertiary);" />
      </div>

      <div
        class="absolute left-0 right-0 flex items-center justify-center px-4 text-sm"
        :style="{ top: `${totalHeight}px`, height: `${STATUS_HEIGHT}px`, color: 'var(--text-secondary)' }"
      >
        <span v-if="isLoading">正在加载更多...</span>
        <span v-else-if="errorMessage">{{ errorMessage }}</span>
        <span v-else-if="!hasMore">没有更多路线了</span>
      </div>
    </div>
  </div>
</template>
