import type { EntityId } from './id'

export interface TrailAuthor {
  id: EntityId
  username: string
  avatar: string
  avatarBg: string
}

export interface TrailTrackDetail {
  hasTrack: boolean
  sourceFormat?: 'gpx' | 'kml' | string
  originalFileName?: string | null
  downloadUrl?: string | null
  geoJson?: unknown
  bounds?: {
    minLng: number
    minLat: number
    maxLng: number
    maxLat: number
  } | null
  startPoint?: {
    lng: number
    lat: number
  } | null
  endPoint?: {
    lng: number
    lat: number
  } | null
  distanceMeters?: number | null
  elevationGainMeters?: number | null
  elevationLossMeters?: number | null
  durationSeconds?: number | null
}

export interface TrailInteractionState {
  trailId: EntityId
  likes: number
  favorites: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
}

export interface TrailInteractionResult extends TrailInteractionState {}

export interface TrailListItem {
  id: EntityId
  image: string
  name: string
  location: string
  ip: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  rating: number
  reviewCount: number
  distance: string
  elevation: string
  duration: string
  description: string
  tags: string[]
  favorites: number
  likes: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
  authorId: EntityId
  publishTime: string
  createdAt: string
  author: TrailAuthor
  track?: TrailTrackDetail | null
}

export interface TrailListParams {
  keyword?: string
  difficulty?: string
  packType?: string
  durationType?: string
  distance?: string
  sort?: 'latest' | 'hot' | 'rating'
  pageNum?: number
  pageSize?: number
}

export interface PageResponse<T> {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
  totalPages: number
}
