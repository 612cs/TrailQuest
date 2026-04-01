<script setup lang="ts">
import { Trash2, EyeOff, RotateCcw, XCircle } from 'lucide-vue-next'

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
  <div class="review-batch-bar">
    <div class="review-batch-bar__summary">
      <div class="selection-indicator">
        <strong>已选 {{ selectedCount }} 条</strong>
        <span class="selection-details">
          正常 {{ activeCount }} / 可恢复 {{ restorableCount }}
        </span>
      </div>
    </div>

    <div class="review-batch-bar__actions">
      <button
        class="batch-btn hide"
        type="button"
        :disabled="activeCount === 0"
        title="批量隐藏"
        @click="emit('batch-hide')"
      >
        <EyeOff :size="16" />
        <span>批量隐藏</span>
      </button>
      <button
        class="batch-btn restore"
        type="button"
        :disabled="restorableCount === 0"
        title="批量恢复"
        @click="emit('batch-restore')"
      >
        <RotateCcw :size="16" />
        <span>批量恢复</span>
      </button>
      <button 
        class="batch-btn clear" 
        type="button" 
        title="清空选择"
        @click="emit('clear')"
      >
        <XCircle :size="16" />
        <span>清空</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.review-batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  background: var(--bg-soft);
  border: 1px solid var(--primary-soft);
  border-radius: 12px;
  padding: 0.4rem 1rem;
  height: 44px;
}

.review-batch-bar__summary {
  display: flex;
  align-items: center;
}

.selection-indicator {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.selection-indicator strong {
  font-size: 0.875rem;
  color: var(--primary);
}

.selection-details {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.review-batch-bar__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.85rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  background: white;
  color: var(--text-strong);
}

.batch-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(1);
}

.batch-btn.hide:hover:not(:disabled) {
  border-color: #8a8a8a;
  color: #8a8a8a;
}

.batch-btn.restore:hover:not(:disabled) {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.batch-btn.clear {
  background: transparent;
  color: var(--text-muted);
}

.batch-btn.clear:hover {
  color: var(--danger);
  background: rgba(var(--danger-rgb), 0.05);
}

@media (max-width: 640px) {
  .selection-details {
    display: none;
  }
  .batch-btn span {
    display: none;
  }
  .batch-btn {
    padding: 0.4rem;
  }
}
</style>
