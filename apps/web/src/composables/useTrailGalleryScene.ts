import { onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import type { Ref } from 'vue'
import * as THREE from 'three'

interface UseTrailGallerySceneOptions {
  containerRef: Ref<HTMLDivElement | null>
  focusProgress: Ref<number>
}

interface RibbonState {
  line: THREE.Line
  amplitude: number
  speed: number
  phase: number
  basePoints: Array<{ x: number; y: number; z: number }>
}

export function useTrailGalleryScene(options: UseTrailGallerySceneOptions) {
  const rendererRef = shallowRef<THREE.WebGLRenderer | null>(null)
  const sceneRef = shallowRef<THREE.Scene | null>(null)
  const cameraRef = shallowRef<THREE.PerspectiveCamera | null>(null)
  const resizeObserverRef = shallowRef<ResizeObserver | null>(null)
  const animationFrameRef = shallowRef<number | null>(null)
  const beaconRef = shallowRef<THREE.Group | null>(null)
  const particlesRef = shallowRef<THREE.Points | null>(null)
  const ribbonsRef = shallowRef<RibbonState[]>([])
  const terrainRef = shallowRef<THREE.LineSegments | null>(null)
  const clockRef = shallowRef<THREE.Clock | null>(null)

  onMounted(() => {
    watch(
      () => options.containerRef.value,
      (element) => {
        if (!element) {
          disposeScene()
          return
        }

        initScene(element)
      },
      { immediate: true },
    )
  })

  onBeforeUnmount(() => {
    disposeScene()
  })

  function initScene(container: HTMLDivElement) {
    disposeScene()

    const scene = new THREE.Scene()
    scene.fog = new THREE.FogExp2('#031012', 0.06)

    const camera = new THREE.PerspectiveCamera(42, 1, 0.1, 100)
    camera.position.set(0, 0.8, 15)

    const renderer = new THREE.WebGLRenderer({
      antialias: true,
      alpha: true,
      powerPreference: 'high-performance',
    })
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.8))
    renderer.setClearColor('#000000', 0)
    container.appendChild(renderer.domElement)

    const ambientLight = new THREE.AmbientLight('#d9f9ec', 1.1)
    const directionalLight = new THREE.DirectionalLight('#b4ffd5', 1.2)
    directionalLight.position.set(4, 6, 8)
    const rimLight = new THREE.PointLight('#e1fff2', 1.4, 30, 2)
    rimLight.position.set(-6, 2, 6)
    scene.add(ambientLight, directionalLight, rimLight)

    const terrain = createTerrain()
    const particles = createParticles()
    const ribbons = createRibbons()
    const beacon = createBeacon()

    scene.add(terrain)
    scene.add(particles)
    scene.add(beacon)
    ribbons.forEach((ribbon) => scene.add(ribbon.line))

    sceneRef.value = scene
    cameraRef.value = camera
    rendererRef.value = renderer
    terrainRef.value = terrain
    particlesRef.value = particles
    ribbonsRef.value = ribbons
    beaconRef.value = beacon
    clockRef.value = new THREE.Clock()

    resizeObserverRef.value = new ResizeObserver(() => {
      resizeRenderer()
    })
    resizeObserverRef.value.observe(container)
    resizeRenderer()
    animate()
  }

  function createTerrain() {
    const planeGeometry = new THREE.PlaneGeometry(24, 12, 28, 14)
    const position = planeGeometry.getAttribute('position') as THREE.BufferAttribute

    for (let index = 0; index < position.count; index += 1) {
      const x = position.getX(index)
      const y = position.getY(index)
      const z = Math.sin(x * 0.9) * 0.24 + Math.cos(y * 1.1) * 0.18
      position.setZ(index, z)
    }

    planeGeometry.computeVertexNormals()
    const material = new THREE.LineBasicMaterial({
      color: '#1e6a57',
      transparent: true,
      opacity: 0.26,
    })
    const edges = new THREE.EdgesGeometry(planeGeometry, 10)
    planeGeometry.dispose()
    const terrain = new THREE.LineSegments(edges, material)
    terrain.rotation.x = -0.96
    terrain.position.set(1.2, -2.4, -6)
    return terrain
  }

  function createParticles() {
    const count = 280
    const positions = new Float32Array(count * 3)
    const colors = new Float32Array(count * 3)
    const color = new THREE.Color()

    for (let index = 0; index < count; index += 1) {
      const offset = index * 3
      positions[offset] = THREE.MathUtils.randFloatSpread(26)
      positions[offset + 1] = THREE.MathUtils.randFloatSpread(12)
      positions[offset + 2] = THREE.MathUtils.randFloat(-11, 5)

      color.set(index % 3 === 0 ? '#80f8c8' : index % 3 === 1 ? '#d5fff2' : '#3d9a84')
      colors[offset] = color.r
      colors[offset + 1] = color.g
      colors[offset + 2] = color.b
    }

    const geometry = new THREE.BufferGeometry()
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

    const material = new THREE.PointsMaterial({
      size: 0.08,
      transparent: true,
      opacity: 0.95,
      depthWrite: false,
      vertexColors: true,
    })

    return new THREE.Points(geometry, material)
  }

  function createRibbons() {
    return Array.from({ length: 3 }, (_, index) => {
      const points = Array.from({ length: 40 }, (_point, pointIndex) => {
        const progress = pointIndex / 39
        const x = -11 + progress * 22
        const y = Math.sin(progress * Math.PI * (1.4 + index * 0.2)) * (1.1 + index * 0.45)
        const z = -4.5 - index * 1.8 + Math.cos(progress * Math.PI * 2) * 0.6
        return new THREE.Vector3(x, y, z)
      })

      const geometry = new THREE.BufferGeometry().setFromPoints(points)
      const material = new THREE.LineBasicMaterial({
        color: index === 0 ? '#9effdf' : index === 1 ? '#4ed0a5' : '#1f6a59',
        transparent: true,
        opacity: index === 0 ? 0.5 : 0.22,
      })

      return {
        line: new THREE.Line(geometry, material),
        amplitude: 0.18 + index * 0.05,
        speed: 0.4 + index * 0.18,
        phase: index * Math.PI * 0.35,
        basePoints: points.map((point) => ({ x: point.x, y: point.y, z: point.z })),
      }
    })
  }

  function createBeacon() {
    const group = new THREE.Group()
    const core = new THREE.Mesh(
      new THREE.SphereGeometry(0.16, 24, 24),
      new THREE.MeshBasicMaterial({ color: '#d8fff2', transparent: true, opacity: 0.95 }),
    )
    const ring = new THREE.Mesh(
      new THREE.TorusGeometry(0.52, 0.04, 16, 80),
      new THREE.MeshBasicMaterial({ color: '#5ef4bc', transparent: true, opacity: 0.88 }),
    )
    ring.rotation.x = Math.PI / 2
    const halo = new THREE.Mesh(
      new THREE.PlaneGeometry(1.8, 1.8),
      new THREE.MeshBasicMaterial({
        color: '#52f1b8',
        transparent: true,
        opacity: 0.12,
        side: THREE.DoubleSide,
      }),
    )

    group.add(core, ring, halo)
    group.position.set(-7, -0.8, -2.3)
    return group
  }

  function resizeRenderer() {
    const container = options.containerRef.value
    const renderer = rendererRef.value
    const camera = cameraRef.value
    if (!container || !renderer || !camera) return

    const width = Math.max(container.clientWidth, 1)
    const height = Math.max(container.clientHeight, 1)
    renderer.setSize(width, height)
    camera.aspect = width / height
    camera.position.z = width < 900 ? 17 : 15
    camera.updateProjectionMatrix()
  }

  function animate() {
    const renderer = rendererRef.value
    const scene = sceneRef.value
    const camera = cameraRef.value
    const clock = clockRef.value
    const particles = particlesRef.value
    const terrain = terrainRef.value
    const beacon = beaconRef.value

    if (!renderer || !scene || !camera || !clock) return

    const tick = () => {
      animationFrameRef.value = window.requestAnimationFrame(tick)
      const elapsed = clock.getElapsedTime()
      const progress = options.focusProgress.value

      if (particles) {
        particles.rotation.y = elapsed * 0.03
        particles.rotation.x = Math.sin(elapsed * 0.08) * 0.02
      }

      if (terrain) {
        terrain.position.x = Math.sin(elapsed * 0.14) * 0.6
        const material = terrain.material as THREE.LineBasicMaterial
        material.opacity = 0.22 + Math.sin(elapsed * 0.3) * 0.03
      }

      ribbonsRef.value.forEach((ribbon, index) => {
        const geometry = ribbon.line.geometry
        const positions = geometry.getAttribute('position') as THREE.BufferAttribute
        for (let pointIndex = 0; pointIndex < positions.count; pointIndex += 1) {
          const basePoint = ribbon.basePoints[pointIndex]
          if (!basePoint) continue

          const y = Math.sin(pointIndex * 0.28 + elapsed * ribbon.speed + ribbon.phase) * ribbon.amplitude
          positions.setY(pointIndex, basePoint.y + y)
          positions.setX(pointIndex, basePoint.x + Math.cos(elapsed * 0.2 + index) * 0.1)
          positions.setZ(pointIndex, basePoint.z + Math.sin(elapsed * 0.3 + pointIndex * 0.18) * 0.04)
        }
        positions.needsUpdate = true
      })

      if (beacon) {
        beacon.position.x = THREE.MathUtils.lerp(-7.4, 7.2, progress)
        beacon.position.y = -0.8 + Math.sin(elapsed * 1.4 + progress * Math.PI * 2) * 0.24
        beacon.rotation.z += 0.008
        beacon.children[1]!.scale.setScalar(1 + Math.sin(elapsed * 2.4) * 0.08)
        beacon.children[2]!.lookAt(camera.position)
      }

      camera.position.x += ((progress - 0.5) * 2.4 - camera.position.x) * 0.02
      camera.position.y = 0.8 + Math.sin(elapsed * 0.2) * 0.08
      camera.lookAt(camera.position.x * 0.2, -0.4, -3)
      renderer.render(scene, camera)
    }

    tick()
  }

  function disposeScene() {
    if (animationFrameRef.value != null) {
      window.cancelAnimationFrame(animationFrameRef.value)
      animationFrameRef.value = null
    }

    resizeObserverRef.value?.disconnect()
    resizeObserverRef.value = null

    const renderer = rendererRef.value
    if (renderer?.domElement.parentElement) {
      renderer.domElement.parentElement.removeChild(renderer.domElement)
    }
    renderer?.dispose()

    sceneRef.value?.traverse((object) => {
      const geometry = (object as THREE.Mesh).geometry
      const material = (object as THREE.Mesh).material

      geometry?.dispose?.()
      if (Array.isArray(material)) {
        material.forEach((item) => item.dispose())
      } else {
        material?.dispose?.()
      }
    })

    rendererRef.value = null
    sceneRef.value = null
    cameraRef.value = null
    particlesRef.value = null
    ribbonsRef.value = []
    terrainRef.value = null
    beaconRef.value = null
    clockRef.value = null
  }
}
