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
    :header-style="{ padding: '1.2rem 1.2rem 0', display: 'flex', alignItems: 'center' }"
    :body-style="{ padding: '0.8rem 1.2rem 0' }"
    :footer-style="{ padding: '1rem 1.2rem 1.2rem' }"
    @update:show="emit('update:show', $event)"
  >
    <template #header>
      <div class="admin-dialog__header">
        <h3>{{ props.title }}</h3>
      </div>
    </template>

    <div class="admin-dialog__body">
      <p>{{ props.message }}</p>

      <label v-if="props.requireReason" class="admin-dialog__field">
        <span>{{ props.reasonLabel }}</span>
        <textarea
          v-model="reason"
          class="admin-textarea"
          rows="4"
          :placeholder="props.reasonPlaceholder"
        />
      </label>

      <p v-if="localError" class="admin-dialog__error">{{ localError }}</p>
    </div>

    <template #footer>
      <div class="admin-dialog__footer">
        <button class="admin-button admin-button-secondary" type="button" :disabled="props.loading" @click="close">
          {{ props.cancelText }}
        </button>
        <button class="admin-button admin-button-primary" type="button" :disabled="confirmDisabled" @click="handleConfirm">
          {{ props.loading ? '处理中...' : props.confirmText }}
        </button>
      </div>
    </template>
  </ModalShell>
</template>

<style scoped>
.admin-dialog__header h3 {
  margin: 0;
  color: var(--text-strong);
  font-size: 1.12rem;
}

.admin-dialog__body {
  margin-top: 0.8rem;
  display: grid;
  gap: 0.9rem;
}

.admin-dialog__body p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.admin-dialog__field {
  display: grid;
  gap: 0.5rem;
}

.admin-dialog__field span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-dialog__error {
  color: var(--danger);
}

.admin-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
</style>
