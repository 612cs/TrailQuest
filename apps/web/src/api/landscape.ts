import { http } from './http'
import type { EntityId } from '../types/id'
import type { TrailLandscapePredictionResponse } from '../types/landscape'

export function fetchTrailLandscapePrediction(id: EntityId, days = 7, signal?: AbortSignal) {
  return http.get<TrailLandscapePredictionResponse>(`/api/trails/${id}/landscape-prediction`, {
    params: { days },
    signal,
  })
}
