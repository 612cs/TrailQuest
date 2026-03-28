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

export interface TrailWeatherForecastDay {
  date: string
  week: string
  dayWeather: string
  nightWeather: string
  dayTemp: number
  nightTemp: number
}

interface ResolveWeatherOptions {
  adcode: string
  signal?: AbortSignal
}

const amapKey = import.meta.env.VITE_AMAP_WEATHER_KEY as string | undefined

export function useTrailWeather() {
  const weather = shallowRef<TrailWeather | null>(null)
  const forecast = shallowRef<TrailWeatherForecastDay[]>([])
  const isLoading = shallowRef(false)
  const error = shallowRef<string | null>(null)

  function getNextDay(dateString: string): string {
    const d = new Date(dateString)
    d.setDate(d.getDate() + 1)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  function getNextWeek(week: string): string {
    const w = parseInt(week, 10)
    return String(w === 7 ? 1 : w + 1)
  }
  async function resolve(options: ResolveWeatherOptions) {
    isLoading.value = true
    error.value = null
    weather.value = null
    forecast.value = []

    try {
      if (!amapKey || !options.adcode) {
        // Provide mock data if no key or no adcode is present
        const mockWeather: TrailWeather = {
          city: '模拟城市',
          weather: '多云',
          temperature: '22',
          humidity: '45',
          windDirection: '东南',
          windPower: '3',
          reportTime: new Date().toISOString(),
        }
        weather.value = mockWeather

        const mockForecast: TrailWeatherForecastDay[] = []
        const today = new Date().toISOString().split('T')[0]!
        let currentDayLabel = today
        let currentWeek = '1'

        for (let i = 0; i < 7; i++) {
          mockForecast.push({
            date: currentDayLabel,
            week: currentWeek,
            dayWeather: i % 2 === 0 ? '晴' : '多云',
            nightWeather: '阴',
            dayTemp: 20 + i,
            nightTemp: 12 + i,
          })
          currentDayLabel = getNextDay(currentDayLabel)
          currentWeek = getNextWeek(currentWeek)
        }
        forecast.value = mockForecast
        return { weather: mockWeather, forecast: mockForecast }
      }

      const urlBase = `https://restapi.amap.com/v3/weather/weatherInfo?key=${amapKey}&city=${encodeURIComponent(options.adcode)}&extensions=base`
      const urlAll = `https://restapi.amap.com/v3/weather/weatherInfo?key=${amapKey}&city=${encodeURIComponent(options.adcode)}&extensions=all`

      const [resBase, resAll] = await Promise.all([
        fetch(urlBase, { signal: options.signal }),
        fetch(urlAll, { signal: options.signal })
      ])

      const dataBase = await resBase.json()
      const dataAll = await resAll.json()

      if (dataBase?.status !== '1' || !dataBase.lives?.length) {
        error.value = '天气实况查询失败'
        weather.value = null
        forecast.value = []
        return null
      }

      const live = dataBase.lives[0]
      const resultWeather: TrailWeather = {
        city: live.city,
        weather: live.weather,
        temperature: live.temperature,
        humidity: live.humidity,
        windDirection: live.winddirection,
        windPower: live.windpower,
        reportTime: live.reporttime,
      }
      weather.value = resultWeather

      const resultForecast: TrailWeatherForecastDay[] = []
      if (dataAll?.status === '1' && dataAll.forecasts?.length && dataAll.forecasts[0].casts) {
        const casts = dataAll.forecasts[0].casts
        for (const cast of casts) {
          resultForecast.push({
            date: cast.date,
            week: cast.week,
            dayWeather: cast.dayweather,
            nightWeather: cast.nightweather,
            dayTemp: Number(cast.daytemp),
            nightTemp: Number(cast.nighttemp),
          })
        }

        // Mock additional days to reach exactly 7 days
        while (resultForecast.length > 0 && resultForecast.length < 7) {
          const lastDay = resultForecast[resultForecast.length - 1]!
          const mockTempOffset = (Math.random() > 0.5 ? 1 : -1) * Math.floor(Math.random() * 3)
          resultForecast.push({
            date: getNextDay(lastDay.date),
            week: getNextWeek(lastDay.week),
            dayWeather: lastDay.dayWeather, // keep same weather type for simplicity
            nightWeather: lastDay.nightWeather,
            dayTemp: lastDay.dayTemp + mockTempOffset,
            nightTemp: lastDay.nightTemp + mockTempOffset,
          })
        }
      }

      forecast.value = resultForecast

      return { weather: resultWeather, forecast: resultForecast }
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return null
      }
      error.value = '天气查询异常'
      weather.value = null
      forecast.value = []
      return null
    } finally {
      isLoading.value = false
    }
  }

  return {
    weather,
    forecast,
    isLoading,
    error,
    resolve,
  }
}
