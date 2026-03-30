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
  <header class="admin-topbar admin-panel">
    <div class="admin-topbar__left">
      <button type="button" class="admin-topbar__icon-btn" @click="emit('toggle-sidebar')">
        <component :is="props.collapsed ? Menu : PanelLeftClose" :size="18" :stroke-width="2" />
      </button>
      <div>
        <p class="admin-topbar__eyebrow">TrailQuest Admin</p>
        <h1 class="admin-topbar__title">{{ props.title }}</h1>
      </div>
    </div>

    <div class="admin-topbar__right">
      <button type="button" class="admin-topbar__theme" @click="emit('toggle-theme')">
        <SunMedium v-if="themeLabel === '浅色'" :size="16" :stroke-width="2" />
        <MoonStar v-else :size="16" :stroke-width="2" />
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
        <div class="admin-topbar__user-info">
          <strong>{{ props.user?.username || '管理员' }}</strong>
          <small>{{ props.user?.email || 'TrailQuest 管理员' }}</small>
        </div>
      </div>

      <button type="button" class="admin-topbar__icon-btn danger" @click="emit('logout')">
        <LogOut :size="18" :stroke-width="2" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 1rem 1.15rem;
}

.admin-topbar__left,
.admin-topbar__right {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.admin-topbar__eyebrow {
  margin: 0 0 0.1rem;
  color: var(--primary);
  font-size: 0.78rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.admin-topbar__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 1.4rem;
}

.admin-topbar__icon-btn,
.admin-topbar__theme {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.72rem 0.95rem;
  background: transparent;
  color: var(--text-strong);
  cursor: pointer;
}

.admin-topbar__icon-btn.danger {
  color: var(--danger);
}

.admin-topbar__theme {
  color: var(--text-muted);
}

.admin-topbar__user {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.5rem 0.75rem;
  border-radius: 999px;
  background: var(--bg-soft);
}

.admin-topbar__avatar {
  width: 2.6rem;
  height: 2.6rem;
  overflow: hidden;
  border-radius: 999px;
  border: 1px solid var(--border);
  display: grid;
  place-items: center;
  flex: none;
  background: var(--bg-elevated);
  color: var(--primary);
  font-weight: 700;
}

.admin-topbar__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-topbar__user-info {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.admin-topbar__user-info strong {
  color: var(--text-strong);
}

.admin-topbar__user-info small {
  color: var(--text-muted);
}

@media (max-width: 960px) {
  .admin-topbar__user-info {
    display: none;
  }
}
</style>
