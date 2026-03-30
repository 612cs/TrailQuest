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
  <section class="admin-card admin-section">
    <div class="admin-list-header">
      <h2 class="admin-title">举报处理</h2>
      <button class="admin-button admin-button-secondary" type="button" @click="load()">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div class="admin-list-body">
      <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

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
              <td>{{ item.targetId }}</td>
              <td><StatusBadge :status="item.status" /></td>
              <td>{{ item.reason }}</td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <button class="admin-button admin-button-secondary" type="button" :disabled="actionLoading" @click="handleResolve(item.id)">
                  <CheckCheck :size="16" :stroke-width="2" />
                  处理
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <EmptyState
        v-else
        title="暂未接入前台举报来源"
        description="当前版本先预留后台治理入口，等前台举报链路上线后再接入真实数据。"
        :icon="FlagTriangleRight"
      />

      <AdminPagination
        :current="pageNum"
        :total-pages="Math.max(1, Math.ceil(total / pageSize))"
        :total-items="total"
        @update:current="load"
      />
    </div>
  </section>
</template>

<style scoped>
.admin-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.admin-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-list-body {
  flex: 1;
  min-height: 0;
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
}

.admin-list-error {
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-table-wrap {
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: auto;
}

</style>
