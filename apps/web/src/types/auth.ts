export interface CurrentUser {
  id: number
  username: string
  avatar: string
  avatarBg: string
  email?: string
  role: 'USER' | 'ADMIN'
}

export interface AuthPayload {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: CurrentUser
}
