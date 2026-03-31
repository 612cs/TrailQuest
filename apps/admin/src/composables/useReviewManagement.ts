import { computed, ref } from 'vue'

import {
  batchHideAdminReviews,
  batchRestoreAdminReviews,
  deleteAdminReview,
  fetchAdminReviewDetail,
  fetchAdminReviews,
  hideAdminReview,
  restoreAdminReview,
} from '../api/admin'
import type { AdminReviewDetail, AdminReviewListItem } from '../types/admin'

type ActionType = 'hide' | 'restore' | 'delete' | 'batch-hide' | 'batch-restore'

export function useReviewManagement() {
  const loading = ref(false)
  const actionLoading = ref(false)
  const detailLoading = ref(false)
  const list = ref<AdminReviewListItem[]>([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const keyword = ref('')
  const trailKeyword = ref('')
  const authorKeyword = ref('')
  const status = ref('')
  const errorMessage = ref('')
  const selectedIds = ref<string[]>([])
  const detailVisible = ref(false)
  const detail = ref<AdminReviewDetail | null>(null)
  const targetReview = ref<AdminReviewListItem | null>(null)
  const confirmAction = ref<ActionType | null>(null)
  const confirmVisible = ref(false)
  const noticeVisible = ref(false)
  const noticeTitle = ref('')
  const noticeMessage = ref('')

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
  const selectedSet = computed(() => new Set(selectedIds.value))
  const selectedReviews = computed(() => list.value.filter((item) => selectedSet.value.has(String(item.id))))
  const selectedActiveIds = computed(() => selectedReviews.value
    .filter((item) => item.status === 'active')
    .map((item) => String(item.id)))
  const selectedRestorableIds = computed(() => selectedReviews.value
    .filter((item) => item.status !== 'active')
    .map((item) => String(item.id)))
  const currentPageIds = computed(() => list.value.map((item) => String(item.id)))
  const allCurrentSelected = computed(() => currentPageIds.value.length > 0
    && currentPageIds.value.every((id) => selectedSet.value.has(id)))

  async function load(page = pageNum.value) {
    loading.value = true
    errorMessage.value = ''
    try {
      const result = await fetchAdminReviews({
        pageNum: page,
        pageSize: pageSize.value,
        keyword: keyword.value.trim() || undefined,
        trailKeyword: trailKeyword.value.trim() || undefined,
        authorKeyword: authorKeyword.value.trim() || undefined,
        status: status.value || undefined,
      })
      list.value = result.list
      total.value = result.total
      pageNum.value = result.pageNum
      pageSize.value = result.pageSize
      selectedIds.value = []
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '评论列表加载失败'
    } finally {
      loading.value = false
    }
  }

  function resetFilters() {
    keyword.value = ''
    trailKeyword.value = ''
    authorKeyword.value = ''
    status.value = ''
    void load(1)
  }

  function toggleSelection(reviewId: string | number, checked: boolean) {
    const normalizedId = String(reviewId)
    if (checked) {
      if (!selectedSet.value.has(normalizedId)) {
        selectedIds.value = [...selectedIds.value, normalizedId]
      }
      return
    }
    selectedIds.value = selectedIds.value.filter((id) => id !== normalizedId)
  }

  function toggleSelectAllCurrent(checked: boolean) {
    selectedIds.value = checked ? [...currentPageIds.value] : []
  }

  async function openDetail(reviewId: string | number) {
    detailVisible.value = true
    detailLoading.value = true
    detail.value = null
    errorMessage.value = ''
    try {
      detail.value = await fetchAdminReviewDetail(reviewId)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '评论详情加载失败'
      detailVisible.value = false
    } finally {
      detailLoading.value = false
    }
  }

  function closeDetail() {
    detailVisible.value = false
    detail.value = null
  }

  function requestHide(review: AdminReviewListItem) {
    targetReview.value = review
    confirmAction.value = 'hide'
    confirmVisible.value = true
  }

  function requestRestore(review: AdminReviewListItem) {
    targetReview.value = review
    confirmAction.value = 'restore'
    confirmVisible.value = true
  }

  function requestDelete(review: AdminReviewListItem) {
    targetReview.value = review
    confirmAction.value = 'delete'
    confirmVisible.value = true
  }

  function requestBatchHide() {
    confirmAction.value = 'batch-hide'
    confirmVisible.value = true
  }

  function requestBatchRestore() {
    confirmAction.value = 'batch-restore'
    confirmVisible.value = true
  }

  function resetConfirmState() {
    confirmVisible.value = false
    confirmAction.value = null
    targetReview.value = null
  }

  function closeConfirm() {
    if (actionLoading.value) {
      return
    }
    resetConfirmState()
  }

  function openNotice(title: string, message: string) {
    noticeTitle.value = title
    noticeMessage.value = message
    noticeVisible.value = true
  }

  async function submitConfirm(reason: string) {
    if (!confirmAction.value) {
      return
    }

    actionLoading.value = true
    errorMessage.value = ''
    try {
      if (confirmAction.value === 'hide' && targetReview.value) {
        await hideAdminReview(targetReview.value.id, { remark: reason })
        openNotice('评论已隐藏', '该评论已从前台隐藏，仍可在后台恢复。')
      } else if (confirmAction.value === 'restore' && targetReview.value) {
        await restoreAdminReview(targetReview.value.id)
        openNotice('评论已恢复', '该评论已经恢复为前台可见状态。')
      } else if (confirmAction.value === 'delete' && targetReview.value) {
        await deleteAdminReview(targetReview.value.id, { remark: reason })
        openNotice('评论已删除', '该评论已被标记为删除状态，前台不会再展示。')
      } else if (confirmAction.value === 'batch-hide') {
        await batchHideAdminReviews({ ids: selectedActiveIds.value, remark: reason })
        openNotice('批量隐藏完成', `已隐藏 ${selectedActiveIds.value.length} 条评论。`)
      } else if (confirmAction.value === 'batch-restore') {
        await batchRestoreAdminReviews({ ids: selectedRestorableIds.value })
        openNotice('批量恢复完成', `已恢复 ${selectedRestorableIds.value.length} 条评论。`)
      }

      resetConfirmState()
      await load(pageNum.value)
      if (detailVisible.value && detail.value?.id) {
        await openDetail(detail.value.id)
      }
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '评论治理操作失败'
    } finally {
      actionLoading.value = false
    }
  }

  const confirmConfig = computed(() => {
    switch (confirmAction.value) {
      case 'hide':
        return {
          title: '确认隐藏评论',
          message: targetReview.value ? `隐藏后，评论“${targetReview.value.text.slice(0, 24)}”不会在前台展示。` : '隐藏后该评论不会在前台展示。',
          confirmText: '确认隐藏',
          requireReason: true,
          reasonLabel: '处理原因',
          reasonPlaceholder: '请输入隐藏原因',
        }
      case 'restore':
        return {
          title: '确认恢复评论',
          message: targetReview.value ? `恢复后，评论“${targetReview.value.text.slice(0, 24)}”会重新在前台展示。` : '恢复后该评论会重新在前台展示。',
          confirmText: '确认恢复',
          requireReason: false,
          reasonLabel: '处理原因',
          reasonPlaceholder: '',
        }
      case 'delete':
        return {
          title: '确认删除评论',
          message: targetReview.value ? `删除后，评论“${targetReview.value.text.slice(0, 24)}”会被标记为删除状态。` : '删除后该评论会被标记为删除状态。',
          confirmText: '确认删除',
          requireReason: true,
          reasonLabel: '删除原因',
          reasonPlaceholder: '请输入删除原因',
        }
      case 'batch-hide':
        return {
          title: '确认批量隐藏评论',
          message: `本次将隐藏 ${selectedActiveIds.value.length} 条正常状态评论。`,
          confirmText: '确认批量隐藏',
          requireReason: true,
          reasonLabel: '处理原因',
          reasonPlaceholder: '请输入批量隐藏原因',
        }
      case 'batch-restore':
        return {
          title: '确认批量恢复评论',
          message: `本次将恢复 ${selectedRestorableIds.value.length} 条已隐藏或已删除评论。`,
          confirmText: '确认批量恢复',
          requireReason: false,
          reasonLabel: '处理原因',
          reasonPlaceholder: '',
        }
      default:
        return {
          title: '确认操作',
          message: '确认继续当前操作？',
          confirmText: '确认',
          requireReason: false,
          reasonLabel: '处理原因',
          reasonPlaceholder: '',
        }
    }
  })

  return {
    loading,
    actionLoading,
    detailLoading,
    list,
    total,
    pageNum,
    pageSize,
    keyword,
    trailKeyword,
    authorKeyword,
    status,
    errorMessage,
    totalPages,
    selectedIds,
    selectedActiveIds,
    selectedRestorableIds,
    allCurrentSelected,
    detailVisible,
    detail,
    confirmVisible,
    confirmConfig,
    noticeVisible,
    noticeTitle,
    noticeMessage,
    load,
    resetFilters,
    toggleSelection,
    toggleSelectAllCurrent,
    openDetail,
    closeDetail,
    requestHide,
    requestRestore,
    requestDelete,
    requestBatchHide,
    requestBatchRestore,
    closeConfirm,
    submitConfirm,
  }
}
