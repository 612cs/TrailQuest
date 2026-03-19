import { http } from './http'
import type { EntityId } from '../types/id'
import type {
  CreateReviewPayload,
  CreateReviewResult,
  DeleteReviewResult,
  FetchTrailReviewsParams,
  ReviewPageResult,
  UserCard,
} from '../types/review'

export function fetchTrailReviews(trailId: EntityId, params: FetchTrailReviewsParams = {}) {
  return http.get<ReviewPageResult>(`/api/trails/${trailId}/reviews`, { params })
}

export function createReview(payload: CreateReviewPayload) {
  return http.post<CreateReviewResult>('/api/reviews', payload)
}

export function deleteReview(reviewId: EntityId) {
  return http.delete<DeleteReviewResult>(`/api/reviews/${reviewId}`)
}

export function fetchUserCard(userId: EntityId) {
  return http.get<UserCard>(`/api/users/${userId}/card`)
}
