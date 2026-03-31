<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
}>()

const meta = computed(() => {
  switch (props.status) {
    case 'pending':
      return { label: '待审核', className: 'admin-badge admin-badge-pending' }
    case 'approved':
      return { label: '已通过', className: 'admin-badge admin-badge-approved' }
    case 'rejected':
      return { label: '已驳回', className: 'admin-badge admin-badge-rejected' }
    case 'resolved':
      return { label: '已处理', className: 'admin-badge admin-badge-approved' }
    case 'hidden':
      return { label: '已隐藏', className: 'admin-badge admin-badge-neutral' }
    case 'disabled':
      return { label: '已禁用', className: 'admin-badge admin-badge-rejected' }
    case 'active':
      return { label: '正常', className: 'admin-badge admin-badge-approved' }
    case 'banned':
      return { label: '已封禁', className: 'admin-badge admin-badge-rejected' }
    case 'deleted':
      return { label: '已下架', className: 'admin-badge admin-badge-neutral' }
    default:
      return { label: props.status || '未知', className: 'admin-badge admin-badge-neutral' }
  }
})
</script>

<template>
  <span :class="meta.className">{{ meta.label }}</span>
</template>
