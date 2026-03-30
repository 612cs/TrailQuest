import { computed, ref } from 'vue'

import { fetchAdminTrails } from '../api/admin'
import type { AdminTrailListItem } from '../types/admin'

export function useTrailReviewList() {
  const loading = ref(false)
  const list = ref<AdminTrailListItem[]>([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const keyword = ref('')
  const reviewStatus = ref('')
  const authorKeyword = ref('')
  const errorMessage = ref('')
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  async function load(page = pageNum.value) {
    loading.value = true
    errorMessage.value = ''
    try {
      const result = await fetchAdminTrails({
        pageNum: page,
        pageSize: pageSize.value,
        keyword: keyword.value.trim() || undefined,
        reviewStatus: reviewStatus.value || undefined,
        authorKeyword: authorKeyword.value.trim() || undefined,
      })
      list.value = result.list
      total.value = result.total
      pageNum.value = result.pageNum
      pageSize.value = result.pageSize
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '路线列表加载失败'
    } finally {
      loading.value = false
    }
  }

  function resetFilters() {
    keyword.value = ''
    reviewStatus.value = ''
    authorKeyword.value = ''
    void load(1)
  }

  function changePage(delta: number) {
    const next = pageNum.value + delta
    if (next < 1 || next > totalPages.value) {
      return
    }
    void load(next)
  }

  return {
    loading,
    list,
    total,
    pageNum,
    pageSize,
    keyword,
    reviewStatus,
    authorKeyword,
    errorMessage,
    totalPages,
    load,
    resetFilters,
    changePage,
  }
}
