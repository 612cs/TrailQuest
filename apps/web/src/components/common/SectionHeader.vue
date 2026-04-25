<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="mb-6 flex items-end justify-between sm:mb-8">
    <div>
      <h2 class="text-xl font-bold sm:text-2xl" style="color: var(--text-primary)">{{ title }}</h2>
      <p v-if="subtitle" class="mt-1 text-sm" style="color: var(--text-secondary)">
        {{ subtitle }}
      </p>
    </div>
    <slot name="action" />
  </div>
</template>
