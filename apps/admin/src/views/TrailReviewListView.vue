<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshCcw, Search, Eye } from 'lucide-vue-next'

import { fetchAdminTrails } from '../api/admin'
import StatusBadge from '../components/common/StatusBadge.vue'
import type { AdminTrailListItem } from '../types/admin'
import { formatDateTime } from '../utils/format'

const router = useRouter()
const loading = ref(false)
const list = ref<AdminTrailListItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const reviewStatus = ref('')
const authorKeyword = ref('')
const errorMessage = ref('')

async function load(page = pageNum.value) {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminTrails({
      pageNum: page,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      reviewStatus: reviewStatus.value || undefined,
      authorKeyword: authorKeyword.value.trim() || undefined,
    })
    list.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
    pageSize.value = result.pageSize
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '路线列表加载失败'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  reviewStatus.value = ''
  authorKeyword.value = ''
  pageNum.value = 1
  load(1)
}

function openDetail(id: string | number) {
  router.push({ name: 'trail-review-detail', params: { id: String(id) } })
}

function changePage(delta: number) {
  const next = pageNum.value + delta
  if (next < 1) {
    return
  }
  const totalPages = Math.max(1, Math.ceil(total.value / pageSize.value))
  if (next > totalPages) {
    return
  }
  load(next)
}

onMounted(load)
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-header">
      <div>
        <h2 class="admin-title">路线审核</h2>
        <p class="admin-subtitle">查看待审核、已通过与已驳回的路线。</p>
      </div>
      <button class="admin-button admin-button-secondary" type="button" @click="load()">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div class="admin-list-filters admin-grid-3">
      <label>
        <span>关键字</span>
        <input v-model="keyword" class="admin-input" placeholder="路线名称 / 地点" @keyup.enter="load(1)" />
      </label>
      <label>
        <span>审核状态</span>
        <select v-model="reviewStatus" class="admin-select" @change="load(1)">
          <option value="">全部</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已驳回</option>
        </select>
      </label>
      <label>
        <span>作者</span>
        <input v-model="authorKeyword" class="admin-input" placeholder="作者昵称" @keyup.enter="load(1)" />
      </label>
    </div>

    <div class="admin-list-actions">
      <button class="admin-button admin-button-primary" type="button" @click="load(1)">
        <Search :size="16" :stroke-width="2" />
        搜索
      </button>
      <button class="admin-button admin-button-secondary" type="button" @click="resetFilters">
        重置
      </button>
    </div>

    <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

    <div class="admin-table-wrap" v-if="list.length">
      <table class="admin-table">
        <thead>
          <tr>
            <th>路线</th>
            <th>作者</th>
            <th>位置</th>
            <th>状态</th>
            <th>发布时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>
              <div class="admin-trail-cell">
                <img v-if="item.image" :src="item.image" :alt="item.name" />
                <div v-else class="admin-trail-cell__placeholder">Trail</div>
                <div>
                  <strong>{{ item.name }}</strong>
                  <small>编号 {{ item.id }}</small>
                </div>
              </div>
            </td>
            <td>{{ item.authorUsername }}</td>
            <td>{{ item.location || '--' }}</td>
            <td><StatusBadge :status="item.reviewStatus" /></td>
            <td>{{ formatDateTime(item.createdAt) }}</td>
            <td>
              <button class="admin-button admin-button-secondary" type="button" @click="openDetail(item.id)">
                <Eye :size="16" :stroke-width="2" />
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="admin-empty-wrap">
      <div v-if="loading" class="admin-muted">正在加载路线列表...</div>
      <div v-else class="admin-muted">暂无路线数据。</div>
    </div>

    <div class="admin-pagination">
      <span class="admin-muted">第 {{ pageNum }} 页 / 共 {{ Math.max(1, Math.ceil(total / pageSize)) }} 页，共 {{ total }} 条</span>
      <div class="admin-pagination__actions">
        <button class="admin-button admin-button-secondary" type="button" :disabled="pageNum <= 1 || loading" @click="changePage(-1)">上一页</button>
        <button class="admin-button admin-button-secondary" type="button" :disabled="pageNum >= Math.ceil(total / pageSize) || loading" @click="changePage(1)">下一页</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-list-header,
.admin-list-actions,
.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-list-filters {
  margin-top: 1rem;
}

.admin-list-filters label {
  display: grid;
  gap: 0.55rem;
}

.admin-list-filters span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-list-actions {
  margin-top: 1rem;
}

.admin-list-error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-table-wrap {
  margin-top: 1rem;
  overflow-x: auto;
}

.admin-trail-cell {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.admin-trail-cell img,
.admin-trail-cell__placeholder {
  width: 4rem;
  height: 4rem;
  border-radius: 14px;
  object-fit: cover;
  border: 1px solid var(--border);
  flex: none;
}

.admin-trail-cell__placeholder {
  display: grid;
  place-items: center;
  background: var(--bg-soft);
  color: var(--text-muted);
  font-size: 0.78rem;
  font-weight: 700;
}

.admin-trail-cell strong,
.admin-trail-cell small {
  display: block;
}

.admin-trail-cell strong {
  color: var(--text-strong);
  margin-bottom: 0.25rem;
}

.admin-trail-cell small {
  color: var(--text-muted);
}

.admin-empty-wrap {
  display: grid;
  place-items: center;
  min-height: 240px;
}

.admin-pagination {
  margin-top: 1rem;
}

.admin-pagination__actions {
  display: flex;
  gap: 0.6rem;
}
</style>
