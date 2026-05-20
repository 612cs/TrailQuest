本文给出 TrailQuest 在仓库层面的架构总览，便于 Agent 在动手前快速建立心智模型。详细的依赖方向规则见 DOCS/agents/architecture/boundaries.md。

## 工作区拓扑

```text
hiking/                       pnpm workspace 根
├── apps/
│   ├── web/                  用户端 SPA：Vue 3 + Vite 7 + Pinia 3
│   ├── admin/                管理后台 SPA：Vue 3 + Vite 7
│   └── api/                  Spring Boot 3 + MyBatis-Plus + MySQL + Redis
├── packages/
│   └── ui/                   共享 UI 组件库 @trailquest/ui
├── tools/harness/            Harness 自定义检查脚本
└── DOCS/                     项目文档
```

## 前端运行时分层

```text
src/
├── views/        路由级页面，组合 components 与 stores
├── components/   UI 组件，按特性域分包（home、search、trail、community、chat、profile、auth、common）
├── stores/       Pinia store，跨页面状态与持久化
├── api/          axios 封装与后端接口适配层
├── composables/  纯函数式逻辑复用
├── router/       Vue Router 配置
├── mock/         （仅 web）TypeScript 接口与开发期假数据
├── types/        类型定义
└── utils/        无副作用工具函数
```

依赖方向：`utils / types → mock → api → stores → composables / components → views`。下层不感知上层。

## 后端运行时分层

```text
apps/api/src/main/java/
├── controller/   REST 控制器，只做参数校验与委托
├── service/      业务逻辑
├── mapper/       MyBatis-Plus Mapper
├── entity/       数据库实体
├── dto/          请求 / 响应载体
├── config/       Spring 配置
└── common/       响应封装、异常处理、通用工具
```

依赖方向：`entity / dto → mapper → service → controller`。Controller 不直接操作数据库，Service 不直接返回 entity。

## 跨进程协作

- 前端通过 `axios` 调用后端 REST，统一信封 `{ success, data, message, code }`。
- AI 聊天走 WebSocket + SSE 双通道。
- 上传走 OSS STS 客户端直传，前端拿临时凭证后直传 OSS。
- 缓存使用 Redis（Docker），仅后端访问。

## 关键约定速查

| 主题 | 来源 |
|------|------|
| 依赖边界与硬规则 | DOCS/agents/architecture/boundaries.md |
| 命名 / 编码规范 | DOCS/agents/conventions/README.md |
| 测试策略 | DOCS/agents/conventions/testing.md |
| 数据库脚本 | DOCS/database/ |
| 工具链 | DOCS/TOOLCHAIN.md |

修改架构（新增子包、调整分层）时，请同步更新本文件、`DOCS/agents/architecture/boundaries.md` 与 `tools/harness/check-boundaries.mjs`。
