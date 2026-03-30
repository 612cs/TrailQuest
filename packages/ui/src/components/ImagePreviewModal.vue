<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ChevronLeft, ChevronRight, X } from 'lucide-vue-next'

const props = defineProps<{
  images: string[]
  initialIndex?: number
  show?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'close'): void
}>()

const currentIndex = ref(props.initialIndex ?? 0)
const isVisible = ref(Boolean(props.show))
const closeTimer = ref<number | null>(null)
const isControlled = computed(() => typeof props.show === 'boolean')
const hasMultiple = computed(() => props.images.length > 1)
const previousBodyOverflow = ref('')

function syncBodyOverflow(visible: boolean) {
  if (typeof document === 'undefined') {
    return
  }

  if (visible) {
    previousBodyOverflow.value = document.body.style.overflow
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = previousBodyOverflow.value
  }
}

function openPreview() {
  if (closeTimer.value != null) {
    window.clearTimeout(closeTimer.value)
    closeTimer.value = null
  }
  isVisible.value = true
  syncBodyOverflow(true)
}

function closePreview() {
  isVisible.value = false
  syncBodyOverflow(false)
  emit('update:show', false)

  if (closeTimer.value != null) {
    window.clearTimeout(closeTimer.value)
  }

  closeTimer.value = window.setTimeout(() => {
    emit('close')
  }, 300)
}

function next() {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value += 1
  }
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1
  }
}

function onKeydown(event: KeyboardEvent) {
  if (!isVisible.value) {
    return
  }

  if (event.key === 'Escape') {
    closePreview()
  } else if (event.key === 'ArrowRight') {
    next()
  } else if (event.key === 'ArrowLeft') {
    prev()
  }
}

watch(
  () => props.initialIndex,
  (value) => {
    if (typeof value === 'number') {
      currentIndex.value = value
    }
  },
)

watch(
  () => props.show,
  (show) => {
    if (!isControlled.value) {
      return
    }

    if (show) {
      currentIndex.value = props.initialIndex ?? 0
      openPreview()
      return
    }

    isVisible.value = false
    syncBodyOverflow(false)
  },
  { immediate: true },
)

onMounted(() => {
  if (!isControlled.value) {
    openPreview()
  }

  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  syncBodyOverflow(false)

  if (closeTimer.value != null) {
    window.clearTimeout(closeTimer.value)
  }
})
</script>

<template>
  <Teleport to="body">
    <transition name="modal-fade" appear>
      <div
        v-if="isVisible"
        class="fixed inset-0 z-[100] flex items-center justify-center"
        @click.self="closePreview"
      >
        <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="closePreview" />

        <button
          type="button"
          class="absolute top-4 right-4 z-10 flex h-10 w-10 items-center justify-center rounded-full bg-white/10 text-white backdrop-blur transition-colors hover:bg-white/20"
          @click="closePreview"
        >
          <X :size="22" :stroke-width="2" />
        </button>

        <div class="absolute top-4 left-4 z-10 rounded-full bg-white/10 px-3 py-1.5 text-sm font-medium text-white backdrop-blur">
          {{ currentIndex + 1 }} / {{ images.length }}
        </div>

        <button
          v-if="currentIndex > 0"
          type="button"
          class="absolute left-3 z-10 flex h-10 w-10 items-center justify-center rounded-full bg-white/10 text-white backdrop-blur transition-colors hover:bg-white/20"
          @click="prev"
        >
          <ChevronLeft :size="24" :stroke-width="2" />
        </button>

        <button
          v-if="currentIndex < images.length - 1"
          type="button"
          class="absolute right-3 z-10 flex h-10 w-10 items-center justify-center rounded-full bg-white/10 text-white backdrop-blur transition-colors hover:bg-white/20"
          @click="next"
        >
          <ChevronRight :size="24" :stroke-width="2" />
        </button>

        <div class="relative z-[1] flex max-h-[80vh] max-w-[90vw] items-center justify-center">
          <transition name="image-fade" mode="out-in">
            <img
              :key="currentIndex"
              :src="images[currentIndex]"
              alt=""
              class="max-h-[80vh] max-w-full rounded-lg object-contain shadow-2xl"
            />
          </transition>
        </div>

        <div
          v-if="hasMultiple"
          class="absolute bottom-6 left-1/2 z-10 flex -translate-x-1/2 gap-2 rounded-full bg-black/40 px-4 py-2 backdrop-blur-sm"
        >
          <button
            v-for="(src, index) in images"
            :key="`${src}-${index}`"
            type="button"
            class="h-10 w-10 shrink-0 overflow-hidden rounded-md border-2 transition-all duration-200"
            :class="index === currentIndex ? 'scale-110 border-white' : 'border-transparent opacity-60 hover:opacity-90'"
            @click="currentIndex = index"
          >
            <img :src="src" alt="" class="h-full w-full object-cover" />
          </button>
        </div>
      </div>
    </transition>
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

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.2, 0, 0, 1);
  transform-origin: center center;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
</style>
