import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import { pinia } from './stores/pinia'
import { useThemeStore } from './stores/theme'
import './style.css'

const app = createApp(App)

app.use(pinia)
app.use(router)

const themeStore = useThemeStore(pinia)
themeStore.init()

app.mount('#app')
