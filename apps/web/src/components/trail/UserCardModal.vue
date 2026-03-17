<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import type { UserCard } from '../../types/review'

defineProps<{
  show: boolean
  userCard: UserCard | null
  isLoading?: boolean
  errorMessage?: string
}>()

const emit = defineEmits<{
  (event: 'close'): void
}>()

function getRoleLabel(role?: UserCard['role']) {
  return role === 'ADMIN' ? '管理员' : '徒步爱好者'
}
</script>

<template>
  <Teleport to="body">
    <Transition name="user-card-fade">
      <div v-if="show" class="fixed inset-0 z-[80] flex items-center justify-center px-4">
        <div class="absolute inset-0 bg-black/10 backdrop-blur-[1px]" @click="emit('close')" />
        <div class="relative w-full max-w-sm rounded-3xl border shadow-[0_24px_60px_rgba(0,0,0,0.16)] overflow-hidden" style="background-color: var(--bg-card); border-color: var(--border-default);">
          <button
            type="button"
            class="absolute right-4 top-4 z-10 h-9 w-9 rounded-full border flex items-center justify-center transition-colors hover:bg-primary-500/8"
            style="border-color: var(--border-default); color: var(--text-tertiary);"
            @click="emit('close')"
          >
            <BaseIcon name="X" :size="18" />
          </button>

          <div class="px-6 pt-6 pb-5" style="background: linear-gradient(135deg, color-mix(in srgb, var(--primary-500) 18%, transparent), transparent 62%);">
            <div v-if="isLoading" class="py-10 text-center text-sm" style="color: var(--text-secondary);">
              正在加载用户资料...
            </div>

            <div v-else-if="errorMessage" class="py-8 text-center">
              <div class="mx-auto mb-3 h-12 w-12 rounded-full flex items-center justify-center" style="background-color: color-mix(in srgb, var(--color-hard) 10%, transparent); color: var(--color-hard);">
                <BaseIcon name="AlertCircle" :size="22" />
              </div>
              <p class="text-sm font-medium" style="color: var(--text-primary);">加载用户卡片失败</p>
              <p class="mt-1 text-xs" style="color: var(--text-secondary);">{{ errorMessage }}</p>
            </div>

            <div v-else-if="userCard" class="space-y-5">
              <div class="flex items-start gap-4 pr-10">
                <div class="h-16 w-16 shrink-0 overflow-hidden rounded-2xl border" style="border-color: var(--border-default);">
                  <img
                    v-if="userCard.avatarMediaUrl"
                    :src="userCard.avatarMediaUrl"
                    alt="用户头像"
                    class="h-full w-full object-cover"
                  />
                  <div
                    v-else
                    class="flex h-full w-full items-center justify-center text-lg font-bold text-white"
                    :style="{ backgroundColor: userCard.avatarBg }"
                  >
                    {{ userCard.avatar }}
                  </div>
                </div>

                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h3 class="text-lg font-semibold leading-tight" style="color: var(--text-primary);">{{ userCard.username }}</h3>
                    <span class="rounded-full px-2.5 py-1 text-[11px] font-medium" style="background-color: var(--bg-tag); color: var(--primary-500);">
                      {{ getRoleLabel(userCard.role) }}
                    </span>
                  </div>
                  <p class="mt-1 text-xs" style="color: var(--text-tertiary);">{{ userCard.joinDate }}</p>
                  <p class="mt-3 text-sm leading-relaxed" style="color: var(--text-secondary);">{{ userCard.bio }}</p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="rounded-2xl border px-4 py-3" style="border-color: var(--border-default);">
                  <p class="text-[11px] uppercase tracking-[0.12em]" style="color: var(--text-tertiary);">地区</p>
                  <p class="mt-2 text-sm font-medium" style="color: var(--text-primary);">{{ userCard.location }}</p>
                </div>
                <div class="rounded-2xl border px-4 py-3" style="border-color: var(--border-default);">
                  <p class="text-[11px] uppercase tracking-[0.12em]" style="color: var(--text-tertiary);">收藏路线</p>
                  <p class="mt-2 text-sm font-medium" style="color: var(--text-primary);">{{ userCard.savedCount }}</p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="rounded-2xl px-4 py-3" style="background-color: var(--bg-tag);">
                  <p class="text-[11px]" style="color: var(--text-tertiary);">发布路线</p>
                  <p class="mt-1 text-xl font-semibold text-primary-500">{{ userCard.postCount }}</p>
                </div>
                <div class="rounded-2xl px-4 py-3" style="background-color: var(--bg-tag);">
                  <p class="text-[11px]" style="color: var(--text-tertiary);">已加入</p>
                  <p class="mt-1 text-sm font-medium" style="color: var(--text-primary);">{{ userCard.joinDate }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.user-card-fade-enter-active,
.user-card-fade-leave-active {
  transition: opacity 0.2s ease;
}

.user-card-fade-enter-from,
.user-card-fade-leave-to {
  opacity: 0;
}
</style>
