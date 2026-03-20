import type { TrackViewerData, TrackViewerPath, TrackViewerPoint } from '../types/trackViewer'

function normalizeName(value?: string | null) {
  if (!value) return '未知'
  return value.replace(/\.[^.]+$/, '').trim() || '未知'
}

function isCoordinate(value: unknown): value is [number, number, number?] {
  return Array.isArray(value)
    && typeof value[0] === 'number'
    && typeof value[1] === 'number'
    && Number.isFinite(value[0])
    && Number.isFinite(value[1])
}

function toTrackPoint(value: [number, number, number?]): TrackViewerPoint {
  return {
    lng: value[0],
    lat: value[1],
    ele: typeof value[2] === 'number' && Number.isFinite(value[2]) ? value[2] : undefined,
  }
}

function extractPaths(geoJson: unknown): TrackViewerPath[] {
  const features = Array.isArray((geoJson as { features?: unknown[] } | null)?.features)
    ? (geoJson as { features: unknown[] }).features
    : []

  const paths: TrackViewerPath[] = []

  for (const feature of features) {
    const typedFeature = feature as {
      geometry?: { type?: string; coordinates?: unknown }
      properties?: Record<string, unknown>
    }
    const geometry = typedFeature.geometry
    if (!geometry) continue

    const pathName = typeof typedFeature.properties?.name === 'string' ? typedFeature.properties.name : undefined
    const pathColor = typeof typedFeature.properties?.stroke === 'string' ? typedFeature.properties.stroke : undefined

    if (geometry.type === 'LineString' && Array.isArray(geometry.coordinates)) {
      const points = geometry.coordinates.filter(isCoordinate).map(toTrackPoint)
      if (points.length >= 2) {
        paths.push({ name: pathName, color: pathColor, points })
      }
      continue
    }

    if (geometry.type === 'MultiLineString' && Array.isArray(geometry.coordinates)) {
      for (const line of geometry.coordinates) {
        if (!Array.isArray(line)) continue
        const points = line.filter(isCoordinate).map(toTrackPoint)
        if (points.length >= 2) {
          paths.push({ name: pathName, color: pathColor, points })
        }
      }
    }
  }

  return paths
}

function haversine(start: TrackViewerPoint, end: TrackViewerPoint) {
  const toRad = (value: number) => (value * Math.PI) / 180
  const lat1 = toRad(start.lat)
  const lat2 = toRad(end.lat)
  const deltaLat = lat2 - lat1
  const deltaLng = toRad(end.lng - start.lng)
  const a = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) ** 2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return 6371000 * c
}

function resolveDistanceMeters(paths: TrackViewerPath[], explicitDistanceMeters?: number | null) {
  if (typeof explicitDistanceMeters === 'number' && Number.isFinite(explicitDistanceMeters) && explicitDistanceMeters > 0) {
    return explicitDistanceMeters
  }

  return paths.reduce((total, path) => {
    if (path.points.length < 2) return total
    let pathDistance = 0
    for (let index = 1; index < path.points.length; index += 1) {
      const previous = path.points[index - 1]
      const current = path.points[index]
      if (!previous || !current) continue
      pathDistance += haversine(previous, current)
    }
    return total + pathDistance
  }, 0)
}

function resolveMainPathIndex(paths: TrackViewerPath[]) {
  let longestIndex = -1
  let longestLength = 0

  for (let index = 0; index < paths.length; index += 1) {
    const path = paths[index]
    const length = path?.points.length ?? 0
    if (length > longestLength) {
      longestLength = length
      longestIndex = index
    }
  }

  return longestIndex >= 0 ? longestIndex : 0
}

function resolveElevationGain(path: TrackViewerPath | undefined, explicitGain?: number | null) {
  if (typeof explicitGain === 'number' && Number.isFinite(explicitGain) && explicitGain >= 0) {
    return explicitGain
  }
  if (!path) return 0

  let gain = 0
  for (let index = 1; index < path.points.length; index += 1) {
    const previous = path.points[index - 1]
    const current = path.points[index]
    if (!previous || !current) continue
    if (typeof previous.ele !== 'number' || typeof current.ele !== 'number') continue
    if (current.ele > previous.ele) {
      gain += current.ele - previous.ele
    }
  }

  return gain
}

function resolveHighestPoint(path: TrackViewerPath | undefined) {
  if (!path?.points.length) return null

  let highestPoint = path.points[0] ?? null
  if (!highestPoint) return null

  for (const point of path.points) {
    const elevation = typeof point.ele === 'number' ? point.ele : 0
    const currentHighest = typeof highestPoint.ele === 'number' ? highestPoint.ele : 0
    if (elevation > currentHighest) {
      highestPoint = point
    }
  }

  return highestPoint
}

export function createTrackViewerData(input: {
  title?: string | null
  fileName?: string | null
  distanceMeters?: number | null
  elevationGainMeters?: number | null
  geoJson?: unknown
}): TrackViewerData | null {
  const paths = extractPaths(input.geoJson)
  if (!paths.length) {
    return null
  }

  const displayTitle = normalizeName(input.fileName ?? input.title)
  const mainPathIndex = resolveMainPathIndex(paths)
  const mainPath = paths[mainPathIndex]
  return {
    title: displayTitle,
    fileName: input.fileName ?? null,
    distanceMeters: resolveDistanceMeters(paths, input.distanceMeters),
    elevationGainMeters: resolveElevationGain(mainPath, input.elevationGainMeters),
    highestPoint: resolveHighestPoint(mainPath),
    mainPathIndex,
    paths,
  }
}
