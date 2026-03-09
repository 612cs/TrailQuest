import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '../mock/mockData'
import { generateMessageId, getAIReply } from '../mock/mockData'

export const useChatStore = defineStore('chat', () => {
    const messages = ref<ChatMessage[]>([])
    const isTyping = ref(false)

    function addMessage(role: 'user' | 'assistant', content: string): ChatMessage {
        const msg: ChatMessage = {
            id: generateMessageId(),
            role,
            content,
            timestamp: Date.now(),
        }
        messages.value.push(msg)
        return msg
    }

    async function sendMessage(text: string) {
        if (!text.trim() || isTyping.value) return

        // Add user message
        addMessage('user', text.trim())

        // Simulate AI thinking
        isTyping.value = true
        await new Promise((r) => setTimeout(r, 800 + Math.random() * 600))

        // Get and add AI reply with typing effect
        const reply = getAIReply(text.trim())
        const aiMsg = addMessage('assistant', '')

        // Simulate typing effect
        for (let i = 0; i < reply.length; i++) {
            aiMsg.content = reply.slice(0, i + 1)
            await new Promise((r) => setTimeout(r, 15 + Math.random() * 20))
        }

        isTyping.value = false
    }

    function clearMessages() {
        messages.value = []
    }

    return { messages, isTyping, sendMessage, clearMessages }
})
