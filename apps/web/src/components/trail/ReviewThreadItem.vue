<script setup lang="ts">
import { computed } from 'vue'

import type { ReviewItem } from '../../types/review'
import BaseIcon from '../common/BaseIcon.vue'
import CommentForm from './CommentForm.vue'

const props = defineProps<{
  review: ReviewItem
  level: number
  currentUserId?: string | null
  replyingToId?: string | null
  replyFormResetKey?: number
  isSubmitting?: boolean
  deletingIds?: string[]
}>()

const emit = defineEmits([
  'previewImage',
  'startReply',
  'cancelReply',
  'submitReply',
  'deleteReview',
  'openUserCard',
])

const isOwnComment = computed(() => props.currentUserId === props.review.userId)
const isReplying = computed(() => props.replyingToId === props.review.id)
const isDeleting = computed(() => props.deletingIds?.includes(props.review.id) ?? false)

const avatarSizeClass = computed(() => {
  if (props.level === 0) return 'h-9 w-9 text-xs'
  if (props.level === 1) return 'h-7 w-7 text-[10px]'
  return 'h-6 w-6 text-[9px]'
})

const bodyTextClass = computed(() => (props.level >= 2 ? 'text-xs' : 'text-sm'))
const metaTextClass = computed(() => (props.level >= 2 ? 'text-[10px]' : 'text-xs'))
const replyButtonIconSize = computed(() => (props.level >= 2 ? 10 : props.level === 1 ? 12 : 14))
const imageThumbClass = computed(() => {
  if (props.level === 0) return 'h-16 w-16 sm:h-20 sm:w-20'
  if (props.level === 1) return 'h-14 w-14'
  return 'h-12 w-12'
})

function handlePreview(images: string[], index: number) {
  emit('previewImage', images, index)
}

function handleStartReply(review: ReviewItem) {
  emit('startReply', review)
}

function handleSubmitReply(payload: { rating?: number; text: string; images: string[] }) {
  emit('submitReply', payload)
}

function handleDelete(review: ReviewItem) {
  emit('deleteReview', review)
}

function handleOpenUserCard(userId: string) {
  emit('openUserCard', userId)
}
</script>

<template>
  <div class="flex items-start gap-3" :class="level > 0 ? 'gap-2.5' : ''">
    <button
      type="button"
      class="shrink-0 overflow-hidden rounded-full border transition-transform hover:scale-[1.03]"
      :class="avatarSizeClass"
      style="border-color: var(--border-default);"
      @click="handleOpenUserCard(review.author.id)"
    >
      <img
        v-if="review.author.avatarMediaUrl"
        :src="review.author.avatarMediaUrl"
        alt="用户头像"
        class="h-full w-full object-cover"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center font-bold text-white"
        :style="{ backgroundColor: review.author.avatarBg }"
      >
        {{ review.author.avatar }}
      </div>
    </button>

    <div class="min-w-0 flex-1">
      <div class="flex items-center gap-2 flex-wrap">
        <button
          type="button"
          class="font-semibold text-left transition-colors hover:text-primary-500"
          :class="level === 0 ? 'text-sm' : 'text-xs'"
          style="color: var(--text-primary);"
          @click="handleOpenUserCard(review.author.id)"
        >
          {{ review.author.username }}
        </button>
        <span v-if="review.replyTo" :class="metaTextClass" style="color: var(--text-tertiary);">
          回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ review.replyTo }}</span>
        </span>
        <span class="ml-auto" :class="metaTextClass" style="color: var(--text-tertiary);">{{ review.time }}</span>
      </div>

      <div v-if="review.rating" class="my-1 flex gap-0.5">
        <BaseIcon
          v-for="index in 5"
          :key="index"
          name="Star"
          :size="14"
          :class="index <= review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
        />
      </div>

      <p class="leading-relaxed" :class="bodyTextClass" style="color: var(--text-secondary);">{{ review.text }}</p>

      <div v-if="review.images.length > 0" class="mt-2 flex gap-2 flex-wrap">
        <button
          v-for="(image, imageIndex) in review.images"
          :key="`${review.id}-${imageIndex}`"
          type="button"
          class="overflow-hidden rounded-lg"
          :class="imageThumbClass"
          @click="handlePreview(review.images, imageIndex)"
        >
          <img :src="image" alt="" class="h-full w-full object-cover transition-transform hover:scale-105" />
        </button>
      </div>

      <div class="mt-2 flex items-center gap-3 flex-wrap">
        <button
          type="button"
          class="flex items-center gap-1 font-medium transition-colors hover:text-primary-500"
          :class="metaTextClass"
          style="color: var(--text-tertiary);"
          @click="handleStartReply(review)"
        >
          <BaseIcon name="Reply" :size="replyButtonIconSize" />
          回复
        </button>

        <button
          v-if="isOwnComment"
          type="button"
          class="flex items-center gap-1 font-medium transition-colors hover:text-[var(--color-hard)] disabled:opacity-60 disabled:cursor-not-allowed"
          :class="metaTextClass"
          style="color: var(--text-tertiary);"
          :disabled="isDeleting"
          @click="handleDelete(review)"
        >
          <BaseIcon name="Trash2" :size="replyButtonIconSize" />
          {{ isDeleting ? '删除中...' : '删除' }}
        </button>
      </div>

      <div v-if="isReplying" class="mt-3">
        <CommentForm
          mode="reply"
          compact
          submit-label="发送回复"
          :placeholder="`回复 @${review.author.username}...`"
          :disabled="isSubmitting"
          :reset-key="replyFormResetKey"
          @submit="handleSubmitReply"
        />
        <button
          type="button"
          class="mt-2 text-xs"
          style="color: var(--text-tertiary);"
          @click="emit('cancelReply')"
        >
          取消回复
        </button>
      </div>

      <div
        v-if="review.replies.length > 0"
        class="mt-3 space-y-3 border-l-2 pl-4"
        :class="level >= 1 ? 'pl-3' : ''"
        style="border-color: var(--border-default);"
      >
        <ReviewThreadItem
          v-for="reply in review.replies"
          :key="reply.id"
          :review="reply"
          :level="Math.min(level + 1, 2)"
          :current-user-id="currentUserId"
          :replying-to-id="replyingToId"
          :reply-form-reset-key="replyFormResetKey"
          :is-submitting="isSubmitting"
          :deleting-ids="deletingIds"
          @preview-image="handlePreview"
          @start-reply="handleStartReply"
          @cancel-reply="emit('cancelReply')"
          @submit-reply="handleSubmitReply"
          @delete-review="handleDelete"
          @open-user-card="handleOpenUserCard"
        />
      </div>
    </div>
  </div>
</template>
