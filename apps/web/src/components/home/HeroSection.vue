<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseIcon from '../common/BaseIcon.vue'
import { fetchHomeHeroSetting } from '../../api/site'

const router = useRouter()
const searchQuery = ref('')
const remoteHeroImage = ref('')

const activeHeroImage = computed(() => remoteHeroImage.value || '')

onMounted(async () => {
  try {
    const setting = await fetchHomeHeroSetting()
    remoteHeroImage.value = setting.imageUrl?.trim() || ''
  } catch (error) {
    console.warn('Failed to load home hero setting, fallback to local asset.', error)
  }
})

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value.trim() } })
  } else {
    router.push('/search')
  }
}
</script>

<template>
  <section class="relative flex h-screen min-h-[100vh] items-end overflow-hidden">
    <!-- Background Image -->
    <div class="absolute inset-0">
      <img :src="activeHeroImage" alt="壮丽山景" class="h-full w-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-black/85 via-black/35 to-black/10"></div>
    </div>

    <!-- Content Overlay -->
    <div
      class="relative z-10 mx-auto w-full max-w-7xl px-4 pt-24 pb-12 sm:px-6 sm:pt-28 sm:pb-20 lg:px-10"
    >
      <div class="max-w-2xl space-y-5 sm:space-y-6">
        <h1
          class="animate-fade-in-up text-4xl leading-tight font-extrabold tracking-tight text-white sm:text-5xl lg:text-6xl"
          style="text-shadow: 0 2px 16px rgba(0, 0, 0, 0.3)"
        >
          冒险在召唤。
        </h1>
        <p class="animate-fade-in-up stagger-1 max-w-lg text-base text-white/80 sm:text-lg">
          通过经过验证的地图和社区评论，发现成千上万条步道。
        </p>

        <!-- Search Bar -->
        <div class="animate-fade-in-up stagger-2 flex items-stretch gap-2 sm:gap-3">
          <div
            class="flex flex-1 items-center gap-3 rounded-xl border border-white/20 bg-white/10 px-4 py-3 backdrop-blur-md"
          >
            <BaseIcon name="Search" class="shrink-0 text-white/60" :size="20" />
            <input
              v-model="searchQuery"
              @keydown.enter="handleSearch"
              type="text"
              placeholder="按城市、公园或步道搜索"
              class="w-full bg-transparent text-sm text-white placeholder-white/50 focus:outline-none"
            />
          </div>
          <button
            @click="handleSearch"
            class="bg-primary-500 hover:bg-primary-600 flex shrink-0 items-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold text-white transition-all duration-200 hover:shadow-lg sm:px-6"
          >
            <BaseIcon name="Compass" :size="16" />
            寻找步道
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
