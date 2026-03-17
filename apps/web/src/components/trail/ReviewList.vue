<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import type { ReviewItem, UserCard } from '../../types/review'
import { useUserStore } from '../../stores/useUserStore'
import BaseIcon from '../common/BaseIcon.vue'
import ImagePreviewModal from '../common/ImagePreviewModal.vue'
import SectionHeader from '../common/SectionHeader.vue'
import CommentForm from './CommentForm.vue'
import ReviewThreadItem from './ReviewThreadItem.vue'
import UserCardModal from './UserCardModal.vue'

const userStore = useUserStore()

const props = defineProps<{
  reviews: ReviewItem[]
  averageRating?: number
  totalReviews?: number
  isSubmitting?: boolean
  isLoadingMore?: boolean
  hasMore?: boolean
  errorMessage?: string
  loadMoreError?: string
  reviewFormResetKey?: number
  replyFormResetKey?: number
  activeUserCard?: UserCard | null
  isUserCardLoading?: boolean
  userCardErrorMessage?: string
  deletingIds?: string[]
  userCardVisible?: boolean
}>()

const emit = defineEmits([
  'submitReview',
  'submitReply',
  'loadMore',
  'retryLoadMore',
  'deleteReview',
  'openUserCard',
  'closeUserCard',
])

const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)
const replyingToId = ref<string | null>(null)
const replyingToName = ref('')
const loadMoreSentinel = ref<HTMLElement | null>(null)
const currentUserId = computed(() => userStore.profile?.id ? String(userStore.profile.id) : null)
const effectiveTotalReviews = computed(() => props.totalReviews ?? props.reviews.length)
let observer: IntersectionObserver | null = null

function openPreview(images: string[], index: number) {
  previewImages.value = images
  previewIndex.value = index
  showPreview.value = true
}

function startReply(review: ReviewItem) {
  userStore.requireAuth(() => {
    replyingToId.value = review.id
    replyingToName.value = review.author.username
  })
}

function cancelReply() {
  replyingToId.value = null
  replyingToName.value = ''
}

function handleCommentSubmit(payload: { rating?: number; text: string; images: string[] }) {
  userStore.requireAuth(() => {
    emit('submitReview', payload)
  })
}

function handleReplySubmit(payload: { rating?: number; text: string; images: string[] }) {
  if (!replyingToId.value) return

  userStore.requireAuth(() => {
    emit('submitReply', {
      parentId: replyingToId.value,
      text: payload.text,
      replyTo: replyingToName.value,
      images: payload.images,
    })
  })
}

function handleDeleteReview(review: ReviewItem) {
  emit('deleteReview', review)
}

function handleOpenUserCard(userId: string) {
  emit('openUserCard', userId)
}

function observeLoadMore() {
  if (observer) {
    observer.disconnect()
    observer = null
  }

  if (!loadMoreSentinel.value) return

  observer = new IntersectionObserver((entries) => {
    if (!entries[0]?.isIntersecting) return
    if (!props.hasMore || props.isLoadingMore || props.loadMoreError) return
    emit('loadMore')
  }, { rootMargin: '160px 0px 160px 0px' })

  observer.observe(loadMoreSentinel.value)
}

watch(
  () => [props.hasMore, props.isLoadingMore, props.loadMoreError, props.reviews.length],
  () => {
    void nextTick(() => {
      observeLoadMore()
    })
  },
  { deep: true },
)

watch(loadMoreSentinel, () => {
  void nextTick(() => {
    observeLoadMore()
  })
})

watch(() => props.replyFormResetKey, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    cancelReply()
  }
})

onMounted(() => {
  observeLoadMore()
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<template>
  <section class="animate-fade-in-up stagger-4">
    <SectionHeader title="用户评价">
      <template #action>
        <div class="flex items-center gap-1">
          <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
          <span class="text-sm font-bold text-primary-500">{{ averageRating ?? 0 }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">({{ effectiveTotalReviews }} 条)</span>
        </div>
      </template>
    </SectionHeader>

    <div class="mb-4">
      <CommentForm
        :disabled="isSubmitting"
        :reset-key="reviewFormResetKey"
        @submit="handleCommentSubmit"
      />
    </div>

    <p v-if="errorMessage" class="mb-4 text-sm" style="color: var(--color-hard);">
      {{ errorMessage }}
    </p>

    <div v-if="reviews.length === 0" class="card p-6 text-center text-sm" style="color: var(--text-secondary);">
      暂时还没有评论，来抢第一个沙发吧。
    </div>

    <div v-else class="space-y-3">
      <div v-for="review in reviews" :key="review.id" class="card p-4">
        <ReviewThreadItem
          :review="review"
          :level="0"
          :current-user-id="currentUserId"
          :replying-to-id="replyingToId"
          :reply-form-reset-key="replyFormResetKey"
          :is-submitting="isSubmitting"
          :deleting-ids="deletingIds"
          @preview-image="openPreview"
          @start-reply="startReply"
          @cancel-reply="cancelReply"
          @submit-reply="handleReplySubmit"
          @delete-review="handleDeleteReview"
          @open-user-card="handleOpenUserCard"
        />
      </div>

      <div v-if="loadMoreError" class="card p-4 text-center">
        <p class="text-sm" style="color: var(--color-hard);">{{ loadMoreError }}</p>
        <button
          type="button"
          class="mt-3 inline-flex items-center gap-1 rounded-full px-4 py-2 text-sm font-medium transition-colors hover:bg-primary-500/10"
          style="color: var(--primary-500);"
          @click="emit('retryLoadMore')"
        >
          <BaseIcon name="RefreshCcw" :size="14" />
          重试加载更多
        </button>
      </div>

      <div v-else-if="isLoadingMore" class="py-3 text-center text-sm" style="color: var(--text-secondary);">
        正在加载更多评论...
      </div>

      <div v-else-if="!hasMore" class="py-2 text-center text-xs" style="color: var(--text-tertiary);">
        已经到底啦
      </div>

      <div v-if="hasMore" ref="loadMoreSentinel" class="h-4 w-full" aria-hidden="true" />
    </div>

    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />

    <UserCardModal
      :show="userCardVisible ?? false"
      :user-card="activeUserCard ?? null"
      :is-loading="isUserCardLoading"
      :error-message="userCardErrorMessage"
      @close="emit('closeUserCard')"
    />
  </section>
</template>
