<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import ModalShell from '@trailquest/ui/components/ModalShell.vue'

const props = withDefaults(defineProps<{
  show: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  requireReason?: boolean
  reasonLabel?: string
  reasonPlaceholder?: string
  initialReason?: string
}>(), {
  confirmText: '确认',
  cancelText: '取消',
  loading: false,
  requireReason: false,
  reasonLabel: '原因',
  reasonPlaceholder: '请输入原因',
  initialReason: '',
})

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'confirm', reason: string): void
}>()

const reason = ref(props.initialReason)
const localError = ref('')
const confirmDisabled = computed(() => props.loading || (props.requireReason && !reason.value.trim()))

watch(() => props.show, (show) => {
  if (show) {
    reason.value = props.initialReason
    localError.value = ''
  }
})

watch(() => props.initialReason, (value) => {
  reason.value = value
})

function close() {
  if (props.loading) {
    return
  }
  emit('update:show', false)
}

function handleConfirm() {
  if (props.requireReason && !reason.value.trim()) {
    localError.value = `${props.reasonLabel}不能为空`
    return
  }
  localError.value = ''
  emit('confirm', reason.value.trim())
}
</script>

<template>
  <ModalShell
    :show="props.show"
    :aria-label="props.title"
    :panel-style="{ width: 'min(520px, 100%)', borderRadius: '24px' }"
    :header-style="{ padding: '1.2rem 1.5rem 0', display: 'flex', alignItems: 'center' }"
    :body-style="{ padding: '1rem 1.5rem 0' }"
    :footer-style="{ padding: '1.2rem 1.5rem 1.5rem' }"
    @update:show="emit('update:show', $event)"
  >
    <template #header>
      <div class="dialog-header">
        <h3>{{ props.title }}</h3>
      </div>
    </template>

    <div class="dialog-body">
      <p class="dialog-message">{{ props.message }}</p>

      <label v-if="props.requireReason" class="dialog-field">
        <span class="field-label">{{ props.reasonLabel }}</span>
        <textarea
          v-model="reason"
          class="styled-textarea"
          rows="4"
          :placeholder="props.reasonPlaceholder"
        />
      </label>

      <p v-if="localError" class="dialog-error">{{ localError }}</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button class="btn btn--secondary" type="button" :disabled="props.loading" @click="close">
          {{ props.cancelText }}
        </button>
        <button class="btn btn--primary" type="button" :disabled="confirmDisabled" @click="handleConfirm">
          {{ props.loading ? '处理中...' : props.confirmText }}
        </button>
      </div>
    </template>
  </ModalShell>
</template>

<style scoped>
.dialog-header h3 {
  margin: 0;
  color: var(--text-strong);
  font-size: 1.25rem;
  font-weight: 700;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.dialog-message {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.6;
  font-size: 0.9375rem;
}

.dialog-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-label {
  color: var(--text-strong);
  font-size: 0.875rem;
  font-weight: 700;
}

.styled-textarea {
  width: 100%;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-strong);
  border-radius: 12px;
  padding: 0.75rem 1rem;
  outline: none;
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
  transition: all 0.2s;
}

.styled-textarea:focus {
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.dialog-error {
  margin: 0;
  color: var(--danger);
  font-size: 0.875rem;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn {
  padding: 0.6rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn--primary {
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 10px rgba(var(--primary-rgb), 0.15);
}
.btn--primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(var(--primary-rgb), 0.25);
}

.btn--secondary {
  background: var(--bg-soft);
  color: var(--text-strong);
  border-color: var(--border);
}
.btn--secondary:hover:not(:disabled) {
  background: white;
  border-color: var(--primary-soft);
  color: var(--primary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
