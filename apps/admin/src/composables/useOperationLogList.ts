import { computed, ref } from 'vue'

import { fetchAdminOperationLogDetail, fetchAdminOperationLogs } from '../api/admin'
import type { AdminOperationLogDetail, AdminOperationLogListItem } from '../types/admin'

export function useOperationLogList() {
  const loading = ref(false)
  const detailLoading = ref(false)
  const list = ref<AdminOperationLogListItem[]>([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const moduleCode = ref('')
  const actionCode = ref('')
  const operatorKeyword = ref('')
  const targetType = ref('')
  const targetId = ref('')
  const dateFrom = ref('')
  const dateTo = ref('')
  const errorMessage = ref('')
  const detailVisible = ref(false)
  const detail = ref<AdminOperationLogDetail | null>(null)

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  async function load(page = pageNum.value) {
    loading.value = true
    errorMessage.value = ''
    try {
      const result = await fetchAdminOperationLogs({
        pageNum: page,
        pageSize: pageSize.value,
        moduleCode: moduleCode.value || undefined,
        actionCode: actionCode.value || undefined,
        operatorKeyword: operatorKeyword.value.trim() || undefined,
        targetType: targetType.value || undefined,
        targetId: targetId.value.trim() || undefined,
        dateFrom: dateFrom.value || undefined,
        dateTo: dateTo.value || undefined,
      })
      list.value = result.list
      total.value = result.total
      pageNum.value = result.pageNum
      pageSize.value = result.pageSize
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '操作日志加载失败'
    } finally {
      loading.value = false
    }
  }

  function resetFilters() {
    moduleCode.value = ''
    actionCode.value = ''
    operatorKeyword.value = ''
    targetType.value = ''
    targetId.value = ''
    dateFrom.value = ''
    dateTo.value = ''
    void load(1)
  }

  async function openDetail(logId: string | number) {
    detailVisible.value = true
    detailLoading.value = true
    detail.value = null
    errorMessage.value = ''
    try {
      detail.value = await fetchAdminOperationLogDetail(logId)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '操作日志详情加载失败'
      detailVisible.value = false
    } finally {
      detailLoading.value = false
    }
  }

  function closeDetail() {
    detailVisible.value = false
    detail.value = null
  }

  return {
    loading,
    detailLoading,
    list,
    total,
    pageNum,
    pageSize,
    moduleCode,
    actionCode,
    operatorKeyword,
    targetType,
    targetId,
    dateFrom,
    dateTo,
    errorMessage,
    totalPages,
    detailVisible,
    detail,
    load,
    resetFilters,
    openDetail,
    closeDetail,
  }
}
