import { computed, onBeforeUnmount, shallowRef } from 'vue'
import OSS from 'ali-oss'

import { completeUpload, createUploadSts } from '../api/uploads'
import { ApiError } from '../types/api'

type UploadBizType = 'avatar' | 'trail_cover' | 'trail_gallery' | 'review'

export interface UploadedImageItem {
  id: string
  localUrl: string
  remoteUrl: string
  progress: number
  status: 'uploading' | 'success' | 'error'
  errorMessage: string
}

interface UseOssImageUploaderOptions {
  bizType: UploadBizType
  max?: number
}

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

function buildItemId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export function useOssImageUploader(options: UseOssImageUploaderOptions) {
  const max = options.max ?? 9
  const items = shallowRef<UploadedImageItem[]>([])
  const isUploading = computed(() => items.value.some((item) => item.status === 'uploading'))
  const canAddMore = computed(() => items.value.length < max)
  const uploadedUrls = computed(() =>
    items.value
      .filter((item) => item.status === 'success' && item.remoteUrl)
      .map((item) => item.remoteUrl),
  )

  async function addFiles(files: FileList | File[]) {
    const fileList = Array.from(files)
    const available = Math.max(max - items.value.length, 0)
    const nextFiles = fileList.slice(0, available)
    for (const file of nextFiles) {
      await uploadFile(file)
    }
  }

  async function uploadFile(file: File) {
    const localUrl = URL.createObjectURL(file)
    const id = buildItemId()
    const draftItem: UploadedImageItem = {
      id,
      localUrl,
      remoteUrl: '',
      progress: 0,
      status: 'uploading',
      errorMessage: '',
    }
    items.value = [...items.value, draftItem]

    try {
      const sts = await createUploadSts({
        bizType: options.bizType,
        fileName: file.name,
        mimeType: file.type,
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
          patchItem(id, {
            progress: Math.round(percentage * 100),
          })
        },
        mime: file.type,
      })

      const { width, height } = await loadImageSize(file)
      const remote = await completeUpload({
        bizType: options.bizType,
        objectKey: sts.objectKey,
        url: `${sts.publicUrlBase}/${sts.objectKey}`,
        originalName: file.name,
        extension: getExtension(file),
        mimeType: file.type,
        size: file.size,
        width,
        height,
      })

      patchItem(id, {
        remoteUrl: remote.url,
        progress: 100,
        status: 'success',
        errorMessage: '',
      })
    } catch (error) {
      patchItem(id, {
        status: 'error',
        errorMessage: getErrorMessage(error),
      })
    }
  }

  function removeItem(id: string) {
    const target = items.value.find((item) => item.id === id)
    if (target?.localUrl.startsWith('blob:')) {
      URL.revokeObjectURL(target.localUrl)
    }
    items.value = items.value.filter((item) => item.id !== id)
  }

  function clear() {
    items.value.forEach((item) => {
      if (item.localUrl.startsWith('blob:')) {
        URL.revokeObjectURL(item.localUrl)
      }
    })
    items.value = []
  }

  function patchItem(id: string, patch: Partial<UploadedImageItem>) {
    items.value = items.value.map((item) => (item.id === id ? { ...item, ...patch } : item))
  }

  function getErrorMessage(error: unknown) {
    if (error instanceof ApiError) {
      return `${error.message}（${error.code}）`
    }
    if (error instanceof Error) {
      return error.message
    }
    return '上传失败，请稍后重试'
  }

  onBeforeUnmount(() => {
    clear()
  })

  return {
    items,
    isUploading,
    canAddMore,
    uploadedUrls,
    addFiles,
    removeItem,
    clear,
  }
}
