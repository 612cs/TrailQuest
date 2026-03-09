<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import type { ChatMessage } from '../../mock/mockData'

defineProps<{
  message: ChatMessage
}>()

function formatTime(timestamp: number): string {
  const d = new Date(timestamp)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<template>
  <div
    class="flex gap-3 max-w-[85%] animate-fade-in-up"
    :class="message.role === 'user' ? 'ml-auto flex-row-reverse' : ''"
  >
    <!-- Avatar -->
    <div
      class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-white"
      :class="message.role === 'user' ? 'bg-primary-500' : 'bg-gradient-to-br from-violet-500 to-fuchsia-500'"
    >
      <BaseIcon :name="message.role === 'user' ? 'User' : 'Sparkles'" :size="16" />
    </div>

    <!-- Bubble -->
    <div class="space-y-1">
      <div
        class="px-4 py-2.5 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap"
        :class="
          message.role === 'user'
            ? 'rounded-br-md bg-primary-500 text-white'
            : 'rounded-bl-md'
        "
        :style="message.role === 'assistant' ? 'background-color: var(--bg-card); color: var(--text-primary); border: 1px solid var(--border-card);' : ''"
      >
        {{ message.content }}<span v-if="message.role === 'assistant' && message.content && !message.content.endsWith('。') && !message.content.endsWith('.') && !message.content.endsWith('\n')" class="inline-block w-0.5 h-4 bg-primary-500 ml-0.5 animate-pulse align-text-bottom" />
      </div>
      <p
        class="text-[10px] px-1"
        :class="message.role === 'user' ? 'text-right' : ''"
        style="color: var(--text-tertiary);"
      >
        {{ formatTime(message.timestamp) }}
      </p>
    </div>
  </div>
</template>
