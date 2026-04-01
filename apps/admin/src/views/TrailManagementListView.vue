<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshCcw, Search } from 'lucide-vue-next'

import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import TrailManagementTable from '../components/trails/TrailManagementTable.vue'
import { useTrailManagementList } from '../composables/useTrailManagementList'

const router = useRouter()
const {
  loading,
  list,
  total,
  pageNum,
  status,
  keyword,
  authorKeyword,
  errorMessage,
  totalPages,
  load,
  resetFilters,
} = useTrailManagementList()

function openDetail(id: string) {
  router.push({ name: 'trail-manage-detail', params: { id } })
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="list-page-container">
    <!-- Header -->
    <header class="page-header">
      <h1 class="page-title">路线管理</h1>
      <p class="page-subtitle">管理、审核并维护社区内所有的徒步路线数据与状态。</p>
    </header>

    <!-- Main List Card -->
    <section class="settings-card list-view-card">
      <div class="list-toolbar">
        <div class="filter-group">
          <select v-model="status" class="styled-select" aria-label="展示状态" @change="load(1)">
            <option value="">全部状态</option>
            <option value="active">正常展示</option>
            <option value="deleted">已下架</option>
          </select>
          <input v-model="keyword" class="styled-input" placeholder="输入路线名称 / 地理位置" aria-label="关键词" @keyup.enter="load(1)" />
          <input v-model="authorKeyword" class="styled-input" placeholder="输入作者昵称" aria-label="作者" @keyup.enter="load(1)" />
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

        <TrailManagementTable v-if="list.length" :items="list" @open="openDetail" />

        <div v-else class="empty-wrap">
          <div v-if="loading" class="loading-state">
            <RefreshCcw class="animate-spin" :size="24" />
            <p>正在加载路线列表...</p>
          </div>
          <EmptyState
            v-else
            title="暂无路线数据"
            description="当前筛选条件下没有匹配的路线，请尝试调整搜索条件。"
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
  align-items: center;
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

.styled-input, .styled-select {
  background: var(--bg-soft);
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 0.6rem 1rem;
  font-size: 0.875rem;
  color: var(--text-strong);
  min-width: 140px;
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
  .filter-group {
    flex: 1 1 100%;
  }
  .action-group {
    flex: 1 1 100%;
    justify-content: flex-end;
  }
}
</style>
