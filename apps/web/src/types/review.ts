export interface ReviewAuthor {
  id: string
  username: string
  avatar: string
  avatarBg: string
  avatarMediaUrl?: string | null
}

export interface ReviewItem {
  id: string
  trailId: string
  userId: string
  parentId?: string | null
  rating?: number | null
  time: string
  text: string
  images: string[]
  replies: ReviewItem[]
  replyTo?: string | null
  author: ReviewAuthor
}

export interface CreateReviewPayload {
  trailId: number
  parentId?: string
  rating?: number
  text: string
  replyTo?: string
  images?: string[]
}

export interface CreateReviewResult {
  review: ReviewItem
  trailRating: number
  trailReviewCount: number
}
