<script setup lang="ts">
import { EyeOff, Info, RotateCcw, Trash2 } from 'lucide-vue-next'
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
          <th>评论内容</th>
          <th>关联路线</th>
          <th>作者</th>
          <th>状态</th>
          <th>发布时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="item in props.items"
          :key="item.id"
          class="review-table__row"
        >
          <td class="review-table__checkbox-cell" @click.stop>
            <input
              type="checkbox"
              :checked="isSelected(item.id)"
              @change="emit('toggle-select', { id: String(item.id), checked: ($event.target as HTMLInputElement).checked })"
            />
          </td>
          <td class="review-table__content-cell" @click="emit('detail', item)">
            <div class="review-table__text-summary">
              <strong>{{ item.text }}</strong>
              <small v-if="item.rating" class="rating-badge">评分 {{ item.rating }}</small>
            </div>
            <small class="review-id">ID: #{{ item.id }}</small>
          </td>
          <td @click="emit('detail', item)">
            <div class="trail-info">
              <span>{{ item.trailName }}</span>
            </div>
          </td>
          <td @click="emit('detail', item)">
            <div class="review-table__author">
              <div class="review-table__avatar-mini" :style="item.avatarMediaUrl ? '' : `background:${item.avatarBg || 'var(--bg-soft)'}`">
                <img v-if="item.avatarMediaUrl" :src="item.avatarMediaUrl" :alt="item.authorUsername" />
                <span v-else>{{ item.avatar || item.authorUsername.slice(0, 1).toUpperCase() }}</span>
              </div>
              <span>{{ item.authorUsername }}</span>
            </div>
          </td>
          <td @click="emit('detail', item)"><StatusBadge :status="item.status" /></td>
          <td @click="emit('detail', item)">
            <div class="time-cell">
              {{ formatDateTime(item.createdAt) }}
            </div>
          </td>
          <td class="review-table__actions" @click.stop>
            <button class="action-icon info" type="button" title="详情" @click="emit('detail', item)">
              <Info :size="18" />
            </button>
            
            <button
              v-if="item.status === 'active'"
              class="action-icon hide"
              type="button"
              title="隐藏"
              @click="emit('hide', item)"
            >
              <EyeOff :size="18" />
            </button>
            <button
              v-else
              class="action-icon restore"
              type="button"
              title="恢复"
              @click="emit('restore', item)"
            >
              <RotateCcw :size="18" />
            </button>
            
            <button 
              class="action-icon delete" 
              type="button" 
              title="删除" 
              @click="emit('delete', item)"
            >
              <Trash2 :size="18" />
            </button>
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

.review-table__content-cell {
  max-width: 20rem;
}

.review-table__text-summary {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.review-id {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-style: italic;
  margin-top: 0.25rem;
  display: block;
}

.trail-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}


.review-table__author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.review-table__avatar-mini {
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  overflow: hidden;
  display: grid;
  place-items: center;
  font-size: 0.65rem;
  font-weight: 700;
  color: white;
  background: var(--primary);
}

.review-table__avatar-mini img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rating-badge {
  background: var(--bg-soft);
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.time-cell {
  font-size: 0.875rem;
  color: var(--text-muted);
  white-space: nowrap;
}

.review-table__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-icon {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon.info { color: #6e8e6b; }
.action-icon.hide { color: #8a8a8a; }
.action-icon.restore { color: #5a9e70; }
.action-icon.delete { color: #d67a7a; }

.action-icon:hover {
  background: var(--bg-soft);
  transform: scale(1.1);
}

.action-icon.delete:hover {
  background: rgba(214, 122, 122, 0.1);
}
</style>
