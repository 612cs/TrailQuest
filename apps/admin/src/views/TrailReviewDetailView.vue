<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle2, ImageIcon, RefreshCcw, XCircle } from 'lucide-vue-next'

import { approveTrail, fetchAdminTrailDetail, rejectTrail } from '../api/admin'
import AdminConfirmDialog from '../components/common/AdminConfirmDialog.vue'
import AdminNoticeDialog from '../components/common/AdminNoticeDialog.vue'
import EmptyState from '../components/common/EmptyState.vue'
import TrailDetailContent from '../components/trails/TrailDetailContent.vue'
import type { AdminTrailDetail } from '../types/admin'

const route = useRoute()
const router = useRouter()
const detail = ref<AdminTrailDetail | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')
const rejectDialogVisible = ref(false)
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

async function handleReject(reason: string) {
  if (!trailId.value) {
    return
  }
  actionLoading.value = true
  errorMessage.value = ''
  try {
    await rejectTrail(trailId.value, { remark: reason })
    rejectDialogVisible.value = false
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

onMounted(() => {
  void loadDetail()
})

watch(() => route.params.id, () => {
  void loadDetail()
})
</script>

<template>
  <div class="detail-page-container">
    <div class="detail-header">
      <button class="btn btn--secondary" type="button" @click="router.push({ name: 'trail-review-list' })">
        <ArrowLeft :size="16" :stroke-width="2" /> 返回列表
      </button>
      <button class="btn btn--secondary" type="button" @click="loadDetail">
        <RefreshCcw :size="16" :stroke-width="2" /> 刷新
      </button>
    </div>

    <div v-if="errorMessage" class="notice-alert is-error">{{ errorMessage }}</div>

    <template v-if="detail">
      <TrailDetailContent :detail="detail" />

      <section class="settings-card action-card">
        <div class="action-card__content">
          <h3>审核操作</h3>
          <p class="admin-subtitle">通过或驳回该路线。驳回时必须填写审核原因。</p>
        </div>
        <div class="action-card__buttons">
          <button class="btn btn--primary" type="button" :disabled="actionLoading" @click="handleApprove">
            <CheckCircle2 :size="16" :stroke-width="2" /> 通过审核
          </button>
          <button class="btn btn--danger" type="button" :disabled="actionLoading" @click="rejectDialogVisible = true">
            <XCircle :size="16" :stroke-width="2" /> 驳回
          </button>
        </div>
      </section>
    </template>

    <div v-else-if="!loading" class="empty-wrap settings-card">
      <EmptyState
        title="路线不存在"
        description="当前路线审核记录不存在或已被删除。"
        :icon="ImageIcon"
      />
    </div>

    <div v-else class="loading-state">
      <RefreshCcw class="animate-spin" :size="32" />
      <p>正在加载路线详情...</p>
    </div>

    <AdminConfirmDialog
      v-model:show="rejectDialogVisible"
      title="确认驳回路线"
      message="驳回后该路线不会在前台公开展示，作者修改后可重新提交审核。"
      confirm-text="确认驳回"
      :loading="actionLoading"
      require-reason
      reason-label="驳回原因"
      reason-placeholder="请输入驳回原因"
      :initial-reason="detail?.reviewRemark || ''"
      @confirm="handleReject"
    />

    <AdminNoticeDialog
      v-model:show="successDialog.show"
      :title="successDialog.title"
      :message="successDialog.message"
    />
  </div>
</template>

<style scoped>
.detail-page-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding-bottom: 2rem;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.notice-alert {
  padding: 0.85rem 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
}

.notice-alert.is-error {
  background: rgba(181, 68, 68, 0.08);
  color: var(--danger);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.settings-card {
  background: white;
  border-radius: 20px;
  border: 1px solid var(--border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.action-card {
  margin-top: 1.5rem;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.action-card__content h3 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-strong);
}

.admin-subtitle {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.action-card__buttons {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.6rem 1.25rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn--primary {
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 10px rgba(var(--primary-rgb), 0.15);
}
.btn--primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(var(--primary-rgb), 0.25);
}

.btn--danger {
  background: rgba(181, 68, 68, 0.1);
  color: var(--danger);
}
.btn--danger:hover:not(:disabled) {
  background: var(--danger);
  color: white;
}

.btn--secondary {
  background: var(--bg-soft);
  color: var(--text-strong);
  border-color: var(--border);
}
.btn--secondary:hover:not(:disabled) {
  background: white;
  border-color: var(--primary-soft);
  color: var(--primary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-wrap {
  min-height: 400px;
  display: grid;
  place-items: center;
}

.loading-state {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  color: var(--text-muted);
}
</style>
