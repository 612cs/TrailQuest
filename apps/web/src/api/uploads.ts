import { http } from './http'
import type { MediaFilePayload, UploadStsPayload } from '../types/upload'

interface CreateUploadStsRequest {
  bizType: 'avatar' | 'trail_cover' | 'trail_gallery' | 'review'
  fileName: string
  mimeType: string
}

interface CompleteUploadRequest {
  bizType: 'avatar' | 'trail_cover' | 'trail_gallery' | 'review'
  objectKey: string
  url: string
  originalName?: string
  extension?: string
  mimeType: string
  size: number
  width?: number
  height?: number
}

export function createUploadSts(payload: CreateUploadStsRequest) {
  return http.post<UploadStsPayload>('/api/uploads/sts', payload)
}

export function completeUpload(payload: CompleteUploadRequest) {
  return http.post<MediaFilePayload>('/api/uploads/complete', payload)
}
