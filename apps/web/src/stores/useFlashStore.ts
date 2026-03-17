import { defineStore } from 'pinia'
import { ref } from 'vue'

type FlashType = 'success' | 'error'

export interface FlashMessage {
  id: number
  message: string
  type: FlashType
}

export const useFlashStore = defineStore('flash', () => {
  const message = ref<FlashMessage | null>(null)
  let timeoutId: number | null = null

  function show(messageText: string, type: FlashType = 'success', duration = 2200) {
    message.value = {
      id: Date.now(),
      message: messageText,
      type,
    }

    if (timeoutId !== null) {
      window.clearTimeout(timeoutId)
    }

    timeoutId = window.setTimeout(() => {
      message.value = null
      timeoutId = null
    }, duration)
  }

  function showSuccess(messageText: string, duration?: number) {
    show(messageText, 'success', duration)
  }

  function showError(messageText: string, duration = 2800) {
    show(messageText, 'error', duration)
  }

  function clear() {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId)
      timeoutId = null
    }
    message.value = null
  }

  return {
    message,
    show,
    showSuccess,
    showError,
    clear,
  }
})
