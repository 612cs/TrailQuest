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
    :header-style="{ padding: '1.2rem 1.2rem 0', display: 'flex', alignItems: 'center' }"
    :body-style="{ padding: '0.8rem 1.2rem 0' }"
    :footer-style="{ padding: '1rem 1.2rem 1.2rem' }"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <template #header>
      <div class="admin-dialog__header">
        <h3>{{ props.title }}</h3>
      </div>
    </template>

    <div class="admin-dialog__body">
      <p>{{ props.message }}</p>
    </div>

    <template #footer>
      <div class="admin-dialog__footer">
        <button class="admin-button admin-button-primary" type="button" @click="handleClose">
          {{ props.buttonText }}
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
  padding: 0.9rem 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.admin-dialog__body p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.admin-dialog__footer {
  display: flex;
  justify-content: flex-end;
}
</style>
