export {}

declare global {
  interface Window {
    _AMapSecurityConfig?: {
      securityJsCode: string
    }
    AMap?: typeof AMap
    AMapLoader?: {
      load: (options: {
        key: string
        version: string
        plugins?: string[]
      }) => Promise<typeof AMap>
    }
  }

  const AMap: any
}
