<script setup lang="ts">
import { onMounted } from 'vue'
import { RefreshCcw, Search, ScrollText } from 'lucide-vue-next'

import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import OperationLogDetailDialog from '../components/logs/OperationLogDetailDialog.vue'
import OperationLogTable from '../components/logs/OperationLogTable.vue'
import { useOperationLogList } from '../composables/useOperationLogList'

const {
  loading,
  detailLoading,
  list,
  total,
  pageNum,
  moduleCode,
  actionCode,
  operatorKeyword,
  targetType,
  targetId,
  dateFrom,
  dateTo,
  errorMessage,
  totalPages,
  detailVisible,
  detail,
  load,
  resetFilters,
  openDetail,
  closeDetail,
} = useOperationLogList()

const moduleOptions = [
  { value: '', label: '全部模块' },
  { value: 'user_management', label: '用户管理' },
  { value: 'trail_review', label: '路线审核' },
  { value: 'trail_management', label: '路线管理' },
  { value: 'review_management', label: '评论治理' },
  { value: 'report_management', label: '举报处理' },
  { value: 'setting_management', label: '系统设置' },
  { value: 'config_center', label: '配置中心' },
] as const

const actionOptions = [
  { value: '', label: '全部动作' },
  { value: 'user_ban', label: '封禁用户' },
  { value: 'user_unban', label: '解封用户' },
  { value: 'trail_approve', label: '通过路线审核' },
  { value: 'trail_reject', label: '驳回路线审核' },
  { value: 'trail_offline', label: '下架路线' },
  { value: 'trail_restore', label: '恢复路线' },
  { value: 'review_hide', label: '隐藏评论' },
  { value: 'review_restore', label: '恢复评论' },
  { value: 'review_delete', label: '删除评论' },
  { value: 'report_resolve', label: '处理举报' },
  { value: 'home_hero_update', label: '更新首页大图' },
  { value: 'option_item_create', label: '创建配置项' },
  { value: 'option_item_update', label: '更新配置项' },
  { value: 'option_item_toggle', label: '切换配置项状态' },
] as const

const targetTypeOptions = [
  { value: '', label: '全部对象' },
  { value: 'user', label: '用户' },
  { value: 'trail', label: '路线' },
  { value: 'review', label: '评论' },
  { value: 'report', label: '举报' },
  { value: 'setting', label: '设置' },
  { value: 'option_item', label: '配置项' },
] as const

function moduleLabel(code: string) {
  return moduleOptions.find((item) => item.value === code)?.label || code || '--'
}

function actionLabel(code: string) {
  return actionOptions.find((item) => item.value === code)?.label || code || '--'
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-toolbar">
      <select v-model="moduleCode" class="admin-select" aria-label="模块" @change="load(1)">
        <option v-for="item in moduleOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <select v-model="actionCode" class="admin-select" aria-label="动作" @change="load(1)">
        <option v-for="item in actionOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <input v-model="operatorKeyword" class="admin-input" placeholder="操作人" aria-label="操作人" @keyup.enter="load(1)" />
      <select v-model="targetType" class="admin-select" aria-label="对象类型" @change="load(1)">
        <option v-for="item in targetTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <input v-model="targetId" class="admin-input" placeholder="对象 ID" aria-label="对象 ID" @keyup.enter="load(1)" />
      <input v-model="dateFrom" class="admin-input" type="date" aria-label="开始日期" @change="load(1)" />
      <input v-model="dateTo" class="admin-input" type="date" aria-label="结束日期" @change="load(1)" />
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

    <div class="admin-list-body">
      <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

      <OperationLogTable
        v-if="list.length"
        :items="list"
        :module-label="moduleLabel"
        :action-label="actionLabel"
        @detail="openDetail"
      />

      <div v-else class="admin-empty-wrap">
        <div v-if="loading" class="admin-muted">正在加载操作日志...</div>
        <EmptyState
          v-else
          title="暂无操作日志"
          description="当前筛选条件下没有匹配的后台操作记录。"
          :icon="ScrollText"
        />
      </div>

      <AdminPagination
        :current="pageNum"
        :total-pages="totalPages"
        :total-items="total"
        @update:current="load"
      />
    </div>

    <OperationLogDetailDialog
      :show="detailVisible"
      :detail="detail"
      :loading="detailLoading"
      :module-label="moduleLabel"
      :action-label="actionLabel"
      @update:show="closeDetail"
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
