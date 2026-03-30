import { createPinia, setActivePinia } from 'pinia'
import { mount, type MountingOptions, type VueWrapper } from '@vue/test-utils'
import type { Component } from 'vue'

export function renderWithApp<T extends Component>(component: T, options: MountingOptions<any> = {}): VueWrapper<any> {
  const pinia = createPinia()
  setActivePinia(pinia)

  return mount(component, {
    ...options,
    global: {
      ...options.global,
      plugins: [...(options.global?.plugins ?? []), pinia],
    },
  })
}
