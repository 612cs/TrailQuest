export type TrackViewerMode = 'embedded' | 'detail' | 'fullscreen'
export type TrackWeatherScene = 'clear' | 'partly_cloudy' | 'overcast' | 'rain' | 'snow' | 'windy'

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
  elevationGainMeters?: number | null
  highestPoint?: TrackViewerPoint | null
  mainPathIndex?: number
  paths: TrackViewerPath[]
}
