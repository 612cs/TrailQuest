<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import BaseIcon from './BaseIcon.vue'

const props = defineProps<{
  images: string[]
  initialIndex?: number
}>()

const emit = defineEmits<{
  close: []
}>()

const currentIndex = ref(props.initialIndex ?? 0)

function next() {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  }
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') emit('close')
  if (e.key === 'ArrowRight') next()
  if (e.key === 'ArrowLeft') prev()
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
  document.body.style.overflow = 'hidden'
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})

watch(() => props.initialIndex, (val) => {
  if (val !== undefined) currentIndex.value = val
})
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-[100] flex items-center justify-center" @click.self="emit('close')">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="emit('close')" />

      <!-- Close Button -->
      <button
        @click="emit('close')"
        class="absolute top-4 right-4 z-10 w-10 h-10 rounded-full bg-white/10 backdrop-blur text-white flex items-center justify-center hover:bg-white/20 transition-colors"
      >
        <BaseIcon name="X" :size="22" />
      </button>

      <!-- Counter -->
      <div class="absolute top-4 left-4 z-10 px-3 py-1.5 rounded-full bg-white/10 backdrop-blur text-white text-sm font-medium">
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>

      <!-- Prev Button -->
      <button
        v-if="currentIndex > 0"
        @click="prev"
        class="absolute left-3 z-10 w-10 h-10 rounded-full bg-white/10 backdrop-blur text-white flex items-center justify-center hover:bg-white/20 transition-colors"
      >
        <BaseIcon name="ChevronLeft" :size="24" />
      </button>

      <!-- Next Button -->
      <button
        v-if="currentIndex < images.length - 1"
        @click="next"
        class="absolute right-3 z-10 w-10 h-10 rounded-full bg-white/10 backdrop-blur text-white flex items-center justify-center hover:bg-white/20 transition-colors"
      >
        <BaseIcon name="ChevronRight" :size="24" />
      </button>

      <!-- Main Image -->
      <div class="relative z-[1] max-w-[90vw] max-h-[80vh] flex items-center justify-center">
        <transition name="image-fade" mode="out-in">
          <img
            :key="currentIndex"
            :src="images[currentIndex]"
            alt=""
            class="max-w-full max-h-[80vh] object-contain rounded-lg shadow-2xl"
          />
        </transition>
      </div>

      <!-- Thumbnail Strip -->
      <div v-if="images.length > 1" class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 flex gap-2 px-4 py-2 rounded-full bg-black/40 backdrop-blur-sm">
        <button
          v-for="(src, i) in images"
          :key="i"
          @click="currentIndex = i"
          class="w-10 h-10 rounded-md overflow-hidden border-2 transition-all duration-200 shrink-0"
          :class="i === currentIndex ? 'border-white scale-110' : 'border-transparent opacity-60 hover:opacity-90'"
        >
          <img :src="src" alt="" class="w-full h-full object-cover" />
        </button>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.image-fade-enter-active,
.image-fade-leave-active {
  transition: opacity 0.2s ease;
}
.image-fade-enter-from,
.image-fade-leave-to {
  opacity: 0;
}
</style>
