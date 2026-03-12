<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import ImageUploader from '../common/ImageUploader.vue'

const emit = defineEmits<{
  submit: [review: { rating: number; text: string; images: string[] }]
}>()

const rating = ref(0)
const hoverRating = ref(0)
const text = ref('')
const images = ref<string[]>([])
const isSubmitting = ref(false)

function setRating(value: number) {
  rating.value = value
}

async function handleSubmit() {
  if (!rating.value || !text.value.trim()) return

  isSubmitting.value = true
  // Simulate network delay
  await new Promise((r) => setTimeout(r, 600))

  emit('submit', {
    rating: rating.value,
    text: text.value.trim(),
    images: [...images.value],
  })

  // Reset form
  rating.value = 0
  text.value = ''
  images.value = []
  isSubmitting.value = false
}
</script>

<template>
  <div class="card p-4 sm:p-5 space-y-4">
    <h3 class="text-sm font-semibold" style="color: var(--text-primary);">发表评论</h3>

    <!-- Star Rating -->
    <div class="flex items-center gap-3">
      <span class="text-xs" style="color: var(--text-secondary);">评分</span>
      <div class="flex gap-0.5">
        <button
          v-for="i in 5"
          :key="i"
          @click="setRating(i)"
          @mouseenter="hoverRating = i"
          @mouseleave="hoverRating = 0"
          class="p-0.5 transition-transform hover:scale-110"
        >
          <BaseIcon
            name="Star"
            :size="22"
            :class="
              i <= (hoverRating || rating)
                ? 'text-yellow-400 fill-current'
                : 'text-gray-300'
            "
          />
        </button>
      </div>
      <span v-if="rating > 0" class="text-xs font-medium text-primary-500">
        {{ ['', '很差', '较差', '一般', '不错', '很棒'][rating] }}
      </span>
    </div>

    <!-- Text Input -->
    <div>
      <textarea
        v-model="text"
        rows="3"
        placeholder="分享你的徒步体验..."
        class="w-full px-3 py-2.5 rounded-lg text-sm resize-none transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
        style="
          background-color: var(--bg-input);
          color: var(--text-primary);
          border: 1px solid var(--border-default);
        "
      />
    </div>

    <!-- Image Upload -->
    <ImageUploader v-model="images" :max="9" />

    <!-- Submit Button -->
    <div class="flex justify-end">
      <button
        @click="handleSubmit"
        :disabled="!rating || !text.trim() || isSubmitting"
        class="px-5 py-2 rounded-lg text-sm font-medium text-white transition-all duration-200 flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
        :class="
          !rating || !text.trim() || isSubmitting
            ? 'bg-gray-400'
            : 'bg-primary-500 hover:bg-primary-600 hover:shadow-md active:scale-[0.97]'
        "
      >
        <BaseIcon v-if="isSubmitting" name="Loader2" :size="16" class="animate-spin" />
        <BaseIcon v-else name="Send" :size="16" />
        {{ isSubmitting ? '提交中...' : '发布评论' }}
      </button>
    </div>
  </div>
</template>
