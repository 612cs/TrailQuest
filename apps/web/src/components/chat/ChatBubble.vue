<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import ChatRouteRecommendations from './ChatRouteRecommendations.vue'
import type { AiMessage } from '../../types/ai'

const props = defineProps<{
  message: AiMessage
}>()

const emit = defineEmits<{
  (event: 'follow-up', text: string): void
}>()

function formatTime(timestamp: string): string {
  const d = new Date(timestamp)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="flex max-w-[92%] gap-3 animate-fade-in-up" :class="message.role === 'user' ? 'ml-auto flex-row-reverse' : ''">
    <div
      class="mt-1 flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border"
      :style="message.role === 'user'
        ? 'border-color: color-mix(in srgb, var(--color-primary-500) 24%, transparent); background-color: var(--color-primary-500); color: white;'
        : 'border-color: var(--border-default); background-color: color-mix(in srgb, var(--color-primary-500) 8%, var(--bg-card)); color: var(--color-primary-500);'"
    >
      <BaseIcon :name="message.role === 'user' ? 'User' : 'Leaf'" :size="17" />
    </div>

    <div class="min-w-0 space-y-2">
      <div
        class="rounded-[28px] px-4 py-3 text-sm leading-7 sm:px-5"
        :style="message.role === 'user'
          ? 'background-color: var(--color-primary-500); color: white;'
          : 'border: 1px solid var(--border-card); background-color: var(--bg-card); color: var(--text-primary);'"
      >
        <p class="whitespace-pre-wrap break-words">{{ message.content || (message.isStreaming ? '正在整理路线建议...' : '') }}</p>
        <span
          v-if="message.isStreaming"
          class="ml-1 inline-flex items-center gap-1 align-middle"
          style="color: var(--color-primary-500);"
        >
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current" style="animation-delay: 0ms;" />
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current" style="animation-delay: 120ms;" />
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-current" style="animation-delay: 240ms;" />
        </span>

        <ChatRouteRecommendations v-if="message.role === 'assistant'" :items="message.trailCards" />

        <div v-if="message.role === 'assistant' && message.followUps.length > 0" class="mt-3 flex flex-wrap gap-2 border-t pt-3" style="border-color: var(--border-default);">
          <button
            v-for="followUp in message.followUps"
            :key="followUp.text"
            @click="emit('follow-up', followUp.text)"
            class="rounded-full border px-3 py-1.5 text-xs transition-colors hover:bg-primary-500/5 sm:text-sm"
            style="border-color: var(--border-default); color: var(--text-secondary);"
          >
            {{ followUp.text }}
          </button>
        </div>
      </div>
      <p class="px-2 text-[11px]" :class="message.role === 'user' ? 'text-right' : ''" style="color: var(--text-tertiary);">
        {{ formatTime(message.createdAt) }}
      </p>
    </div>
  </div>
</template>
