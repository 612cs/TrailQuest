<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { FlagTriangleRight, RefreshCcw, CheckCheck } from 'lucide-vue-next'

import { fetchAdminReports, resolveReport } from '../api/admin'
import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import StatusBadge from '../components/common/StatusBadge.vue'
import type { AdminReportListItem } from '../types/admin'
import { formatDateTime } from '../utils/format'

const loading = ref(false)
const list = ref<AdminReportListItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')
const actionLoading = ref(false)

async function load(page = pageNum.value) {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminReports({ pageNum: page, pageSize: pageSize.value })
    list.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
    pageSize.value = result.pageSize
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '举报列表加载失败'
  } finally {
    loading.value = false
  }
}

async function handleResolve(id: string | number) {
  actionLoading.value = true
  errorMessage.value = ''
  try {
    await resolveReport(id)
    await load(pageNum.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '处理举报失败'
  } finally {
    actionLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="list-page-container">
    <!-- Header -->
    <header class="page-header">
      <h1 class="page-title">举报处理</h1>
      <p class="page-subtitle">管理用户举报信息，及时处理违规内容，维护社区健康环境。</p>
    </header>

    <!-- Main List Card -->
    <section class="settings-card list-view-card">
      <div class="list-toolbar">
        <div class="notice-alert is-warning">当前举报入口仍在前台接入中，先保留治理入口。</div>
        <div class="action-group">
          <button class="btn btn--secondary" type="button" @click="load()">
            <RefreshCcw :size="16" :stroke-width="2" /> 刷新
          </button>
        </div>
      </div>

      <div class="list-body">
        <div v-if="errorMessage" class="notice-alert is-error">{{ errorMessage }}</div>

        <div v-if="list.length" class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>对象类型</th>
                <th>对象 ID</th>
                <th>状态</th>
                <th>原因</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in list" :key="item.id">
                <td>{{ item.targetType }}</td>
                <td class="id-col">{{ item.targetId }}</td>
                <td><StatusBadge :status="item.status" /></td>
                <td>{{ item.reason }}</td>
                <td class="time-col">{{ formatDateTime(item.createdAt) }}</td>
                <td>
                  <button class="btn btn--secondary action-btn" type="button" :disabled="actionLoading" @click="handleResolve(item.id)">
                    <CheckCheck :size="16" :stroke-width="2" /> 处理
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-wrap">
          <EmptyState
            title="暂未接入前台举报来源"
            description="当前版本先预留后台治理入口，等前台举报链路上线后再接入真实数据。"
            :icon="FlagTriangleRight"
          />
        </div>

        <AdminPagination
          class="pagination-wrap"
          :current="pageNum"
          :total-pages="Math.max(1, Math.ceil(total / pageSize))"
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

.notice-alert {
  padding: 0.85rem 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  flex: 1;
}

.notice-alert.is-warning {
  background: rgba(var(--primary-rgb), 0.08);
  color: var(--primary);
  border: 1px solid rgba(var(--primary-rgb), 0.16);
  margin: 0;
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

.action-btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.8125rem;
}

/* Body & Table */
.list-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notice-alert.is-error {
  background: rgba(181, 68, 68, 0.08);
  color: var(--danger);
  border: 1px solid rgba(181, 68, 68, 0.16);
  margin-bottom: 1rem;
}

.empty-wrap {
  flex: 1;
  display: grid;
  place-items: center;
  min-height: 300px;
}

.admin-table-wrap {
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: auto;
  border-radius: 12px;
  border: 1px solid var(--border);
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.875rem;
  min-width: 800px;
}

.admin-table th {
  background: var(--bg-soft);
  padding: 1rem;
  font-weight: 700;
  color: var(--text-muted);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.admin-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--border);
  color: var(--text-strong);
  vertical-align: middle;
}

.admin-table tr:hover td {
  background: rgba(var(--bg-soft-rgb), 0.5);
}

.admin-table tr:last-child td {
  border-bottom: none;
}

.id-col {
  font-family: monospace;
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.time-col {
  color: var(--text-muted);
  white-space: nowrap;
}

.pagination-wrap {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
}
</style>
