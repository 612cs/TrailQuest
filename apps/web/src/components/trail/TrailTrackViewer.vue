<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, useTemplateRef, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

import BaseIcon from '../common/BaseIcon.vue'
import type { TrackViewerData, TrackViewerMode, TrackViewerPoint, TrackWeatherScene } from '../../types/trackViewer'

interface WorldPathData {
  name?: string
  color?: string
  points: TrackViewerPoint[]
  vectors: THREE.Vector3[]
}

interface MarkerDatum {
  label: string
  detail: string
  root: THREE.Object3D
  hitMesh: THREE.Mesh
  preferredScale: number
}

interface CloudWrapper {
  mesh: THREE.Group
  angle: number
  radius: number
  speed: number
}

interface BirdFlockWrapper {
  group: THREE.Group
  angle: number
  radius: number
  speed: number
  baseHeight: number
  hitMesh: THREE.Mesh
  climbMeters: number
}

interface PrecipitationState {
  points: THREE.Points
  velocities: Float32Array
  lowerBound: number
  upperBound: number
}

interface TooltipState {
  visible: boolean
  title: string
  detail: string
  left: number
  top: number
}

interface WeatherPreset {
  background: string
  fogColor: string
  fogDensity: number
  oceanColor: string
  oceanOpacity: number
  ambientColor: string
  ambientIntensity: number
  directionalColor: string
  directionalIntensity: number
  sunVisible: boolean
  cloudColor: string
  cloudCount: number
  cloudHeight: [number, number]
  cloudSpeedMultiplier: number
  precipitation: 'rain' | 'snow' | null
}

const props = withDefaults(defineProps<{
  data: TrackViewerData | null
  mode?: TrackViewerMode
  weatherScene?: TrackWeatherScene
  showFullscreenButton?: boolean
  showScrollToContentButton?: boolean
}>(), {
  mode: 'embedded',
  weatherScene: 'partly_cloudy',
  showFullscreenButton: false,
  showScrollToContentButton: false,
})

const emit = defineEmits<{
  (event: 'requestFullscreen'): void
  (event: 'exitFullscreen'): void
  (event: 'scrollToContent'): void
}>()

const surfaceRef = useTemplateRef<HTMLDivElement>('surface')
const containerRef = useTemplateRef<HTMLDivElement>('container')

const rendererRef = shallowRef<THREE.WebGLRenderer | null>(null)
const sceneRef = shallowRef<THREE.Scene | null>(null)
const cameraRef = shallowRef<THREE.PerspectiveCamera | null>(null)
const controlsRef = shallowRef<OrbitControls | null>(null)
const resizeObserverRef = shallowRef<ResizeObserver | null>(null)
const animationFrameRef = shallowRef<number | null>(null)
const clockRef = shallowRef<THREE.Clock | null>(null)

const pathCurveRef = shallowRef<THREE.CatmullRomCurve3 | null>(null)
const travelerRef = shallowRef<THREE.Group | null>(null)
const travelerLimbsRef = shallowRef<{
  legL: THREE.Group
  legR: THREE.Group
  armL: THREE.Group
  armR: THREE.Group
} | null>(null)
const rotationDummyRef = shallowRef<THREE.Object3D | null>(null)
const cloudWrappersRef = shallowRef<CloudWrapper[]>([])
const birdFlocksRef = shallowRef<BirdFlockWrapper[]>([])
const precipitationRef = shallowRef<PrecipitationState | null>(null)
const interactionTargetsRef = shallowRef<MarkerDatum[]>([])
const activeTargetRef = shallowRef<MarkerDatum | null>(null)
const pointerVectorRef = shallowRef(new THREE.Vector2(-100, -100))
const pointerLocalRef = shallowRef({ x: 0, y: 0 })
const animationProgressRef = shallowRef(0)

const tooltip = ref<TooltipState>({
  visible: false,
  title: '',
  detail: '',
  left: 0,
  top: 0,
})

const prefersHover = typeof window !== 'undefined'
  ? window.matchMedia('(hover: hover) and (pointer: fine)')
  : null

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

function buildWeatherPreset(sceneType: TrackWeatherScene): WeatherPreset {
  switch (sceneType) {
    case 'clear':
      return {
        background: '#78c9ff',
        fogColor: '#78c9ff',
        fogDensity: 0.006,
        oceanColor: '#5dade2',
        oceanOpacity: 0.8,
        ambientColor: '#fff9d8',
        ambientIntensity: 0.75,
        directionalColor: '#fff7c2',
        directionalIntensity: 1.35,
        sunVisible: true,
        cloudColor: '#ffffff',
        cloudCount: 4,
        cloudHeight: [24, 34],
        cloudSpeedMultiplier: 0.8,
        precipitation: null,
      }
    case 'overcast':
      return {
        background: '#8ea5ba',
        fogColor: '#8ea5ba',
        fogDensity: 0.012,
        oceanColor: '#4f7f9b',
        oceanOpacity: 0.9,
        ambientColor: '#dfe6ee',
        ambientIntensity: 0.6,
        directionalColor: '#d2d9e1',
        directionalIntensity: 0.55,
        sunVisible: false,
        cloudColor: '#9da9b5',
        cloudCount: 18,
        cloudHeight: [16, 28],
        cloudSpeedMultiplier: 1.1,
        precipitation: null,
      }
    case 'rain':
      return {
        background: '#6f8ca2',
        fogColor: '#6f8ca2',
        fogDensity: 0.016,
        oceanColor: '#416a86',
        oceanOpacity: 0.92,
        ambientColor: '#d8e1ea',
        ambientIntensity: 0.55,
        directionalColor: '#d9e4ef',
        directionalIntensity: 0.48,
        sunVisible: false,
        cloudColor: '#7b8793',
        cloudCount: 22,
        cloudHeight: [14, 24],
        cloudSpeedMultiplier: 1.35,
        precipitation: 'rain',
      }
    case 'snow':
      return {
        background: '#c7d5e4',
        fogColor: '#c7d5e4',
        fogDensity: 0.015,
        oceanColor: '#7ba0bc',
        oceanOpacity: 0.9,
        ambientColor: '#eef4fb',
        ambientIntensity: 0.68,
        directionalColor: '#edf3f8',
        directionalIntensity: 0.6,
        sunVisible: false,
        cloudColor: '#ccd5df',
        cloudCount: 16,
        cloudHeight: [18, 30],
        cloudSpeedMultiplier: 0.95,
        precipitation: 'snow',
      }
    case 'windy':
      return {
        background: '#88bfe6',
        fogColor: '#88bfe6',
        fogDensity: 0.009,
        oceanColor: '#4c93c4',
        oceanOpacity: 0.84,
        ambientColor: '#eef5fb',
        ambientIntensity: 0.7,
        directionalColor: '#fff0d8',
        directionalIntensity: 1.05,
        sunVisible: true,
        cloudColor: '#edf1f5',
        cloudCount: 10,
        cloudHeight: [20, 32],
        cloudSpeedMultiplier: 1.9,
        precipitation: null,
      }
    case 'partly_cloudy':
    default:
      return {
        background: '#82c8fa',
        fogColor: '#82c8fa',
        fogDensity: 0.008,
        oceanColor: '#5dade2',
        oceanOpacity: 0.82,
        ambientColor: '#ffffff',
        ambientIntensity: 0.65,
        directionalColor: '#ffffff',
        directionalIntensity: 1.15,
        sunVisible: true,
        cloudColor: '#ffffff',
        cloudCount: 10,
        cloudHeight: [18, 30],
        cloudSpeedMultiplier: 1,
        precipitation: null,
      }
  }
}

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
    rendererRef.value.domElement.parentElement?.removeChild(rendererRef.value.domElement)
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
  clockRef.value = null
  pathCurveRef.value = null
  travelerRef.value = null
  travelerLimbsRef.value = null
  rotationDummyRef.value = null
  cloudWrappersRef.value = []
  birdFlocksRef.value = []
  precipitationRef.value = null
  interactionTargetsRef.value = []
  activeTargetRef.value = null
  animationProgressRef.value = 0
  tooltip.value = { visible: false, title: '', detail: '', left: 0, top: 0 }
}

function haversine(start: TrackViewerPoint, end: TrackViewerPoint) {
  const toRad = (value: number) => (value * Math.PI) / 180
  const lat1 = toRad(start.lat)
  const lat2 = toRad(end.lat)
  const deltaLat = lat2 - lat1
  const deltaLng = toRad(end.lng - start.lng)
  const a = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) ** 2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return 6371000 * c
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

  const convertPoint = (point: TrackViewerPoint) => new THREE.Vector3(
    (point.lng - centerLng) * scale,
    (((point.ele ?? 0) - minEle) / eleRange) * 12 + 0.5,
    -(point.lat - centerLat) * scale,
  )

  const worldPaths: WorldPathData[] = paths.map((path) => ({
    ...path,
    vectors: path.points.map(convertPoint),
  }))

  return {
    mapSize,
    worldPaths,
    referencePoints: worldPaths.flatMap((path) => path.vectors),
  }
}

function createPrecipitation(scene: THREE.Scene, mapSize: number, preset: WeatherPreset) {
  if (!preset.precipitation) {
    precipitationRef.value = null
    return
  }

  const count = preset.precipitation === 'rain' ? 1200 : 900
  const positions = new Float32Array(count * 3)
  const velocities = new Float32Array(count)
  const lowerBound = -6
  const upperBound = 38

  for (let index = 0; index < count; index += 1) {
    positions[index * 3] = (Math.random() - 0.5) * mapSize * 5
    positions[index * 3 + 1] = Math.random() * (upperBound - lowerBound) + lowerBound
    positions[index * 3 + 2] = (Math.random() - 0.5) * mapSize * 5
    velocities[index] = preset.precipitation === 'rain'
      ? 16 + Math.random() * 10
      : 3 + Math.random() * 2
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))

  const material = new THREE.PointsMaterial({
    color: preset.precipitation === 'rain' ? '#d9f1ff' : '#ffffff',
    size: preset.precipitation === 'rain' ? 0.11 : 0.22,
    transparent: true,
    opacity: preset.precipitation === 'rain' ? 0.6 : 0.9,
  })

  const points = new THREE.Points(geometry, material)
  scene.add(points)
  precipitationRef.value = { points, velocities, lowerBound, upperBound }
}

function buildEnvironment(scene: THREE.Scene, mapSize: number, preset: WeatherPreset) {
  const oceanGeometry = new THREE.PlaneGeometry(mapSize * 20, mapSize * 20)
  const oceanMaterial = new THREE.MeshToonMaterial({
    color: preset.oceanColor,
    transparent: true,
    opacity: preset.oceanOpacity,
  })
  const ocean = new THREE.Mesh(oceanGeometry, oceanMaterial)
  ocean.rotation.x = -Math.PI / 2
  ocean.position.y = -3.6
  scene.add(ocean)

  if (preset.sunVisible) {
    const sun = new THREE.Mesh(
      new THREE.SphereGeometry(15, 24, 24),
      new THREE.MeshBasicMaterial({ color: '#ffea00' }),
    )
    sun.position.set(80, 100, -100)
    scene.add(sun)
  }

  const cloudMaterial = new THREE.MeshToonMaterial({ color: preset.cloudColor })
  const clouds: CloudWrapper[] = []
  for (let index = 0; index < preset.cloudCount; index += 1) {
    const cloud = new THREE.Group()
    const puffCount = 3 + Math.round(Math.random() * 3)
    for (let puffIndex = 0; puffIndex < puffCount; puffIndex += 1) {
      const puff = new THREE.Mesh(
        new THREE.SphereGeometry(2 + Math.random() * 2.2, 8, 8),
        cloudMaterial,
      )
      puff.position.set(
        (Math.random() - 0.5) * 5,
        (Math.random() - 0.5) * 2.5,
        (Math.random() - 0.5) * 5,
      )
      cloud.add(puff)
    }

    const radius = 48 + Math.random() * 46
    const angle = Math.random() * Math.PI * 2
    const cloudHeight = preset.cloudHeight[0] + Math.random() * (preset.cloudHeight[1] - preset.cloudHeight[0])
    cloud.position.set(Math.cos(angle) * radius, cloudHeight, Math.sin(angle) * radius)
    scene.add(cloud)
    clouds.push({
      mesh: cloud,
      angle,
      radius,
      speed: (0.04 + Math.random() * 0.07) * preset.cloudSpeedMultiplier,
    })
  }

  cloudWrappersRef.value = clouds
  createPrecipitation(scene, mapSize, preset)
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

function generateTerrain(scene: THREE.Scene, referencePoints: THREE.Vector3[], mapSize: number, weatherScene: TrackWeatherScene) {
  if (!referencePoints.length) return

  const segments = 120
  const geometry = new THREE.PlaneGeometry(mapSize * 2.5, mapSize * 2.5, segments, segments)
  geometry.rotateX(-Math.PI / 2)

  const positions = geometry.getAttribute('position')
  if (!(positions instanceof THREE.BufferAttribute)) return
  const colors = new Float32Array(positions.count * 3)

  const grass = new THREE.Color(weatherScene === 'snow' ? '#bed9c8' : '#88d4ab')
  const rock = new THREE.Color('#d1c7b7')
  const snow = new THREE.Color('#ffffff')
  const ocean = new THREE.Color(weatherScene === 'rain' ? '#4b748f' : '#5dade2')

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

    if (weatherScene === 'snow' && height > 3) {
      color.lerp(snow, 0.4)
    }

    colors[index * 3] = color.r
    colors[index * 3 + 1] = color.g
    colors[index * 3 + 2] = color.b
  }

  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.computeVertexNormals()

  const terrain = new THREE.Mesh(
    geometry,
    new THREE.MeshToonMaterial({ vertexColors: true }),
  )
  terrain.receiveShadow = true
  scene.add(terrain)
}

function generateDecorations(scene: THREE.Scene, referencePoints: THREE.Vector3[], mapSize: number) {
  if (!referencePoints.length) return

  const treeLeafMaterial = new THREE.MeshToonMaterial({ color: '#5eb376' })
  const treeTrunkMaterial = new THREE.MeshToonMaterial({ color: '#8b5a2b' })
  const rockMaterial = new THREE.MeshToonMaterial({ color: '#b0bec5' })
  const bushMaterial = new THREE.MeshToonMaterial({ color: '#6bbf83' })

  const leafGeometry = new THREE.IcosahedronGeometry(0.3, 0)
  const trunkGeometry = new THREE.CylinderGeometry(0.06, 0.1, 0.4, 6)
  const rockGeometry = new THREE.DodecahedronGeometry(0.25, 0)

  for (let index = 0; index < 260; index += 1) {
    const x = (Math.random() - 0.5) * 2 * mapSize
    const z = (Math.random() - 0.5) * 2 * mapSize
    const { height, distanceToTrack } = getTerrainHeight(x, z, referencePoints)

    if (distanceToTrack <= 1.2 || distanceToTrack >= 12 || height <= -1 || height >= 8) continue

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
    } else if (random > 0.2) {
      const rock = new THREE.Mesh(rockGeometry, rockMaterial)
      rock.position.y = 0.1
      rock.scale.set(1 + Math.random(), 0.5 + Math.random() * 0.5, 1 + Math.random())
      rock.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, 0)
      rock.castShadow = true
      decoration.add(rock)
    } else {
      const bush = new THREE.Mesh(rockGeometry, bushMaterial)
      bush.position.y = 0.08
      bush.scale.set(1.5, 0.6, 1.5)
      bush.castShadow = true
      decoration.add(bush)
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

function createMarkerCanvas(label: string, detail: string, accent: string, isPeak: boolean) {
  const canvas = document.createElement('canvas')
  canvas.width = isPeak ? 460 : 400
  canvas.height = 196
  const context = canvas.getContext('2d')
  if (!context) return canvas

  context.fillStyle = isPeak ? 'rgba(255, 247, 214, 0.96)' : 'rgba(255, 255, 255, 0.95)'
  context.strokeStyle = accent
  context.lineWidth = 10
  const radius = 20
  context.beginPath()
  context.moveTo(radius, 0)
  context.lineTo(canvas.width - radius, 0)
  context.quadraticCurveTo(canvas.width, 0, canvas.width, radius)
  context.lineTo(canvas.width, canvas.height - radius)
  context.quadraticCurveTo(canvas.width, canvas.height, canvas.width - radius, canvas.height)
  context.lineTo(radius, canvas.height)
  context.quadraticCurveTo(0, canvas.height, 0, canvas.height - radius)
  context.lineTo(0, radius)
  context.quadraticCurveTo(0, 0, radius, 0)
  context.closePath()
  context.fill()
  context.stroke()

  context.textAlign = 'center'
  context.fillStyle = isPeak ? '#c44a13' : '#425466'
  context.font = 'bold 48px "Comic Sans MS", "Microsoft YaHei", sans-serif'
  context.fillText(label, canvas.width / 2, 84)
  context.fillStyle = accent
  context.font = 'bold 40px "Comic Sans MS", "Microsoft YaHei", sans-serif'
  context.fillText(detail, canvas.width / 2, 150)

  return canvas
}

function createMarkerSprite(options: {
  label: string
  detail: string
  accent: string
  isPeak?: boolean
  scale?: number
}) {
  const group = new THREE.Group()

  const postHeight = options.isPeak ? 1.05 : 0.62
  const post = new THREE.Mesh(
    new THREE.CylinderGeometry(0.03, 0.03, postHeight, 8),
    new THREE.MeshToonMaterial({ color: options.isPeak ? '#d08f20' : '#8b5a2b' }),
  )
  post.position.y = postHeight / 2
  post.castShadow = true
  group.add(post)

  const texture = new THREE.CanvasTexture(
    createMarkerCanvas(options.label, options.detail, options.accent, Boolean(options.isPeak)),
  )
  texture.minFilter = THREE.LinearFilter
  const material = new THREE.SpriteMaterial({ map: texture, transparent: true })
  const sprite = new THREE.Sprite(material)
  const scale = options.scale ?? (options.isPeak ? 1.1 : 0.84)
  sprite.position.y = options.isPeak ? 1.22 : 0.82
  sprite.scale.set(scale, scale * 0.48, 1)
  group.add(sprite)

  const hitMesh = new THREE.Mesh(
    new THREE.SphereGeometry(options.isPeak ? 1.25 : 1.05, 10, 10),
    new THREE.MeshBasicMaterial({ visible: false }),
  )
  hitMesh.position.y = options.isPeak ? 0.9 : 0.65
  group.add(hitMesh)

  return { group, hitMesh }
}

function buildMilestoneMarkers(scene: THREE.Scene, mainPath: WorldPathData) {
  if (mainPath.points.length < 2 || mainPath.vectors.length < 2) return

  const targets: MarkerDatum[] = []

  const firstPoint = mainPath.points[0]
  const firstVector = mainPath.vectors[0]
  if (firstPoint && firstVector) {
    const startMarker = createMarkerSprite({
      label: '起点',
      detail: `海拔 ${Math.round(firstPoint.ele ?? 0)} m`,
      accent: '#8e6dff',
    })
    startMarker.group.position.copy(firstVector)
    scene.add(startMarker.group)
    targets.push({
      label: '起点',
      detail: `海拔 ${Math.round(firstPoint.ele ?? 0)} m`,
      root: startMarker.group,
      hitMesh: startMarker.hitMesh,
      preferredScale: 1.16,
    })
  }

  let accumulatedDistance = 0
  let nextMarkerAt = 1000
  let kilometerIndex = 1
  let highestPoint = mainPath.points[0] ?? null
  let highestVector = mainPath.vectors[0] ?? null

  for (let index = 1; index < mainPath.points.length; index += 1) {
    const previousPoint = mainPath.points[index - 1]
    const currentPoint = mainPath.points[index]
    const currentVector = mainPath.vectors[index]
    if (!previousPoint || !currentPoint || !currentVector) continue

    if ((currentPoint.ele ?? 0) > (highestPoint?.ele ?? 0)) {
      highestPoint = currentPoint
      highestVector = currentVector
    }

    accumulatedDistance += haversine(previousPoint, currentPoint)

    while (accumulatedDistance >= nextMarkerAt) {
      const marker = createMarkerSprite({
        label: `第 ${kilometerIndex} 公里`,
        detail: `海拔 ${Math.round(currentPoint.ele ?? 0)} m`,
        accent: '#ff7a7a',
      })
      marker.group.position.copy(currentVector).add(new THREE.Vector3(0, 0.1, 0))
      scene.add(marker.group)
      targets.push({
        label: `第 ${kilometerIndex} 公里`,
        detail: `海拔 ${Math.round(currentPoint.ele ?? 0)} m`,
        root: marker.group,
        hitMesh: marker.hitMesh,
        preferredScale: 1.16,
      })
      nextMarkerAt += 1000
      kilometerIndex += 1
    }
  }

  if (highestPoint && highestVector) {
    const peakMarker = createMarkerSprite({
      label: '最高点',
      detail: `${Math.round(highestPoint.ele ?? 0)} m`,
      accent: '#f0672a',
      isPeak: true,
      scale: 1.08,
    })
    peakMarker.group.position.copy(highestVector).add(new THREE.Vector3(0, 0.22, 0))
    scene.add(peakMarker.group)
    targets.push({
      label: '最高点',
      detail: `海拔 ${Math.round(highestPoint.ele ?? 0)} m`,
      root: peakMarker.group,
      hitMesh: peakMarker.hitMesh,
      preferredScale: 1.24,
    })
  }

  interactionTargetsRef.value = [...interactionTargetsRef.value, ...targets]
}

function createBirdMesh() {
  const bird = new THREE.Group()
  const bodyMaterial = new THREE.MeshToonMaterial({ color: '#25364f' })
  const wingMaterial = new THREE.MeshToonMaterial({ color: '#425f7d' })

  const body = new THREE.Mesh(new THREE.SphereGeometry(0.18, 10, 10), bodyMaterial)
  body.scale.set(1.3, 0.7, 0.8)
  bird.add(body)

  const wingGeometry = new THREE.BoxGeometry(0.48, 0.05, 0.16)
  const wingLeft = new THREE.Mesh(wingGeometry, wingMaterial)
  wingLeft.position.set(-0.18, 0.02, 0)
  wingLeft.rotation.z = Math.PI / 5

  const wingRight = new THREE.Mesh(wingGeometry, wingMaterial)
  wingRight.position.set(0.18, 0.02, 0)
  wingRight.rotation.z = -Math.PI / 5

  const beak = new THREE.Mesh(new THREE.ConeGeometry(0.05, 0.14, 6), new THREE.MeshToonMaterial({ color: '#f39c12' }))
  beak.position.set(0.26, -0.01, 0)
  beak.rotation.z = -Math.PI / 2

  bird.add(wingLeft, wingRight, beak)
  return bird
}

function createBirdFlocks(scene: THREE.Scene, mapSize: number, elevationGainMeters: number, weatherScene: TrackWeatherScene) {
  const groupCount = Math.min(8, Math.max(0, Math.floor(elevationGainMeters / 200)))
  if (!groupCount) {
    birdFlocksRef.value = []
    return
  }

  const wrappers: BirdFlockWrapper[] = []
  const targets: MarkerDatum[] = []
  const speedMultiplier = weatherScene === 'windy' ? 1.8 : 1

  for (let index = 0; index < groupCount; index += 1) {
    const flock = new THREE.Group()
    const birdA = createBirdMesh()
    const birdB = createBirdMesh()
    birdA.position.set(-0.48, 0, 0)
    birdB.position.set(0.48, 0.12, -0.18)
    birdB.scale.setScalar(0.92)
    flock.add(birdA, birdB)

    const baseHeight = 12 + index * 0.75
    const radius = mapSize * (1.25 + index * 0.08)
    const angle = (index / Math.max(groupCount, 1)) * Math.PI * 2
    flock.position.set(Math.cos(angle) * radius, baseHeight, Math.sin(angle) * radius)

    const hitMesh = new THREE.Mesh(
      new THREE.SphereGeometry(1.15, 10, 10),
      new THREE.MeshBasicMaterial({ visible: false }),
    )
    flock.add(hitMesh)
    scene.add(flock)

    const climbMeters = (index + 1) * 200
    wrappers.push({
      group: flock,
      angle,
      radius,
      speed: (0.22 + index * 0.015) * speedMultiplier,
      baseHeight,
      hitMesh,
      climbMeters,
    })

    targets.push({
      label: `鸟群 ${index + 1}`,
      detail: `累计爬升 ${climbMeters} m`,
      root: flock,
      hitMesh,
      preferredScale: 1.18,
    })
  }

  birdFlocksRef.value = wrappers
  interactionTargetsRef.value = [...interactionTargetsRef.value, ...targets]
}

function buildPathMeshes(scene: THREE.Scene, worldPaths: WorldPathData[]) {
  let longestPath: WorldPathData | null = null

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

    if (!longestPath || path.points.length > longestPath.points.length) {
      longestPath = path
    }
  }

  if (!longestPath || longestPath.vectors.length < 2) return null

  pathCurveRef.value = new THREE.CatmullRomCurve3(longestPath.vectors)
  createTraveler()
  if (travelerRef.value) {
    scene.add(travelerRef.value)
  }

  return longestPath
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

function updateBirdFlocks(elapsed: number) {
  for (const flock of birdFlocksRef.value) {
    flock.angle += 0.0035 * flock.speed
    flock.group.position.x = Math.cos(flock.angle) * flock.radius
    flock.group.position.z = Math.sin(flock.angle) * flock.radius * 0.68
    flock.group.position.y = flock.baseHeight + Math.sin(elapsed * 1.6 + flock.angle * 2) * 0.55
    flock.group.rotation.y = -flock.angle + Math.PI / 2

    flock.group.children.forEach((child, index) => {
      if (!(child instanceof THREE.Group)) return
      const wingPhase = elapsed * 10 + index
      child.rotation.z = Math.sin(wingPhase) * 0.12
      child.rotation.x = Math.cos(wingPhase) * 0.06
    })
  }
}

function updatePrecipitation(delta: number, weatherScene: TrackWeatherScene) {
  const precipitation = precipitationRef.value
  if (!precipitation) return

  const positions = precipitation.points.geometry.getAttribute('position')
  if (!(positions instanceof THREE.BufferAttribute)) return

  for (let index = 0; index < positions.count; index += 1) {
    const speed = precipitation.velocities[index] ?? 0
    const drift = weatherScene === 'rain' ? 0.6 : 0.1
    positions.setY(index, positions.getY(index) - speed * delta)
    positions.setX(index, positions.getX(index) + drift * delta)

    if (positions.getY(index) < precipitation.lowerBound) {
      positions.setY(index, precipitation.upperBound)
    }
  }

  positions.needsUpdate = true
}

function setOverviewView() {
  const camera = cameraRef.value
  const controls = controlsRef.value
  if (!camera || !controls) return
  camera.position.set(0, 56, 0.1)
  controls.target.set(0, 4, 0)
  controls.update()
}

function updateTooltipPlacement() {
  const surface = surfaceRef.value
  if (!surface || !tooltip.value.visible) return

  const margin = 18
  const maxLeft = surface.clientWidth - 160
  const maxTop = surface.clientHeight - 72
  tooltip.value.left = Math.min(Math.max(pointerLocalRef.value.x, margin), Math.max(maxLeft, margin))
  tooltip.value.top = Math.min(Math.max(pointerLocalRef.value.y - 18, margin), Math.max(maxTop, margin))
}

function showTooltipForTarget(target: MarkerDatum) {
  tooltip.value.visible = true
  tooltip.value.title = target.label
  tooltip.value.detail = target.detail
  updateTooltipPlacement()
}

function hideTooltip() {
  tooltip.value.visible = false
}

function setActiveTarget(target: MarkerDatum | null) {
  activeTargetRef.value = target
  if (target) {
    showTooltipForTarget(target)
  } else {
    hideTooltip()
  }
}

function resolveHoveredTarget() {
  const camera = cameraRef.value
  if (!camera || !interactionTargetsRef.value.length) return null
  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(pointerVectorRef.value, camera)
  const intersects = raycaster.intersectObjects(interactionTargetsRef.value.map((target) => target.hitMesh), false)
  if (!intersects.length) return null
  const hitObject = intersects[0]?.object
  return interactionTargetsRef.value.find((target) => target.hitMesh === hitObject) ?? null
}

function refreshInteractionState() {
  const desktopHover = prefersHover?.matches ?? true
  if (desktopHover) {
    setActiveTarget(resolveHoveredTarget())
  }

  for (const target of interactionTargetsRef.value) {
    const desiredScale = target === activeTargetRef.value ? target.preferredScale : 1
    target.root.scale.lerp(new THREE.Vector3(desiredScale, desiredScale, desiredScale), 0.16)
  }
}

function animate() {
  const renderer = rendererRef.value
  const scene = sceneRef.value
  const camera = cameraRef.value
  const controls = controlsRef.value
  const clock = clockRef.value
  if (!renderer || !scene || !camera || !controls || !clock) return

  const delta = clock.getDelta()
  const elapsed = clock.elapsedTime
  updateClouds(delta)
  updatePrecipitation(delta, props.weatherScene)
  updateBirdFlocks(elapsed)
  updateTraveler()
  refreshInteractionState()
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

function updatePointerPosition(event: PointerEvent) {
  const surface = surfaceRef.value
  if (!surface) return

  const rect = surface.getBoundingClientRect()
  const localX = event.clientX - rect.left
  const localY = event.clientY - rect.top
  pointerLocalRef.value = { x: localX, y: localY }
  pointerVectorRef.value = new THREE.Vector2(
    (localX / rect.width) * 2 - 1,
    -(localY / rect.height) * 2 + 1,
  )
  if (tooltip.value.visible) {
    updateTooltipPlacement()
  }
}

function handlePointerMove(event: PointerEvent) {
  updatePointerPosition(event)
}

function handlePointerLeave() {
  pointerVectorRef.value = new THREE.Vector2(-100, -100)
  if (prefersHover?.matches ?? true) {
    setActiveTarget(null)
  }
}

function handlePointerDown(event: PointerEvent) {
  updatePointerPosition(event)
  const target = resolveHoveredTarget()
  if (target) {
    setActiveTarget(target)
  } else if (!(prefersHover?.matches ?? true) || event.pointerType === 'touch') {
    setActiveTarget(null)
  }
}

async function initScene() {
  if (!props.data || !containerRef.value) return

  disposeScene()
  await nextTick()

  const container = containerRef.value
  if (!container) return

  const preset = buildWeatherPreset(props.weatherScene)
  const scene = new THREE.Scene()
  scene.background = new THREE.Color(preset.background)
  scene.fog = new THREE.FogExp2(preset.fogColor, preset.fogDensity)

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

  scene.add(new THREE.AmbientLight(preset.ambientColor, preset.ambientIntensity))

  const directionalLight = new THREE.DirectionalLight(preset.directionalColor, preset.directionalIntensity)
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
  interactionTargetsRef.value = []

  const { mapSize, worldPaths, referencePoints } = toLocalWorld(props.data.paths)
  const terrainReferences = referencePoints.filter((_, index) => index % Math.max(Math.ceil(referencePoints.length / 800), 1) === 0)
  buildEnvironment(scene, mapSize, preset)
  generateTerrain(scene, terrainReferences, mapSize, props.weatherScene)
  generateDecorations(scene, terrainReferences, mapSize)

  const mainPath = buildPathMeshes(scene, worldPaths)
  if (mainPath) {
    buildMilestoneMarkers(scene, mainPath)
  }
  createBirdFlocks(scene, mapSize, props.data.elevationGainMeters ?? 0, props.weatherScene)

  controls.maxDistance = mapSize * 2.2
  controls.target.set(0, 4, 0)
  controls.update()

  sceneRef.value = scene
  cameraRef.value = camera
  rendererRef.value = renderer
  controlsRef.value = controls
  clockRef.value = new THREE.Clock()

  resizeObserverRef.value = new ResizeObserver(() => handleResize())
  resizeObserverRef.value.observe(container)
  handleResize()
  animate()
}

watch(
  () => [props.data, props.mode, props.weatherScene] as const,
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

    <div
      ref="surface"
      class="track-viewer-surface"
      :class="containerClass"
      @pointermove="handlePointerMove"
      @pointerleave="handlePointerLeave"
      @pointerdown="handlePointerDown"
    >
      <div ref="container" class="viewer-canvas-host"></div>

      <div class="status-pill">
        {{ statusText }}
      </div>

      <div
        v-if="tooltip.visible"
        class="viewer-tooltip"
        :style="{ left: `${tooltip.left}px`, top: `${tooltip.top}px` }"
      >
        <p class="viewer-tooltip-title">{{ tooltip.title }}</p>
        <p class="viewer-tooltip-detail">{{ tooltip.detail }}</p>
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

.viewer-tooltip {
  position: absolute;
  z-index: 12;
  min-width: 9rem;
  transform: translate(-50%, -110%);
  border-radius: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: rgba(255, 255, 255, 0.92);
  padding: 0.75rem 0.9rem;
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.14);
  backdrop-filter: blur(10px);
  pointer-events: none;
}

.viewer-tooltip-title {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--text-primary);
}

.viewer-tooltip-detail {
  margin-top: 0.16rem;
  font-size: 0.76rem;
  color: var(--text-secondary);
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

  .viewer-tooltip {
    min-width: 8rem;
    padding: 0.68rem 0.82rem;
  }

  .scroll-button {
    top: auto;
    right: 1rem;
    bottom: 4.8rem;
    transform: none;
  }
}
</style>
