import { http } from './http'
import type { AuthPayload, CurrentUser } from '../types/auth'
import type { HikingProfile } from '../types/hikingProfile'
import type { PageResponse } from '../types/trail'
import type { UserTrailListItem } from '../types/profile'

interface LoginRequest {
  email: string
  password: string
}

interface RegisterRequest {
  username: string
  email: string
  password: string
  location?: string | null
  hikingProfile?: HikingProfile | null
}

export interface UpdateProfileRequest {
  username: string
  bio?: string | null
  location?: string | null
  avatarMediaId?: string | null
}

export interface UpdateHikingProfileRequest {
  hikingProfile: HikingProfile
}

export function login(payload: LoginRequest) {
  return http.post<AuthPayload>('/api/auth/login', payload)
}

export function register(payload: RegisterRequest) {
  return http.post<AuthPayload>('/api/auth/register', payload)
}

export function fetchCurrentUser() {
  return http.get<CurrentUser>('/api/auth/me')
}

export function updateProfile(payload: UpdateProfileRequest) {
  return http.put<CurrentUser>('/api/users/me/profile', payload)
}

export function updateHikingProfile(payload: UpdateHikingProfileRequest) {
  return http.put<CurrentUser>('/api/users/me/hiking-profile', payload)
}

export function fetchMyPublishedTrails(pageNum = 1, pageSize = 10) {
  return http.get<PageResponse<UserTrailListItem>>('/api/users/me/published-trails', {
    params: { pageNum, pageSize },
  })
}

export function fetchMyFavoriteTrails(pageNum = 1, pageSize = 10) {
  return http.get<PageResponse<UserTrailListItem>>('/api/users/me/favorite-trails', {
    params: { pageNum, pageSize },
  })
}
