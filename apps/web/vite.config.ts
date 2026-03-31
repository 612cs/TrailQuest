import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    tailwindcss(),
    vue(),
  ],
  server: {
    fs: {
      allow: [
        fileURLToPath(new URL('.', import.meta.url)),
        fileURLToPath(new URL('../../packages/ui', import.meta.url)),
      ],
    },
  },
  resolve: {
    alias: {
      '@trailquest/ui': fileURLToPath(new URL('../../packages/ui/src', import.meta.url)),
      vue: fileURLToPath(new URL('./node_modules/vue/dist/vue.runtime.esm-bundler.js', import.meta.url)),
      'lucide-vue-next': fileURLToPath(new URL('./node_modules/lucide-vue-next', import.meta.url)),
    },
  },
})
