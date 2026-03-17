import { http } from './http'
import type {
  CreateReviewPayload,
  CreateReviewResult,
  DeleteReviewResult,
  FetchTrailReviewsParams,
  ReviewPageResult,
  UserCard,
} from '../types/review'

export function fetchTrailReviews(trailId: number, params: FetchTrailReviewsParams = {}) {
  return http.get<ReviewPageResult>(`/api/trails/${trailId}/reviews`, { params })
}

export function createReview(payload: CreateReviewPayload) {
  return http.post<CreateReviewResult>('/api/reviews', payload)
}

export function deleteReview(reviewId: string) {
  return http.delete<DeleteReviewResult>(`/api/reviews/${reviewId}`)
}

export function fetchUserCard(userId: string) {
  return http.get<UserCard>(`/api/users/${userId}/card`)
}
