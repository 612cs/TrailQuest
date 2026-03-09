<script setup lang="ts">
import { ref } from 'vue'
import SearchBar from '../components/search/SearchBar.vue'
import FilterBar from '../components/search/FilterBar.vue'
import ViewToggle from '../components/search/ViewToggle.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import TrailHorizontalCard from '../components/trail/TrailHorizontalCard.vue'

const searchQuery = ref('广西 桂林')
const activeView = ref<'list' | 'map'>('list')

const filters = ['难度', '路线长度', '海拔增益', '评分']

const trails = [
  {
    id: 1,
    image: '/trail-pine.png',
    name: '龙脊梯田：平安至大寨精华线',
    difficulty: 'moderate',
    difficultyLabel: '适中',
    distance: '12.5 km',
    duration: '4.5 小时',
    elevation: '+450 m',
    description: '穿梭于世界著名的龙脊梯田之间，体验壮族与瑶族的农耕文化。沿途设有多个观景台，包括"七星伴月"和"九龙五虎"，风光旖旎。',
    rating: 4.8,
    reviews: '(124 条评论)',
  },
  {
    id: 2,
    image: '/trail-foggy.png',
    name: '阳朔老寨山：全景俯瞰徒步',
    difficulty: 'hard',
    difficultyLabel: '困难',
    distance: '3.2 km',
    duration: '2 小时',
    elevation: '+300 m',
    description: '虽然距离较短，但坡度极陡，部分路段需手脚并用。顶峰可 360 度俯瞰漓江第一湾，是拍摄日落的绝佳地点。',
    rating: 4.9,
    reviews: '(256 条评论)',
  },
  {
    id: 3,
    image: '/trail-lake.png',
    name: '遇龙河滨水步道：悠闲漫步',
    difficulty: 'easy',
    difficultyLabel: '容易',
    distance: '8.0 km',
    duration: '2.5 小时',
    elevation: '+20 m',
    description: '平坦的滨水步道，适合亲子家庭。沿途可以欣赏到遇龙河宁静的水面、竹筏漂流以及点缀在山间的古老村落。',
    rating: 4.5,
    reviews: '(89 条评论)',
  },
]
</script>

<template>
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-10 py-6 sm:py-10">
    <!-- Search & Filters -->
    <div class="space-y-4 mb-6">
      <SearchBar v-model="searchQuery" placeholder="搜索路线..." />

      <FilterBar :filters="filters">
        <template #extra>
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium shrink-0 border transition-colors hover:border-primary-500/30"
            style="border-color: var(--border-default); color: var(--text-secondary);"
          >
            <BaseIcon name="Filter" :size="14" />
            更多筛选
          </button>
        </template>
      </FilterBar>
    </div>

    <!-- Results Header -->
    <div class="flex items-center justify-between py-3 mb-4 border-b" style="border-color: var(--border-default);">
      <h3 class="text-sm font-medium" style="color: var(--text-secondary);">找到 12 条符合条件的路线</h3>
      <ViewToggle v-model="activeView" />
    </div>

    <!-- Trail List -->
    <div class="space-y-4 sm:space-y-5">
      <TrailHorizontalCard
        v-for="trail in trails"
        :key="trail.id"
        v-bind="trail"
      />
    </div>
  </main>
</template>
