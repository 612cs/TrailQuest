import { http } from './http'
import type { EntityId } from '../types/id'
import type { TrailWeatherResponse } from '../types/weather'

export function fetchTrailWeather(id: EntityId, signal?: AbortSignal) {
  return http.get<TrailWeatherResponse>(`/api/trails/${id}/weather`, { signal })
}
