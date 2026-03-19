export type TrackViewerMode = 'embedded' | 'detail' | 'fullscreen'

export interface TrackViewerPoint {
  lng: number
  lat: number
  ele?: number
}

export interface TrackViewerPath {
  name?: string
  color?: string
  points: TrackViewerPoint[]
}

export interface TrackViewerData {
  title: string
  fileName?: string | null
  distanceMeters?: number | null
  paths: TrackViewerPath[]
}
