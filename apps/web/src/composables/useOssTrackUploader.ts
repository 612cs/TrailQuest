import { computed, onBeforeUnmount, shallowRef } from 'vue'
import OSS from 'ali-oss'

import { completeUpload, createUploadSts } from '../api/uploads'
import { ApiError } from '../types/api'

export interface UploadedTrackFile {
  id: string
  fileName: string
  localUrl: string
  remoteUrl: string
  mediaId: string | null
  mimeType: string
  extension: string
  progress: number
  status: 'idle' | 'uploading' | 'success' | 'error'
  errorMessage: string
}

interface ExistingTrackItem {
  mediaId: string | number | null
  fileName: string
  remoteUrl: string
  mimeType?: string
  extension?: string
}

function buildItemId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function getExtension(file: File) {
  const index = file.name.lastIndexOf('.')
  return index >= 0 ? file.name.slice(index + 1).toLowerCase() : ''
}

function inferMimeType(file: File) {
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

export function useOssTrackUploader() {
  const item = shallowRef<UploadedTrackFile | null>(null)
  const isUploading = computed(() => item.value?.status === 'uploading')

  async function setFile(file: File) {
    clear()

    const extension = getExtension(file)
    const mimeType = inferMimeType(file)
    const localUrl = URL.createObjectURL(file)
    const draftItem: UploadedTrackFile = {
      id: buildItemId(),
      fileName: file.name,
      localUrl,
      remoteUrl: '',
      mediaId: null,
      mimeType,
      extension,
      progress: 0,
      status: 'uploading',
      errorMessage: '',
    }

    item.value = draftItem

    try {
      const sts = await createUploadSts({
        bizType: 'trail_track',
        fileName: file.name,
        mimeType,
      })

      const client = new OSS({
        region: sts.region,
        endpoint: `https://${sts.endpoint}`,
        bucket: sts.bucket,
        accessKeyId: sts.accessKeyId,
        accessKeySecret: sts.accessKeySecret,
        stsToken: sts.securityToken,
        secure: true,
      })

      await client.multipartUpload(sts.objectKey, file, {
        progress: (percentage: number) => {
          if (!item.value) return
          item.value = {
            ...item.value,
            progress: Math.round(percentage * 100),
          }
        },
        mime: mimeType,
      })

      const remote = await completeUpload({
        bizType: 'trail_track',
        objectKey: sts.objectKey,
        url: `${sts.publicUrlBase}/${sts.objectKey}`,
        originalName: file.name,
        extension,
        mimeType,
        size: file.size,
      })

      item.value = {
        ...draftItem,
        progress: 100,
        mediaId: remote.mediaId,
        remoteUrl: remote.url,
        status: 'success',
      }
    } catch (error) {
      item.value = {
        ...draftItem,
        status: 'error',
        errorMessage: getErrorMessage(error),
      }
    }
  }

  function clear() {
    if (item.value?.localUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(item.value.localUrl)
    }
    item.value = null
  }

  function setExistingItem(existing: ExistingTrackItem | null) {
    clear()
    if (!existing) {
      return
    }

    item.value = {
      id: buildItemId(),
      fileName: existing.fileName,
      localUrl: existing.remoteUrl,
      remoteUrl: existing.remoteUrl,
      mediaId: existing.mediaId == null ? null : String(existing.mediaId),
      mimeType: existing.mimeType ?? inferMimeType(new File([], existing.fileName)),
      extension: existing.extension ?? getExtension(new File([], existing.fileName)),
      progress: 100,
      status: 'success',
      errorMessage: '',
    }
  }

  function getErrorMessage(error: unknown) {
    if (error instanceof ApiError) {
      return `${error.message}（${error.code}）`
    }
    if (error instanceof Error) {
      return error.message
    }
    return '轨迹上传失败，请稍后重试'
  }

  onBeforeUnmount(() => {
    clear()
  })

  return {
    item,
    isUploading,
    setFile,
    clear,
    setExistingItem,
  }
}
