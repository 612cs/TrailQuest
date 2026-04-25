# TrailQuest

TrailQuest 是一个面向中文用户的徒步路线发现与分享平台，采用 pnpm monorepo 统一管理前端、管理后台、后端与共享 UI 包。

## 项目结构

```text
hiking/
├── apps/
│   ├── web/       # 用户端 SPA — Vue 3 + TypeScript + Vite 7
│   ├── admin/     # 管理后台 SPA — Vue 3 + TypeScript + Vite 7
│   └── api/       # Spring Boot 3 后端
├── packages/
│   └── ui/        # 共享 UI 组件库 (@trailquest/ui)
└── DOCS/          # 项目文档（数据库、后端、前端规范、交接记录）
```

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端框架** | Vue 3 (Composition API + `<script setup>`)、TypeScript、Vite 7 |
| **状态管理** | Pinia 3 |
| **路由** | Vue Router 5 |
| **样式** | Tailwind CSS v4（`@theme` 语法）、CSS Variables |
| **图标** | lucide-vue-next |
| **地图** | 高德地图 JS API（@amap/amap-jsapi-loader） |
| **3D 渲染** | Three.js（轨迹可视化） |
| **文件上传** | 阿里云 OSS STS（ali-oss 客户端直传） |
| **地理数据** | @mapbox/togeojson（GPX/KML → GeoJSON） |
| **后端** | Spring Boot 3.5、Java 17、MyBatis-Plus 3.5、Spring Security |
| **数据库** | MySQL (hikingDB) |
| **缓存** | Redis (Docker) |
| **API 文档** | Springdoc OpenAPI / Swagger UI |
| **测试** | Vitest（单元）、Playwright（E2E） |

## 常用命令

在仓库根目录执行：

```bash
# 安装依赖
pnpm install

# 启动前端开发环境
pnpm dev          # 用户端 (apps/web)
pnpm dev:admin    # 管理后台 (apps/admin)

# 启动后端开发环境
pnpm dev:api      # 等价于 ./dev-api.sh

# 构建
pnpm build        # 构建用户端
pnpm build:admin  # 构建管理后台
pnpm build:api    # Maven package

# 测试
pnpm test:web:unit   # Vitest 单元测试
pnpm test:web:e2e    # Playwright E2E 测试
pnpm test:api        # Maven 测试
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 用户端前端 | `http://localhost:5173` |
| 管理后台 | `http://localhost:5174`（默认） |
| 后端 API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

## 功能概览

### 用户端 (apps/web)

| 模块 | 说明 |
|------|------|
| **首页** | Hero 横幅 + 热门路线推荐（Top 6） |
| **搜索** | 全文搜索 + 多维筛选（难度、背包类型、时长、距离） |
| **社区** | 分页路线动态流，点赞/收藏（乐观更新） |
| **路线详情** | 路线信息、天气实况 + 7 日预报、景观预测（日出/星空/云海/雾凇/冰挂）、GPX/KML 轨迹地图、嵌套评论 |
| **发布路线** | 多阶段上传流水线（封面 → 相册 → 轨迹 → 创建），OSS 直传，草稿自动保存 |
| **AI 助手** | WebSocket + SSE 双通道流式对话，路线卡片推荐，多轮会话管理 |
| **个人中心** | 资料编辑、徒步档案、已发布/已收藏路线 |
| **图片漫游** | 路线图片全屏浏览 |
| **主题** | 明/暗模式切换，跟随系统偏好 |

### 管理后台 (apps/admin)

| 模块 | 说明 |
|------|------|
| **仪表盘** | 概览统计、趋势图表、风险动态、待办事项 |
| **路线审核** | 审批/驳回用户提交的路线 |
| **路线管理** | 路线列表、详情、下架/恢复 |
| **用户管理** | 用户列表、封禁/解封 |
| **评论管理** | 评论列表、隐藏/恢复/删除、批量操作 |
| **举报处理** | 举报列表与处理 |
| **操作日志** | 全量审计日志（含变更前后快照） |
| **配置中心** | 选项分组与条目管理（搜索筛选项等） |
| **站点设置** | 首页 Hero 图片管理 |

## 核心架构

- **Monorepo** — pnpm workspace，前端通过 Vite alias 共享 `@trailquest/ui` 包
- **API 信封模式** — 所有响应统一为 `{ success, data, message, code }` 结构
- **乐观 UI** — 点赞/收藏即时更新，失败自动回滚
- **后台上传队列** — 发布流程异步上传，支持重试和阶段追踪
- **WebSocket + SSE** — AI 聊天双通道流式传输
- **会话持久化** — 发布草稿通过 sessionStorage 跨页面保持
- **服务端配置** — 筛选项懒加载并缓存到 Pinia
- **路由缓存** — 首页、搜索、社区页使用 `<keep-alive>` 缓存
- **TypeScript 全覆盖** — 15 个类型文件完整映射 API 契约

## 项目进度

- 用户端前端已完成主体功能开发（搜索、社区、详情、发布、AI 助手、个人中心）
- 管理后台已完成（仪表盘、路线审核/管理、用户管理、评论管理、举报处理、操作日志、配置中心、站点设置）
- Spring Boot 后端已连通 MySQL 与 Redis
- MyBatis-Plus 分页插件已接入
- Swagger / OpenAPI 已接入
- `media_files` 通用媒体表已设计并落地
- 阿里云 OSS STS 上传链路已打通
- AI 聊天模块已实现 WebSocket + SSE 双通道
- 景观预测系统已接入（日出/星空/云海/雾凇/冰挂）
- 高德天气 API 已接入（实况 + 7 日预报）
- 数据库已通过 12 个升级脚本迭代至当前版本

## 重点文档

- [DOCS 索引](./DOCS/README.md)
- [最新交接文档](./DOCS/handoff/)
- [后端开发清单](./DOCS/backend/backend-development-checklist.md)
- [MySQL 建表脚本](./DOCS/database/mysql-schema.md)
- [MySQL 升级脚本](./DOCS/database/)
- [前端 UI 指南](./DOCS/frontend/UI_GUIDELINES.md)

## 下一步推荐

1. 完成评论模块全量后端化，接入真实数据
2. 路线封面/相册与头像编辑接入真实 OSS 上传
3. 个人页补齐"我的发布 / 我的收藏 / 统计信息"真实数据
4. 地图视图（搜索页列表/地图切换）补全
5. 前端单元测试与 E2E 测试覆盖核心流程
