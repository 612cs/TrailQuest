import { shallowRef } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const amapKey = import.meta.env.VITE_AMAP_WEB_KEY as string | undefined
const amapSecurityCode = import.meta.env.VITE_AMAP_WEB_SECURITY_CODE as string | undefined

let loaderPromise: Promise<any> | null = null

export function useAmapLoader() {
  const isReady = shallowRef(false)
  const error = shallowRef<string | null>(null)

  async function load() {
    if (window.AMap) {
      isReady.value = true
      return window.AMap
    }

    if (!amapKey) {
      error.value = '未配置高德地图 Key'
      return null
    }

    if (!loaderPromise) {
      // 设置安全密钥
      if (amapSecurityCode) {
        window._AMapSecurityConfig = { securityJsCode: amapSecurityCode }
      }

      loaderPromise = AMapLoader.load({
        key: amapKey,
        version: '2.0',
        plugins: ['AMap.GeoJSON'], // 注册 GeoJSON 等级插件
      }).then((AMap) => {
        window.AMap = AMap
        isReady.value = true
        return AMap
      }).catch((e) => {
        const err = new Error(e?.message || '高德地图脚本加载失败')
        loaderPromise = null
        throw err
      })
    }

    try {
      return await loaderPromise
    } catch (err) {
      error.value = (err as Error).message
      return null
    }
  }

  return {
    isReady,
    error,
    load,
  }
}
