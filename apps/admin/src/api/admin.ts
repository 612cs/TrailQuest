import { http } from './http'
import type {
  AdminDashboardSummary,
  AdminHomeHeroSetting,
  AdminReportListItem,
  AdminReportPageQuery,
  AdminReviewListItem,
  AdminReviewPageQuery,
  AdminRejectTrailRequest,
  AdminTrailDetail,
  AdminTrailListItem,
  AdminTrailPageQuery,
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

export function fetchAdminUsers(query: AdminUserPageQuery = {}) {
  return http.get<PageResponse<AdminUserListItem>>('/api/admin/users', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function approveTrail(id: string | number) {
  return http.post<void>(`/api/admin/trails/${id}/approve`)
}

export function rejectTrail(id: string | number, payload: AdminRejectTrailRequest) {
  return http.post<void>(`/api/admin/trails/${id}/reject`, payload)
}

export function fetchAdminReviews(query: AdminReviewPageQuery = {}) {
  return http.get<PageResponse<AdminReviewListItem>>('/api/admin/reviews', {
    params: cleanQuery({ pageNum: 1, pageSize: 10, ...query }),
  })
}

export function deleteAdminReview(id: string | number) {
  return http.delete<void>(`/api/admin/reviews/${id}`)
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
