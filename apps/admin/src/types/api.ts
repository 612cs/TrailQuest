export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
  totalPages: number
}

export class ApiError extends Error {
  code: string
  status: number

  constructor(message: string, code = 'HTTP_ERROR', status = 500) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}
