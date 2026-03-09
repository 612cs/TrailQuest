<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import SectionHeader from '../common/SectionHeader.vue'

const weatherData = [
  { label: '温度', value: '22°C', desc: '体感温度 19°C', icon: 'ThermometerSun' },
  { label: '湿度', value: '65%', desc: '舒适区间 / 无雨', icon: 'Droplets' },
  { label: '风速', value: '12 km/h', desc: '微风 / 适宜徒步', icon: 'Wind' },
]
</script>

<template>
  <section class="animate-fade-in-up stagger-1">
    <SectionHeader title="当前天气状况" />
    <div class="grid grid-cols-3 gap-3">
      <div v-for="w in weatherData" :key="w.label" class="card p-4 text-center group hover:border-primary-500/30 transition-colors">
        <div class="flex justify-center text-primary-500 group-hover:scale-110 transition-transform">
          <BaseIcon :name="w.icon" :size="28" />
        </div>
        <p class="text-lg font-bold mt-2" style="color: var(--text-primary);">{{ w.value }}</p>
        <p class="text-xs mt-0.5" style="color: var(--text-tertiary);">{{ w.label }}</p>
        <p class="text-xs mt-1" style="color: var(--text-secondary);">{{ w.desc }}</p>
      </div>
    </div>
  </section>
</template>
