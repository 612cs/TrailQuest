import { http } from './http'

export interface ReverseGeoPayload {
  lng: number
  lat: number
}

export interface ReverseGeoResult {
  lng: number
  lat: number
  country?: string | null
  province?: string | null
  city?: string | null
  district?: string | null
  formattedLocation: string
}

export function reverseGeo(payload: ReverseGeoPayload) {
  return http.post<ReverseGeoResult>('/api/geo/reverse', payload)
}
