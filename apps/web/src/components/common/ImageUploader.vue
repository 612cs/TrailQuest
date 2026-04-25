<script setup lang="ts">
import { ref, computed } from 'vue'
import BaseIcon from './BaseIcon.vue'

const props = withDefaults(
  defineProps<{
    modelValue: string[]
    max?: number
    multiple?: boolean
  }>(),
  {
    max: 9,
    multiple: true,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  preview: [index: number]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)

const canAdd = computed(() => props.modelValue.length < props.max)
const remaining = computed(() => props.max - props.modelValue.length)

function triggerUpload() {
  fileInput.value?.click()
}

function handleFiles(files: FileList | null) {
  if (!files) return
  const available = props.max - props.modelValue.length
  const toAdd = Array.from(files).slice(0, available)
  const urls = toAdd.map((file) => URL.createObjectURL(file))
  emit('update:modelValue', [...props.modelValue, ...urls])
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  handleFiles(input.files)
  input.value = ''
}

function onDrop(event: DragEvent) {
  isDragging.value = false
  handleFiles(event.dataTransfer?.files ?? null)
}

function removeImage(index: number) {
  const updated = [...props.modelValue]
  const removed = updated.splice(index, 1)
  removed.forEach((url) => {
    if (url.startsWith('blob:')) URL.revokeObjectURL(url)
  })
  emit('update:modelValue', updated)
}
</script>

<template>
  <div class="space-y-3">
    <!-- Image Grid -->
    <div v-if="modelValue.length > 0" class="grid grid-cols-3 gap-2 sm:grid-cols-4">
      <div
        v-for="(src, index) in modelValue"
        :key="index"
        class="group relative aspect-square cursor-pointer overflow-hidden rounded-lg"
        @click="emit('preview', index)"
      >
        <img :src="src" alt="" class="h-full w-full object-cover" />
        <div class="absolute inset-0 bg-black/0 transition-colors group-hover:bg-black/30" />
        <button
          @click.stop="removeImage(index)"
          class="absolute top-1 right-1 flex h-6 w-6 items-center justify-center rounded-full bg-black/60 text-white opacity-0 transition-opacity group-hover:opacity-100 hover:bg-red-500"
        >
          <BaseIcon name="X" :size="14" />
        </button>
      </div>

      <!-- Add Button (inline) -->
      <button
        v-if="canAdd"
        @click="triggerUpload"
        class="hover:border-primary-500 hover:bg-primary-500/5 flex aspect-square flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed transition-colors"
        style="border-color: var(--border-default); color: var(--text-tertiary)"
      >
        <BaseIcon name="Plus" :size="20" />
        <span class="text-xs">{{ remaining }}/{{ max }}</span>
      </button>
    </div>

    <!-- Empty State / Drop Zone -->
    <div
      v-else
      @click="triggerUpload"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onDrop"
      class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 transition-all duration-200"
      :class="
        isDragging
          ? 'border-primary-500 bg-primary-500/5'
          : 'hover:border-primary-400 hover:bg-primary-500/5'
      "
      :style="!isDragging ? 'border-color: var(--border-default);' : ''"
    >
      <div
        class="flex h-10 w-10 items-center justify-center rounded-full"
        style="background-color: var(--bg-tag)"
      >
        <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
      </div>
      <p class="text-sm font-medium" style="color: var(--text-secondary)">点击或拖拽上传图片</p>
      <p class="text-xs" style="color: var(--text-tertiary)">
        最多 {{ max }} 张，支持 JPG、PNG 格式
      </p>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      :multiple="multiple"
      class="hidden"
      @change="onFileChange"
    />
  </div>
</template>
