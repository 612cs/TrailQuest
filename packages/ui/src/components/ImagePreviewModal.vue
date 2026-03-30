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
        class="image-preview"
        @click.self="closePreview"
      >
        <div class="image-preview__backdrop" @click="closePreview" />

        <button
          type="button"
          class="image-preview__close"
          @click="closePreview"
        >
          <X :size="22" :stroke-width="2" />
        </button>

        <div class="image-preview__counter">
          {{ currentIndex + 1 }} / {{ images.length }}
        </div>

        <button
          v-if="currentIndex > 0"
          type="button"
          class="image-preview__nav image-preview__nav--prev"
          @click="prev"
        >
          <ChevronLeft :size="24" :stroke-width="2" />
        </button>

        <button
          v-if="currentIndex < images.length - 1"
          type="button"
          class="image-preview__nav image-preview__nav--next"
          @click="next"
        >
          <ChevronRight :size="24" :stroke-width="2" />
        </button>

        <div class="image-preview__stage">
          <transition name="image-fade" mode="out-in">
            <img
              :key="currentIndex"
              :src="images[currentIndex]"
              alt=""
              class="image-preview__image"
            />
          </transition>
        </div>

        <div
          v-if="hasMultiple"
          class="image-preview__thumbs"
        >
          <button
            v-for="(src, index) in images"
            :key="`${src}-${index}`"
            type="button"
            class="image-preview__thumb"
            :class="{ 'image-preview__thumb--active': index === currentIndex }"
            @click="currentIndex = index"
          >
            <img :src="src" alt="" class="image-preview__thumb-image" />
          </button>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.image-preview {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview__backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(6px);
}

.image-preview__close,
.image-preview__nav {
  position: absolute;
  z-index: 10;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.image-preview__close:hover,
.image-preview__nav:hover {
  background: rgba(255, 255, 255, 0.2);
}

.image-preview__close {
  top: 1rem;
  right: 1rem;
}

.image-preview__counter {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 10;
  padding: 0.375rem 0.75rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.image-preview__nav--prev {
  left: 0.75rem;
}

.image-preview__nav--next {
  right: 0.75rem;
}

.image-preview__stage {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 90vw;
  max-height: 80vh;
}

.image-preview__image {
  max-width: 100%;
  max-height: 80vh;
  border-radius: 0.5rem;
  object-fit: contain;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
}

.image-preview__thumbs {
  position: absolute;
  left: 50%;
  bottom: 1.5rem;
  z-index: 10;
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  transform: translateX(-50%);
}

.image-preview__thumb {
  width: 2.5rem;
  height: 2.5rem;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 0.375rem;
  overflow: hidden;
  background: transparent;
  opacity: 0.6;
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease, border-color 0.2s ease;
  flex-shrink: 0;
}

.image-preview__thumb:hover {
  opacity: 0.9;
}

.image-preview__thumb--active {
  opacity: 1;
  border-color: #fff;
  transform: scale(1.1);
}

.image-preview__thumb-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

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

@media (max-width: 640px) {
  .image-preview__thumbs {
    max-width: calc(100vw - 2rem);
    overflow-x: auto;
  }
}
</style>
