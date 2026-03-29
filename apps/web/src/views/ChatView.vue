<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import BaseIcon from '../components/common/BaseIcon.vue'
import ChatBubble from '../components/chat/ChatBubble.vue'
import ChatInput from '../components/chat/ChatInput.vue'
import ChatSidebar from '../components/chat/ChatSidebar.vue'
import { useChatStore } from '../stores/chat'
import { useFlashStore } from '../stores/useFlashStore'
import { useUserStore } from '../stores/useUserStore'
import { ApiError } from '../types/api'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const flashStore = useFlashStore()

const messagesContainer = ref<HTMLElement | null>(null)
const mobileSidebarOpen = ref(false)

const suggestions = [
  { icon: 'Compass', text: '推荐杭州周边适合周末单日徒步的路线' },
  { icon: 'Trees', text: '想找一条适合新手、风景好的森林路线' },
  { icon: 'Camera', text: '有没有适合拍日出的轻装路线' },
  { icon: 'Backpack', text: '春季徒步一般要准备哪些装备' },
]

const welcomeDescription = computed(() => (
  userStore.isLoggedIn
    ? '告诉我地点、难度、时长或你想看的风景，我会结合 TrailQuest 内部路线库给你建议。'
    : '登录后可以保存会话、继续追问，并获得更贴近你徒步画像的路线建议。'
))

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

async function ensureBootstrapped() {
  if (!userStore.isLoggedIn) {
    chatStore.reset()
    return
  }
  try {
    await chatStore.bootstrap()
  } catch (error) {
    const message = error instanceof Error ? error.message : 'AI 会话加载失败'
    flashStore.showError(message)
  }
}

async function handleSend(text: string) {
  if (!userStore.isLoggedIn) {
    userStore.showAuthModal = true
    return
  }
  try {
    await chatStore.sendMessage(text, {
      currentCity: userStore.profile?.location || null,
    })
    scrollToBottom()
  } catch (error) {
    if (error instanceof ApiError) {
      flashStore.showError(error.message)
      return
    }
    flashStore.showError(error instanceof Error ? error.message : 'AI 对话发送失败')
  }
}

function handleSuggestion(text: string) {
  handleSend(text)
}

function handleFollowUp(text: string) {
  handleSend(text)
}

async function handleCreateConversation() {
  if (!userStore.isLoggedIn) {
    userStore.showAuthModal = true
    return
  }
  try {
    await chatStore.createConversation()
    mobileSidebarOpen.value = false
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '新建会话失败')
  }
}

async function handleSelectConversation(conversationId: string | number) {
  try {
    await chatStore.selectConversation(conversationId)
    mobileSidebarOpen.value = false
    scrollToBottom()
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '读取会话失败')
  }
}

async function handleDeleteConversation(conversationId: string | number) {
  try {
    await chatStore.removeConversation(conversationId)
    mobileSidebarOpen.value = false
    flashStore.showSuccess('会话已删除', 1800)
  } catch (error) {
    flashStore.showError(error instanceof Error ? error.message : '删除会话失败')
  }
}

watch(
  () => userStore.isLoggedIn,
  () => {
    ensureBootstrapped()
  },
  { immediate: true },
)

watch(
  () => chatStore.messages.length,
  () => scrollToBottom(),
)

watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  () => scrollToBottom(),
)

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <main class="min-h-[100vh] px-4 py-4 sm:px-6 lg:px-8">
    <div class="mx-auto flex h-[96vh] max-w-7xl gap-4 lg:gap-5">
      <div class="hidden w-[320px] shrink-0 lg:block">
        <ChatSidebar
          :conversations="chatStore.conversations"
          :active-conversation-id="chatStore.activeConversationId"
          :loading="chatStore.isBootstrapping"
          @create="handleCreateConversation"
          @select="handleSelectConversation"
          @delete="handleDeleteConversation"
        />
      </div>

      <section class="flex min-w-0 flex-1 flex-col overflow-hidden rounded-[32px] border" style="border-color: var(--border-card); background-color: var(--bg-card);">
        <header class="flex items-center justify-between border-b px-4 py-4 sm:px-6" style="border-color: var(--border-default);">
          <div class="flex items-center gap-3">
            <button
              @click="router.back()"
              class="flex h-10 w-10 items-center justify-center rounded-2xl border transition-colors hover:bg-primary-500/5"
              style="border-color: var(--border-default); color: var(--text-primary);"
            >
              <BaseIcon name="ChevronLeft" :size="20" />
            </button>
            <button
              class="flex h-10 w-10 items-center justify-center rounded-2xl border lg:hidden"
              style="border-color: var(--border-default); color: var(--text-primary);"
              @click="mobileSidebarOpen = true"
            >
              <BaseIcon name="PanelLeft" :size="18" />
            </button>
            <div>
              <p class="text-xs uppercase tracking-[0.24em] text-primary-500">TrailQuest AI</p>
              <h1 class="mt-1 text-lg font-semibold sm:text-xl" style="color: var(--text-primary);">路线顾问</h1>
            </div>
          </div>

          <div class="hidden items-center gap-2 rounded-full border px-4 py-2 text-xs sm:flex" style="border-color: var(--border-default); color: var(--text-secondary);">
            <BaseIcon name="ShieldCheck" :size="14" class="text-primary-500" />
            仅基于内部路线库生成建议
          </div>
        </header>

        <div ref="messagesContainer" class="flex-1 overflow-y-auto px-4 py-5 sm:px-6">
          <div v-if="chatStore.streamError" class="mb-4 rounded-2xl border px-4 py-3 text-sm" style="border-color: rgba(239, 68, 68, 0.25); background-color: rgba(239, 68, 68, 0.06); color: var(--text-secondary);">
            {{ chatStore.streamError }}
          </div>

          <div v-if="!userStore.isLoggedIn" class="flex h-full flex-col items-center justify-center px-4 text-center">
            <div class="flex h-16 w-16 items-center justify-center rounded-[24px] border" style="border-color: var(--border-default); background-color: color-mix(in srgb, var(--color-primary-500) 8%, var(--bg-card)); color: var(--color-primary-500);">
              <BaseIcon name="Leaf" :size="28" />
            </div>
            <h2 class="mt-5 text-xl font-semibold" style="color: var(--text-primary);">登录后开启 TrailQuest AI</h2>
            <p class="mt-3 max-w-md text-sm leading-7" style="color: var(--text-secondary);">
              {{ welcomeDescription }}
            </p>
            <button
              class="mt-6 rounded-full border px-5 py-2.5 text-sm font-medium transition-colors hover:bg-primary-500/5"
              style="border-color: var(--border-default); color: var(--text-primary);"
              @click="userStore.showAuthModal = true"
            >
              立即登录
            </button>
          </div>

          <div v-else-if="chatStore.messages.length === 0" class="flex h-full flex-col items-center justify-center px-4 text-center">
            <div class="flex h-16 w-16 items-center justify-center rounded-[24px] border" style="border-color: var(--border-default); background-color: color-mix(in srgb, var(--color-primary-500) 8%, var(--bg-card)); color: var(--color-primary-500);">
              <BaseIcon name="Leaf" :size="28" />
            </div>
            <h2 class="mt-5 text-xl font-semibold" style="color: var(--text-primary);">把路线问题交给我</h2>
            <p class="mt-3 max-w-2xl text-sm leading-7" style="color: var(--text-secondary);">
              {{ welcomeDescription }}
            </p>
            <div class="mt-8 grid w-full max-w-3xl grid-cols-1 gap-3 md:grid-cols-2">
              <button
                v-for="item in suggestions"
                :key="item.text"
                class="rounded-[24px] border px-4 py-4 text-left transition-all duration-200 hover:-translate-y-0.5 hover:bg-primary-500/5"
                style="border-color: var(--border-card); background-color: var(--bg-page);"
                @click="handleSuggestion(item.text)"
              >
                <div class="flex items-start gap-3">
                  <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border" style="border-color: var(--border-default); color: var(--color-primary-500);">
                    <BaseIcon :name="item.icon" :size="18" />
                  </div>
                  <p class="text-sm leading-7" style="color: var(--text-primary);">{{ item.text }}</p>
                </div>
              </button>
            </div>
          </div>

          <div v-else class="space-y-5">
            <ChatBubble
              v-for="msg in chatStore.messages"
              :key="msg.id"
              :message="msg"
              :user-avatar-text="userStore.profile?.avatar || 'U'"
              :user-avatar-media-url="userStore.profile?.avatarMediaUrl || ''"
              :user-avatar-bg="userStore.profile?.avatarBg || 'var(--color-primary-500)'"
              @follow-up="handleFollowUp"
            />
          </div>
        </div>

        <ChatInput :disabled="!userStore.isLoggedIn || chatStore.isStreaming" @send="handleSend" />
      </section>
    </div>

    <transition name="fade">
      <div v-if="mobileSidebarOpen" class="fixed inset-0 z-40 bg-black/35 lg:hidden" @click="mobileSidebarOpen = false">
        <aside class="absolute left-0 top-0 h-full w-[86vw] max-w-[360px] p-3" @click.stop>
          <ChatSidebar
            :conversations="chatStore.conversations"
            :active-conversation-id="chatStore.activeConversationId"
            :loading="chatStore.isBootstrapping"
            @create="handleCreateConversation"
            @select="handleSelectConversation"
            @delete="handleDeleteConversation"
          />
        </aside>
      </div>
    </transition>
  </main>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
