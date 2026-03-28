<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef, watch } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'

const props = defineProps<{
  images: string[]
  title: string
}>()

const emit = defineEmits<{
  (event: 'active-change', index: number): void
  (event: 'camera-move', payload: { x: number, max: number }): void
  (event: 'preview', index: number): void
}>()

const getGsap = () => (window as any).gsap

const containerRef = useTemplateRef<HTMLDivElement>('container')
const cardRefs = ref<HTMLElement[]>([])

const viewportWidth = ref(0)
const scrollProxy = { x: 0 }
const activeIndex = ref(0)
const autoPanResumeAt = ref(0)
const AUTO_PAN_SPEED = 0.5
const AUTO_PAN_RESUME_DELAY = 2200
const MOBILE_BREAKPOINT = 768

let tickerFn: (() => void) | null = null

const isMobileLayout = computed(() => viewportWidth.value > 0 && viewportWidth.value <= MOBILE_BREAKPOINT)

const cardWidth = computed(() => {
  if (viewportWidth.value <= 0) return 560
  if (viewportWidth.value < MOBILE_BREAKPOINT) return Math.min(viewportWidth.value - 32, 460)
  if (viewportWidth.value < 1280) return Math.min(viewportWidth.value * 0.4, 480)
  return Math.min(viewportWidth.value * 0.35, 560)
})

const cardGap = computed(() => {
  if (viewportWidth.value < 768) return Math.max(28, viewportWidth.value * 0.06)
  if (viewportWidth.value < 1280) return Math.max(160, viewportWidth.value * 0.16)
  return Math.max(220, viewportWidth.value * 0.18)
})

const multiplier = computed(() => {
  if (isMobileLayout.value) return 1
  if (!props.images.length) return 1
  const setW = props.images.length * (cardWidth.value + cardGap.value)
  const viewW = viewportWidth.value || window.innerWidth || 1920
  return Math.max(5, Math.ceil((viewW * 2.5) / setW) + 1)
})

const wrappedImages = computed(() => {
  if (!props.images.length) return []
  if (isMobileLayout.value) {
    return props.images.map((image, index) => ({
      id: `mobile-img${index}`,
      image,
      originalIndex: index,
    }))
  }
  const arr = []
  for (let i = 0; i < multiplier.value; i++) {
    for (let j = 0; j < props.images.length; j++) {
      arr.push({ id: `set${i}-img${j}`, image: props.images[j], originalIndex: j })
    }
  }
  return arr
})

const statusText = computed(() => props.images.length
  ? (isMobileLayout.value ? '向上或向下滑动浏览整组图片' : '使用滚轮、触控板或方向键横向穿梭图片空间')
  : '当前路线暂无可展示图片')

watch(() => props.images, () => {
  scrollProxy.x = 0
  activeIndex.value = 0
  autoPanResumeAt.value = 0
}, { immediate: true })

watch(isMobileLayout, (mobile) => {
  if (!mobile) return
  const gsap = getGsap()
  cardRefs.value.forEach((el) => {
    if (!el) return
    gsap?.set(el, { clearProps: 'transform,x,y,xPercent,yPercent,scale,rotationY,zIndex,opacity' })
  })
})

onMounted(() => {
  measureViewport()
  window.addEventListener('resize', measureViewport)
  window.addEventListener('keydown', handleKeydown)
  
  const gsap = getGsap()
  if (gsap) {
    tickerFn = () => tick()
    gsap.ticker.add(tickerFn)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', measureViewport)
  window.removeEventListener('keydown', handleKeydown)
  
  const gsap = getGsap()
  if (gsap && tickerFn) {
    gsap.ticker.remove(tickerFn)
  }
  gsap?.killTweensOf(scrollProxy)
})

function measureViewport() {
  viewportWidth.value = containerRef.value?.clientWidth ?? window.innerWidth
}

function pauseAutoPan(delay = AUTO_PAN_RESUME_DELAY) {
  autoPanResumeAt.value = performance.now() + delay
}


function handleWheel(event: WheelEvent) {
  if (isMobileLayout.value) return
  if (!props.images.length) return
  event.preventDefault()
  pauseAutoPan()
  
  const gsap = getGsap()
  gsap?.killTweensOf(scrollProxy)
  const delta = Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : event.deltaY
  scrollProxy.x += delta * 1.5
}

function handleKeydown(event: KeyboardEvent) {
  if (isMobileLayout.value) return
  if (!props.images.length) return
  const key = event.key.toLowerCase()
  if (event.key === 'ArrowRight' || key === 'd') {
    jumpToOffset(1)
  }
  if (event.key === 'ArrowLeft' || key === 'a') {
    jumpToOffset(-1)
  }
}

function jumpToOffset(offset: number) {
  pauseAutoPan()
  const gsap = getGsap()
  if (!gsap) return
  
  const width = cardWidth.value + cardGap.value
  const targetX = scrollProxy.x + offset * width
  gsap.to(scrollProxy, {
    x: targetX,
    duration: 0.8,
    ease: 'power3.out',
    overwrite: true
  })
}


function tick() {
  const gsap = getGsap()
  if (!gsap || !cardRefs.value?.length || viewportWidth.value === 0) return
  
  const originalCount = props.images.length
  if (originalCount === 0) return

  if (isMobileLayout.value) {
    emit('camera-move', { x: 0, max: Math.max(originalCount, 1) })
    activeIndex.value = 0
    return
  }
  
  if (performance.now() >= autoPanResumeAt.value) {
    if (!gsap.isTweening(scrollProxy)) {
      scrollProxy.x += AUTO_PAN_SPEED
    }
  }

  const width = cardWidth.value
  const gap = cardGap.value
  const setW = originalCount * (width + gap)
  const totalW = multiplier.value * setW

  // Emit camera-move for background parallax in TrailGalleryView
  // Emit the raw un-wrapped value so the background can continuously slide without snapping!
  emit('camera-move', { x: scrollProxy.x, max: setW })

  let closestIndex = 0
  let minDistance = Infinity

  cardRefs.value.forEach((el, index) => {
    if (!el) return
    const cardData = wrappedImages.value[index]
    if (!cardData) return
    
    const localIndex = cardData.originalIndex
    const baseX = index * (width + gap)
    
    const unadjustedX = baseX - scrollProxy.x
    // GSAP wrap makes it loop infinitely!
    const screenX = gsap.utils.wrap(-totalW / 2, totalW / 2, unadjustedX)
    
    // distance is based on center screen Anchor
    const distance = screenX
    const normalized = distance / viewportWidth.value
    const distanceFactor = Math.min(Math.abs(normalized), 1.2)
    const focusStrength = Math.max(0, 1 - distanceFactor / 1.2)
    
    const rowOffsetBase = viewportWidth.value < 768 ? 58 : 96
    const rowOffset = localIndex % 2 === 0 ? -rowOffsetBase : rowOffsetBase
    const focusLift = -focusStrength * (viewportWidth.value < 768 ? 12 : 18)
    const driftOffset = distanceFactor * (viewportWidth.value < 768 ? 6 : 10)
    
    const scale = 0.94 + focusStrength * 0.08
    const rotateY = gsap.utils.clamp(-12, 12, normalized * -12)
    const translateY = rowOffset + focusLift + driftOffset
    
    // Position exactly at screenX and translateY, then let GSAP center the anchor with xPercent/yPercent.
    const transformX = screenX
    
    // Only render cards that are generally close to screen limits (optional optimization)
    // But setting invisible ones is essentially free in CSS3D
    gsap.set(el, {
      x: transformX,
      y: translateY,
      xPercent: -50,
      yPercent: -50,
      scale: scale,
      rotationY: rotateY,
      zIndex: 300 - Math.round(distanceFactor * 100),
      opacity: 1 // can dynamically hide cards far off screen if needed
    })

    if (Math.abs(distance) < minDistance) {
      minDistance = Math.abs(distance)
      closestIndex = localIndex
    }

    const frame = el.querySelector('.gallery-card-frame') as HTMLElement
    if (frame) {
      const glowStr = String(Math.max(0, 1 - Math.abs(normalized) * 2.8))
      if (frame.dataset.glow !== glowStr) {
         frame.style.setProperty('--glow-opacity', glowStr)
         frame.dataset.glow = glowStr
      }
      
      if (cardData.originalIndex === activeIndex.value) {
         if (!frame.classList.contains('active-glow')) frame.classList.add('active-glow')
      } else {
         if (frame.classList.contains('active-glow')) frame.classList.remove('active-glow')
      }
    }
  })

  if (activeIndex.value !== closestIndex) {
    activeIndex.value = closestIndex
    emit('active-change', closestIndex)
  }
}
</script>

<template>
  <div
    ref="container"
    class="gallery-experience"
    :class="{ 'gallery-experience-mobile': isMobileLayout }"
    @wheel.prevent="handleWheel"
  >
    <div class="gallery-background">
      <div class="gallery-noise"></div>
      <div class="gallery-gradient-orb gallery-gradient-orb-left"></div>
      <div class="gallery-gradient-orb gallery-gradient-orb-right"></div>
      <div class="gallery-floor"></div>
      <div class="gallery-grid"></div>
    </div>

    <div class="gallery-copy">
      <p class="gallery-kicker">Trail Image Voyage</p>
      <h1 class="gallery-title">{{ props.title }}</h1>
      <p class="gallery-subtitle">把整条路线的影像展开成一条横向长廊。你不是在翻页，而是在穿过它。</p>
    </div>

    <div class="gallery-viewport" :class="{ 'gallery-viewport-mobile': isMobileLayout }">
      <!-- GSAP Absolute Wrapper -->
      <div class="gallery-rail" :class="{ 'gallery-rail-mobile': isMobileLayout }">
        <article
          v-for="card in wrappedImages"
          :key="card.id"
          ref="cardRefs"
          class="gallery-card"
          :class="{ 'gallery-card-mobile': isMobileLayout }"
          :style="{ width: `${cardWidth}px` }"
        >
          <div class="gallery-card-frame">
            <div class="gallery-card-image-shell group cursor-pointer" @click="emit('preview', card.originalIndex)">
              <img
                :src="card.image"
                :alt="`${props.title} 图片 ${card.originalIndex + 1}`"
                class="gallery-card-image transition-transform duration-700 ease-out group-hover:scale-105"
                draggable="false"
              />
              <div class="gallery-card-veil"></div>
            </div>

            <div class="gallery-card-caption">
              <span class="gallery-card-index">{{ String(card.originalIndex + 1).padStart(2, '0') }}</span>
            </div>
          </div>
        </article>
      </div>
    </div>

    <div class="gallery-hud">
      <div class="gallery-hint">
        <BaseIcon name="MousePointer2" :size="14" />
        {{ statusText }}
      </div>

      <div class="gallery-progress">
        <div class="gallery-progress-track">
          <div
            class="gallery-progress-fill"
            :style="{ width: `${((activeIndex + 1) / Math.max(props.images.length, 1)) * 100}%` }"
          ></div>
        </div>
        <div class="gallery-progress-meta">
          <span>{{ String(activeIndex + 1).padStart(2, '0') }}</span>
          <span>{{ String(props.images.length).padStart(2, '0') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gallery-experience {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
  user-select: none;
  touch-action: none; /* Prevent browser pan/zoom on mobile */
}

.gallery-experience-mobile {
  overflow-y: auto;
  overflow-x: hidden;
  touch-action: pan-y;
}

.gallery-background,
.gallery-noise,
.gallery-gradient-orb,
.gallery-floor,
.gallery-grid {
  position: absolute;
}

.gallery-background,
.gallery-noise,
.gallery-grid {
  inset: 0;
}

.gallery-noise {
  opacity: 0.18;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(180deg, rgba(255, 255, 255, 0.75), transparent 82%);
}

.gallery-grid {
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px),
    linear-gradient(180deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
  background-size: 140px 140px;
  transform: perspective(1400px) rotateX(78deg) translateY(52vh) scale(1.55);
  transform-origin: center top;
  opacity: 0.18;
}

.gallery-gradient-orb {
  top: 50%;
  width: 40vw;
  height: 40vw;
  border-radius: 999px;
  filter: blur(110px);
  opacity: 0.22;
  transform: translateY(-50%);
}

.gallery-gradient-orb-left {
  left: -10vw;
  background: color-mix(in srgb, var(--color-primary-500) 68%, #7dc6ff 32%);
}

.gallery-gradient-orb-right {
  right: -12vw;
  background: color-mix(in srgb, #95d8ff 54%, var(--color-primary-400) 46%);
}

.gallery-floor {
  left: -10%;
  right: -10%;
  bottom: -12vh;
  height: 34vh;
  background: radial-gradient(circle at center, rgba(111, 182, 122, 0.2), transparent 60%);
  filter: blur(38px);
}

.gallery-copy {
  position: absolute;
  top: 1.9rem;
  left: 2rem;
  z-index: 4;
  max-width: min(32rem, 66vw);
  pointer-events: none;
}

.gallery-kicker {
  color: rgba(222, 236, 226, 0.7);
  font-size: 0.72rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.gallery-title {
  margin-top: 0.7rem;
  color: white;
  font-size: clamp(2.3rem, 4vw, 4.6rem);
  line-height: 0.95;
  font-weight: 700;
}

.gallery-subtitle {
  margin-top: 1rem;
  max-width: 28rem;
  color: rgba(228, 235, 231, 0.72);
  font-size: 0.95rem;
  line-height: 1.75;
}

.gallery-viewport {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.gallery-viewport-mobile {
  position: relative;
  inset: auto;
  min-height: 100%;
  overflow: visible;
}

.gallery-rail {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 0;
  height: 0;
  overflow: visible;
  perspective: 1600px;
}

.gallery-rail-mobile {
  position: relative;
  left: auto;
  top: auto;
  width: 100%;
  height: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 5.5rem 1rem 2rem;
  perspective: none;
}

.gallery-card {
  position: absolute;
  left: 0;
  top: 0;
  transform-style: preserve-3d;
  will-change: transform;
}

.gallery-card-mobile {
  position: relative;
  left: auto;
  top: auto;
  transform-style: flat;
}

.gallery-card-frame {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  padding: 0;
  border-radius: 0;
  overflow: hidden;
  background: transparent;
  box-shadow: 0 16px 44px rgba(3, 7, 18, 0.22);
  transition: box-shadow 0.4s ease;
}

.gallery-card-frame.active-glow {
  box-shadow: 0 28px 90px rgba(4, 10, 24, 0.34), 0 0 48px rgba(45, 89, 39, 0.18);
}

.gallery-card-image-shell {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 0;
}

.gallery-card-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
}

.gallery-card-veil {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), transparent 22%, rgba(0, 0, 0, 0.14)),
    radial-gradient(circle at top, rgba(255, 255, 255, 0.08), transparent 42%);
  opacity: var(--glow-opacity, 0);
  pointer-events: none;
}

.gallery-card-caption {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  color: white;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
}

.gallery-card-index {
  padding: 0.42rem 0.62rem;
  border-radius: 999px;
  background: rgba(7, 11, 19, 0.44);
  backdrop-filter: blur(10px);
  font-size: 0.92rem;
  font-weight: 700;
}

.gallery-card-frame::after {
  content: '';
  position: absolute;
  inset: auto 12% -12% 12%;
  height: 2.1rem;
  border-radius: 999px;
  background: radial-gradient(circle at center, rgba(0, 0, 0, 0.28), transparent 72%);
  filter: blur(18px);
  opacity: 0.48;
}

.gallery-hud {
  position: absolute;
  right: 2rem;
  bottom: 6.2rem;
  z-index: 4;
  width: min(24rem, calc(100vw - 2rem));
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.9rem;
  pointer-events: none;
}

.gallery-hint,
.gallery-progress {
  padding: 0.95rem 1rem;
  border-radius: 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(8, 12, 22, 0.42);
  backdrop-filter: blur(14px);
  color: rgba(241, 247, 243, 0.92);
}

.gallery-hint {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.6rem;
  font-size: 0.82rem;
}

.gallery-progress-track {
  height: 5px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
}

.gallery-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--color-primary-300), color-mix(in srgb, var(--color-primary-500) 58%, #9ce5ff 42%));
  transition: width 0.3s ease-out;
}

.gallery-progress-meta {
  margin-top: 0.7rem;
  display: flex;
  justify-content: space-between;
  color: rgba(228, 235, 231, 0.76);
  font-size: 0.82rem;
}

@media (max-width: 1024px) {
  .gallery-copy {
    top: 1.5rem;
    left: 1.25rem;
  }
  .gallery-hud {
    right: 1rem;
    bottom: 5.8rem;
  }
}

@media (max-width: 768px) {
  .gallery-copy {
    max-width: calc(100vw - 2rem);
  }
  .gallery-subtitle {
    max-width: none;
    font-size: 0.88rem;
  }
  .gallery-card-caption {
    right: 1rem;
    top: 0.9rem;
  }
  .gallery-hud {
    width: calc(100vw - 2rem);
    right: 1rem;
    bottom: 5.4rem;
  }

  .gallery-card-frame {
    box-shadow: 0 18px 42px rgba(3, 7, 18, 0.26);
  }
}
</style>
