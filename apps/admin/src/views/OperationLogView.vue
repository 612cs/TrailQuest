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
  <div class="list-page-container">
    <header class="page-header">
      <h1 class="page-title">操作日志</h1>
      <p class="page-subtitle">追溯管理员在系统内的关键操作与历史记录，便于安全审计与故障排查。</p>
    </header>

    <section class="settings-card list-view-card">
      <div class="list-toolbar">
        <div class="filter-group filter-group--large">
          <select v-model="moduleCode" class="styled-select" aria-label="模块" @change="load(1)">
            <option v-for="item in moduleOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <select v-model="actionCode" class="styled-select" aria-label="动作" @change="load(1)">
            <option v-for="item in actionOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <input v-model="operatorKeyword" class="styled-input" placeholder="操作人" aria-label="操作人" @keyup.enter="load(1)" />
          <select v-model="targetType" class="styled-select" aria-label="对象类型" @change="load(1)">
            <option v-for="item in targetTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <input v-model="targetId" class="styled-input" placeholder="对象 ID" aria-label="对象 ID" @keyup.enter="load(1)" />
          <div class="date-range">
            <input v-model="dateFrom" class="styled-input" type="date" aria-label="开始日期" @change="load(1)" />
            <span class="date-sep">-</span>
            <input v-model="dateTo" class="styled-input" type="date" aria-label="结束日期" @change="load(1)" />
          </div>
        </div>
        
        <div class="action-group">
          <button class="btn btn--primary" type="button" @click="load(1)">
            <Search :size="16" :stroke-width="2" /> 搜索
          </button>
          <button class="btn btn--secondary" type="button" @click="resetFilters">重置</button>
          <button class="btn btn--secondary" type="button" @click="load()">
            <RefreshCcw :size="16" :stroke-width="2" /> 刷新
          </button>
        </div>
      </div>

      <div class="list-body">
        <div v-if="errorMessage" class="notice-alert is-error">{{ errorMessage }}</div>

        <OperationLogTable
          v-if="list.length"
          :items="list"
          :module-label="moduleLabel"
          :action-label="actionLabel"
          @detail="openDetail"
        />

        <div v-else class="empty-wrap">
          <div v-if="loading" class="loading-state">
            <RefreshCcw class="animate-spin" :size="24" />
            <p>正在加载操作日志...</p>
          </div>
          <EmptyState
            v-else
            title="暂无操作日志"
            description="当前筛选条件下没有匹配的后台操作记录。"
            :icon="ScrollText"
          />
        </div>

        <AdminPagination
          class="pagination-wrap"
          :current="pageNum"
          :total-pages="totalPages"
          :total-items="total"
          @update:current="load"
        />
      </div>
    </section>

    <OperationLogDetailDialog
      :show="detailVisible"
      :detail="detail"
      :loading="detailLoading"
      :module-label="moduleLabel"
      :action-label="actionLabel"
      @update:show="closeDetail"
    />
  </div>
</template>

<style scoped>
.list-page-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  height: 100%;
  padding: 0;
}

.page-header {
  margin-bottom: 0.5rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 0.9375rem;
  color: var(--text-muted);
  margin: 0.5rem 0 0;
}

.settings-card {
  background: white;
  border-radius: 20px;
  border: 1px solid var(--border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.list-view-card {
  flex: 1;
  min-height: 0;
  padding: 1.5rem;
}

/* Toolbar */
.list-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 0.75rem;
  flex: 1;
  flex-wrap: wrap;
}

.filter-group--large .styled-input,
.filter-group--large .styled-select {
  flex: 1 1 140px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1 1 280px;
}

.date-sep {
  color: var(--text-muted);
}

.styled-input, .styled-select {
  background: var(--bg-soft);
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 0.6rem 1rem;
  font-size: 0.875rem;
  color: var(--text-strong);
  min-width: 0;
  flex: 1;
  transition: all 0.2s;
}

.styled-input:focus, .styled-select:focus {
  outline: none;
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.action-group {
  display: flex;
  gap: 0.75rem;
  padding-top: 0.2rem;
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
  white-space: nowrap;
}

.btn--primary {
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 10px rgba(var(--primary-rgb), 0.15);
}
.btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(var(--primary-rgb), 0.25);
}

.btn--secondary {
  background: var(--bg-soft);
  color: var(--text-strong);
  border-color: var(--border);
}
.btn--secondary:hover {
  background: white;
  border-color: var(--primary-soft);
  color: var(--primary);
}

/* Body */
.list-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notice-alert {
  padding: 0.85rem 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.notice-alert.is-error {
  background: rgba(181, 68, 68, 0.08);
  color: var(--danger);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.empty-wrap {
  flex: 1;
  display: grid;
  place-items: center;
  min-height: 300px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-muted);
}

.pagination-wrap {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
}

@media (max-width: 1200px) {
  .list-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .action-group {
    justify-content: flex-end;
  }
}
</style>
