<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

const activities = [
  { name: '徒步', icon: 'Footprints' },
  { name: '跑步', icon: 'Activity' },
  { name: '骑行', icon: 'Bike' },
  { name: '宠物友好', icon: 'PawPrint' },
  { name: '背包客', icon: 'Backpack' },
  { name: '轮椅友好', icon: 'Accessibility' },
]
</script>

<template>
  <section class="py-10 sm:py-16" style="background-color: var(--bg-card);">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-10">
      <h2 class="text-xl sm:text-2xl font-bold mb-6 sm:mb-8" style="color: var(--text-primary);">按活动探索</h2>
      <div class="grid grid-cols-3 sm:grid-cols-6 gap-3 sm:gap-4">
        <button
          v-for="(activity, i) in activities"
          :key="activity.name"
          class="card card-hover flex flex-col items-center justify-center gap-2 py-5 sm:py-6 px-3 animate-fade-in-up"
          :class="`stagger-${i + 1}`"
        >
          <div class="text-primary-500">
            <BaseIcon :name="activity.icon" :size="32" />
          </div>
          <span class="text-xs sm:text-sm font-medium" style="color: var(--text-primary);">{{ activity.name }}</span>
        </button>
      </div>
    </div>
  </section>
</template>
