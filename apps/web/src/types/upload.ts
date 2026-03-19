export interface UploadStsPayload {
  accessKeyId: string
  accessKeySecret: string
  securityToken: string
  region: string
  bucket: string
  endpoint: string
  publicUrlBase: string
  dir: string
  objectKey: string
  expireAt: number
}

export interface MediaFilePayload {
  mediaId: string
  url: string
  objectKey: string
  bizType: 'avatar' | 'trail_cover' | 'trail_gallery' | 'trail_track' | 'review'
}
