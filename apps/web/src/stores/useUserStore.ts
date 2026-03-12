import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

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

import { mockUsers } from '../mock/mockData'

export const useUserStore = defineStore('user', () => {
    // We can keep a default fallback, or just construct it dynamically after login.
    // For convenience, we define a helper to construct a profile from a MockUser.
    const createProfileFromUser = (user: any): UserProfile => ({
        id: user.id,
        username: user.username,
        avatar: user.avatar,
        avatarBg: user.avatarBg,
        email: user.email,
        role: '徒步爱好者',
        joinDate: '2023年1月',
        postCount: Math.floor(Math.random() * 30),
        savedCount: Math.floor(Math.random() * 100),
        bio: '热爱自然，探索未知。',
        location: '未知地点'
    })

    // Define state
    const profile = ref<UserProfile | null>(null)
    const showAuthModal = ref(false)

    // Initialize from localStorage
    const storedProfile = localStorage.getItem('trailquest_user_profile')
    if (storedProfile) {
        try {
            const parsed = JSON.parse(storedProfile)
            profile.value = parsed || null
        } catch (e) {
            console.error('Failed to parse user profile from local storage', e)
        }
    }

    // Watch for changes and save to localStorage
    watch(
        profile,
        (newProfile) => {
            if (newProfile) {
                localStorage.setItem('trailquest_user_profile', JSON.stringify(newProfile))
            } else {
                localStorage.removeItem('trailquest_user_profile')
            }
        },
        { deep: true }
    )

    // Getters
    const isLoggedIn = computed(() => !!profile.value)

    // Actions
    function updateProfile(updates: Partial<UserProfile>) {
        if (profile.value) {
            profile.value = { ...profile.value, ...updates }
        }
    }

    function login(email?: string, password?: string): { success: boolean, message?: string } {
        if (!email || !password) {
            return { success: false, message: '账号或密码不能为空' }
        }

        const matchedUser = mockUsers.find(u => u.email === email && u.password === password)
        if (matchedUser) {
            profile.value = createProfileFromUser(matchedUser)
            showAuthModal.value = false
            return { success: true }
        }

        return { success: false, message: '邮箱或密码不正确' }
    }

    function register(email: string, password: string, username: string): { success: boolean, message?: string } {
        // Simple mock registration
        const newUser = {
            id: Date.now(),
            username,
            avatar: username.slice(0, 2).toUpperCase(),
            avatarBg: 'var(--color-primary-500)',
            email,
            password
        }
        
        profile.value = createProfileFromUser(newUser)
        showAuthModal.value = false
        return { success: true }
    }

    function logout() {
        profile.value = null
    }

    /**
     * Intercepts an action requiring authentication.
     * If logged in, executes the action. 
     * If not logged in, shows the auth modal.
     */
    function requireAuth(action: () => void) {
        if (isLoggedIn.value) {
            action()
        } else {
            showAuthModal.value = true
        }
    }

    return {
        profile,
        isLoggedIn,
        showAuthModal,
        updateProfile,
        login,
        register,
        logout,
        requireAuth
    }
})
