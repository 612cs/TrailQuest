<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

import BaseIcon from '../common/BaseIcon.vue'
import type { TrackViewerData, TrackViewerMode } from '../../types/trackViewer'

const props = withDefaults(defineProps<{
  data: TrackViewerData | null
  mode?: TrackViewerMode
  showFullscreenButton?: boolean
  showScrollToContentButton?: boolean
}>(), {
  mode: 'embedded',
  showFullscreenButton: false,
  showScrollToContentButton: false,
})

const emit = defineEmits<{
  (event: 'requestFullscreen'): void
  (event: 'exitFullscreen'): void
  (event: 'scrollToContent'): void
}>()

const containerRef = useTemplateRef<HTMLDivElement>('container')
const rendererRef = shallowRef<THREE.WebGLRenderer | null>(null)
const sceneRef = shallowRef<THREE.Scene | null>(null)
const cameraRef = shallowRef<THREE.PerspectiveCamera | null>(null)
const controlsRef = shallowRef<OrbitControls | null>(null)
const resizeObserverRef = shallowRef<ResizeObserver | null>(null)
const animationFrameRef = shallowRef<number | null>(null)

const pathCurveRef = shallowRef<THREE.CatmullRomCurve3 | null>(null)
const travelerRef = shallowRef<THREE.Group | null>(null)
const travelerLimbsRef = shallowRef<{
  legL: THREE.Group
  legR: THREE.Group
  armL: THREE.Group
  armR: THREE.Group
} | null>(null)
const rotationDummyRef = shallowRef<THREE.Object3D | null>(null)
const cloudWrappersRef = shallowRef<Array<{ mesh: THREE.Group; angle: number; radius: number; speed: number }>>([])
const animationProgressRef = shallowRef(0)
const teardownFns: Array<() => void> = []

const statusText = computed(() => {
  if (!props.data) return '等待轨迹载入'
  const distanceKm = ((props.data.distanceMeters ?? 0) / 1000).toFixed(2)
  return `欢迎来到 ${props.data.title} 岛屿！全长 ${distanceKm} km`
})

const containerClass = computed(() => {
  if (props.mode === 'fullscreen') return 'viewer-shell viewer-shell-fullscreen'
  if (props.mode === 'detail') return 'viewer-shell viewer-shell-detail'
  return 'viewer-shell viewer-shell-embedded'
})

const viewerTitle = computed(() => props.mode === 'detail' ? '轨迹沉浸浏览' : '轨迹三维预览')
const viewerSubtitle = computed(() => props.mode === 'detail'
  ? '可俯瞰轨迹全貌，继续向下浏览路线详情'
  : '上传轨迹后会在这里生成沉浸式岛屿预览')

function disposeScene() {
  if (animationFrameRef.value != null) {
    cancelAnimationFrame(animationFrameRef.value)
  }
  animationFrameRef.value = null

  resizeObserverRef.value?.disconnect()
  resizeObserverRef.value = null

  controlsRef.value?.dispose()
  controlsRef.value = null

  if (rendererRef.value) {
    rendererRef.value.dispose()
    const canvas = rendererRef.value.domElement
    canvas.parentElement?.removeChild(canvas)
  }
  rendererRef.value = null

  sceneRef.value?.traverse((object: THREE.Object3D) => {
    const mesh = object as THREE.Mesh
    if (mesh.geometry) {
      mesh.geometry.dispose()
    }
    const material = mesh.material
    if (Array.isArray(material)) {
      material.forEach((item) => item.dispose())
    } else if (material) {
      material.dispose()
    }
  })

  sceneRef.value = null
  cameraRef.value = null
  pathCurveRef.value = null
  travelerRef.value = null
  travelerLimbsRef.value = null
  rotationDummyRef.value = null
  cloudWrappersRef.value = []
  animationProgressRef.value = 0

  while (teardownFns.length) {
    const fn = teardownFns.pop()
    fn?.()
  }
}

function toLocalWorld(paths: TrackViewerData['paths']) {
  const points = paths.flatMap((path) => path.points)
  let minLat = Infinity
  let maxLat = -Infinity
  let minLng = Infinity
  let maxLng = -Infinity
  let minEle = Infinity
  let maxEle = -Infinity

  for (const point of points) {
    minLat = Math.min(minLat, point.lat)
    maxLat = Math.max(maxLat, point.lat)
    minLng = Math.min(minLng, point.lng)
    maxLng = Math.max(maxLng, point.lng)
    minEle = Math.min(minEle, point.ele ?? 0)
    maxEle = Math.max(maxEle, point.ele ?? 0)
  }

  const mapSize = 50
  const scale = mapSize / Math.max(maxLat - minLat || 1, maxLng - minLng || 1)
  const centerLat = (minLat + maxLat) / 2
  const centerLng = (minLng + maxLng) / 2
  const eleRange = maxEle - minEle || 1

  const convertPoint = (point: { lng: number; lat: number; ele?: number }) => new THREE.Vector3(
    (point.lng - centerLng) * scale,
    (((point.ele ?? 0) - minEle) / eleRange) * 12 + 0.5,
    -(point.lat - centerLat) * scale,
  )

  const worldPaths = paths.map((path) => ({
    ...path,
    vectors: path.points.map(convertPoint),
  }))

  return {
    mapSize,
    worldPaths,
    referencePoints: worldPaths.flatMap((path) => path.vectors),
  }
}

function buildEnvironment(scene: THREE.Scene, mapSize: number) {
  const oceanGeometry = new THREE.PlaneGeometry(mapSize * 20, mapSize * 20)
  const oceanMaterial = new THREE.MeshToonMaterial({ color: '#5dade2', transparent: true, opacity: 0.82 })
  const ocean = new THREE.Mesh(oceanGeometry, oceanMaterial)
  ocean.rotation.x = -Math.PI / 2
  ocean.position.y = -3.6
  scene.add(ocean)

  const sun = new THREE.Mesh(
    new THREE.SphereGeometry(15, 24, 24),
    new THREE.MeshBasicMaterial({ color: '#ffea00' }),
  )
  sun.position.set(80, 100, -100)
  scene.add(sun)

  const cloudMaterial = new THREE.MeshToonMaterial({ color: '#ffffff' })
  const clouds: Array<{ mesh: THREE.Group; angle: number; radius: number; speed: number }> = []
  for (let index = 0; index < 12; index += 1) {
    const cloud = new THREE.Group()
    const puffCount = 3 + Math.round(Math.random() * 2)
    for (let puffIndex = 0; puffIndex < puffCount; puffIndex += 1) {
      const puff = new THREE.Mesh(
        new THREE.SphereGeometry(2 + Math.random() * 2, 8, 8),
        cloudMaterial,
      )
      puff.position.set(
        (Math.random() - 0.5) * 4,
        (Math.random() - 0.5) * 2,
        (Math.random() - 0.5) * 4,
      )
      cloud.add(puff)
    }

    const radius = 50 + Math.random() * 40
    const angle = Math.random() * Math.PI * 2
    cloud.position.set(
      Math.cos(angle) * radius,
      18 + Math.random() * 28,
      Math.sin(angle) * radius,
    )
    scene.add(cloud)
    clouds.push({ mesh: cloud, angle, radius, speed: 0.05 + Math.random() * 0.08 })
  }

  cloudWrappersRef.value = clouds
}

function getTerrainHeight(x: number, z: number, referencePoints: THREE.Vector3[]) {
  let numerator = 0
  let denominator = 0
  let minDistanceSquared = Infinity

  for (const point of referencePoints) {
    const distanceSquared = (x - point.x) ** 2 + (z - point.z) ** 2
    minDistanceSquared = Math.min(minDistanceSquared, distanceSquared)
    const weight = 1 / (Math.pow(distanceSquared, 1.2) + 0.1)
    numerator += point.y * weight
    denominator += weight
  }

  const distanceToTrack = Math.sqrt(minDistanceSquared)
  let height = (numerator / Math.max(denominator, 0.0001)) - (distanceToTrack * 0.3 + Math.pow(distanceToTrack / 6, 2.5)) - 0.15
  if (distanceToTrack > 15) {
    height -= (distanceToTrack - 15) * 5
  }

  return { height, distanceToTrack }
}

function generateTerrain(scene: THREE.Scene, referencePoints: THREE.Vector3[], mapSize: number) {
  if (!referencePoints.length) return

  const segments = 120
  const geometry = new THREE.PlaneGeometry(mapSize * 2.5, mapSize * 2.5, segments, segments)
  geometry.rotateX(-Math.PI / 2)

  const positions = geometry.getAttribute('position')
  if (!(positions instanceof THREE.BufferAttribute)) {
    return
  }
  const colors = new Float32Array(positions.count * 3)

  const grass = new THREE.Color('#88d4ab')
  const rock = new THREE.Color('#d1c7b7')
  const snow = new THREE.Color('#ffffff')
  const ocean = new THREE.Color('#5dade2')

  for (let index = 0; index < positions.count; index += 1) {
    const x = positions.getX(index)
    const z = positions.getZ(index)
    const { height } = getTerrainHeight(x, z, referencePoints)
    positions.setY(index, height)

    const color = new THREE.Color()
    if (height < -3) {
      color.lerpColors(rock, ocean, Math.min(1, (-height - 3) / 10))
    } else if (height < 2) {
      const noise = (Math.sin(x * 5) + Math.cos(z * 5)) * 0.03
      color.copy(grass)
      color.r += noise
      color.g += noise
      color.b += noise
    } else if (height < 8) {
      color.lerpColors(grass, rock, (height - 2) / 6)
    } else {
      color.lerpColors(rock, snow, Math.min(1, (height - 8) / 4))
    }

    colors[index * 3] = color.r
    colors[index * 3 + 1] = color.g
    colors[index * 3 + 2] = color.b
  }

  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.computeVertexNormals()

  const terrainMaterial = new THREE.MeshToonMaterial({ vertexColors: true })

  const terrain = new THREE.Mesh(geometry, terrainMaterial)
  terrain.receiveShadow = true
  scene.add(terrain)
}

function generateDecorations(scene: THREE.Scene, referencePoints: THREE.Vector3[], mapSize: number) {
  if (!referencePoints.length) return

  const treeLeafMaterial = new THREE.MeshToonMaterial({ color: '#5eb376' })
  const treeTrunkMaterial = new THREE.MeshToonMaterial({ color: '#8b5a2b' })
  const rockMaterial = new THREE.MeshToonMaterial({ color: '#b0bec5' })

  const leafGeometry = new THREE.IcosahedronGeometry(0.3, 0)
  const trunkGeometry = new THREE.CylinderGeometry(0.06, 0.1, 0.4, 6)
  const rockGeometry = new THREE.DodecahedronGeometry(0.25, 0)

  for (let index = 0; index < 260; index += 1) {
    const x = (Math.random() - 0.5) * 2 * mapSize
    const z = (Math.random() - 0.5) * 2 * mapSize
    const { height, distanceToTrack } = getTerrainHeight(x, z, referencePoints)

    if (distanceToTrack <= 1.2 || distanceToTrack >= 12 || height <= -1 || height >= 8) {
      continue
    }

    const decoration = new THREE.Group()
    const random = Math.random()
    if (random > 0.5) {
      const trunk = new THREE.Mesh(trunkGeometry, treeTrunkMaterial)
      trunk.position.y = 0.2
      trunk.castShadow = true

      const leaf = new THREE.Mesh(leafGeometry, treeLeafMaterial)
      leaf.position.y = 0.5
      leaf.scale.set(1 + Math.random() * 0.5, 1 + Math.random() * 0.5, 1 + Math.random() * 0.5)
      leaf.rotation.set(Math.random(), Math.random(), Math.random())
      leaf.castShadow = true
      decoration.add(trunk, leaf)
    } else {
      const rock = new THREE.Mesh(rockGeometry, rockMaterial)
      rock.position.y = 0.1
      rock.scale.set(1 + Math.random(), 0.5 + Math.random() * 0.5, 1 + Math.random())
      rock.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, 0)
      rock.castShadow = true
      decoration.add(rock)
    }

    decoration.position.set(x, height, z)
    const scale = 0.5 + Math.random() * 0.7
    decoration.scale.set(scale, scale, scale)
    scene.add(decoration)
  }
}

function createTraveler() {
  const group = new THREE.Group()
  const skinMaterial = new THREE.MeshToonMaterial({ color: '#ffcc99' })
  const shirtMaterial = new THREE.MeshToonMaterial({ color: '#ff6b6b' })
  const pantsMaterial = new THREE.MeshToonMaterial({ color: '#4ecdc4' })

  const head = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.3, 0.3), skinMaterial)
  head.position.y = 0.6
  head.castShadow = true
  group.add(head)

  const body = new THREE.Mesh(new THREE.BoxGeometry(0.25, 0.3, 0.15), shirtMaterial)
  body.position.y = 0.3
  body.castShadow = true
  group.add(body)

  const createLimb = (material: THREE.Material, yOffset: number) => {
    const limb = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.22, 0.1), material)
    limb.position.y = yOffset
    limb.castShadow = true
    const pivot = new THREE.Group()
    pivot.add(limb)
    return pivot
  }

  const legL = createLimb(pantsMaterial, -0.1)
  legL.position.set(-0.08, 0.15, 0)
  const legR = createLimb(pantsMaterial, -0.1)
  legR.position.set(0.08, 0.15, 0)
  const armL = createLimb(skinMaterial, -0.1)
  armL.position.set(-0.18, 0.4, 0)
  const armR = createLimb(skinMaterial, -0.1)
  armR.position.set(0.18, 0.4, 0)
  group.add(legL, legR, armL, armR)

  travelerLimbsRef.value = { legL, legR, armL, armR }
  travelerRef.value = group
}

function buildPathMeshes(scene: THREE.Scene, worldPaths: Array<{ color?: string; vectors: THREE.Vector3[] }>) {
  let longestPath: THREE.Vector3[] | null = null

  for (const path of worldPaths) {
    if (path.vectors.length < 2) continue
    const curve = new THREE.CatmullRomCurve3(path.vectors)
    const tube = new THREE.Mesh(
      new THREE.TubeGeometry(curve, Math.max(path.vectors.length * 2, 60), 0.08, 8, false),
      new THREE.MeshToonMaterial({
        color: path.color || '#ff5f96',
        emissive: path.color || '#ff5f96',
        emissiveIntensity: 0.12,
      }),
    )
    tube.castShadow = true
    tube.position.y += 0.05
    scene.add(tube)

    if (!longestPath || path.vectors.length > longestPath.length) {
      longestPath = path.vectors
    }
  }

  if (!longestPath || longestPath.length < 2) return

  pathCurveRef.value = new THREE.CatmullRomCurve3(longestPath)
  createTraveler()
  if (travelerRef.value) {
    scene.add(travelerRef.value)
  }
}

function updateTraveler() {
  const curve = pathCurveRef.value
  const traveler = travelerRef.value
  const limbs = travelerLimbsRef.value
  const rotationDummy = rotationDummyRef.value
  if (!curve || !traveler || !limbs || !rotationDummy) return

  animationProgressRef.value += 0.0009
  if (animationProgressRef.value > 1) {
    animationProgressRef.value = 0
  }

  const position = curve.getPointAt(animationProgressRef.value)
  const nextPosition = curve.getPointAt(Math.min(animationProgressRef.value + 0.01, 1))

  traveler.position.copy(position)
  rotationDummy.position.copy(position)
  rotationDummy.lookAt(new THREE.Vector3(nextPosition.x, position.y, nextPosition.z))
  traveler.quaternion.slerp(rotationDummy.quaternion, 0.1)

  const swing = Math.sin(animationProgressRef.value * 60)
  limbs.legL.rotation.x = swing * 0.6
  limbs.legR.rotation.x = -swing * 0.6
  limbs.armL.rotation.x = -swing * 0.6
  limbs.armR.rotation.x = swing * 0.6
  traveler.position.y += Math.abs(Math.sin(animationProgressRef.value * 60)) * 0.05
}

function updateClouds(delta: number) {
  for (const cloud of cloudWrappersRef.value) {
    cloud.angle += delta * cloud.speed
    cloud.mesh.position.x = Math.cos(cloud.angle) * cloud.radius
    cloud.mesh.position.z = Math.sin(cloud.angle) * cloud.radius
  }
}

function setOverviewView() {
  const camera = cameraRef.value
  const controls = controlsRef.value
  if (!camera || !controls) return
  camera.position.set(0, 56, 0.1)
  controls.target.set(0, 4, 0)
  controls.update()
}

function animate() {
  const renderer = rendererRef.value
  const scene = sceneRef.value
  const camera = cameraRef.value
  const controls = controlsRef.value
  if (!renderer || !scene || !camera || !controls) return

  const delta = 1 / 60
  updateClouds(delta)
  updateTraveler()
  controls.update()
  renderer.render(scene, camera)
  animationFrameRef.value = requestAnimationFrame(animate)
}

function handleResize() {
  const container = containerRef.value
  const renderer = rendererRef.value
  const camera = cameraRef.value
  if (!container || !renderer || !camera) return

  const width = container.clientWidth
  const height = container.clientHeight
  if (!width || !height) return

  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
  renderer.setPixelRatio(window.devicePixelRatio)
}

async function initScene() {
  if (!props.data || !containerRef.value) return

  disposeScene()
  await nextTick()

  const container = containerRef.value
  if (!container) return

  const scene = new THREE.Scene()
  scene.background = new THREE.Color('#82c8fa')
  scene.fog = new THREE.FogExp2('#82c8fa', 0.008)

  const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 1000)
  camera.position.set(0, 30, 45)

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.maxPolarAngle = Math.PI / 2 - 0.02

  const ambientLight = new THREE.AmbientLight('#ffffff', 0.6)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight('#ffffff', 1.2)
  directionalLight.position.set(30, 60, 40)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 2048
  directionalLight.shadow.mapSize.height = 2048
  directionalLight.shadow.camera.near = 0.5
  directionalLight.shadow.camera.far = 200
  const shadowSize = 40
  directionalLight.shadow.camera.left = -shadowSize
  directionalLight.shadow.camera.right = shadowSize
  directionalLight.shadow.camera.top = shadowSize
  directionalLight.shadow.camera.bottom = -shadowSize
  directionalLight.shadow.bias = -0.001
  scene.add(directionalLight)

  rotationDummyRef.value = new THREE.Object3D()

  const { mapSize, worldPaths, referencePoints } = toLocalWorld(props.data.paths)
  buildEnvironment(scene, mapSize)
  generateTerrain(scene, referencePoints.filter((_, index) => index % Math.max(Math.ceil(referencePoints.length / 800), 1) === 0), mapSize)
  generateDecorations(scene, referencePoints.filter((_, index) => index % Math.max(Math.ceil(referencePoints.length / 800), 1) === 0), mapSize)
  buildPathMeshes(scene, worldPaths)

  controls.maxDistance = mapSize * 2.2
  controls.target.set(0, 4, 0)
  controls.update()

  sceneRef.value = scene
  cameraRef.value = camera
  rendererRef.value = renderer
  controlsRef.value = controls

  resizeObserverRef.value = new ResizeObserver(() => handleResize())
  resizeObserverRef.value.observe(container)
  handleResize()
  animate()
}

watch(
  () => [props.data, props.mode] as const,
  () => {
    if (!props.data) {
      disposeScene()
      return
    }
    void initScene()
  },
  { immediate: true, deep: true },
)

onMounted(() => {
  if (props.data) {
    void initScene()
  }
})

onBeforeUnmount(() => {
  disposeScene()
})
</script>

<template>
  <section class="track-viewer-frame animate-fade-in-up">
    <div class="track-viewer-header">
      <div>
        <h3 class="track-viewer-title">{{ viewerTitle }}</h3>
        <p class="track-viewer-subtitle">{{ viewerSubtitle }}</p>
      </div>
      <button
        v-if="showFullscreenButton && mode !== 'fullscreen'"
        type="button"
        class="track-action-button"
        @click="emit('requestFullscreen')"
      >
        <BaseIcon name="Maximize2" :size="16" />
        全屏
      </button>
      <button
        v-else-if="mode === 'fullscreen'"
        type="button"
        class="track-action-button"
        @click="emit('exitFullscreen')"
      >
        <BaseIcon name="Minimize2" :size="16" />
        退出全屏
      </button>
    </div>

    <div class="track-viewer-surface" :class="containerClass">
      <div ref="container" class="viewer-canvas-host"></div>

      <div class="status-pill">
        {{ statusText }}
      </div>

      <div class="viewer-actions">
        <button type="button" class="overview-button" @click="setOverviewView">
          <BaseIcon name="Plane" :size="16" />
          俯瞰全景
        </button>
      </div>

      <button
        v-if="showScrollToContentButton"
        type="button"
        class="scroll-button"
        @click="emit('scrollToContent')"
      >
        <BaseIcon name="ChevronsDown" :size="16" />
        浏览下方内容
      </button>
    </div>
  </section>
</template>

<style scoped>
.track-viewer-frame {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.track-viewer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.track-viewer-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.track-viewer-subtitle {
  margin-top: 0.2rem;
  font-size: 0.82rem;
  color: var(--text-secondary);
}

.track-viewer-surface {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--primary-500) 18%, transparent);
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--primary-500) 18%, transparent), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.18));
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
}

.viewer-shell-embedded {
  height: 25rem;
}

.viewer-shell-detail {
  height: min(82vh, 46rem);
}

.viewer-shell-fullscreen {
  height: calc(100vh - 3rem);
}

.viewer-canvas-host {
  height: 100%;
  width: 100%;
}

.status-pill {
  position: absolute;
  left: 50%;
  top: 1rem;
  transform: translateX(-50%);
  max-width: min(82%, 40rem);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.88);
  padding: 0.85rem 1.35rem;
  text-align: center;
  font-size: 0.92rem;
  font-weight: 700;
  color: #5a5a5a;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.viewer-actions {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
}

.overview-button,
.track-action-button,
.scroll-button {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  border: none;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.overview-button,
.scroll-button {
  border-radius: 999px;
  padding: 0.9rem 1.1rem;
  color: #fff;
  background: linear-gradient(135deg, #ff9cee, #b28dff);
  box-shadow: 0 12px 28px rgba(178, 141, 255, 0.35);
}

.track-action-button {
  border-radius: 999px;
  padding: 0.72rem 0.95rem;
  color: var(--primary-500);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}

.overview-button:hover,
.track-action-button:hover,
.scroll-button:hover {
  transform: translateY(-1px);
}

.scroll-button {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
}

@media (max-width: 768px) {
  .viewer-shell-detail {
    height: 75vh;
  }

  .status-pill {
    top: 0.75rem;
    max-width: 88%;
    font-size: 0.8rem;
    padding: 0.75rem 1rem;
  }

  .track-viewer-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .scroll-button {
    top: auto;
    right: 1rem;
    bottom: 4.8rem;
    transform: none;
  }
}
</style>
