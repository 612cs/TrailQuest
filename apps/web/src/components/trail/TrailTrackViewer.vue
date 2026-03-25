<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import BaseIcon from '../common/BaseIcon.vue'
import ActionButton from '../common/ActionButton.vue'

export interface TrackData {
  id: string | number
  name: string
  difficulty: string
  distance: number
  elevation: number
  track?: {
    geoJson?: any
    elevationGainMeters?: number
  }
}

const props = withDefaults(defineProps<{
  data: TrackData
  mode?: 'embedded' | 'detail' | 'fullscreen'
  weatherScene?: 'sunny' | 'partly_cloudy' | 'rainy' | 'snowy'
  showScrollToContentButton?: boolean
  showFullscreenButton?: boolean
}>(), {
  mode: 'embedded',
  weatherScene: 'sunny',
  showScrollToContentButton: true,
  showFullscreenButton: true
})

const emit = defineEmits<{
  (e: 'exitFullscreen'): void
  (e: 'requestFullscreen'): void
}>()

const statusLabel = computed(() => {
  if (props.mode === 'fullscreen') return '沉浸模式 · 实景预览已开启'
  return '3D 轨迹预览 · 点击右下角进入全屏'
})

const difficultyColor = computed(() => {
  switch (props.data.difficulty.toLowerCase()) {
    case 'easy': return 'text-primary-500'
    case 'moderate': return 'text-yellow-500'
    case 'hard': return 'text-orange-500'
    default: return 'text-primary-500'
  }
})

// Three.js and Map elements would go here
// This is a simplified mock visualization
</script>

<template>
  <section class="track-viewer-frame animate-fade-in-up">
    <!-- Controls Layout (Floating) -->
    <div
      v-if="showFullscreenButton || mode === 'fullscreen'"
      class="track-viewer-controls"
    >
      <button
        v-if="showFullscreenButton && mode !== 'fullscreen'"
        type="button"
        class="track-action-button"
        @click="emit('requestFullscreen')"
      >
        <BaseIcon name="Maximize2" :size="16" />
        进入全屏
      </button>
      <button
        v-else-if="mode === 'fullscreen'"
        type="button"
        class="track-action-button"
        @click="emit('exitFullscreen')"
      >
        <BaseIcon name="Minimize2" :size="16" />
        退出全屏
      </button>
    </div>

    <!-- Header (Hidden in Fullscreen) -->
    <div
      v-if="mode !== 'fullscreen'"
      class="track-viewer-header"
    >
      <div class="track-viewer-title-group">
        <h3 class="track-viewer-title">3D 轨迹预览</h3>
        <p class="track-viewer-subtitle">
          全长 {{ data.distance }} km · 累计爬升 {{ data.elevation }} m
        </p>
      </div>
    </div>

    <!-- Main View Surface -->
    <div class="track-viewer-surface">
      <div :class="['viewer-shell', `viewer-shell-${mode}`]">
        <!-- Mock 3D Map Content -->
        <div id="three-container" class="viewer-canvas-host">
          <div class="mock-3d-scene">
            <!-- Dynamic UI Overlays -->
            <div class="status-pill">
              <div class="pulse-indicator"></div>
              <span>{{ statusLabel }}</span>
            </div>

            <div class="viewer-actions">
              <button class="overview-button">
                <BaseIcon name="Navigation" :size="18" />
                俯瞰全景
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.track-viewer-frame {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.track-viewer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0 1rem;
}

.track-viewer-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.track-viewer-subtitle {
  margin-top: 0.2rem;
  font-size: 0.82rem;
  color: var(--text-secondary);
}

.track-viewer-surface {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
  flex: 1;
  min-height: 0;
  border: 1px solid color-mix(in srgb, var(--primary-500) 18%, transparent);
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--primary-500) 18%, transparent), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.18));
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
}

.viewer-shell-embedded {
  height: 25rem;
}

.viewer-shell-detail {
  height: 100%;
}

.viewer-shell-fullscreen {
  height: 100vh;
}

.viewer-canvas-host {
  height: 100%;
  width: 100%;
  background: linear-gradient(180deg, #87ceeb 0%, #a8e063 100%);
  position: relative;
}

.mock-3d-scene {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1.5rem;
  background: url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80') center/cover;
}

.status-pill {
  position: absolute;
  top: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.8rem 1.25rem;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.pulse-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #a8e063;
  box-shadow: 0 0 10px #a8e063;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

.viewer-actions {
  position: absolute;
  bottom: 2rem;
  right: 2rem;
}

.overview-button,
.track-action-button,
.scroll-button {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  border: none;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.overview-button,
.scroll-button {
  border-radius: 999px;
  padding: 0.9rem 1.1rem;
  color: #fff;
  background: linear-gradient(135deg, #ff9cee, #b28dff);
  box-shadow: 0 12px 28px rgba(178, 141, 255, 0.35);
}

.track-action-button {
  border-radius: 999px;
  padding: 0.72rem 0.95rem;
  color: var(--primary-500);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}

.track-viewer-controls {
  position: absolute;
  top: 1.5rem;
  left: 1.5rem;
  z-index: 50;
}

.overview-button:hover,
.track-action-button:hover,
.scroll-button:hover {
  transform: translateY(-1px);
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
