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
  iconDay?: string | null
  iconNight?: string | null
  tempMax: number | null
  tempMin: number | null
  wind360Day?: number | null
  windDirDay: string | null
  windScaleDay: string | null
  windSpeedDay?: number | null
  wind360Night?: number | null
  windDirNight?: string | null
  windScaleNight?: string | null
  windSpeedNight?: number | null
  humidity: number | null
  pressure?: number | null
  cloud?: number | null
  uvIndex?: number | null
  vis?: number | null
  precip: number | null
  sunrise?: string | null
  sunset?: string | null
}

export interface TrailWeatherCurrent {
  obsTime: string | null
  text: string | null
  temp: number | null
  humidity: number | null
  windSpeed: number | null
  windScale: string | null
  windDir: string | null
  wind360: number | null
  pressure: number | null
  cloud: number | null
  dew: number | null
  vis: number | null
}

export interface TrailWeatherHourly {
  fxTime: string
  text: string | null
  temp: number | null
  humidity: number | null
  windSpeed: number | null
  windScale: string | null
  windDir: string | null
  wind360: number | null
  pop: number | null
  precip: number | null
  pressure: number | null
  cloud: number | null
  dew: number | null
  vis: number | null
}

export interface TrailWeatherAstro {
  sunrise: string | null
  sunset: string | null
  sunriseSolarElevation: number | null
  sunsetSolarElevation: number | null
  moonrise: string | null
  moonset: string | null
  moonPhase: string | null
  illumination: number | null
}

export interface TrailWeatherSource {
  provider: string
  cached: boolean
  dailyReady?: boolean
  currentReady?: boolean
  hourlyReady?: boolean
  astroReady?: boolean
  lightPollutionReady?: boolean
}

export interface TrailWeatherResponse {
  locationContext: TrailWeatherLocationContext
  forecast: TrailWeatherForecastDay[]
  current?: TrailWeatherCurrent | null
  hourly?: TrailWeatherHourly[]
  astro?: TrailWeatherAstro | null
  source: TrailWeatherSource
}
