export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const AUTH_TOKEN_STORAGE_KEY = 'trailquest_access_token'
export const USER_PROFILE_STORAGE_KEY = 'trailquest_user_profile'
