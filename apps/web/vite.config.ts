import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

function getApiPort(): number {
  try {
    const port = readFileSync(resolve(__dirname, '../../.api-port'), 'utf-8').trim()
    return Number(port) || 8080
  } catch {
    return 8080
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [tailwindcss(), vue()],
  server: {
    fs: {
      allow: [
        fileURLToPath(new URL('.', import.meta.url)),
        fileURLToPath(new URL('../../packages/ui', import.meta.url)),
      ],
    },
    proxy: {
      '/api': {
        target: `http://localhost:${getApiPort()}`,
        changeOrigin: true,
      },
      '/ws': {
        target: `ws://localhost:${getApiPort()}`,
        ws: true,
      },
    },
  },
  resolve: {
    alias: {
      '@trailquest/ui': fileURLToPath(new URL('../../packages/ui/src', import.meta.url)),
      vue: fileURLToPath(
        new URL('./node_modules/vue/dist/vue.runtime.esm-bundler.js', import.meta.url),
      ),
      'lucide-vue-next': fileURLToPath(new URL('./node_modules/lucide-vue-next', import.meta.url)),
    },
  },
})
