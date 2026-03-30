<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ChevronLeft, ChevronRight, X } from 'lucide-vue-next'

import ModalShell from './ModalShell.vue'

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
const isVisible = computed(() => props.show ?? true)
const hasImages = computed(() => props.images.length > 0)
const activeImage = computed(() => props.images[currentIndex.value] ?? '')
const counterLabel = computed(() => `${currentIndex.value + 1} / ${props.images.length}`)
const previousBodyOverflow = ref('')

watch(
  () => [props.initialIndex, props.images],
  () => {
    currentIndex.value = clampIndex(props.initialIndex ?? 0)
  },
  { deep: true },
)

watch(
  () => props.show,
  (show) => {
    if (show !== false) {
      currentIndex.value = clampIndex(props.initialIndex ?? 0)
    }
  },
)

watch(
  isVisible,
  (visible) => {
    if (typeof document === 'undefined') {
      return
    }

    if (visible) {
      previousBodyOverflow.value = document.body.style.overflow
      document.body.style.overflow = 'hidden'
      return
    }

    document.body.style.overflow = previousBodyOverflow.value
  },
  { immediate: true },
)

function clampIndex(index: number) {
  if (!props.images.length) {
    return 0
  }
  return Math.min(Math.max(index, 0), props.images.length - 1)
}

function close() {
  emit('update:show', false)
  emit('close')
}

function goPrevious() {
  currentIndex.value = (currentIndex.value - 1 + props.images.length) % props.images.length
}

function goNext() {
  currentIndex.value = (currentIndex.value + 1) % props.images.length
}

function handleKeydown(event: KeyboardEvent) {
  if (!isVisible.value || !hasImages.value) {
    return
  }
  if (event.key === 'ArrowLeft') {
    goPrevious()
  } else if (event.key === 'ArrowRight') {
    goNext()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (typeof document !== 'undefined') {
    document.body.style.overflow = previousBodyOverflow.value
  }
})
</script>

<template>
  <ModalShell
    v-if="hasImages"
    :show="isVisible"
    aria-label="图片预览"
    :panel-style="{ width: 'min(1120px, 100%)', maxHeight: 'min(92vh, 100%)', borderRadius: '28px' }"
    :body-style="{ padding: '0' }"
    :overlay-style="{ background: 'rgba(6, 8, 7, 0.78)', backdropFilter: 'blur(14px)' }"
    @update:show="emit('update:show', $event)"
    @close="emit('close')"
  >
    <div class="shared-image-preview">
      <div class="shared-image-preview__toolbar">
        <span class="shared-image-preview__counter">{{ counterLabel }}</span>
        <button class="shared-image-preview__icon" type="button" @click="close">
          <X :size="18" :stroke-width="2" />
        </button>
      </div>

      <div class="shared-image-preview__viewport">
        <button v-if="props.images.length > 1" class="shared-image-preview__nav" type="button" @click="goPrevious">
          <ChevronLeft :size="24" :stroke-width="2" />
        </button>

        <img class="shared-image-preview__image" :src="activeImage" :alt="`预览图片 ${currentIndex + 1}`" />

        <button v-if="props.images.length > 1" class="shared-image-preview__nav" type="button" @click="goNext">
          <ChevronRight :size="24" :stroke-width="2" />
        </button>
      </div>

      <div v-if="props.images.length > 1" class="shared-image-preview__thumbs">
        <button
          v-for="(image, index) in props.images"
          :key="`${image}-${index}`"
          class="shared-image-preview__thumb"
          :class="{ 'shared-image-preview__thumb--active': index === currentIndex }"
          type="button"
          @click="currentIndex = index"
        >
          <img :src="image" :alt="`缩略图 ${index + 1}`" />
        </button>
      </div>
    </div>
  </ModalShell>
</template>

<style scoped>
.shared-image-preview {
  display: grid;
  gap: 1rem;
  padding: 1rem;
  background: color-mix(in srgb, var(--bg-card, var(--bg-surface, #101310)) 88%, #000);
}

.shared-image-preview__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.shared-image-preview__counter {
  color: var(--text-primary, var(--text-strong, #f8fafc));
  font-size: 0.95rem;
  font-weight: 600;
}

.shared-image-preview__icon,
.shared-image-preview__nav,
.shared-image-preview__thumb {
  border: 0;
  cursor: pointer;
}

.shared-image-preview__icon {
  width: 2.5rem;
  height: 2.5rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: color-mix(in srgb, var(--bg-card, var(--bg-surface, #101310)) 78%, #000);
  color: var(--text-primary, var(--text-strong, #f8fafc));
}

.shared-image-preview__viewport {
  min-height: min(62vh, 680px);
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 1rem;
}

.shared-image-preview__nav {
  width: 3rem;
  height: 3rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: color-mix(in srgb, var(--bg-card, var(--bg-surface, #101310)) 72%, #000);
  color: var(--text-primary, var(--text-strong, #f8fafc));
}

.shared-image-preview__image {
  width: 100%;
  max-height: min(68vh, 760px);
  object-fit: contain;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.04);
}

.shared-image-preview__thumbs {
  display: flex;
  gap: 0.75rem;
  overflow-x: auto;
  padding-bottom: 0.15rem;
}

.shared-image-preview__thumb {
  padding: 0;
  width: 5.5rem;
  height: 5.5rem;
  flex: 0 0 auto;
  border-radius: 18px;
  overflow: hidden;
  border: 2px solid transparent;
  background: transparent;
  opacity: 0.72;
}

.shared-image-preview__thumb--active {
  border-color: var(--color-primary-500, var(--primary, #2d5927));
  opacity: 1;
}

.shared-image-preview__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 720px) {
  .shared-image-preview {
    padding: 0.85rem;
  }

  .shared-image-preview__viewport {
    min-height: min(52vh, 540px);
    grid-template-columns: minmax(0, 1fr);
  }

  .shared-image-preview__nav {
    display: none;
  }
}
</style>
