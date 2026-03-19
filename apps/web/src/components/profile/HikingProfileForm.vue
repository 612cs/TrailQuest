<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'
import type { HikingProfileFormValue } from '../../types/hikingProfile'

const props = withDefaults(defineProps<{
  modelValue: HikingProfileFormValue
  showLocation?: boolean
}>(), {
  showLocation: true,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: HikingProfileFormValue): void
}>()

const experienceOptions = [
  { value: 'beginner', label: '新手', icon: 'Sprout' },
  { value: 'intermediate', label: '进阶', icon: 'Mountain' },
  { value: 'expert', label: '老驴', icon: 'Compass' },
] as const

const trailStyleOptions = [
  { value: 'city_weekend', label: '城市周边', icon: 'Trees' },
  { value: 'long_distance', label: '长线徒步', icon: 'Map' },
  { value: 'both', label: '都走', icon: 'Route' },
] as const

const packPreferenceOptions = [
  { value: 'light', label: '轻装', icon: 'Backpack' },
  { value: 'heavy', label: '重装', icon: 'Package2' },
  { value: 'both', label: '都可以', icon: 'Scale' },
] as const

function patchValue<K extends keyof HikingProfileFormValue>(key: K, value: HikingProfileFormValue[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value,
  })
}
</script>

<template>
  <div class="space-y-5">
    <div class="space-y-2">
      <div class="flex items-center gap-2">
        <BaseIcon name="Footprints" :size="16" class="text-primary-500" />
        <p class="text-sm font-semibold" style="color: var(--text-primary);">你的徒步经验</p>
      </div>
      <div class="grid grid-cols-3 gap-2">
        <button
          v-for="option in experienceOptions"
          :key="option.value"
          type="button"
          class="rounded-2xl border px-3 py-3 text-sm font-medium transition-colors"
          :style="modelValue.experienceLevel === option.value
            ? 'border-color: var(--color-primary-500); background-color: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--color-primary-500);'
            : 'border-color: var(--border-default); background-color: var(--bg-card); color: var(--text-secondary);'"
          @click="patchValue('experienceLevel', option.value)"
        >
          <BaseIcon :name="option.icon" :size="16" class="mb-2" />
          <span class="block">{{ option.label }}</span>
        </button>
      </div>
    </div>

    <div class="space-y-2">
      <div class="flex items-center gap-2">
        <BaseIcon name="MapPinned" :size="16" class="text-primary-500" />
        <p class="text-sm font-semibold" style="color: var(--text-primary);">平常更常走哪类路线</p>
      </div>
      <div class="grid grid-cols-3 gap-2">
        <button
          v-for="option in trailStyleOptions"
          :key="option.value"
          type="button"
          class="rounded-2xl border px-3 py-3 text-sm font-medium transition-colors"
          :style="modelValue.trailStyle === option.value
            ? 'border-color: var(--color-primary-500); background-color: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--color-primary-500);'
            : 'border-color: var(--border-default); background-color: var(--bg-card); color: var(--text-secondary);'"
          @click="patchValue('trailStyle', option.value)"
        >
          <BaseIcon :name="option.icon" :size="16" class="mb-2" />
          <span class="block">{{ option.label }}</span>
        </button>
      </div>
    </div>

    <div class="space-y-2">
      <div class="flex items-center gap-2">
        <BaseIcon name="Package" :size="16" class="text-primary-500" />
        <p class="text-sm font-semibold" style="color: var(--text-primary);">你更偏好哪种出行方式</p>
      </div>
      <div class="grid grid-cols-3 gap-2">
        <button
          v-for="option in packPreferenceOptions"
          :key="option.value"
          type="button"
          class="rounded-2xl border px-3 py-3 text-sm font-medium transition-colors"
          :style="modelValue.packPreference === option.value
            ? 'border-color: var(--color-primary-500); background-color: color-mix(in srgb, var(--primary-500) 10%, transparent); color: var(--color-primary-500);'
            : 'border-color: var(--border-default); background-color: var(--bg-card); color: var(--text-secondary);'"
          @click="patchValue('packPreference', option.value)"
        >
          <BaseIcon :name="option.icon" :size="16" class="mb-2" />
          <span class="block">{{ option.label }}</span>
        </button>
      </div>
    </div>

    <div v-if="showLocation" class="space-y-1.5">
      <label class="block text-sm font-medium" style="color: var(--text-secondary);">所在地区</label>
      <input
        :value="modelValue.location"
        type="text"
        class="w-full rounded-xl border bg-transparent px-4 py-2.5 text-sm transition-colors focus:border-primary-500 focus:outline-none"
        style="border-color: var(--border-default); color: var(--text-primary);"
        placeholder="例如：中国，上海"
        @input="patchValue('location', ($event.target as HTMLInputElement).value)"
      />
    </div>
  </div>
</template>
