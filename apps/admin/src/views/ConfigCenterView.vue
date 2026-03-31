<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowLeft, Plus, RefreshCcw, Save } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

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

const router = useRouter()
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
  <section class="admin-section admin-config">
    <div class="admin-card admin-config__header">
      <div>
        <p class="admin-config__eyebrow">TRAILQUEST CONFIG CENTER</p>
        <h1 class="admin-title">配置中心</h1>
        <p class="admin-subtitle">统一管理首页活动入口、搜索筛选项和徒步画像展示项。</p>
      </div>
      <div class="admin-config__header-actions">
        <button class="admin-button admin-button-secondary" type="button" @click="router.push({ name: 'settings' })">
          <ArrowLeft :size="16" :stroke-width="2" />
          返回设置
        </button>
        <button class="admin-button admin-button-secondary" type="button" :disabled="loadingGroups || loadingItems" @click="loadGroups">
          <RefreshCcw :size="16" :stroke-width="2" />
          刷新
        </button>
      </div>
    </div>

    <div class="admin-config__layout">
      <aside class="admin-card admin-config__sidebar">
        <h2 class="admin-title">配置分组</h2>
        <p class="admin-subtitle">先选择分组，再编辑具体配置项。</p>

        <div v-if="loadingGroups" class="admin-muted">正在加载分组...</div>
        <button
          v-for="group in groups"
          :key="group.groupCode"
          type="button"
          class="admin-config__group"
          :class="{ active: selectedGroupCode === group.groupCode }"
          @click="selectedGroupCode = group.groupCode"
        >
          <strong>{{ group.groupName }}</strong>
          <span>{{ group.groupCode }}</span>
          <small>{{ group.itemCount }} 项</small>
        </button>
      </aside>

      <div class="admin-card admin-config__content">
        <template v-if="selectedGroup">
          <div class="admin-config__content-header">
            <div>
              <h2 class="admin-title">{{ selectedGroup.groupName }}</h2>
              <p class="admin-subtitle">{{ selectedGroup.description || '当前分组暂无补充说明。' }}</p>
            </div>
            <span class="admin-badge" :class="selectedGroup.status === 'active' ? 'admin-badge-approved' : 'admin-badge-neutral'">
              {{ selectedGroup.status === 'active' ? '启用中' : '未启用' }}
            </span>
          </div>

          <div v-if="errorMessage" class="admin-list-error">{{ errorMessage }}</div>

          <div v-if="loadingItems" class="admin-empty-wrap">
            <div class="admin-muted">正在加载配置项...</div>
          </div>

          <div v-else class="admin-config__items">
            <div v-if="selectedGroup.allowCreate" class="admin-panel admin-config__create">
              <div class="admin-config__create-header">
                <h3>新增配置项</h3>
                <button class="admin-button admin-button-primary" type="button" :disabled="creating" @click="createItem">
                  <Plus :size="16" :stroke-width="2" />
                  {{ creating ? '创建中...' : '新增' }}
                </button>
              </div>

              <div class="admin-config__grid">
                <label class="admin-setting-field">
                  <span>编码</span>
                  <input v-model="newItem.code" class="admin-input" type="text" placeholder="例如 hiking" />
                </label>
                <label class="admin-setting-field">
                  <span>名称</span>
                  <input v-model="newItem.itemLabel" class="admin-input" type="text" placeholder="例如 徒步" />
                </label>
                <label class="admin-setting-field">
                  <span>图标</span>
                  <select v-model="newItem.iconName" class="admin-select">
                    <option v-for="icon in ICON_OPTIONS" :key="icon" :value="icon">{{ icon }}</option>
                  </select>
                </label>
                <label class="admin-setting-field">
                  <span>排序</span>
                  <input v-model.number="newItem.sortOrder" class="admin-input" type="number" min="1" />
                </label>
                <label class="admin-setting-field admin-setting-field--full">
                  <span>搜索预设词</span>
                  <input v-model="newItem.query" class="admin-input" type="text" placeholder="用于首页活动入口点击后的搜索词" />
                </label>
              </div>
            </div>

            <EmptyState
              v-if="!drafts.length"
              title="当前分组暂无配置项"
              description="可以先补齐基础配置后再接入前台展示。"
            />

            <div v-else class="admin-config__item-list">
              <article v-for="draft in drafts" :key="draft.id" class="admin-panel admin-config__item">
                <div class="admin-config__item-header">
                  <div>
                    <strong>{{ draft.itemLabel || draft.code }}</strong>
                    <p>{{ draft.code }}</p>
                  </div>
                  <button
                    class="admin-button admin-button-primary"
                    type="button"
                    :disabled="savingId === draft.id"
                    @click="saveDraft(draft)"
                  >
                    <Save :size="16" :stroke-width="2" />
                    {{ savingId === draft.id ? '保存中...' : '保存' }}
                  </button>
                </div>

                <div class="admin-config__grid">
                  <label class="admin-setting-field">
                    <span>名称</span>
                    <input v-model="draft.itemLabel" class="admin-input" type="text" />
                  </label>
                  <label class="admin-setting-field">
                    <span>副文案</span>
                    <input v-model="draft.itemSubLabel" class="admin-input" type="text" />
                  </label>
                  <label class="admin-setting-field">
                    <span>图标</span>
                    <select v-model="draft.iconName" class="admin-select">
                      <option value="">无图标</option>
                      <option v-for="icon in ICON_OPTIONS" :key="icon" :value="icon">{{ icon }}</option>
                    </select>
                  </label>
                  <label class="admin-setting-field">
                    <span>排序</span>
                    <input v-model.number="draft.sortOrder" class="admin-input" type="number" min="1" />
                  </label>
                  <label class="admin-setting-field">
                    <span>启用状态</span>
                    <select v-model="draft.enabled" class="admin-select">
                      <option :value="true">启用</option>
                      <option :value="false">停用</option>
                    </select>
                  </label>
                  <label class="admin-setting-field">
                    <span>是否内建</span>
                    <input class="admin-input" type="text" :value="draft.builtin ? '是' : '否'" readonly />
                  </label>
                  <label class="admin-setting-field admin-setting-field--full">
                    <span>说明</span>
                    <input v-model="draft.description" class="admin-input" type="text" />
                  </label>
                  <label class="admin-setting-field admin-setting-field--full" v-if="selectedGroup.groupCode === 'home_activity'">
                    <span>搜索预设词</span>
                    <input v-model="draft.query" class="admin-input" type="text" />
                  </label>
                </div>
              </article>
            </div>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.admin-config {
  gap: 1rem;
  height: 100%;
}

.admin-config__header,
.admin-config__sidebar,
.admin-config__content {
  padding: 1.2rem 1.3rem;
}

.admin-config__header,
.admin-config__content-header,
.admin-config__header-actions,
.admin-config__create-header,
.admin-config__item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-config__eyebrow {
  margin: 0 0 0.35rem;
  font-size: 0.72rem;
  letter-spacing: 0.32em;
  color: var(--text-muted);
}

.admin-config__layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 18rem minmax(0, 1fr);
  gap: 1rem;
}

.admin-config__sidebar,
.admin-config__content {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.admin-config__group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.2rem;
  width: 100%;
  margin-top: 0.75rem;
  padding: 0.9rem 1rem;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: transparent;
  color: var(--text-strong);
  cursor: pointer;
}

.admin-config__group span,
.admin-config__group small,
.admin-config__item-header p {
  color: var(--text-muted);
}

.admin-config__group.active {
  border-color: color-mix(in srgb, var(--primary) 35%, transparent);
  background: color-mix(in srgb, var(--primary) 10%, transparent);
}

.admin-config__items,
.admin-config__item-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 0;
}

.admin-config__items {
  flex: 1;
  margin-top: 1rem;
  overflow: auto;
}

.admin-config__create,
.admin-config__item {
  padding: 1rem;
}

.admin-config__create-header h3,
.admin-config__item-header strong {
  margin: 0;
  color: var(--text-strong);
}

.admin-config__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.9rem 1rem;
  margin-top: 1rem;
}

.admin-setting-field {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.admin-setting-field span {
  color: var(--text-muted);
  font-size: 0.88rem;
}

.admin-setting-field--full {
  grid-column: 1 / -1;
}

.admin-list-error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-empty-wrap {
  flex: 1;
  display: grid;
  place-items: center;
}

@media (max-width: 1200px) {
  .admin-config__layout {
    grid-template-columns: 1fr;
  }

  .admin-config__grid {
    grid-template-columns: 1fr;
  }

  .admin-config__header,
  .admin-config__content-header,
  .admin-config__header-actions,
  .admin-config__create-header,
  .admin-config__item-header {
    flex-wrap: wrap;
  }
}
</style>
