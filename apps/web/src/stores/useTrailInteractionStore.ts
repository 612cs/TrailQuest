import { defineStore } from 'pinia'
import { ref } from 'vue'

import { favoriteTrail, likeTrail, unfavoriteTrail, unlikeTrail } from '../api/trails'
import { useFlashStore } from './useFlashStore'
import { useUserStore } from './useUserStore'
import type { EntityId } from '../types/id'
import type { TrailInteractionResult, TrailInteractionState, TrailListItem } from '../types/trail'
import { ApiError } from '../types/api'

type TrailLikeState = Pick<
  TrailListItem,
  'id' | 'likes' | 'favorites' | 'likedByCurrentUser' | 'favoritedByCurrentUser'
>

export const useTrailInteractionStore = defineStore('trailInteraction', () => {
  const interactionMap = ref<Record<string, TrailInteractionState>>({})
  const likePendingMap = ref<Record<string, boolean>>({})
  const favoritePendingMap = ref<Record<string, boolean>>({})
  const userStore = useUserStore()
  const flashStore = useFlashStore()

  function hydrateTrail(trail: TrailLikeState) {
    interactionMap.value[String(trail.id)] = normalizeTrail(trail)
  }

  function hydrateTrails(trails: TrailLikeState[]) {
    trails.forEach(hydrateTrail)
  }

  function applyToTrail<T extends TrailLikeState>(trail: T): T {
    const interaction = interactionMap.value[String(trail.id)]
    if (!interaction) {
      return trail
    }

    return {
      ...trail,
      ...interaction,
    }
  }

  function isLikePending(trailId: EntityId) {
    return !!likePendingMap.value[String(trailId)]
  }

  function isFavoritePending(trailId: EntityId) {
    return !!favoritePendingMap.value[String(trailId)]
  }

  async function toggleLike(trail: TrailLikeState) {
    if (isLikePending(trail.id)) return false

    if (!userStore.isLoggedIn) {
      userStore.requireAuth(() => {})
      return false
    }

    const previous = ensureInteraction(trail)
    const optimistic: TrailInteractionState = {
      ...previous,
      likes: Math.max(previous.likes + (previous.likedByCurrentUser ? -1 : 1), 0),
      likedByCurrentUser: !previous.likedByCurrentUser,
    }

    interactionMap.value[String(trail.id)] = optimistic
    likePendingMap.value[String(trail.id)] = true

    try {
      const result = previous.likedByCurrentUser ? await unlikeTrail(trail.id) : await likeTrail(trail.id)
      applyInteractionResult(result)
      return true
    } catch (error) {
      interactionMap.value[String(trail.id)] = previous
      flashStore.showError(resolveErrorMessage(error, previous.likedByCurrentUser ? '取消点赞失败' : '点赞失败'))
      return false
    } finally {
      likePendingMap.value[String(trail.id)] = false
    }
  }

  async function toggleFavorite(trail: TrailLikeState) {
    if (isFavoritePending(trail.id)) return false

    if (!userStore.isLoggedIn) {
      userStore.requireAuth(() => {})
      return false
    }

    const previous = ensureInteraction(trail)
    const optimistic: TrailInteractionState = {
      ...previous,
      favorites: Math.max(previous.favorites + (previous.favoritedByCurrentUser ? -1 : 1), 0),
      favoritedByCurrentUser: !previous.favoritedByCurrentUser,
    }

    interactionMap.value[String(trail.id)] = optimistic
    favoritePendingMap.value[String(trail.id)] = true

    try {
      const result = previous.favoritedByCurrentUser ? await unfavoriteTrail(trail.id) : await favoriteTrail(trail.id)
      applyInteractionResult(result)
      return true
    } catch (error) {
      interactionMap.value[String(trail.id)] = previous
      flashStore.showError(resolveErrorMessage(error, previous.favoritedByCurrentUser ? '取消收藏失败' : '收藏失败'))
      return false
    } finally {
      favoritePendingMap.value[String(trail.id)] = false
    }
  }

  function applyInteractionResult(result: TrailInteractionResult) {
    interactionMap.value[String(result.trailId)] = normalizeInteraction(result)
  }

  function ensureInteraction(trail: TrailLikeState) {
    const current = interactionMap.value[String(trail.id)]
    if (current) {
      return { ...current }
    }

    const normalized = normalizeTrail(trail)
    interactionMap.value[String(trail.id)] = normalized
    return { ...normalized }
  }

  function normalizeTrail(trail: TrailLikeState): TrailInteractionState {
    return {
      trailId: trail.id,
      likes: trail.likes ?? 0,
      favorites: trail.favorites ?? 0,
      likedByCurrentUser: Boolean(trail.likedByCurrentUser),
      favoritedByCurrentUser: Boolean(trail.favoritedByCurrentUser),
    }
  }

  function normalizeInteraction(result: TrailInteractionResult): TrailInteractionState {
    return {
      trailId: result.trailId,
      likes: result.likes ?? 0,
      favorites: result.favorites ?? 0,
      likedByCurrentUser: Boolean(result.likedByCurrentUser),
      favoritedByCurrentUser: Boolean(result.favoritedByCurrentUser),
    }
  }

  function resolveErrorMessage(error: unknown, fallback: string) {
    if (error instanceof ApiError) {
      return error.message
    }
    if (error instanceof Error) {
      return error.message
    }
    return fallback
  }

  return {
    hydrateTrail,
    hydrateTrails,
    applyToTrail,
    isLikePending,
    isFavoritePending,
    toggleLike,
    toggleFavorite,
    applyInteractionResult,
  }
})
