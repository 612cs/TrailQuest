<script setup lang="ts">
import { computed } from 'vue'
import { MoonStar, SunMedium, UserCircle2, Settings2 } from 'lucide-vue-next'

import { useAdminAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import { pinia } from '../stores/pinia'

const authStore = useAdminAuthStore(pinia)
const themeStore = useThemeStore(pinia)

const user = computed(() => authStore.user)
</script>

<template>
  <div class="admin-grid-2">
    <section class="admin-card admin-section">
      <h2 class="admin-title">账户信息</h2>
      <p class="admin-subtitle">当前后台登录状态与账号概览。</p>

      <div class="admin-setting-account" v-if="user">
        <div class="admin-setting-account__avatar">
          <img v-if="user.avatarMediaUrl" :src="user.avatarMediaUrl" :alt="user.username" />
          <span v-else>{{ user.username.slice(0, 2).toUpperCase() }}</span>
        </div>
        <div>
          <strong>{{ user.username }}</strong>
          <p>{{ user.email || '暂无邮箱' }}</p>
          <span class="admin-badge admin-badge-approved">管理员</span>
        </div>
      </div>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">外观设置</h2>
      <p class="admin-subtitle">TrailQuest 绿色主题，支持浅色与深色。</p>

      <button class="admin-button admin-button-secondary admin-setting-theme" type="button" @click="themeStore.toggle">
        <SunMedium v-if="themeStore.isDark === false" :size="18" :stroke-width="2" />
        <MoonStar v-else :size="18" :stroke-width="2" />
        切换为{{ themeStore.themeLabel === '浅色' ? '深色' : '浅色' }}模式
      </button>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">后台说明</h2>
      <div class="admin-setting-note">
        <UserCircle2 :size="18" :stroke-width="2" />
        <span>后台登录继续复用前台 /api/auth/login 与 /api/auth/me。</span>
      </div>
      <div class="admin-setting-note">
        <Settings2 :size="18" :stroke-width="2" />
        <span>当前版本聚焦路线审核、评论治理与举报占位。</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-setting-account {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.admin-setting-account__avatar {
  width: 3.5rem;
  height: 3.5rem;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid var(--border);
  color: var(--primary);
  background: var(--bg-elevated);
  font-weight: 800;
}

.admin-setting-account__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-setting-account strong {
  display: block;
  color: var(--text-strong);
}

.admin-setting-account p {
  margin: 0.25rem 0 0.55rem;
  color: var(--text-muted);
}

.admin-setting-theme {
  margin-top: 1rem;
}

.admin-setting-note {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  margin-top: 1rem;
  padding: 0.95rem 1rem;
  border-radius: 16px;
  background: var(--bg-soft);
  color: var(--text-muted);
  line-height: 1.7;
}
</style>
