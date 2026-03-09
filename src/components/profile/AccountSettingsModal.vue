<script setup lang="ts">
import { ref } from 'vue'
import BaseModal from '../common/BaseModal.vue'

defineProps<{
  show: boolean
}>()

defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

// Dummy settings state for demonstration
const emailNotifications = ref(true)
const pushNotifications = ref(false)
const privateProfile = ref(false)

</script>

<template>
  <BaseModal
    :show="show"
    @update:show="$emit('update:show', $event)"
    title="账户设置"
  >
    <div class="space-y-6">
      
      <!-- Notifications Section -->
      <div>
        <h3 class="text-sm font-semibold mb-3" style="color: var(--text-primary);">通知偏好</h3>
        <div class="space-y-3">
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-sm" style="color: var(--text-secondary);">电子邮件通知</span>
            <input type="checkbox" v-model="emailNotifications" class="w-4 h-4 rounded border-gray-300 focus:ring-primary-500 text-primary-500 accent-primary-500" />
          </label>
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-sm" style="color: var(--text-secondary);">推送通知</span>
            <input type="checkbox" v-model="pushNotifications" class="w-4 h-4 rounded border-gray-300 focus:ring-primary-500 text-primary-500 accent-primary-500" />
          </label>
        </div>
      </div>

      <!-- Privacy Section -->
      <div class="border-t pt-4" style="border-color: var(--border-default);">
        <h3 class="text-sm font-semibold mb-3" style="color: var(--text-primary);">隐私与安全</h3>
        <div class="space-y-3">
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-sm" style="color: var(--text-secondary);">私密账户</span>
            <input type="checkbox" v-model="privateProfile" class="w-4 h-4 rounded border-gray-300 focus:ring-primary-500 text-primary-500 accent-primary-500" />
          </label>
          <div class="pt-2">
            <button class="text-sm font-medium transition-colors" style="color: var(--color-primary-500); hover: opacity: 0.8;">
              退出登录
            </button>
          </div>
        </div>
      </div>

    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div class="flex justify-end gap-3">
        <button 
          @click="$emit('update:show', false)"
          class="px-6 py-2 rounded-lg text-sm font-medium bg-primary-500 text-white transition-colors hover:bg-primary-600"
        >
          完成
        </button>
      </div>
    </template>
  </BaseModal>
</template>
