import OSS from 'ali-oss'

import { completeUpload, createUploadSts } from '../api/uploads'
import type { MediaFilePayload } from '../types/upload'
import { ApiError } from '../types/api'

type ImageBizType = 'trail_cover' | 'trail_gallery'

function getExtension(file: File) {
  const index = file.name.lastIndexOf('.')
  return index >= 0 ? file.name.slice(index + 1).toLowerCase() : ''
}

function loadImageSize(file: File) {
  return new Promise<{ width: number; height: number }>((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const image = new Image()

    image.onload = () => {
      resolve({ width: image.width, height: image.height })
      URL.revokeObjectURL(objectUrl)
    }

    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('无法读取图片尺寸'))
    }

    image.src = objectUrl
  })
}

function inferTrackMimeType(file: File) {
  if (file.type) {
    return file.type
  }
  const extension = getExtension(file)
  if (extension === 'gpx') {
    return 'application/gpx+xml'
  }
  if (extension === 'kml') {
    return 'application/vnd.google-earth.kml+xml'
  }
  return 'application/octet-stream'
}

function createOssClient(sts: Awaited<ReturnType<typeof createUploadSts>>) {
  return new OSS({
    region: sts.region,
    endpoint: `https://${sts.endpoint}`,
    bucket: sts.bucket,
    accessKeyId: sts.accessKeyId,
    accessKeySecret: sts.accessKeySecret,
    stsToken: sts.securityToken,
    secure: true,
  })
}

export async function uploadPublishImage(
  file: File,
  bizType: ImageBizType,
  onProgress?: (progress: number) => void,
) {
  const sts = await createUploadSts({
    bizType,
    fileName: file.name,
    mimeType: file.type,
  })

  const client = createOssClient(sts)

  await client.multipartUpload(sts.objectKey, file, {
    progress: (percentage: number) => {
      onProgress?.(Math.round(percentage * 100))
    },
    mime: file.type,
  })

  const { width, height } = await loadImageSize(file)
  return completeUpload({
    bizType,
    objectKey: sts.objectKey,
    url: `${sts.publicUrlBase}/${sts.objectKey}`,
    originalName: file.name,
    extension: getExtension(file),
    mimeType: file.type,
    size: file.size,
    width,
    height,
  })
}

export async function uploadPublishTrack(
  file: File,
  onProgress?: (progress: number) => void,
) {
  const mimeType = inferTrackMimeType(file)
  const extension = getExtension(file)
  const sts = await createUploadSts({
    bizType: 'trail_track',
    fileName: file.name,
    mimeType,
  })

  const client = createOssClient(sts)

  await client.multipartUpload(sts.objectKey, file, {
    progress: (percentage: number) => {
      onProgress?.(Math.round(percentage * 100))
    },
    mime: mimeType,
  })

  return completeUpload({
    bizType: 'trail_track',
    objectKey: sts.objectKey,
    url: `${sts.publicUrlBase}/${sts.objectKey}`,
    originalName: file.name,
    extension,
    mimeType,
    size: file.size,
  })
}

export function resolveUploadErrorMessage(error: unknown) {
  if (error instanceof ApiError) {
    return `${error.message}（${error.code}）`
  }
  if (error instanceof Error) {
    return error.message
  }
  return '上传失败，请稍后重试'
}

export function mediaIdOf(payload: MediaFilePayload | null | undefined) {
  return payload?.mediaId ?? null
}
