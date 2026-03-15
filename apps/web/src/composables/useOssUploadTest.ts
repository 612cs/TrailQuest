import { computed, shallowRef } from 'vue'
import OSS from 'ali-oss'

import { completeUpload, createUploadSts } from '../api/uploads'
import type { MediaFilePayload } from '../types/upload'
import { ApiError } from '../types/api'

type UploadBizType = 'avatar' | 'trail_cover' | 'trail_gallery' | 'review'

interface UploadResult {
  localPreviewUrl: string
  remote: MediaFilePayload
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

export function useOssUploadTest() {
  const selectedFile = shallowRef<File | null>(null)
  const localPreviewUrl = shallowRef('')
  const uploadResult = shallowRef<UploadResult | null>(null)
  const isUploading = shallowRef(false)
  const uploadProgress = shallowRef(0)
  const errorMessage = shallowRef('')

  const canUpload = computed(() => !!selectedFile.value && !isUploading.value)

  function setFile(file: File | null) {
    uploadResult.value = null
    errorMessage.value = ''
    uploadProgress.value = 0

    if (localPreviewUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(localPreviewUrl.value)
    }

    selectedFile.value = file
    localPreviewUrl.value = file ? URL.createObjectURL(file) : ''
  }

  async function upload(bizType: UploadBizType) {
    if (!selectedFile.value) {
      throw new Error('请先选择一张图片')
    }

    const file = selectedFile.value
    isUploading.value = true
    uploadProgress.value = 0
    errorMessage.value = ''

    try {
      const sts = await createUploadSts({
        bizType,
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
          uploadProgress.value = Math.round(percentage * 100)
        },
        mime: file.type,
      })

      const { width, height } = await loadImageSize(file)
      const remote = await completeUpload({
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

      uploadResult.value = {
        localPreviewUrl: localPreviewUrl.value,
        remote,
      }
      uploadProgress.value = 100
      return remote
    } catch (error) {
      if (error instanceof ApiError) {
        errorMessage.value = `${error.message}（${error.code}）`
      } else if (error instanceof Error) {
        errorMessage.value = error.message
      } else {
        errorMessage.value = '上传失败，请稍后重试'
      }
      throw error
    } finally {
      isUploading.value = false
    }
  }

  return {
    selectedFile,
    localPreviewUrl,
    uploadResult,
    isUploading,
    uploadProgress,
    errorMessage,
    canUpload,
    setFile,
    upload,
  }
}
