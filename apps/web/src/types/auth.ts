export interface CurrentUser {
  id: number
  username: string
  avatar: string
  avatarBg: string
  avatarMediaId?: number | null
  avatarMediaUrl?: string | null
  email?: string
  role: 'USER' | 'ADMIN'
}

export interface AuthPayload {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: CurrentUser
}
