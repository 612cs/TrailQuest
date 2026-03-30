<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ImagePlus, Link2, Mountain, MessageSquareMore, FlagTriangleRight, RefreshCcw, UsersRound } from 'lucide-vue-next'

import { fetchDashboardSummary } from '../api/admin'
import AdminStatCard from '../components/common/AdminStatCard.vue'
import { formatDateTime } from '../utils/format'
import type { AdminDashboardSummary } from '../types/admin'

const summary = ref<AdminDashboardSummary | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const refreshedAt = ref('')

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await fetchDashboardSummary()
    refreshedAt.value = formatDateTime(new Date().toISOString())
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '统计加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
</script>

<template>
  <div class="admin-dashboard admin-grid-2">
    <section class="admin-card admin-section">
      <div class="admin-dashboard__header">
        <div>
          <h2 class="admin-title">治理概览</h2>
          <p class="admin-subtitle">快速查看待审核路线、评论和举报处理状态。</p>
        </div>
        <button class="admin-button admin-button-secondary" type="button" @click="loadSummary">
          <RefreshCcw :size="16" :stroke-width="2" />
          刷新
        </button>
      </div>

      <div v-if="errorMessage" class="admin-dashboard__error">{{ errorMessage }}</div>

      <div class="admin-dashboard__stats">
        <AdminStatCard
          title="待审核路线"
          :value="loading ? '...' : summary?.pendingTrailCount ?? 0"
          description="发布后默认进入待审核状态"
          :icon="Mountain"
        />
        <AdminStatCard
          title="评论总数"
          :value="loading ? '...' : summary?.reviewCount ?? 0"
          description="用户侧评论管理入口"
          :icon="MessageSquareMore"
        />
        <AdminStatCard
          title="待处理举报"
          :value="loading ? '...' : summary?.pendingReportCount ?? 0"
          description="第一版先预留治理入口"
          :icon="FlagTriangleRight"
        />
        <AdminStatCard
          title="用户总数"
          :value="loading ? '...' : summary?.userCount ?? 0"
          description="当前平台已注册用户规模"
          :icon="UsersRound"
        />
      </div>

      <div class="admin-dashboard__footer">
        <div>
          <span class="admin-muted">最近刷新</span>
          <strong>{{ refreshedAt || '--' }}</strong>
        </div>
        <div class="admin-dashboard__links">
          <RouterLink class="admin-dashboard__link" to="/trails/review">路线审核</RouterLink>
          <RouterLink class="admin-dashboard__link" to="/trails/manage">路线管理</RouterLink>
          <RouterLink class="admin-dashboard__link" to="/users">用户管理</RouterLink>
          <RouterLink class="admin-dashboard__link" to="/reviews">评论管理</RouterLink>
          <RouterLink class="admin-dashboard__link" to="/reports">举报处理</RouterLink>
          <RouterLink class="admin-dashboard__link" to="/settings">首页大图管理</RouterLink>
        </div>
      </div>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">后台说明</h2>
      <p class="admin-subtitle" style="margin-top: 0.4rem">
        当前后台采用 TrailQuest 绿色系主题，优先支持内容治理最小集。
      </p>

      <div class="admin-dashboard__notes">
        <div class="admin-dashboard__note">
          <Link2 :size="18" :stroke-width="2" />
          <div>
            <strong>接口复用</strong>
            <p>沿用现有 /api/auth/login 与 /api/auth/me。</p>
          </div>
        </div>
        <div class="admin-dashboard__note">
          <Mountain :size="18" :stroke-width="2" />
          <div>
            <strong>路线审核</strong>
            <p>支持通过、驳回与备注记录。</p>
          </div>
        </div>
        <div class="admin-dashboard__note">
          <Mountain :size="18" :stroke-width="2" />
          <div>
            <strong>路线管理</strong>
            <p>支持对已上线路线进行下架与恢复。</p>
          </div>
        </div>
        <div class="admin-dashboard__note">
          <MessageSquareMore :size="18" :stroke-width="2" />
          <div>
            <strong>评论治理</strong>
            <p>支持列表、筛选与删除。</p>
          </div>
        </div>
        <div class="admin-dashboard__note">
          <ImagePlus :size="18" :stroke-width="2" />
          <div>
            <strong>首页运营位</strong>
            <p>支持动态修改前台首页首屏大图。</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-dashboard {
  align-items: start;
}

.admin-dashboard__header,
.admin-dashboard__footer,
.admin-dashboard__note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 1rem;
  gap: 1rem;
}

.admin-dashboard__error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-dashboard__footer {
  margin-top: 1.1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}

.admin-dashboard__footer strong {
  display: block;
  color: var(--text-strong);
  margin-top: 0.2rem;
}

.admin-dashboard__links {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.admin-dashboard__link {
  padding: 0.55rem 0.85rem;
  border-radius: 999px;
  color: var(--primary);
  background: var(--bg-soft);
}

.admin-dashboard__notes {
  display: grid;
  gap: 0.85rem;
  margin-top: 1rem;
}

.admin-dashboard__note {
  align-items: flex-start;
  padding: 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.admin-dashboard__note strong {
  display: block;
  margin-bottom: 0.3rem;
}

.admin-dashboard__note p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.65;
}

@media (max-width: 1280px) {
  .admin-dashboard__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .admin-dashboard__stats {
    grid-template-columns: 1fr;
  }
}
</style>
