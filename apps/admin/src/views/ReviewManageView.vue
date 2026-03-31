<script setup lang="ts">
import { onMounted } from 'vue'
import { MessageSquareMore, RefreshCcw, Search } from 'lucide-vue-next'

import AdminConfirmDialog from '../components/common/AdminConfirmDialog.vue'
import AdminNoticeDialog from '../components/common/AdminNoticeDialog.vue'
import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import ReviewBatchActionBar from '../components/reviews/ReviewBatchActionBar.vue'
import ReviewDetailDialog from '../components/reviews/ReviewDetailDialog.vue'
import ReviewManagementTable from '../components/reviews/ReviewManagementTable.vue'
import { useReviewManagement } from '../composables/useReviewManagement'

const {
  loading,
  actionLoading,
  detailLoading,
  list,
  total,
  pageNum,
  keyword,
  trailKeyword,
  authorKeyword,
  status,
  errorMessage,
  totalPages,
  selectedIds,
  selectedActiveIds,
  selectedRestorableIds,
  allCurrentSelected,
  detailVisible,
  detail,
  confirmVisible,
  confirmConfig,
  noticeVisible,
  noticeTitle,
  noticeMessage,
  load,
  resetFilters,
  toggleSelection,
  toggleSelectAllCurrent,
  openDetail,
  closeDetail,
  requestHide,
  requestRestore,
  requestDelete,
  requestBatchHide,
  requestBatchRestore,
  closeConfirm,
  submitConfirm,
} = useReviewManagement()

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-toolbar">
      <input
        v-model="keyword"
        class="admin-input"
        placeholder="评论内容"
        aria-label="评论关键字"
        @keyup.enter="load(1)"
      />
      <input
        v-model="trailKeyword"
        class="admin-input"
        placeholder="路线名称"
        aria-label="路线关键字"
        @keyup.enter="load(1)"
      />
      <input
        v-model="authorKeyword"
        class="admin-input"
        placeholder="作者昵称"
        aria-label="作者"
        @keyup.enter="load(1)"
      />
      <select
        v-model="status"
        class="admin-select"
        aria-label="评论状态"
        @change="load(1)"
      >
        <option value="">全部状态</option>
        <option value="active">正常</option>
        <option value="hidden">已隐藏</option>
        <option value="deleted">已删除</option>
      </select>
      <div class="admin-list-toolbar__actions">
        <button class="admin-button admin-button-primary" type="button" @click="load(1)">
          <Search :size="16" :stroke-width="2" />
          搜索
        </button>
        <button class="admin-button admin-button-secondary" type="button" @click="resetFilters">重置</button>
        <button class="admin-button admin-button-secondary" type="button" @click="load()">
          <RefreshCcw :size="16" :stroke-width="2" />
          刷新
        </button>
      </div>
    </div>

    <ReviewBatchActionBar
      v-if="selectedIds.length"
      :selected-count="selectedIds.length"
      :active-count="selectedActiveIds.length"
      :restorable-count="selectedRestorableIds.length"
      @batch-hide="requestBatchHide"
      @batch-restore="requestBatchRestore"
      @clear="toggleSelectAllCurrent(false)"
    />

    <div class="admin-list-body">
      <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

      <ReviewManagementTable
        v-if="list.length"
        :items="list"
        :selected-ids="selectedIds"
        :all-current-selected="allCurrentSelected"
        @toggle-select-all="toggleSelectAllCurrent"
        @toggle-select="toggleSelection($event.id, $event.checked)"
        @detail="openDetail($event.id)"
        @hide="requestHide"
        @restore="requestRestore"
        @delete="requestDelete"
      />

      <div v-else class="admin-empty-wrap">
        <div v-if="loading" class="admin-muted">正在加载评论治理列表...</div>
        <EmptyState
          v-else
          title="暂无评论数据"
          description="当前筛选条件下没有匹配的评论。"
          :icon="MessageSquareMore"
        />
      </div>

      <AdminPagination
        :current="pageNum"
        :total-pages="totalPages"
        :total-items="total"
        @update:current="load"
      />
    </div>

    <ReviewDetailDialog
      :show="detailVisible"
      :detail="detail"
      :loading="detailLoading"
      @update:show="closeDetail"
    />

    <AdminConfirmDialog
      v-model:show="confirmVisible"
      :title="confirmConfig.title"
      :message="confirmConfig.message"
      :confirm-text="confirmConfig.confirmText"
      :loading="actionLoading"
      :require-reason="confirmConfig.requireReason"
      :reason-label="confirmConfig.reasonLabel"
      :reason-placeholder="confirmConfig.reasonPlaceholder"
      @update:show="!$event && closeConfirm()"
      @confirm="submitConfirm"
    />

    <AdminNoticeDialog
      v-model:show="noticeVisible"
      :title="noticeTitle"
      :message="noticeMessage"
    />
  </section>
</template>

<style scoped>
.admin-section {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.admin-list-toolbar,
.admin-list-toolbar__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-list-toolbar {
  align-items: center;
  flex-wrap: wrap;
}

.admin-list-toolbar > .admin-input,
.admin-list-toolbar > .admin-select {
  flex: 1 1 12rem;
  min-width: 0;
}

.admin-list-toolbar__actions {
  flex: 0 0 auto;
  align-items: center;
  flex-wrap: nowrap;
}

.admin-list-body {
  flex: 1;
  min-height: 0;
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-list-error {
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-empty-wrap {
  flex: 1;
  min-height: 0;
  display: grid;
  place-items: center;
}

@media (max-width: 1200px) {
  .admin-list-toolbar__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
