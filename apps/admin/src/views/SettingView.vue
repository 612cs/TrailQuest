<script setup lang="ts">
import { computed, onMounted, ref, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { LoaderCircle, MoonStar, RefreshCcw, SunMedium, Upload, UserCircle2, Settings2 } from 'lucide-vue-next'

import { fetchAdminHomeHeroSetting, updateAdminHomeHeroSetting } from '../api/admin'
import AdminNoticeDialog from '../components/common/AdminNoticeDialog.vue'
import { useAdminAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import { pinia } from '../stores/pinia'
import { mediaUrlOf, resolveUploadErrorMessage, uploadAdminImage } from '../utils/adminUpload'

const authStore = useAdminAuthStore(pinia)
const themeStore = useThemeStore(pinia)
const router = useRouter()

const user = computed(() => authStore.user)
const heroImageUrl = ref('')
const heroLoading = ref(false)
const heroSaving = ref(false)
const heroUploading = ref(false)
const heroUploadProgress = ref(0)
const heroError = ref('')
const heroDialogShow = ref(false)
const heroFileInput = useTemplateRef<HTMLInputElement>('heroFileInput')

async function loadHeroSetting() {
  heroLoading.value = true
  heroError.value = ''
  try {
    const result = await fetchAdminHomeHeroSetting()
    heroImageUrl.value = result.imageUrl?.trim() || ''
  } catch (error) {
    heroError.value = error instanceof Error ? error.message : '首页大图配置加载失败'
  } finally {
    heroLoading.value = false
  }
}

async function saveHeroSetting() {
  heroSaving.value = true
  heroError.value = ''
  try {
    await updateAdminHomeHeroSetting({ imageUrl: heroImageUrl.value.trim() || undefined })
    heroDialogShow.value = true
  } catch (error) {
    heroError.value = error instanceof Error ? error.message : '首页大图保存失败'
  } finally {
    heroSaving.value = false
  }
}

function openHeroFilePicker() {
  heroFileInput.value?.click()
}

async function handleHeroFileChange(event: Event) {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (!file) {
    return
  }

  heroError.value = ''
  heroUploading.value = true
  heroUploadProgress.value = 0

  try {
    const remote = await uploadAdminImage(file, 'home_hero', (progress) => {
      heroUploadProgress.value = progress
    })
    heroImageUrl.value = mediaUrlOf(remote)
    await updateAdminHomeHeroSetting({ imageUrl: heroImageUrl.value })
    heroDialogShow.value = true
  } catch (error) {
    heroError.value = resolveUploadErrorMessage(error)
  } finally {
    heroUploading.value = false
    heroUploadProgress.value = 0
    if (target) {
      target.value = ''
    }
  }
}

function resetHeroSetting() {
  heroImageUrl.value = ''
  void saveHeroSetting()
}

onMounted(() => {
  void loadHeroSetting()
})
</script>

<template>
  <div class="admin-grid-2">
    <section class="admin-card admin-section">
      <h2 class="admin-title">账户信息</h2>
      <p class="admin-subtitle">当前后台登录状态与账号概览。</p>

      <div class="admin-setting-account" v-if="user">
        <div class="admin-setting-account__avatar">
          <img v-if="user.avatarMediaUrl" :src="user.avatarMediaUrl" :alt="user.username" />
          <span v-else>{{ user.username.slice(0, 2).toUpperCase() }}</span>
        </div>
        <div>
          <strong>{{ user.username }}</strong>
          <p>{{ user.email || '暂无邮箱' }}</p>
          <span class="admin-badge admin-badge-approved">管理员</span>
        </div>
      </div>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">外观设置</h2>
      <p class="admin-subtitle">TrailQuest 绿色主题，支持浅色与深色。</p>

      <button class="admin-button admin-button-secondary admin-setting-theme" type="button" @click="themeStore.toggle">
        <SunMedium v-if="themeStore.isDark === false" :size="18" :stroke-width="2" />
        <MoonStar v-else :size="18" :stroke-width="2" />
        切换为{{ themeStore.themeLabel === '浅色' ? '深色' : '浅色' }}模式
      </button>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">后台说明</h2>
      <div class="admin-setting-note">
        <UserCircle2 :size="18" :stroke-width="2" />
        <span>后台登录继续复用前台 /api/auth/login 与 /api/auth/me。</span>
      </div>
      <div class="admin-setting-note">
        <Settings2 :size="18" :stroke-width="2" />
        <span>当前版本聚焦路线审核、评论治理与举报占位。</span>
      </div>
    </section>

    <section class="admin-card admin-section">
      <h2 class="admin-title">配置中心</h2>
      <p class="admin-subtitle">统一管理首页活动入口、搜索筛选项和徒步画像展示项。</p>

      <div class="admin-setting-note">
        <Settings2 :size="18" :stroke-width="2" />
        <span>支持调整展示名称、图标、排序和启用状态，不改变底层语义 code。</span>
      </div>

      <button class="admin-button admin-button-primary admin-setting-theme" type="button" @click="router.push({ name: 'config-center' })">
        进入配置中心
      </button>
    </section>

    <section class="admin-card admin-section admin-grid-2 admin-setting-hero">
      <div>
        <div class="admin-setting-hero__header">
          <div>
            <h2 class="admin-title">首页大屏图片</h2>
            <p class="admin-subtitle">从本地选择图片后会自动上传到阿里云 OSS，并将图片地址设置为首页大图。</p>
          </div>
          <button class="admin-button admin-button-secondary" type="button" :disabled="heroLoading || heroSaving || heroUploading" @click="loadHeroSetting">
            <RefreshCcw :size="16" :stroke-width="2" />
            刷新
          </button>
        </div>

        <label class="admin-setting-field">
          <span>上传首页大图</span>
          <input
            ref="heroFileInput"
            class="admin-file-input"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            @change="handleHeroFileChange"
          />
          <button
            class="admin-button admin-button-primary admin-setting-upload"
            type="button"
            :disabled="heroUploading || heroSaving"
            @click="openHeroFilePicker"
          >
            <LoaderCircle v-if="heroUploading" class="admin-spin" :size="16" :stroke-width="2" />
            <Upload v-else :size="16" :stroke-width="2" />
            {{ heroUploading ? `上传中 ${heroUploadProgress}%` : '选择图片并上传' }}
          </button>
          <small class="admin-setting-hint">支持 JPG、PNG、WEBP。上传成功后会自动写入首页配置，不需要手动复制链接。</small>
        </label>

        <label class="admin-setting-field">
          <span>当前 OSS 地址</span>
          <input
            v-model="heroImageUrl"
            class="admin-input"
            type="text"
            readonly
            placeholder="上传成功后会自动显示 OSS 图片地址"
          />
        </label>

        <div v-if="heroError" class="admin-setting-error">{{ heroError }}</div>

        <div class="admin-setting-actions">
          <button class="admin-button admin-button-secondary" type="button" :disabled="heroSaving || heroUploading" @click="resetHeroSetting">
            恢复默认
          </button>
        </div>
      </div>

      <div class="admin-setting-hero__preview">
        <h3>实时预览</h3>
        <div v-if="heroImageUrl" class="admin-setting-hero__image">
          <img :src="heroImageUrl" alt="首页大屏预览" />
        </div>
        <div v-else class="admin-setting-hero__empty">
          当前未配置额外大图，前台会继续使用默认首屏图片。
        </div>
      </div>
    </section>
  </div>

  <AdminNoticeDialog
    v-model:show="heroDialogShow"
    title="保存成功"
    message="首页大屏图片已经上传并更新，前台首页刷新后会显示最新图片。"
  />
</template>

<style scoped>
.admin-setting-account {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.admin-setting-account__avatar {
  width: 3.5rem;
  height: 3.5rem;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid var(--border);
  color: var(--primary);
  background: var(--bg-elevated);
  font-weight: 800;
}

.admin-setting-account__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-setting-account strong {
  display: block;
  color: var(--text-strong);
}

.admin-setting-account p {
  margin: 0.25rem 0 0.55rem;
  color: var(--text-muted);
}

.admin-setting-theme {
  margin-top: 1rem;
}

.admin-setting-note {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  margin-top: 1rem;
  padding: 0.95rem 1rem;
  border-radius: 16px;
  background: var(--bg-soft);
  color: var(--text-muted);
  line-height: 1.7;
}

.admin-setting-hero {
  grid-column: 1 / -1;
  align-items: start;
}

.admin-setting-hero__header,
.admin-setting-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-setting-field {
  display: grid;
  gap: 0.55rem;
  margin-top: 1rem;
}

.admin-setting-field span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-file-input {
  display: none;
}

.admin-setting-upload {
  width: fit-content;
}

.admin-setting-hint {
  color: var(--text-dim);
  line-height: 1.6;
}

.admin-setting-actions {
  justify-content: flex-start;
  margin-top: 1rem;
}

.admin-setting-error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-setting-hero__preview h3 {
  margin: 0 0 0.8rem;
  color: var(--text-strong);
}

.admin-setting-hero__image,
.admin-setting-hero__empty {
  min-height: 260px;
  border-radius: 22px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
}

.admin-setting-hero__image {
  overflow: hidden;
}

.admin-setting-hero__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-setting-hero__empty {
  display: grid;
  place-items: center;
  padding: 1.4rem;
  color: var(--text-muted);
  text-align: center;
  line-height: 1.8;
}

.admin-spin {
  animation: admin-rotate 1s linear infinite;
}

@media (max-width: 900px) {
  .admin-setting-hero {
    grid-template-columns: 1fr;
  }

  .admin-setting-hero__header {
    align-items: flex-start;
    flex-direction: column;
  }
}

@keyframes admin-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
