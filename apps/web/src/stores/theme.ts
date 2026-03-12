import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
    const isDark = ref(false)

    function init() {
        const saved = localStorage.getItem('theme')
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
        localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
        applyTheme()
    }

    function applyTheme() {
        if (isDark.value) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }

    watch(isDark, applyTheme)

    return { isDark, init, toggle }
})
