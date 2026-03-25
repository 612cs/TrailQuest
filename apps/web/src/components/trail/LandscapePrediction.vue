<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

const predictions = [
  { name: '云海', probability: '78%', icon: 'Cloud', color: 'text-primary-500', bgColor: 'bg-primary-500/10' },
  { name: '日出', probability: '92%', icon: 'Sunrise', color: 'text-yellow-500', bgColor: 'bg-yellow-500/10' },
  { name: '日落', probability: '85%', icon: 'Sunset', color: 'text-orange-500', bgColor: 'bg-orange-500/10' },
  { name: '雾凇', probability: '15%', icon: 'CloudSnow', color: 'text-blue-400', bgColor: 'bg-blue-400/10' }
]
</script>

<template>
  <section class="animate-fade-in-up stagger-3 mt-4">
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div 
        v-for="item in predictions" 
        :key="item.name"
        class="p-4 bg-white/5 rounded-2xl border border-primary-500/10 flex flex-col items-center gap-2 hover:bg-white/10 transition-colors group"
      >
        <div :class="['p-2 rounded-xl group-hover:scale-110 transition-transform', item.bgColor, item.color]">
          <BaseIcon :name="item.icon" :size="20" />
        </div>
        <span class="text-xl font-bold" style="color: var(--text-primary);">{{ item.probability }}</span>
        <span class="text-xs font-medium" style="color: var(--text-tertiary);">{{ item.name }}</span>
      </div>
    </div>
  </section>
</template>
