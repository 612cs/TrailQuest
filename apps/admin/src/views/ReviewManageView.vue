<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RefreshCcw, Search, Trash2 } from 'lucide-vue-next'

import { deleteAdminReview, fetchAdminReviews } from '../api/admin'
import AdminPagination from '../components/common/AdminPagination.vue'
import EmptyState from '../components/common/EmptyState.vue'
import { formatDateTime } from '../utils/format'
import type { AdminReviewListItem } from '../types/admin'

const loading = ref(false)
const list = ref<AdminReviewListItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const trailKeyword = ref('')
const authorKeyword = ref('')
const errorMessage = ref('')

async function load(page = pageNum.value) {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminReviews({
      pageNum: page,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      trailKeyword: trailKeyword.value.trim() || undefined,
      authorKeyword: authorKeyword.value.trim() || undefined,
    })
    list.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
    pageSize.value = result.pageSize
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '评论列表加载失败'
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: string | number) {
  if (!window.confirm('确认删除这条评论吗？')) {
    return
  }
  loading.value = true
  try {
    await deleteAdminReview(id)
    await load(pageNum.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '删除评论失败'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  trailKeyword.value = ''
  authorKeyword.value = ''
  load(1)
}

onMounted(load)
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-toolbar">
      <input v-model="keyword" class="admin-input" placeholder="评论内容" aria-label="评论关键字" @keyup.enter="load(1)" />
      <input v-model="trailKeyword" class="admin-input" placeholder="路线名称" aria-label="路线关键字" @keyup.enter="load(1)" />
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

      <div v-if="list.length" class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>评论</th>
              <th>作者</th>
              <th>路线</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td class="admin-review-preview">{{ item.text }}</td>
              <td>{{ item.authorUsername }}</td>
              <td>{{ item.trailName }}</td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <button class="admin-button admin-button-danger" type="button" :disabled="loading" @click="handleDelete(item.id)">
                  <Trash2 :size="16" :stroke-width="2" />
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <EmptyState
        v-else-if="!loading"
        title="暂无评论"
        description="当前筛选条件下没有评论数据。"
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

.admin-list-toolbar,
.admin-list-toolbar__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-list-toolbar {
  align-items: end;
  flex-wrap: wrap;
}

.admin-list-toolbar > .admin-input,
.admin-list-toolbar > .admin-select {
  flex: 1 1 14rem;
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

.admin-table-wrap {
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: auto;
}

.admin-review-preview {
  max-width: 30rem;
  color: var(--text-strong);
  line-height: 1.65;
}

</style>
