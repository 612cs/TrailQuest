import { http } from './http'
import type { PageResponse, TrailListItem, TrailListParams } from '../types/trail'

export function fetchTrails(params: TrailListParams = {}) {
  return http.get<PageResponse<TrailListItem>>('/api/trails', { params })
}

export function fetchTrailDetail(id: number) {
  return http.get<TrailListItem>(`/api/trails/${id}`)
}
