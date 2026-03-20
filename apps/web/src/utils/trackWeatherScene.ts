import type { TrackWeatherScene } from '../types/trackViewer'

export function mapWeatherToTrackScene(weatherLabel?: string | null, windPower?: string | number | null): TrackWeatherScene {
  const label = (weatherLabel ?? '').trim()
  const numericWindPower = typeof windPower === 'number'
    ? windPower
    : Number(String(windPower ?? '').match(/\d+/)?.[0] ?? '0')

  if (label.includes('雪') || label.includes('冰雹') || label.includes('冻雨')) {
    return 'snow'
  }

  if (label.includes('雨')) {
    return 'rain'
  }

  if (numericWindPower >= 5) {
    return 'windy'
  }

  if (label.includes('阴')) {
    return 'overcast'
  }

  if (label.includes('多云') || label.includes('少云') || label.includes('晴间多云') || label.includes('云')) {
    return 'partly_cloudy'
  }

  if (label.includes('晴')) {
    return 'clear'
  }

  return 'partly_cloudy'
}
