<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import BaseIcon from './BaseIcon.vue'

const router = useRouter()

const BUTTON_SIZE = 56
const VIEWPORT_MARGIN = 24
const DRAG_THRESHOLD = 6

const positionX = shallowRef(0)
const positionY = shallowRef(0)
const activePointerId = shallowRef<number | null>(null)
const pointerStartX = shallowRef(0)
const pointerStartY = shallowRef(0)
const pointerOriginX = shallowRef(0)
const pointerOriginY = shallowRef(0)
const hasDragged = shallowRef(false)

const buttonStyle = computed(() => ({
  left: `${positionX.value}px`,
  top: `${positionY.value}px`,
}))

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max)
}

function syncDefaultPosition() {
  const maxX = Math.max(window.innerWidth - BUTTON_SIZE - VIEWPORT_MARGIN, VIEWPORT_MARGIN)
  const maxY = Math.max(window.innerHeight - BUTTON_SIZE - VIEWPORT_MARGIN, VIEWPORT_MARGIN)

  if (positionX.value === 0 && positionY.value === 0) {
    positionX.value = maxX
    positionY.value = maxY
    return
  }

  positionX.value = clamp(positionX.value, VIEWPORT_MARGIN, maxX)
  positionY.value = clamp(positionY.value, VIEWPORT_MARGIN, maxY)
}

function handlePointerDown(event: PointerEvent) {
  activePointerId.value = event.pointerId
  pointerStartX.value = event.clientX
  pointerStartY.value = event.clientY
  pointerOriginX.value = positionX.value
  pointerOriginY.value = positionY.value
  hasDragged.value = false
}

function handlePointerMove(event: PointerEvent) {
  if (activePointerId.value !== event.pointerId) return

  const deltaX = event.clientX - pointerStartX.value
  const deltaY = event.clientY - pointerStartY.value

  if (!hasDragged.value && Math.hypot(deltaX, deltaY) > DRAG_THRESHOLD) {
    hasDragged.value = true
  }

  if (!hasDragged.value) return

  const maxX = Math.max(window.innerWidth - BUTTON_SIZE - VIEWPORT_MARGIN, VIEWPORT_MARGIN)
  const maxY = Math.max(window.innerHeight - BUTTON_SIZE - VIEWPORT_MARGIN, VIEWPORT_MARGIN)

  positionX.value = clamp(pointerOriginX.value + deltaX, VIEWPORT_MARGIN, maxX)
  positionY.value = clamp(pointerOriginY.value + deltaY, VIEWPORT_MARGIN, maxY)
}

function stopDragging(event?: PointerEvent) {
  if (event && activePointerId.value !== event.pointerId) return
  activePointerId.value = null
}

function handleClick(event: MouseEvent) {
  if (hasDragged.value) {
    event.preventDefault()
    event.stopPropagation()
    hasDragged.value = false
    return
  }

  void router.push('/chat')
}

onMounted(() => {
  syncDefaultPosition()
  window.addEventListener('resize', syncDefaultPosition)
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', stopDragging)
  window.addEventListener('pointercancel', stopDragging)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncDefaultPosition)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', stopDragging)
  window.removeEventListener('pointercancel', stopDragging)
})
</script>

<template>
  <button
    type="button"
    class="fixed z-40 w-14 h-14 rounded-full bg-primary-500 text-white flex items-center justify-center shadow-lg hover:bg-primary-600 hover:shadow-xl transition-colors duration-200 touch-none select-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-300/70"
    :style="buttonStyle"
    aria-label="打开 AI 助手"
    @pointerdown="handlePointerDown"
    @click="handleClick"
  >
    <BaseIcon name="MessageSquare" :size="24" />
  </button>
</template>
