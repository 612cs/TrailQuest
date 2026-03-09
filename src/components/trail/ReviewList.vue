<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import SectionHeader from '../common/SectionHeader.vue'

const reviews = [
  { id: 1, user: 'Sarah M.', avatar: 'SM', avatarBg: '#8b5cf6', rating: 5, time: '1 周前', text: '壮观的景色！山顶的日出令人叹为观止。路径标记清晰,但最后一段比较陡峭。建议穿登山鞋。' },
  { id: 2, user: 'Mike R.', avatar: 'MR', avatarBg: '#ef4444', rating: 4, time: '2 周前', text: '很棒的路线！沿途有很多拍照点。只是周末人会比较多,建议工作日前往。停车位充足。' },
]
</script>

<template>
  <section class="animate-fade-in-up stagger-4">
    <SectionHeader title="用户评价">
      <template #action>
        <div class="flex items-center gap-1">
          <BaseIcon name="Star" :size="16" class="text-primary-500 fill-current" />
          <span class="text-sm font-bold text-primary-500">4.8</span>
          <span class="text-xs" style="color: var(--text-tertiary);">(124 条)</span>
        </div>
      </template>
    </SectionHeader>
    <div class="space-y-3">
      <div v-for="review in reviews" :key="review.id" class="card p-4">
        <div class="flex items-start gap-3">
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0"
            :style="`background-color: ${review.avatarBg}`"
          >
            {{ review.avatar }}
          </div>
          <div class="flex-1">
            <div class="flex items-center justify-between">
              <span class="text-sm font-semibold" style="color: var(--text-primary);">{{ review.user }}</span>
              <span class="text-xs" style="color: var(--text-tertiary);">{{ review.time }}</span>
            </div>
            <div class="flex gap-0.5 my-1">
              <BaseIcon
                v-for="i in 5"
                :key="i"
                name="Star"
                :size="14"
                :class="i <= review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
              />
            </div>
            <p class="text-sm leading-relaxed" style="color: var(--text-secondary);">{{ review.text }}</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
