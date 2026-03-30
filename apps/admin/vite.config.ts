import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@trailquest/ui': fileURLToPath(new URL('../../packages/ui/src', import.meta.url)),
      vue: fileURLToPath(new URL('./node_modules/vue/dist/vue.runtime.esm-bundler.js', import.meta.url)),
      'lucide-vue-next': fileURLToPath(new URL('./node_modules/lucide-vue-next', import.meta.url)),
    },
  },
})
