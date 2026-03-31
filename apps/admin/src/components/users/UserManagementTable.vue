<script setup lang="ts">
import { ShieldCheck } from 'lucide-vue-next'

import StatusBadge from '../common/StatusBadge.vue'
import { formatDateTime } from '../../utils/format'
import type { AdminUserListItem } from '../../types/admin'

const props = defineProps<{
  items: AdminUserListItem[]
  actionLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'ban', user: AdminUserListItem): void
  (e: 'unban', user: AdminUserListItem): void
}>()
</script>

<template>
  <div class="admin-table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th>用户</th>
          <th>邮箱</th>
          <th>角色</th>
          <th>账号状态</th>
          <th>封禁时间</th>
          <th>已发布路线</th>
          <th>注册时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in props.items" :key="item.id">
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
          <td><StatusBadge :status="item.status" /></td>
          <td>{{ item.bannedAt ? formatDateTime(item.bannedAt) : '--' }}</td>
          <td>{{ item.publishedTrailCount }}</td>
          <td>{{ formatDateTime(item.createdAt) }}</td>
          <td>
            <button
              v-if="item.status === 'banned'"
              class="admin-button admin-button-secondary"
              type="button"
              :disabled="props.actionLoading"
              @click="emit('unban', item)"
            >
              解封
            </button>
            <button
              v-else
              class="admin-button admin-button-danger"
              type="button"
              :disabled="props.actionLoading || item.role === 'ADMIN'"
              @click="emit('ban', item)"
            >
              封禁
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.admin-table-wrap {
  overflow-x: auto;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
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
</style>
