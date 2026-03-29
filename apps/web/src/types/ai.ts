import type { EntityId } from './id'

export interface AiConversationSummary {
  id: EntityId
  title: string
  updatedAt: string
  preview: string
}

export interface AiTrailCard {
  id: EntityId
  image: string
  name: string
  location: string
  description: string
  difficulty: 'easy' | 'moderate' | 'hard'
  difficultyLabel: string
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  rating: number
  reviewCount: number
  reviews: string
  distance: string
  elevation: string
  duration: string
  likes: number
  favorites: number
  likedByCurrentUser: boolean
  favoritedByCurrentUser: boolean
  reason: string
}

export interface AiFollowUp {
  text: string
}

export interface AiMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdAt: string
  trailCards: AiTrailCard[]
  followUps: AiFollowUp[]
  isStreaming?: boolean
}

export interface AiChatContextPayload {
  currentTrailId?: EntityId | null
  currentCity?: string | null
  useUserPreference?: boolean
}

export interface AiChatStreamPayload {
  conversationId?: EntityId | null
  message: string
  context?: AiChatContextPayload
}

export type AiStreamEvent =
  | { event: 'auth.ok'; data: { userId: EntityId; displayName: string } }
  | { event: 'session'; data: { conversationId: EntityId } }
  | { event: 'delta'; data: { content: string } }
  | { event: 'trail_cards'; data: AiTrailCard[] }
  | { event: 'follow_ups'; data: AiFollowUp[] }
  | { event: 'done'; data: { conversationId: EntityId; intent: string; finishedAt: string; hasTrailCards: boolean; modelFirstTokenMs?: number; modelTotalMs?: number; intentParseMs?: number } }
  | { event: 'error'; data: { code: string; message: string } }
