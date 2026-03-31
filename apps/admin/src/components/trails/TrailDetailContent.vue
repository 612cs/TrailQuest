<script setup lang="ts">
import { computed, ref } from 'vue'
import { Clock3, MapPinned, Mountain, Tag } from 'lucide-vue-next'

import ImagePreviewModal from '@trailquest/ui/components/ImagePreviewModal.vue'

import StatusBadge from '../common/StatusBadge.vue'
import type { AdminTrailDetail } from '../../types/admin'
import { formatDateTime } from '../../utils/format'

const props = defineProps<{
  detail: AdminTrailDetail
}>()

const previewVisible = ref(false)
const previewIndex = ref(0)

const previewImages = computed(() => {
  const candidates = [
    props.detail.image,
    ...props.detail.gallery.map((item) => item.url),
  ].filter((value): value is string => Boolean(value))

  return Array.from(new Set(candidates))
})

function openPreviewByUrl(url: string) {
  const index = previewImages.value.findIndex((item) => item === url)
  previewIndex.value = index >= 0 ? index : 0
  previewVisible.value = true
}
</script>

<template>
  <div class="admin-detail-layout">
    <article class="admin-detail-hero">
      <button
        class="admin-detail-hero__image"
        :class="{ 'admin-detail-hero__image--clickable': previewImages.length > 0 }"
        type="button"
        :disabled="previewImages.length === 0"
        @click="detail.image && openPreviewByUrl(detail.image)"
      >
        <img v-if="detail.image" :src="detail.image" :alt="detail.name" />
        <div v-else class="admin-detail-hero__placeholder">TrailQuest</div>
      </button>

      <div class="admin-detail-hero__content">
        <div class="admin-detail-hero__top">
          <div>
            <h2 class="admin-title">{{ detail.name }}</h2>
            <p class="admin-subtitle">{{ detail.location || '地点未设置' }}</p>
          </div>
          <div class="admin-detail-statuses">
            <StatusBadge :status="detail.status" />
            <StatusBadge :status="detail.reviewStatus" />
          </div>
        </div>

        <div class="admin-detail-meta">
          <span><MapPinned :size="16" :stroke-width="2" /> {{ detail.geoProvince || '未识别省份' }}</span>
          <span><Mountain :size="16" :stroke-width="2" /> {{ detail.difficultyLabel || detail.difficulty || '--' }}</span>
          <span><Clock3 :size="16" :stroke-width="2" /> {{ formatDateTime(detail.createdAt) }}</span>
        </div>

        <div v-if="detail.tags.length" class="admin-detail-tags">
          <span v-for="tag in detail.tags" :key="tag" class="admin-tag">
            <Tag :size="14" :stroke-width="2" />
            {{ tag }}
          </span>
        </div>
      </div>
    </article>

    <div class="admin-grid-2 admin-detail-grid">
      <section class="admin-card admin-detail-card">
        <h3>基本信息</h3>
        <dl>
          <div><dt>作者</dt><dd>{{ detail.author.username }}</dd></div>
          <div><dt>浏览/收藏</dt><dd>{{ detail.likes }} / {{ detail.favorites }}</dd></div>
          <div><dt>评论数</dt><dd>{{ detail.reviewCount }}</dd></div>
          <div><dt>轨迹来源</dt><dd>{{ detail.track.hasTrack ? detail.track.originalFileName || detail.track.sourceFormat || '已上传' : '未上传' }}</dd></div>
        </dl>
      </section>

      <section class="admin-card admin-detail-card">
        <h3>结构化地点</h3>
        <dl>
          <div><dt>国家</dt><dd>{{ detail.geoCountry || '--' }}</dd></div>
          <div><dt>省份</dt><dd>{{ detail.geoProvince || '--' }}</dd></div>
          <div><dt>城市</dt><dd>{{ detail.geoCity || '--' }}</dd></div>
          <div><dt>区县</dt><dd>{{ detail.geoDistrict || '--' }}</dd></div>
          <div><dt>来源</dt><dd>{{ detail.geoSource || '--' }}</dd></div>
          <div><dt>审核备注</dt><dd>{{ detail.reviewRemark || '--' }}</dd></div>
        </dl>
      </section>
    </div>

    <section class="admin-card admin-detail-card">
      <h3>路线描述</h3>
      <p class="admin-detail-copy">{{ detail.description || '暂无路线描述。' }}</p>
    </section>

    <section class="admin-card admin-detail-card">
      <h3>轨迹与建模</h3>
      <div v-if="detail.track.hasTrack" class="admin-detail-track">
        <span>海拔上升 {{ detail.track.elevationGainMeters ?? '--' }} m</span>
        <span>海拔下降 {{ detail.track.elevationLossMeters ?? '--' }} m</span>
        <span>距离 {{ detail.track.distanceMeters ?? '--' }} m</span>
        <span>时长 {{ detail.track.durationSeconds ?? '--' }} s</span>
      </div>
      <div v-else class="admin-detail-track-empty">该路线暂未上传轨迹。</div>
    </section>

    <section class="admin-card admin-detail-card">
      <h3>相册</h3>
      <div v-if="detail.gallery.length" class="admin-detail-gallery">
        <button
          v-for="item in detail.gallery"
          :key="item.url"
          class="admin-detail-gallery__item"
          type="button"
          @click="openPreviewByUrl(item.url)"
        >
          <img :src="item.url" :alt="detail.name" />
        </button>
      </div>
      <div v-else class="admin-detail-track-empty">暂无相册图片。</div>
    </section>

    <ImagePreviewModal
      :show="previewVisible"
      :images="previewImages"
      :initial-index="previewIndex"
      @update:show="previewVisible = $event"
      @close="previewVisible = false"
    />
  </div>
</template>

<style scoped>
.admin-detail-layout {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

.admin-detail-hero {
  display: grid;
  grid-template-columns: minmax(0, 320px) minmax(0, 1fr);
  gap: 1rem;
  padding: 1rem;
  border-radius: 24px;
  background: var(--bg-soft);
}

.admin-detail-hero__image {
  padding: 0;
  border: 0;
  min-height: 220px;
  overflow: hidden;
  border-radius: 20px;
  background: var(--bg-elevated);
}

.admin-detail-hero__image--clickable,
.admin-detail-gallery__item {
  cursor: zoom-in;
}

.admin-detail-hero__image img,
.admin-detail-hero__placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-detail-hero__placeholder {
  display: grid;
  place-items: center;
  color: var(--text-muted);
  font-weight: 700;
}

.admin-detail-hero__content {
  display: grid;
  gap: 1rem;
  align-content: start;
}

.admin-detail-hero__top,
.admin-detail-statuses,
.admin-detail-meta,
.admin-detail-tags,
.admin-detail-track {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.admin-detail-hero__top {
  align-items: flex-start;
  justify-content: space-between;
}

.admin-detail-meta span,
.admin-tag,
.admin-detail-track span {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 0.75rem;
  border-radius: 999px;
  background: var(--bg-surface);
  color: var(--text-muted);
}

.admin-tag {
  color: var(--primary);
}

.admin-detail-card {
  padding: 1.1rem;
}

.admin-detail-card h3 {
  margin: 0 0 0.8rem;
  color: var(--text-strong);
}

.admin-detail-card dl {
  display: grid;
  gap: 0.75rem;
  margin: 0;
}

.admin-detail-card dl div {
  display: grid;
  gap: 0.2rem;
}

.admin-detail-card dt {
  color: var(--text-muted);
  font-size: 0.88rem;
}

.admin-detail-card dd {
  margin: 0;
  color: var(--text-strong);
}

.admin-detail-copy,
.admin-detail-track-empty {
  color: var(--text-muted);
  line-height: 1.75;
}

.admin-detail-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 0.8rem;
}

.admin-detail-gallery__item {
  padding: 0;
  border: 0;
  background: transparent;
}

.admin-detail-gallery__item img,
.admin-detail-gallery img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 18px;
  border: 1px solid var(--border);
}

@media (max-width: 900px) {
  .admin-detail-hero {
    grid-template-columns: 1fr;
  }
}
</style>
