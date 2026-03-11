<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '../common/BaseModal.vue'
import { useUserStore } from '../../stores/useUserStore'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

const userStore = useUserStore()

// Local state for editing
const formData = ref({
  username: '',
  bio: '',
  location: ''
})

// Sync form with store when modal opens
watch(() => props.show, (isOpen) => {
  if (isOpen && userStore.profile) {
    formData.value = {
      username: userStore.profile.username,
      bio: userStore.profile.bio,
      location: userStore.profile.location || ''
    }
  }
})

const handleSave = () => {
  userStore.updateProfile(formData.value)
  emit('update:show', false)
}
</script>

<template>
  <BaseModal
    :show="show"
    @update:show="$emit('update:show', $event)"
    title="编辑个人资料"
  >
    <div class="space-y-4">
      <!-- Username -->
      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">昵称</label>
        <input 
          v-model="formData.username"
          type="text" 
          class="w-full px-4 py-2.5 rounded-xl border bg-transparent text-sm transition-colors focus:border-primary-500 focus:outline-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="请输入昵称"
        />
      </div>

      <!-- Location (New field) -->
      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">所在地</label>
        <input 
          v-model="formData.location"
          type="text" 
          class="w-full px-4 py-2.5 rounded-xl border bg-transparent text-sm transition-colors focus:border-primary-500 focus:outline-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="例如：中国，上海"
        />
      </div>

      <!-- Bio -->
      <div class="space-y-1.5">
        <label class="block text-sm font-medium" style="color: var(--text-secondary);">个人简介</label>
        <textarea 
          v-model="formData.bio"
          rows="4"
          class="w-full px-4 py-2.5 rounded-xl border bg-transparent text-sm transition-colors focus:border-primary-500 focus:outline-none resize-none"
          style="border-color: var(--border-default); color: var(--text-primary);"
          placeholder="介绍一下自己..."
        ></textarea>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div class="flex justify-end gap-3">
        <button 
          @click="$emit('update:show', false)"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors hover:bg-primary-500/10"
          style="color: var(--text-secondary);"
        >
          取消
        </button>
        <button 
          @click="handleSave"
          class="px-6 py-2 rounded-lg text-sm font-medium bg-primary-500 text-white transition-colors hover:bg-primary-600"
        >
          保存更改
        </button>
      </div>
    </template>
  </BaseModal>
</template>
