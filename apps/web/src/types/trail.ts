export interface TrailAuthor {
  id: number
  username: string
  avatar: string
  avatarBg: string
}

export interface TrailListItem {
  id: number
  image: string
  name: string
  location: string
  ip: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  rating: number
  reviewCount: number
  distance: string
  elevation: string
  duration: string
  description: string
  tags: string[]
  favorites: number
  likes: number
  authorId: number
  publishTime: string
  createdAt: string
  author: TrailAuthor
}

export interface TrailListParams {
  keyword?: string
  difficulty?: string
  packType?: string
  durationType?: string
  distance?: string
  sort?: 'latest' | 'hot' | 'rating'
  pageNum?: number
  pageSize?: number
}

export interface PageResponse<T> {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
  totalPages: number
}
