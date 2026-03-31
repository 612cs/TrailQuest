import { http } from './http'
import type {
  AdminBanUserRequest,
  AdminDashboardSummary,
  AdminHomeHeroSetting,
  AdminCreateOptionItemRequest,
  AdminOptionGroup,
  AdminOptionItem,
  AdminReportListItem,
  AdminReportPageQuery,
  AdminReviewActionRequest,
  AdminReviewBatchActionRequest,
  AdminReviewDetail,
  AdminReviewListItem,
  AdminReviewPageQuery,
  AdminRejectTrailRequest,
  AdminTrailDetail,
  AdminTrailListItem,
  AdminTrailManagementPageQuery,
  AdminTrailPageQuery,
  AdminUpdateOptionItemRequest,
  AdminUserListItem,
  AdminUserPageQuery,
} from '../types/admin'
import type { PageResponse } from '../types/api'

type QueryValue = string | number | boolean | null | undefined

function cleanQuery(query: Record<string, QueryValue>) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Record<string, string | number | boolean>
}

export function fetchDashboardSummary() {
  return http.get<AdminDashboardSummary>('/api/admin/dashboard/summary')
}

export function fetchAdminTrails(query: AdminTrailPageQuery = {}) {
  return http.get<PageResponse<AdminTrailListItem>>('/api/admin/trails', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function fetchAdminTrailDetail(id: string | number) {
  return http.get<AdminTrailDetail>(`/api/admin/trails/${id}`)
}

export function fetchAdminTrailManagement(query: AdminTrailManagementPageQuery = {}) {
  return http.get<PageResponse<AdminTrailListItem>>('/api/admin/trail-management', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function fetchAdminTrailManagementDetail(id: string | number) {
  return http.get<AdminTrailDetail>(`/api/admin/trail-management/${id}`)
}

export function fetchAdminUsers(query: AdminUserPageQuery = {}) {
  return http.get<PageResponse<AdminUserListItem>>('/api/admin/users', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function banAdminUser(id: string | number, payload: AdminBanUserRequest) {
  return http.post<void>(`/api/admin/users/${id}/ban`, payload)
}

export function unbanAdminUser(id: string | number) {
  return http.post<void>(`/api/admin/users/${id}/unban`)
}

export function approveTrail(id: string | number) {
  return http.post<void>(`/api/admin/trails/${id}/approve`)
}

export function rejectTrail(id: string | number, payload: AdminRejectTrailRequest) {
  return http.post<void>(`/api/admin/trails/${id}/reject`, payload)
}

export function offlineTrail(id: string | number) {
  return http.post<void>(`/api/admin/trail-management/${id}/offline`)
}

export function restoreTrail(id: string | number) {
  return http.post<void>(`/api/admin/trail-management/${id}/restore`)
}

export function fetchAdminReviews(query: AdminReviewPageQuery = {}) {
  return http.get<PageResponse<AdminReviewListItem>>('/api/admin/reviews', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function fetchAdminReviewDetail(id: string | number) {
  return http.get<AdminReviewDetail>(`/api/admin/reviews/${id}`)
}

export function hideAdminReview(id: string | number, payload: AdminReviewActionRequest) {
  return http.post<void>(`/api/admin/reviews/${id}/hide`, payload)
}

export function restoreAdminReview(id: string | number) {
  return http.post<void>(`/api/admin/reviews/${id}/restore`)
}

export function deleteAdminReview(id: string | number, payload: AdminReviewActionRequest) {
  return http.post<void>(`/api/admin/reviews/${id}/delete`, payload)
}

export function batchHideAdminReviews(payload: AdminReviewBatchActionRequest) {
  return http.post<void>('/api/admin/reviews/batch-hide', payload)
}

export function batchRestoreAdminReviews(payload: AdminReviewBatchActionRequest) {
  return http.post<void>('/api/admin/reviews/batch-restore', payload)
}

export function fetchAdminReports(query: AdminReportPageQuery = {}) {
  return http.get<PageResponse<AdminReportListItem>>('/api/admin/reports', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function resolveReport(id: string | number) {
  return http.post<void>(`/api/admin/reports/${id}/resolve`)
}

export function fetchAdminHomeHeroSetting() {
  return http.get<AdminHomeHeroSetting>('/api/admin/settings/home-hero')
}

export function updateAdminHomeHeroSetting(payload: { imageUrl?: string | null }) {
  return http.post<void>('/api/admin/settings/home-hero', payload)
}

export function fetchAdminOptionGroups() {
  return http.get<AdminOptionGroup[]>('/api/admin/config/groups')
}

export function fetchAdminOptionGroupItems(groupCode: string) {
  return http.get<AdminOptionItem[]>(`/api/admin/config/groups/${groupCode}/items`)
}

export function createAdminOptionItem(groupCode: string, payload: AdminCreateOptionItemRequest) {
  return http.post<AdminOptionItem>(`/api/admin/config/groups/${groupCode}/items`, payload)
}

export function updateAdminOptionItem(groupCode: string, itemId: string | number, payload: AdminUpdateOptionItemRequest) {
  return http.put<AdminOptionItem>(`/api/admin/config/groups/${groupCode}/items/${itemId}`, payload)
}
