<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { useOssImageUploader } from '../../composables/useOssImageUploader'
import BaseIcon from '../common/BaseIcon.vue'

const props = withDefaults(defineProps<{
  mode?: 'review' | 'reply'
  placeholder?: string
  submitLabel?: string
  compact?: boolean
  disabled?: boolean
  resetKey?: number
}>(), {
  mode: 'review',
  placeholder: '分享你的徒步体验...',
  submitLabel: '发布评论',
  compact: false,
  disabled: false,
  resetKey: 0,
})

const emit = defineEmits<{
  submit: [review: { rating?: number; text: string; images: string[] }]
}>()

const rating = ref(0)
const hoverRating = ref(0)
const text = ref('')
const {
  items,
  isUploading,
  canAddMore,
  uploadedUrls,
  addFiles,
  removeItem: removeUploadedImage,
  clear,
} = useOssImageUploader({
  bizType: 'review',
  max: 9,
})

const requiresRating = computed(() => props.mode === 'review')
const canSubmit = computed(() => {
  if (props.disabled || isUploading.value || !text.value.trim()) {
    return false
  }
  if (requiresRating.value && !rating.value) {
    return false
  }
  return true
})

function setRating(value: number) {
  rating.value = value
}

async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files?.length) {
    await addFiles(input.files)
  }
  input.value = ''
}

function removeImage(imageId: string) {
  removeUploadedImage(imageId)
}

function resetForm() {
  rating.value = 0
  hoverRating.value = 0
  text.value = ''
  clear()
}

function handleSubmit() {
  if (!canSubmit.value) return

  emit('submit', {
    rating: requiresRating.value ? rating.value : undefined,
    text: text.value.trim(),
    images: [...uploadedUrls.value],
  })
}

watch(() => props.resetKey, () => {
  resetForm()
})
</script>

<template>
  <div
    class="card space-y-4"
    :class="compact ? 'p-3 sm:p-4' : 'p-4 sm:p-5'"
  >
    <h3 v-if="!compact" class="text-sm font-semibold" style="color: var(--text-primary);">
      {{ mode === 'review' ? '发表评论' : '写下回复' }}
    </h3>

    <div v-if="requiresRating" class="flex items-center gap-3">
      <span class="text-xs" style="color: var(--text-secondary);">评分</span>
      <div class="flex gap-0.5">
        <button
          v-for="i in 5"
          :key="i"
          type="button"
          :disabled="disabled"
          class="p-0.5 transition-transform hover:scale-110 disabled:cursor-not-allowed"
          @click="setRating(i)"
          @mouseenter="hoverRating = i"
          @mouseleave="hoverRating = 0"
        >
          <BaseIcon
            name="Star"
            :size="compact ? 18 : 22"
            :class="i <= (hoverRating || rating) ? 'text-yellow-400 fill-current' : 'text-gray-300'"
          />
        </button>
      </div>
      <span v-if="rating > 0" class="text-xs font-medium text-primary-500">
        {{ ['', '很差', '较差', '一般', '不错', '很棒'][rating] }}
      </span>
    </div>

    <div>
      <textarea
        v-model="text"
        :rows="compact ? 2 : 3"
        :placeholder="placeholder"
        :disabled="disabled"
        class="w-full px-3 py-2.5 rounded-lg text-sm resize-none transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30 disabled:opacity-60"
        style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
      />
    </div>

    <div class="space-y-3">
      <div v-if="items.length > 0" class="grid grid-cols-3 sm:grid-cols-4 gap-2">
        <div
          v-for="item in items"
          :key="item.id"
          class="relative group aspect-square overflow-hidden rounded-lg border"
          style="border-color: var(--border-default);"
        >
          <img :src="item.localUrl" alt="" class="h-full w-full object-cover" />
          <div class="absolute inset-x-0 bottom-0 bg-black/60 px-2 py-1 text-[10px] text-white">
            <span v-if="item.status === 'uploading'">上传中 {{ item.progress }}%</span>
            <span v-else-if="item.status === 'success'">上传完成</span>
            <span v-else>{{ item.errorMessage }}</span>
          </div>
          <button
            type="button"
            class="absolute right-1 top-1 flex h-6 w-6 items-center justify-center rounded-full bg-black/60 text-white opacity-0 transition-opacity group-hover:opacity-100 hover:bg-red-500"
            @click="removeImage(item.id)"
          >
            <BaseIcon name="X" :size="14" />
          </button>
        </div>

        <label
          v-if="canAddMore"
          class="aspect-square rounded-lg border-2 border-dashed flex cursor-pointer flex-col items-center justify-center gap-1 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
          style="border-color: var(--border-default); color: var(--text-tertiary);"
        >
          <BaseIcon name="Plus" :size="18" />
          <span class="text-xs">{{ items.length }}/9</span>
          <input type="file" accept="image/*" multiple class="hidden" @change="onFileChange" />
        </label>
      </div>

      <label
        v-else
        class="flex cursor-pointer items-center gap-2 rounded-xl border border-dashed px-3 py-3 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
        style="border-color: var(--border-default); color: var(--text-tertiary);"
      >
        <BaseIcon name="ImagePlus" :size="18" class="text-primary-500" />
        <span class="text-xs">上传图片，最多 9 张</span>
        <input type="file" accept="image/*" multiple class="hidden" @change="onFileChange" />
      </label>
    </div>

    <div class="flex justify-end">
      <button
        type="button"
        :disabled="!canSubmit"
        class="px-5 py-2 rounded-lg text-sm font-medium text-white transition-all duration-200 flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
        :class="canSubmit ? 'bg-primary-500 hover:bg-primary-600 hover:shadow-md active:scale-[0.97]' : 'bg-gray-400'"
        @click="handleSubmit"
      >
        <BaseIcon v-if="isUploading" name="Loader2" :size="16" class="animate-spin" />
        <BaseIcon v-else :name="mode === 'review' ? 'Send' : 'CornerDownLeft'" :size="16" />
        {{ isUploading ? '图片上传中...' : submitLabel }}
      </button>
    </div>
  </div>
</template>
