<script setup lang="ts">
import ModalShell from '@trailquest/ui/components/ModalShell.vue'

import StatusBadge from '../common/StatusBadge.vue'
import { formatDateTime } from '../../utils/format'
import type { AdminReviewDetail } from '../../types/admin'

const props = defineProps<{
  show: boolean
  detail: AdminReviewDetail | null
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()
</script>

<template>
  <ModalShell
    :show="props.show"
    aria-label="评论详情"
    :panel-style="{ width: 'min(920px, 100%)', borderRadius: '24px' }"
    :header-style="{ padding: '1.2rem 1.2rem 0', display: 'flex', alignItems: 'center' }"
    :body-style="{ padding: '1rem 1.2rem 1.2rem' }"
    @update:show="emit('update:show', $event)"
  >
    <template #header>
      <div class="review-detail__header">
        <div>
          <h3>评论详情</h3>
          <p v-if="props.detail" class="review-detail__subtitle">路线：{{ props.detail.trailName }}</p>
        </div>
        <StatusBadge v-if="props.detail" :status="props.detail.status" />
      </div>
    </template>

    <div v-if="props.loading" class="review-detail__loading">正在加载评论详情...</div>

    <div v-else-if="props.detail" class="review-detail">
      <section class="review-detail__card">
        <h4>基础信息</h4>
        <dl class="review-detail__grid">
          <div><dt>评论 ID</dt><dd>{{ props.detail.id }}</dd></div>
          <div><dt>作者</dt><dd>{{ props.detail.authorUsername }}</dd></div>
          <div><dt>路线</dt><dd>{{ props.detail.trailName }}</dd></div>
          <div><dt>评分</dt><dd>{{ props.detail.rating ?? '--' }}</dd></div>
          <div><dt>创建时间</dt><dd>{{ formatDateTime(props.detail.createdAt) }}</dd></div>
          <div><dt>最近治理</dt><dd>{{ props.detail.moderatedAt ? formatDateTime(props.detail.moderatedAt) : '--' }}</dd></div>
        </dl>
      </section>

      <section class="review-detail__card">
        <h4>评论正文</h4>
        <p class="review-detail__copy">{{ props.detail.text }}</p>
      </section>

      <section class="review-detail__card">
        <h4>上下文</h4>
        <div class="review-detail__context">
          <div>
            <strong>父评论</strong>
            <p>{{ props.detail.parentText || '当前为顶级评论，无父评论。' }}</p>
          </div>
          <div>
            <strong>治理备注</strong>
            <p>{{ props.detail.moderationReason || '暂无治理备注。' }}</p>
          </div>
        </div>
      </section>

      <section class="review-detail__card">
        <h4>直接回复</h4>
        <div v-if="props.detail.replies.length" class="review-detail__reply-list">
          <article v-for="reply in props.detail.replies" :key="reply.id" class="review-detail__reply-item">
            <div class="review-detail__reply-top">
              <strong>{{ reply.authorUsername }}</strong>
              <StatusBadge :status="reply.status" />
            </div>
            <p>{{ reply.text }}</p>
            <small>创建于 {{ formatDateTime(reply.createdAt) }}</small>
          </article>
        </div>
        <p v-else class="review-detail__empty">暂无直接回复。</p>
      </section>
    </div>
  </ModalShell>
</template>

<style scoped>
.review-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
}

.review-detail__header h3 {
  margin: 0;
  color: var(--text-strong);
}

.review-detail__subtitle {
  margin: 0.35rem 0 0;
  color: var(--text-muted);
}

.review-detail,
.review-detail__grid,
.review-detail__context,
.review-detail__reply-list {
  display: grid;
  gap: 1rem;
}

.review-detail__card {
  padding: 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.review-detail__card h4 {
  margin: 0 0 0.8rem;
  color: var(--text-strong);
}

.review-detail__grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.review-detail__grid div {
  display: grid;
  gap: 0.2rem;
}

.review-detail__grid dt,
.review-detail__context strong,
.review-detail__reply-item small,
.review-detail__empty {
  color: var(--text-muted);
}

.review-detail__grid dd,
.review-detail__copy,
.review-detail__context p,
.review-detail__reply-item p {
  margin: 0;
  color: var(--text-strong);
  line-height: 1.7;
}

.review-detail__reply-item {
  padding: 0.9rem 1rem;
  border-radius: 16px;
  background: var(--bg-surface);
}

.review-detail__reply-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.55rem;
}

.review-detail__loading {
  color: var(--text-muted);
  padding: 1rem 0;
}

@media (max-width: 720px) {
  .review-detail__grid {
    grid-template-columns: 1fr;
  }
}
</style>
