<script setup lang="ts">
import SectionHeader from '../common/SectionHeader.vue'
</script>

<template>
  <section class="animate-fade-in-up stagger-3">
    <SectionHeader title="景观预测" />
    <div class="grid grid-cols-2 gap-3">
      <div class="card p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium" style="color: var(--text-primary);">云海概率</span>
          <span class="text-sm font-bold text-primary-500">78%</span>
        </div>
        <div class="w-full h-2 rounded-full overflow-hidden" style="background-color: var(--bg-tag);">
          <div class="h-full rounded-full bg-primary-500 transition-all duration-1000" style="width: 78%"></div>
        </div>
        <p class="text-xs mt-2" style="color: var(--text-tertiary);">最佳时间：06:00 - 08:00</p>
      </div>
      <div class="card p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium" style="color: var(--text-primary);">日出可见度</span>
          <span class="text-sm font-bold text-primary-500">92%</span>
        </div>
        <div class="w-full h-2 rounded-full overflow-hidden" style="background-color: var(--bg-tag);">
          <div class="h-full rounded-full bg-primary-500 transition-all duration-1000" style="width: 92%"></div>
        </div>
        <p class="text-xs mt-2" style="color: var(--text-tertiary);">日出时间：06:15</p>
      </div>
    </div>
  </section>
</template>
