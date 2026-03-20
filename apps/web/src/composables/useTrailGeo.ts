import { shallowRef } from 'vue'

export interface TrailGeo {
  city: string
  adcode: string
  center: [number, number]
  source: 'ip' | 'geocode'
}

interface ResolveGeoOptions {
  ip?: string
  locationLabel: string
  signal?: AbortSignal
}

const amapKey = import.meta.env.VITE_AMAP_WEATHER_KEY as string | undefined

function parseRectangle(rectangle: string | undefined): [number, number] | null {
  if (!rectangle) return null
  const parts = rectangle.split(';')
  if (parts.length !== 2) return null

  const [startPoint, endPoint] = parts
  if (!startPoint || !endPoint) return null

  const startCoords = startPoint.split(',')
  const endCoords = endPoint.split(',')
  if (startCoords.length !== 2 || endCoords.length !== 2) return null

  const [startLng, startLat] = startCoords
  const [endLng, endLat] = endCoords
  if (!startLng || !startLat || !endLng || !endLat) return null

  const lng1 = Number(startLng)
  const lat1 = Number(startLat)
  const lng2 = Number(endLng)
  const lat2 = Number(endLat)

  if ([lng1, lat1, lng2, lat2].some((v) => Number.isNaN(v))) return null
  return [(lng1 + lng2) / 2, (lat1 + lat2) / 2]
}

async function fetchIpGeo(ip: string, signal?: AbortSignal): Promise<TrailGeo | null> {
  if (!amapKey) return null
  const url = `https://restapi.amap.com/v3/ip?key=${amapKey}&ip=${encodeURIComponent(ip)}`
  const res = await fetch(url, { signal })
  const data = await res.json()
  if (data?.status !== '1') return null
  const center = parseRectangle(data.rectangle)
  if (!center) return null
  const city = data.city && data.city.length > 0 ? data.city : data.province
  if (!city || !data.adcode) return null
  return {
    city,
    adcode: data.adcode,
    center,
    source: 'ip',
  }
}

async function fetchGeocode(locationLabel: string, signal?: AbortSignal): Promise<TrailGeo | null> {
  if (!amapKey) return null
  const url = `https://restapi.amap.com/v3/geocode/geo?key=${amapKey}&address=${encodeURIComponent(locationLabel)}`
  const res = await fetch(url, { signal })
  const data = await res.json()
  if (data?.status !== '1' || !data.geocodes?.length) return null
  const geo = data.geocodes[0]
  if (!geo?.location || !geo?.adcode) return null
  const [lng, lat] = geo.location.split(',').map(Number)
  if ([lng, lat].some((v) => Number.isNaN(v))) return null
  const city = geo.city?.length ? geo.city : geo.province
  if (!city) return null
  return {
    city,
    adcode: geo.adcode,
    center: [lng, lat],
    source: 'geocode',
  }
}

export function useTrailGeo() {
  const geo = shallowRef<TrailGeo | null>(null)
  const isLoading = shallowRef(false)
  const error = shallowRef<string | null>(null)

  async function resolve(options: ResolveGeoOptions) {
    if (!amapKey) {
      error.value = '未配置高德 Web 服务 Key'
      geo.value = null
      return null
    }

    isLoading.value = true
    error.value = null
    geo.value = null

    try {
      if (options.ip) {
        const ipGeo = await fetchIpGeo(options.ip, options.signal)
        if (ipGeo) {
          geo.value = ipGeo
          return ipGeo
        }
      }

      const geocodeGeo = await fetchGeocode(options.locationLabel, options.signal)
      if (geocodeGeo) {
        geo.value = geocodeGeo
        return geocodeGeo
      }

      error.value = '定位失败'
      geo.value = null
      return null
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return null
      }
      error.value = '定位异常'
      geo.value = null
      return null
    } finally {
      isLoading.value = false
    }
  }

  return {
    geo,
    isLoading,
    error,
    resolve,
  }
}
