<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import type { ReviewItem } from '../../types/review'
import { useUserStore } from '../../stores/useUserStore'
import BaseIcon from '../common/BaseIcon.vue'
import ImagePreviewModal from '../common/ImagePreviewModal.vue'
import SectionHeader from '../common/SectionHeader.vue'
import CommentForm from './CommentForm.vue'

const userStore = useUserStore()

const props = defineProps<{
  reviews: ReviewItem[]
  averageRating?: number
  totalReviews?: number
  isSubmitting?: boolean
  errorMessage?: string
  reviewFormResetKey?: number
  replyFormResetKey?: number
}>()

const emit = defineEmits<{
  submitReview: [payload: { rating?: number; text: string; images: string[] }]
  submitReply: [payload: { parentId: string; text: string; replyTo?: string; images: string[] }]
}>()

const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)
const replyingToId = ref<string | null>(null)
const replyingToName = ref('')

const effectiveTotalReviews = computed(() => props.totalReviews ?? props.reviews.length)

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
      parentId: replyingToId.value!,
      text: payload.text,
      replyTo: replyingToName.value,
      images: payload.images,
    })
  })
}

watch(() => props.replyFormResetKey, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    cancelReply()
  }
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
        <div class="flex items-start gap-3">
          <div class="h-9 w-9 shrink-0 overflow-hidden rounded-full border" style="border-color: var(--border-default);">
            <img
              v-if="review.author.avatarMediaUrl"
              :src="review.author.avatarMediaUrl"
              alt="用户头像"
              class="h-full w-full object-cover"
            />
            <div
              v-else
              class="flex h-full w-full items-center justify-center text-xs font-bold text-white"
              :style="{ backgroundColor: review.author.avatarBg }"
            >
              {{ review.author.avatar }}
            </div>
          </div>

          <div class="min-w-0 flex-1">
            <div class="flex items-center justify-between gap-3">
              <span class="text-sm font-semibold" style="color: var(--text-primary);">{{ review.author.username }}</span>
              <span class="text-xs" style="color: var(--text-tertiary);">{{ review.time }}</span>
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

            <p class="text-sm leading-relaxed" style="color: var(--text-secondary);">{{ review.text }}</p>

            <div v-if="review.images.length > 0" class="mt-2 flex gap-2">
              <button
                v-for="(image, imageIndex) in review.images"
                :key="image"
                type="button"
                class="h-16 w-16 overflow-hidden rounded-lg sm:h-20 sm:w-20"
                @click="openPreview(review.images, imageIndex)"
              >
                <img :src="image" alt="" class="h-full w-full object-cover transition-transform hover:scale-105" />
              </button>
            </div>

            <button
              type="button"
              class="mt-2 flex items-center gap-1 text-xs font-medium transition-colors hover:text-primary-500"
              style="color: var(--text-tertiary);"
              @click="startReply(review)"
            >
              <BaseIcon name="Reply" :size="14" />
              回复
            </button>

            <div v-if="replyingToId === review.id" class="mt-3">
              <CommentForm
                mode="reply"
                compact
                submit-label="发送回复"
                :placeholder="`回复 @${replyingToName}...`"
                :disabled="isSubmitting"
                :reset-key="replyFormResetKey"
                @submit="handleReplySubmit"
              />
              <button
                type="button"
                class="mt-2 text-xs"
                style="color: var(--text-tertiary);"
                @click="cancelReply"
              >
                取消回复
              </button>
            </div>

            <div v-if="review.replies.length > 0" class="mt-3 space-y-3 border-l-2 pl-4" style="border-color: var(--border-default);">
              <template v-for="reply1 in review.replies" :key="reply1.id">
                <div class="flex items-start gap-2.5">
                  <div class="h-7 w-7 shrink-0 overflow-hidden rounded-full border" style="border-color: var(--border-default);">
                    <img
                      v-if="reply1.author.avatarMediaUrl"
                      :src="reply1.author.avatarMediaUrl"
                      alt="用户头像"
                      class="h-full w-full object-cover"
                    />
                    <div
                      v-else
                      class="flex h-full w-full items-center justify-center text-[10px] font-bold text-white"
                      :style="{ backgroundColor: reply1.author.avatarBg }"
                    >
                      {{ reply1.author.avatar }}
                    </div>
                  </div>

                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-semibold" style="color: var(--text-primary);">{{ reply1.author.username }}</span>
                      <span v-if="reply1.replyTo" class="text-xs" style="color: var(--text-tertiary);">
                        回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ reply1.replyTo }}</span>
                      </span>
                      <span class="ml-auto text-[10px]" style="color: var(--text-tertiary);">{{ reply1.time }}</span>
                    </div>
                    <p class="mt-0.5 text-sm leading-relaxed" style="color: var(--text-secondary);">{{ reply1.text }}</p>

                    <div v-if="reply1.images.length > 0" class="mt-2 flex gap-2">
                      <button
                        v-for="(image, imageIndex) in reply1.images"
                        :key="image"
                        type="button"
                        class="h-14 w-14 overflow-hidden rounded-lg"
                        @click="openPreview(reply1.images, imageIndex)"
                      >
                        <img :src="image" alt="" class="h-full w-full object-cover" />
                      </button>
                    </div>

                    <button
                      type="button"
                      class="mt-1 flex items-center gap-1 text-xs font-medium transition-colors hover:text-primary-500"
                      style="color: var(--text-tertiary);"
                      @click="startReply(reply1)"
                    >
                      <BaseIcon name="Reply" :size="12" />
                      回复
                    </button>

                    <div v-if="replyingToId === reply1.id" class="mt-2">
                      <CommentForm
                        mode="reply"
                        compact
                        submit-label="发送回复"
                        :placeholder="`回复 @${replyingToName}...`"
                        :disabled="isSubmitting"
                        :reset-key="replyFormResetKey"
                        @submit="handleReplySubmit"
                      />
                      <button
                        type="button"
                        class="mt-2 text-xs"
                        style="color: var(--text-tertiary);"
                        @click="cancelReply"
                      >
                        取消回复
                      </button>
                    </div>

                    <div v-if="reply1.replies.length > 0" class="mt-2 space-y-2 border-l-2 pl-3" style="border-color: var(--border-default);">
                      <div v-for="reply2 in reply1.replies" :key="reply2.id" class="flex items-start gap-2">
                        <div class="h-6 w-6 shrink-0 overflow-hidden rounded-full border" style="border-color: var(--border-default);">
                          <img
                            v-if="reply2.author.avatarMediaUrl"
                            :src="reply2.author.avatarMediaUrl"
                            alt="用户头像"
                            class="h-full w-full object-cover"
                          />
                          <div
                            v-else
                            class="flex h-full w-full items-center justify-center text-[9px] font-bold text-white"
                            :style="{ backgroundColor: reply2.author.avatarBg }"
                          >
                            {{ reply2.author.avatar }}
                          </div>
                        </div>

                        <div class="min-w-0 flex-1">
                          <div class="flex items-center gap-2">
                            <span class="text-xs font-semibold" style="color: var(--text-primary);">{{ reply2.author.username }}</span>
                            <span v-if="reply2.replyTo" class="text-xs" style="color: var(--text-tertiary);">
                              回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ reply2.replyTo }}</span>
                            </span>
                            <span class="ml-auto text-[10px]" style="color: var(--text-tertiary);">{{ reply2.time }}</span>
                          </div>
                          <p class="mt-0.5 text-xs leading-relaxed" style="color: var(--text-secondary);">{{ reply2.text }}</p>

                          <div v-if="reply2.images.length > 0" class="mt-2 flex gap-2">
                            <button
                              v-for="(image, imageIndex) in reply2.images"
                              :key="image"
                              type="button"
                              class="h-12 w-12 overflow-hidden rounded-lg"
                              @click="openPreview(reply2.images, imageIndex)"
                            >
                              <img :src="image" alt="" class="h-full w-full object-cover" />
                            </button>
                          </div>

                          <button
                            type="button"
                            class="mt-1 flex items-center gap-1 text-[10px] font-medium transition-colors hover:text-primary-500"
                            style="color: var(--text-tertiary);"
                            @click="startReply(reply2)"
                          >
                            <BaseIcon name="Reply" :size="10" />
                            回复
                          </button>

                          <div v-if="replyingToId === reply2.id" class="mt-2">
                            <CommentForm
                              mode="reply"
                              compact
                              submit-label="发送回复"
                              :placeholder="`回复 @${replyingToName}...`"
                              :disabled="isSubmitting"
                              :reset-key="replyFormResetKey"
                              @submit="handleReplySubmit"
                            />
                            <button
                              type="button"
                              class="mt-2 text-xs"
                              style="color: var(--text-tertiary);"
                              @click="cancelReply"
                            >
                              取消回复
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />
  </section>
</template>
