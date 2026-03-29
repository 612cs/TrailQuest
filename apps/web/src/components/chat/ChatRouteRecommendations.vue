<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

import BaseIcon from '../common/BaseIcon.vue'
import TagBadge from '../common/TagBadge.vue'
import type { AiTrailCard } from '../../types/ai'

const route = useRoute()

const props = defineProps<{
  items: AiTrailCard[]
}>()

const packLabels: Record<'light' | 'heavy' | 'both', string> = {
  light: '轻装',
  heavy: '重装',
  both: '轻重皆可',
}

const durationLabels: Record<'single_day' | 'multi_day', string> = {
  single_day: '单日',
  multi_day: '多日',
}

function detailLink(id: string | number) {
  return {
    name: 'TrailDetail',
    params: { id },
    query: { from: route.fullPath },
  }
}
</script>

<template>
  <div v-if="props.items.length > 0" class="space-y-3 pt-2">
    <RouterLink
      v-for="item in props.items"
      :key="item.id"
      :to="detailLink(item.id)"
      class="group block rounded-3xl border p-3 transition-all duration-200 hover:-translate-y-0.5"
      style="border-color: var(--border-card); background-color: var(--bg-page);"
    >
      <div class="flex gap-3">
        <img :src="item.image" :alt="item.name" class="h-24 w-28 rounded-2xl object-cover sm:h-28 sm:w-36" />
        <div class="min-w-0 flex-1 space-y-2">
          <div class="flex flex-wrap items-center gap-2">
            <TagBadge :label="item.difficultyLabel" />
            <TagBadge :label="packLabels[item.packType]" />
            <TagBadge :label="durationLabels[item.durationType]" />
          </div>
          <div class="space-y-1">
            <h4 class="line-clamp-1 text-sm font-semibold sm:text-base" style="color: var(--text-primary);">
              {{ item.name }}
            </h4>
            <p class="line-clamp-1 text-xs sm:text-sm" style="color: var(--text-secondary);">
              {{ item.location }}
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-3 text-xs sm:text-sm" style="color: var(--text-secondary);">
            <span class="flex items-center gap-1"><BaseIcon name="TrendingUp" :size="14" />{{ item.distance }}</span>
            <span class="flex items-center gap-1"><BaseIcon name="Mountain" :size="14" />{{ item.elevation }}</span>
            <span class="flex items-center gap-1"><BaseIcon name="Clock" :size="14" />{{ item.duration }}</span>
          </div>
        </div>
      </div>
      <div class="mt-3 flex items-start justify-between gap-3 border-t pt-3" style="border-color: var(--border-default);">
        <p class="text-xs leading-6 sm:text-sm" style="color: var(--text-secondary);">
          {{ item.reason }}
        </p>
        <span class="flex shrink-0 items-center gap-1 text-xs font-medium sm:text-sm text-primary-500">
          查看详情
          <BaseIcon name="ChevronRight" :size="16" />
        </span>
      </div>
    </RouterLink>
  </div>
</template>
