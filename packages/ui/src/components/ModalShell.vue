<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import type { StyleValue } from 'vue'

const props = withDefaults(defineProps<{
  show: boolean
  ariaLabel?: string
  closeOnBackdrop?: boolean
  closeOnEscape?: boolean
  wrapperStyle?: StyleValue
  overlayStyle?: StyleValue
  panelStyle?: StyleValue
  headerStyle?: StyleValue
  bodyStyle?: StyleValue
  footerStyle?: StyleValue
  transitionName?: string
}>(), {
  ariaLabel: '对话框',
  closeOnBackdrop: true,
  closeOnEscape: true,
  transitionName: 'shared-modal',
})

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'close'): void
  (e: 'backdrop'): void
}>()

function close() {
  emit('update:show', false)
  emit('close')
}

function handleBackdropClick() {
  emit('backdrop')
  if (props.closeOnBackdrop) {
    close()
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (!props.show || !props.closeOnEscape) {
    return
  }

  if (event.key === 'Escape') {
    close()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition :name="props.transitionName">
      <div v-if="props.show" class="shared-modal" :style="props.wrapperStyle">
        <div class="shared-modal__backdrop" :style="props.overlayStyle" @click="handleBackdropClick" />
        <section
          class="shared-modal__panel"
          role="dialog"
          aria-modal="true"
          :aria-label="props.ariaLabel"
          :style="props.panelStyle"
        >
          <header v-if="$slots.header" class="shared-modal__header" :style="props.headerStyle">
            <slot name="header" />
          </header>
          <div class="shared-modal__body" :style="props.bodyStyle">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="shared-modal__footer" :style="props.footerStyle">
            <slot name="footer" />
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.shared-modal {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: grid;
  place-items: center;
  padding: 1.5rem;
}

.shared-modal__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(8, 12, 9, 0.48);
  backdrop-filter: blur(6px);
}

.shared-modal__panel {
  position: relative;
  width: min(520px, 100%);
  max-height: min(90vh, 100%);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  background: var(--modal-bg, var(--bg-card, var(--bg-surface, #fff)));
  border: 1px solid var(--modal-border, var(--border-default, var(--border, rgba(0, 0, 0, 0.08))));
  box-shadow: var(--modal-shadow, var(--shadow-card, var(--shadow, 0 20px 50px rgba(0, 0, 0, 0.15))));
}

.shared-modal__body {
  min-height: 0;
}

.shared-modal-enter-active,
.shared-modal-leave-active {
  transition: opacity 0.2s ease;
}

.shared-modal-enter-active .shared-modal__panel,
.shared-modal-leave-active .shared-modal__panel {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.shared-modal-enter-from,
.shared-modal-leave-to {
  opacity: 0;
}

.shared-modal-enter-from .shared-modal__panel,
.shared-modal-leave-to .shared-modal__panel {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>
