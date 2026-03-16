import { http } from './http'
import type { CreateReviewPayload, CreateReviewResult, ReviewItem } from '../types/review'

export function fetchTrailReviews(trailId: number) {
  return http.get<ReviewItem[]>(`/api/trails/${trailId}/reviews`)
}

export function createReview(payload: CreateReviewPayload) {
  return http.post<CreateReviewResult>('/api/reviews', payload)
}
