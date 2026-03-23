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
const activeIndex = ref(0)

const trailId = computed<EntityId>(() => {
  const rawId = route.params.id
  return Array.isArray(rawId) ? rawId[0] ?? '' : String(rawId ?? '')
})

const galleryImages = computed(() => {
  const urls = [trailData.value?.image, ...(trailData.value?.gallery?.map((item) => item.url) ?? [])]
    .filter((item): item is string => !!item)
  return urls.filter((url, index) => urls.indexOf(url) === index)
})

const activeLabel = computed(() => `${Math.min(activeIndex.value + 1, galleryImages.value.length || 1)} / ${galleryImages.value.length || 1}`)

watch(
  trailId,
  async (nextId) => {
    if (!nextId) {
      trailData.value = null
      return
    }

    isLoading.value = true
    errorMessage.value = ''
    activeIndex.value = 0

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

function handleBack() {
  void router.push(`/trail/${trailId.value}`)
}
</script>

<template>
  <main class="gallery-page">
    <div class="gallery-shell">
      <template v-if="trailData && galleryImages.length">
        <TrailGalleryExperience
          :images="galleryImages"
          :title="trailData.name"
          :location="trailData.location"
          :description="trailData.description"
          :distance="trailData.distance"
          :elevation="trailData.elevation"
          :duration="trailData.duration"
          :difficulty-label="trailData.difficultyLabel"
          :track="trailData.track ?? null"
          @active-change="activeIndex = $event"
        />

        <div class="gallery-topbar">
          <button
            type="button"
            class="gallery-action"
            @click="handleBack"
          >
            <BaseIcon name="ChevronLeft" :size="18" />
            返回详情
          </button>

          <div class="gallery-counter">
            <BaseIcon name="Image" :size="15" />
            {{ activeLabel }}
          </div>
        </div>

        <div class="gallery-bottombar">
          <div class="gallery-tip">
            <BaseIcon name="MousePointer2" :size="15" />
            拖拽、滚轮或方向键浏览全部图片
          </div>
        </div>
      </template>

      <div v-else class="gallery-fallback">
        <div class="gallery-fallback-card">
          <BaseIcon v-if="isLoading" name="Loader2" :size="22" class="animate-spin text-primary-500" />
          <BaseIcon v-else name="ImageOff" :size="22" class="text-primary-500" />
          <p class="gallery-fallback-title">{{ isLoading ? '正在加载图片漫游...' : '暂时无法进入图片漫游' }}</p>
          <p class="gallery-fallback-text">{{ errorMessage || '当前路线暂无可展示的图片。' }}</p>
          <button type="button" class="gallery-action" @click="handleBack">
            返回详情
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.gallery-page {
  min-height: 100vh;
  background: #04070d;
}

.gallery-shell {
  position: relative;
  height: 100vh;
  min-height: 100vh;
}

.gallery-topbar,
.gallery-bottombar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.4rem;
  pointer-events: none;
}

.gallery-topbar {
  top: 0;
}

.gallery-bottombar {
  bottom: 0;
}

.gallery-action,
.gallery-counter,
.gallery-tip {
  pointer-events: auto;
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.8rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(5, 10, 22, 0.42);
  backdrop-filter: blur(14px);
  color: rgba(248, 251, 255, 0.92);
  font-size: 0.88rem;
}

.gallery-action {
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.gallery-action:hover {
  background: rgba(15, 31, 59, 0.6);
  border-color: rgba(89, 167, 255, 0.28);
  transform: translateY(-1px);
}

.gallery-fallback {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem;
}

.gallery-fallback-card {
  width: min(28rem, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.9rem;
  text-align: center;
  padding: 2rem;
  border-radius: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(9, 14, 24, 0.9);
  color: white;
}

.gallery-fallback-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.gallery-fallback-text {
  color: rgba(224, 232, 243, 0.72);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .gallery-topbar,
  .gallery-bottombar {
    padding: 1rem;
  }

  .gallery-topbar {
    gap: 0.7rem;
    flex-wrap: wrap;
  }

  .gallery-bottombar {
    justify-content: flex-start;
  }

  .gallery-action,
  .gallery-counter,
  .gallery-tip {
    border-radius: 1rem;
    font-size: 0.82rem;
  }
}
</style>
