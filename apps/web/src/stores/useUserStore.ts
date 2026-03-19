import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

import * as authApi from '../api/auth'
import { AUTH_TOKEN_STORAGE_KEY, USER_PROFILE_STORAGE_KEY } from '../api/constants'
import type { CurrentUser } from '../types/auth'
import type { HikingProfile } from '../types/hikingProfile'
import { ApiError } from '../types/api'

export interface UserProfile {
  id: string
  username: string
  avatar: string
  avatarBg: string
  avatarMediaId?: string | null
  avatarMediaUrl?: string | null
  email?: string
  role: string
  joinDate: string
  postCount: number
  savedCount: number
  bio: string
  location: string
  hikingProfile: HikingProfile | null
}

interface AuthActionResult {
  success: boolean
  message?: string
}

interface SaveProfilePayload {
  username: string
  bio?: string | null
  location?: string | null
  avatarMediaId?: string | null
}

interface RegisterPayload {
  email: string
  password: string
  username: string
  location?: string | null
  hikingProfile?: HikingProfile | null
}

interface SaveHikingProfilePayload {
  hikingProfile: HikingProfile
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

  async function register(payload: RegisterPayload): Promise<AuthActionResult> {
    try {
      const data = await authApi.register({
        email: payload.email,
        password: payload.password,
        username: payload.username,
        location: normalizeOptional(payload.location),
        hikingProfile: payload.hikingProfile ?? null,
      })
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

  async function saveProfile(updates: SaveProfilePayload): Promise<AuthActionResult> {
    if (!profile.value) {
      return { success: false, message: '请先登录后再编辑资料' }
    }

    try {
      const currentUser = await authApi.updateProfile({
        username: updates.username,
        bio: normalizeOptional(updates.bio),
        location: normalizeOptional(updates.location),
        avatarMediaId: updates.avatarMediaId ?? profile.value.avatarMediaId ?? null,
      })
      profile.value = buildProfile(currentUser)
      return { success: true }
    } catch (error) {
      return { success: false, message: getErrorMessage(error) }
    }
  }

  async function saveHikingProfile(payload: SaveHikingProfilePayload): Promise<AuthActionResult> {
    if (!profile.value) {
      return { success: false, message: '请先登录后再编辑徒步画像' }
    }

    try {
      const currentUser = await authApi.updateHikingProfile({
        hikingProfile: payload.hikingProfile,
      })
      profile.value = buildProfile(currentUser)
      return { success: true }
    } catch (error) {
      return { success: false, message: getErrorMessage(error) }
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
      avatarMediaId: user.avatarMediaId,
      avatarMediaUrl: user.avatarMediaUrl,
      email: user.email,
      role: user.role === 'ADMIN' ? '管理员' : '徒步爱好者',
      joinDate: '2026年3月',
      postCount: 0,
      savedCount: 0,
      bio: user.bio?.trim() || '',
      location: user.location?.trim() || '',
      hikingProfile: user.hikingProfile ?? null,
    }
  }

  function normalizeOptional(value?: string | null) {
    const trimmed = value?.trim()
    return trimmed ? trimmed : null
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
    login,
    register,
    logout,
    requireAuth,
    bootstrap,
    saveProfile,
    saveHikingProfile,
  }
})
