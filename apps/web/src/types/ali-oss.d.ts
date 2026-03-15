declare module 'ali-oss' {
  interface MultipartUploadOptions {
    progress?: (percentage: number) => void | Promise<void>
    mime?: string
  }

  interface OSSClientOptions {
    region: string
    endpoint?: string
    bucket: string
    accessKeyId: string
    accessKeySecret: string
    stsToken?: string
    secure?: boolean
  }

  interface UploadResult {
    name: string
    url?: string
    res?: unknown
  }

  export default class OSS {
    constructor(options: OSSClientOptions)
    multipartUpload(name: string, file: Blob | File, options?: MultipartUploadOptions): Promise<UploadResult>
  }
}
