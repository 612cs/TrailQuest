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
  status: 'active' | 'deleted' | string
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
  status: 'active' | 'deleted' | string
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

export interface AdminBanUserRequest {
  reason: string
}

export interface AdminTrailPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  reviewStatus?: string
  authorKeyword?: string
}

export interface AdminTrailManagementPageQuery {
  pageNum?: number
  pageSize?: number
  status?: string
  keyword?: string
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
  status: 'active' | 'banned' | 'deleted' | string
  location?: string | null
  avatar: string
  avatarBg: string
  avatarMediaUrl?: string | null
  bannedAt?: string | null
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

export interface AdminOptionGroup {
  groupCode: string
  groupName: string
  description?: string | null
  status: 'active' | 'inactive' | string
  itemCount: number
  allowCreate: boolean
}

export interface AdminOptionItem {
  id: string | number
  code: string
  label: string
  subLabel?: string | null
  description?: string | null
  icon?: string | null
  sort: number
  enabled: boolean
  builtin: boolean
  extra: Record<string, unknown>
}

export interface AdminCreateOptionItemRequest {
  itemCode: string
  itemLabel: string
  itemSubLabel?: string | null
  description?: string | null
  iconName?: string | null
  sortOrder: number
  enabled: boolean
  extra?: Record<string, unknown>
}

export interface AdminUpdateOptionItemRequest {
  itemLabel: string
  itemSubLabel?: string | null
  description?: string | null
  iconName?: string | null
  sortOrder: number
  enabled: boolean
  extra?: Record<string, unknown>
}
