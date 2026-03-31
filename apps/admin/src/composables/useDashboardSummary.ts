import { computed, shallowRef } from 'vue'

import { fetchDashboardSummary } from '../api/admin'
import { formatDateTime } from '../utils/format'
import type { AdminDashboardSummary, AdminDashboardTodoItem } from '../types/admin'

export function useDashboardSummary() {
  const summary = shallowRef<AdminDashboardSummary | null>(null)
  const loading = shallowRef(false)
  const errorMessage = shallowRef('')
  const refreshedAt = shallowRef('')

  const todoItems = computed<AdminDashboardTodoItem[]>(() => {
    const data = summary.value
    if (!data) {
      return []
    }

    return [
      {
        key: 'pending-trails',
        title: '待审核路线',
        count: data.pendingTrailCount,
        description: '新发布路线还在审核队列中，建议优先清空积压。',
        actionLabel: '去审核',
        to: { path: '/trails/review', query: { reviewStatus: 'pending' } },
      },
      {
        key: 'pending-reports',
        title: '待处理举报',
        count: data.pendingReportCount,
        description: '举报链路尚在补齐中，当前先保留治理入口和待办占位。',
        actionLabel: '去处理',
        to: { path: '/reports' },
      },
      {
        key: 'hidden-reviews',
        title: '已隐藏评论',
        count: data.hiddenReviewCount,
        description: '隐藏评论持续增加时，通常意味着社区风险内容在上升。',
        actionLabel: '去治理',
        to: { path: '/reviews', query: { status: 'hidden' } },
      },
      {
        key: 'offline-trails',
        title: '已下线路线',
        count: data.offlineTrailCount,
        description: '查看近期被下架的路线，避免长期积压在不可见状态。',
        actionLabel: '去查看',
        to: { path: '/trails/manage', query: { status: 'deleted' } },
      },
    ]
  })

  async function loadSummary() {
    loading.value = true
    errorMessage.value = ''
    try {
      summary.value = await fetchDashboardSummary()
      refreshedAt.value = formatDateTime(new Date().toISOString())
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '仪表盘加载失败'
    } finally {
      loading.value = false
    }
  }

  return {
    summary,
    loading,
    errorMessage,
    refreshedAt,
    todoItems,
    loadSummary,
  }
}
