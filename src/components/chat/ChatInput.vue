<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'

const emit = defineEmits<{
  send: [text: string]
}>()

defineProps<{
  disabled?: boolean
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
  <div class="px-4 py-3 border-t" style="border-color: var(--border-default); background-color: var(--bg-card);">
    <div class="max-w-3xl mx-auto flex items-end gap-2">
      <div class="flex-1 relative">
        <textarea
          v-model="text"
          @keydown="handleKeydown"
          :disabled="disabled"
          rows="1"
          placeholder="输入消息..."
          class="w-full px-4 py-2.5 pr-4 rounded-xl text-sm resize-none transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30 max-h-32"
          style="
            background-color: var(--bg-input);
            color: var(--text-primary);
            border: 1px solid var(--border-default);
          "
        />
      </div>
      <button
        @click="handleSend"
        :disabled="!text.trim() || disabled"
        class="w-10 h-10 rounded-xl flex items-center justify-center shrink-0 transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed"
        :class="
          text.trim() && !disabled
            ? 'bg-primary-500 text-white hover:bg-primary-600 active:scale-95'
            : 'bg-gray-200 text-gray-400'
        "
      >
        <BaseIcon name="Send" :size="18" />
      </button>
    </div>
  </div>
</template>
