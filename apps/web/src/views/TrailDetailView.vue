<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Share2, 
  Heart, 
  MapPin, 
  Navigation, 
  Calendar, 
  Clock, 
  TrendingUp, 
  MessageSquare,
  ChevronRight,
  Info,
  Maximize2
} from 'lucide-vue-next'
import ActionButton from '../components/common/ActionButton.vue'
import BaseIcon from '../components/common/BaseIcon.vue'
import { mockTrails } from '../mock/mockData'
import TrailTrackViewer from '../components/trail/TrailTrackViewer.vue'
import WeatherSection from '../components/trail/WeatherSection.vue'
import WeatherForecast from '../components/trail/WeatherForecast.vue'
import LandscapePrediction from '../components/trail/LandscapePrediction.vue'
import { useTrailWeather } from '../composables/useTrailWeather'

const route = useRoute()
const router = useRouter()
const trailId = route.params.id as string
const trail = computed(() => mockTrails.find(t => t.id === trailId))

const isLiked = ref(false)
const activeTab = ref('overview')

// Fullscreen Viewer State
const isViewerFullscreen = ref(false)

const { weather, forecast, isLoading, resolve } = useTrailWeather()

onMounted(() => {
  if (trail.value?.adcode) {
    resolve({ adcode: trail.value.adcode })
  }
})

// Watch for route changes to refresh data
watch(() => route.params.id, (newId) => {
  const newTrail = mockTrails.find(t => t.id === newId)
  if (newTrail?.adcode) {
    resolve({ adcode: newTrail.adcode })
  }
})

const goBack = () => router.back()
const toggleLike = () => { isLiked.value = !isLiked.value }

const tabs = [
  { id: 'overview', label: '详情' },
  { id: 'reviews', label: '评价' }
]

const difficultyLabel = computed(() => {
  switch (trail.value?.difficulty) {
    case 'Easy': return '轻松'
    case 'Moderate': return '进阶'
    case 'Hard': return '挑战'
    default: return '未知'
  }
})

const difficultyClass = computed(() => {
  switch (trail.value?.difficulty) {
    case 'Easy': return 'badge-easy'
    case 'Moderate': return 'badge-moderate'
    case 'Hard': return 'badge-hard'
    default: return ''
  }
})

// Fullscreen Handlers
const requestFullscreen = () => {
  isViewerFullscreen.value = true
}

const exitFullscreen = () => {
  isViewerFullscreen.value = false
}
</script>

<template>
  <div class="page-container pb-20 bg-slate-50 dark:bg-slate-950 min-h-screen">
    <!-- Fullscreen Viewer Overlay -->
    <Transition name="fade-in-up">
      <div v-if="isViewerFullscreen" class="fixed inset-0 z-[100] bg-slate-950 flex flex-col">
        <div class="h-12 flex items-center justify-between px-4 bg-slate-900/80 backdrop-blur-md border-b border-white/5">
          <span class="text-white font-medium text-sm">{{ trail?.title }} - 3D轨迹</span>
          <button 
            @click="exitFullscreen"
            class="p-1 px-3 rounded-lg bg-white/10 hover:bg-white/20 text-white text-xs transition-colors"
          >
            退出全屏
          </button>
        </div>
        <div class="flex-1 relative overflow-hidden">
          <TrailTrackViewer 
            v-if="trail"
            :data="trail.trackData" 
            mode="fullscreen"
            :weather-scene="'partly_cloudy'"
            @exit-fullscreen="exitFullscreen"
          />
        </div>
      </div>
    </Transition>

    <!-- Header Overlay -->
    <div class="fixed top-0 left-0 right-0 z-50 h-16 flex items-center justify-between px-4 glass-header border-b border-black/5 dark:border-white/5">
      <button @click="goBack" class="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
        <ArrowLeft :size="20" />
      </button>
      <div class="flex gap-2">
        <button @click="toggleLike" class="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/5 transition-colors" :class="{ 'text-red-500': isLiked }">
          <Heart :size="20" :fill="isLiked ? 'currentColor' : 'none'" />
        </button>
        <button class="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
          <Share2 :size="20" />
        </button>
      </div>
    </div>

    <div v-if="trail" class="max-w-7xl mx-auto px-4 pt-20">
      <!-- Title Section -->
      <div class="mb-6 animate-fade-in-up">
        <div class="flex items-center gap-2 mb-2">
          <span class="badge" :class="difficultyClass">{{ difficultyLabel }}</span>
          <div class="flex items-center text-slate-500 dark:text-slate-400 text-sm">
            <MapPin :size="14" class="mr-1" />
            {{ trail.location }}
          </div>
        </div>
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white mb-2">{{ trail.title }}</h1>
        <div class="flex items-center gap-4 text-sm text-slate-500 dark:text-slate-400">
          <div class="flex items-center">
            <BaseIcon name="Star" :size="14" class="text-amber-400 mr-1" fill="currentColor" />
            <span class="font-bold text-slate-900 dark:text-white mr-1">{{ trail.rating }}</span>
            <span>({{ trail.reviews }} 评价)</span>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        <!-- Left Column: Weather & Forecast (4/12) -->
        <div class="lg:col-span-5 space-y-8 animate-fade-in-up stagger-1">
          <!-- Real-time Weather -->
          <div class="card p-6">
            <WeatherSection :weather="weather" :is-loading="isLoading" :fallback-city="trail.location" />
            
            <div class="mt-8 pt-6 border-t border-black/5 dark:border-white/5">
              <h3 class="text-sm font-medium mb-4 flex items-center gap-2" style="color: var(--text-primary);">
                <BaseIcon name="CloudRain" :size="16" class="text-primary-500" />
                景观与环境预测
              </h3>
              <LandscapePrediction />
            </div>

            <div class="mt-8 pt-6 border-t border-black/5 dark:border-white/5">
              <h3 class="text-sm font-medium mb-4 flex items-center gap-2" style="color: var(--text-primary);">
                <BaseIcon name="Calendar" :size="16" class="text-primary-500" />
                未来六天天气预报
              </h3>
              <WeatherForecast :forecast="forecast" />
            </div>
          </div>

          <!-- Trail Highlights -->
          <div class="card p-6">
            <h3 class="text-lg font-bold mb-4">路线亮点</h3>
            <div class="space-y-4">
              <div v-for="(highlight, index) in ['壮丽的云海景观', '原始森林穿越', '高山草甸牧场', '清澈的溪流水源']" :key="index" class="flex gap-3">
                <div class="w-6 h-6 rounded-full bg-primary-50 dark:bg-primary-900/30 flex items-center justify-center flex-shrink-0 text-primary-600 dark:text-primary-400 font-bold text-xs">
                  {{ index + 1 }}
                </div>
                <p class="text-slate-600 dark:text-slate-300 text-sm leading-relaxed">{{ highlight }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: 3D Model & Specs (7/12) -->
        <div class="lg:col-span-7 space-y-8 animate-fade-in-up stagger-2">
          <!-- 3D Track Viewer Card -->
          <div class="card overflow-hidden h-[600px] flex flex-col group relative">
            <div class="absolute top-4 left-4 z-10 p-3 rounded-2xl bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border border-black/5 dark:border-white/10 shadow-sm">
              <h3 class="text-sm font-bold flex items-center gap-2">
                <Navigation :size="16" class="text-primary-500" />
                3D 轨迹建模
              </h3>
            </div>
            
            <TrailTrackViewer 
              v-if="trail.trackData" 
              :data="trail.trackData" 
              mode="detail"
              :weather-scene="weather?.weather.includes('雨') ? 'rain' : weather?.weather.includes('云') ? 'partly_cloudy' : 'clear'"
              @request-fullscreen="requestFullscreen"
            />
          </div>

          <!-- Quick Stats Row -->
          <div class="grid grid-cols-3 gap-4">
            <div class="card p-4 flex flex-col items-center justify-center text-center">
              <BaseIcon name="Route" :size="20" class="text-primary-500 mb-2" />
              <span class="text-lg font-bold">{{ trail.distance }}</span>
              <span class="text-xs text-slate-500">全长</span>
            </div>
            <div class="card p-4 flex flex-col items-center justify-center text-center">
              <BaseIcon name="TrendingUp" :size="20" class="text-blue-500 mb-2" />
              <span class="text-lg font-bold">{{ trail.elevationGainMeters }}m</span>
              <span class="text-xs text-slate-500">爬升</span>
            </div>
            <div class="card p-4 flex flex-col items-center justify-center text-center">
              <BaseIcon name="Clock" :size="20" class="text-amber-500 mb-2" />
              <span class="text-lg font-bold">{{ trail.duration }}</span>
              <span class="text-xs text-slate-500">用时</span>
            </div>
          </div>

          <!-- Description -->
          <div class="card p-6">
            <div class="flex items-center gap-2 mb-4">
              <Info :size="20" class="text-primary-500" />
              <h3 class="text-lg font-bold">路线简介</h3>
            </div>
            <p class="text-slate-600 dark:text-slate-300 leading-relaxed">
              {{ trail.description }}
            </p>
          </div>
        </div>

        <!-- Full Width: Comments Row -->
        <div class="lg:col-span-12 animate-fade-in-up stagger-3">
          <div class="card p-8">
            <div class="flex items-center justify-between mb-8">
              <div class="flex items-center gap-3">
                <MessageSquare :size="24" class="text-primary-500" />
                <h3 class="text-xl font-bold">社区评价</h3>
                <span class="px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-xs font-bold text-slate-500">{{ trail.reviews }}</span>
              </div>
              <ActionButton class="btn-primary px-6">写评价</ActionButton>
            </div>
            
            <!-- Mock reviews list -->
            <div class="space-y-8">
              <div v-for="i in 3" :key="i" class="pb-8 border-b border-black/5 dark:border-white/5 last:border-0 last:pb-0">
                <div class="flex items-start justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${i + 10}`" class="w-10 h-10 rounded-full bg-slate-200" />
                    <div>
                      <h4 class="font-bold text-sm">驴友_{{ i }}829</h4>
                      <div class="flex items-center mt-0.5">
                        <div class="flex text-amber-400 mr-2">
                          <BaseIcon v-for="s in 5" :key="s" name="Star" :size="10" :fill="s <= 5 - i ? 'currentColor' : 'none'" />
                        </div>
                        <span class="text-[10px] text-slate-400">2024-03-{{ 10 + i }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <p class="text-sm text-slate-600 dark:text-slate-300 leading-relaxed mb-4">
                  这条路线非常有挑战性，尤其是最后的冲刺阶段。建议带够水和补给。山顶的云海真的绝了！
                </p>
                <div class="flex gap-2">
                  <img v-for="j in 3" :key="j" :src="`https://picsum.photos/seed/trail_${i}_${j}/200/200`" class="w-20 h-20 rounded-xl object-cover hover:scale-105 transition-transform cursor-pointer" />
                </div>
              </div>
            </div>
            
            <div class="mt-8 text-center">
              <button class="text-primary-500 text-sm font-bold flex items-center gap-1 mx-auto hover:gap-2 transition-all">
                查看全部评价 <ChevronRight :size="16" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-header {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
}

.dark .glass-header {
  background: rgba(15, 23, 42, 0.82);
}

.badge {
  @apply px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider;
}

.badge-easy {
  @apply bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400;
}

.badge-moderate {
  @apply bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-400;
}

.badge-hard {
  @apply bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-400;
}
</style>
