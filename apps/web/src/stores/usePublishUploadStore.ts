import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

import { createTrail, updateTrail } from '../api/trails'
import { useFlashStore } from './useFlashStore'
import { useTrailFeedRefreshStore } from './useTrailFeedRefreshStore'
import type { EntityId } from '../types/id'
import type { TrailListItem } from '../types/trail'
import type {
  PublishDraftFields,
  PublishDraftGeo,
  PublishDraftMode,
  PublishDraftState,
  PublishImageAsset,
  PublishTaskStage,
  PublishTrackAsset,
} from '../types/publishUpload'
import {
  mediaIdOf,
  resolveUploadErrorMessage,
  uploadPublishImage,
  uploadPublishTrack,
} from '../utils/publishUploadExecutor'

const STORAGE_KEY = 'publish-upload-drafts'
const GALLERY_CONCURRENCY = 2

function buildId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function createEmptyFields(): PublishDraftFields {
  return {
    name: '',
    location: '',
    difficulty: 'moderate',
    packType: 'light',
    durationType: 'single_day',
    distance: '',
    elevation: '',
    duration: '',
    description: '',
    selectedTags: [],
    customTag: '',
  }
}

function createEmptyGeo(): PublishDraftGeo {
  return {
    country: '',
    province: '',
    city: '',
    district: '',
    source: '',
  }
}

function createEmptyTask() {
  return {
    id: null,
    stage: 'idle' as PublishTaskStage,
    errorMessage: '',
    resultTrailId: null,
    updatedAt: null,
  }
}

function resetDraftAfterCreateSuccess(draft: PublishDraftState) {
  draft.fields = createEmptyFields()
  draft.coverItems = []
  draft.galleryItems = []
  draft.trackItem = null
  draft.geoJsonData = null
  draft.trackPreviewError = ''
  draft.geo = createEmptyGeo()
  draft.task = createEmptyTask()
  draft.hydratedFromServer = false
}

function serializeImageAsset(item: PublishImageAsset) {
  return {
    id: item.id,
    source: item.source,
    bizType: item.bizType,
    file: null,
    fileName: item.fileName,
    mimeType: item.mimeType,
    localUrl: item.source === 'existing' ? item.localUrl : '',
    remoteUrl: item.remoteUrl,
    mediaId: item.mediaId,
    progress: item.progress,
    status: item.file || item.mediaId ? item.status : 'missing',
    errorMessage: item.file ? item.errorMessage : (item.mediaId ? item.errorMessage : '本地文件已失效，请重新选择'),
  }
}

function serializeTrackAsset(item: PublishTrackAsset | null) {
  if (!item) {
    return null
  }
  return {
    id: item.id,
    source: item.source,
    file: null,
    fileName: item.fileName,
    localUrl: item.source === 'existing' ? item.localUrl : '',
    remoteUrl: item.remoteUrl,
    mediaId: item.mediaId,
    mimeType: item.mimeType,
    extension: item.extension,
    progress: item.progress,
    status: item.file || item.mediaId ? item.status : 'missing',
    errorMessage: item.file ? item.errorMessage : (item.mediaId ? item.errorMessage : '本地文件已失效，请重新选择'),
  }
}

function serializeDraft(draft: PublishDraftState) {
  return {
    scopeKey: draft.scopeKey,
    mode: draft.mode,
    trailId: draft.trailId,
    hydratedFromServer: draft.hydratedFromServer,
    fields: draft.fields,
    coverItems: draft.coverItems.map(serializeImageAsset),
    galleryItems: draft.galleryItems.map(serializeImageAsset),
    trackItem: serializeTrackAsset(draft.trackItem),
    geoJsonData: draft.geoJsonData,
    trackPreviewError: draft.trackPreviewError,
    geo: draft.geo,
    task: draft.task,
  }
}

function createDraft(scopeKey: string, mode: PublishDraftMode, trailId: EntityId | null): PublishDraftState {
  return {
    scopeKey,
    mode,
    trailId,
    hydratedFromServer: false,
    fields: createEmptyFields(),
    coverItems: [],
    galleryItems: [],
    trackItem: null,
    geoJsonData: null,
    trackPreviewError: '',
    geo: createEmptyGeo(),
    task: createEmptyTask(),
  }
}

export const usePublishUploadStore = defineStore('publishUpload', () => {
  const flashStore = useFlashStore()
  const trailFeedRefreshStore = useTrailFeedRefreshStore()
  const drafts = ref<Record<string, PublishDraftState>>({})

  hydrateFromSession()
  watch(drafts, persist, { deep: true })

  function hydrateFromSession() {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return
    }

    try {
      const parsed = JSON.parse(raw) as Record<string, PublishDraftState>
      drafts.value = Object.fromEntries(
        Object.entries(parsed).map(([scopeKey, draft]) => [
          scopeKey,
          {
            ...draft,
            geo: draft.geo ?? createEmptyGeo(),
            task: draft.task.stage === 'success'
              ? draft.task
              : {
                ...draft.task,
                stage: draft.task.stage === 'idle' ? 'idle' : 'error',
                errorMessage: draft.task.stage === 'idle'
                  ? draft.task.errorMessage
                  : (draft.task.errorMessage || '页面刷新后后台上传已中断，请重新选择未完成文件后重试'),
              },
          },
        ]),
      )
    } catch (error) {
      console.error('Failed to parse publish drafts from session storage', error)
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  function persist() {
    sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(
        Object.fromEntries(
          Object.entries(drafts.value).map(([scopeKey, draft]) => [scopeKey, serializeDraft(draft)]),
        ),
      ),
    )
  }

  function ensureDraft(scopeKey: string, mode: PublishDraftMode, trailId: EntityId | null) {
    if (!drafts.value[scopeKey]) {
      drafts.value[scopeKey] = createDraft(scopeKey, mode, trailId)
    }
    return drafts.value[scopeKey]
  }

  function hydrateDraftFromTrail(scopeKey: string, trail: TrailListItem) {
    const draft = ensureDraft(scopeKey, 'edit', trail.id)
    if (draft.hydratedFromServer) {
      return draft
    }

    draft.fields = {
      name: trail.name,
      location: trail.location,
      difficulty: trail.difficulty,
      packType: trail.packType,
      durationType: trail.durationType,
      distance: trail.distance,
      elevation: trail.elevation,
      duration: trail.duration,
      description: trail.description,
      selectedTags: [...trail.tags],
      customTag: '',
    }
    draft.coverItems = trail.coverMediaId
      ? [{
        id: buildId(),
        source: 'existing',
        bizType: 'trail_cover',
        file: null,
        fileName: trail.name || 'cover',
        mimeType: 'image/*',
        localUrl: trail.image,
        remoteUrl: trail.image,
        mediaId: trail.coverMediaId == null ? null : String(trail.coverMediaId),
        progress: 100,
        status: 'existing',
        errorMessage: '',
      }]
      : []
    draft.galleryItems = (trail.gallery ?? []).map((item) => ({
      id: buildId(),
      source: 'existing' as const,
      bizType: 'trail_gallery' as const,
      file: null,
      fileName: item.url.split('/').pop() ?? 'gallery',
      mimeType: 'image/*',
      localUrl: item.url,
      remoteUrl: item.url,
      mediaId: item.mediaId == null ? null : String(item.mediaId),
      progress: 100,
      status: 'existing' as const,
      errorMessage: '',
    }))
    draft.trackItem = trail.track?.hasTrack
      ? {
        id: buildId(),
        source: 'existing',
        file: null,
        fileName: trail.track.originalFileName ?? 'track.gpx',
        localUrl: trail.track.downloadUrl ?? '',
        remoteUrl: trail.track.downloadUrl ?? '',
        mediaId: trail.track.mediaFileId == null ? null : String(trail.track.mediaFileId),
        mimeType: trail.track.sourceFormat === 'kml'
          ? 'application/vnd.google-earth.kml+xml'
          : 'application/gpx+xml',
        extension: trail.track.sourceFormat === 'kml' ? 'kml' : 'gpx',
        progress: 100,
        status: 'existing',
        errorMessage: '',
      }
      : null
    draft.geoJsonData = trail.track?.geoJson ?? null
    draft.trackPreviewError = ''
    draft.geo = {
      country: trail.geoCountry ?? '',
      province: trail.geoProvince ?? '',
      city: trail.geoCity ?? '',
      district: trail.geoDistrict ?? '',
      source: trail.geoSource ?? '',
    }
    draft.hydratedFromServer = true
    return draft
  }

  function setTrackPreview(scopeKey: string, geoJsonData: unknown, errorMessage = '') {
    const draft = drafts.value[scopeKey]
    if (!draft) {
      return
    }
    draft.geoJsonData = geoJsonData
    draft.trackPreviewError = errorMessage
  }

  function clearDraft(scopeKey: string) {
    const draft = drafts.value[scopeKey]
    if (!draft) {
      return
    }

    revokeDraftUrls(draft)
    delete drafts.value[scopeKey]
  }

  function revokeDraftUrls(draft: PublishDraftState) {
    draft.coverItems.forEach(revokeImageUrl)
    draft.galleryItems.forEach(revokeImageUrl)
    revokeTrackUrl(draft.trackItem)
  }

  function revokeImageUrl(item: PublishImageAsset) {
    if (item.source === 'local' && item.localUrl.startsWith('blob:')) {
      URL.revokeObjectURL(item.localUrl)
    }
  }

  function revokeTrackUrl(item: PublishTrackAsset | null) {
    if (item?.source === 'local' && item.localUrl.startsWith('blob:')) {
      URL.revokeObjectURL(item.localUrl)
    }
  }

  async function submitDraft(scopeKey: string) {
    const draft = drafts.value[scopeKey]
    if (!draft) {
      throw new Error('未找到待提交的发布草稿')
    }
    if (isTaskRunning(draft.task.stage)) {
      return
    }

    const taskId = buildId()
    draft.task = {
      id: taskId,
      stage: 'queued',
      errorMessage: '',
      resultTrailId: null,
      updatedAt: Date.now(),
    }
    flashStore.showSuccess('已进入后台上传队列，上传完成后路线会进入审核，请耐心等待。', 3200)
    void runDraft(scopeKey, taskId)
  }

  async function retryDraft(scopeKey: string) {
    const draft = drafts.value[scopeKey]
    if (!draft) {
      throw new Error('未找到待重试的发布草稿')
    }
    const taskId = buildId()
    draft.task = {
      id: taskId,
      stage: 'queued',
      errorMessage: '',
      resultTrailId: draft.task.resultTrailId,
      updatedAt: Date.now(),
    }
    normalizeFailedAssets(draft)
    flashStore.showSuccess('已重新加入后台上传队列，上传完成后路线会重新进入审核。', 2800)
    void runDraft(scopeKey, taskId)
  }

  function normalizeFailedAssets(draft: PublishDraftState) {
    draft.coverItems.forEach((item) => {
      if (item.status === 'error' || item.status === 'missing') {
        item.status = item.file ? 'pending' : 'missing'
        item.errorMessage = item.file ? '' : '本地文件已失效，请重新选择'
        item.progress = item.file ? 0 : item.progress
      }
    })
    draft.galleryItems.forEach((item) => {
      if (item.status === 'error' || item.status === 'missing') {
        item.status = item.file ? 'pending' : 'missing'
        item.errorMessage = item.file ? '' : '本地文件已失效，请重新选择'
        item.progress = item.file ? 0 : item.progress
      }
    })
    if (draft.trackItem && (draft.trackItem.status === 'error' || draft.trackItem.status === 'missing')) {
      draft.trackItem.status = draft.trackItem.file ? 'pending' : 'missing'
      draft.trackItem.errorMessage = draft.trackItem.file ? '' : '本地文件已失效，请重新选择'
      if (draft.trackItem.file) {
        draft.trackItem.progress = 0
      }
    }
  }

  async function runDraft(scopeKey: string, taskId: string) {
    const draft = drafts.value[scopeKey]
    if (!draft || draft.task.id !== taskId) {
      return
    }

    try {
      await uploadCover(draft, taskId)
      await uploadGallery(draft, taskId)
      await uploadTrack(draft, taskId)

      updateStage(draft, draft.mode === 'edit' ? 'updating-trail' : 'creating-trail')
      const coverMediaId = draft.coverItems[0]?.mediaId
      if (!coverMediaId) {
        throw new Error('封面上传未完成，请重新尝试')
      }

      const payload = {
        name: draft.fields.name.trim(),
        location: draft.fields.location.trim(),
        geoCountry: draft.geo.country || undefined,
        geoProvince: draft.geo.province || undefined,
        geoCity: draft.geo.city || undefined,
        geoDistrict: draft.geo.district || undefined,
        geoSource: draft.geo.source || undefined,
        difficulty: draft.fields.difficulty,
        difficultyLabel: resolveDifficultyLabel(draft.fields.difficulty),
        packType: draft.fields.packType,
        durationType: draft.fields.durationType,
        distance: draft.fields.distance.trim() || undefined,
        elevation: draft.fields.elevation.trim() || undefined,
        duration: draft.fields.duration.trim() || undefined,
        description: draft.fields.description.trim(),
        coverMediaId,
        galleryMediaIds: draft.galleryItems
          .filter((item) => item.mediaId)
          .map((item) => item.mediaId!),
        trackMediaId: draft.trackItem?.mediaId ?? null,
        tags: draft.fields.selectedTags,
      }

      const result = draft.mode === 'edit' && draft.trailId
        ? await updateTrail(draft.trailId, payload)
        : await createTrail(payload)

      draft.task = {
        id: taskId,
        stage: 'success',
        errorMessage: '',
        resultTrailId: result.id,
        updatedAt: Date.now(),
      }
      trailFeedRefreshStore.bump()
      flashStore.showSuccess(
        draft.mode === 'edit'
          ? '路线更新已提交审核，请耐心等待'
          : '路线已提交审核，请耐心等待',
        2600,
      )

      if (draft.mode === 'create') {
        revokeDraftUrls(draft)
        resetDraftAfterCreateSuccess(draft)
      } else {
        draft.task = createEmptyTask()
      }
    } catch (error) {
      const message = resolveUploadErrorMessage(error)
      draft.task = {
        id: taskId,
        stage: 'error',
        errorMessage: message,
        resultTrailId: draft.task.resultTrailId,
        updatedAt: Date.now(),
      }
      flashStore.showError(`后台上传失败：${message}`, 3200)
    }
  }

  async function uploadCover(draft: PublishDraftState, taskId: string) {
    const cover = draft.coverItems[0]
    if (!cover) {
      throw new Error('请先选择路线封面')
    }
    if (draft.task.id !== taskId) {
      return
    }
    if (cover.mediaId && (cover.status === 'existing' || cover.status === 'uploaded')) {
      return
    }
    if (!cover.file) {
      cover.status = 'missing'
      cover.errorMessage = '本地封面文件已失效，请重新选择'
      throw new Error(cover.errorMessage)
    }

    updateStage(draft, 'uploading-cover')
    cover.status = 'uploading'
    cover.errorMessage = ''
    cover.progress = 0

    try {
      const remote = await uploadPublishImage(cover.file, 'trail_cover', (progress) => {
        cover.progress = progress
      })
      cover.mediaId = mediaIdOf(remote)
      cover.remoteUrl = remote.url
      cover.status = 'uploaded'
      cover.progress = 100
    } catch (error) {
      cover.status = 'error'
      cover.errorMessage = resolveUploadErrorMessage(error)
      throw error
    }
  }

  async function uploadGallery(draft: PublishDraftState, taskId: string) {
    const pendingItems = draft.galleryItems.filter((item) => !item.mediaId && item.status !== 'missing')
    if (!pendingItems.length) {
      return
    }

    updateStage(draft, 'uploading-gallery')

    for (let index = 0; index < pendingItems.length; index += GALLERY_CONCURRENCY) {
      const batch = pendingItems.slice(index, index + GALLERY_CONCURRENCY)
      await Promise.all(batch.map(async (item) => {
        if (draft.task.id !== taskId) {
          return
        }
        if (!item.file) {
          item.status = 'missing'
          item.errorMessage = '本地图片文件已失效，请重新选择'
          throw new Error(item.errorMessage)
        }

        item.status = 'uploading'
        item.errorMessage = ''
        item.progress = 0

        try {
          const remote = await uploadPublishImage(item.file, 'trail_gallery', (progress) => {
            item.progress = progress
          })
          item.mediaId = mediaIdOf(remote)
          item.remoteUrl = remote.url
          item.status = 'uploaded'
          item.progress = 100
        } catch (error) {
          item.status = 'error'
          item.errorMessage = resolveUploadErrorMessage(error)
          throw error
        }
      }))
    }
  }

  async function uploadTrack(draft: PublishDraftState, taskId: string) {
    const track = draft.trackItem
    if (!track) {
      return
    }
    if (draft.task.id !== taskId) {
      return
    }
    if (track.mediaId && (track.status === 'existing' || track.status === 'uploaded')) {
      return
    }
    if (!track.file) {
      track.status = 'missing'
      track.errorMessage = '本地轨迹文件已失效，请重新选择'
      throw new Error(track.errorMessage)
    }

    updateStage(draft, 'uploading-track')
    track.status = 'uploading'
    track.errorMessage = ''
    track.progress = 0

    try {
      const remote = await uploadPublishTrack(track.file, (progress) => {
        track.progress = progress
      })
      track.mediaId = mediaIdOf(remote)
      track.remoteUrl = remote.url
      track.status = 'uploaded'
      track.progress = 100
    } catch (error) {
      track.status = 'error'
      track.errorMessage = resolveUploadErrorMessage(error)
      throw error
    }
  }

  function updateStage(draft: PublishDraftState, stage: PublishTaskStage) {
    draft.task.stage = stage
    draft.task.updatedAt = Date.now()
  }

  function isTaskRunning(stage: PublishTaskStage) {
    return !['idle', 'error', 'success'].includes(stage)
  }

  function resolveDifficultyLabel(difficulty: PublishDraftFields['difficulty']) {
    return difficulty === 'easy' ? '简单' : difficulty === 'hard' ? '困难' : '适中'
  }

  return {
    drafts,
    ensureDraft,
    hydrateDraftFromTrail,
    setTrackPreview,
    clearDraft,
    submitDraft,
    retryDraft,
    isTaskRunning,
  }
})
