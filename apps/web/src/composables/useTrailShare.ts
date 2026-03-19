import { useFlashStore } from '../stores/useFlashStore'

interface TrailSharePayload {
  id: number | string
  name: string
  location: string
}

export function useTrailShare() {
  const flashStore = useFlashStore()

  async function shareTrail(payload: TrailSharePayload) {
    const shareUrl = new URL(`/trail/${payload.id}`, window.location.origin).toString()
    const shareText = payload.location ? `${payload.name} · ${payload.location}` : payload.name
    const canUseNativeShare = typeof navigator.share === 'function'

    try {
      if (canUseNativeShare) {
        await navigator.share({
          title: payload.name,
          text: shareText,
          url: shareUrl,
        })
      } else {
        await navigator.clipboard.writeText(shareUrl)
      }

      flashStore.showSuccess(canUseNativeShare ? '已打开系统分享' : '路线链接已复制')
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return
      }

      flashStore.showError('分享失败，请稍后重试')
    }
  }

  return {
    shareTrail,
  }
}
