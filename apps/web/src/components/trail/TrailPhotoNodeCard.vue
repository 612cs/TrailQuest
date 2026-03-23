<script setup lang="ts">
const props = defineProps<{
  title: string
  image: string
  alt: string
  indexLabel: string
  segmentLabel: string
  cameraLabel: string
  progressLabel: string
  size: 'hero' | 'detail' | 'glimpse'
  isActive: boolean
  emphasis: number
}>()

const emit = defineEmits<{
  (event: 'select'): void
}>()
</script>

<template>
  <button
    type="button"
    class="photo-node-card"
    :class="[
      `photo-node-card-${props.size}`,
      { 'photo-node-card-active': props.isActive },
    ]"
    :style="{ '--card-emphasis': String(props.emphasis) }"
    @click="emit('select')"
  >
    <div class="photo-node-card-shell">
      <div class="photo-node-card-image-wrap">
        <img :src="props.image" :alt="props.alt" class="photo-node-card-image" draggable="false" />
        <div class="photo-node-card-overlay"></div>
      </div>

      <div class="photo-node-card-meta">
        <div class="photo-node-card-topline">
          <span class="photo-node-card-index">{{ props.indexLabel }}</span>
          <span class="photo-node-card-dot"></span>
          <span class="photo-node-card-segment">{{ props.segmentLabel }}</span>
        </div>
        <p class="photo-node-card-title">{{ props.title }}</p>
        <p class="photo-node-card-caption">{{ props.cameraLabel }}</p>
      </div>

      <div class="photo-node-card-progress">
        <span>{{ props.progressLabel }}</span>
        <span>影像节点</span>
      </div>
    </div>
  </button>
</template>

<style scoped>
.photo-node-card {
  position: relative;
  display: block;
  width: 100%;
  border: 0;
  padding: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transform: translateZ(0);
}

.photo-node-card-shell {
  position: relative;
  overflow: hidden;
  border-radius: 1.8rem;
  border: 1px solid rgba(219, 255, 238, 0.14);
  background:
    linear-gradient(180deg, rgba(7, 17, 17, 0.18), rgba(7, 12, 15, 0.72)),
    rgba(6, 15, 16, 0.74);
  box-shadow:
    0 26px 80px rgba(1, 10, 12, 0.45),
    0 0 0 1px rgba(197, 255, 230, 0.05);
  backdrop-filter: blur(14px);
  transition:
    transform 0.45s ease,
    border-color 0.35s ease,
    box-shadow 0.35s ease;
}

.photo-node-card:hover .photo-node-card-shell,
.photo-node-card-active .photo-node-card-shell {
  transform: translateY(-6px);
  border-color: rgba(168, 255, 217, 0.35);
  box-shadow:
    0 34px 100px rgba(1, 10, 12, 0.54),
    0 0 0 1px rgba(168, 255, 217, 0.12),
    0 0 70px rgba(81, 190, 140, 0.18);
}

.photo-node-card-image-wrap {
  position: relative;
  aspect-ratio: 5 / 4;
  overflow: hidden;
}

.photo-node-card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(calc(1.04 + var(--card-emphasis) * 0.02));
  transition: transform 0.6s ease;
}

.photo-node-card:hover .photo-node-card-image,
.photo-node-card-active .photo-node-card-image {
  transform: scale(calc(1.1 + var(--card-emphasis) * 0.03));
}

.photo-node-card-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(1, 5, 10, 0) 40%, rgba(1, 8, 10, 0.78) 100%),
    linear-gradient(135deg, rgba(163, 255, 216, 0.12), transparent 55%);
}

.photo-node-card-meta {
  position: absolute;
  inset-inline: 0;
  bottom: 0;
  padding: 1rem 1rem 1.1rem;
  color: rgba(246, 255, 251, 0.96);
}

.photo-node-card-topline,
.photo-node-card-progress {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.72rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(211, 240, 229, 0.68);
}

.photo-node-card-dot {
  width: 0.25rem;
  height: 0.25rem;
  border-radius: 999px;
  background: rgba(170, 255, 218, 0.82);
}

.photo-node-card-title {
  margin-top: 0.7rem;
  font-family: 'Iowan Old Style', 'Palatino Linotype', 'Times New Roman', serif;
  font-size: 1.15rem;
  line-height: 1.1;
}

.photo-node-card-caption {
  margin-top: 0.32rem;
  max-width: 18rem;
  font-size: 0.82rem;
  line-height: 1.45;
  color: rgba(229, 248, 239, 0.78);
}

.photo-node-card-progress {
  justify-content: space-between;
  padding: 0.82rem 1rem 1rem;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0), rgba(4, 9, 11, 0.48));
}

.photo-node-card-hero .photo-node-card-title {
  font-size: 1.45rem;
}

.photo-node-card-glimpse .photo-node-card-title {
  font-size: 0.98rem;
}

.photo-node-card-glimpse .photo-node-card-caption {
  display: none;
}

.photo-node-card-glimpse .photo-node-card-progress {
  display: none;
}
</style>
