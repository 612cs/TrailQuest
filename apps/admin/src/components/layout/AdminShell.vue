<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, RouterView, useRoute } from 'vue-router'

import AdminSidebar from './AdminSidebar.vue'
import AdminTopbar from './AdminTopbar.vue'
import { useAdminAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'
import { pinia } from '../../stores/pinia'

const collapsed = ref(false)
const authStore = useAdminAuthStore(pinia)
const themeStore = useThemeStore(pinia)
const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => String(route.meta.title || '后台首页'))

function handleLogout() {
  authStore.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <div class="admin-page admin-shell">
    <AdminSidebar :collapsed="collapsed" />

    <div class="admin-shell__main">
      <AdminTopbar
        :title="pageTitle"
        :collapsed="collapsed"
        :user="authStore.user"
        :theme-label="themeStore.themeLabel"
        @toggle-sidebar="collapsed = !collapsed"
        @toggle-theme="themeStore.toggle"
        @logout="handleLogout"
      />

      <main class="admin-shell__content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  height: 100vh;
  overflow: hidden;
}

.admin-shell__main {
  min-width: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 1.15rem 1.15rem 1.35rem 0;
  overflow: hidden;
}

.admin-shell__content {
  flex: 1;
  min-height: 0;
  padding: 0 0.25rem 0.25rem 0;
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .admin-shell {
    grid-template-columns: 5.5rem minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-shell__main {
    padding: 1rem;
  }
}
</style>
