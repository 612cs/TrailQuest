import axios, { AxiosError, type AxiosRequestConfig } from 'axios'

import { API_BASE_URL, AUTH_TOKEN_STORAGE_KEY } from './constants'
import type { ApiResponse } from '../types/api'
import { ApiError } from '../types/api'

const httpClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const payload = error.response?.data
    return Promise.reject(
      new ApiError(
        payload?.message || error.message || '请求失败，请稍后重试',
        payload?.code || 'HTTP_ERROR',
        error.response?.status || 500,
      ),
    )
  },
)

function unwrap<T>(payload: ApiResponse<T> | null | undefined, status: number) {
  if (!payload) {
    throw new ApiError('服务返回了空响应', 'EMPTY_RESPONSE', status)
  }

  if (!payload.success) {
    throw new ApiError(payload.message || '请求失败', payload.code || 'REQUEST_FAILED', status)
  }

  return payload.data
}

export const http = {
  async get<T>(path: string, config?: AxiosRequestConfig) {
    const response = await httpClient.get<ApiResponse<T>>(path, config)
    return unwrap(response.data, response.status)
  },
  async post<T>(path: string, data?: unknown, config?: AxiosRequestConfig) {
    const response = await httpClient.post<ApiResponse<T>>(path, data, config)
    return unwrap(response.data, response.status)
  },
  async delete<T>(path: string, config?: AxiosRequestConfig) {
    const response = await httpClient.delete<ApiResponse<T>>(path, config)
    return unwrap(response.data, response.status)
  },
}
