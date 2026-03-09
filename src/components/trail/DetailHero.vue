<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  image: string
  name: string
  location: string
  difficulty: string
  difficultyLabel: string
  distance: string
  elevation: string
  duration: string
}>()
</script>

<template>
  <div class="card overflow-hidden animate-fade-in-up">
    <div class="relative aspect-[16/9] overflow-hidden">
      <img :src="image" :alt="name" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"></div>
      <div class="absolute bottom-4 left-4 right-4">
        <span
          class="px-2.5 py-1 rounded-full text-xs font-semibold"
          :class="{
            'badge-easy': difficulty === 'easy',
            'badge-moderate': difficulty === 'moderate',
            'badge-hard': difficulty === 'hard'
          }"
        >
          {{ difficultyLabel }}
        </span>
        <h1 class="text-xl sm:text-2xl font-bold text-white mt-2">{{ name }}</h1>
        <p class="text-sm text-white/80 flex items-center gap-1 mt-1">
          <BaseIcon name="MapPin" :size="14" />
          {{ location }}
        </p>
      </div>
    </div>
    <!-- Stats Bar -->
    <div class="grid grid-cols-3 divide-x p-4" style="border-color: var(--border-default);">
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ distance }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">距离</p>
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ elevation }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">海拔增益</p>
      </div>
      <div class="text-center">
        <p class="text-sm font-bold text-primary-500">{{ duration }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">预计时间</p>
      </div>
    </div>
  </div>
</template>
