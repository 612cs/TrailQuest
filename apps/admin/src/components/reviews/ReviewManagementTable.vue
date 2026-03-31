<script setup lang="ts">
import StatusBadge from '../common/StatusBadge.vue'
import { formatDateTime } from '../../utils/format'
import type { AdminReviewListItem } from '../../types/admin'

const props = defineProps<{
  items: AdminReviewListItem[]
  selectedIds: string[]
  allCurrentSelected: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-select-all', checked: boolean): void
  (e: 'toggle-select', payload: { id: string; checked: boolean }): void
  (e: 'detail', review: AdminReviewListItem): void
  (e: 'hide', review: AdminReviewListItem): void
  (e: 'restore', review: AdminReviewListItem): void
  (e: 'delete', review: AdminReviewListItem): void
}>()

function isSelected(id: string | number) {
  return props.selectedIds.includes(String(id))
}
</script>

<template>
  <div class="admin-table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th class="review-table__checkbox-cell">
            <input
              type="checkbox"
              :checked="allCurrentSelected"
              @change="emit('toggle-select-all', ($event.target as HTMLInputElement).checked)"
            />
          </th>
          <th>评论</th>
          <th>作者</th>
          <th>路线</th>
          <th>父评论</th>
          <th>状态</th>
          <th>治理备注</th>
          <th>时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="item in props.items"
          :key="item.id"
          class="review-table__row"
          @click="emit('detail', item)"
        >
          <td class="review-table__checkbox-cell" @click.stop>
            <input
              type="checkbox"
              :checked="isSelected(item.id)"
              @change="emit('toggle-select', { id: String(item.id), checked: ($event.target as HTMLInputElement).checked })"
            />
          </td>
          <td class="review-table__content-cell">
            <strong>{{ item.text }}</strong>
            <small v-if="item.rating">评分 {{ item.rating }} / 5</small>
          </td>
          <td>
            <div class="review-table__author">
              <div class="review-table__avatar" :style="item.avatarMediaUrl ? '' : `background:${item.avatarBg || 'var(--bg-soft)'}`">
                <img v-if="item.avatarMediaUrl" :src="item.avatarMediaUrl" :alt="item.authorUsername" />
                <span v-else>{{ item.avatar || item.authorUsername.slice(0, 2).toUpperCase() }}</span>
              </div>
              <div>
                <strong>{{ item.authorUsername }}</strong>
                <small>ID {{ item.userId }}</small>
              </div>
            </div>
          </td>
          <td>{{ item.trailName }}</td>
          <td class="review-table__parent-cell">{{ item.parentText || '--' }}</td>
          <td><StatusBadge :status="item.status" /></td>
          <td class="review-table__reason-cell">{{ item.moderationReason || '--' }}</td>
          <td>
            <strong>{{ formatDateTime(item.createdAt) }}</strong>
            <small v-if="item.moderatedAt">治理于 {{ formatDateTime(item.moderatedAt) }}</small>
          </td>
          <td class="review-table__actions" @click.stop>
            <button class="admin-button admin-button-secondary" type="button" @click="emit('detail', item)">详情</button>
            <button
              v-if="item.status === 'active'"
              class="admin-button admin-button-secondary"
              type="button"
              @click="emit('hide', item)"
            >
              隐藏
            </button>
            <button
              v-else
              class="admin-button admin-button-primary"
              type="button"
              @click="emit('restore', item)"
            >
              恢复
            </button>
            <button class="admin-button admin-button-danger" type="button" @click="emit('delete', item)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.admin-table-wrap {
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: auto;
}

.review-table__row {
  cursor: pointer;
}

.review-table__row:hover {
  background: var(--bg-soft);
}

.review-table__checkbox-cell {
  width: 3rem;
}

.review-table__content-cell,
.review-table__author,
.review-table__actions {
  display: flex;
  gap: 0.75rem;
}

.review-table__content-cell,
.review-table__author {
  flex-direction: column;
}

.review-table__content-cell strong,
.review-table__author strong {
  color: var(--text-strong);
}

.review-table__content-cell small,
.review-table__author small,
.review-table__actions small,
.review-table__reason-cell,
.review-table__parent-cell,
.review-table td small {
  color: var(--text-muted);
}

.review-table__author {
  flex-direction: row;
  align-items: center;
}

.review-table__avatar {
  width: 2.5rem;
  height: 2.5rem;
  overflow: hidden;
  border-radius: 999px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border);
  color: #fff;
  font-weight: 700;
}

.review-table__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-table__actions {
  flex-wrap: wrap;
  align-items: center;
}

.review-table__reason-cell,
.review-table__parent-cell {
  max-width: 18rem;
  line-height: 1.65;
}
</style>
