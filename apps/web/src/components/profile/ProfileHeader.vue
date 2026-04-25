<script setup lang="ts">
import { useUserStore } from '../../stores/useUserStore'
import BaseIcon from '../common/BaseIcon.vue'
import ActionButton from '../common/ActionButton.vue'

const userStore = useUserStore()

defineEmits<{
  (e: 'show-edit'): void
  (e: 'show-hiking-profile'): void
  (e: 'show-settings'): void
}>()
</script>

<template>
  <div v-if="userStore.profile" class="card p-5 sm:p-8">
    <div class="flex flex-col items-center gap-5 sm:flex-row sm:items-start sm:gap-8">
      <div class="relative shrink-0">
        <div
          class="flex h-20 w-20 items-center justify-center rounded-full border-4 text-2xl font-bold text-white shadow-inner sm:h-24 sm:w-24"
          :style="{ background: userStore.profile.avatarBg, borderColor: 'var(--border-default)' }"
        >
          <img
            v-if="userStore.profile.avatarMediaUrl"
            :src="userStore.profile.avatarMediaUrl"
            alt="用户头像"
            class="h-full w-full rounded-full object-cover"
          />
          <span v-else>{{ userStore.profile.avatar }}</span>
        </div>
        <button
          class="bg-primary-500 absolute -right-1 -bottom-1 flex h-8 w-8 items-center justify-center rounded-full border-2 text-white shadow-lg"
          style="border-color: var(--bg-card)"
          @click="$emit('show-edit')"
        >
          <BaseIcon name="Pencil" :size="14" />
        </button>
      </div>
      <div class="flex-1 space-y-3 text-center sm:text-left">
        <div class="flex flex-col items-center gap-2 sm:flex-row sm:items-start">
          <h1 class="text-xl font-bold sm:text-2xl" style="color: var(--text-primary)">
            {{ userStore.profile.username }}
          </h1>
        </div>
        <p class="text-sm" style="color: var(--text-secondary)">
          {{ userStore.profile.role }} • {{ userStore.profile.joinDate }}加入
        </p>
        <p class="text-xs italic" style="color: var(--text-tertiary)">
          {{ userStore.profile.bio }}
        </p>
        <div class="flex items-center justify-center gap-2 sm:justify-start">
          <ActionButton variant="primary" @click="$emit('show-edit')">
            <BaseIcon name="Settings2" :size="16" />
            编辑资料
          </ActionButton>
          <ActionButton variant="secondary" @click="$emit('show-hiking-profile')">
            <BaseIcon name="Route" :size="16" />
            徒步画像
          </ActionButton>
          <ActionButton variant="secondary" @click="$emit('show-settings')">
            <BaseIcon name="Shield" :size="16" />
            账户设置
          </ActionButton>
        </div>
      </div>
      <div class="flex gap-4 sm:flex-col sm:gap-3">
        <div
          class="group hover:border-primary-500/50 rounded-xl border px-6 py-3 text-center transition-colors"
          style="border-color: var(--border-default); background-color: var(--bg-page)"
        >
          <p class="text-primary-500 text-xl font-bold">{{ userStore.profile.postCount }}</p>
          <p
            class="mt-0.5 text-[10px] font-semibold tracking-wider uppercase"
            style="color: var(--text-tertiary)"
          >
            我的发布
          </p>
        </div>
        <div
          class="group hover:border-primary-500/50 rounded-xl border px-6 py-3 text-center transition-colors"
          style="border-color: var(--border-default); background-color: var(--bg-page)"
        >
          <p class="text-primary-500 text-xl font-bold">{{ userStore.profile.savedCount }}</p>
          <p
            class="mt-0.5 text-[10px] font-semibold tracking-wider uppercase"
            style="color: var(--text-tertiary)"
          >
            已保存
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
