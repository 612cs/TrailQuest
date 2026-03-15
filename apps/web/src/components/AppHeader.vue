<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '../stores/theme'
import { useUserStore } from '../stores/useUserStore'
import BaseIcon from './common/BaseIcon.vue'

const themeStore = useThemeStore()
const userStore = useUserStore()
const route = useRoute()
const mobileMenuOpen = ref(false)

const navLinks = [
  { name: '首页', path: '/', icon: 'Home' },
  { name: '搜索', path: '/search', icon: 'Search' },
  { name: '社区', path: '/community', icon: 'Users' },
]

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <header class="glass-header sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-10">
      <div class="flex items-center justify-between h-14 sm:h-16">
        <!-- Logo -->
        <RouterLink to="/" class="flex items-center gap-2 shrink-0">
          <div class="p-1.5 rounded-lg bg-primary-500 text-white">
            <BaseIcon name="Mountain" :size="20" stroke-width="2.5" />
          </div>
          <span class="text-lg sm:text-xl font-extrabold tracking-tight" style="color: var(--color-primary-500);">
            TrailQuest
          </span>
        </RouterLink>

        <!-- Desktop Nav -->
        <nav class="hidden md:flex items-center gap-1">
          <RouterLink
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center gap-2"
            :class="isActive(link.path)
              ? 'bg-primary-500/10 text-primary-500'
              : 'hover:bg-primary-500/5'"
            :style="!isActive(link.path) ? 'color: var(--text-secondary)' : ''"
          >
            <BaseIcon :name="link.icon" :size="16" />
            {{ link.name }}
          </RouterLink>
        </nav>

        <!-- Right Side -->
        <div class="flex items-center gap-2 sm:gap-3">
          <!-- Publish Button -->
          <RouterLink
            to="/publish"
            class="p-2 rounded-full transition-colors duration-200 hover:bg-primary-500/10"
            title="发布路线"
          >
            <BaseIcon name="PlusCircle" :size="20" :class="isActive('/publish') ? 'text-primary-500' : ''" :style="!isActive('/publish') ? 'color: var(--text-secondary)' : ''" />
          </RouterLink>

          <!-- Theme Toggle -->
          <button
            @click="themeStore.toggle()"
            class="p-2 rounded-full transition-colors duration-200 hover:bg-primary-500/10"
            :title="themeStore.isDark ? '切换浅色模式' : '切换深色模式'"
          >
            <BaseIcon :name="themeStore.isDark ? 'Sun' : 'Moon'" :size="20" style="color: var(--text-secondary)" />
          </button>

          <!-- Profile -->
          <RouterLink to="/profile" class="shrink-0">
            <div
              class="w-8 h-8 sm:w-9 sm:h-9 rounded-full border-2 flex items-center justify-center text-sm font-bold overflow-hidden"
              :class="isActive('/profile') ? 'border-primary-500' : ''"
              :style="`background-color: var(--color-primary-500); color: white; border-color: ${isActive('/profile') ? 'var(--color-primary-400)' : 'var(--border-default)'}`"
            >
              <img
                v-if="userStore.profile?.avatarMediaUrl"
                :src="userStore.profile.avatarMediaUrl"
                alt="用户头像"
                class="w-full h-full object-cover"
              />
              <span v-else-if="userStore.profile" class="leading-none">
                {{ userStore.profile.avatar }}
              </span>
              <BaseIcon v-else name="User" :size="20" />
            </div>
          </RouterLink>

          <!-- Mobile Menu Button -->
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="md:hidden p-2 rounded-lg"
            style="color: var(--text-secondary)"
          >
            <BaseIcon :name="mobileMenuOpen ? 'X' : 'Menu'" :size="20" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide">
      <div v-if="mobileMenuOpen" class="md:hidden border-t" style="border-color: var(--border-default); background-color: var(--bg-card);">
        <nav class="px-4 py-3 space-y-1">
          <RouterLink
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            @click="mobileMenuOpen = false"
            class="flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors"
            :class="isActive(link.path) ? 'bg-primary-500/10 text-primary-500' : ''"
            :style="!isActive(link.path) ? 'color: var(--text-secondary)' : ''"
          >
            <BaseIcon :name="link.icon" :size="18" />
            {{ link.name }}
          </RouterLink>
        </nav>
      </div>
    </transition>
  </header>
</template>


<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
