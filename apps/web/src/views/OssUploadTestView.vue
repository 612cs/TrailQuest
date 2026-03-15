<script setup lang="ts">
import { computed, shallowRef } from 'vue'
import { useRouter } from 'vue-router'

import BaseIcon from '../components/common/BaseIcon.vue'
import { useOssUploadTest } from '../composables/useOssUploadTest'
import { useUserStore } from '../stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()
const {
  selectedFile,
  localPreviewUrl,
  uploadResult,
  isUploading,
  uploadProgress,
  errorMessage,
  canUpload,
  setFile,
  upload,
} = useOssUploadTest()

const selectedBizType = shallowRef<'avatar' | 'trail_cover' | 'trail_gallery' | 'review'>('avatar')
const successMessage = shallowRef('')

const bizTypeOptions = [
  { value: 'avatar' as const, label: '头像' },
  { value: 'trail_cover' as const, label: '路线封面' },
  { value: 'trail_gallery' as const, label: '路线相册' },
  { value: 'review' as const, label: '评论图片' },
]

const uploadButtonLabel = computed(() => {
  if (isUploading.value) {
    return `上传中 ${uploadProgress.value}%`
  }
  return '开始上传'
})

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  setFile(input.files?.[0] ?? null)
  successMessage.value = ''
  input.value = ''
}

async function handleUpload() {
  if (!canUpload.value) return
  successMessage.value = ''
  try {
    const remote = await upload(selectedBizType.value)
    successMessage.value = `上传完成，mediaId=${remote.mediaId}`
  } catch {
    successMessage.value = ''
  }
}
</script>

<template>
  <main class="min-h-screen">
    <div class="glass-header sticky top-14 sm:top-16 z-40 px-4 py-3">
      <div class="max-w-3xl mx-auto flex items-center justify-between">
        <button
          @click="router.back()"
          class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500"
          style="color: var(--text-secondary)"
        >
          <BaseIcon name="ChevronLeft" :size="20" />
          返回
        </button>
        <h2 class="text-sm font-semibold" style="color: var(--text-primary)">OSS 上传测试</h2>
        <div class="w-12" />
      </div>
    </div>

    <div class="max-w-3xl mx-auto px-4 sm:px-6 py-6 space-y-5">
      <section class="card p-5 space-y-4 animate-fade-in-up">
        <div>
          <h1 class="text-xl font-bold" style="color: var(--text-primary)">最小上传验证</h1>
          <p class="text-sm mt-1" style="color: var(--text-secondary)">
            这页会真实调用 STS、直传 OSS，并在上传完成后调用 `/api/uploads/complete` 保存 `media_files` 记录。
          </p>
        </div>

        <div class="rounded-xl border p-4" style="border-color: var(--border-default); background-color: var(--bg-page)">
          <p class="text-xs uppercase tracking-wide font-semibold mb-2" style="color: var(--text-tertiary)">
            当前登录用户
          </p>
          <p v-if="userStore.profile" class="text-sm" style="color: var(--text-primary)">
            {{ userStore.profile.username }} · {{ userStore.profile.email }}
          </p>
          <p v-else class="text-sm" style="color: var(--text-secondary)">
            当前未登录，先登录后再测试上传。
          </p>
        </div>

        <div class="space-y-2">
          <label class="block text-xs font-medium" style="color: var(--text-secondary)">业务类型</label>
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
            <button
              v-for="option in bizTypeOptions"
              :key="option.value"
              type="button"
              class="rounded-lg border px-3 py-2 text-sm transition-colors"
              :style="selectedBizType === option.value
                ? 'border-color: var(--color-primary-500); color: var(--color-primary-500); background-color: rgba(22, 163, 74, 0.08)'
                : 'border-color: var(--border-default); color: var(--text-secondary)'"
              @click="selectedBizType = option.value"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <label
            class="flex min-h-48 cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed px-6 py-8 text-center transition-colors hover:border-primary-500 hover:bg-primary-500/5"
            style="border-color: var(--border-default)"
          >
            <BaseIcon name="ImagePlus" :size="28" class="text-primary-500" />
            <p class="mt-3 text-sm font-medium" style="color: var(--text-primary)">
              {{ selectedFile ? `已选择：${selectedFile.name}` : '选择一张图片开始测试' }}
            </p>
            <p class="mt-1 text-xs" style="color: var(--text-tertiary)">
              支持 JPG / PNG / WEBP，单张不超过 10MB
            </p>
            <input type="file" accept="image/png,image/jpeg,image/webp" class="hidden" @change="onFileChange" />
          </label>

          <div v-if="localPreviewUrl" class="rounded-2xl overflow-hidden border" style="border-color: var(--border-default)">
            <img :src="localPreviewUrl" alt="本地预览" class="w-full h-72 object-cover" />
          </div>
        </div>

        <div v-if="isUploading" class="space-y-2">
          <div class="flex items-center justify-between text-xs" style="color: var(--text-secondary)">
            <span>上传进度</span>
            <span>{{ uploadProgress }}%</span>
          </div>
          <div class="h-2 rounded-full overflow-hidden" style="background-color: var(--bg-page)">
            <div class="h-full bg-primary-500 transition-all duration-200" :style="{ width: `${uploadProgress}%` }" />
          </div>
        </div>

        <div v-if="errorMessage" class="rounded-xl border px-4 py-3 text-sm" style="border-color: rgba(239, 68, 68, 0.35); color: #dc2626; background-color: rgba(239, 68, 68, 0.08)">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="rounded-xl border px-4 py-3 text-sm" style="border-color: rgba(22, 163, 74, 0.35); color: #15803d; background-color: rgba(22, 163, 74, 0.08)">
          {{ successMessage }}
        </div>

        <button
          type="button"
          class="w-full rounded-xl px-4 py-3 text-sm font-semibold text-white transition disabled:cursor-not-allowed disabled:opacity-60"
          style="background-color: var(--color-primary-500)"
          :disabled="!userStore.isLoggedIn || !canUpload"
          @click="handleUpload"
        >
          {{ uploadButtonLabel }}
        </button>
      </section>

      <section v-if="uploadResult" class="card p-5 space-y-3 animate-fade-in-up">
        <div class="flex items-center gap-2">
          <BaseIcon name="BadgeCheck" :size="18" class="text-primary-500" />
          <h3 class="text-base font-semibold" style="color: var(--text-primary)">上传结果</h3>
        </div>

        <div class="grid gap-3 sm:grid-cols-2">
          <div class="rounded-xl border p-4" style="border-color: var(--border-default); background-color: var(--bg-page)">
            <p class="text-xs uppercase tracking-wide font-semibold mb-2" style="color: var(--text-tertiary)">本地预览</p>
            <img :src="uploadResult.localPreviewUrl" alt="本地预览" class="h-44 w-full rounded-xl object-cover" />
          </div>

          <div class="rounded-xl border p-4 space-y-2" style="border-color: var(--border-default); background-color: var(--bg-page)">
            <p class="text-xs uppercase tracking-wide font-semibold" style="color: var(--text-tertiary)">远端记录</p>
            <p class="text-sm break-all" style="color: var(--text-primary)">mediaId: {{ uploadResult.remote.mediaId }}</p>
            <p class="text-sm break-all" style="color: var(--text-primary)">bizType: {{ uploadResult.remote.bizType }}</p>
            <p class="text-sm break-all" style="color: var(--text-primary)">objectKey: {{ uploadResult.remote.objectKey }}</p>
            <a
              :href="uploadResult.remote.url"
              target="_blank"
              rel="noreferrer"
              class="inline-flex items-center gap-1 text-sm font-medium text-primary-500"
            >
              打开 OSS 文件
              <BaseIcon name="ExternalLink" :size="16" />
            </a>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
