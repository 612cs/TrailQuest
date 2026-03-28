export interface LandscapePredictionCard {
  enabled: boolean
  experimental?: boolean
  score?: number | null
  level?: string | null
  bestWindow?: string | null
  summary?: string | null
  risks?: string[]
  confidence?: number | null
  resolvedFrom?: string | null
}

export interface LandscapeSunriseSunsetPrediction extends LandscapePredictionCard {
  sunriseTime?: string | null
  sunsetTime?: string | null
  sunriseScore?: number | null
  sunsetScore?: number | null
}

export interface LandscapeMilkyWayPrediction extends LandscapePredictionCard {
  visible?: boolean
}

export interface LandscapePredictionSource {
  provider: string
  weatherReady: boolean
  astroReady: boolean
  lightPollutionReady: boolean
}

export interface TrailLandscapePredictionResponse {
  sunriseSunset: LandscapeSunriseSunsetPrediction
  milkyWay: LandscapeMilkyWayPrediction
  cloudSea: LandscapePredictionCard
  rime: LandscapePredictionCard
  icicle: LandscapePredictionCard
  source: LandscapePredictionSource
}
