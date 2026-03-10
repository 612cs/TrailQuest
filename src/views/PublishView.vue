<script setup lang="ts">
import { ref, computed, shallowRef, useTemplateRef, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import BaseIcon from '../components/common/BaseIcon.vue'
import ImageUploader from '../components/common/ImageUploader.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import { presetTags } from '../mock/mockData'

const router = useRouter()
import { useAmapLoader } from '../composables/useAmapLoader'
import toGeoJSON from '@mapbox/togeojson'

// Form state
const name = ref('')
const location = ref('')
const difficulty = ref<'easy' | 'moderate' | 'hard'>('moderate')
const distance = ref('')
const elevation = ref('')
const duration = ref('')
const description = ref('')
const coverImages = ref<string[]>([])
const trailPhotos = ref<string[]>([])
const selectedTags = ref<string[]>([])
const customTag = ref('')

// UI state
const isSubmitting = ref(false)
const showSuccess = ref(false)
const previewImages = ref<string[]>([])
const previewIndex = ref(0)
const showPreview = ref(false)

// GPX Map State
const mapRef = useTemplateRef<HTMLDivElement>('mapContainer')
const mapInstance = shallowRef<any>(null)
const geoJsonData = ref<any>(null)
const gpxFileName = ref('')
const mapError = ref('')
const mapLoading = ref(false)

const difficultyOptions = [
  { value: 'easy' as const, label: '简单', color: 'var(--color-easy)' },
  { value: 'moderate' as const, label: '适中', color: 'var(--color-primary-500)' },
  { value: 'hard' as const, label: '困难', color: 'var(--color-hard)' },
]

const isFormValid = computed(() => {
  return name.value.trim() && location.value.trim() && description.value.trim()
})

function toggleTag(tag: string) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

function addCustomTag() {
  const tag = customTag.value.trim()
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
  customTag.value = ''
}

function openPreview(images: string[], index: number) {
  previewImages.value = images
  previewIndex.value = index
  showPreview.value = true
}

async function renderMapWithGeoJSON() {
  if (!geoJsonData.value) return

  await nextTick()
  if (!mapRef.value) return

  const AMap = await useAmapLoader().load()
  if (!AMap) {
    mapError.value = '获取高德地图服务失败'
    return
  }

  if (!mapInstance.value) {
    mapInstance.value = new AMap.Map(mapRef.value, {
      zoom: 11,
      resizeEnable: true,
    })
  }

  mapInstance.value.clearMap()

  AMap.plugin('AMap.GeoJSON', () => {
    // 过滤掉单独的离散锚点，或者将零散的点统一提取为一条 LineString
    const lineCoordinates: any[] = []
    geoJsonData.value.features.forEach((feature: any) => {
      if (feature.geometry.type === 'Point') {
        lineCoordinates.push(feature.geometry.coordinates)
      } else if (feature.geometry.type === 'LineString') {
        lineCoordinates.push(...feature.geometry.coordinates)
      } else if (feature.geometry.type === 'MultiLineString') {
        feature.geometry.coordinates.forEach((line: any) => lineCoordinates.push(...line))
      }
    })

    const processedGeoJSON = {
      type: 'FeatureCollection',
      features: [
        {
          type: 'Feature',
          geometry: {
            type: 'LineString',
            coordinates: lineCoordinates,
          },
          properties: {
             name: '轨迹路线'
          }
        }
      ]
    }

    const geojsonObj = new AMap.GeoJSON({
      geoJSON: processedGeoJSON,
      getPolyline: function (_geojson: any, lnglats: any) {
        return new AMap.Polyline({
          path: lnglats,
          strokeColor: 'var(--primary-500)',
          strokeWeight: 6,
          strokeOpacity: 0.8,
          lineJoin: 'round',
          lineCap: 'round',
          showDir: true
        })
      }
    })

    mapInstance.value.add(geojsonObj)
    mapInstance.value.setFitView()
  })
}

async function handleGpxUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  gpxFileName.value = file.name
  mapLoading.value = true
  mapError.value = ''

  try {
    const text = await file.text()
    const doc = new DOMParser().parseFromString(text, 'text/xml')
    geoJsonData.value = toGeoJSON.gpx(doc)
    await renderMapWithGeoJSON()
  } catch (err) {
    console.error(err)
    mapError.value = '解析 GPX 文件失败，请检查文件格式。'
    geoJsonData.value = null
  } finally {
    mapLoading.value = false
  }
}

async function handleSubmit(isDraft = false) {
  if (!isFormValid.value && !isDraft) return
  isSubmitting.value = true

  // Simulate API call
  await new Promise((r) => setTimeout(r, 1000))

  isSubmitting.value = false
  showSuccess.value = true

  setTimeout(() => {
    showSuccess.value = false
    router.push('/')
  }, 1500)
}
</script>

<template>
  <main>
    <!-- Page Header -->
    <div class="glass-header sticky top-14 sm:top-16 z-40 px-4 py-3">
      <div class="max-w-3xl mx-auto flex items-center justify-between">
        <button
          @click="router.back()"
          class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500"
          style="color: var(--text-secondary);"
        >
          <BaseIcon name="ChevronLeft" :size="20" />
          返回
        </button>
        <h2 class="text-sm font-semibold" style="color: var(--text-primary);">发布路线</h2>
        <div class="w-12" />
      </div>
    </div>

    <div class="max-w-3xl mx-auto px-4 sm:px-6 py-6 space-y-5">
      <!-- Section 1: Cover Image -->
      <section class="card p-4 sm:p-5 animate-fade-in-up stagger-1">
        <h3 class="text-sm font-semibold mb-3 flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Image" :size="16" class="text-primary-500" />
          封面图片
        </h3>
        <ImageUploader v-model="coverImages" :max="1" :multiple="false" @preview="openPreview(coverImages, $event)" />
      </section>

      <!-- Section 2: Basic Info -->
      <section class="card p-4 sm:p-5 space-y-4 animate-fade-in-up stagger-2">
        <h3 class="text-sm font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="FileText" :size="16" class="text-primary-500" />
          基本信息
        </h3>

        <!-- Trail Name -->
        <div>
          <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">
            路线名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="name"
            type="text"
            placeholder="例如：龙脊梯田精华线"
            class="w-full px-3 py-2.5 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
            style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
          />
        </div>

        <!-- Location -->
        <div>
          <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">
            所在位置 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="location"
            type="text"
            placeholder="例如：广西 桂林 龙胜"
            class="w-full px-3 py-2.5 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
            style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
          />
        </div>

        <!-- Difficulty -->
        <div>
          <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">难度等级</label>
          <div class="flex gap-2">
            <button
              v-for="opt in difficultyOptions"
              :key="opt.value"
              @click="difficulty = opt.value"
              class="flex-1 py-2 rounded-lg text-sm font-medium border-2 transition-all duration-200"
              :class="difficulty === opt.value ? 'scale-[1.02]' : 'opacity-60 hover:opacity-80'"
              :style="{
                borderColor: difficulty === opt.value ? opt.color : 'var(--border-default)',
                backgroundColor: difficulty === opt.value ? opt.color + '15' : 'transparent',
                color: difficulty === opt.value ? opt.color : 'var(--text-secondary)',
              }"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
      </section>

      <!-- Section 3: Trail Route (GPX) -->
      <section class="card p-4 sm:p-5 space-y-4 animate-fade-in-up stagger-3">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold flex items-center gap-2" style="color: var(--text-primary);">
            <BaseIcon name="Map" :size="16" class="text-primary-500" />
            路线轨迹 (GPX)
          </h3>
          <label class="px-3 py-1.5 rounded-lg text-xs font-medium text-primary-500 bg-primary-500/10 cursor-pointer hover:bg-primary-500/20 transition-colors">
            {{ gpxFileName ? '重新上传' : '上传 GPX' }}
            <input type="file" accept=".gpx" class="hidden" @change="handleGpxUpload" />
          </label>
        </div>

        <div v-if="gpxFileName" class="text-xs" style="color: var(--text-secondary);">
          已选择文件: <span class="font-medium text-primary-500">{{ gpxFileName }}</span>
        </div>

        <div class="map-shell relative bg-gray-100 rounded-xl overflow-hidden shadow-inner border border-gray-200 dark:border-gray-800" style="height: 240px; border: 1px solid var(--border-default);">
           <div ref="mapContainer" class="w-full h-full" :style="{ display: geoJsonData ? 'block' : 'none' }"></div>
           
           <div v-if="mapLoading" class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-black/5 backdrop-blur-sm z-10">
              <BaseIcon name="Loader2" :size="20" class="animate-spin text-primary-500" />
              <span class="text-xs font-medium" style="color: var(--text-secondary);">解析轨迹中...</span>
           </div>
           <div v-else-if="mapError" class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-black/5 z-10">
              <BaseIcon name="AlertCircle" :size="20" class="text-red-500" />
              <span class="text-xs font-medium text-red-500">{{ mapError }}</span>
           </div>
           <div v-else-if="!geoJsonData" class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-black/5 z-10">
              <BaseIcon name="MapPin" :size="24" style="color: var(--text-tertiary);" />
              <span class="text-xs font-medium" style="color: var(--text-tertiary);">请上传 GPX 轨迹文件以在地图中预览</span>
           </div>
        </div>
      </section>

      <!-- Section 4: Trail Parameters -->
      <section class="card p-4 sm:p-5 space-y-4 animate-fade-in-up stagger-3">
        <h3 class="text-sm font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Ruler" :size="16" class="text-primary-500" />
          路线参数
        </h3>
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">距离</label>
            <input
              v-model="distance"
              type="text"
              placeholder="12.5 km"
              class="w-full px-3 py-2.5 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">海拔</label>
            <input
              v-model="elevation"
              type="text"
              placeholder="+450 m"
              class="w-full px-3 py-2.5 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--text-secondary);">时长</label>
            <input
              v-model="duration"
              type="text"
              placeholder="4.5 小时"
              class="w-full px-3 py-2.5 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
              style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
            />
          </div>
        </div>
      </section>

      <!-- Section 4: Description -->
      <section class="card p-4 sm:p-5 space-y-3 animate-fade-in-up stagger-4">
        <h3 class="text-sm font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="AlignLeft" :size="16" class="text-primary-500" />
          路线描述 <span class="text-red-500 text-xs font-normal">*</span>
        </h3>
        <textarea
          v-model="description"
          rows="5"
          placeholder="描述这条路线的特色、注意事项、推荐理由..."
          class="w-full px-3 py-2.5 rounded-lg text-sm resize-none transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
          style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
        />
      </section>

      <!-- Section 5: Trail Photos -->
      <section class="card p-4 sm:p-5 animate-fade-in-up stagger-5">
        <h3 class="text-sm font-semibold mb-3 flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Camera" :size="16" class="text-primary-500" />
          路线照片
        </h3>
        <ImageUploader v-model="trailPhotos" :max="9" @preview="openPreview(trailPhotos, $event)" />
      </section>

      <!-- Section 6: Tags -->
      <section class="card p-4 sm:p-5 space-y-3 animate-fade-in-up stagger-6">
        <h3 class="text-sm font-semibold flex items-center gap-2" style="color: var(--text-primary);">
          <BaseIcon name="Tag" :size="16" class="text-primary-500" />
          路线标签
        </h3>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in presetTags"
            :key="tag"
            @click="toggleTag(tag)"
            class="px-3 py-1.5 rounded-full text-xs font-medium border transition-all duration-200"
            :class="
              selectedTags.includes(tag)
                ? 'bg-primary-500 text-white border-primary-500'
                : 'hover:border-primary-400'
            "
            :style="
              !selectedTags.includes(tag)
                ? 'color: var(--text-secondary); border-color: var(--border-default); background-color: var(--bg-tag);'
                : ''
            "
          >
            {{ tag }}
          </button>
        </div>
        <!-- Custom tag input -->
        <div class="flex gap-2">
          <input
            v-model="customTag"
            type="text"
            placeholder="自定义标签..."
            class="flex-1 px-3 py-2 rounded-lg text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500/30"
            style="background-color: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-default);"
            @keyup.enter="addCustomTag"
          />
          <button
            @click="addCustomTag"
            :disabled="!customTag.trim()"
            class="px-3 py-2 rounded-lg text-sm font-medium text-primary-500 border border-primary-500/30 hover:bg-primary-500/10 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            添加
          </button>
        </div>
      </section>

      <!-- Action Buttons -->
      <div class="flex gap-3 pb-8 animate-fade-in-up stagger-6">
        <button
          @click="handleSubmit(true)"
          :disabled="isSubmitting"
          class="flex-1 py-3 rounded-xl text-sm font-medium border transition-all duration-200 hover:shadow-md disabled:opacity-40"
          style="color: var(--text-secondary); border-color: var(--border-default); background-color: var(--bg-card);"
        >
          <BaseIcon name="Save" :size="16" class="inline mr-1.5" />
          保存草稿
        </button>
        <button
          @click="handleSubmit(false)"
          :disabled="!isFormValid || isSubmitting"
          class="flex-1 py-3 rounded-xl text-sm font-medium text-white transition-all duration-200 flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
          :class="isFormValid && !isSubmitting ? 'bg-primary-500 hover:bg-primary-600 hover:shadow-md active:scale-[0.98]' : 'bg-gray-400'"
        >
          <BaseIcon v-if="isSubmitting" name="Loader2" :size="16" class="animate-spin" />
          <BaseIcon v-else name="Send" :size="16" />
          {{ isSubmitting ? '发布中...' : '发布路线' }}
        </button>
      </div>
    </div>

    <!-- Success Toast -->
    <transition name="toast">
      <div v-if="showSuccess" class="fixed top-20 left-1/2 -translate-x-1/2 z-50 px-5 py-3 rounded-xl bg-primary-500 text-white text-sm font-medium shadow-lg flex items-center gap-2">
        <BaseIcon name="CheckCircle" :size="18" />
        发布成功！即将跳转...
      </div>
    </transition>

    <!-- Image Preview -->
    <ImagePreviewModal
      v-if="showPreview"
      :images="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />
  </main>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -12px);
}
</style>
