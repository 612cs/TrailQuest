<script setup lang="ts">
import ModalShell from '@trailquest/ui/components/ModalShell.vue'

const props = withDefaults(defineProps<{
  show: boolean
  title?: string
  message: string
  buttonText?: string
}>(), {
  title: '提示',
  buttonText: '知道了',
})

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'close'): void
}>()

function handleClose() {
  emit('update:show', false)
  emit('close')
}
</script>

<template>
  <ModalShell
    :show="props.show"
    :aria-label="props.title"
    :panel-style="{ width: 'min(460px, 100%)', borderRadius: '24px' }"
    :header-style="{ padding: '1.2rem 1.5rem 0', display: 'flex', alignItems: 'center' }"
    :body-style="{ padding: '1rem 1.5rem 0' }"
    :footer-style="{ padding: '1.2rem 1.5rem 1.5rem' }"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <template #header>
      <div class="dialog-header">
        <h3>{{ props.title }}</h3>
      </div>
    </template>

    <div class="dialog-body">
      <p class="dialog-message">{{ props.message }}</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button class="btn btn--primary" type="button" @click="handleClose">
          {{ props.buttonText }}
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
  background: var(--bg-soft);
  padding: 1.25rem;
  border-radius: 16px;
}

.dialog-message {
  margin: 0;
  color: var(--text-strong);
  line-height: 1.6;
  font-size: 0.9375rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 0.6rem 1.5rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn--primary {
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 10px rgba(var(--primary-rgb), 0.15);
}
.btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(var(--primary-rgb), 0.25);
}
</style>
