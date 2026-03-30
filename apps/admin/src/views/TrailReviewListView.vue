<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshCcw, Search } from 'lucide-vue-next'

import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import TrailReviewTable from '../components/trails/TrailReviewTable.vue'
import { useTrailReviewList } from '../composables/useTrailReviewList'

const router = useRouter()
const {
  loading,
  list,
  total,
  pageNum,
  keyword,
  reviewStatus,
  authorKeyword,
  errorMessage,
  totalPages,
  load,
  resetFilters,
} = useTrailReviewList()

function openDetail(id: string) {
  router.push({ name: 'trail-review-detail', params: { id } })
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-toolbar">
      <input v-model="keyword" class="admin-input" placeholder="路线名称 / 地点" aria-label="关键词" @keyup.enter="load(1)" />
      <select v-model="reviewStatus" class="admin-select" aria-label="审核状态" @change="load(1)">
          <option value="">全部</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已驳回</option>
      </select>
      <input v-model="authorKeyword" class="admin-input" placeholder="作者昵称" aria-label="作者" @keyup.enter="load(1)" />
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

      <TrailReviewTable v-if="list.length" :items="list" @open="openDetail" />

      <div v-else class="admin-empty-wrap">
        <div v-if="loading" class="admin-muted">正在加载路线列表...</div>
        <EmptyState
          v-else
          title="暂无路线数据"
          description="当前筛选条件下没有匹配的审核记录。"
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
  flex-wrap: nowrap;
}

.admin-list-toolbar > .admin-input,
.admin-list-toolbar > .admin-select {
  flex: 1 1 14rem;
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
  .admin-list-toolbar {
    flex-wrap: wrap;
  }

  .admin-list-toolbar__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
