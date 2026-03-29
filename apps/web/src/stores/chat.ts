import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { createAiConversation, fetchAiConversations, fetchAiMessages, streamAiChat } from '../api/ai'
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
  const activeAbortController = ref<AbortController | null>(null)

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

  function reset() {
    activeAbortController.value?.abort()
    activeAbortController.value = null
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

    const abortController = new AbortController()
    activeAbortController.value = abortController

    try {
      await streamAiChat(
        {
          conversationId,
          message: trimmed,
          context: {
            currentCity: context?.currentCity ?? null,
            currentTrailId: context?.currentTrailId ?? null,
            useUserPreference: true,
          },
        },
        {
          signal: abortController.signal,
          onEvent: (event) => handleStreamEvent(event, assistantMessage.id),
        },
      )
      await refreshActiveConversation()
    } catch (error) {
      const message = error instanceof ApiError ? error.message : 'AI 对话暂时不可用，请稍后重试'
      streamError.value = message
      updateAssistantMessage(assistantMessage.id, (current) => ({
        ...current,
        content: current.content || message,
        isStreaming: false,
      }))
      throw error
    } finally {
      pendingUserMessage.value = ''
      isStreaming.value = false
      activeAbortController.value = null
    }
  }

  function handleStreamEvent(event: AiStreamEvent, assistantMessageId: string) {
    switch (event.event) {
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
    sendMessage,
    refreshActiveConversation,
    reset,
  }
})
