import type { TrailListItem } from '../types/trail'

export function toHomeTrailCard(trail: TrailListItem) {
  return {
    id: trail.id,
    image: trail.image,
    name: trail.name,
    difficulty: trail.difficulty,
    difficultyLabel: trail.difficultyLabel,
    packType: trail.packType,
    durationType: trail.durationType,
    rating: trail.rating,
    reviews: `(${trail.reviewCount >= 1000 ? (trail.reviewCount / 1000).toFixed(1) + 'k' : trail.reviewCount} 条评论)`,
    distance: trail.distance,
    elevation: trail.elevation,
    duration: trail.duration,
    likes: trail.likes,
    favorites: trail.favorites,
    likedByCurrentUser: trail.likedByCurrentUser,
    favoritedByCurrentUser: trail.favoritedByCurrentUser,
  }
}

export function toSearchTrailCard(trail: TrailListItem) {
  return {
    id: trail.id,
    image: trail.image,
    name: trail.name,
    difficulty: trail.difficulty,
    difficultyLabel: trail.difficultyLabel,
    packType: trail.packType,
    durationType: trail.durationType,
    distance: trail.distance,
    duration: trail.duration,
    elevation: trail.elevation,
    description: trail.description,
    rating: trail.rating,
    reviewCount: trail.reviewCount,
    likes: trail.likes,
    favorites: trail.favorites,
    likedByCurrentUser: trail.likedByCurrentUser,
    favoritedByCurrentUser: trail.favoritedByCurrentUser,
  }
}

export function toCommunityPost(trail: TrailListItem) {
  return trail
}
