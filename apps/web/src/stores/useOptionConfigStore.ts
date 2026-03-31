import { defineStore } from 'pinia'
import { ref } from 'vue'

import { fetchOptionConfigs } from '../api/config'
import type { OptionConfigGroupMap, OptionConfigItem } from '../types/config'

export const useOptionConfigStore = defineStore('option-config', () => {
  const groups = ref<OptionConfigGroupMap>({})
  const loading = ref(false)
  const loadedGroups = ref<string[]>([])

  async function ensureGroups(groupCodes: string[]) {
    const normalized = Array.from(new Set(groupCodes.filter(Boolean)))
    const missing = normalized.filter((code) => !loadedGroups.value.includes(code))
    if (!missing.length) {
      return
    }

    loading.value = true
    try {
      const result = await fetchOptionConfigs(missing)
      groups.value = {
        ...groups.value,
        ...result,
      }
      loadedGroups.value = Array.from(new Set([...loadedGroups.value, ...Object.keys(result)]))
    } finally {
      loading.value = false
    }
  }

  function getGroup(groupCode: string, fallback: OptionConfigItem[] = []) {
    const list = groups.value[groupCode]
    return Array.isArray(list) && list.length ? list : fallback
  }

  return {
    groups,
    loading,
    loadedGroups,
    ensureGroups,
    getGroup,
  }
})
