<script setup lang="ts">
import { computed, onMounted, ref, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { 
  LoaderCircle, 
  MoonStar, 
  RefreshCcw, 
  SunMedium, 
  Palette,
  Trees,
  UserPen,
  Copy,
  History,
  Save,
  RotateCcw,
  CloudUpload
} from 'lucide-vue-next'

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

function copyUrl() {
  if (heroImageUrl.value) {
    navigator.clipboard.writeText(heroImageUrl.value)
  }
}

onMounted(() => {
  void loadHeroSetting()
})
</script>

<template>
  <div class="settings-view">
    <!-- Header -->
    <header class="settings-header">
      <h1 class="page-title">设置中心</h1>
      <p class="page-subtitle">管理您的账户偏好、系统外观及全局配置。</p>
    </header>

    <!-- Bento Grid -->
    <div class="bento-grid">
      <!-- Left Column: Identity & System -->
      <div class="bento-column bento-column--left">
        <!-- Account Card -->
        <section class="settings-card account-card text-center" v-if="user">
          <div class="avatar-wrapper">
            <img v-if="user.avatarMediaUrl" :src="user.avatarMediaUrl" :alt="user.username" class="user-avatar" />
            <div v-else class="user-avatar-placeholder">{{ user.username.slice(0, 1) }}</div>
            <button class="edit-badge"><UserPen :size="14" /></button>
          </div>
          <h3 class="user-name">{{ user.username }}</h3>
          <p class="user-email">{{ user.email || 'admin@trailquest.com' }}</p>
          <div class="role-tag">系统管理员</div>
        </section>

        <!-- Appearance Card -->
        <section class="settings-card appearance-card">
          <h4 class="card-subtitle"><Palette :size="16" /> 外观设置</h4>
          <div class="theme-switch-row">
            <div class="theme-info">
              <SunMedium v-if="!themeStore.isDark" :size="20" class="theme-icon" />
              <MoonStar v-else :size="20" class="theme-icon" />
              <span class="theme-label">{{ themeStore.isDark ? '深色模式' : '浅色模式' }}</span>
            </div>
            <button 
              class="theme-toggle" 
              :class="{ 'is-active': themeStore.isDark }"
              @click="themeStore.toggle"
            >
              <div class="toggle-thumb"></div>
            </button>
          </div>
          <p class="card-hint">根据环境光线调节系统亮度，保护视力的同时也更加节能。</p>
        </section>

        <!-- System Statement -->
        <section class="settings-card statement-card">
          <div class="statement-content">
            <h3 class="statement-title">系统定位</h3>
            <p class="statement-text">
              TrailQuest Admin 是专为户外步道设计的数字化管理核心。通过实时数据流，我们旨在为管理者提供“数字巡林”的一站式决策支持。
            </p>
          </div>
          <Trees :size="120" class="statement-icon" />
        </section>
      </div>

      <!-- Right Column: Hero Management -->
      <div class="bento-column bento-column--right">
        <section class="settings-card hero-management">
          <div class="card-header">
            <div>
              <h2 class="card-title">首页大屏影像管理</h2>
              <p class="card-desc">管理移动端及网页端首页的视觉引导区域</p>
            </div>
            <div class="header-tools">
              <button class="tool-btn" @click="loadHeroSetting" :disabled="heroLoading"><RefreshCcw :size="18" /></button>
              <button class="tool-btn"><History :size="18" /></button>
            </div>
          </div>

          <!-- Preview -->
          <div class="preview-stage group">
            <img v-if="heroImageUrl" :src="heroImageUrl" alt="Hero Preview" class="preview-img" />
            <div v-else class="preview-empty">
              <CloudUpload :size="48" />
              <p>暂未设置自定义大图</p>
            </div>
            <div class="preview-overlay">
              <div class="preview-labels">
                <span class="preview-tag">实时预览</span>
              </div>
              <div class="preview-content">
                <h4 class="preview-headline">纵横群山，探索未知境界</h4>
              </div>
            </div>
            <div class="oss-status">OSS 在线</div>
          </div>

          <!-- Controls -->
          <div class="controls-grid">
            <div class="input-group">
              <label class="input-label">资源 URL</label>
              <div class="input-wrapper">
                <input 
                  type="text" 
                  class="styled-input" 
                  readonly 
                  :value="heroImageUrl || '等待上传成功...'"
                  placeholder="https://oss.trailquest.com/..."
                />
                <button class="copy-btn" @click="copyUrl"><Copy :size="16" /></button>
              </div>
            </div>

            <div class="input-group">
              <label class="input-label">上传状态</label>
              <div class="status-panel">
                <div class="status-info">
                  <span class="status-badge" :class="{ 'is-uploaded': heroImageUrl }">
                    {{ heroUploading ? '上传中' : (heroImageUrl ? '上传成功' : '待上传') }}
                  </span>
                  <span class="status-percent">{{ heroUploading ? `${heroUploadProgress}%` : (heroImageUrl ? '100%' : '0%') }}</span>
                </div>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: heroUploading ? `${heroUploadProgress}%` : (heroImageUrl ? '100%' : '0%') }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Upload Area -->
          <div class="upload-zone" @click="openHeroFilePicker">
            <input 
              ref="heroFileInput" 
              type="file" 
              class="hidden" 
              accept="image/*" 
              @change="handleHeroFileChange"
            />
            <div class="upload-icon">
              <CloudUpload v-if="!heroUploading" :size="32" />
              <LoaderCircle v-else :size="32" class="animate-spin" />
            </div>
            <div class="upload-text">
              <p class="primary-text">点击或拖拽新影像至此处</p>
              <p class="secondary-text">支持 JPG, PNG, WebP (最大 5MB)，建议 16:9</p>
            </div>
          </div>

          <!-- Actions -->
          <div class="action-footer">
            <button class="reset-link" @click="resetHeroSetting">
              <RotateCcw :size="14" /> 恢复出厂默认值
            </button>
            <div class="action-buttons">
              <button class="btn btn--secondary" @click="router.push({ name: 'config-center' })">进入配置中心</button>
              <button class="btn btn--primary" @click="saveHeroSetting" :disabled="heroSaving">
                <Save :size="18" /> 保存全局配置
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- Footer Meta -->
    <footer class="settings-footer">
      <div class="copyright">© 2026 TRAILQUEST DIGITAL ECOSYSTEM</div>
      <div class="footer-links">
        <a href="#">隐私协议</a>
        <div class="system-status">
          <span class="status-dot"></span>
          系统状态：良好
        </div>
      </div>
    </footer>

    <AdminNoticeDialog
      v-model:show="heroDialogShow"
      title="更新成功"
      message="首页大屏配置已同步，前台页面刷新后即可看到最新视觉。"
    />
  </div>
</template>

<style scoped>
.settings-view {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.page-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 1rem;
  color: var(--text-muted);
  margin: 0.5rem 0 0;
}

/* Bento Grid */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
}

.bento-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.bento-column--left { grid-column: span 4; }
.bento-column--right { grid-column: span 8; }

.settings-card {
  background: white;
  border-radius: 24px;
  padding: 1.5rem;
  border: 1px solid var(--border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

/* Account Card */
.account-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 1.25rem;
}

.user-avatar, .user-avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--bg-soft);
  box-shadow: var(--shadow);
}

.user-avatar-placeholder {
  display: grid;
  place-items: center;
  background: var(--primary);
  color: white;
  font-size: 2.5rem;
  font-weight: 800;
}

.edit-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  display: grid;
  place-items: center;
  border: 3px solid white;
  cursor: pointer;
}

.user-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0;
}

.user-email {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0.5rem 0 1rem;
}

.role-tag {
  background: rgba(47, 106, 58, 0.1);
  color: #2f6a3a;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

/* Appearance Card */
.card-subtitle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  margin-bottom: 1.5rem;
}

.theme-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-soft);
  padding: 1rem;
  border-radius: 16px;
}

.theme-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.theme-icon { color: var(--primary); }
.theme-label { font-weight: 600; font-size: 0.9375rem; }

.theme-toggle {
  width: 48px;
  height: 24px;
  background: #cbd5e1;
  border-radius: 999px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.theme-toggle.is-active { background: var(--primary); }

.toggle-thumb {
  position: absolute;
  left: 3px;
  top: 3px;
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.is-active .toggle-thumb { transform: translateX(24px); }

.card-hint {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin-top: 1rem;
  line-height: 1.6;
}

/* Statement Card */
.statement-card {
  background: var(--primary);
  color: white;
  position: relative;
  overflow: hidden;
  height: 200px;
}

.statement-content { position: relative; z-index: 2; }
.statement-title { font-size: 1.125rem; font-weight: 700; margin-bottom: 0.75rem; }
.statement-text { font-size: 0.875rem; line-height: 1.6; opacity: 0.9; }

.statement-icon {
  position: absolute;
  right: -20px;
  bottom: -20px;
  opacity: 0.1;
  transform: rotate(-10deg);
}

/* Hero Management */
.hero-management {
  padding: 2.5rem;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.card-title { font-size: 1.5rem; font-weight: 700; color: var(--text-strong); margin: 0; }
.card-desc { font-size: 0.9375rem; color: var(--text-muted); margin: 0.25rem 0 0; }

.header-tools { display: flex; gap: 0.5rem; }
.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  display: grid;
  place-items: center;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}
.tool-btn:hover { background: white; color: var(--primary); border-color: var(--primary-soft); }

/* Preview Stage */
.preview-stage {
  aspect-ratio: 21 / 10;
  background: var(--bg-soft);
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  border: 4px solid var(--bg-soft);
  margin-bottom: 2rem;
}

.preview-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.6s ease; }
.preview-stage:hover .preview-img { transform: scale(1.05); }

.preview-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  color: var(--text-muted);
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 60%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 2rem;
  color: white;
}

.preview-tag {
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(8px);
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.625rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.preview-headline { font-size: 1.25rem; font-weight: 700; margin-top: 0.5rem; }

.oss-status {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(255,255,255,0.8);
  color: var(--primary);
  font-size: 0.625rem;
  font-weight: 800;
  padding: 0.25rem 0.6rem;
  border-radius: 99px;
  text-transform: uppercase;
}

/* Controls Grid */
.controls-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.input-label {
  display: block;
  font-size: 0.6875rem;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--text-muted);
  letter-spacing: 0.1em;
  margin-bottom: 0.6rem;
}

.input-wrapper {
  display: flex;
  gap: 0.5rem;
}

.styled-input {
  flex: 1;
  background: var(--bg-soft);
  border: none;
  border-radius: 12px;
  padding: 0.75rem 1rem;
  font-size: 0.8125rem;
  font-family: monospace;
  color: var(--text-strong);
}

.copy-btn {
  width: 44px;
  background: var(--bg-soft);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: grid;
  place-items: center;
  color: var(--text-muted);
}
.copy-btn:hover { background: var(--border); color: var(--text-strong); }

/* Progress Panel */
.status-panel { display: flex; flex-direction: column; gap: 0.5rem; }
.status-info { display: flex; justify-content: space-between; align-items: center; }

.status-badge {
  font-size: 0.6875rem;
  font-weight: 800;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  background: var(--bg-soft);
  color: var(--text-muted);
}
.status-badge.is-uploaded { background: rgba(47, 106, 58, 0.12); color: #2f6a3a; }

.status-percent { font-size: 0.75rem; font-weight: 700; color: var(--text-strong); }

.progress-bar {
  height: 6px;
  background: var(--bg-soft);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  transition: width 0.3s ease;
}

/* Upload Zone */
.upload-zone {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  border: 2px dashed var(--border);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(var(--primary-rgb), 0.02);
}

.upload-zone:hover {
  background: var(--bg-soft);
  border-color: var(--primary-soft);
}

.upload-icon {
  width: 60px;
  height: 60px;
  background: white;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--primary);
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.upload-text .primary-text { font-weight: 700; font-size: 0.9375rem; margin: 0; }
.upload-text .secondary-text { font-size: 0.75rem; color: var(--text-muted); margin: 0.25rem 0 0; }

/* Action Footer */
.action-footer {
  margin-top: auto;
  padding-top: 2rem;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reset-link {
  background: none;
  border: none;
  color: var(--danger);
  font-size: 0.8125rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  cursor: pointer;
}
.reset-link:hover { text-decoration: underline; }

.action-buttons { display: flex; gap: 1rem; }

.btn {
  padding: 0.75rem 1.75rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  transition: all 0.2s;
  border: none;
}

.btn--primary {
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 12px rgba(var(--primary-rgb), 0.2);
}
.btn--primary:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(var(--primary-rgb), 0.3); }

.btn--secondary { background: var(--bg-soft); color: var(--text-strong); }
.btn--secondary:hover { background: var(--border); }

/* Footer */
.settings-footer {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.625rem;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.1em;
}

.footer-links { display: flex; gap: 1.5rem; align-items: center; }
.footer-links a:hover { color: var(--primary); }

.system-status { display: flex; align-items: center; gap: 0.4rem; }
.status-dot { width: 6px; height: 6px; background: #2f6a3a; border-radius: 50%; }

@media (max-width: 1024px) {
  .bento-grid { grid-template-columns: 1fr; }
  .bento-column--left, .bento-column--right { grid-column: span 12; }
}

@media (max-width: 640px) {
  .controls-grid { grid-template-columns: 1fr; }
  .action-footer { flex-direction: column; gap: 1.5rem; align-items: flex-start; }
}
</style>
