<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import BaseIcon from './BaseIcon.vue'

const props = defineProps<{
  show: boolean
  title: string
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'close'): void
}>()

const close = () => {
  emit('update:show', false)
  emit('close')
}

// Handle Escape key to close
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.show) {
    close()
  }
}

// Prevent background scrolling when modal is open
watch(() => props.show, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center px-4 pointer-events-none">
        <!-- Transparent Backdrop to catch clicks without darkening -->
        <div class="absolute inset-0 pointer-events-auto" @click="close"></div>
        
        <!-- Modal Content -->
        <div class="relative w-full max-w-md bg-white rounded-2xl shadow-[0_20px_50px_rgba(0,0,0,0.15)] overflow-hidden flex flex-col max-h-[90vh] pointer-events-auto">
          <!-- Header -->
          <div class="px-6 py-4 flex items-center justify-between border-b" style="border-color: var(--border-default);">
            <h2 class="text-xl font-bold" style="color: var(--text-primary);">{{ title }}</h2>
            <button @click="close" class="p-2 -mr-2 rounded-full hover:bg-gray-100 transition-colors" style="color: var(--text-tertiary);">
              <BaseIcon name="X" :size="20" />
            </button>
          </div>
          
          <!-- Body -->
          <div class="p-6 overflow-y-auto">
            <slot></slot>
          </div>
          
          <!-- Footer -->
          <div v-if="$slots.footer" class="px-6 py-4 border-t" style="background-color: var(--bg-tag); border-color: var(--border-default);">
            <slot name="footer"></slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active > div:nth-child(2),
.fade-leave-active > div:nth-child(2) {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-enter-from > div:nth-child(2) {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.fade-leave-to > div:nth-child(2) {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>
