<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Plus, Save } from 'lucide-vue-next'

import {
  createAdminOptionItem,
  fetchAdminOptionGroupItems,
  fetchAdminOptionGroups,
  updateAdminOptionItem,
} from '../api/admin'
import EmptyState from '../components/common/EmptyState.vue'
import type {
  AdminCreateOptionItemRequest,
  AdminOptionGroup,
  AdminOptionItem,
  AdminUpdateOptionItemRequest,
} from '../types/admin'

interface ItemDraft {
  id: string | number
  code: string
  itemLabel: string
  itemSubLabel: string
  description: string
  iconName: string
  sortOrder: number
  enabled: boolean
  query: string
  builtin: boolean
}

interface GroupCategory {
  key: string
  title: string
  description: string
  groups: AdminOptionGroup[]
}

const ICON_OPTIONS = [
  'Accessibility',
  'Activity',
  'Backpack',
  'Bike',
  'Compass',
  'Footprints',
  'Map',
  'Mountain',
  'Package2',
  'PawPrint',
  'Route',
  'Scale',
  'Sprout',
  'Trees',
] as const

const groups = ref<AdminOptionGroup[]>([])
const selectedGroupCode = ref('')
const items = ref<AdminOptionItem[]>([])
const drafts = ref<ItemDraft[]>([])
const loadingGroups = ref(false)
const loadingItems = ref(false)
const savingId = ref<string | number | null>(null)
const creating = ref(false)
const errorMessage = ref('')
const newItem = ref<ItemDraft>({
  id: 'new',
  code: '',
  itemLabel: '',
  itemSubLabel: '',
  description: '',
  iconName: 'Footprints',
  sortOrder: 99,
  enabled: true,
  query: '',
  builtin: false,
})

const selectedGroup = computed(() => groups.value.find((group) => group.groupCode === selectedGroupCode.value) ?? null)
const groupCategories = computed<GroupCategory[]>(() => {
  const allGroups = groups.value
  return [
    {
      key: 'hiking-profile',
      title: '徒步画像',
      description: '管理用户画像中的经验、路线偏好和负重偏好展示项。',
      groups: allGroups.filter((group) => [
        'hiking_experience_level',
        'hiking_trail_style',
        'hiking_pack_preference',
      ].includes(group.groupCode)),
    },
    {
      key: 'home-activity',
      title: '首页活动探索',
      description: '管理首页底部按活动探索卡片，包括名称、图标、排序和搜索预设词。',
      groups: allGroups.filter((group) => group.groupCode === 'home_activity'),
    },
    {
      key: 'search-config',
      title: '搜索配置',
      description: '管理搜索页的难度、长度、装备和耗时筛选展示项。',
      groups: allGroups.filter((group) => [
        'search_difficulty',
        'search_distance',
        'search_pack_type',
        'search_duration_type',
      ].includes(group.groupCode)),
    },
  ].filter((category) => category.groups.length > 0)
})

watch(selectedGroupCode, (groupCode) => {
  if (!groupCode) {
    items.value = []
    drafts.value = []
    return
  }
  void loadItems(groupCode)
})

function toDraft(item: AdminOptionItem): ItemDraft {
  return {
    id: item.id,
    code: item.code,
    itemLabel: item.label,
    itemSubLabel: item.subLabel ?? '',
    description: item.description ?? '',
    iconName: item.icon ?? '',
    sortOrder: item.sort,
    enabled: item.enabled,
    query: typeof item.extra?.query === 'string' ? item.extra.query : '',
    builtin: item.builtin,
  }
}

function resetCreateForm() {
  newItem.value = {
    id: 'new',
    code: '',
    itemLabel: '',
    itemSubLabel: '',
    description: '',
    iconName: 'Footprints',
    sortOrder: items.value.length + 1,
    enabled: true,
    query: '',
    builtin: false,
  }
}

async function loadGroups() {
  loadingGroups.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminOptionGroups()
    groups.value = result
    if (!selectedGroupCode.value && result.length) {
      const firstGroup = result[0]
      if (firstGroup) {
        selectedGroupCode.value = firstGroup.groupCode
      }
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '配置分组加载失败'
  } finally {
    loadingGroups.value = false
  }
}

async function loadItems(groupCode = selectedGroupCode.value) {
  if (!groupCode) {
    return
  }
  loadingItems.value = true
  errorMessage.value = ''
  try {
    const result = await fetchAdminOptionGroupItems(groupCode)
    items.value = result
    drafts.value = result.map(toDraft)
    resetCreateForm()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '配置项加载失败'
  } finally {
    loadingItems.value = false
  }
}

async function saveDraft(draft: ItemDraft) {
  if (!selectedGroup.value) {
    return
  }
  savingId.value = draft.id
  errorMessage.value = ''
  try {
    const payload: AdminUpdateOptionItemRequest = {
      itemLabel: draft.itemLabel.trim(),
      itemSubLabel: draft.itemSubLabel.trim() || undefined,
      description: draft.description.trim() || undefined,
      iconName: draft.iconName.trim() || undefined,
      sortOrder: Number(draft.sortOrder),
      enabled: draft.enabled,
      extra: draft.query.trim() ? { query: draft.query.trim() } : {},
    }
    await updateAdminOptionItem(selectedGroup.value.groupCode, draft.id, payload)
    await loadItems(selectedGroup.value.groupCode)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '配置项保存失败'
  } finally {
    savingId.value = null
  }
}

async function createItem() {
  if (!selectedGroup.value) {
    return
  }
  creating.value = true
  errorMessage.value = ''
  try {
    const payload: AdminCreateOptionItemRequest = {
      itemCode: newItem.value.code.trim(),
      itemLabel: newItem.value.itemLabel.trim(),
      itemSubLabel: newItem.value.itemSubLabel.trim() || undefined,
      description: newItem.value.description.trim() || undefined,
      iconName: newItem.value.iconName.trim() || undefined,
      sortOrder: Number(newItem.value.sortOrder),
      enabled: newItem.value.enabled,
      extra: newItem.value.query.trim() ? { query: newItem.value.query.trim() } : {},
    }
    await createAdminOptionItem(selectedGroup.value.groupCode, payload)
    await loadGroups()
    await loadItems(selectedGroup.value.groupCode)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '配置项创建失败'
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  void loadGroups()
})
</script>

<template>
  <div class="settings-view">
    <header class="settings-header">
      <h1 class="page-title">配置中心</h1>
      <p class="page-subtitle">按业务模块查看和维护配置项，支持动态新增和修改选项配置。</p>
    </header>

    <div class="bento-grid">
      <!-- Sidebar / Group List -->
      <div class="bento-column bento-column--left">
        <aside class="settings-card sidebar-card">
          <h2 class="card-title">分类导航</h2>
          
          <div v-if="loadingGroups" class="loading-state">
            <RefreshCcw class="animate-spin" :size="24" />
            <p>正在加载分组...</p>
          </div>
          <div v-else class="category-list">
            <section
              v-for="category in groupCategories"
              :key="category.key"
              class="category-section"
            >
              <header class="category-header">
                <strong>{{ category.title }}</strong>
                <p>{{ category.description }}</p>
              </header>
              <button
                v-for="group in category.groups"
                :key="group.groupCode"
                type="button"
                class="group-button"
                :class="{ 'is-active': selectedGroupCode === group.groupCode }"
                @click="selectedGroupCode = group.groupCode"
              >
                <div class="group-info">
                  <strong class="group-name">{{ group.groupName }}</strong>
                  <span class="group-code">{{ group.groupCode }}</span>
                </div>
                <div class="group-count">{{ group.itemCount }} 项</div>
              </button>
            </section>
          </div>
        </aside>
      </div>

      <!-- Content Area -->
      <div class="bento-column bento-column--right">
        <template v-if="selectedGroup">
          <section class="settings-card group-header-card">
            <div class="group-header-info">
              <h2 class="card-title">{{ selectedGroup.groupName }}</h2>
              <p class="card-desc">{{ selectedGroup.description || '当前分组暂无补充说明。' }}</p>
            </div>
            <div class="status-badge" :class="selectedGroup.status === 'active' ? 'is-active' : 'is-inactive'">
              {{ selectedGroup.status === 'active' ? '启用中' : '未启用' }}
            </div>
          </section>

          <div v-if="errorMessage" class="notice-alert is-error">{{ errorMessage }}</div>

          <div v-if="loadingItems" class="empty-wrap settings-card">
            <div class="loading-state">
              <RefreshCcw class="animate-spin" :size="32" />
              <p>正在加载配置项...</p>
            </div>
          </div>

          <div v-else class="items-container">
            <!-- Create Item Form -->
            <section v-if="selectedGroup.allowCreate" class="settings-card item-card create-item-card">
              <div class="item-header">
                <h3 class="item-title">新增配置项</h3>
                <button class="btn btn--primary" type="button" :disabled="creating" @click="createItem">
                  <Plus :size="16" :stroke-width="2" />
                  {{ creating ? '创建中...' : '新增' }}
                </button>
              </div>

              <div class="controls-grid">
                <div class="input-group">
                  <label class="input-label">编码</label>
                  <input v-model="newItem.code" class="styled-input" type="text" placeholder="例如 hiking" />
                </div>
                <div class="input-group">
                  <label class="input-label">名称</label>
                  <input v-model="newItem.itemLabel" class="styled-input" type="text" placeholder="例如 徒步" />
                </div>
                <div class="input-group">
                  <label class="input-label">图标</label>
                  <select v-model="newItem.iconName" class="styled-select">
                    <option v-for="icon in ICON_OPTIONS" :key="icon" :value="icon">{{ icon }}</option>
                  </select>
                </div>
                <div class="input-group">
                  <label class="input-label">排序</label>
                  <input v-model.number="newItem.sortOrder" class="styled-input" type="number" min="1" />
                </div>
                <div class="input-group input-group--full" v-if="selectedGroup.groupCode === 'home_activity'">
                  <label class="input-label">搜索预设词</label>
                  <input v-model="newItem.query" class="styled-input" type="text" placeholder="用于首页活动入口点击后的搜索词" />
                </div>
              </div>
            </section>

            <!-- Items List -->
            <template v-if="drafts.length">
              <section v-for="draft in drafts" :key="draft.id" class="settings-card item-card">
                <div class="item-header">
                  <div class="item-info">
                    <strong class="item-title">{{ draft.itemLabel || draft.code }}</strong>
                    <span class="item-code">{{ draft.code }}</span>
                  </div>
                  <button
                    class="btn btn--primary"
                    type="button"
                    :disabled="savingId === draft.id"
                    @click="saveDraft(draft)"
                  >
                    <Save :size="16" :stroke-width="2" />
                    {{ savingId === draft.id ? '保存中...' : '保存修改' }}
                  </button>
                </div>

                <div class="controls-grid">
                  <div class="input-group">
                    <label class="input-label">名称</label>
                    <input v-model="draft.itemLabel" class="styled-input" type="text" />
                  </div>
                  <div class="input-group">
                    <label class="input-label">副文案</label>
                    <input v-model="draft.itemSubLabel" class="styled-input" type="text" />
                  </div>
                  <div class="input-group">
                    <label class="input-label">图标</label>
                    <select v-model="draft.iconName" class="styled-select">
                      <option value="">无图标</option>
                      <option v-for="icon in ICON_OPTIONS" :key="icon" :value="icon">{{ icon }}</option>
                    </select>
                  </div>
                  <div class="input-group">
                    <label class="input-label">排序</label>
                    <input v-model.number="draft.sortOrder" class="styled-input" type="number" min="1" />
                  </div>
                  <div class="input-group">
                    <label class="input-label">启用状态</label>
                    <select v-model="draft.enabled" class="styled-select">
                      <option :value="true">启用</option>
                      <option :value="false">停用</option>
                    </select>
                  </div>
                  <div class="input-group">
                    <label class="input-label">是否内建</label>
                    <input class="styled-input is-readonly" type="text" :value="draft.builtin ? '是' : '否'" readonly disabled />
                  </div>
                  <div class="input-group input-group--full">
                    <label class="input-label">说明</label>
                    <input v-model="draft.description" class="styled-input" type="text" />
                  </div>
                  <div class="input-group input-group--full" v-if="selectedGroup.groupCode === 'home_activity'">
                    <label class="input-label">搜索预设词</label>
                    <input v-model="draft.query" class="styled-input" type="text" />
                  </div>
                </div>
              </section>
            </template>
            <div v-else class="empty-wrap settings-card">
              <EmptyState
                title="当前分组暂无配置项"
                description="可以先补齐基础配置然后再接入前台展示。"
              />
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-view {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  height: 100%;
}

.settings-header {
  margin-bottom: 0.5rem;
}

.page-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 0.9375rem;
  color: var(--text-muted);
  margin: 0.5rem 0 0;
}

/* Bento Grid */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
  flex: 1;
  min-height: 0;
}

.bento-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  min-height: 0;
}

.bento-column--left { 
  grid-column: span 4; 
}
.bento-column--right { 
  grid-column: span 8; 
  overflow-y: auto;
  padding-right: 0.5rem; /* Add some padding for scrollbar */
}

/* Scrollbar styling for the right column */
.bento-column--right::-webkit-scrollbar {
  width: 6px;
}
.bento-column--right::-webkit-scrollbar-thumb {
  background-color: var(--border);
  border-radius: 10px;
}

.settings-card {
  background: white;
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid var(--border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.sidebar-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 1.5rem;
  color: var(--text-strong);
}

.card-desc {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0.25rem 0 0;
}

/* Category List */
.category-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  overflow-y: auto;
}

.category-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.category-header strong {
  display: block;
  font-size: 0.8125rem;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  margin-bottom: 0.25rem;
}

.category-header p {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-muted);
  line-height: 1.4;
}

.group-button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 1rem;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
}

.group-button:hover {
  background: var(--bg-soft);
  border-color: var(--primary-soft);
}

.group-button.is-active {
  background: color-mix(in srgb, var(--primary) 6%, transparent);
  border-color: var(--primary);
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.group-name {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--text-strong);
}
.group-button.is-active .group-name {
  color: var(--primary);
}

.group-code {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-family: monospace;
}

.group-count {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  background: var(--bg-surface);
  border-radius: 6px;
  color: var(--text-muted);
}
.group-button.is-active .group-count {
  background: white;
  color: var(--primary);
}

/* Right Column Content */
.group-header-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0;
}

.status-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.35rem 0.75rem;
  border-radius: 99px;
}
.status-badge.is-active {
  background: rgba(47, 106, 58, 0.1);
  color: #2f6a3a;
}
.status-badge.is-inactive {
  background: var(--bg-soft);
  color: var(--text-muted);
}

.items-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.item-card {
  display: flex;
  flex-direction: column;
}

.create-item-card {
  border: 1.5px dashed var(--border);
  background: var(--bg-soft);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

.item-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.item-code {
  font-family: monospace;
  font-size: 0.8125rem;
  padding: 0.2rem 0.5rem;
  background: var(--bg-surface);
  border-radius: 6px;
  color: var(--text-muted);
}

/* Form Controls */
.controls-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.input-group--full {
  grid-column: 1 / -1;
}

.input-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
}

.styled-input, .styled-select {
  background: var(--bg-soft);
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 0.6rem 1rem;
  font-size: 0.875rem;
  color: var(--text-strong);
  transition: all 0.2s;
  width: 100%;
}
.create-item-card .styled-input, 
.create-item-card .styled-select {
  background: white;
}

.styled-input:focus, .styled-select:focus {
  outline: none;
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.styled-input.is-readonly {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 10px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: all 0.2s;
  border: none;
}
.btn--primary {
  background: var(--primary);
  color: white;
}
.btn--primary:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.notice-alert {
  padding: 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}
.notice-alert.is-error {
  background: rgba(181, 68, 68, 0.08);
  color: var(--danger);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.empty-wrap, .loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--text-muted);
  gap: 1rem;
}

@media (max-width: 1024px) {
  .bento-grid {
    grid-template-columns: 1fr;
    overflow-y: auto;
  }
  .bento-column--left, .bento-column--right {
    grid-column: span 12;
    overflow-y: visible;
  }
  .sidebar-card {
    height: auto;
    max-height: 400px;
  }
}
@media (max-width: 640px) {
  .controls-grid {
    grid-template-columns: 1fr;
  }
}
</style>
