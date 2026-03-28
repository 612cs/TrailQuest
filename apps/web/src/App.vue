<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AuthModal from './components/auth/AuthModal.vue'
import FlashToast from './components/common/FlashToast.vue'

const route = useRoute()
const hideHeaderRoutes = ['TrailDetail', 'TrailGallery']
const showHeader = computed(() => !hideHeaderRoutes.includes(route.name as string))
const headerVariant = computed<'default' | 'overlay'>(() => (
  route.meta.headerVariant === 'overlay' ? 'overlay' : 'default'
))
</script>

<template>
  <div class="min-h-screen" style="background-color: var(--bg-page); color: var(--text-primary);">
    <AppHeader v-if="showHeader" :variant="headerVariant" />
    <RouterView v-slot="{ Component, route }">
      <keep-alive>
        <component :is="Component" v-if="route.meta.keepAlive" :key="route.fullPath" />
      </keep-alive>
      <component :is="Component" v-if="!route.meta.keepAlive" :key="route.fullPath" />
    </RouterView>
    <AuthModal />
    <FlashToast />
  </div>
</template>
