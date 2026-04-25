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
const props = withDefaults(
  defineProps<{
    variant?: 'default' | 'overlay'
  }>(),
  {
    variant: 'default',
  },
)

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
  <header
    class="z-50 w-full"
    :class="props.variant === 'overlay' ? 'absolute top-0 left-0' : 'glass-header sticky top-0'"
    :style="
      props.variant === 'overlay'
        ? 'background-color: transparent; border-color: transparent; backdrop-filter: none;'
        : ''
    "
  >
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-10">
      <div class="flex h-14 items-center justify-between sm:h-16">
        <!-- Logo -->
        <RouterLink to="/" class="flex shrink-0 items-center gap-2">
          <img
            src="/favicon.svg"
            alt="TrailQuest"
            class="h-8 w-8"
            :class="props.variant === 'overlay' ? 'drop-shadow-md' : ''"
          />
          <span
            class="text-lg font-extrabold tracking-tight sm:text-xl"
            :style="
              props.variant === 'overlay' ? 'color: white;' : 'color: var(--color-primary-500);'
            "
          >
            TrailQuest
          </span>
        </RouterLink>

        <!-- Desktop Nav -->
        <nav class="hidden items-center gap-1 md:flex">
          <RouterLink
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            class="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200"
            :class="
              isActive(link.path)
                ? props.variant === 'overlay'
                  ? 'bg-white/20 text-white backdrop-blur-sm'
                  : 'bg-primary-500/10 text-primary-500'
                : props.variant === 'overlay'
                  ? 'hover:bg-white/10'
                  : 'hover:bg-primary-500/5'
            "
            :style="
              !isActive(link.path)
                ? props.variant === 'overlay'
                  ? 'color: rgba(255,255,255,0.88);'
                  : 'color: var(--text-secondary)'
                : ''
            "
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
            class="rounded-full p-2 transition-colors duration-200"
            :class="props.variant === 'overlay' ? 'hover:bg-white/10' : 'hover:bg-primary-500/10'"
            title="发布路线"
          >
            <BaseIcon
              name="PlusCircle"
              :size="20"
              :class="isActive('/publish') && props.variant !== 'overlay' ? 'text-primary-500' : ''"
              :style="
                !isActive('/publish')
                  ? props.variant === 'overlay'
                    ? 'color: white;'
                    : 'color: var(--text-secondary)'
                  : props.variant === 'overlay'
                    ? 'color: white;'
                    : ''
              "
            />
          </RouterLink>

          <!-- Theme Toggle -->
          <button
            @click="themeStore.toggle()"
            class="rounded-full p-2 transition-colors duration-200"
            :class="props.variant === 'overlay' ? 'hover:bg-white/10' : 'hover:bg-primary-500/10'"
            :title="themeStore.isDark ? '切换浅色模式' : '切换深色模式'"
          >
            <BaseIcon
              :name="themeStore.isDark ? 'Sun' : 'Moon'"
              :size="20"
              :style="
                props.variant === 'overlay' ? 'color: white;' : 'color: var(--text-secondary)'
              "
            />
          </button>

          <!-- Profile -->
          <RouterLink to="/profile" class="shrink-0">
            <div
              class="flex h-8 w-8 items-center justify-center overflow-hidden rounded-full border-2 text-sm font-bold sm:h-9 sm:w-9"
              :class="isActive('/profile') ? 'border-primary-500' : ''"
              :style="`background-color: ${props.variant === 'overlay' ? 'rgba(255,255,255,0.16)' : 'var(--color-primary-500)'}; color: white; border-color: ${isActive('/profile') ? 'var(--color-primary-400)' : props.variant === 'overlay' ? 'rgba(255,255,255,0.3)' : 'var(--border-default)'}; backdrop-filter: ${props.variant === 'overlay' ? 'blur(8px)' : 'none'};`"
            >
              <img
                v-if="userStore.profile?.avatarMediaUrl"
                :src="userStore.profile.avatarMediaUrl"
                alt="用户头像"
                class="h-full w-full object-cover"
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
            class="rounded-lg p-2 md:hidden"
            :style="props.variant === 'overlay' ? 'color: white;' : 'color: var(--text-secondary)'"
          >
            <BaseIcon :name="mobileMenuOpen ? 'X' : 'Menu'" :size="20" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide">
      <div
        v-if="mobileMenuOpen"
        class="border-t md:hidden"
        :style="
          props.variant === 'overlay'
            ? 'border-color: rgba(255,255,255,0.18); background-color: rgba(255,255,255,0.16); backdrop-filter: blur(14px);'
            : 'border-color: var(--border-default); background-color: var(--bg-card);'
        "
      >
        <nav class="space-y-1 px-4 py-3">
          <RouterLink
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            @click="mobileMenuOpen = false"
            class="flex items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-medium transition-colors"
            :class="
              isActive(link.path)
                ? props.variant === 'overlay'
                  ? 'bg-white/18 text-white'
                  : 'bg-primary-500/10 text-primary-500'
                : ''
            "
            :style="
              !isActive(link.path)
                ? props.variant === 'overlay'
                  ? 'color: rgba(255,255,255,0.88);'
                  : 'color: var(--text-secondary)'
                : ''
            "
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
