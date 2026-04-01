<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, EyeOff, ImageIcon, RefreshCcw, RotateCcw } from 'lucide-vue-next'

import { fetchAdminTrailManagementDetail, offlineTrail, restoreTrail } from '../api/admin'
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
const confirmDialogVisible = ref(false)
const successDialog = ref({
  show: false,
  title: '',
  message: '',
})

const trailId = computed(() => String(route.params.id || ''))
const isOffline = computed(() => detail.value?.status === 'deleted')

async function loadDetail() {
  if (!trailId.value) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    detail.value = await fetchAdminTrailManagementDetail(trailId.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '路线详情加载失败'
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  if (!trailId.value || !detail.value) {
    return
  }
  actionLoading.value = true
  errorMessage.value = ''
  try {
    if (isOffline.value) {
      await restoreTrail(trailId.value)
      successDialog.value = {
        show: true,
        title: '路线已恢复',
        message: detail.value.reviewStatus === 'approved'
          ? '路线已恢复为公开可见状态。'
          : '路线已恢复记录存在状态，但当前审核状态仍不允许公开展示。',
      }
    } else {
      await offlineTrail(trailId.value)
      successDialog.value = {
        show: true,
        title: '路线已下架',
        message: '该路线已从前台公开列表和搜索结果中移除。',
      }
    }
    confirmDialogVisible.value = false
    await loadDetail()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '路线治理操作失败'
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
      <button class="btn btn--secondary" type="button" @click="router.push({ name: 'trail-manage-list' })">
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
          <h3>路线治理</h3>
          <p class="admin-subtitle">针对已进入系统的路线执行下架或恢复，默认不改变审核状态。</p>
        </div>
        <div class="action-card__buttons">
          <button
            class="btn"
            :class="isOffline ? 'btn--primary' : 'btn--danger'"
            type="button"
            :disabled="actionLoading"
            @click="confirmDialogVisible = true"
          >
            <RotateCcw v-if="isOffline" :size="16" :stroke-width="2" />
            <EyeOff v-else :size="16" :stroke-width="2" />
            {{ isOffline ? '恢复路线' : '下架路线' }}
          </button>
        </div>
      </section>
    </template>

    <div v-else-if="!loading" class="empty-wrap settings-card">
      <EmptyState
        title="路线不存在"
        description="当前路线管理记录不存在。"
        :icon="ImageIcon"
      />
    </div>

    <div v-else class="loading-state">
      <RefreshCcw class="animate-spin" :size="32" />
      <p>正在加载路线详情...</p>
    </div>

    <AdminConfirmDialog
      v-model:show="confirmDialogVisible"
      :title="isOffline ? '确认恢复路线' : '确认下架路线'"
      :message="isOffline
        ? '恢复后若该路线审核状态仍为已通过，将重新在前台公开展示。'
        : '下架后该路线不会出现在前台公开列表与搜索结果中，但后台仍可恢复。'"
      :confirm-text="isOffline ? '确认恢复' : '确认下架'"
      :loading="actionLoading"
      @confirm="handleConfirm"
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
