<script setup lang="ts">
import { computed } from 'vue'

import BaseIcon from './BaseIcon.vue'
import BaseModal from './BaseModal.vue'

const props = withDefaults(defineProps<{
  show: boolean
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  confirmLoading?: boolean
  tone?: 'danger' | 'primary'
}>(), {
  title: '请确认操作',
  confirmText: '确认',
  cancelText: '取消',
  confirmLoading: false,
  tone: 'primary',
})

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const toneIconClass = computed(() => (
  props.tone === 'danger' ? 'text-primary-500' : 'text-primary-500'
))

const toneBadgeStyle = computed(() => (
  props.tone === 'danger'
    ? 'background-color: color-mix(in srgb, var(--color-primary-500) 14%, transparent);'
    : 'background-color: color-mix(in srgb, var(--color-primary-500) 12%, transparent);'
))

const confirmButtonStyle = computed(() => (
  props.tone === 'danger'
    ? 'background-color: var(--color-primary-500); color: var(--text-inverse);'
    : 'background-color: var(--color-primary-500); color: var(--text-inverse);'
))

function handleClose() {
  emit('update:show', false)
  emit('cancel')
}

function handleConfirm() {
  emit('confirm')
}
</script>

<template>
  <BaseModal
    :show="show"
    :title="title"
    footer-tone="plain"
    @update:show="emit('update:show', $event)"
    @close="handleClose"
  >
    <div class="flex items-start gap-4" style="background-color: var(--bg-secondary); padding: 12px; border-radius: 8px;">
      <div
        class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl"
        :style="toneBadgeStyle"
      >
        <BaseIcon name="TriangleAlert" :size="20" :class="toneIconClass" />
      </div>

      <div class="space-y-2">
        <p class="text-sm leading-6" style="color: var(--text-secondary);">
          {{ message }}
        </p>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <button
          type="button"
          class="rounded-xl px-4 py-2 text-sm font-medium transition-colors hover:bg-primary-500/10 disabled:cursor-not-allowed disabled:opacity-60"
          style="color: var(--text-secondary);"
          :disabled="confirmLoading"
          @click="handleClose"
        >
          {{ cancelText }}
        </button>
        <button
          type="button"
          class="inline-flex min-w-24 items-center justify-center rounded-xl px-4 py-2 text-sm font-semibold transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
          :style="confirmButtonStyle"
          :disabled="confirmLoading"
          @click="handleConfirm"
        >
          {{ confirmLoading ? '处理中...' : confirmText }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>
