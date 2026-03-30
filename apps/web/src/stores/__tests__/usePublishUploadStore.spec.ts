import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { usePublishUploadStore } from '../usePublishUploadStore'
import { useFlashStore } from '../useFlashStore'
import { useTrailFeedRefreshStore } from '../useTrailFeedRefreshStore'

const createTrailMock = vi.fn()
const updateTrailMock = vi.fn()
const uploadPublishImageMock = vi.fn()
const uploadPublishTrackMock = vi.fn()
const mediaIdOfMock = vi.fn((payload: { mediaId: string } | null | undefined) => payload?.mediaId ?? null)

vi.mock('../../api/trails', () => ({
  createTrail: createTrailMock,
  updateTrail: updateTrailMock,
}))

vi.mock('../../utils/publishUploadExecutor', () => ({
  uploadPublishImage: uploadPublishImageMock,
  uploadPublishTrack: uploadPublishTrackMock,
  mediaIdOf: mediaIdOfMock,
  resolveUploadErrorMessage: (error: unknown) => error instanceof Error ? error.message : '上传失败',
}))

function flushBackgroundTasks() {
  return new Promise((resolve) => setTimeout(resolve, 0))
}

describe('usePublishUploadStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    createTrailMock.mockReset()
    updateTrailMock.mockReset()
    uploadPublishImageMock.mockReset()
    uploadPublishTrackMock.mockReset()
    sessionStorage.clear()
    localStorage.clear()
  })

  it('should submit create draft with structured geo payload and reset create draft after success', async () => {
    const store = usePublishUploadStore()
    const flashStore = useFlashStore()
    const feedRefreshStore = useTrailFeedRefreshStore()

    const draft = store.ensureDraft('create', 'create', null)
    draft.fields.name = '武功山反穿'
    draft.fields.location = '萍乡 芦溪'
    draft.fields.description = '一条适合周末的路线'
    draft.fields.selectedTags = ['云海', '单日']
    draft.geo = {
      country: '中国',
      province: '江西',
      city: '萍乡',
      district: '芦溪',
      source: 'track_reverse',
    }
    draft.coverItems = [{
      id: 'cover-1',
      source: 'local',
      bizType: 'trail_cover',
      file: new File(['cover'], 'cover.png', { type: 'image/png' }),
      fileName: 'cover.png',
      mimeType: 'image/png',
      localUrl: 'blob:cover',
      remoteUrl: '',
      mediaId: null,
      progress: 0,
      status: 'pending',
      errorMessage: '',
    }]
    draft.trackItem = {
      id: 'track-1',
      source: 'local',
      file: new File(['track'], 'sample.kml', { type: 'application/vnd.google-earth.kml+xml' }),
      fileName: 'sample.kml',
      localUrl: 'blob:track',
      remoteUrl: '',
      mediaId: null,
      mimeType: 'application/vnd.google-earth.kml+xml',
      extension: 'kml',
      progress: 0,
      status: 'pending',
      errorMessage: '',
    }

    uploadPublishImageMock.mockResolvedValue({ mediaId: 'cover-media-1', url: 'https://img.example.com/cover.png' })
    uploadPublishTrackMock.mockResolvedValue({ mediaId: 'track-media-1', url: 'https://cdn.example.com/sample.kml' })
    createTrailMock.mockResolvedValue({ id: 'trail-1001' })

    await store.submitDraft('create')
    await flushBackgroundTasks()
    await flushBackgroundTasks()

    expect(createTrailMock).toHaveBeenCalledTimes(1)
    expect(createTrailMock).toHaveBeenCalledWith(expect.objectContaining({
      name: '武功山反穿',
      location: '萍乡 芦溪',
      geoCountry: '中国',
      geoProvince: '江西',
      geoCity: '萍乡',
      geoDistrict: '芦溪',
      geoSource: 'track_reverse',
      coverMediaId: 'cover-media-1',
      trackMediaId: 'track-media-1',
      tags: ['云海', '单日'],
    }))
    expect(draft.fields.name).toBe('')
    expect(draft.fields.location).toBe('')
    expect(draft.coverItems).toHaveLength(0)
    expect(draft.trackItem).toBeNull()
    expect(draft.geo.city).toBe('')
    expect(feedRefreshStore.version).toBe(1)
    expect(flashStore.message?.message).toBe('路线发布成功')
  })

  it('should preserve existing geo fields when submit falls back to manual location', async () => {
    const store = usePublishUploadStore()
    const draft = store.ensureDraft('create', 'create', null)
    draft.fields.name = '明月山徒步'
    draft.fields.location = '宜春 袁州'
    draft.coverItems = [{
      id: 'cover-2',
      source: 'local',
      bizType: 'trail_cover',
      file: new File(['cover'], 'cover.png', { type: 'image/png' }),
      fileName: 'cover.png',
      mimeType: 'image/png',
      localUrl: 'blob:cover2',
      remoteUrl: '',
      mediaId: null,
      progress: 0,
      status: 'pending',
      errorMessage: '',
    }]
    draft.geo = {
      country: '中国',
      province: '江西',
      city: '宜春',
      district: '袁州',
      source: 'location_lookup',
    }

    uploadPublishImageMock.mockResolvedValue({ mediaId: 'cover-media-2', url: 'https://img.example.com/cover2.png' })
    createTrailMock.mockResolvedValue({ id: 'trail-1002' })

    await store.submitDraft('create')
    await flushBackgroundTasks()
    await flushBackgroundTasks()

    expect(createTrailMock).toHaveBeenCalledWith(expect.objectContaining({
      geoCountry: '中国',
      geoProvince: '江西',
      geoCity: '宜春',
      geoDistrict: '袁州',
      geoSource: 'location_lookup',
      trackMediaId: null,
    }))
  })
})
