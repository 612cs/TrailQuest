import { shallowRef } from 'vue'

export interface TrailWeather {
  city: string
  weather: string
  temperature: string
  humidity: string
  windDirection: string
  windPower: string
  reportTime: string
}

interface ResolveWeatherOptions {
  adcode: string
  signal?: AbortSignal
}

const amapKey = import.meta.env.VITE_AMAP_WEATHER_KEY as string | undefined

export function useTrailWeather() {
  const weather = shallowRef<TrailWeather | null>(null)
  const isLoading = shallowRef(false)
  const error = shallowRef<string | null>(null)

  async function resolve(options: ResolveWeatherOptions) {
    if (!amapKey) {
      error.value = '未配置高德天气 Key'
      weather.value = null
      return null
    }

    isLoading.value = true
    error.value = null
    weather.value = null

    try {
      const url = `https://restapi.amap.com/v3/weather/weatherInfo?key=${amapKey}&city=${encodeURIComponent(options.adcode)}&extensions=base`
      const res = await fetch(url, { signal: options.signal })
      const data = await res.json()
      if (data?.status !== '1' || !data.lives?.length) {
        error.value = '天气查询失败'
        weather.value = null
        return null
      }

      const live = data.lives[0]
      const result: TrailWeather = {
        city: live.city,
        weather: live.weather,
        temperature: live.temperature,
        humidity: live.humidity,
        windDirection: live.winddirection,
        windPower: live.windpower,
        reportTime: live.reporttime,
      }
      weather.value = result
      return result
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return null
      }
      error.value = '天气查询异常'
      weather.value = null
      return null
    } finally {
      isLoading.value = false
    }
  }

  return {
    weather,
    isLoading,
    error,
    resolve,
  }
}
