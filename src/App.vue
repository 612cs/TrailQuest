<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'

const route = useRoute()
const hideHeaderRoutes = ['TrailDetail']
const showHeader = computed(() => !hideHeaderRoutes.includes(route.name as string))
</script>

<template>
  <div class="min-h-screen" style="background-color: var(--bg-page); color: var(--text-primary);">
    <AppHeader v-if="showHeader" />
    <RouterView v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <KeepAlive>
          <component :is="Component" />
        </KeepAlive>
      </transition>
    </RouterView>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
