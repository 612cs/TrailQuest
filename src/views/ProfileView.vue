<script setup lang="ts">
import { ref } from 'vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import ProfileHeader from '../components/profile/ProfileHeader.vue'
import Pagination from '../components/common/Pagination.vue'

const activeTab = ref<'posts' | 'saved'>('posts')
const currentPage = ref(1)
const totalPages = 3

const posts = [
  { id: 1, image: '/trail-pine.png', title: '隐藏海岸小径指南', editTime: '2小时前编辑', visibility: '公开' },
  { id: 2, image: '/trail-foggy.png', title: '高山脊摄影点', editTime: '昨天编辑', visibility: '草稿' },
  { id: 3, image: '/trail-lake.png', title: '城市咖啡之旅 - 市中心', editTime: '3天前编辑', visibility: '私密' },
  { id: 4, image: '/hero-mountain.png', title: '原生林探险', editTime: '1周前编辑', visibility: '公开' },
]
</script>

<template>
  <main class="max-w-4xl mx-auto px-4 sm:px-6 py-8 sm:py-12 space-y-6">
    <ProfileHeader />

    <!-- Tabs & Content Card -->
    <div class="card overflow-hidden">
      <div class="flex items-center border-b" style="border-color: var(--border-default); background-color: var(--bg-tag);">
        <button @click="activeTab = 'posts'" class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition-colors" :class="activeTab === 'posts' ? 'border-primary-500 text-primary-500' : 'border-transparent'" :style="activeTab !== 'posts' ? 'color: var(--text-secondary)' : ''">
          <BaseIcon name="FileText" :size="16" />
          我的发布
        </button>
        <button @click="activeTab = 'saved'" class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition-colors" :class="activeTab === 'saved' ? 'border-primary-500 text-primary-500' : 'border-transparent'" :style="activeTab !== 'saved' ? 'color: var(--text-secondary)' : ''">
          <BaseIcon name="Bookmark" :size="16" />
          保存记录
        </button>
      </div>
      <div class="divide-y" style="border-color: var(--border-default);">
        <div v-for="post in posts" :key="post.id" class="flex items-center gap-4 p-4 hover:bg-primary-500/5 transition-colors cursor-pointer" style="border-color: var(--border-default);">
          <div class="w-14 h-14 rounded-lg overflow-hidden shrink-0"><img :src="post.image" :alt="post.title" class="w-full h-full object-cover" /></div>
          <div class="flex-1 min-w-0">
            <h3 class="text-sm font-medium truncate" style="color: var(--text-primary);">{{ post.title }}</h3>
            <p class="text-xs mt-1" style="color: var(--text-secondary);">{{ post.editTime }} • {{ post.visibility }}</p>
          </div>
          <BaseIcon name="ChevronRight" :size="16" style="color: var(--text-tertiary);" />
        </div>
      </div>
      <div class="flex items-center justify-between px-4 py-3" style="background-color: var(--bg-tag);">
        <span class="text-xs" style="color: var(--text-tertiary);">显示第 1-4 条，共 24 条发布</span>
        <Pagination
          v-model:current="currentPage"
          :total="totalPages"
          :max-visible="3"
        />
      </div>
    </div>
  </main>
</template>
