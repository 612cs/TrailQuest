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
  <section class="admin-card admin-section">
    <div class="admin-list-header">
      <h2 class="admin-title">路线管理</h2>
      <button class="admin-button admin-button-secondary" type="button" @click="load()">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div class="admin-list-toolbar">
      <label>
        <span>展示状态</span>
        <select v-model="status" class="admin-select" @change="load(1)">
          <option value="">全部</option>
          <option value="active">正常</option>
          <option value="deleted">已下架</option>
        </select>
      </label>
      <label>
        <span>关键词</span>
        <input v-model="keyword" class="admin-input" placeholder="路线名称 / 地点" @keyup.enter="load(1)" />
      </label>
      <label>
        <span>作者</span>
        <input v-model="authorKeyword" class="admin-input" placeholder="作者昵称" @keyup.enter="load(1)" />
      </label>
      <div class="admin-list-toolbar__actions">
        <button class="admin-button admin-button-primary" type="button" @click="load(1)">
          <Search :size="16" :stroke-width="2" />
          搜索
        </button>
        <button class="admin-button admin-button-secondary" type="button" @click="resetFilters">重置</button>
      </div>
    </div>

    <div class="admin-list-body">
      <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

      <TrailManagementTable v-if="list.length" :items="list" @open="openDetail" />

      <div v-else class="admin-empty-wrap">
        <div v-if="loading" class="admin-muted">正在加载路线管理列表...</div>
        <EmptyState
          v-else
          title="暂无路线数据"
          description="当前筛选条件下没有匹配的路线。"
        />
      </div>

      <AdminPagination
        :current="pageNum"
        :total-pages="totalPages"
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

.admin-list-header,
.admin-list-toolbar,
.admin-list-toolbar__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-list-toolbar {
  margin-top: 1rem;
  align-items: end;
  flex-wrap: wrap;
}

.admin-list-toolbar label {
  flex: 1 1 18rem;
  display: grid;
  gap: 0.55rem;
}

.admin-list-toolbar span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-list-toolbar__actions {
  flex: 0 0 auto;
  align-items: center;
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

.admin-empty-wrap {
  flex: 1;
  min-height: 0;
  display: grid;
  place-items: center;
}
</style>
