import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import * as authApi from '../api/auth'
import { ADMIN_AUTH_TOKEN_STORAGE_KEY } from '../api/constants'
import { ApiError } from '../types/api'
import type { CurrentUser } from '../types/auth'

interface AuthResult {
  success: boolean
  message?: string
  code?: string
  noPermission?: boolean
}

function buildErrorResult(error: unknown): AuthResult {
  if (error instanceof ApiError) {
    const noPermission = error.status === 403 || error.code === 'FORBIDDEN' || error.code === 'ADMIN_ONLY'
    return {
      success: false,
      message: noPermission ? '当前账号没有后台权限' : error.message,
      code: error.code,
      noPermission,
    }
  }
  return { success: false, message: '登录失败，请稍后重试', code: 'LOGIN_FAILED' }
}

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const token = ref('')
  const user = ref<CurrentUser | null>(null)
  const bootstrapping = ref(false)
  const ready = ref(false)

  const storedToken = localStorage.getItem(ADMIN_AUTH_TOKEN_STORAGE_KEY)
  if (storedToken) {
    token.value = storedToken
  }

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function bootstrap(): Promise<AuthResult> {
    if (ready.value || bootstrapping.value) {
      return { success: true }
    }

    bootstrapping.value = true
    try {
      if (!token.value) {
        return { success: true }
      }
      const currentUser = await authApi.fetchCurrentUser()
      if (currentUser.role !== 'ADMIN') {
        clearAuth()
        return { success: false, message: '当前账号没有后台权限', code: 'ADMIN_ONLY', noPermission: true }
      }
      user.value = currentUser
      return { success: true }
    } catch (error) {
      clearAuth()
      return buildErrorResult(error)
    } finally {
      bootstrapping.value = false
      ready.value = true
    }
  }

  async function login(email: string, password: string): Promise<AuthResult> {
    try {
      const payload = await authApi.login({ email, password })
      setToken(payload.accessToken)
      if (payload.user.role !== 'ADMIN') {
        clearAuth()
        return { success: false, message: '当前账号没有后台权限', code: 'ADMIN_ONLY', noPermission: true }
      }
      const currentUser = await authApi.fetchCurrentUser()
      if (currentUser.role !== 'ADMIN') {
        clearAuth()
        return { success: false, message: '当前账号没有后台权限', code: 'ADMIN_ONLY', noPermission: true }
      }
      user.value = currentUser
      ready.value = true
      return { success: true }
    } catch (error) {
      clearAuth()
      return buildErrorResult(error)
    }
  }

  function logout() {
    clearAuth()
    ready.value = true
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(ADMIN_AUTH_TOKEN_STORAGE_KEY, newToken)
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem(ADMIN_AUTH_TOKEN_STORAGE_KEY)
  }

  return {
    token,
    user,
    ready,
    isLoggedIn,
    isAdmin,
    bootstrap,
    login,
    logout,
    clearAuth,
  }
})
