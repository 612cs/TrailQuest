export interface UserTrailListItem {
  id: number
  image: string
  name: string
  location: string
  authorId: number
  authorUsername: string
  publishTime: string
  createdAt: string
  favoritedByCurrentUser: boolean
  likedByCurrentUser: boolean
  favorites: number
  likes: number
}

export interface ProfileTrailFeedState {
  items: UserTrailListItem[]
  pageNum: number
  hasMore: boolean
  isLoading: boolean
  initialized: boolean
  errorMessage: string
  total: number
}
