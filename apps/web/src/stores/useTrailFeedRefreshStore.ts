import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTrailFeedRefreshStore = defineStore('trailFeedRefresh', () => {
  const version = ref(0)

  function bump() {
    version.value += 1
  }

  return {
    version,
    bump,
  }
})
