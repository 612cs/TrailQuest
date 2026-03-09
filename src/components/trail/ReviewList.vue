<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import SectionHeader from '../common/SectionHeader.vue'
import ImagePreviewModal from '../common/ImagePreviewModal.vue'
import CommentForm from './CommentForm.vue'
import type { Review } from '../../mock/mockData'
import { generateReviewId } from '../../mock/mockData'

const props = defineProps<{
  reviews: Review[]
  averageRating?: number
  totalReviews?: number
}>()

const emit = defineEmits<{
  addReview: [review: Review]
}>()

// Image preview state
const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)

// Reply state
const replyingTo = ref<{ reviewId: number; userName: string; parentChain: number[] } | null>(null)

function openPreview(images: string[], index: number) {
  previewImages.value = images
  previewIndex.value = index
  showPreview.value = true
}

function startReply(reviewId: number, userName: string, parentChain: number[] = []) {
  replyingTo.value = { reviewId, userName, parentChain: [...parentChain, reviewId] }
}

function cancelReply() {
  replyingTo.value = null
}

function handleCommentSubmit(data: { rating: number; text: string; images: string[] }) {
  const newReview: Review = {
    id: generateReviewId(),
    user: '当前用户',
    avatar: 'ME',
    avatarBg: '#4f9a48',
    rating: data.rating,
    time: '刚刚',
    text: data.text,
    images: data.images.length > 0 ? data.images : undefined,
  }
  emit('addReview', newReview)
}

function handleReplySubmit(text: string) {
  if (!replyingTo.value || !text.trim()) return

  const reply: Review = {
    id: generateReviewId(),
    user: '当前用户',
    avatar: 'ME',
    avatarBg: '#4f9a48',
    time: '刚刚',
    text: text.trim(),
    replyTo: replyingTo.value.userName,
  }

  // Find the target review and add reply
  addReplyToReview(props.reviews, replyingTo.value.reviewId, reply)
  replyingTo.value = null
}

function addReplyToReview(reviews: Review[], targetId: number, reply: Review): boolean {
  for (const review of reviews) {
    if (review.id === targetId) {
      if (!review.replies) review.replies = []
      review.replies.push(reply)
      return true
    }
    if (review.replies && addReplyToReview(review.replies, targetId, reply)) {
      return true
    }
  }
  return false
}
</script>

<template>
  <section class="animate-fade-in-up stagger-4">
    <SectionHeader title="用户评价">
      <template #action>
        <div class="flex items-center gap-1">
          <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
          <span class="text-sm font-bold text-primary-500">{{ averageRating ?? 4.8 }}</span>
          <span class="text-xs" style="color: var(--text-tertiary);">({{ totalReviews ?? reviews.length }} 条)</span>
        </div>
      </template>
    </SectionHeader>

    <!-- Comment Form -->
    <div class="mb-4">
      <CommentForm @submit="handleCommentSubmit" />
    </div>

    <!-- Review List -->
    <div class="space-y-3">
      <div v-for="review in reviews" :key="review.id" class="card p-4">
        <!-- Top-level Review -->
        <component
          :is="'div'"
          class="review-item"
        >
          <div class="flex items-start gap-3">
            <div
              class="w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0"
              :style="`background-color: ${review.avatarBg}`"
            >
              {{ review.avatar }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <span class="text-sm font-semibold" style="color: var(--text-primary);">{{ review.user }}</span>
                <span class="text-xs" style="color: var(--text-tertiary);">{{ review.time }}</span>
              </div>
              <div v-if="review.rating" class="flex gap-0.5 my-1">
                <BaseIcon
                  v-for="i in 5"
                  :key="i"
                  name="Star"
                  :size="14"
                  :class="i <= review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                />
              </div>
              <p class="text-sm leading-relaxed" style="color: var(--text-secondary);">{{ review.text }}</p>

              <!-- Review Images -->
              <div v-if="review.images && review.images.length > 0" class="flex gap-2 mt-2">
                <div
                  v-for="(img, idx) in review.images"
                  :key="idx"
                  class="w-16 h-16 sm:w-20 sm:h-20 rounded-lg overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                  @click="openPreview(review.images!, idx)"
                >
                  <img :src="img" alt="" class="w-full h-full object-cover" />
                </div>
              </div>

              <!-- Reply Button -->
              <button
                @click="startReply(review.id, review.user)"
                class="mt-2 flex items-center gap-1 text-xs font-medium transition-colors hover:text-primary-500"
                style="color: var(--text-tertiary);"
              >
                <BaseIcon name="Reply" :size="14" />
                回复
              </button>

              <!-- Inline Reply Input (for this review) -->
              <div v-if="replyingTo?.reviewId === review.id" class="mt-3 pl-0">
                <div class="flex gap-2">
                  <div class="w-7 h-7 rounded-full bg-primary-500 flex items-center justify-center text-white text-[10px] font-bold shrink-0">ME</div>
                  <div class="flex-1">
                    <p class="text-xs mb-1.5" style="color: var(--text-tertiary);">
                      回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ replyingTo.userName }}</span>
                    </p>
                    <div class="flex gap-2">
                      <input
                        type="text"
                        :placeholder="`回复 @${replyingTo.userName}...`"
                        class="flex-1 px-3 py-2 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                        style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
                        @keyup.enter="handleReplySubmit(($event.target as HTMLInputElement).value); ($event.target as HTMLInputElement).value = ''"
                      />
                      <button
                        @click="cancelReply"
                        class="px-2 py-1.5 text-xs rounded-md transition-colors hover:bg-red-500/10"
                        style="color: var(--text-tertiary);"
                      >
                        取消
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Nested Replies (Level 1) -->
              <div v-if="review.replies && review.replies.length > 0" class="mt-3 space-y-3 pl-4 border-l-2" style="border-color: var(--border-default);">
                <div v-for="reply1 in review.replies" :key="reply1.id">
                  <div class="flex items-start gap-2.5">
                    <div
                      class="w-7 h-7 rounded-full flex items-center justify-center text-white text-[10px] font-bold shrink-0"
                      :style="`background-color: ${reply1.avatarBg}`"
                    >
                      {{ reply1.avatar }}
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <span class="text-xs font-semibold" style="color: var(--text-primary);">{{ reply1.user }}</span>
                        <span v-if="reply1.replyTo" class="text-xs" style="color: var(--text-tertiary);">
                          回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ reply1.replyTo }}</span>
                        </span>
                        <span class="text-[10px] ml-auto" style="color: var(--text-tertiary);">{{ reply1.time }}</span>
                      </div>
                      <p class="text-sm leading-relaxed mt-0.5" style="color: var(--text-secondary);">{{ reply1.text }}</p>
                      <button
                        @click="startReply(reply1.id, reply1.user, [review.id])"
                        class="mt-1 flex items-center gap-1 text-xs font-medium transition-colors hover:text-primary-500"
                        style="color: var(--text-tertiary);"
                      >
                        <BaseIcon name="Reply" :size="12" />
                        回复
                      </button>

                      <!-- Inline Reply Input (for level 1 reply) -->
                      <div v-if="replyingTo?.reviewId === reply1.id" class="mt-2">
                        <div class="flex gap-2">
                          <div class="w-6 h-6 rounded-full bg-primary-500 flex items-center justify-center text-white text-[9px] font-bold shrink-0">ME</div>
                          <div class="flex-1 flex gap-2">
                            <input
                              type="text"
                              :placeholder="`回复 @${replyingTo.userName}...`"
                              class="flex-1 px-3 py-1.5 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                              style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
                              @keyup.enter="handleReplySubmit(($event.target as HTMLInputElement).value); ($event.target as HTMLInputElement).value = ''"
                            />
                            <button
                              @click="cancelReply"
                              class="px-2 py-1 text-xs rounded-md hover:bg-red-500/10"
                              style="color: var(--text-tertiary);"
                            >
                              取消
                            </button>
                          </div>
                        </div>
                      </div>

                      <!-- Nested Replies (Level 2+) -->
                      <div v-if="reply1.replies && reply1.replies.length > 0" class="mt-2 space-y-2 pl-3 border-l-2" style="border-color: var(--border-default);">
                        <div v-for="reply2 in reply1.replies" :key="reply2.id">
                          <div class="flex items-start gap-2">
                            <div
                              class="w-6 h-6 rounded-full flex items-center justify-center text-white text-[9px] font-bold shrink-0"
                              :style="`background-color: ${reply2.avatarBg}`"
                            >
                              {{ reply2.avatar }}
                            </div>
                            <div class="flex-1 min-w-0">
                              <div class="flex items-center gap-2">
                                <span class="text-xs font-semibold" style="color: var(--text-primary);">{{ reply2.user }}</span>
                                <span v-if="reply2.replyTo" class="text-xs" style="color: var(--text-tertiary);">
                                  回复 <span class="font-medium" style="color: var(--text-secondary);">@{{ reply2.replyTo }}</span>
                                </span>
                                <span class="text-[10px] ml-auto" style="color: var(--text-tertiary);">{{ reply2.time }}</span>
                              </div>
                              <p class="text-xs leading-relaxed mt-0.5" style="color: var(--text-secondary);">{{ reply2.text }}</p>
                              <button
                                @click="startReply(reply2.id, reply2.user, [review.id, reply1.id])"
                                class="mt-1 flex items-center gap-1 text-[10px] font-medium transition-colors hover:text-primary-500"
                                style="color: var(--text-tertiary);"
                              >
                                <BaseIcon name="Reply" :size="10" />
                                回复
                              </button>

                              <!-- Inline Reply Input (for level 2 reply) -->
                              <div v-if="replyingTo?.reviewId === reply2.id" class="mt-2">
                                <div class="flex gap-2">
                                  <div class="w-5 h-5 rounded-full bg-primary-500 flex items-center justify-center text-white text-[8px] font-bold shrink-0">ME</div>
                                  <div class="flex-1 flex gap-2">
                                    <input
                                      type="text"
                                      :placeholder="`回复 @${replyingTo.userName}...`"
                                      class="flex-1 px-2 py-1.5 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                                      style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
                                      @keyup.enter="handleReplySubmit(($event.target as HTMLInputElement).value); ($event.target as HTMLInputElement).value = ''"
                                    />
                                    <button
                                      @click="cancelReply"
                                      class="px-2 py-1 text-xs rounded-md hover:bg-red-500/10"
                                      style="color: var(--text-tertiary);"
                                    >
                                      取消
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </component>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />
  </section>
</template>
