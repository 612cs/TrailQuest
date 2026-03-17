import { http } from './http'
import type { PageResponse, TrailInteractionResult, TrailListItem, TrailListParams } from '../types/trail'

export function fetchTrails(params: TrailListParams = {}) {
  return http.get<PageResponse<TrailListItem>>('/api/trails', { params })
}

export function fetchTrailDetail(id: number) {
  return http.get<TrailListItem>(`/api/trails/${id}`)
}

export function likeTrail(id: number) {
  return http.post<TrailInteractionResult>(`/api/trails/${id}/like`)
}

export function unlikeTrail(id: number) {
  return http.delete<TrailInteractionResult>(`/api/trails/${id}/like`)
}

export function favoriteTrail(id: number) {
  return http.post<TrailInteractionResult>(`/api/trails/${id}/favorite`)
}

export function unfavoriteTrail(id: number) {
  return http.delete<TrailInteractionResult>(`/api/trails/${id}/favorite`)
}
