<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RefreshCcw, Search, ShieldCheck, UsersRound } from 'lucide-vue-next'

import { fetchAdminUsers } from '../api/admin'
import EmptyState from '../components/common/EmptyState.vue'
import { formatDateTime } from '../utils/format'
import type { AdminUserListItem } from '../types/admin'

const loading = ref(false)
const list = ref<AdminUserListItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const role = ref('')
const errorMessage = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

async function load(page = pageNum.value) {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminUsers({
      pageNum: page,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      role: role.value || undefined,
    })
    list.value = result.list
    total.value = result.total
    pageNum.value = result.pageNum
    pageSize.value = result.pageSize
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户列表加载失败'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  role.value = ''
  void load(1)
}

function changePage(delta: number) {
  const next = pageNum.value + delta
  if (next < 1 || next > totalPages.value) {
    return
  }
  void load(next)
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="admin-card admin-section">
    <div class="admin-list-header">
      <div>
        <h2 class="admin-title">用户管理</h2>
        <p class="admin-subtitle">查看当前系统用户规模、角色分布与发布活跃度。</p>
      </div>
      <button class="admin-button admin-button-secondary" type="button" @click="load()">
        <RefreshCcw :size="16" :stroke-width="2" />
        刷新
      </button>
    </div>

    <div class="admin-list-filters admin-grid-2">
      <label>
        <span>关键词</span>
        <input v-model="keyword" class="admin-input" placeholder="用户名、邮箱或所在地" @keyup.enter="load(1)" />
      </label>
      <label>
        <span>角色</span>
        <select v-model="role" class="admin-select">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
        </select>
      </label>
    </div>

    <div class="admin-list-actions">
      <button class="admin-button admin-button-primary" type="button" @click="load(1)">
        <Search :size="16" :stroke-width="2" />
        搜索
      </button>
      <button class="admin-button admin-button-secondary" type="button" @click="resetFilters">重置</button>
    </div>

    <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

    <div v-if="list.length" class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>所在地</th>
            <th>已发布路线</th>
            <th>注册时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>
              <div class="admin-user-cell">
                <div class="admin-user-cell__avatar" :style="item.avatarMediaUrl ? '' : `background:${item.avatarBg || 'var(--bg-soft)'}`">
                  <img v-if="item.avatarMediaUrl" :src="item.avatarMediaUrl" :alt="item.username" />
                  <span v-else>{{ item.avatar || item.username.slice(0, 2).toUpperCase() }}</span>
                </div>
                <div>
                  <strong>{{ item.username }}</strong>
                  <small>ID {{ item.id }}</small>
                </div>
              </div>
            </td>
            <td>{{ item.email }}</td>
            <td>
              <span class="admin-badge" :class="item.role === 'ADMIN' ? 'admin-badge-approved' : 'admin-badge-neutral'">
                <ShieldCheck v-if="item.role === 'ADMIN'" :size="14" :stroke-width="2" />
                {{ item.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>{{ item.location || '--' }}</td>
            <td>{{ item.publishedTrailCount }}</td>
            <td>{{ formatDateTime(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <EmptyState
      v-else-if="!loading"
      title="暂无用户数据"
      description="当前筛选条件下没有匹配的用户。"
      :icon="UsersRound"
    />

    <div class="admin-pagination">
      <span class="admin-muted">第 {{ pageNum }} 页 / 共 {{ totalPages }} 页，共 {{ total }} 位用户</span>
      <div class="admin-pagination__actions">
        <button class="admin-button admin-button-secondary" type="button" :disabled="pageNum <= 1 || loading" @click="changePage(-1)">上一页</button>
        <button class="admin-button admin-button-secondary" type="button" :disabled="pageNum >= totalPages || loading" @click="changePage(1)">下一页</button>
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

.admin-user-cell {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.admin-user-cell__avatar {
  width: 2.75rem;
  height: 2.75rem;
  overflow: hidden;
  display: grid;
  place-items: center;
  border-radius: 14px;
  border: 1px solid var(--border);
  color: #fff;
  font-weight: 700;
}

.admin-user-cell__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-user-cell strong,
.admin-user-cell small {
  display: block;
}

.admin-user-cell small {
  margin-top: 0.25rem;
  color: var(--text-muted);
}

.admin-pagination {
  margin-top: 1rem;
}

.admin-pagination__actions {
  display: flex;
  gap: 0.6rem;
}
</style>
