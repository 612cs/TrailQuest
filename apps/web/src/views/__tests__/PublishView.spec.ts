import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'

import PublishView from '../PublishView.vue'
import { useFlashStore } from '../../stores/useFlashStore'
import { usePublishUploadStore } from '../../stores/usePublishUploadStore'

const replaceMock = vi.fn()
const backMock = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ replace: replaceMock, back: backMock }),
}))

vi.mock('../../api/trails', async () => {
  const actual = await vi.importActual<typeof import('../../api/trails')>('../../api/trails')
  return {
    ...actual,
    fetchTrailDetail: vi.fn(),
  }
})

vi.mock('../../api/geo', () => ({
  reverseGeo: vi.fn(),
}))

vi.mock('../../composables/useAmapLoader', () => ({
  useAmapLoader: () => ({ load: vi.fn().mockResolvedValue(null) }),
}))

vi.mock('../../composables/useTrailGeo', () => ({
  useTrailGeo: () => ({ resolve: vi.fn().mockResolvedValue(null) }),
}))

vi.mock('../../composables/useTrailWeather', () => ({
  useTrailWeather: () => ({
    weather: { value: null },
    resolve: vi.fn().mockResolvedValue(null),
  }),
}))

describe('PublishView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    localStorage.clear()
    replaceMock.mockReset()
    backMock.mockReset()
  })

  it('should show missing required field message only after submit click', async () => {
    const wrapper = mount(PublishView, {
      global: {
        stubs: {
          BaseIcon: { template: '<span />' },
          ImagePreviewModal: { template: '<div />' },
          TrailTrackViewer: { template: '<div />' },
          Teleport: true,
          Transition: false,
        },
      },
    })

    await flushPromises()

    const flashStore = useFlashStore()
    const publishStore = usePublishUploadStore()
    const draft = publishStore.ensureDraft('create', 'create', null)

    expect(flashStore.message).toBeNull()
    expect(draft.fields.name).toBe('')

    await wrapper.get('[data-testid="publish-submit-button"]').trigger('click')
    await flushPromises()

    expect(flashStore.message?.type).toBe('error')
    expect(flashStore.message?.message).toContain('请先补充：封面图片、路线名称、所在位置')
  })
})
