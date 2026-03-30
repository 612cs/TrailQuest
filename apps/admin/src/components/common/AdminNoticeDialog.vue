<script setup lang="ts">
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
  <Teleport to="body">
    <Transition name="admin-dialog">
      <div v-if="props.show" class="admin-dialog">
        <div class="admin-dialog__backdrop" @click="handleClose" />
        <section class="admin-dialog__panel admin-card" role="dialog" aria-modal="true" :aria-label="props.title">
          <header class="admin-dialog__header">
            <h3>{{ props.title }}</h3>
          </header>
          <div class="admin-dialog__body">
            <p>{{ props.message }}</p>
          </div>
          <footer class="admin-dialog__footer">
            <button class="admin-button admin-button-primary" type="button" @click="handleClose">
              {{ props.buttonText }}
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
  width: min(460px, 100%);
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
