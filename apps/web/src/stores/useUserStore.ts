import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

import * as authApi from '../api/auth'
import { AUTH_TOKEN_STORAGE_KEY, USER_PROFILE_STORAGE_KEY } from '../api/constants'
import type { CurrentUser } from '../types/auth'
import { ApiError } from '../types/api'

export interface UserProfile {
  id: number
  username: string
  avatar: string
  avatarBg: string
  email?: string
  role: string
  joinDate: string
  postCount: number
  savedCount: number
  bio: string
  location: string
}

interface AuthActionResult {
  success: boolean
  message?: string
}

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null)
  const token = ref('')
  const showAuthModal = ref(false)
  const isBootstrapping = ref(false)

  const storedToken = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
  if (storedToken) {
    token.value = storedToken
  }

  const storedProfile = localStorage.getItem(USER_PROFILE_STORAGE_KEY)
  if (storedProfile) {
    try {
      profile.value = JSON.parse(storedProfile) as UserProfile
    } catch (error) {
      console.error('Failed to parse user profile from local storage', error)
      localStorage.removeItem(USER_PROFILE_STORAGE_KEY)
    }
  }

  watch(token, (newToken) => {
    if (newToken) {
      localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, newToken)
    } else {
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
    }
  })

  watch(
    profile,
    (newProfile) => {
      if (newProfile) {
        localStorage.setItem(USER_PROFILE_STORAGE_KEY, JSON.stringify(newProfile))
      } else {
        localStorage.removeItem(USER_PROFILE_STORAGE_KEY)
      }
    },
    { deep: true },
  )

  const isLoggedIn = computed(() => !!token.value && !!profile.value)

  function updateProfile(updates: Partial<UserProfile>) {
    if (!profile.value) return
    profile.value = { ...profile.value, ...updates }
  }

  async function login(email?: string, password?: string): Promise<AuthActionResult> {
    if (!email || !password) {
      return { success: false, message: '账号或密码不能为空' }
    }

    try {
      const data = await authApi.login({ email, password })
      applyAuthPayload(data.accessToken, data.user)
      showAuthModal.value = false
      return { success: true }
    } catch (error) {
      return { success: false, message: getErrorMessage(error) }
    }
  }

  async function register(email: string, password: string, username: string): Promise<AuthActionResult> {
    try {
      const data = await authApi.register({ email, password, username })
      applyAuthPayload(data.accessToken, data.user)
      showAuthModal.value = false
      return { success: true }
    } catch (error) {
      return { success: false, message: getErrorMessage(error) }
    }
  }

  function logout() {
    token.value = ''
    profile.value = null
  }

  function requireAuth(action: () => void) {
    if (isLoggedIn.value) {
      action()
    } else {
      showAuthModal.value = true
    }
  }

  async function bootstrap() {
    if (!token.value || isBootstrapping.value) {
      return
    }

    isBootstrapping.value = true
    try {
      const currentUser = await authApi.fetchCurrentUser()
      profile.value = buildProfile(currentUser)
    } catch (error) {
      console.warn('Failed to restore user session', error)
      logout()
    } finally {
      isBootstrapping.value = false
    }
  }

  function applyAuthPayload(accessToken: string, currentUser: CurrentUser) {
    token.value = accessToken
    profile.value = buildProfile(currentUser)
  }

  function buildProfile(user: CurrentUser): UserProfile {
    return {
      id: user.id,
      username: user.username,
      avatar: user.avatar,
      avatarBg: user.avatarBg,
      email: user.email,
      role: user.role === 'ADMIN' ? '管理员' : '徒步爱好者',
      joinDate: '2026年3月',
      postCount: 0,
      savedCount: 0,
      bio: user.role === 'ADMIN' ? '负责内容与用户管理。' : '热爱自然，探索未知。',
      location: '未知地点',
    }
  }

  function getErrorMessage(error: unknown) {
    if (error instanceof ApiError) {
      return error.message
    }
    if (error instanceof Error) {
      return error.message
    }
    return '请求失败，请稍后重试'
  }

  return {
    profile,
    token,
    isLoggedIn,
    isBootstrapping,
    showAuthModal,
    updateProfile,
    login,
    register,
    logout,
    requireAuth,
    bootstrap,
  }
})
