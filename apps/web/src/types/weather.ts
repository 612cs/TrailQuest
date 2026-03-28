export interface TrailWeatherLocationContext {
  lng: number
  lat: number
  resolvedFrom: 'start_coordinate' | 'location_text' | string
}

export interface TrailWeatherForecastDay {
  date: string
  fxDate: string
  week: string
  textDay: string | null
  textNight: string | null
  tempMax: number | null
  tempMin: number | null
  windDirDay: string | null
  windScaleDay: string | null
  humidity: number | null
  precip: number | null
}

export interface TrailWeatherSource {
  provider: string
  cached: boolean
}

export interface TrailWeatherResponse {
  locationContext: TrailWeatherLocationContext
  forecast: TrailWeatherForecastDay[]
  source: TrailWeatherSource
}
