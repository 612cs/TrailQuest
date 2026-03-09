<script setup lang="ts">
import { RouterLink } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'
</script>

<template>
  <section class="relative min-h-[70vh] sm:min-h-[80vh] flex items-end overflow-hidden">
    <!-- Background Image -->
    <div class="absolute inset-0">
      <img src="/hero-mountain.png" alt="壮丽山景" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/30 to-transparent"></div>
    </div>

    <!-- Content Overlay -->
    <div class="relative z-10 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-10 pb-12 sm:pb-20">
      <div class="max-w-2xl space-y-5 sm:space-y-6">
        <h1 class="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white leading-tight tracking-tight animate-fade-in-up" style="text-shadow: 0 2px 16px rgba(0,0,0,0.3);">
          冒险在召唤。
        </h1>
        <p class="text-base sm:text-lg text-white/80 max-w-lg animate-fade-in-up stagger-1">
          通过经过验证的地图和社区评论，发现成千上万条步道。
        </p>

        <!-- Search Bar -->
        <div class="flex items-stretch gap-2 sm:gap-3 animate-fade-in-up stagger-2">
          <div class="flex-1 flex items-center gap-3 px-4 py-3 rounded-xl border border-white/20 bg-white/10 backdrop-blur-md">
            <BaseIcon name="Search" class="text-white/60 shrink-0" :size="20" />
            <input
              type="text"
              placeholder="按城市、公园或步道搜索"
              class="w-full bg-transparent text-white placeholder-white/50 text-sm focus:outline-none"
            />
          </div>
          <RouterLink
            to="/search"
            class="flex items-center gap-2 px-5 sm:px-6 py-3 rounded-xl bg-primary-500 hover:bg-primary-600 text-white font-semibold text-sm transition-all duration-200 shrink-0 hover:shadow-lg"
          >
            <BaseIcon name="Compass" :size="16" />
            寻找步道
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>
