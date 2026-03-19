<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import BaseIcon from '../common/BaseIcon.vue'
import BaseModal from '../common/BaseModal.vue'
import type { HikingProfile, HikingProfileFormValue } from '../../types/hikingProfile'
import HikingProfileForm from '../profile/HikingProfileForm.vue'
import { useUserStore } from '../../stores/useUserStore'

type AuthView = 'login' | 'register-profile' | 'register-account'

const userStore = useUserStore()

const currentView = ref<AuthView>('login')
const email = ref('')
const username = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const errorCode = ref('')
const hikingProfileForm = ref<HikingProfileFormValue>({
  experienceLevel: '',
  trailStyle: '',
  packPreference: '',
  location: '',
})

const canSubmitLogin = computed(() => email.value.includes('@') && password.value.length >= 6)
const canContinueProfile = computed(() =>
  !!hikingProfileForm.value.experienceLevel
  && !!hikingProfileForm.value.trailStyle
  && !!hikingProfileForm.value.packPreference,
)
const canSubmitRegister = computed(() =>
  email.value.includes('@') && password.value.length >= 6 && username.value.trim().length >= 2,
)

const modalTitle = computed(() => {
  if (currentView.value === 'login') return '欢迎回来'
  if (currentView.value === 'register-profile') return '先了解一下你'
  return '加入 TrailQuest'
})

watch(
  () => userStore.showAuthModal,
  (isOpen) => {
    if (isOpen) {
      return
    }
    resetAll()
  },
)

function resetAll() {
  currentView.value = 'login'
  email.value = ''
  username.value = ''
  password.value = ''
  errorMessage.value = ''
  errorCode.value = ''
  isLoading.value = false
  hikingProfileForm.value = {
    experienceLevel: '',
    trailStyle: '',
    packPreference: '',
    location: '',
  }
}

function goToRegisterProfile() {
  errorMessage.value = ''
  errorCode.value = ''
  currentView.value = 'register-profile'
}

function goToLogin() {
  errorMessage.value = ''
  errorCode.value = ''
  currentView.value = 'login'
}

function skipToRegister() {
  errorMessage.value = ''
  errorCode.value = ''
  hikingProfileForm.value = {
    ...hikingProfileForm.value,
    experienceLevel: '',
    trailStyle: '',
    packPreference: '',
  }
  currentView.value = 'register-account'
}

function continueToRegister() {
  if (!canContinueProfile.value) {
    return
  }
  errorMessage.value = ''
  errorCode.value = ''
  currentView.value = 'register-account'
}

function backToProfile() {
  errorMessage.value = ''
  errorCode.value = ''
  currentView.value = 'register-profile'
}

function continueRegisterFromMissingUser() {
  errorMessage.value = ''
  errorCode.value = ''
  password.value = ''
  username.value = ''
  currentView.value = 'register-profile'
}

async function handleSubmit() {
  if (currentView.value === 'login') {
    if (!canSubmitLogin.value) return
    isLoading.value = true
    errorMessage.value = ''
    errorCode.value = ''
    const result = await userStore.login(email.value, password.value)
    isLoading.value = false
    if (result.success) {
      resetAll()
      return
    }
    errorMessage.value = result.message || '操作失败'
    errorCode.value = result.code || ''
    return
  }

  if (!canSubmitRegister.value) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  errorCode.value = ''
  const hikingProfile = buildCompleteHikingProfile(hikingProfileForm.value)
  const result = await userStore.register({
    email: email.value,
    password: password.value,
    username: username.value,
    location: hikingProfileForm.value.location,
    hikingProfile,
  })
  isLoading.value = false

  if (result.success) {
    resetAll()
    return
  }
  errorMessage.value = result.message || '操作失败'
}

function buildCompleteHikingProfile(form: HikingProfileFormValue): HikingProfile | null {
  if (!form.experienceLevel || !form.trailStyle || !form.packPreference) {
    return null
  }

  return {
    experienceLevel: form.experienceLevel,
    trailStyle: form.trailStyle,
    packPreference: form.packPreference,
  }
}

const isUserNotFoundError = computed(() => currentView.value === 'login' && errorCode.value === 'USER_NOT_FOUND')
</script>

<template>
  <BaseModal
    :show="userStore.showAuthModal"
    :title="modalTitle"
    @update:show="userStore.showAuthModal = $event"
    class="auth-modal"
  >
    <div class="space-y-6">
      <div v-if="currentView === 'login'" class="space-y-6">
        <p class="text-sm" style="color: var(--text-secondary);">
          登录以保存路线、发表评论和与社区互动。
        </p>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div v-if="errorMessage" class="mb-4 flex items-center gap-2 rounded-lg px-4 py-2.5 text-sm" style="background-color: var(--bg-tag); color: var(--color-hard);">
            <BaseIcon name="AlertCircle" :size="16" />
            <span>{{ errorMessage }}</span>
          </div>

          <div
            v-if="isUserNotFoundError"
            class="rounded-2xl border px-4 py-4"
            style="border-color: var(--border-default); background-color: var(--bg-tag);"
          >
            <div class="flex items-start gap-3">
              <div class="flex h-9 w-9 items-center justify-center rounded-full" style="background-color: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--primary-500);">
                <BaseIcon name="UserPlus" :size="18" />
              </div>
              <div class="min-w-0 flex-1">
                <p class="text-sm font-semibold" style="color: var(--text-primary);">这个邮箱还没有注册</p>
                <p class="mt-1 text-sm" style="color: var(--text-secondary);">
                  继续注册时会保留当前邮箱，并先进入补充信息页。
                </p>
                <button
                  type="button"
                  class="mt-3 inline-flex items-center gap-2 rounded-xl bg-primary-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-600"
                  @click="continueRegisterFromMissingUser"
                >
                  <BaseIcon name="ArrowRight" :size="16" />
                  <span>去注册</span>
                </button>
              </div>
            </div>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium" style="color: var(--text-primary);">邮箱</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
                <BaseIcon name="Mail" :size="18" />
              </span>
              <input
                v-model="email"
                type="email"
                placeholder="your@email.com"
                class="w-full rounded-xl border py-2.5 pl-10 pr-4 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
              />
            </div>
          </div>

          <div class="space-y-1">
            <div class="flex items-center justify-between">
              <label class="block text-sm font-medium" style="color: var(--text-primary);">密码</label>
              <a href="#" class="text-xs font-medium text-primary-500 hover:underline">忘记密码？</a>
            </div>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
                <BaseIcon name="Lock" :size="18" />
              </span>
              <input
                v-model="password"
                type="password"
                placeholder="••••••••"
                class="w-full rounded-xl border py-2.5 pl-10 pr-4 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="!canSubmitLogin || isLoading"
            class="mt-6 flex w-full items-center justify-center gap-2 rounded-xl py-3 font-medium text-white transition-all disabled:cursor-not-allowed"
            :class="!canSubmitLogin || isLoading ? 'opacity-70' : 'bg-primary-500 hover:bg-primary-600 hover:shadow-lg hover:shadow-primary-500/30 active:scale-[0.98]'"
            :style="!canSubmitLogin || isLoading ? 'background-color: var(--color-surface-400);' : ''"
          >
            <BaseIcon v-if="isLoading" name="Loader2" :size="20" class="animate-spin" />
            <span>{{ isLoading ? '请稍候...' : '登录' }}</span>
          </button>
        </form>

        <div class="border-t pt-4 text-center text-sm" style="border-color: var(--border-default); color: var(--text-secondary);">
          还没有账户？
          <button class="font-semibold text-primary-500 transition-colors hover:underline" @click="goToRegisterProfile">
            立即注册
          </button>
        </div>
      </div>

      <div v-else-if="currentView === 'register-profile'" class="space-y-6">
        <div class="space-y-2">
          <p class="text-sm" style="color: var(--text-secondary);">
            这一步是可选的。告诉我们你的徒步偏好，后续推荐会更贴近你，也 <b class="text-base text-primary-500">可以先跳过</b> 直接注册。
          </p>
        </div>

        <HikingProfileForm v-model="hikingProfileForm" />

        <div class="flex gap-3">
          <button
            type="button"
            class="flex-1 rounded-xl border px-4 py-3 text-sm font-medium transition-colors hover:border-primary-500/30 hover:bg-primary-500/6"
            style="border-color: var(--border-default); color: var(--text-secondary);"
            @click="skipToRegister"
          >
            跳过
          </button>
          <button
            type="button"
            class="flex-1 rounded-xl bg-primary-500 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="!canContinueProfile"
            @click="continueToRegister"
          >
            下一步
          </button>
        </div>

        <div class="border-t pt-4 text-center text-sm" style="border-color: var(--border-default); color: var(--text-secondary);">
          已有账户？
          <button class="font-semibold text-primary-500 transition-colors hover:underline" @click="goToLogin">
            去登录
          </button>
        </div>
      </div>

      <div v-else class="space-y-6">
        <p class="text-sm" style="color: var(--text-secondary);">
          创建一个账户，开始你的户外探索之旅。已填写的补充信息会在注册成功后直接保存到个人主页。
        </p>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div v-if="errorMessage" class="mb-4 flex items-center gap-2 rounded-lg px-4 py-2.5 text-sm" style="background-color: var(--bg-tag); color: var(--color-hard);">
            <BaseIcon name="AlertCircle" :size="16" />
            <span>{{ errorMessage }}</span>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium" style="color: var(--text-primary);">邮箱</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
                <BaseIcon name="Mail" :size="18" />
              </span>
              <input
                v-model="email"
                type="email"
                placeholder="your@email.com"
                class="w-full rounded-xl border py-2.5 pl-10 pr-4 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
              />
            </div>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium" style="color: var(--text-primary);">用户昵称</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
                <BaseIcon name="User" :size="18" />
              </span>
              <input
                v-model="username"
                type="text"
                placeholder="你的昵称"
                class="w-full rounded-xl border py-2.5 pl-10 pr-4 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
              />
            </div>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium" style="color: var(--text-primary);">密码</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
                <BaseIcon name="Lock" :size="18" />
              </span>
              <input
                v-model="password"
                type="password"
                placeholder="••••••••"
                class="w-full rounded-xl border py-2.5 pl-10 pr-4 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
                style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
              />
            </div>
          </div>

          <div class="rounded-2xl border px-4 py-3 text-sm" style="border-color: var(--border-default); background-color: var(--bg-tag); color: var(--text-secondary);">
            <p>地区：{{ hikingProfileForm.location || '未填写' }}</p>
            <p class="mt-1">
              徒步画像：
              {{ hikingProfileForm.experienceLevel ? '已补充' : '已跳过' }}
            </p>
          </div>

          <div class="flex gap-3 pt-2">
            <button
              type="button"
              class="rounded-xl border px-4 py-3 text-sm font-medium transition-colors hover:border-primary-500/30 hover:bg-primary-500/6"
              style="border-color: var(--border-default); color: var(--text-secondary);"
              @click="backToProfile"
            >
              上一步
            </button>
            <button
              type="submit"
              :disabled="!canSubmitRegister || isLoading"
              class="flex-1 rounded-xl py-3 font-medium text-white transition-all disabled:cursor-not-allowed"
              :class="!canSubmitRegister || isLoading ? 'opacity-70' : 'bg-primary-500 hover:bg-primary-600 hover:shadow-lg hover:shadow-primary-500/30 active:scale-[0.98]'"
              :style="!canSubmitRegister || isLoading ? 'background-color: var(--color-surface-400);' : ''"
            >
              <span>{{ isLoading ? '请稍候...' : '注册' }}</span>
            </button>
          </div>
        </form>

        <div class="border-t pt-4 text-center text-sm" style="border-color: var(--border-default); color: var(--text-secondary);">
          已有账户？
          <button class="font-semibold text-primary-500 transition-colors hover:underline" @click="goToLogin">
            去登录
          </button>
        </div>
      </div>
    </div>
  </BaseModal>
</template>

<style scoped>
:deep(.modal-content) {
  padding: 0 !important;
}
</style>
