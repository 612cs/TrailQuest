<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
</script>

<template>
  <section class="animate-fade-in-up stagger-3 mt-4">
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Sea of clouds -->
      <div class="p-4 bg-white/5 rounded-2xl border border-white/5 flex flex-col items-center gap-2 hover:bg-white/10 transition-colors group">
        <div class="p-2 rounded-xl bg-primary-500/10 text-primary-500 group-hover:scale-110 transition-transform">
          <BaseIcon name="Cloud" :size="20" />
        </div>
        <span class="text-xl font-bold" style="color: var(--text-primary);">78%</span>
        <span class="text-xs font-medium" style="color: var(--text-tertiary);">云海</span>
      </div>

      <!-- Sunrise -->
      <div class="p-4 bg-white/5 rounded-2xl border border-white/5 flex flex-col items-center gap-2 hover:bg-white/10 transition-colors group">
        <div class="p-2 rounded-xl bg-yellow-500/10 text-yellow-500 group-hover:scale-110 transition-transform">
          <BaseIcon name="Sunrise" :size="20" />
        </div>
        <span class="text-xl font-bold" style="color: var(--text-primary);">92%</span>
        <span class="text-xs font-medium" style="color: var(--text-tertiary);">日出</span>
      </div>

      <!-- Sunset -->
      <div class="p-4 bg-white/5 rounded-2xl border border-white/5 flex flex-col items-center gap-2 hover:bg-white/10 transition-colors group">
        <div class="p-2 rounded-xl bg-orange-500/10 text-orange-500 group-hover:scale-110 transition-transform">
          <BaseIcon name="Sunset" :size="20" />
        </div>
        <span class="text-xl font-bold" style="color: var(--text-primary);">85%</span>
        <span class="text-xs font-medium" style="color: var(--text-tertiary);">日落</span>
      </div>

      <!-- Rime (雾凇) -->
      <div class="p-4 bg-white/5 rounded-2xl border border-white/5 flex flex-col items-center gap-2 hover:bg-white/10 transition-colors group">
        <div class="p-2 rounded-xl bg-blue-400/10 text-blue-400 group-hover:scale-110 transition-transform">
          <BaseIcon name="CloudSnow" :size="20" />
        </div>
        <span class="text-xl font-bold" style="color: var(--text-primary);">15%</span>
        <span class="text-xs font-medium" style="color: var(--text-tertiary);">雾凇</span>
      </div>
    </div>
  </section>
</template>
