import { API_BASE_URL, AUTH_TOKEN_STORAGE_KEY } from './constants'
import { http } from './http'
import { ApiError } from '../types/api'
import type { AiChatStreamPayload, AiConversationSummary, AiMessage, AiStreamEvent } from '../types/ai'

export function fetchAiConversations() {
  return http.get<AiConversationSummary[]>('/api/ai/conversations')
}

export function createAiConversation(title?: string) {
  return http.post<AiConversationSummary>('/api/ai/conversations', title ? { title } : {})
}

export function fetchAiMessages(conversationId: string | number) {
  return http.get<AiMessage[]>(`/api/ai/conversations/${conversationId}/messages`)
}

export async function streamAiChat(
  payload: AiChatStreamPayload,
  handlers: {
    onEvent: (event: AiStreamEvent) => void
    signal?: AbortSignal
  },
) {
  const token = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
  if (!token) {
    throw new ApiError('请先登录后再使用 AI 对话', 'UNAUTHORIZED', 401)
  }

  const response = await fetch(`${API_BASE_URL}/api/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
    signal: handlers.signal,
  })

  if (!response.ok) {
    const text = await response.text()
    try {
      const payload = JSON.parse(text) as { message?: string; code?: string }
      throw new ApiError(payload.message || 'AI 请求失败', payload.code || 'AI_REQUEST_FAILED', response.status)
    } catch {
      throw new ApiError(text || 'AI 请求失败', 'AI_REQUEST_FAILED', response.status)
    }
  }

  if (!response.body) {
    throw new ApiError('AI 返回了空流', 'EMPTY_STREAM', 500)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    buffer = buffer.replace(/\r\n/g, '\n')

    let boundaryIndex = buffer.indexOf('\n\n')
    while (boundaryIndex >= 0) {
      const rawEvent = buffer.slice(0, boundaryIndex)
      buffer = buffer.slice(boundaryIndex + 2)
      const parsed = parseEventBlock(rawEvent)
      if (parsed) {
        handlers.onEvent(parsed)
      }
      boundaryIndex = buffer.indexOf('\n\n')
    }
  }

  const finalText = decoder.decode()
  if (finalText) {
    buffer += finalText
  }
  buffer = buffer.replace(/\r\n/g, '\n')
  const parsed = parseEventBlock(buffer)
  if (parsed) {
    handlers.onEvent(parsed)
  }
}

function parseEventBlock(block: string): AiStreamEvent | null {
  if (!block.trim()) {
    return null
  }

  let eventName = ''
  const dataLines: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }

  if (!eventName || dataLines.length === 0) {
    return null
  }

  const data = JSON.parse(dataLines.join('\n'))
  return { event: eventName as AiStreamEvent['event'], data } as AiStreamEvent
}
