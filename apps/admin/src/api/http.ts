import { ADMIN_AUTH_TOKEN_STORAGE_KEY, API_BASE_URL } from './constants'
import type { ApiResponse } from '../types/api'
import { ApiError } from '../types/api'

interface RequestOptions extends Omit<RequestInit, 'body'> {
  params?: Record<string, string | number | boolean | null | undefined>
  body?: unknown
}

function buildUrl(path: string, params?: RequestOptions['params']) {
  const url = new URL(path, API_BASE_URL)
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })
  return url.toString()
}

async function request<T>(path: string, options: RequestOptions = {}) {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')

  if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }

  const token = localStorage.getItem(ADMIN_AUTH_TOKEN_STORAGE_KEY)
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(buildUrl(path, options.params), {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  const rawText = await response.text()
  let payload: ApiResponse<T> | null = null

  if (rawText) {
    try {
      payload = JSON.parse(rawText) as ApiResponse<T>
    } catch (error) {
      throw new ApiError('服务返回了非 JSON 响应', 'INVALID_RESPONSE', response.status)
    }
  }

  if (!response.ok) {
    throw new ApiError(payload?.message || response.statusText || '请求失败', payload?.code || 'HTTP_ERROR', response.status)
  }

  if (!payload) {
    throw new ApiError('服务返回了空响应', 'EMPTY_RESPONSE', response.status)
  }

  if (!payload.success) {
    throw new ApiError(payload.message || '请求失败', payload.code || 'REQUEST_FAILED', response.status)
  }

  return payload.data
}

export const http = {
  get<T>(path: string, options?: RequestOptions) {
    return request<T>(path, { ...options, method: 'GET' })
  },
  post<T>(path: string, body?: unknown, options?: RequestOptions) {
    return request<T>(path, { ...options, method: 'POST', body })
  },
  delete<T>(path: string, options?: RequestOptions) {
    return request<T>(path, { ...options, method: 'DELETE' })
  },
  put<T>(path: string, body?: unknown, options?: RequestOptions) {
    return request<T>(path, { ...options, method: 'PUT', body })
  },
}
