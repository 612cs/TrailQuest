<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import BaseIcon from '../common/BaseIcon.vue'
import { useOptionConfigStore } from '../../stores/useOptionConfigStore'

const DEFAULT_ACTIVITIES = [
  { code: 'hiking', label: '徒步', icon: 'Footprints', extra: { query: '徒步' }, sort: 1, enabled: true },
  { code: 'running', label: '跑步', icon: 'Activity', extra: { query: '跑步' }, sort: 2, enabled: true },
  { code: 'cycling', label: '骑行', icon: 'Bike', extra: { query: '骑行' }, sort: 3, enabled: true },
  { code: 'pet_friendly', label: '宠物友好', icon: 'PawPrint', extra: { query: '宠物友好' }, sort: 4, enabled: true },
  { code: 'backpacking', label: '背包客', icon: 'Backpack', extra: { query: '背包客' }, sort: 5, enabled: true },
  { code: 'wheelchair_friendly', label: '轮椅友好', icon: 'Accessibility', extra: { query: '轮椅友好' }, sort: 6, enabled: true },
] as const

const router = useRouter()
const optionConfigStore = useOptionConfigStore()

const activities = computed(() => optionConfigStore.getGroup('home_activity', [...DEFAULT_ACTIVITIES])
  .filter((item) => item.enabled !== false)
  .map((item) => ({
    code: item.code,
    name: item.label,
    icon: item.icon || 'Footprints',
    query: typeof item.extra?.query === 'string' ? item.extra.query : item.label,
  })))

function handleActivityClick(query: string) {
  void router.push({ path: '/search', query: { q: query } })
}

onMounted(() => {
  void optionConfigStore.ensureGroups(['home_activity'])
})
</script>

<template>
  <section class="py-10 sm:py-16" style="background-color: var(--bg-card);">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-10">
      <h2 class="text-xl sm:text-2xl font-bold mb-6 sm:mb-8" style="color: var(--text-primary);">按活动探索</h2>
      <div class="grid grid-cols-3 sm:grid-cols-6 gap-3 sm:gap-4">
        <button
          v-for="(activity, i) in activities"
          :key="activity.code"
          class="card card-hover flex flex-col items-center justify-center gap-2 py-5 sm:py-6 px-3 animate-fade-in-up"
          :class="`stagger-${i + 1}`"
          @click="handleActivityClick(activity.query)"
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
