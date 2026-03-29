<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import type { AiConversationSummary } from '../../types/ai'

const props = defineProps<{
  conversations: AiConversationSummary[]
  activeConversationId?: string
  loading?: boolean
}>()

const emit = defineEmits<{
  (event: 'select', id: string | number): void
  (event: 'create'): void
}>()

function formatTime(value: string) {
  const date = new Date(value)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<template>
  <aside class="flex h-full flex-col rounded-[28px] border" style="border-color: var(--border-card); background-color: var(--bg-card);">
    <div class="flex items-center justify-between border-b px-4 py-4" style="border-color: var(--border-default);">
      <div>
        <p class="text-xs uppercase tracking-[0.22em] text-primary-500">TrailQuest AI</p>
        <h2 class="mt-1 text-sm font-semibold sm:text-base" style="color: var(--text-primary);">会话</h2>
      </div>
      <button
        @click="emit('create')"
        class="flex h-10 w-10 items-center justify-center rounded-2xl border transition-colors hover:bg-primary-500/5"
        style="border-color: var(--border-default); color: var(--text-primary);"
        title="新建对话"
      >
        <BaseIcon name="Plus" :size="18" />
      </button>
    </div>

    <div class="flex-1 overflow-y-auto px-3 py-3">
      <div v-if="props.loading" class="space-y-2">
        <div v-for="item in 4" :key="item" class="h-20 animate-pulse rounded-2xl" style="background-color: var(--bg-tag);" />
      </div>
      <div v-else-if="props.conversations.length === 0" class="flex h-full flex-col items-center justify-center px-4 text-center">
        <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl border" style="border-color: var(--border-default); color: var(--text-secondary);">
          <BaseIcon name="MessageSquareDashed" :size="22" />
        </div>
        <p class="text-sm font-medium" style="color: var(--text-primary);">还没有对话</p>
        <p class="mt-1 text-xs leading-5" style="color: var(--text-secondary);">新建一个会话，我们就可以开始帮你找路线了。</p>
      </div>
      <div v-else class="space-y-2">
        <button
          v-for="conversation in props.conversations"
          :key="conversation.id"
          @click="emit('select', conversation.id)"
          class="w-full rounded-2xl border px-3 py-3 text-left transition-all duration-200"
          :style="String(conversation.id) === props.activeConversationId
            ? 'border-color: var(--color-primary-400); background: color-mix(in srgb, var(--color-primary-500) 8%, var(--bg-card));'
            : 'border-color: var(--border-default); background-color: var(--bg-page);'"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0 flex-1">
              <p class="line-clamp-1 text-sm font-semibold" style="color: var(--text-primary);">{{ conversation.title }}</p>
              <p class="mt-1 line-clamp-2 text-xs leading-5" style="color: var(--text-secondary);">{{ conversation.preview }}</p>
            </div>
            <span class="shrink-0 text-[10px]" style="color: var(--text-tertiary);">{{ formatTime(conversation.updatedAt) }}</span>
          </div>
        </button>
      </div>
    </div>
  </aside>
</template>
