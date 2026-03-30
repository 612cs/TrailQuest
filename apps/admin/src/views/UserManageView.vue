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
  <section class="admin-card admin-section">
    <div class="admin-list-toolbar">
      <input v-model="keyword" class="admin-input" placeholder="用户名、邮箱或所在地" aria-label="关键词" @keyup.enter="load(1)" />
      <select v-model="role" class="admin-select" aria-label="角色" @change="load(1)">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
      </select>
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

      <UserManagementTable
        v-if="list.length"
        :items="list"
        :action-loading="actionLoading"
        @ban="openBanDialog"
        @unban="openUnbanDialog"
      />

      <EmptyState
        v-else-if="!loading"
        title="暂无用户数据"
        description="当前筛选条件下没有匹配的用户。"
        :icon="UsersRound"
      />

      <AdminPagination
        :current="pageNum"
        :total-pages="totalPages"
        :total-items="total"
        item-label="位用户"
        @update:current="load"
      />
    </div>

    <AdminConfirmDialog
      v-model:show="banDialogVisible"
      title="确认封禁用户"
      :message="targetUser ? `封禁 ${targetUser.username} 后，该账号将无法登录、发布和互动。` : '封禁后该账号将无法登录。'"
      confirm-text="确认封禁"
      :loading="actionLoading"
      require-reason
      reason-label="封禁原因"
      reason-placeholder="请输入封禁原因"
      @confirm="handleBan"
    />

    <AdminConfirmDialog
      v-model:show="unbanDialogVisible"
      title="确认解封用户"
      :message="targetUser ? `解封 ${targetUser.username} 后，该账号将恢复正常使用。` : '确认恢复该账号？'"
      confirm-text="确认解封"
      :loading="actionLoading"
      @confirm="handleUnban"
    />

    <AdminNoticeDialog
      v-model:show="successDialog.show"
      :title="successDialog.title"
      :message="successDialog.message"
    />
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
