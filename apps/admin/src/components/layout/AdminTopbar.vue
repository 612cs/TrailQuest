<script setup lang="ts">
import { LogOut, Menu, MoonStar, PanelLeftClose, SunMedium } from 'lucide-vue-next'
import type { CurrentUser } from '../../types/auth'

const props = defineProps<{
  title: string
  collapsed: boolean
  user: CurrentUser | null
  themeLabel: string
}>()

const emit = defineEmits<{
  (event: 'toggle-sidebar'): void
  (event: 'toggle-theme'): void
  (event: 'logout'): void
}>()

function userInitials() {
  const name = props.user?.username?.trim() || 'Admin'
  return name.slice(0, 2).toUpperCase()
}
</script>

<template>
  <header class="admin-topbar">
    <div class="admin-topbar__left">
      <button type="button" class="admin-topbar__toggle" @click="emit('toggle-sidebar')">
        <component :is="props.collapsed ? Menu : PanelLeftClose" :size="18" :stroke-width="2.5" />
      </button>
      <h1 class="admin-topbar__title">{{ props.title }}</h1>
    </div>

    <div class="admin-topbar__right">
      <button type="button" class="theme-switch" @click="emit('toggle-theme')">
        <SunMedium v-if="themeLabel === '浅色'" :size="15" :stroke-width="2.5" />
        <MoonStar v-else :size="15" :stroke-width="2.5" />
        <span>{{ props.themeLabel }}</span>
      </button>

      <div class="admin-topbar__user">
        <div class="admin-topbar__avatar">
          <img
            v-if="props.user?.avatarMediaUrl"
            :src="props.user.avatarMediaUrl"
            :alt="props.user.username"
          />
          <span v-else>{{ userInitials() }}</span>
        </div>
        <strong>{{ props.user?.username || '管理员' }}</strong>
      </div>

      <button type="button" class="logout-btn" title="退出登录" @click="emit('logout')">
        <LogOut :size="17" :stroke-width="2.5" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1.5rem;
  background: var(--bg-surface);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
  z-index: 100;
  transition: all 0.3s ease;
  height: 3.5rem;
}

.admin-topbar__left,
.admin-topbar__right {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.admin-topbar__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border: none;
  background: var(--bg-soft);
  color: var(--primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.admin-topbar__toggle:hover {
  background: var(--primary);
  color: white;
  transform: scale(1.05);
}

.admin-topbar__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 1.15rem;
  font-weight: 700;
}

.theme-switch {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-muted);
  font-size: 0.75rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-switch:hover {
  border-color: var(--primary-soft);
  color: var(--primary);
  background: var(--primary-soft);
}

.admin-topbar__user {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.3rem 0.6rem;
  border-radius: 10px;
  background: var(--bg-soft);
}

.admin-topbar__avatar {
  width: 1.6rem;
  height: 1.6rem;
  border-radius: 6px;
  overflow: hidden;
  background: var(--primary);
  color: white;
  display: grid;
  place-items: center;
  font-size: 0.7rem;
  font-weight: 800;
}

.admin-topbar__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-topbar__user strong {
  font-size: 0.8125rem;
  color: var(--text-strong);
  font-weight: 600;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--danger);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(181, 68, 68, 0.1);
  border-color: var(--danger);
}

@media (max-width: 960px) {
  .admin-topbar__user strong {
    display: none;
  }
  .theme-switch span {
    display: none;
  }
}
</style>
