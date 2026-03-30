export interface AdminDashboardSummary {
  pendingTrailCount: number
  reviewCount: number
  pendingReportCount: number
  userCount: number
}

export interface AdminTrailListItem {
  id: string | number
  image?: string | null
  name: string
  location?: string | null
  reviewStatus: 'pending' | 'approved' | 'rejected'
  authorUsername: string
  createdAt: string
}

export interface AdminTrailDetail {
  id: string | number
  image?: string | null
  name: string
  location?: string | null
  geoCountry?: string | null
  geoProvince?: string | null
  geoCity?: string | null
  geoDistrict?: string | null
  geoSource?: string | null
  difficulty?: string | null
  difficultyLabel?: string | null
  packType?: string | null
  durationType?: string | null
  distance?: string | null
  elevation?: string | null
  duration?: string | null
  description?: string | null
  tags: string[]
  favorites: number
  likes: number
  reviewCount: number
  reviewStatus: 'pending' | 'approved' | 'rejected'
  reviewRemark?: string | null
  reviewedBy?: string | number | null
  reviewedAt?: string | null
  createdAt: string
  author: {
    id: string | number
    username: string
    avatar: string
    avatarBg: string
  }
  gallery: Array<{ mediaId?: string | null; url: string }>
  track: {
    hasTrack: boolean
    originalFileName?: string | null
    sourceFormat?: string | null
    distanceMeters?: number | null
    elevationGainMeters?: number | null
    elevationLossMeters?: number | null
    durationSeconds?: number | null
  }
}

export interface AdminReviewListItem {
  id: string | number
  text: string
  authorUsername: string
  trailName: string
  createdAt: string
}

export interface AdminReportListItem {
  id: string | number
  targetType: string
  targetId: string
  status: string
  reason: string
  createdAt: string
}

export interface AdminRejectTrailRequest {
  remark: string
}

export interface AdminTrailPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  reviewStatus?: string
  authorKeyword?: string
}

export interface AdminReviewPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  trailKeyword?: string
  authorKeyword?: string
}

export interface AdminReportPageQuery {
  pageNum?: number
  pageSize?: number
}

export interface AdminUserListItem {
  id: string | number
  username: string
  email: string
  role: 'USER' | 'ADMIN' | string
  location?: string | null
  avatar: string
  avatarBg: string
  avatarMediaUrl?: string | null
  publishedTrailCount: number
  createdAt: string
}

export interface AdminUserPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  role?: string
}

export interface AdminHomeHeroSetting {
  imageUrl?: string | null
  usingDefault: boolean
  updatedAt?: string | null
}
