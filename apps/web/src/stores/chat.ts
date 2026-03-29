import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { buildAiWebSocketUrl, createAiConversation, deleteAiConversation, fetchAiConversations, fetchAiMessages, getAiAccessToken, parseAiSocketMessage } from '../api/ai'
import type { AiConversationSummary, AiMessage, AiTrailCard, AiFollowUp, AiStreamEvent } from '../types/ai'
import { ApiError } from '../types/api'

function createLocalId(prefix: string) {
  return prefix + '_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
}

function nowIso() {
  return new Date().toISOString()
}

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<AiConversationSummary[]>([])
  const activeConversationId = ref('')
  const messages = ref<AiMessage[]>([])
  const isBootstrapping = ref(false)
  const isStreaming = ref(false)
  const streamError = ref('')
  const pendingUserMessage = ref('')
  const socket = ref<WebSocket | null>(null)
  const socketReadyPromise = ref<Promise<void> | null>(null)
  const activeAssistantMessageId = ref('')

  const hasMessages = computed(() => messages.value.length > 0)
  const activeConversation = computed(() => (
    conversations.value.find((item) => String(item.id) === activeConversationId.value) ?? null
  ))

  async function bootstrap() {
    if (isBootstrapping.value) {
      return
    }
    isBootstrapping.value = true
    streamError.value = ''
    try {
      conversations.value = await fetchAiConversations()
      const firstConversation = conversations.value[0]
      if (firstConversation) {
        await selectConversation(firstConversation.id)
      } else {
        activeConversationId.value = ''
        messages.value = []
      }
    } finally {
      isBootstrapping.value = false
    }
  }

  async function createConversation(title?: string) {
    const conversation = await createAiConversation(title)
    conversations.value = [conversation, ...conversations.value.filter((item) => String(item.id) !== String(conversation.id))]
    activeConversationId.value = String(conversation.id)
    messages.value = []
    return conversation
  }

  async function selectConversation(conversationId: string | number) {
    activeConversationId.value = String(conversationId)
    messages.value = await fetchAiMessages(conversationId)
    streamError.value = ''
  }

  async function removeConversation(conversationId: string | number) {
    await deleteAiConversation(conversationId)
    conversations.value = conversations.value.filter((item) => String(item.id) !== String(conversationId))

    if (activeConversationId.value === String(conversationId)) {
      const nextConversation = conversations.value[0]
      if (nextConversation) {
        await selectConversation(nextConversation.id)
      } else {
        activeConversationId.value = ''
        messages.value = []
      }
    }
  }

  function reset() {
    socket.value?.close()
    socket.value = null
    socketReadyPromise.value = null
    activeAssistantMessageId.value = ''
    conversations.value = []
    activeConversationId.value = ''
    messages.value = []
    isBootstrapping.value = false
    isStreaming.value = false
    streamError.value = ''
    pendingUserMessage.value = ''
  }

  async function sendMessage(text: string, context?: { currentCity?: string | null; currentTrailId?: string | number | null }) {
    const trimmed = text.trim()
    if (!trimmed || isStreaming.value) {
      return
    }

    streamError.value = ''
    pendingUserMessage.value = trimmed
    isStreaming.value = true

    let conversationId = activeConversationId.value
    if (!conversationId) {
      const conversation = await createConversation(trimmed.length <= 18 ? trimmed : trimmed.slice(0, 18) + '...')
      conversationId = String(conversation.id)
    }

    const userMessage: AiMessage = {
      id: createLocalId('user'),
      role: 'user',
      content: trimmed,
      createdAt: nowIso(),
      trailCards: [],
      followUps: [],
    }

    const assistantMessage: AiMessage = {
      id: createLocalId('assistant'),
      role: 'assistant',
      content: '',
      createdAt: nowIso(),
      trailCards: [],
      followUps: [],
      isStreaming: true,
    }

    messages.value = [...messages.value, userMessage, assistantMessage]
    activeAssistantMessageId.value = assistantMessage.id

    try {
      await ensureSocketReady()
      socket.value?.send(JSON.stringify({
        type: 'chat.send',
        conversationId,
        message: trimmed,
        context: {
          currentCity: context?.currentCity ?? null,
          currentTrailId: context?.currentTrailId ?? null,
          useUserPreference: true,
        },
      }))
      await waitForStreamComplete(assistantMessage.id)
      try {
        await refreshActiveConversation()
      } catch (error) {
        console.warn('Failed to refresh AI conversation after stream completion', error)
      }
    } catch (error) {
      const currentAssistant = messages.value.find((item) => item.id === assistantMessage.id)
      const hasAssistantContent = Boolean(currentAssistant?.content?.trim())
      const message = error instanceof ApiError ? error.message : 'AI 对话暂时不可用，请稍后重试'
      if (!hasAssistantContent) {
        streamError.value = message
      }
      updateAssistantMessage(assistantMessage.id, (current) => ({
        ...current,
        content: current.content || message,
        isStreaming: false,
      }))
      throw error
    } finally {
      pendingUserMessage.value = ''
      isStreaming.value = false
      activeAssistantMessageId.value = ''
    }
  }

  function handleStreamEvent(event: AiStreamEvent, assistantMessageId: string) {
    switch (event.event) {
      case 'auth.ok':
        break
      case 'session':
        activeConversationId.value = String(event.data.conversationId)
        break
      case 'delta':
        updateAssistantMessage(assistantMessageId, (current) => ({
          ...current,
          content: current.content + event.data.content,
        }))
        break
      case 'trail_cards':
        updateAssistantMessage(assistantMessageId, (current) => ({
          ...current,
          trailCards: event.data as AiTrailCard[],
        }))
        break
      case 'follow_ups':
        updateAssistantMessage(assistantMessageId, (current) => ({
          ...current,
          followUps: event.data as AiFollowUp[],
        }))
        break
      case 'done':
        updateAssistantMessage(assistantMessageId, (current) => ({
          ...current,
          isStreaming: false,
        }))
        break
      case 'error':
        streamError.value = event.data.message
        updateAssistantMessage(assistantMessageId, (current) => ({
          ...current,
          content: current.content || event.data.message,
          isStreaming: false,
        }))
        break
    }
  }

  function updateAssistantMessage(messageId: string, updater: (message: AiMessage) => AiMessage) {
    messages.value = messages.value.map((message) => (
      message.id === messageId ? updater(message) : message
    ))
  }

  function failActiveStream(message: string) {
    if (!activeAssistantMessageId.value) {
      return
    }
    streamError.value = message
    updateAssistantMessage(activeAssistantMessageId.value, (current) => ({
      ...current,
      content: current.content || message,
      isStreaming: false,
    }))
  }

  async function ensureSocketReady() {
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
      return
    }
    if (socketReadyPromise.value) {
      return socketReadyPromise.value
    }

    const token = getAiAccessToken()
    if (!token) {
      throw new ApiError('请先登录后再使用 AI 对话', 'UNAUTHORIZED', 401)
    }

    socketReadyPromise.value = new Promise<void>((resolve, reject) => {
      const ws = new WebSocket(buildAiWebSocketUrl())
      socket.value = ws
      let authed = false

      ws.onopen = () => {
        ws.send(JSON.stringify({ type: 'auth', token }))
      }

      ws.onmessage = (event) => {
        try {
          const parsed = parseAiSocketMessage(String(event.data))
          if (!parsed) {
            return
          }
          if (parsed.event === 'auth.ok') {
            authed = true
            resolve()
            return
          }
          if (parsed.event === 'error' && !authed) {
            reject(new ApiError(parsed.data.message, parsed.data.code, 401))
            return
          }
          if (activeAssistantMessageId.value) {
            handleStreamEvent(parsed, activeAssistantMessageId.value)
          }
        } catch (error) {
          reject(error instanceof Error ? error : new Error('AI WebSocket 消息解析失败'))
        }
      }

      ws.onerror = () => {
        if (!authed) {
          reject(new ApiError('AI 连接建立失败，请稍后重试', 'AI_SOCKET_CONNECT_FAILED', 500))
          return
        }
        failActiveStream('AI 连接中断，请重新发送这条消息')
      }

      ws.onclose = () => {
        socket.value = null
        socketReadyPromise.value = null
        if (!authed) {
          reject(new ApiError('AI 连接已关闭，请重新尝试', 'AI_SOCKET_CLOSED', 500))
          return
        }
        if (isStreaming.value) {
          failActiveStream('AI 连接已断开，请重新发送这条消息')
        }
      }
    })

    try {
      await socketReadyPromise.value
    } finally {
      socketReadyPromise.value = null
    }
  }

  function waitForStreamComplete(assistantMessageId: string) {
    return new Promise<void>((resolve, reject) => {
      const startedAt = Date.now()
      const stop = window.setInterval(() => {
        const assistant = messages.value.find((item) => item.id === assistantMessageId)
        if (!assistant) {
          window.clearInterval(stop)
          resolve()
          return
        }
        if (streamError.value && !assistant.isStreaming) {
          window.clearInterval(stop)
          reject(new ApiError(streamError.value, 'AI_STREAM_FAILED', 500))
          return
        }
        if (!assistant.isStreaming) {
          window.clearInterval(stop)
          resolve()
          return
        }
        if (Date.now() - startedAt > 90000) {
          window.clearInterval(stop)
          failActiveStream('AI 响应超时，请稍后重试')
          reject(new ApiError('AI 响应超时，请稍后重试', 'AI_STREAM_TIMEOUT', 504))
        }
      }, 60)
    })
  }

  async function refreshActiveConversation() {
    if (!activeConversationId.value) {
      return
    }
    messages.value = await fetchAiMessages(activeConversationId.value)
    conversations.value = await fetchAiConversations()
  }

  return {
    conversations,
    activeConversationId,
    activeConversation,
    messages,
    hasMessages,
    isBootstrapping,
    isStreaming,
    streamError,
    pendingUserMessage,
    bootstrap,
    createConversation,
    selectConversation,
    removeConversation,
    sendMessage,
    refreshActiveConversation,
    reset,
  }
})
