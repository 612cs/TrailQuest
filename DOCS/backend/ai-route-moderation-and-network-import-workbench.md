# AI 路线审核与网络导入开发工作台

> Markdown 是事实源。HTML 看板如有生成，仅作为只读展示。飞书多维表格如有生成，作为日常状态追踪视图。

## 当前状态

- 状态：开发中
- 规模：L
- 最近更新：2026-05-21
- 结论：WP-1 已冻结，WP-2/WP-3 已完成 MVP，WP-4 正在收口；下一阶段新增“真实联网 provider 升级”批次，替换当前 stub 白名单搜索。

## 一句话总结

当 AI 对话在库内找不到路线时，系统应支持外部搜索并结构化导入路线，但所有用户发布路线和网络导入路线都要先经过 AI 审核，再决定自动通过、自动拒绝或进入人工复核。

## 需求翻译

### 术语表

| 术语 | 人话解释 | 影响 |
| --- | --- | --- |
| AI 路线审核 | 用模型先做一轮内容安全和真实性预判 | 决定路线能否自动通过或进入人工复核 |
| 网络搜索导入 | 库内没命中时，从外部来源找路线基础信息并入库 | 解决路线冷启动与供给不足 |
| AI 网络导入路线 | 通过外部搜索抓到并标准化后的路线记录 | 需要和用户原创路线区分来源与审核状态 |
| 库内命中不足 | 数据库搜索结果为空，或结果数不足以满足推荐 | 决定是否触发外部搜索兜底 |
| 人工复核 | AI 无法明确判定时转后台审核 | 防止误放行和误拒绝 |
| 来源治理 | 记录来源站点、来源 URL、来源图片和导入批次 | 支撑去重、追溯、版权与风控 |

### 已对齐口径

| 决策项 | 结论 | 对实现的影响 |
| --- | --- | --- |
| 用户发布路线 AI 通过后是否直接公开 | 是，AI 通过后直接将 `review_status` 置为 `approved` | 可减少重复人工审核，人工审核链路可逐步收口 |
| 网络导入路线 AI 通过后是否直接公开 | 否，只允许在当前对话里返回并入库，正式公开仍需人工复核 | 需要区分“可引用”和“可公开”两种状态 |
| 外部来源范围 | 先做白名单 | 外部搜索服务需要来源配置与站点控制 |
| AI 风险分类 | 通用风险直接拒绝，业务风险转人工复核 | 审核输出需要分层表达风险类型 |
| 网络收录展示 | 要标记“网络收录”，并提供可信/准确交互 | 前端需要来源标记和用户反馈入口 |

### 建议默认规则

| 规则项 | 建议值 | 说明 |
| --- | --- | --- |
| 升格前提 | 网络收录路线需先存在 `14` 天 | 给内容足够的自然观察窗口 |
| 有效反馈量 | 至少 `20` 个去重后的有效用户反馈 | 防止少量反馈误导结论 |
| 正向阈值 | 可信/准确正向率 `>= 80%` | 说明大多数用户认可 |
| 负向阈值 | 不准确/不可信反馈 `<= 10%` | 避免明显争议路线升格 |
| 稳定性条件 | 最近 `7` 天无新增 AI 拒绝、无人工下架 | 保证近期质量稳定 |
| 升格动作 | 先进入“内部收录候选”，再由管理员一键确认 | 保留最后一道人工把关 |
| 反馈类型 | `可信 / 不可信 / 准确 / 不准确` 其中一组 | 第一版不要做太复杂的投票体系 |
| 反馈展示 | 公开展示正向率和反馈数 | 让用户知道这条路线“热度如何、口碑如何” |
| 防刷基础 | 同一用户同一路线 `7` 天内只能计 1 次有效反馈 | 先用简单规则压住刷票 |
| 防刷增强 | 账号注册满 `7` 天且至少有 `3` 次正常行为后才能计入权重 | 降低新号批量灌票 |
| 防刷隔离 | 同 IP / 同设备短时间高频反馈只记日志，不计入阈值 | 先拦低成本刷票 |

### 业务对象与关系

| 对象 | 说明 | 与其他对象的关系 |
| --- | --- | --- |
| `trails` | 平台核心路线记录 | 当前已承载用户发布路线；后续也会承载网络导入路线 |
| AI 对话请求 | 用户在 AI 助手页输入的自然语言需求 | 先走库内检索，不足时触发外部搜索 |
| 外部路线候选 | 从网络搜索得到的原始候选数据 | 需要抽取、标准化、去重、审核后才能成为平台路线 |
| AI 审核结果 | 模型对路线内容给出的通过/拒绝/人工复核结论 | 影响 `trails` 的最终展示状态和后台待处理列表 |
| 后台审核视图 | 管理员查看待审路线和审核结果的工作台 | 需要同时看见人工审核状态、AI 审核状态和导入来源 |
| 来源记录/导入日志 | 保存外部候选的原始来源与处理结果 | 便于回查、去重、重试和运营排查 |

### 主流程

1. 用户在 AI 对话中输入找路线需求，系统先调用现有路线库检索能力。
2. 如果库内结果足够，则直接按现有 AI 推荐链路返回。
3. 如果库内为空或不足，则触发外部路线搜索，抽取名称、简介、地点、经纬度、图片和来源。
4. 系统对外部结果做去重、地理校验和标准化入库，标记为网络导入路线。
5. 用户发布路线和网络导入路线都会触发 AI 审核。
6. AI 审核输出 `approved / rejected / needs_manual_review` 结论，并回写到路线记录及后台视图。
7. 仅 AI 审核通过的导入路线可返回给当前对话并参与后续库内搜索；高风险拒绝，不确定转人工复核。

### 关键边界情况

1. 外部搜索拿到了文本但拿不到可靠经纬度：允许放弃导入，不强行入库。
2. AI 审核服务超时或失败：不能默认放行，应回写为待人工复核。
3. 外部候选与库内已有路线重复：应复用已有记录或更新来源信息，不能无限新建。
4. 外部图片可展示但可能失效或有版权风险：本期只保留来源与 URL，不默认转存 OSS。
5. 用户明确要求“只看平台已有路线”：不触发外部搜索。

## 范围边界

| 类型 | 内容 |
| --- | --- |
| 前端负责 | AI 对话中展示“网络收录”来源标记；必要时展示外部路线卡片来源信息；网络收录路线的可信/准确反馈入口；后台审核页展示 AI 审核状态、来源站点、来源链接与复核入口 |
| 后端负责 | 路线搜索兜底判定、外部搜索服务、结构化抽取、导入去重、AI 审核服务、状态回写、后台查询字段扩展、导入日志与错误重试 |
| 产品待定 | 图片策略；部分边缘场景的人工兜底口径 |
| 暂不做 | 大规模通用爬虫、轨迹自动生成、图片版权自动识别、景观预测自动补齐、复杂多 Agent 编排平台化、全量 Lark 看板同步 |

## 阶段计划

| 阶段 | 目标 | 交付物 | 前置条件 |
| --- | --- | --- | --- |
| Sprint 0 | 冻结规则与数据方案 | 工作台定稿、字段方案、状态流转口径、来源白名单初稿 | 用户确认 PRD 主方向 |
| Sprint 1 | 先打通 AI 审核主链路 | 用户发布路线 AI 预审、审核回写、后台可见 AI 审核字段 | 确认 AI 审核三态与失败兜底规则 |
| Sprint 2 | 打通外部搜索与导入 MVP | 外部搜索服务、标准化入库、去重、导入后 AI 审核 | 确认来源白名单与经纬度校验方案 |
| Sprint 3 | 接回 AI 对话与后台工作台 | AI 对话外部兜底、来源标记、后台复核与重试入口 | Sprint 1/2 完成并联调可用 |
| Sprint 4 | 稳定性与验收 | 测试用例、错误重试、日志、验收清单闭环 | 前三阶段主流程稳定 |
| Sprint 5 | 真实联网 provider 升级 | 白名单站点真实搜索、抓取、抽取、配置与观测补齐 | MVP 导入链路稳定，可安全替换 stub provider |

## Epic 与用户故事

### Epic 1：路线内容 AI 审核

目标：

让用户发布路线和网络导入路线在进入平台展示层之前，先经过统一的 AI 审核门禁。

用户故事：

- 作为平台管理员，我希望路线先被 AI 预审，以便减少明显低质或违规内容进入人工审核队列。
- 作为平台运营，我希望 AI 审核失败时默认转人工复核，而不是默认通过，以便控制风险。

验收标准：

- [ ] 用户发布路线可自动触发 AI 审核
- [ ] 网络导入路线可自动触发 AI 审核
- [ ] AI 审核支持 `approved / rejected / needs_manual_review`
- [ ] 后台可查看 AI 审核结果、原因和风险分类

### Epic 2：库内无结果时的网络搜索导入

目标：

在现有 AI 对话找路线链路中补一层外部搜索兜底，解决路线冷启动问题。

用户故事：

- 作为普通用户，我希望库内没有相关路线时，AI 仍能给我可参考的路线信息，而不是只说“没有结果”。
- 作为平台，我希望这些结果能入库复用，而不是每次都重复搜索。

验收标准：

- [ ] AI 推荐库内命中不足时会触发外部搜索
- [ ] 外部候选满足最小结构化要求才允许入库
- [ ] 重复候选能命中去重规则
- [ ] 导入路线带有来源字段和来源说明

### Epic 3：导入路线审核后再展示

目标：

确保外部搜索结果不是“抓到就展示”，而是“抓到、审核、通过后展示”。

用户故事：

- 作为普通用户，我希望看到的网络导入路线仍然是平台筛过一层的，而不是原样照搬。
- 作为管理员，我希望不确定或高风险导入路线沉淀到后台复核列表。

验收标准：

- [ ] AI 审核拒绝的导入路线不会出现在前台
- [ ] 需要人工复核的导入路线默认不公开展示
- [ ] AI 审核通过的导入路线可返回当前对话并参与后续检索

### Epic 4：网络收录反馈与升格机制

目标：

让网络收录路线带有“可信 / 准确”等用户交互反馈，并在达到一定正向反馈后可升格为内部收录。

用户故事：

- 作为普通用户，我希望能对网络收录路线投票，帮助平台判断它是否靠谱。
- 作为平台，我希望通过用户反馈识别哪些网络收录路线值得升格为内部收录。

验收标准：

- [ ] 网络收录路线可展示可信度或准确性相关反馈入口
- [ ] 可记录正向反馈数量与趋势
- [ ] 达到阈值后可进入“内部收录候选”流程

## 工作包

### WP-1：冻结审核与导入口径

| 字段 | 内容 |
| --- | --- |
| 类型 | Discovery |
| 状态 | Done |
| Owner | 主 Agent |
| 可并行 | 是 |
| 依赖 | [ai-route-moderation-and-network-import-prd.md](./ai-route-moderation-and-network-import-prd.md)、现有 `trails.review_status` 设计 |
| 范围 | 明确 AI 审核三态、失败兜底、外部来源白名单、自动公开策略、去重口径、来源字段与导入日志方案 |
| 不做 | 不写业务代码、不改前后端页面 |
| 影响范围 | 文档 / 数据模型 / 规则说明 |
| 边界情况 | 审核失败、来源站点不可信、图片失效、经纬度缺失 |
| 验收 | 已形成可实施的字段、状态流转、升格阈值与防刷默认规则，相关待确认问题已收敛为 `Answered` |

### WP-2：用户发布路线 AI 预审主链路

| 字段 | 内容 |
| --- | --- |
| 类型 | Backend dependency |
| 状态 | Done |
| Owner | 子代理 A |
| 可并行 | 是 |
| 依赖 | WP-1；[TrailServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java)；现有后台审核逻辑 |
| 范围 | 在创建/更新路线后触发 AI 审核；回写 AI 审核状态；处理失败兜底；保持现有人工审核链路兼容 |
| 不做 | 不实现外部搜索；不改 AI 对话返回结构；不改后台 UI |
| 影响范围 | `apps/api` 服务层 / 实体字段 / 可能的 SQL 升级脚本 |
| 边界情况 | 创建成功但 AI 审核超时、更新后重复送审、AI 审核明确拒绝、人工审核状态与 AI 状态不一致 |
| 验收 | 已完成独立 `AiTrailModerationService`、创建/更新后的 AI 审核触发、`approved/rejected/needs_manual_review` 三态回写、AI 异常转人工复核兜底，并通过 `TrailServiceImplTest` 与 `AiTrailModerationServiceImplTest` |

### WP-3：外部搜索与标准化导入 MVP

| 字段 | 内容 |
| --- | --- |
| 类型 | Backend dependency |
| 状态 | Done |
| Owner | 子代理 B |
| 可并行 | 是 |
| 依赖 | WP-1；[AiRouteRecommendationServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/ai/AiRouteRecommendationServiceImpl.java)；现有 `TrailService` 数据契约 |
| 范围 | 新增外部搜索服务、标准化结构、来源字段、去重规则、导入日志和导入后触发 AI 审核的服务骨架 |
| 不做 | 不接回 SSE 聊天流；不改后台 UI；不做复杂图片转存 |
| 影响范围 | `apps/api` 服务层 / Mapper / 实体 / SQL 升级脚本 / 文档 |
| 边界情况 | 库内重复、外部候选字段不完整、搜索超时、图片空值、来源不可用 |
| 验收 | 已完成独立 `external import` 服务骨架、最小字段校验、去重抽象、导入日志模型、导入后 AI 审核触发扩展点，并通过 `ExternalTrailImportServiceImplTest`；当前为白名单 stub 搜索，真实联网抓取留待后续接入 |

### WP-4：AI 对话兜底接回与来源透出

| 字段 | 内容 |
| --- | --- |
| 类型 | Integration |
| 状态 | Doing |
| Owner | 主 Agent |
| 可并行 | 否 |
| 依赖 | WP-2、WP-3；[AiChatServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/AiChatServiceImpl.java) |
| 范围 | 在 AI 推荐空结果或结果不足时触发外部搜索；只返回 AI 审核通过的导入路线；为返回结构补来源信息和来源标记，并确保网络收录路线不会通过详情页绕开“仅当前对话可见”限制 |
| 不做 | 不改聊天页 UI 视觉稿；不实现后台工作台字段 |
| 影响范围 | `AiRouteRecommendationService` / `AiChatServiceImpl` / AI VO 结构 / 可能的前端联调协议 |
| 边界情况 | 用户明确只看平台数据、外部搜索慢于 SSE、导入成功但审核未完成、返回结果来源混合、网络收录卡片误跳详情页、导入路线误绑定当前提问用户成为作者 |
| 验收 | AI 对话在库内未命中时能稳定返回“外部收录且审核通过”的路线卡片及文字说明；网络收录路线需明确标记来源，且在未公开前不可作为公开详情入口 |

### WP-5：后台审核页补齐 AI 审核与导入来源视图

| 字段 | 内容 |
| --- | --- |
| 类型 | Frontend |
| 状态 | Backlog |
| Owner | 子代理 D |
| 可并行 | 是 |
| 依赖 | WP-2、WP-3；后台路线审核查询接口补字段 |
| 范围 | 后台列表和详情页展示来源类型、来源站点、来源链接、AI 审核状态、风险等级、审核原因与重试入口占位 |
| 不做 | 不重构整套后台审核信息架构；不新增完整运营报表 |
| 影响范围 | `apps/admin` 路线审核相关页面 / 类型 / API 适配 |
| 边界情况 | 空审核理由、失败状态、来源链接过长、网络导入与用户上传混合筛选 |
| 验收 | 管理员可以分辨“用户上传路线”和“网络导入路线”，并能看懂 AI 审核结论 |

### WP-6：测试、回归与运行手册

| 字段 | 内容 |
| --- | --- |
| 类型 | QA |
| 状态 | Backlog |
| Owner | 主 Agent |
| 可并行 | 是 |
| 依赖 | WP-2、WP-3、WP-4、WP-5 |
| 范围 | 补充接口测试/集成测试建议、主流程回归清单、失败重试与回滚说明、文档更新 |
| 不做 | 不实现业务功能本身 |
| 影响范围 | `apps/api/src/test` / `DOCS/backend` / `DOCS/handoff` |
| 边界情况 | AI 超时、重复导入、审核失败兜底、后台列表筛选、来源标记缺失 |
| 验收 | 主流程具备可执行回归清单，关键失败路径有明确验证办法 |

### WP-7：网络收录反馈与升格机制

| 字段 | 内容 |
| --- | --- |
| 类型 | Frontend / Backend dependency |
| 状态 | Backlog |
| Owner | unknown |
| 可并行 | 否 |
| 依赖 | 网络收录来源标记、导入路线可展示的前提、反刷与阈值规则待定 |
| 范围 | 为网络收录路线增加“可信 / 准确”类用户反馈入口，并为升格为内部收录保留状态与统计口径 |
| 不做 | 不在本期直接实现完整社区信誉系统 |
| 影响范围 | 路线卡片、详情页、来源记录、统计字段 |
| 边界情况 | 重复点击、刷票、恶意踩踏、匿名用户限制 |
| 验收 | 能记录反馈并为后续升格机制留出稳定数据接口 |

## 个人轻量看板

| ID | 用户故事 | Sprint | 类型 | 标题 | 状态 | 阻塞原因 | 可并行 | 子代理建议 | 验收 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| WP-1 | US-1 / US-2 / US-3 | Sprint 0 | Discovery | 冻结审核与导入口径 | Done | 无 | 是 | 主 Agent 本地推进 | 规则、字段、状态流转可实施 |
| WP-2 | US-1 | Sprint 1 | Backend dependency | 用户发布路线 AI 预审主链路 | Done | 无 | 是 | 子代理 A | 已完成 AI 预审服务、三态回写、异常转人工复核，并通过聚焦单测 |
| WP-3 | US-2 | Sprint 2 | Backend dependency | 外部搜索与标准化导入 MVP | Done | 无 | 是 | 子代理 B | 已完成结构化导入骨架、去重和审核触发，并通过聚焦单测 |
| WP-4 | US-2 / US-3 | Sprint 3 | Integration | AI 对话兜底接回与来源透出 | Doing | 无 | 否 | 主 Agent | AI 对话能返回审核通过的外部路线，并正确处理“网络收录 / 仅当前对话可见”边界 |
| WP-5 | US-1 / US-3 | Sprint 3 | Frontend | 后台审核页补齐 AI 审核与导入来源视图 | Backlog | 依赖后台接口补字段 | 是 | 子代理 D | 后台可辨识来源和 AI 审核结果 |
| WP-6 | US-1 / US-2 / US-3 | Sprint 4 | QA | 测试、回归与运行手册 | Backlog | 依赖前置功能稳定 | 是 | 主 Agent 或 QA 子代理 | 形成主流程回归清单与验证记录 |
| WP-7 | US-2 / US-3 | Sprint 4 | Frontend / Backend dependency | 网络收录反馈与升格机制 | Backlog | 依赖网络收录标记与阈值规则 | 否 | 待定 | 记录可信/准确反馈并留出升格机制 |
| WP-8 | US-2 / US-3 | Sprint 5 | Backend dependency | 真实联网 provider 升级 | Done | 无 | 是 | 主 Agent + 子代理研究 / 开发 | 白名单搜索升级为真实联网召回与抽取，且导入审核、去重、来源治理保持不退化 |

## 可并行工作包

| 工作包 | 并行条件 | 写入范围 | 冲突风险 |
| --- | --- | --- | --- |
| WP-1 | 只做规则与文档冻结 | `DOCS/backend` | 低 |
| WP-2 | WP-1 冻结后可开始 | `apps/api` 路线发布与审核相关服务、实体、SQL | 中 |
| WP-3 | WP-1 冻结后可开始 | `apps/api` 外部搜索/导入相关服务、实体、SQL | 中 |
| WP-5 | 先用接口草约开发占位或等 WP-2/WP-3 接口定稿后开始 | `apps/admin` 审核页面和类型 | 低 |
| WP-6 | 对稳定分支做验证和文档补充 | `apps/api/src/test`、`DOCS` | 低 |
| WP-7 | 网络收录反馈和升格口径冻结后可开始 | `apps/web` / `apps/api` 反馈与统计相关范围 | 中 |
| WP-8 | 以 WP-3 MVP 为基础重构 provider 架构后可开始 | `apps/api` external import / config / tests / docs | 中 |

## 并行开发编排

| 批次 | 工作包 | 执行方式 | 负责人 | 备注 |
| --- | --- | --- | --- | --- |
| Batch 0 | WP-1 | 主 Agent 本地执行 | 主 Agent | 已完成冻结 |
| Batch 1 | WP-2、WP-3 | `subagent-driven-development` | 子代理 A、子代理 B | 已完成首批并行开发；主 Agent 负责最终集成收口 |
| Batch 2 | WP-5 | `subagent-driven-development` | 子代理 D | 以前后端接口字段定稿为前提 |
| Batch 3 | WP-4 | 主 Agent 集成 | 主 Agent | 已启动；围绕 AI 对话兜底、来源透出和可见性边界继续收口 |
| Batch 4 | WP-6 | 主 Agent / QA 子代理 | 主 Agent | 在核心链路稳定后收尾 |
| Batch 5 | WP-7 | 后续再排期 | 待定 | 先按默认阈值与防刷规则预留实现位 |
| Batch 6 | WP-8 | `subagent-driven-development` | 主 Agent + 子代理 | 先完成 provider 抽象与配置治理，再接第一个真实联网 provider |

### WP-8：真实联网 provider 升级

| 字段 | 内容 |
| --- | --- |
| 类型 | Backend dependency |
| 状态 | Doing |
| Owner | 主 Agent |
| 可并行 | 是 |
| 依赖 | WP-3、WP-4；[ExternalTrailImportServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/ExternalTrailImportServiceImpl.java)、[StubWhitelistedExternalTrailSearchServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/StubWhitelistedExternalTrailSearchServiceImpl.java)、[external-trail-provider-architecture.md](./external-trail-provider-architecture.md) |
| 范围 | 将当前 stub 白名单搜索升级为真实联网 provider；补齐 provider 抽象、白名单站点配置、候选发现与详情抽取、结构化错误码与日志；保持现有导入去重、AI 审核、仅当前对话可见等约束不变 |
| 不做 | 不在本批次实现全网开放搜索；不把图片自动转存 OSS；不在本批次上线多 provider 聚合排序平台 |
| 影响范围 | `apps/api` external import 服务/配置/测试；`DOCS/backend` 技术设计；可能新增 provider 级集成测试 |
| 边界情况 | 白名单站点无站内搜索接口、动态页需 JS 渲染、搜索成功但抽取字段不完整、provider 超时或限流、导入结果与已有路线重复、来源站点 robots 或版权策略受限 |
| 验收 | 至少一个真实联网 provider 可在白名单域内召回路线候选并结构化导入；失败路径具备统一错误码和日志；不破坏现有审核与可见性边界；已通过编译与 external import targeted test |

## 子代理执行 Prompt

### Prompt：WP-2

```text
你负责执行工作包 WP-2：用户发布路线 AI 预审主链路

Goal：
在用户创建/更新路线后触发 AI 审核，回写 AI 审核状态，并保证 AI 异常时默认进入人工复核而不是默认放行。

Inputs：
- 需求工作台：./ai-route-moderation-and-network-import-workbench.md
- 需求 PRD：./ai-route-moderation-and-network-import-prd.md
- 相关文件：
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/AdminServiceImpl.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/entity/Trail.java

Owned scope：
- 路线创建/更新后的 AI 审核触发
- AI 审核结果字段与状态回写
- 必要的后端服务、实体、SQL 升级脚本

Non-owned scope：
- 外部搜索导入
- AI 对话兜底接回
- 后台前端页面

Dependencies：
- 以工作台 WP-1 的规则为准
- 不要改动外部搜索相关服务命名和契约，除非主 Agent 明确要求

Acceptance：
- 创建/更新路线后可触发 AI 审核
- AI 审核支持 approved/rejected/needs_manual_review
- AI 失败不默认放行
- 与现有 review_status 人工审核链路兼容

Final response format：
- 完成内容
- 修改文件
- 验收结果
- 阻塞/风险
```

### Prompt：WP-3

```text
你负责执行工作包 WP-3：外部搜索与标准化导入 MVP

Goal：
实现库内无结果时可复用的外部路线搜索与标准化导入后端能力，至少覆盖结构化候选、去重、导入记录和导入后触发 AI 审核。

Inputs：
- 需求工作台：./ai-route-moderation-and-network-import-workbench.md
- 需求 PRD：./ai-route-moderation-and-network-import-prd.md
- 相关文件：
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/ai/AiRouteRecommendationServiceImpl.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/TrailService.java
  - ../database/hikingDBStruct.sql

Owned scope：
- 外部搜索服务接口与实现骨架
- 标准化结构和最小字段映射
- 去重规则与导入日志
- 导入后 AI 审核触发点

Non-owned scope：
- 聊天 SSE 集成
- 后台前端页面
- 用户发布路线 AI 审核主链路

Dependencies：
- 以工作台 WP-1 的字段与规则冻结结果为准
- 尽量避免和 WP-2 修改同一服务文件；若必须修改，共享边界请显式说明

Acceptance：
- 服务能接收一个搜索查询并输出结构化候选
- 候选满足最小字段要求才可导入
- 重复候选可命中去重规则
- 导入路线会触发 AI 审核而不是直接公开

Final response format：
- 完成内容
- 修改文件
- 验收结果
- 阻塞/风险
```

### Prompt：WP-5

```text
你负责执行工作包 WP-5：后台审核页补齐 AI 审核与导入来源视图

Goal：
让管理员在后台审核页清楚区分用户上传路线和网络导入路线，并查看 AI 审核状态、风险等级、来源站点与来源链接。

Inputs：
- 需求工作台：./ai-route-moderation-and-network-import-workbench.md
- 需求 PRD：./ai-route-moderation-and-network-import-prd.md
- 相关文件：
  - apps/admin 中路线审核相关 view/api/types
  - apps/api 后台路线审核查询接口对应 VO/Mapper

Owned scope：
- 后台审核列表和详情页展示层
- 前端 API 类型与渲染

Non-owned scope：
- 外部搜索后端服务
- 用户发布路线审核逻辑
- AI 对话集成

Dependencies：
- 依赖后端接口补齐 AI 审核与来源字段
- 不重构整套后台设计系统

Acceptance：
- 后台可筛选或识别来源类型
- 后台可查看 AI 审核状态、原因、风险等级和来源链接
- 空字段与失败状态展示合理

Final response format：
- 完成内容
- 修改文件
- 验收结果
- 阻塞/风险
```

### Prompt：WP-8

```text
你负责执行工作包 WP-8：真实联网 provider 升级

Goal：
将当前 stub 白名单搜索升级为真实联网 provider，实现白名单站点内的真实搜索、抓取与结构化候选返回，同时保持现有导入去重、AI 审核、仅当前对话可见等业务边界不退化。

Inputs：
- 工作台：./ai-route-moderation-and-network-import-workbench.md
- 技术设计：./external-trail-provider-architecture.md
- 相关文件：
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/ExternalTrailSearchService.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/ExternalTrailImportServiceImpl.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/StubWhitelistedExternalTrailSearchServiceImpl.java
  - ../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/ExternalTrailImportProperties.java

Owned scope：
- provider 抽象与 registry / facade
- 白名单站点真实联网搜索与详情抽取
- provider 级配置、错误码、日志字段
- 保留或改造 stub 为测试/回退 provider

Non-owned scope：
- 聊天页 UI 视觉改造
- 后台管理页布局
- 网络收录升格反馈机制

Write scope：
- apps/api external import 相关 service / config / tests
- DOCS/backend provider 设计与运行说明

Dependencies：
- 以现有 WP-3 MVP 契约为基础
- 不要破坏现有导入去重、AI 审核、detailAvailable/仅当前对话可见边界
- 如接入第三方 provider，优先通过环境变量配置密钥，不提交真实密钥

Acceptance：
- 至少一个真实联网 provider 可在白名单域内召回候选
- 候选可被标准化成 ExternalTrailCandidate 并进入现有导入链路
- 超时、限流、抽取失败具备统一错误码和结构化日志
- stub provider 仍可用于本地测试或回退

Final response format：
- 完成内容
- 修改文件
- 验收结果
- 阻塞/风险
```

## 待确认问题

| ID | 分类 | 问题 | 影响 | 建议问谁 | 状态 |
| --- | --- | --- | --- | --- | --- |
| Q-1 | 产品/后端 | 用户发布路线在 AI 审核通过后，是否仍必须保留 `review_status = pending` 等待人工审核，还是允许按配置自动通过 | 影响状态流转和后台待审量 | 产品/你 | Answered |
| Q-2 | 产品/运营 | 网络导入路线的来源白名单有哪些，是否允许先接任意网页搜索结果 | 影响搜索实现边界和风控 | 产品/你 | Answered |
| Q-3 | 产品/运营 | 导入路线默认是否允许前台公开展示，还是只在当前对话中返回并保留后台记录 | 影响曝光策略和数据可信度 | 产品/你 | Answered |
| Q-4 | 后端/产品 | AI 审核风险分类是只做通用内容安全，还是还要加入“路线真实性不足”“经纬度不可信”等业务型风险 | 影响模型输出结构和后台展示字段 | 后端/产品 | Answered |
| Q-5 | 产品/前端 | AI 对话返回的外部路线是否需要在前端做明显“网络收录”样式提示 | 影响前端联调和文案策略 | 产品/前端 | Answered |
| Q-6 | 产品/运营 | 网络收录路线的“可信 / 准确”反馈阈值是多少，如何防刷 | 影响升格机制是否可上线 | 产品/运营/后端 | Answered |

## 风险与阻塞

| ID | 风险 | 影响 | 缓解方式 |
| --- | --- | --- | --- |
| R-1 | LLM 结构化抽取可能幻觉经纬度或简介 | 会把错误路线写入主库 | 对地点和经纬度做地图服务二次校验；不满足最小可信度则放弃导入 |
| R-2 | AI 审核误判 | 可能误拒正常路线或放行风险内容 | 保留人工复核态；高风险拒绝、模糊转人工 |
| R-3 | 外部图片版权与失效问题 | 前台展示不稳定，存在版权隐患 | 本期只记录来源与 URL，不自动转存；后台展示来源页 |
| R-4 | `review_status` 与新增 AI 审核状态混用 | 会让后台逻辑和查询条件变复杂 | 分离 `ai_review_status` 与人工审核状态；在工作台和 SQL 中明确字段职责 |
| R-5 | 并行开发 API 边界不清 | 子代理之间容易改同一文件或互相覆盖 | 先完成 WP-1，明确文件所有权；集成类改动留给主 Agent 收口 |
| R-6 | 真实联网 provider 接入后，成本、超时、站点稳定性和 robots 约束会显著放大 | 可能导致召回不稳定、导入失败率上升或合规风险 | 采用白名单站点优先适配 + Firecrawl 兜底；补齐 provider 配置、错误码、日志和回退 provider；优先接单 provider 小流量验证 |

## 验收清单

- [ ] 工作台与 PRD 一致，没有脱离仓库现实情况
- [ ] 已明确这是 L 级需求，先做 Markdown 事实源
- [x] 已拆出可并行的后端、前端、集成、QA 工作包
- [x] 每个工作包都有范围、依赖、不做项和验收
- [x] 已列出进入并行开发前必须先确认的关键问题
- [x] 子代理 Prompt 已具备直接执行的输入与边界
- [ ] 后续若生成 HTML，看板应从本文件只读渲染

## 执行记录

| 日期 | 工作包 | 动作 | 结果 | 后续 |
| --- | --- | --- | --- | --- |
| 2026-05-20 | WP-1 | 新建工作台并完成首轮拆包 | 已形成 L 级开发工作台初稿 | 继续补齐冻结规则 |
| 2026-05-20 | 用户确认 | 明确 AI 通过即 `review_status=approved`、网络导入仅对话返回且不公开、白名单搜索、通用风险直拒/业务风险转人工、网络收录标记与可信度反馈 | 已收敛核心规则 | 后续围绕 WP-7 补阈值与防刷 |
| 2026-05-20 | WP-1 | 冻结升格阈值与防刷默认规则，关闭主要待确认问题 | WP-1 Done，可进入 Batch 1 并行开发 | 启动 WP-2 与 WP-3 |
| 2026-05-21 | Batch 1 | 启动主 Agent + 子 Agent 并行开发 | WP-2/WP-3 进入 Doing；共享 schema 与查询映射由主 Agent 收口 | 等待子代理结果并集成 |
| 2026-05-21 | WP-2 | 完成用户发布路线 AI 预审主链路 | 新增 `AiTrailModerationService`，创建/更新路线后自动触发 AI 审核并回写三态；AI 异常默认转人工复核；`./mvnw -q -Dtest=TrailServiceImplTest,AiTrailModerationServiceImplTest test` 通过 | 主 Agent 继续推进 WP-4 集成与后台字段收口 |
| 2026-05-21 | WP-3 | 完成外部搜索与标准化导入 MVP 骨架 | 新增独立 external import 服务、stub 白名单搜索、去重抽象、导入日志与审核触发扩展点；`./mvnw -Dtest=ExternalTrailImportServiceImplTest test` 通过 | 主 Agent 收口 WP-4 集成与真实搜索接入策略 |
| 2026-05-21 | WP-4 | 主 Agent 启动 AI 对话兜底接回收口 | 已将外部导入结果映射进 AI 卡片协议，补充“网络收录”来源标记、详情可见性控制，并修复导入路线归属用户导致的详情绕行风险 | 继续完成回归验证并同步只读看板 |
| 2026-05-21 | WP-8 规划 | 完成真实联网 provider 升级方案调研与架构落盘 | 已明确“站点优先适配层 + Firecrawl 兜底 + 少量 Jsoup 抽取器”的推荐路线，并补充 provider 抽象、配置治理和批次建议 | 下一步可进入 WP-8 批次拆分与实现 |
| 2026-05-21 | Batch 6 / WP-8 | 启动主 Agent + 多子 Agent 并行开发，并完成第一批 provider 骨架收口与导入闭环验证 | 已完成 provider 抽象、registry/facade、stub provider 改造、Firecrawl provider 骨架、provider 配置、候选补齐与聚焦测试；`./mvnw -q -DskipTests compile` 与 external import targeted test 通过 | 可以继续做 Firecrawl 更完整的详情抽取与站点级适配增强 |
| 2026-05-21 | Batch 6 / WP-8 | 补齐 Firecrawl v2 默认地址、非中文路线抽取增强与 provider 单测 | 已将 Firecrawl 默认 base URL 切到 `/v2`，补了 `FirecrawlExternalTrailSearchServiceImplTest`，并完成 `./mvnw -q -DskipTests compile` 与外部 provider 聚焦测试 | WP-8 进入收口，可继续做站点级 adapter 扩展 |

## 变更记录

| 日期 | 来源 | 变更 | 影响 |
| --- | --- | --- | --- |
| 2026-05-20 | 用户指令 | 基于 PRD 生成 Markdown 工作台，HTML 视后续需要再生成 | 当前以本文件作为实施事实源 |
| 2026-05-20 | 用户确认 | 补齐 AI 审核和网络收录升格的决策口径 | 工作台进入冻结拆包阶段 |
| 2026-05-20 | 主 Agent | 将 WP-1 标记为冻结完成，并切出 Batch 1 并行开发批次 | 工作台状态切换为开发中 |
| 2026-05-21 | 主 Agent | 启动 Batch 1，并将共享 schema / entity / mapper 收口职责留在主 Agent | 防止 WP-2/WP-3 子代理改动同一批共享文件 |
| 2026-05-21 | 子代理 A | 完成 WP-2 的 service 层实现与聚焦测试，并按现有实体字段对齐 AI 审核回写 | 工作台中 WP-2 进入 Done，可继续推进 WP-4 / WP-5 |
| 2026-05-21 | 主 Agent | 在 WP-4 收口中补齐“网络收录卡片不可直接跳详情”的协议与前端渲染，并改为使用系统导入归属用户而非当前提问用户 | 避免“仅当前对话可见”被详情页或作者权限绕过 |
| 2026-05-21 | 主 Agent + 子代理调研 | 将“真实联网 provider 升级”并回原工作台，并新增独立技术设计文档 | 后续实现不新开平行需求线，而是作为 WP-8 挂接在原工作台下推进 |
