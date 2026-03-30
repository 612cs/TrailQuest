export interface CurrentUser {
  id: string
  username: string
  avatar: string
  avatarBg: string
  avatarMediaId?: string | null
  avatarMediaUrl?: string | null
  bio?: string | null
  location?: string | null
  email?: string
  role: 'USER' | 'ADMIN'
  postCount: number
  savedCount: number
}

export interface AuthPayload {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: CurrentUser
}
