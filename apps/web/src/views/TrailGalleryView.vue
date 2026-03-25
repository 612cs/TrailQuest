<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import BaseIcon from '../components/common/BaseIcon.vue'
import TrailGalleryExperience from '../components/trail/TrailGalleryExperience.vue'
import { fetchTrailDetail } from '../api/trails'
import type { EntityId } from '../types/id'
import type { TrailListItem } from '../types/trail'

const route = useRoute()
const router = useRouter()

const trailData = ref<TrailListItem | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')

const trailId = computed<EntityId>(() => {
  const rawId = route.params.id
  return Array.isArray(rawId) ? rawId[0] ?? '' : String(rawId ?? '')
})

const returnTo = computed(() => {
  const raw = route.query.from
  const value = Array.isArray(raw) ? raw[0] : raw

  if (typeof value !== 'string' || !value.startsWith('/')) {
    return null
  }

  return value
})

const galleryImages = computed(() => {
  const urls = [trailData.value?.image, ...(trailData.value?.gallery?.map((item) => item.url) ?? [])]
    .filter((item): item is string => !!item)

  return urls.filter((url, index) => urls.indexOf(url) === index)
})

const WORD = 'TRAIL'
const ROW_COUNT = 5
const REPEAT_COUNT = 100
const PARALLAX_FACTOR = 0.15 // Slightly slower for better effect

const backgroundRows = computed(() =>
  Array.from({ length: ROW_COUNT }, (_, index) => ({
    text: Array.from({ length: REPEAT_COUNT }, () => WORD).join(' \u00A0 \u00A0 '),
    index,
    isOdd: index % 2 === 0,
  })),
)

watch(
  trailId,
  async (nextId) => {
    if (!nextId) {
      trailData.value = null
      return
    }

    isLoading.value = true
    errorMessage.value = ''

    try {
      trailData.value = await fetchTrailDetail(nextId)
    } catch (error) {
      trailData.value = null
      errorMessage.value = error instanceof Error ? error.message : '图片漫游加载失败'
    } finally {
      isLoading.value = false
    }
  },
  { immediate: true },
)

function handleCameraMove(payload: { x: number; max: number }) {
  const getGsap = () => (window as any).gsap
  const gsap = getGsap()
  if (!gsap) return
  // Handle desktop poster words
  const desktopRows = document.querySelectorAll('.gallery-poster-desktop .gallery-poster-word')
  desktopRows.forEach((el, index) => {
    const isOdd = index % 2 === 0
    // Use raw GSAP x to drive infinite text
    const offset = payload.x * PARALLAX_FACTOR
    const translateX = isOdd ? offset : -offset
    
    // Ensure the background scroll space is massively wide enough to be practically infinite 
    // without hitting the GSAP wrap jump
    const wrappedOffset = gsap.utils.wrap(-40000, 40000, translateX)

    gsap.set(el, { xPercent: -50, x: wrappedOffset })
  })

  // Handle mobile poster words
  const mobileRows = document.querySelectorAll('.gallery-poster-mobile .gallery-mobile-word')
  mobileRows.forEach((el, index) => {
    const isOdd = index % 2 === 0
    const offset = payload.x * PARALLAX_FACTOR * 0.5 // Slower on mobile
    const translateX = isOdd ? offset : -offset
    
    const wrappedOffset = gsap.utils.wrap(-40000, 40000, translateX)
    gsap.set(el, { xPercent: -50, x: wrappedOffset })
  })
}

function handleBack() {
  if (returnTo.value) {
    void router.push(returnTo.value)
    return
  }

  void router.push(`/trail/${trailId.value}`)
}
</script>

<template>
  <main class="gallery-page">
    <div class="gallery-shell">
      <template v-if="trailData && galleryImages.length">
        <div class="gallery-main">
          <div class="gallery-poster gallery-poster-desktop" aria-hidden="true">
            <div class="gallery-poster-grid"></div>
            <div class="gallery-poster-glow gallery-poster-glow-left"></div>
            <div class="gallery-poster-glow gallery-poster-glow-right"></div>

            <p
              v-for="row in backgroundRows"
              :key="row.index"
              class="gallery-poster-word"
              :class="[
                `gallery-poster-word-${row.index + 1}`,
                row.isOdd ? 'gallery-poster-word-green' : 'gallery-poster-word-white',
              ]"
            >
              {{ row.text }}
            </p>
          </div>

          <section class="gallery-stage-panel">
            <TrailGalleryExperience
              class="gallery-stage"
              :images="galleryImages"
              :title="trailData.name"
              @camera-move="handleCameraMove"
            />
          </section>

          <section class="gallery-mobile-letters" aria-hidden="true">
            <div class="gallery-poster gallery-poster-mobile">
              <div class="gallery-poster-grid"></div>

              <p
                v-for="row in backgroundRows"
                :key="`mobile-${row.index}`"
                class="gallery-mobile-word"
                :class="row.isOdd ? 'gallery-poster-word-green' : 'gallery-poster-word-white'"
              >
                {{ row.text }}
              </p>
            </div>
          </section>
        </div>

        <div class="gallery-topbar">
          <button
            type="button"
            class="gallery-action"
            @click="handleBack"
          >
            <BaseIcon name="ChevronLeft" :size="18" />
            返回详情
          </button>
        </div>
      </template>

      <div v-else class="gallery-fallback">
        <div class="gallery-fallback-card">
          <BaseIcon v-if="isLoading" name="Loader2" :size="22" class="animate-spin gallery-fallback-icon" />
          <BaseIcon v-else name="ImageOff" :size="22" class="gallery-fallback-icon" />
          <p class="gallery-fallback-title">{{ isLoading ? '正在加载图片漫游...' : '暂时无法进入图片漫游' }}</p>
          <p class="gallery-fallback-text">{{ errorMessage || '当前路线暂无可展示的图片。' }}</p>
          <button type="button" class="gallery-action gallery-action-solid" @click="handleBack">
            返回详情
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.gallery-page {
  --gallery-word-green: color-mix(in srgb, var(--color-primary-200) 36%, var(--color-primary-400) 64%);
  --gallery-word-white: rgba(255, 255, 255, 0.3);
  --gallery-word-shadow: rgba(5, 18, 8, 0.82);
  --gallery-dot: rgba(126, 186, 121, 0.24);
  min-height: 100vh;
  background:
    radial-gradient(circle at 50% 52%, rgba(45, 89, 39, 0.12), transparent 24%),
    radial-gradient(circle at 18% 78%, rgba(79, 154, 72, 0.08), transparent 20%),
    radial-gradient(circle at 84% 20%, rgba(127, 186, 121, 0.06), transparent 18%),
    linear-gradient(180deg, #020503 0%, #050806 48%, #030503 100%);
}

.gallery-shell {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  isolation: isolate;
}

.gallery-main {
  position: relative;
  min-height: 100vh;
}

.gallery-stage-panel {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

.gallery-poster {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.gallery-poster-desktop {
  background:
    radial-gradient(circle at 50% 56%, rgba(57, 117, 52, 0.12), transparent 24%),
    linear-gradient(180deg, rgba(0, 0, 0, 0.06), transparent 18%, rgba(0, 0, 0, 0.14));
}

.gallery-poster-mobile {
  position: relative;
  min-height: 100%;
}

.gallery-poster-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, var(--gallery-dot) 1.2px, transparent 1.2px);
  background-size: 22px 22px;
  opacity: 0.92;
}

.gallery-poster-glow {
  position: absolute;
  width: 34vw;
  height: 34vw;
  border-radius: 999px;
  filter: blur(90px);
  opacity: 0.18;
}

.gallery-poster-glow-left {
  left: -8vw;
  bottom: 4vh;
  background: rgba(79, 154, 72, 0.24);
}

.gallery-poster-glow-right {
  top: 12vh;
  right: -10vw;
  background: rgba(45, 89, 39, 0.2);
}

.gallery-poster-word {
  position: absolute;
  left: 50%;
  margin: 0;
  font-family: Impact, Haettenschweiler, 'Arial Narrow Bold', sans-serif;
  font-size: clamp(5.4rem, 17vw, 15rem);
  line-height: 0.86;
  letter-spacing: 0.1em;
  white-space: nowrap;
  text-transform: uppercase;
  user-select: none;
  will-change: transform;
}

.gallery-mobile-letters {
  display: none;
}

.gallery-mobile-word {
  position: relative;
  z-index: 1;
  margin: 0;
  font-family: Impact, Haettenschweiler, 'Arial Narrow Bold', sans-serif;
  font-size: clamp(4.5rem, 18vw, 7rem);
  line-height: 0.86;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  text-align: center;
}

.gallery-poster-word-green {
  color: var(--gallery-word-green);
  text-shadow:
    7px 7px 0 var(--gallery-word-shadow),
    0 0 36px rgba(79, 154, 72, 0.16);
}

.gallery-poster-word-white {
  color: var(--gallery-word-white);
  text-shadow:
    7px 7px 0 rgba(0, 0, 0, 0.42),
    0 0 36px rgba(255, 255, 255, 0.08);
}

.gallery-poster-word-1 {
  top: -2vh;
  margin-left: -50%;
}

.gallery-poster-word-2 {
  top: calc(100vh / 5 * 1 - 2vh);
  margin-left: -55%;
}

.gallery-poster-word-3 {
  top: calc(100vh / 5 * 2 - 2vh);
  margin-left: -45%;
}

.gallery-poster-word-4 {
  top: calc(100vh / 5 * 3 - 2vh);
  margin-left: -52%;
}

.gallery-poster-word-5 {
  top: calc(100vh / 5 * 4 - 2vh);
  margin-left: -48%;
}

.gallery-stage {
  position: relative;
  z-index: 1;
}

.gallery-topbar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 8;
  display: flex;
  align-items: flex-start;
  padding: 1.35rem 1.45rem;
  pointer-events: none;
}

.gallery-action {
  pointer-events: auto;
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  min-height: 3.05rem;
  padding: 0.82rem 1rem;
  border: 1px solid rgba(255, 244, 236, 0.14);
  background: transparent;
  color: rgba(255, 244, 236, 0.95);
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.88rem;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.gallery-action:hover {
  transform: translateY(-1px);
  border-color: rgba(185, 222, 176, 0.52);
  background: rgba(79, 154, 72, 0.08);
  box-shadow: 0 0 0 1px rgba(185, 222, 176, 0.14), 0 12px 24px rgba(22, 52, 24, 0.18);
}

.gallery-action-solid {
  background: rgba(79, 154, 72, 0.12);
}

.gallery-fallback {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem;
  background:
    radial-gradient(circle at 50% 18%, rgba(255, 228, 212, 0.08), transparent 28%),
    linear-gradient(180deg, #220606 0%, #120202 100%);
}

.gallery-fallback-card {
  width: min(30rem, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.9rem;
  text-align: center;
  padding: 2rem;
  border-radius: 1.6rem;
  border: 1px solid rgba(255, 244, 236, 0.12);
  background: rgba(25, 6, 6, 0.88);
  color: rgba(255, 244, 236, 0.94);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.28);
}

.gallery-fallback-icon {
  color: rgba(255, 228, 212, 0.92);
}

.gallery-fallback-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.gallery-fallback-text {
  color: rgba(255, 239, 231, 0.7);
  line-height: 1.6;
}

.gallery-stage:deep(.gallery-experience.gallery-experience) {
  background: transparent;
  height: 100vh;
  min-height: 100vh;
}

.gallery-stage:deep(.gallery-background),
.gallery-stage:deep(.gallery-copy),
.gallery-stage:deep(.gallery-hud) {
  display: none;
}

.gallery-stage:deep(.gallery-card-caption) {
  top: 0.85rem;
  right: 0.85rem;
  left: auto;
  bottom: auto;
}

.gallery-stage:deep(.gallery-card-index) {
  color: rgba(255, 244, 236, 0.94);
  border: 1px solid rgba(255, 244, 236, 0.12);
}

@media (max-width: 768px) {
  .gallery-main {
    min-height: 100vh;
    display: grid;
    grid-template-rows: minmax(58vh, 1fr) minmax(42vh, auto);
  }

  .gallery-poster-desktop {
    display: none;
  }

  .gallery-stage-panel {
    min-height: 0;
  }

  .gallery-stage:deep(.gallery-experience.gallery-experience) {
    height: 100%;
    min-height: 0;
  }

  .gallery-mobile-letters {
    position: relative;
    display: block;
    min-height: 42vh;
    overflow: hidden;
    padding: 2rem 1.25rem 2.5rem;
  }

  .gallery-poster-mobile {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 0.6rem;
  }

  .gallery-topbar {
    padding: 1rem;
  }

  .gallery-action {
    min-height: 2.8rem;
    padding: 0.72rem 0.9rem;
  }
}
</style>
