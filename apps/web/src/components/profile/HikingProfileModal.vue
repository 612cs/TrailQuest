<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import BaseModal from '../common/BaseModal.vue'
import { useFlashStore } from '../../stores/useFlashStore'
import { useUserStore } from '../../stores/useUserStore'
import type { HikingProfile, HikingProfileFormValue } from '../../types/hikingProfile'
import HikingProfileForm from './HikingProfileForm.vue'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

const userStore = useUserStore()
const flashStore = useFlashStore()
const isSaving = ref(false)
const formValue = ref<HikingProfileFormValue>({
  experienceLevel: '',
  trailStyle: '',
  packPreference: '',
  location: '',
})

const canSave = computed(() =>
  !isSaving.value
  && !!formValue.value.experienceLevel
  && !!formValue.value.trailStyle
  && !!formValue.value.packPreference,
)

watch(
  () => props.show,
  (isOpen) => {
    if (!isOpen) {
      return
    }

    formValue.value = {
      experienceLevel: userStore.profile?.hikingProfile?.experienceLevel ?? '',
      trailStyle: userStore.profile?.hikingProfile?.trailStyle ?? '',
      packPreference: userStore.profile?.hikingProfile?.packPreference ?? '',
      location: userStore.profile?.location ?? '',
    }
  },
)

async function handleSave() {
  if (!canSave.value) {
    return
  }

  const hikingProfile = buildCompleteHikingProfile(formValue.value)
  if (!hikingProfile) {
    return
  }

  isSaving.value = true
  const [profileResult, hikingResult] = await Promise.all([
    userStore.saveProfile({
      username: userStore.profile?.username ?? '',
      bio: userStore.profile?.bio ?? '',
      location: formValue.value.location,
      avatarMediaId: userStore.profile?.avatarMediaId ?? null,
    }),
    userStore.saveHikingProfile({
      hikingProfile,
    }),
  ])
  isSaving.value = false

  if (!profileResult.success) {
    flashStore.showError(profileResult.message ?? '地区保存失败')
    return
  }
  if (!hikingResult.success) {
    flashStore.showError(hikingResult.message ?? '徒步画像保存失败')
    return
  }

  flashStore.showSuccess('徒步画像已更新')
  emit('update:show', false)
}

function buildCompleteHikingProfile(form: HikingProfileFormValue): HikingProfile | null {
  if (!form.experienceLevel || !form.trailStyle || !form.packPreference) {
    return null
  }

  return {
    experienceLevel: form.experienceLevel,
    trailStyle: form.trailStyle,
    packPreference: form.packPreference,
  }
}
</script>

<template>
  <BaseModal
    :show="show"
    footer-tone="plain"
    title="补充徒步画像"
    @update:show="$emit('update:show', $event)"
  >
    <div class="space-y-4">
      <p class="text-sm leading-6" style="color: var(--text-secondary);">
        告诉 TrailQuest 你更偏好的徒步方式，后续推荐和社区内容会更贴近你的节奏。
      </p>

      <HikingProfileForm v-model="formValue" />
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-medium transition-colors hover:bg-primary-500/10"
          style="color: var(--text-secondary);"
          @click="$emit('update:show', false)"
        >
          取消
        </button>
        <button
          type="button"
          class="rounded-lg bg-primary-500 px-6 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="!canSave"
          @click="handleSave"
        >
          {{ isSaving ? '保存中...' : '保存画像' }}
        </button>
      </div>
    </template>
  </BaseModal>
</template>
