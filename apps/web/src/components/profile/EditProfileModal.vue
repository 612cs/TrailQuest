<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import BaseIcon from '../common/BaseIcon.vue'
import BaseModal from '../common/BaseModal.vue'
import { useOssImageUploader } from '../../composables/useOssImageUploader'
import { useFlashStore } from '../../stores/useFlashStore'
import { useUserStore } from '../../stores/useUserStore'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

const userStore = useUserStore()
const flashStore = useFlashStore()
const avatarUploader = useOssImageUploader({ bizType: 'avatar', max: 1 })

const fileInput = ref<HTMLInputElement | null>(null)
const isSaving = ref(false)
const formData = ref({
  username: '',
  bio: '',
  location: '',
})

const currentAvatarUpload = computed(() => avatarUploader.items.value[0] ?? null)
const previewAvatarUrl = computed(() => currentAvatarUpload.value?.localUrl || userStore.profile?.avatarMediaUrl || '')
const avatarUploadError = computed(() => currentAvatarUpload.value?.status === 'error' ? currentAvatarUpload.value.errorMessage : '')
const pendingAvatarMediaId = computed(() => {
  if (currentAvatarUpload.value?.status === 'success') {
    return currentAvatarUpload.value.mediaId
  }
  return userStore.profile?.avatarMediaId ?? null
})
const saveDisabled = computed(() => !userStore.profile || isSaving.value || avatarUploader.isUploading.value)
const previewAvatarText = computed(() => buildAvatarText(formData.value.username, userStore.profile?.avatar ?? 'U'))
const previewAvatarBg = computed(() => userStore.profile?.avatarBg ?? 'var(--primary-500)')

watch(
  () => props.show,
  (isOpen) => {
    if (isOpen) {
      resetForm()
      return
    }
    avatarUploader.clear()
    isSaving.value = false
  },
)

function resetForm() {
  formData.value = {
    username: userStore.profile?.username ?? '',
    bio: userStore.profile?.bio ?? '',
    location: userStore.profile?.location ?? '',
  }
  avatarUploader.clear()
}

function closeModal() {
  if (isSaving.value) return
  emit('update:show', false)
}

function triggerAvatarUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = input.files
  if (!files?.length) {
    return
  }

  avatarUploader.clear()
  await avatarUploader.addFiles(files)
  input.value = ''
}

async function handleSave() {
  if (saveDisabled.value) return

  isSaving.value = true
  const result = await userStore.saveProfile({
    username: formData.value.username,
    bio: formData.value.bio,
    location: formData.value.location,
    avatarMediaId: pendingAvatarMediaId.value,
  })
  isSaving.value = false

  if (!result.success) {
    flashStore.showError(result.message ?? '资料保存失败')
    return
  }

  flashStore.showSuccess('个人资料已更新')
  emit('update:show', false)
}

function buildAvatarText(username: string, fallback: string) {
  const normalized = username.replace(/\s+/g, '').trim()
  if (!normalized) {
    return fallback
  }
  return normalized.slice(0, 2).toUpperCase()
}
</script>

<template>
  <BaseModal
    :show="show"
    footer-tone="plain"
    title="编辑个人资料"
    @update:show="$emit('update:show', $event)"
    @close="closeModal"
  >
    <div class="space-y-5">
      <div class="flex items-center gap-4 rounded-2xl border p-4" style="border-color: var(--border-default); background-color: var(--bg-tag);">
        <div class="relative shrink-0">
          <div
            class="flex h-[4.5rem] w-[4.5rem] items-center justify-center overflow-hidden rounded-2xl border text-lg font-bold text-white shadow-inner sm:h-20 sm:w-20"
            :style="{ background: previewAvatarBg, borderColor: 'var(--border-default)' }"
          >
            <img
              v-if="previewAvatarUrl"
              :src="previewAvatarUrl"
              alt="头像预览"
              class="h-full w-full object-cover"
            />
            <span v-else>{{ previewAvatarText }}</span>
          </div>
          <div
            v-if="avatarUploader.isUploading"
            class="absolute inset-0 flex items-center justify-center rounded-2xl bg-black/45 text-white"
          >
            <span class="text-xs font-medium">{{ currentAvatarUpload?.progress ?? 0 }}%</span>
          </div>
        </div>

        <div class="min-w-0 flex-1 space-y-2">
          <div>
            <p class="text-sm font-semibold" style="color: var(--text-primary);">头像</p>
            <p class="text-xs leading-5" style="color: var(--text-secondary);">
              支持 JPG、PNG、WEBP，上传后仅在点击“保存更改”后正式生效。
            </p>
          </div>

          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-medium transition-colors hover:border-primary-500/40 hover:bg-primary-500/8"
            style="border-color: var(--border-default); color: var(--text-primary);"
            @click="triggerAvatarUpload"
          >
            <BaseIcon name="ImageUp" :size="16" />
            {{ currentAvatarUpload ? '重新上传头像' : '上传头像' }}
          </button>

          <p v-if="avatarUploadError" class="text-xs" style="color: var(--color-hard);">
            {{ avatarUploadError }}
          </p>
        </div>

        <input
          ref="fileInput"
          accept="image/jpeg,image/png,image/webp"
          class="hidden"
          type="file"
          @change="handleAvatarChange"
        />
      </div>

      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">昵称</label>
        <input
          v-model="formData.username"
          type="text"
          class="w-full rounded-xl border bg-transparent px-4 py-2.5 text-sm transition-colors focus:border-primary-500 focus:outline-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="请输入昵称"
        />
      </div>

      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">所在地</label>
        <input
          v-model="formData.location"
          type="text"
          class="w-full rounded-xl border bg-transparent px-4 py-2.5 text-sm transition-colors focus:border-primary-500 focus:outline-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="例如：中国，上海"
        />
      </div>

      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">个人简介</label>
        <textarea
          v-model="formData.bio"
          rows="4"
          class="w-full resize-none rounded-xl border bg-transparent px-4 py-2.5 text-sm transition-colors focus:border-primary-500 focus:outline-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="介绍一下自己..."
        />
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-medium transition-colors hover:bg-primary-500/10"
          :disabled="isSaving"
          style="color: var(--text-secondary);"
          @click="closeModal"
        >
          取消
        </button>
        <button
          type="button"
          class="rounded-lg bg-primary-500 px-6 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="saveDisabled"
          @click="handleSave"
        >
          {{ avatarUploader.isUploading ? '头像上传中...' : isSaving ? '保存中...' : '保存更改' }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>
