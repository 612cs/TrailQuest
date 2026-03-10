<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import ProfileHeader from '../components/profile/ProfileHeader.vue'
import Pagination from '../components/common/Pagination.vue'
import EditProfileModal from '../components/profile/EditProfileModal.vue'
import AccountSettingsModal from '../components/profile/AccountSettingsModal.vue'
import { useUserStore } from '../stores/useUserStore'

const userStore = useUserStore()

const activeTab = ref<'posts' | 'saved'>('posts')
const currentPage = ref(1)
const totalPages = 3

const showEditModal = ref(false)
const showSettingsModal = ref(false)

const posts = [
  { id: 1, image: '/trail-pine.png', title: '隐藏海岸小径指南', editTime: '2小时前编辑', visibility: '公开' },
  { id: 2, image: '/trail-foggy.png', title: '高山脊摄影点', editTime: '昨天编辑', visibility: '草稿' },
  { id: 3, image: '/trail-lake.png', title: '城市咖啡之旅 - 市中心', editTime: '3天前编辑', visibility: '私密' },
  { id: 4, image: '/hero-mountain.png', title: '原生林探险', editTime: '1周前编辑', visibility: '公开' },
]
</script>

<template>
  <main class="max-w-4xl mx-auto px-4 sm:px-6 py-8 sm:py-12 space-y-6">
    <template v-if="userStore.isLoggedIn">
      <ProfileHeader 
        @show-edit="showEditModal = true"
        @show-settings="showSettingsModal = true"
      />

    <!-- Modals -->
    <EditProfileModal v-model:show="showEditModal" />
    <AccountSettingsModal v-model:show="showSettingsModal" />

    <!-- Tabs & Content Card -->
    <div class="card overflow-hidden">
      <div class="flex items-center border-b" style="border-color: var(--border-default); background-color: var(--bg-tag);">
        <button @click="activeTab = 'posts'" class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition-colors" :class="activeTab === 'posts' ? 'border-primary-500 text-primary-500' : 'border-transparent'" :style="activeTab !== 'posts' ? 'color: var(--text-secondary)' : ''">
          <BaseIcon name="FileText" :size="16" />
          我的发布
        </button>
        <button @click="activeTab = 'saved'" class="flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition-colors" :class="activeTab === 'saved' ? 'border-primary-500 text-primary-500' : 'border-transparent'" :style="activeTab !== 'saved' ? 'color: var(--text-secondary)' : ''">
          <BaseIcon name="Bookmark" :size="16" />
          我的收藏
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
    </template>
    
    <template v-else>
      <div class="card p-8 sm:p-12 text-center rounded-2xl flex flex-col items-center justify-center min-h-[50vh]">
        <div class="w-20 h-20 rounded-full flex items-center justify-center mb-6" style="background-color: var(--bg-tag); color: var(--color-primary-500);">
          <BaseIcon name="User" :size="36" />
        </div>
        <h2 class="text-2xl font-bold mb-3" style="color: var(--text-primary);">欢迎来到 TrailQuest</h2>
        <p class="text-sm sm:text-base mb-8 max-w-sm mx-auto leading-relaxed" style="color: var(--text-secondary);">
          登录以查看您的个人资料、回味走过的路线以及管理您的收藏内容。
        </p>
        <button 
          @click="userStore.showAuthModal = true"
          class="px-8 py-3 rounded-xl font-bold text-white bg-primary-500 hover:bg-primary-600 transition-all shadow-lg hover:shadow-xl shadow-primary-500/30 active:scale-[0.98]"
        >
          立即登录 / 注册
        </button>
      </div>
    </template>
  </main>
</template>

<style scoped>
.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s ease;
}
.slide-enter-from, .slide-leave-to {
  transform: translateY(20px);
  opacity: 0;
}
</style>
