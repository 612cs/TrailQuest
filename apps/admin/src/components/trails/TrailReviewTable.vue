<script setup lang="ts">
import StatusBadge from '../common/StatusBadge.vue'
import { formatDateTime } from '../../utils/format'
import type { AdminTrailListItem } from '../../types/admin'

const props = defineProps<{
  items: AdminTrailListItem[]
}>()

const emit = defineEmits<{
  (e: 'open', id: string): void
}>()

function openDetail(id: string | number) {
  emit('open', String(id))
}
</script>

<template>
  <div class="admin-table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th>路线</th>
          <th>作者</th>
          <th>位置</th>
          <th>状态</th>
          <th>发布时间</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="item in props.items"
          :key="item.id"
          class="admin-table-row"
          @click="openDetail(item.id)"
        >
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

.admin-table-row {
  cursor: pointer;
  transition: background-color 0.18s ease;
}

.admin-table-row:hover {
  background: var(--bg-soft);
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
</style>
