import { http } from './http'
import type { AuthPayload, CurrentUser } from '../types/auth'

interface LoginRequest {
  email: string
  password: string
}

export function login(payload: LoginRequest) {
  return http.post<AuthPayload>('/api/auth/login', payload)
}

export function fetchCurrentUser() {
  return http.get<CurrentUser>('/api/auth/me')
}
