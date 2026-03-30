<script setup lang="ts">
import ModalShell from '@trailquest/ui/components/ModalShell.vue'

import BaseIcon from './BaseIcon.vue'

const props = defineProps<{
  show: boolean
  title: string
  footerTone?: 'muted' | 'plain'
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'close'): void
}>()

const close = () => {
  emit('update:show', false)
  emit('close')
}
</script>

<template>
  <ModalShell
    :show="show"
    :aria-label="title"
    :wrapper-style="{ zIndex: '50', padding: '1rem', pointerEvents: 'none' }"
    :overlay-style="{ background: 'transparent', pointerEvents: 'auto' }"
    :panel-style="{ width: 'min(32rem, 100%)', maxHeight: '90vh', borderRadius: '1rem', pointerEvents: 'auto' }"
    :header-style="{ padding: '1rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-default)' }"
    :body-style="{ padding: '1.5rem', overflowY: 'auto' }"
    :footer-style="props.footerTone === 'plain'
      ? { padding: '1rem 1.5rem', borderTop: '1px solid var(--border-default)', backgroundColor: 'var(--bg-card)' }
      : { padding: '1rem 1.5rem', borderTop: '1px solid var(--border-default)', backgroundColor: 'var(--bg-tag)' }"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <template #header>
      <h2 class="text-xl font-bold" style="color: var(--text-primary);">{{ title }}</h2>
      <button @click="close" class="p-2 -mr-2 rounded-full hover:bg-primary-500/10 transition-colors" style="color: var(--text-tertiary);">
        <BaseIcon name="X" :size="20" />
      </button>
    </template>

    <slot></slot>

    <template v-if="$slots.footer" #footer>
      <slot name="footer"></slot>
    </template>
  </ModalShell>
</template>
