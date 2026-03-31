<script setup lang="ts">
defineProps<{
  selectedCount: number
  activeCount: number
  restorableCount: number
}>()

const emit = defineEmits<{
  (e: 'batch-hide'): void
  (e: 'batch-restore'): void
  (e: 'clear'): void
}>()
</script>

<template>
  <div class="review-batch-bar admin-panel">
    <div class="review-batch-bar__summary">
      <strong>已选 {{ selectedCount }} 条</strong>
      <span class="admin-muted">正常 {{ activeCount }} 条，可恢复 {{ restorableCount }} 条</span>
    </div>

    <div class="review-batch-bar__actions">
      <button
        class="admin-button admin-button-secondary"
        type="button"
        :disabled="activeCount === 0"
        @click="emit('batch-hide')"
      >
        批量隐藏
      </button>
      <button
        class="admin-button admin-button-primary"
        type="button"
        :disabled="restorableCount === 0"
        @click="emit('batch-restore')"
      >
        批量恢复
      </button>
      <button class="admin-button admin-button-secondary" type="button" @click="emit('clear')">
        清空选择
      </button>
    </div>
  </div>
</template>

<style scoped>
.review-batch-bar,
.review-batch-bar__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.review-batch-bar {
  margin-top: 1rem;
  padding: 1rem 1.1rem;
}

.review-batch-bar__summary {
  display: grid;
  gap: 0.2rem;
}

.review-batch-bar__summary strong {
  color: var(--text-strong);
}

.review-batch-bar__actions {
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .review-batch-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .review-batch-bar__actions {
    justify-content: flex-start;
  }
}
</style>
