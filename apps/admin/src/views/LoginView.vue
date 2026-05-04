<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KeyRound, ShieldCheck, Sparkles, Trees } from 'lucide-vue-next'

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
  <div class="login-view">
    <div class="login-background"></div>
    <div class="login-overlay"></div>

    <main class="login-bento-container">
      <div class="login-bento-card">
        <!-- Brand/Hero Section -->
        <section class="login-hero">
          <div class="hero-content">
            <div class="brand-eyebrow">TrailQuest Digital Ecosystem</div>
            <h1 class="hero-title">内容治理后台</h1>
            <p class="hero-desc">
              步道管理、数据洞察与用户服务的数字指挥仓。 体验新一代的极简管理界面设计。
            </p>

            <div class="feature-badges">
              <span class="badge"><ShieldCheck :size="14" /> 权限校验</span>
              <span class="badge"><Sparkles :size="14" /> 沉浸式主题</span>
              <span class="badge"><KeyRound :size="14" /> SSO 登录</span>
            </div>
          </div>
          <Trees :size="180" class="hero-icon-bg" />
        </section>

        <!-- Form Section -->
        <section class="login-form-section">
          <div class="form-header">
            <h2>管理员登录</h2>
            <p>基于身份验证进入工作界面</p>
          </div>

          <div v-if="hintMessage" class="notice-alert is-warning">{{ hintMessage }}</div>
          <div v-if="errorMessage" class="notice-alert is-error">{{ errorMessage }}</div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <div class="input-group">
              <label class="input-label">邮箱</label>
              <input
                v-model="email"
                class="styled-input"
                type="email"
                placeholder="admin@example.com"
                autocomplete="email"
              />
            </div>

            <div class="input-group">
              <label class="input-label">密码</label>
              <input
                v-model="password"
                class="styled-input"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
            </div>

            <button class="btn btn--primary login-submit-btn" type="submit" :disabled="loading">
              {{ loading ? '身份验证中...' : '进入控制台' }}
            </button>
          </form>

          <div class="form-footer">
            <p>未授权访问将被严格记录并审计。© 2026 TrailQuest</p>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-view {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 1.5rem;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 18%, rgba(var(--primary-rgb), 0.16), transparent 26%),
    radial-gradient(circle at 82% 20%, rgba(74, 144, 108, 0.16), transparent 24%),
    radial-gradient(circle at 50% 78%, rgba(20, 83, 45, 0.12), transparent 30%),
    linear-gradient(
      180deg,
      rgba(var(--bg-body-rgb), 0.96) 0%,
      rgba(var(--bg-body-rgb), 0.9) 48%,
      rgba(var(--bg-body-rgb), 0.96) 100%
    );
}

.login-background {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      circle at 20% 18%,
      rgba(var(--primary-rgb), 0.18),
      transparent 0 18%,
      transparent 19%
    ),
    radial-gradient(circle at 78% 24%, rgba(72, 122, 92, 0.18), transparent 0 16%, transparent 17%),
    radial-gradient(circle at 50% 52%, rgba(15, 23, 42, 0.06), transparent 0 20%, transparent 21%),
    linear-gradient(135deg, rgba(var(--bg-surface-rgb), 0.94), rgba(var(--bg-body-rgb), 0.9));
  background-size: cover;
  filter: saturate(0.95);
  z-index: 0;
}

.login-background::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(transparent 0 73%, rgba(32, 78, 50, 0.12) 73% 78%, transparent 78% 100%),
    linear-gradient(
      115deg,
      transparent 0 34%,
      rgba(255, 255, 255, 0.12) 34% 36%,
      transparent 36% 64%,
      rgba(255, 255, 255, 0.08) 64% 66%,
      transparent 66% 100%
    );
  opacity: 0.65;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(var(--bg-body-rgb), 0.22) 0%,
    rgba(var(--bg-body-rgb), 0.54) 48%,
    rgba(var(--bg-body-rgb), 0.86) 100%
  );
  z-index: 0;
}

/* Bento Card Geometry */
.login-bento-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1024px;
  perspective: 1000px;
}

.login-bento-card {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  background: rgba(var(--bg-surface-rgb), 0.86);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 32px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 24px 48px rgba(0, 0, 0, 0.12),
    inset 0 1px 1px rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

/* Left Hero */
.login-hero {
  position: relative;
  padding: 4rem;
  background: linear-gradient(145deg, rgba(236, 253, 245, 0.96), rgba(187, 247, 208, 0.92));
  color: #111827;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.brand-eyebrow {
  font-size: 0.8125rem;
  font-weight: 800;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: #1f2937;
  margin-bottom: 1.5rem;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1.1;
  margin: 0 0 1rem;
  letter-spacing: -0.02em;
}

.hero-desc {
  font-size: 1.0625rem;
  line-height: 1.7;
  color: #374151;
  margin-bottom: 3rem;
  max-width: 90%;
}

.feature-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.56);
  backdrop-filter: blur(8px);
  border-radius: 99px;
  font-size: 0.8125rem;
  font-weight: 600;
  border: 1px solid rgba(22, 101, 52, 0.16);
}

.hero-icon-bg {
  position: absolute;
  right: -40px;
  bottom: -40px;
  color: rgba(21, 128, 61, 0.14);
  transform: rotate(-15deg);
  z-index: 1;
}

/* Right Form Section */
.login-form-section {
  padding: 4rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(var(--bg-surface-rgb), 0.94);
}

.form-header {
  margin-bottom: 2.5rem;
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0 0 0.5rem;
}

.form-header p {
  font-size: 0.9375rem;
  color: var(--text-muted);
  margin: 0;
}

.notice-alert {
  padding: 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.notice-alert.is-warning {
  background: rgba(var(--primary-rgb), 0.1);
  color: var(--primary);
  border: 1px solid rgba(var(--primary-rgb), 0.2);
}

.notice-alert.is-error {
  background: rgba(181, 68, 68, 0.1);
  color: var(--danger);
  border: 1px solid rgba(181, 68, 68, 0.2);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-label {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--text-strong);
}

.styled-input {
  width: 100%;
  padding: 0.875rem 1.25rem;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-strong);
  font-size: 0.9375rem;
  transition: all 0.2s ease;
}

.styled-input:focus {
  outline: none;
  border-color: var(--primary);
  background: var(--bg-surface);
  box-shadow: 0 0 0 4px rgba(var(--primary-rgb), 0.1);
}

.login-submit-btn {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 800;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--primary);
  color: white;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(var(--primary-rgb), 0.2);
}

.login-submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(var(--primary-rgb), 0.3);
}

.login-submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.form-footer {
  margin-top: 2rem;
  text-align: center;
}

.form-footer p {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

@media (max-width: 1024px) {
  .login-bento-card {
    grid-template-columns: 1fr;
  }

  .login-hero {
    padding: 3rem;
  }

  .login-form-section {
    padding: 3rem;
  }
}

@media (max-width: 640px) {
  .login-hero {
    padding: 2.5rem 1.5rem;
  }

  .hero-title {
    font-size: 2.25rem;
  }

  .login-form-section {
    padding: 2.5rem 1.5rem;
  }
}
</style>
