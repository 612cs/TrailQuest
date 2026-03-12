<script setup lang="ts">
import { ref, computed } from 'vue'
import BaseIcon from './BaseIcon.vue'

const props = withDefaults(defineProps<{
  modelValue: string[]
  max?: number
  multiple?: boolean
}>(), {
  max: 9,
  multiple: true,
})

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
    <div v-if="modelValue.length > 0" class="grid grid-cols-3 sm:grid-cols-4 gap-2">
      <div
        v-for="(src, index) in modelValue"
        :key="index"
        class="relative group aspect-square rounded-lg overflow-hidden cursor-pointer"
        @click="emit('preview', index)"
      >
        <img :src="src" alt="" class="w-full h-full object-cover" />
        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors" />
        <button
          @click.stop="removeImage(index)"
          class="absolute top-1 right-1 w-6 h-6 rounded-full bg-black/60 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-500"
        >
          <BaseIcon name="X" :size="14" />
        </button>
      </div>

      <!-- Add Button (inline) -->
      <button
        v-if="canAdd"
        @click="triggerUpload"
        class="aspect-square rounded-lg border-2 border-dashed flex flex-col items-center justify-center gap-1 transition-colors hover:border-primary-500 hover:bg-primary-500/5"
        style="border-color: var(--border-default); color: var(--text-tertiary);"
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
      class="border-2 border-dashed rounded-xl p-6 flex flex-col items-center justify-center gap-2 cursor-pointer transition-all duration-200"
      :class="isDragging ? 'border-primary-500 bg-primary-500/5' : 'hover:border-primary-400 hover:bg-primary-500/5'"
      :style="!isDragging ? 'border-color: var(--border-default);' : ''"
    >
      <div class="w-10 h-10 rounded-full flex items-center justify-center" style="background-color: var(--bg-tag);">
        <BaseIcon name="ImagePlus" :size="20" class="text-primary-500" />
      </div>
      <p class="text-sm font-medium" style="color: var(--text-secondary);">
        点击或拖拽上传图片
      </p>
      <p class="text-xs" style="color: var(--text-tertiary);">
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
