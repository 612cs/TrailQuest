<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RefreshCcw, Search, UsersRound } from 'lucide-vue-next'

import AdminPagination from '../components/common/AdminPagination.vue'
import AdminConfirmDialog from '../components/common/AdminConfirmDialog.vue'
import AdminNoticeDialog from '../components/common/AdminNoticeDialog.vue'
import EmptyState from '../components/common/EmptyState.vue'
import UserManagementTable from '../components/users/UserManagementTable.vue'
import { useUserManagement } from '../composables/useUserManagement'
import type { AdminUserListItem } from '../types/admin'

const {
  loading,
  actionLoading,
  list,
  total,
  pageNum,
  keyword,
  role,
  errorMessage,
  totalPages,
  load,
  resetFilters,
  banUser,
  unbanUser,
} = useUserManagement()

const targetUser = ref<AdminUserListItem | null>(null)
const banDialogVisible = ref(false)
const unbanDialogVisible = ref(false)
const successDialog = ref({
  show: false,
  title: '',
  message: '',
})

function openBanDialog(user: AdminUserListItem) {
  targetUser.value = user
  banDialogVisible.value = true
}

function openUnbanDialog(user: AdminUserListItem) {
  targetUser.value = user
  unbanDialogVisible.value = true
}

async function handleBan(reason: string) {
  if (!targetUser.value) {
    return
  }
  try {
    await banUser(targetUser.value.id, reason)
    banDialogVisible.value = false
    successDialog.value = {
      show: true,
      title: '用户已封禁',
      message: `${targetUser.value.username} 已被封禁，后续将无法登录和进行互动。`,
    }
  } catch {
    return
  }
}

async function handleUnban() {
  if (!targetUser.value) {
    return
  }
  try {
    await unbanUser(targetUser.value.id)
    unbanDialogVisible.value = false
    successDialog.value = {
      show: true,
      title: '用户已解封',
      message: `${targetUser.value.username} 已恢复正常状态。`,
    }
  } catch {
    return
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="list-page-container">
    <!-- Header -->
    <header class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">检索平台用户，执行封禁、解封等账号级管控策略以维护社区秩序。</p>
    </header>

    <!-- Main List Card -->
    <section class="settings-card list-view-card">
      <div class="list-toolbar">
        <div class="filter-group">
          <input v-model="keyword" class="styled-input" placeholder="按用户名、邮箱或所在地检索" aria-label="关键词" @keyup.enter="load(1)" />
          <select v-model="role" class="styled-select" aria-label="角色" @change="load(1)">
            <option value="">全部系统角色</option>
            <option value="USER">普通注册用户</option>
            <option value="ADMIN">系统管理员</option>
          </select>
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

        <UserManagementTable
          v-if="list.length"
          :items="list"
          :action-loading="actionLoading"
          @ban="openBanDialog"
          @unban="openUnbanDialog"
        />

        <div v-else class="empty-wrap">
          <div v-if="loading" class="loading-state">
            <RefreshCcw class="animate-spin" :size="24" />
            <p>正在加载用户数据...</p>
          </div>
          <EmptyState
            v-else
            title="暂无可匹配用户"
            description="当前筛选条件下没有匹配的用户记录，请放宽搜索限制。"
            :icon="UsersRound"
          />
        </div>

        <AdminPagination
          class="pagination-wrap"
          :current="pageNum"
          :total-pages="totalPages"
          :total-items="total"
          item-label="位用户"
          @update:current="load"
        />
      </div>
    </section>

    <!-- Dialogs -->
    <AdminConfirmDialog
      v-model:show="banDialogVisible"
      title="确认封禁用户"
      :message="targetUser ? `封禁 ${targetUser.username} 后，该账号将受到严格限制，无法继续访问 TrailQuest 功能。` : '确认执行封禁操作？'"
      confirm-text="确认封禁"
      :loading="actionLoading"
      require-reason
      reason-label="封禁具体依据"
      reason-placeholder="请简述违反条款或封禁理由"
      @confirm="handleBan"
    />

    <AdminConfirmDialog
      v-model:show="unbanDialogVisible"
      title="确认解封用户"
      :message="targetUser ? `解封 ${targetUser.username} 后，限制将被解除。该用户功能恢复。` : '确认解除其违规惩罚？'"
      confirm-text="确认解封"
      :loading="actionLoading"
      @confirm="handleUnban"
    />

    <AdminNoticeDialog
      v-model:show="successDialog.show"
      :title="successDialog.title"
      :message="successDialog.message"
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
