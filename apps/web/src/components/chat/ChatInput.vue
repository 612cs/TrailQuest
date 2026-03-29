<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'

const emit = defineEmits<{
  send: [text: string]
}>()

defineProps<{
  disabled?: boolean
  placeholder?: string
}>()

const text = ref('')

function handleSend() {
  if (!text.value.trim()) return
  emit('send', text.value)
  text.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="border-t px-4 py-3" style="border-color: var(--border-default); background-color: var(--bg-card);">
    <div class="mx-auto max-w-4xl">
      <div class="flex items-end gap-3 rounded-[26px] border px-3 py-2.5 sm:px-4" style="border-color: var(--border-card); background-color: var(--bg-page);">
        <textarea
          v-model="text"
          @keydown="handleKeydown"
          :disabled="disabled"
          rows="1"
          :placeholder="placeholder || '描述你想去哪里、想走多久，或者直接问我装备和天气建议...'"
          class="min-h-[68px] w-full resize-none bg-transparent px-2 py-1.5 text-sm leading-6 transition-colors focus:outline-none sm:text-[15px]"
          style="
            color: var(--text-primary);
          "
        />
        <button
          @click="handleSend"
          :disabled="!text.trim() || disabled"
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-40"
          :style="text.trim() && !disabled
            ? 'border-color: var(--color-primary-500); background-color: var(--color-primary-500); color: white;'
            : 'border-color: var(--border-default); background-color: var(--bg-card); color: var(--text-tertiary);'"
        >
          <BaseIcon name="Send" :size="18" />
        </button>
      </div>
      <div class="mt-2 flex items-center justify-between px-1 text-[11px] sm:text-xs" style="color: var(--text-tertiary);">
        <span>Enter 发送，Shift + Enter 换行</span>
        <span>仅基于 TrailQuest 内部路线库生成推荐</span>
      </div>
    </div>
  </div>
</template>
