<script setup lang="ts">
import BaseIcon from './BaseIcon.vue'
import { useFlashStore } from '../../stores/useFlashStore'

const flashStore = useFlashStore()
</script>

<template>
  <transition name="flash-toast">
    <div
      v-if="flashStore.message"
      class="fixed left-1/2 top-5 z-[70] -translate-x-1/2 px-4 py-3 rounded-2xl shadow-lg border backdrop-blur-md flex items-center gap-2 min-w-[220px] max-w-[calc(100vw-2rem)]"
      :style="flashStore.message.type === 'success'
        ? 'background-color: color-mix(in srgb, var(--bg-card) 88%, white 12%); border-color: color-mix(in srgb, var(--primary-500) 22%, transparent); color: var(--text-primary);'
        : 'background-color: color-mix(in srgb, var(--bg-card) 90%, white 10%); border-color: color-mix(in srgb, var(--color-hard) 22%, transparent); color: var(--text-primary);'"
    >
      <BaseIcon
        :name="flashStore.message.type === 'success' ? 'CircleCheck' : 'AlertCircle'"
        :size="18"
        :class="flashStore.message.type === 'success' ? 'text-primary-500' : 'text-[var(--color-hard)]'"
      />
      <span class="text-sm font-medium leading-5">{{ flashStore.message.message }}</span>
    </div>
  </transition>
</template>

<style scoped>
.flash-toast-enter-active,
.flash-toast-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.flash-toast-enter-from,
.flash-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}
</style>
