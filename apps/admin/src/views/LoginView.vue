<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KeyRound, ShieldCheck, Sparkles } from 'lucide-vue-next'

import { useAdminAuthStore } from '../stores/auth'
import { pinia } from '../stores/pinia'

const route = useRoute()
const router = useRouter()
const authStore = useAdminAuthStore(pinia)

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

const hintMessage = computed(() => {
  if (route.query.reason === 'no-permission') {
    return '当前账号没有后台权限，请使用管理员账号登录。'
  }
  return ''
})

async function handleSubmit() {
  if (!email.value.trim() || !password.value.trim()) {
    errorMessage.value = '请输入邮箱和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const result = await authStore.login(email.value.trim(), password.value)
    if (!result.success) {
      errorMessage.value = result.message || '登录失败'
      return
    }
    await router.replace({ name: 'dashboard' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page admin-page">
    <div class="admin-login-page__glow" />
    <main class="admin-login-card admin-card">
      <section class="admin-login-card__hero">
        <div class="admin-login-card__eyebrow">TrailQuest Admin</div>
        <h1>内容治理后台</h1>
        <p>
          路线审核、评论管理、举报处理的最小工作台。
          绿色系、低噪音、可快速扩展。
        </p>

        <div class="admin-login-card__feature-list">
          <span><ShieldCheck :size="16" :stroke-width="2" /> 管理员权限校验</span>
          <span><Sparkles :size="16" :stroke-width="2" /> TrailQuest 主题化</span>
          <span><KeyRound :size="16" :stroke-width="2" /> 复用现有登录体系</span>
        </div>
      </section>

      <section class="admin-login-card__form-wrap">
        <div class="admin-login-card__form-header">
          <h2>管理员登录</h2>
          <p>使用现有 /api/auth/login 与 /api/auth/me</p>
        </div>

        <div v-if="hintMessage" class="admin-login-card__notice">{{ hintMessage }}</div>
        <div v-if="errorMessage" class="admin-login-card__error">{{ errorMessage }}</div>

        <form class="admin-login-form" @submit.prevent="handleSubmit">
          <label>
            <span>邮箱</span>
            <input v-model="email" class="admin-input" type="email" placeholder="admin@example.com" autocomplete="email" />
          </label>

          <label>
            <span>密码</span>
            <input v-model="password" class="admin-input" type="password" placeholder="请输入密码" autocomplete="current-password" />
          </label>

          <button class="admin-button admin-button-primary" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '进入后台' }}
          </button>
        </form>
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin-login-page {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 1.5rem;
  overflow: hidden;
}

.admin-login-page__glow {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 18%, rgba(47, 94, 37, 0.18), transparent 30%),
    radial-gradient(circle at 82% 20%, rgba(106, 165, 90, 0.16), transparent 26%),
    radial-gradient(circle at 70% 80%, rgba(47, 94, 37, 0.12), transparent 34%);
  pointer-events: none;
}

.admin-login-card {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  width: min(1080px, 100%);
  overflow: hidden;
}

.admin-login-card__hero {
  padding: 2.5rem;
  background: linear-gradient(135deg, rgba(47, 94, 37, 0.12), rgba(255, 255, 255, 0.24));
  border-right: 1px solid var(--border);
}

.admin-login-card__eyebrow {
  margin-bottom: 0.8rem;
  color: var(--primary);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.admin-login-card__hero h1 {
  margin: 0;
  color: var(--text-strong);
  font-size: clamp(2rem, 5vw, 3.2rem);
  line-height: 1.05;
}

.admin-login-card__hero p {
  margin: 1rem 0 0;
  color: var(--text-muted);
  max-width: 28rem;
  line-height: 1.8;
}

.admin-login-card__feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.admin-login-card__feature-list span {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.55rem 0.85rem;
  border-radius: 999px;
  color: var(--primary);
  background: var(--bg-soft);
}

.admin-login-card__form-wrap {
  padding: 2.2rem;
  background: var(--bg-surface);
}

.admin-login-card__form-header h2 {
  margin: 0;
  color: var(--text-strong);
}

.admin-login-card__form-header p {
  margin: 0.35rem 0 0;
  color: var(--text-muted);
}

.admin-login-card__notice,
.admin-login-card__error {
  margin-top: 1rem;
  border-radius: 16px;
  padding: 0.85rem 1rem;
  line-height: 1.65;
}

.admin-login-card__notice {
  color: var(--primary-strong);
  background: rgba(47, 94, 37, 0.08);
  border: 1px solid rgba(47, 94, 37, 0.16);
}

.admin-login-card__error {
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.18);
}

.admin-login-form {
  display: grid;
  gap: 1rem;
  margin-top: 1.2rem;
}

.admin-login-form label {
  display: grid;
  gap: 0.55rem;
}

.admin-login-form label span {
  color: var(--text-muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.admin-login-form button {
  margin-top: 0.4rem;
  padding-block: 0.95rem;
}

@media (max-width: 900px) {
  .admin-login-card {
    grid-template-columns: 1fr;
  }

  .admin-login-card__hero {
    border-right: 0;
    border-bottom: 1px solid var(--border);
  }
}
</style>
