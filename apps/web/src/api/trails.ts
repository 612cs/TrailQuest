import { http } from './http'
import type { EntityId } from '../types/id'
import type { PageResponse, TrailInteractionResult, TrailListItem, TrailListParams } from '../types/trail'

export interface CreateTrailPayload {
  name: string
  location: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  distance?: string
  elevation?: string
  duration?: string
  description: string
  coverMediaId: string
  galleryMediaIds?: string[]
  trackMediaId?: string | null
  tags: string[]
}

export function fetchTrails(params: TrailListParams = {}) {
  return http.get<PageResponse<TrailListItem>>('/api/trails', { params })
}

export function fetchTrailDetail(id: EntityId) {
  return http.get<TrailListItem>(`/api/trails/${id}`)
}

export function likeTrail(id: EntityId) {
  return http.post<TrailInteractionResult>(`/api/trails/${id}/like`)
}

export function unlikeTrail(id: EntityId) {
  return http.delete<TrailInteractionResult>(`/api/trails/${id}/like`)
}

export function favoriteTrail(id: EntityId) {
  return http.post<TrailInteractionResult>(`/api/trails/${id}/favorite`)
}

export function unfavoriteTrail(id: EntityId) {
  return http.delete<TrailInteractionResult>(`/api/trails/${id}/favorite`)
}

export function createTrail(payload: CreateTrailPayload) {
  return http.post<TrailListItem>('/api/trails', payload)
}
