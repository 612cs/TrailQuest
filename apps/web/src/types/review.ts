import type { EntityId } from './id'

export interface ReviewAuthor {
  id: EntityId
  username: string
  avatar: string
  avatarBg: string
  avatarMediaUrl?: string | null
}

export interface UserCard {
  id: EntityId
  username: string
  avatar: string
  avatarBg: string
  avatarMediaUrl?: string | null
  role: 'USER' | 'ADMIN'
  joinDate: string
  postCount: number
  savedCount: number
  location: string
  bio: string
}

export interface ReviewItem {
  id: EntityId
  trailId: EntityId
  userId: EntityId
  parentId?: EntityId | null
  rating?: number | null
  time: string
  text: string
  images: string[]
  replies: ReviewItem[]
  replyTo?: EntityId | null
  author: ReviewAuthor
}

export interface CreateReviewPayload {
  trailId: EntityId
  parentId?: EntityId
  rating?: number
  text: string
  replyTo?: EntityId
  images?: string[]
}

export interface CreateReviewResult {
  review: ReviewItem
  trailRating: number
  trailReviewCount: number
}

export interface ReviewPageResult {
  list: ReviewItem[]
  nextCursor?: string | null
  hasMore: boolean
}

export interface FetchTrailReviewsParams {
  cursor?: string | null
  limit?: number
}

export interface DeleteReviewResult {
  deletedReviewId: EntityId
  trailRating: number
  trailReviewCount: number
}
