<script setup lang="ts">
import { ref, computed } from 'vue'
import BaseModal from '../common/BaseModal.vue'
import BaseIcon from '../common/BaseIcon.vue'
import { useUserStore } from '../../stores/useUserStore'

const userStore = useUserStore()
const isLoginMode = ref(true)

const email = ref('')
const username = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

const isValid = computed(() => {
  const baseValid = email.value.includes('@') && password.value.length >= 6
  if (isLoginMode.value) return baseValid
  return baseValid && username.value.trim().length >= 2
})

async function handleSubmit() {
  if (!isValid.value) return

  isLoading.value = true
  errorMessage.value = ''
  
  // Simulate network request
  await new Promise(resolve => setTimeout(resolve, 800))
  
  let result
  if (isLoginMode.value) {
    result = userStore.login(email.value, password.value)
  } else {
    result = userStore.register(email.value, password.value, username.value)
  }
  
  if (result.success) {
      // Reset form
      email.value = ''
      username.value = ''
      password.value = ''
      isLoginMode.value = true
  } else {
      errorMessage.value = result.message || '操作失败'
  }
  
  isLoading.value = false
}
</script>

<template>
  <BaseModal
    :show="userStore.showAuthModal"
    @update:show="userStore.showAuthModal = $event"
    :title="isLoginMode ? '欢迎回来' : '加入 TrailQuest'"
    class="auth-modal"
  >
    <div class="space-y-6">
      <!-- Welcome Text -->
      <p class="text-sm" style="color: var(--text-secondary);">
        {{ isLoginMode 
          ? '登录以保存路线、发表评论和与社区互动。' 
          : '创建一个账户，开始您的户外探索之旅。' 
        }}
      </p>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div v-if="errorMessage" class="bg-red-50 text-red-600 text-sm px-4 py-2.5 rounded-lg mb-4 flex items-center gap-2">
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
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
            />
          </div>
        </div>

        <div v-if="!isLoginMode" class="space-y-1">
          <label class="block text-sm font-medium" style="color: var(--text-primary);">用户昵称</label>
          <div class="relative">
            <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
              <BaseIcon name="User" :size="18" />
            </span>
            <input 
              v-model="username"
              type="text" 
              placeholder="您的昵称"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
            />
          </div>
        </div>

        <div class="space-y-1">
          <div class="flex justify-between items-center">
            <label class="block text-sm font-medium" style="color: var(--text-primary);">密码</label>
            <a v-if="isLoginMode" href="#" class="text-xs font-medium hover:underline text-primary-500">忘记密码？</a>
          </div>
          <div class="relative">
            <span class="absolute left-3 top-1/2 -translate-y-1/2" style="color: var(--text-tertiary);">
              <BaseIcon name="Lock" :size="18" />
            </span>
            <input 
              v-model="password"
              type="password" 
              placeholder="••••••••"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); border-color: var(--border-default); color: var(--text-primary);"
            />
          </div>
        </div>

        <button 
          type="submit"
          :disabled="!isValid || isLoading"
          class="w-full py-3 rounded-xl font-medium text-white transition-all flex items-center justify-center gap-2 mt-6 disabled:cursor-not-allowed"
          :class="!isValid || isLoading ? 'bg-gray-400 opacity-70' : 'bg-primary-500 hover:bg-primary-600 hover:shadow-lg hover:shadow-primary-500/30 active:scale-[0.98]'"
        >
          <BaseIcon v-if="isLoading" name="Loader2" :size="20" class="animate-spin" />
          <span>{{ isLoading ? '请稍候...' : (isLoginMode ? '登录' : '注册') }}</span>
        </button>
      </form>

      <!-- Toggle Mode -->
      <div class="pt-4 text-center border-t text-sm" style="border-color: var(--border-default); color: var(--text-secondary);">
        {{ isLoginMode ? '还没有账户？' : '已有账户？' }}
        <button 
          class="font-semibold transition-colors hover:underline text-primary-500"
          @click="isLoginMode = !isLoginMode"
        >
          {{ isLoginMode ? '立即注册' : '去登录' }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<style scoped>
/* Remove padding from base modal to have edge-to-edge content */
:deep(.modal-content) {
  padding: 0 !important;
}
</style>
