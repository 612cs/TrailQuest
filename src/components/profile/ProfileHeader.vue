<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import ActionButton from '../common/ActionButton.vue'
</script>

<template>
  <div class="card p-5 sm:p-8">
    <div class="flex flex-col sm:flex-row items-center sm:items-start gap-5 sm:gap-8">
      <div class="relative shrink-0">
        <div class="w-20 h-20 sm:w-24 sm:h-24 rounded-full border-4 flex items-center justify-center text-2xl font-bold text-white shadow-inner"
          style="background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700)); border-color: var(--border-default);">AT</div>
        <button class="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-primary-500 text-white flex items-center justify-center shadow-lg border-2" style="border-color: var(--bg-card);">
          <BaseIcon name="Pencil" :size="14" />
        </button>
      </div>
      <div class="flex-1 text-center sm:text-left space-y-3">
        <div class="flex flex-col sm:flex-row items-center sm:items-start gap-2">
          <h1 class="text-xl sm:text-2xl font-bold" style="color: var(--text-primary);">亚历克斯·汤普森</h1>
        </div>
        <p class="text-sm" style="color: var(--text-secondary);">管理员权限 • 2023年1月加入</p>
        <div class="flex items-center justify-center sm:justify-start gap-2">
          <ActionButton variant="primary">
            <BaseIcon name="Settings2" :size="16" />
            编辑资料
          </ActionButton>
          <ActionButton variant="secondary">
            <BaseIcon name="Shield" :size="16" />
            账户设置
          </ActionButton>
        </div>
      </div>
      <div class="flex sm:flex-col gap-4 sm:gap-3">
        <div class="text-center px-6 py-3 rounded-xl border group hover:border-primary-500/50 transition-colors" style="border-color: var(--border-default); background-color: var(--bg-page);">
          <p class="text-xl font-bold text-primary-500">24</p>
          <p class="text-[10px] uppercase tracking-wider font-semibold mt-0.5" style="color: var(--text-tertiary);">我的发布</p>
        </div>
        <div class="text-center px-6 py-3 rounded-xl border group hover:border-primary-500/50 transition-colors" style="border-color: var(--border-default); background-color: var(--bg-page);">
          <p class="text-xl font-bold text-primary-500">158</p>
          <p class="text-[10px] uppercase tracking-wider font-semibold mt-0.5" style="color: var(--text-tertiary);">已保存</p>
        </div>
      </div>
    </div>
  </div>
</template>
