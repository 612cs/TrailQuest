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
  <section class="admin-card admin-section">
    <div class="admin-detail-header">
      <button class="admin-button admin-button-secondary" type="button" @click="router.push({ name: 'trail-manage-list' })">
        <ArrowLeft :size="16" :stroke-width="2" />
        返回列表
      </button>
      <button class="admin-button admin-button-secondary" type="button" @click="loadDetail">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div v-if="errorMessage" class="admin-detail-error">{{ errorMessage }}</div>

    <template v-if="detail">
      <TrailDetailContent :detail="detail" />

      <section class="admin-card admin-detail-card">
        <h3>路线治理</h3>
        <p class="admin-subtitle">针对已进入系统的路线执行下架或恢复，默认不改变审核状态。</p>
        <div class="admin-detail-actions">
          <button
            class="admin-button"
            :class="isOffline ? 'admin-button-primary' : 'admin-button-danger'"
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

    <EmptyState
      v-else-if="!loading"
      title="路线不存在"
      description="当前路线管理记录不存在。"
      :icon="ImageIcon"
    />

    <div v-else class="admin-detail-loading">正在加载路线详情...</div>

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
  </section>
</template>

<style scoped>
.admin-detail-header,
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
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-detail-card {
  margin-top: 1rem;
  padding: 1.1rem;
}

.admin-detail-card h3 {
  margin: 0 0 0.6rem;
}

.admin-detail-actions {
  justify-content: flex-start;
  margin-top: 1rem;
}

.admin-detail-loading {
  margin-top: 1rem;
  color: var(--text-muted);
}
</style>
