import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export interface UserProfile {
    id: number
    username: string
    avatar: string
    avatarBg: string
    role: string
    joinDate: string
    postCount: number
    savedCount: number
    bio: string
    location: string
}

export const useUserStore = defineStore('user', () => {
    const defaultUser: UserProfile = {
        id: 1,
        username: '亚历克斯·汤普森',
        avatar: 'AT',
        avatarBg: 'linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700))',
        role: '管理员',
        joinDate: '2023年1月',
        postCount: 24,
        savedCount: 158,
        bio: '热爱自驾游与周末短途徒步，探索城市边缘的自然风光。',
        location: '未知地点'
    }

    // Define state
    const profile = ref<UserProfile>(defaultUser)

    // Initialize from localStorage
    const storedProfile = localStorage.getItem('trailquest_user_profile')
    if (storedProfile) {
        try {
            const parsed = JSON.parse(storedProfile)
            profile.value = { ...defaultUser, ...parsed }
        } catch (e) {
            console.error('Failed to parse user profile from local storage', e)
        }
    }

    // Watch for changes and save to localStorage
    watch(
        profile,
        (newProfile) => {
            localStorage.setItem('trailquest_user_profile', JSON.stringify(newProfile))
        },
        { deep: true }
    )

    // Actions
    function updateProfile(updates: Partial<UserProfile>) {
        profile.value = { ...profile.value, ...updates }
    }

    return {
        profile,
        updateProfile
    }
})
