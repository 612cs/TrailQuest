<script setup lang="ts">
import { computed, ref, watch } from 'vue'

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
  <Teleport to="body">
    <Transition name="admin-dialog">
      <div v-if="props.show" class="admin-dialog">
        <div class="admin-dialog__backdrop" @click="close" />
        <section class="admin-dialog__panel admin-card" role="dialog" aria-modal="true" :aria-label="props.title">
          <header class="admin-dialog__header">
            <h3>{{ props.title }}</h3>
          </header>

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

          <footer class="admin-dialog__footer">
            <button class="admin-button admin-button-secondary" type="button" :disabled="props.loading" @click="close">
              {{ props.cancelText }}
            </button>
            <button class="admin-button admin-button-primary" type="button" :disabled="confirmDisabled" @click="handleConfirm">
              {{ props.loading ? '处理中...' : props.confirmText }}
            </button>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.admin-dialog {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: grid;
  place-items: center;
  padding: 1.5rem;
}

.admin-dialog__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(8, 12, 9, 0.48);
  backdrop-filter: blur(6px);
}

.admin-dialog__panel {
  position: relative;
  width: min(520px, 100%);
  padding: 1.2rem;
  border-radius: 24px;
}

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
  margin-top: 1rem;
}

.admin-dialog-enter-active,
.admin-dialog-leave-active {
  transition: opacity 0.2s ease;
}

.admin-dialog-enter-active .admin-dialog__panel,
.admin-dialog-leave-active .admin-dialog__panel {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.admin-dialog-enter-from,
.admin-dialog-leave-to {
  opacity: 0;
}

.admin-dialog-enter-from .admin-dialog__panel,
.admin-dialog-leave-to .admin-dialog__panel {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>
