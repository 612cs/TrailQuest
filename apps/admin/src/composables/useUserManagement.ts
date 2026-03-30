import { computed, ref } from 'vue'

import { banAdminUser, fetchAdminUsers, unbanAdminUser } from '../api/admin'
import type { AdminUserListItem } from '../types/admin'

export function useUserManagement() {
  const loading = ref(false)
  const actionLoading = ref(false)
  const list = ref<AdminUserListItem[]>([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const keyword = ref('')
  const role = ref('')
  const errorMessage = ref('')
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  async function load(page = pageNum.value) {
    loading.value = true
    errorMessage.value = ''
    try {
      const result = await fetchAdminUsers({
        pageNum: page,
        pageSize: pageSize.value,
        keyword: keyword.value.trim() || undefined,
        role: role.value || undefined,
      })
      list.value = result.list
      total.value = result.total
      pageNum.value = result.pageNum
      pageSize.value = result.pageSize
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '用户列表加载失败'
    } finally {
      loading.value = false
    }
  }

  function resetFilters() {
    keyword.value = ''
    role.value = ''
    void load(1)
  }

  function changePage(delta: number) {
    const next = pageNum.value + delta
    if (next < 1 || next > totalPages.value) {
      return
    }
    void load(next)
  }

  async function banUser(userId: string | number, reason: string) {
    actionLoading.value = true
    errorMessage.value = ''
    try {
      await banAdminUser(userId, { reason })
      await load(pageNum.value)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '封禁用户失败'
      throw error
    } finally {
      actionLoading.value = false
    }
  }

  async function unbanUser(userId: string | number) {
    actionLoading.value = true
    errorMessage.value = ''
    try {
      await unbanAdminUser(userId)
      await load(pageNum.value)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '解封用户失败'
      throw error
    } finally {
      actionLoading.value = false
    }
  }

  return {
    loading,
    actionLoading,
    list,
    total,
    pageNum,
    pageSize,
    keyword,
    role,
    errorMessage,
    totalPages,
    load,
    resetFilters,
    changePage,
    banUser,
    unbanUser,
  }
}
