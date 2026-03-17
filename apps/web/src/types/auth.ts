export interface CurrentUser {
  id: string
  username: string
  avatar: string
  avatarBg: string
  avatarMediaId?: string | null
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
