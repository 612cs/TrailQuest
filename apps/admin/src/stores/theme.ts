import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

import { ADMIN_THEME_STORAGE_KEY } from '../api/constants'

export const useThemeStore = defineStore('admin-theme', () => {
  const isDark = ref(false)

  function init() {
    const saved = localStorage.getItem(ADMIN_THEME_STORAGE_KEY)
    if (saved === 'dark') {
      isDark.value = true
    } else if (saved === 'light') {
      isDark.value = false
    } else {
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  function toggle() {
    isDark.value = !isDark.value
    localStorage.setItem(ADMIN_THEME_STORAGE_KEY, isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  function applyTheme() {
    document.documentElement.dataset.theme = isDark.value ? 'dark' : 'light'
  }

  watch(isDark, applyTheme)

  return {
    isDark,
    themeLabel: computed(() => (isDark.value ? '深色' : '浅色')),
    init,
    toggle,
  }
})
