<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import BaseIcon from '../components/common/BaseIcon.vue'
import ChatBubble from '../components/chat/ChatBubble.vue'
import ChatInput from '../components/chat/ChatInput.vue'
import { mockSuggestedQuestions } from '../mock/mockData'

const router = useRouter()
const chatStore = useChatStore()
const messagesContainer = ref<HTMLElement | null>(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function handleSend(text: string) {
  chatStore.sendMessage(text)
}

function handleSuggestion(text: string) {
  chatStore.sendMessage(text)
}

// Auto scroll on new messages
watch(
  () => chatStore.messages.length,
  () => scrollToBottom(),
)

// Also scroll during typing effect
watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  () => scrollToBottom(),
)
</script>

<template>
  <main class="flex flex-col" style="height: calc(100vh - 56px);">
    <!-- Chat Header -->
    <div class="px-4 py-3 border-b flex items-center justify-between shrink-0" style="border-color: var(--border-default); background-color: var(--bg-card);">
      <button
        @click="router.back()"
        class="flex items-center gap-1 text-sm font-medium transition-colors hover:text-primary-500"
        style="color: var(--text-secondary);"
      >
        <BaseIcon name="ChevronLeft" :size="20" />
        返回
      </button>
      <div class="flex items-center gap-2">
        <div class="w-7 h-7 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center text-white">
          <BaseIcon name="Sparkles" :size="14" />
        </div>
        <span class="text-sm font-semibold" style="color: var(--text-primary);">AI 助手</span>
      </div>
      <button
        v-if="chatStore.messages.length > 0"
        @click="chatStore.clearMessages()"
        class="text-xs px-2 py-1 rounded-md transition-colors hover:bg-red-500/10 hover:text-red-500"
        style="color: var(--text-tertiary);"
      >
        清空
      </button>
      <div v-else class="w-10" />
    </div>

    <!-- Messages Area -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto px-4 py-6 space-y-4">
      <!-- Empty State -->
      <div v-if="chatStore.messages.length === 0" class="flex flex-col items-center justify-center h-full gap-6">
        <!-- Welcome -->
        <div class="text-center space-y-2">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center text-white mx-auto shadow-lg">
            <BaseIcon name="Sparkles" :size="28" />
          </div>
          <h2 class="text-lg font-bold" style="color: var(--text-primary);">你好！我是 TrailQuest AI</h2>
          <p class="text-sm max-w-xs" style="color: var(--text-secondary);">
            我可以帮你推荐路线、提供装备建议、分析天气状况。试试下面的问题吧！
          </p>
        </div>

        <!-- Suggested Questions -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 w-full max-w-md">
          <button
            v-for="q in mockSuggestedQuestions"
            :key="q.text"
            @click="handleSuggestion(q.text)"
            class="flex items-center gap-3 px-4 py-3 rounded-xl text-left text-sm transition-all duration-200 hover:shadow-md hover:-translate-y-0.5 active:scale-[0.98]"
            style="background-color: var(--bg-card); border: 1px solid var(--border-card); color: var(--text-primary);"
          >
            <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0" style="background-color: var(--bg-tag);">
              <BaseIcon :name="q.icon" :size="16" class="text-primary-500" />
            </div>
            <span>{{ q.text }}</span>
          </button>
        </div>
      </div>

      <!-- Message List -->
      <template v-else>
        <ChatBubble
          v-for="msg in chatStore.messages"
          :key="msg.id"
          :message="msg"
        />

        <!-- Typing Indicator -->
        <div v-if="chatStore.isTyping && chatStore.messages[chatStore.messages.length - 1]?.role === 'user'" class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center text-white shrink-0">
            <BaseIcon name="Sparkles" :size="16" />
          </div>
          <div class="px-4 py-3 rounded-2xl rounded-bl-md" style="background-color: var(--bg-card); border: 1px solid var(--border-card);">
            <div class="flex gap-1">
              <span class="w-2 h-2 rounded-full bg-primary-400 animate-bounce" style="animation-delay: 0ms;" />
              <span class="w-2 h-2 rounded-full bg-primary-400 animate-bounce" style="animation-delay: 150ms;" />
              <span class="w-2 h-2 rounded-full bg-primary-400 animate-bounce" style="animation-delay: 300ms;" />
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Input Area -->
    <ChatInput :disabled="chatStore.isTyping" @send="handleSend" />
  </main>
</template>
