# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

TrailQuest is a Vue 3 + TypeScript hiking trail discovery application with features including trail search, community sharing, AI assistant chat, and user profiles.

## Common Commands

```bash
# Install dependencies (uses pnpm)
pnpm install

# Start development server
pnpm dev

# Build for production
pnpm build

# Preview production build
pnpm preview
```

## Tech Stack

- **Vue 3** with Composition API and `<script setup>`
- **TypeScript**
- **Vite 7** for build tooling
- **Pinia** for state management
- **Vue Router 5** for routing
- **Tailwind CSS v4** for styling
- **lucide-vue-next** for icons

## Architecture

### Directory Structure
```
src/
├── components/          # Vue components
│   ├── common/         # Reusable base components (BaseIcon, ActionButton, BaseModal, etc.)
│   ├── home/          # Home page components
│   ├── trail/         # Trail-related components
│   ├── search/        # Search components
│   ├── chat/          # Chat components
│   ├── community/     # Community components
│   └── profile/       # Profile components
├── views/             # Page-level components (routes)
├── stores/            # Pinia stores (counter, chat, theme, useUserStore)
├── mock/              # Mock data and TypeScript interfaces
├── router/            # Vue Router configuration
└── style.css          # Global styles with Tailwind v4 theme
```

### Routes
| Path | Name | Description |
|------|------|-------------|
| `/` | Home | Trail discovery with activity grid |
| `/search` | Search | Trail search with filters |
| `/community` | Community | Community posts feed |
| `/profile` | Profile | User profile and settings |
| `/publish` | Publish | Publish new trail |
| `/chat` | Chat | AI assistant |
| `/trail/:id` | TrailDetail | Trail details with reviews |

### Data Models (in `src/mock/mockData.ts`)
- `User` - User profile with avatar
- `Trail` / `TrailDetail` - Trail information with difficulty, distance, elevation
- `Review` - Trail reviews with nested replies
- `ChatMessage` - AI chat messages

### State Management
- Pinia stores in `src/stores/`
- Theme store handles dark/light mode with CSS variables
- User store manages authentication state (mock)

## Key Conventions

### UI Guidelines
- **No emojis** - Use `lucide-vue-next` icons instead
- Use `stroke-width="2"` for consistent icon styling
- Use CSS variables (e.g., `var(--primary-500)`) instead of hardcoded colors
- **主题适配** - 所有 UI 开发必须适配白天和暗黑模式
- Use `animate-fade-in-up` class for entrance animations

### Component Structure
- Components should be placed in appropriate feature directories
- View files handle layout and data fetching, delegate to smaller components
- All mock data must be in `src/mock/` directory

### Styling (Tailwind CSS v4)
- Theme tokens defined in `style.css` (`@theme` block)
- Custom CSS variables for dark/light mode
- Use `.card` class for standard card styling
- Use `.glass-header` for header with blur effect
- Badge classes: `.badge-easy`, `.badge-moderate`, `.badge-hard`

### TypeScript
- Define interfaces for all data structures
- Mock data uses proper TypeScript interfaces for API migration readiness

# 代码提交

- **流程**：每当完成一个独立的功能模块（如：一个完整的页面、一个复杂的组件或逻辑重构）后，必须使用 `github-mcp-server` 提供的工具将变更提交到远程仓库。
- **提交信息**：遵循约定式提交规范（Conventional Commits），如 `feat:`, `fix:`, `docs:`, `refactor:` 等，并且使用中文进行提交。
- **工具优先**：优先使用 GitHub MCP 进行跨系统的直接提交，以确保操作的原子性和可追溯性。
- git提交信息必须是中文
