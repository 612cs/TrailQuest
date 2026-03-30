<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  ImageIcon,
  MapPinned,
  Mountain,
  RefreshCcw,
  Tag,
  XCircle,
} from 'lucide-vue-next'

import { approveTrail, fetchAdminTrailDetail, rejectTrail } from '../api/admin'
import AdminNoticeDialog from '../components/common/AdminNoticeDialog.vue'
import EmptyState from '../components/common/EmptyState.vue'
import StatusBadge from '../components/common/StatusBadge.vue'
import { formatDateTime } from '../utils/format'
import type { AdminTrailDetail } from '../types/admin'

const route = useRoute()
const router = useRouter()
const detail = ref<AdminTrailDetail | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')
const rejectRemark = ref('')
const successDialog = ref({
  show: false,
  title: '',
  message: '',
})

const trailId = computed(() => String(route.params.id || ''))

async function loadDetail() {
  if (!trailId.value) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    detail.value = await fetchAdminTrailDetail(trailId.value)
    rejectRemark.value = detail.value.reviewRemark || ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '路线详情加载失败'
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  if (!trailId.value) {
    return
  }
  actionLoading.value = true
  errorMessage.value = ''
  try {
    await approveTrail(trailId.value)
    await loadDetail()
    successDialog.value = {
      show: true,
      title: '审核已通过',
      message: '该路线已经审核通过，前台公开列表现在可以展示它了。',
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '审核通过失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleReject() {
  if (!trailId.value) {
    return
  }
  if (!rejectRemark.value.trim()) {
    errorMessage.value = '请填写驳回原因'
    return
  }
  actionLoading.value = true
  errorMessage.value = ''
  try {
    await rejectTrail(trailId.value, { remark: rejectRemark.value.trim() })
    await loadDetail()
    successDialog.value = {
      show: true,
      title: '路线已驳回',
      message: '驳回结果和备注已经保存，作者修改后可重新提交审核。',
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '驳回失败'
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-detail-header">
      <button class="admin-button admin-button-secondary" type="button" @click="router.push({ name: 'trail-review-list' })">
        <ArrowLeft :size="16" :stroke-width="2" />
        返回列表
      </button>
      <button class="admin-button admin-button-secondary" type="button" @click="loadDetail">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div v-if="errorMessage" class="admin-detail-error">{{ errorMessage }}</div>

    <div v-if="detail" class="admin-detail-layout">
      <article class="admin-detail-hero">
        <div class="admin-detail-hero__image">
          <img v-if="detail.image" :src="detail.image" :alt="detail.name" />
          <div v-else class="admin-detail-hero__placeholder">TrailQuest</div>
        </div>

        <div class="admin-detail-hero__content">
          <div class="admin-detail-hero__top">
            <div>
              <h2 class="admin-title">{{ detail.name }}</h2>
              <p class="admin-subtitle">{{ detail.location || '地点未设置' }}</p>
            </div>
            <StatusBadge :status="detail.reviewStatus" />
          </div>

          <div class="admin-detail-meta">
            <span><MapPinned :size="16" :stroke-width="2" /> {{ detail.geoProvince || '未识别省份' }}</span>
            <span><Mountain :size="16" :stroke-width="2" /> {{ detail.difficultyLabel || detail.difficulty || '--' }}</span>
            <span><Clock3 :size="16" :stroke-width="2" /> {{ formatDateTime(detail.createdAt) }}</span>
          </div>

          <div class="admin-detail-tags" v-if="detail.tags.length">
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
            <div><dt>驳回备注</dt><dd>{{ detail.reviewRemark || '--' }}</dd></div>
          </dl>
        </section>
      </div>

      <section class="admin-card admin-detail-card">
        <h3>路线描述</h3>
        <p class="admin-detail-copy">{{ detail.description || '暂无路线描述。' }}</p>
      </section>

      <section class="admin-card admin-detail-card">
        <h3>轨迹与建模</h3>
        <div class="admin-detail-track" v-if="detail.track.hasTrack">
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
          <img v-for="item in detail.gallery" :key="item.url" :src="item.url" :alt="detail.name" />
        </div>
        <div v-else class="admin-detail-track-empty">暂无相册图片。</div>
      </section>

      <section class="admin-card admin-detail-card">
        <h3>审核操作</h3>
        <p class="admin-subtitle">通过或驳回该路线。驳回时需要填写原因。</p>
        <div class="admin-detail-actions">
          <button class="admin-button admin-button-primary" type="button" :disabled="actionLoading" @click="handleApprove">
            <CheckCircle2 :size="16" :stroke-width="2" />
            通过审核
          </button>
          <button class="admin-button admin-button-danger" type="button" :disabled="actionLoading" @click="handleReject">
            <XCircle :size="16" :stroke-width="2" />
            驳回
          </button>
        </div>
        <label class="admin-detail-remark">
          <span>驳回原因</span>
          <textarea v-model="rejectRemark" class="admin-textarea" rows="4" placeholder="请输入驳回原因"></textarea>
        </label>
      </section>
    </div>

    <EmptyState
      v-else-if="!loading"
      title="路线不存在"
      description="当前路线审核记录不存在或已被删除。"
      :icon="ImageIcon"
    />

    <div v-else class="admin-detail-loading">正在加载路线详情...</div>

    <AdminNoticeDialog
      v-model:show="successDialog.show"
      :title="successDialog.title"
      :message="successDialog.message"
    />
  </section>
</template>

<style scoped>
.admin-detail-header,
.admin-detail-top,
.admin-detail-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-detail-error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
}

.admin-detail-error {
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-detail-layout {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

.admin-detail-hero {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 1rem;
}

.admin-detail-hero__image {
  min-height: 220px;
  overflow: hidden;
  border-radius: 22px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
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
  color: var(--primary);
  font-weight: 800;
  letter-spacing: 0.2em;
}

.admin-detail-hero__content {
  padding: 1.1rem 0.5rem 0.4rem;
}

.admin-detail-hero__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.admin-detail-meta,
.admin-detail-tags,
.admin-detail-track {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.admin-detail-meta {
  margin-top: 1rem;
}

.admin-detail-meta span,
.admin-tag,
.admin-detail-track span {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 0.8rem;
  border-radius: 999px;
  color: var(--text-muted);
  background: var(--bg-soft);
}

.admin-tag {
  color: var(--primary);
}

.admin-detail-grid {
  align-items: stretch;
}

.admin-detail-card {
  padding: 1.15rem;
}

.admin-detail-card h3 {
  margin: 0 0 0.9rem;
  color: var(--text-strong);
}

.admin-detail-card dl {
  display: grid;
  gap: 0.85rem;
  margin: 0;
}

.admin-detail-card dl div {
  display: grid;
  gap: 0.25rem;
}

.admin-detail-card dt {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.admin-detail-card dd {
  margin: 0;
  color: var(--text-strong);
  font-weight: 600;
}

.admin-detail-copy {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
  white-space: pre-wrap;
}

.admin-detail-track-empty {
  color: var(--text-muted);
  padding: 0.9rem 0;
}

.admin-detail-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.85rem;
}

.admin-detail-gallery img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 18px;
  border: 1px solid var(--border);
}

.admin-detail-remark {
  display: grid;
  gap: 0.55rem;
  margin-top: 1rem;
}

.admin-detail-remark span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-detail-loading {
  min-height: 280px;
  display: grid;
  place-items: center;
  color: var(--text-muted);
}

@media (max-width: 1100px) {
  .admin-detail-hero {
    grid-template-columns: 1fr;
  }
}
</style>
