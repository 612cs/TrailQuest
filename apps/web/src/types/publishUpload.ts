import type { EntityId } from './id'

export type PublishDraftMode = 'create' | 'edit'

export type PublishAssetStatus =
  | 'existing'
  | 'pending'
  | 'uploading'
  | 'uploaded'
  | 'error'
  | 'missing'

export type PublishTaskStage =
  | 'idle'
  | 'queued'
  | 'uploading-cover'
  | 'uploading-gallery'
  | 'uploading-track'
  | 'creating-trail'
  | 'updating-trail'
  | 'success'
  | 'error'

export interface PublishDraftFields {
  name: string
  location: string
  difficulty: 'easy' | 'moderate' | 'hard'
  packType: 'light' | 'heavy' | 'both'
  durationType: 'single_day' | 'multi_day'
  distance: string
  elevation: string
  duration: string
  description: string
  selectedTags: string[]
  customTag: string
}

export interface PublishImageAsset {
  id: string
  source: 'existing' | 'local'
  bizType: 'trail_cover' | 'trail_gallery'
  file: File | null
  fileName: string
  mimeType: string
  localUrl: string
  remoteUrl: string
  mediaId: string | null
  progress: number
  status: PublishAssetStatus
  errorMessage: string
}

export interface PublishTrackAsset {
  id: string
  source: 'existing' | 'local'
  file: File | null
  fileName: string
  localUrl: string
  remoteUrl: string
  mediaId: string | null
  mimeType: string
  extension: string
  progress: number
  status: PublishAssetStatus
  errorMessage: string
}

export interface PublishTaskState {
  id: string | null
  stage: PublishTaskStage
  errorMessage: string
  resultTrailId: EntityId | null
  updatedAt: number | null
}

export interface PublishDraftState {
  scopeKey: string
  mode: PublishDraftMode
  trailId: EntityId | null
  hydratedFromServer: boolean
  fields: PublishDraftFields
  coverItems: PublishImageAsset[]
  galleryItems: PublishImageAsset[]
  trackItem: PublishTrackAsset | null
  geoJsonData: unknown
  trackPreviewError: string
  task: PublishTaskState
}
