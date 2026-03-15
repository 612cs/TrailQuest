import { http } from './http'
import type { PageResponse, TrailListItem, TrailListParams } from '../types/trail'

export function fetchTrails(params: TrailListParams = {}) {
  return http.get<PageResponse<TrailListItem>>('/api/trails', { params })
}
