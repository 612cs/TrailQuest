# 外部路线真实联网 Provider 升级设计

## 文档定位

本文是 [ai-route-moderation-and-network-import-workbench.md](./ai-route-moderation-and-network-import-workbench.md) 中 `WP-8` 的技术设计文档，目标是把当前基于 stub catalog 的白名单外部搜索升级为真实联网 provider，同时不破坏现有导入、去重、AI 审核与可见性边界。

## 背景

当前外部导入 MVP 已完成以下能力：

- 库内无结果时触发外部导入编排
- 统一 `ExternalTrailCandidate` 结构
- 最小字段校验
- 去重抽象
- 导入后 AI 审核
- “网络收录，仅当前对话可见”边界控制

当前限制：

- [StubWhitelistedExternalTrailSearchServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/StubWhitelistedExternalTrailSearchServiceImpl.java) 仅使用本地 catalog 模拟白名单站点搜索
- 没有真实联网搜索、抓取、抽取能力
- provider 级配置、错误码、日志、回退策略仍不完整

## 目标

1. 将 stub 白名单搜索升级为真实联网搜索
2. 支持白名单域名内的候选 URL 发现与详情抽取
3. 保持现有导入链路契约不变：候选标准化、去重、AI 审核、可见性控制继续复用
4. 为后续多 provider 扩展留出稳定抽象

## 非目标

- 不做开放式全网搜索
- 不在本期实现图片自动转存 OSS
- 不在本期实现多 provider 聚合排序平台
- 不在本期实现网络收录升格反馈机制

## 方案结论

推荐采用组合方案：

1. 白名单站点优先适配层
2. Firecrawl 作为统一联网搜索/抓取/抽取兜底
3. 对静态详情页，按需补少量 Jsoup 专用抽取器

不建议把通用搜索 API 单独当主方案，也不建议新接入 Google Site Restricted JSON API，因为其已于 2025-01-08 停止服务。

## 为什么这样选

### 白名单站点优先适配层

优先尝试：

- 官方 API
- 站内搜索接口
- RSS / JSON Feed
- `sitemap.xml`
- 固定路线列表页

优点：

- 误召回最低
- 结构更稳定
- 更符合白名单治理模型

缺点：

- 接入成本按站点线性增长
- 每站需要单独摸规则

### Firecrawl 兜底层

用途：

- 白名单域内搜索发现候选 URL
- 动态页面抓取
- 首版结构化抽取

优点：

- 搜索、抓取、抽取一体
- 对动态页更友好
- 适合 0 到 1 先把真实联网能力跑通

缺点：

- 有成本
- 对供应商存在依赖
- 抽取结果仍需字段校验

### Jsoup 专用抽取器

用途：

- 对已知静态详情页做低成本高可控抽取

优点：

- Java 原生
- 成本低
- 对稳定静态页维护简单

缺点：

- 无法独立覆盖大量 JS 渲染页面

## 目标架构

```text
AiRouteRecommendationService
  -> ExternalTrailImportService (orchestrator)
       -> ExternalTrailSearchFacade
            -> ExternalTrailProviderRegistry
                 -> SiteSpecificProviderAdapter
                 -> FirecrawlSearchProvider
                 -> StubProvider (test/fallback)
       -> ExternalTrailContentExtractor
            -> JsoupDetailExtractor
            -> FirecrawlExtractAdapter
       -> ExternalTrailCandidateNormalizer
       -> ExternalTrailCandidateValidator
       -> TrailImportDeduplicationService
       -> ExternalTrailGeoEnrichmentService
       -> TrailImportModerationTrigger
       -> TrailMapper / persistence
```

## 推荐分层

### 保留

- [ExternalTrailImportService.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/ExternalTrailImportService.java)
- [ExternalTrailImportServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/ExternalTrailImportServiceImpl.java)
- [TrailImportDeduplicationService.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/TrailImportDeduplicationService.java)
- [TrailImportModerationTrigger.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/TrailImportModerationTrigger.java)
- [ExternalTrailCandidate.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/model/ExternalTrailCandidate.java)

### 重构

- [ExternalTrailSearchService.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/external/ExternalTrailSearchService.java)
  - 现状语义过粗
  - 建议拆成：
    - `ExternalTrailProvider`
    - `ExternalTrailProviderRegistry`
    - `ExternalTrailSearchFacade`

- [ExternalTrailImportProperties.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/ExternalTrailImportProperties.java)
  - 建议迁入 `config`
  - 按 provider 维度拆配置

- [StubWhitelistedExternalTrailSearchServiceImpl.java](../../apps/api/src/main/java/com/sheng/hikingbackend/service/impl/external/StubWhitelistedExternalTrailSearchServiceImpl.java)
  - 从默认实现降级为：
    - `StubProvider`
    - 或 contract test fixture

### 新增建议

- `ExternalTrailProvider`
- `ExternalTrailProviderRegistry`
- `ExternalTrailSearchFacade`
- `ExternalTrailContentExtractor`
- `ExternalTrailCandidateNormalizer`
- `ExternalTrailGeoEnrichmentService`
- `TrailImportAuthorResolver`
- `TrailImportFailureDetail`

## 统一数据流

1. AI 对话判定为路线推荐
2. 库内无结果或结果不足
3. `ExternalTrailSearchFacade` 发起白名单召回
4. provider 返回候选 URL 或结构化结果
5. `ExternalTrailContentExtractor` 补齐详情页字段
6. `ExternalTrailCandidateNormalizer` 转为统一候选结构
7. `ExternalTrailCandidateValidator` 进行最小字段校验
8. `TrailImportDeduplicationService` 去重
9. `ExternalTrailImportServiceImpl` 入库
10. `TrailImportModerationTrigger` 触发 AI 审核
11. 审核通过则仅当前对话返回，公开仍需人工复核

## 配置设计

建议新增：

```properties
trail.external-import.enabled=true
trail.external-import.default-provider=firecrawl
trail.external-import.max-candidates=3
trail.external-import.fallback-author-id=1
trail.external-import.geo-enrich.enabled=true
trail.external-import.cache.ttl-seconds=900

trail.external-import.providers[0].code=firecrawl
trail.external-import.providers[0].enabled=true
trail.external-import.providers[0].base-url=https://api.firecrawl.dev
trail.external-import.providers[0].api-key=${FIRECRAWL_API_KEY}
trail.external-import.providers[0].connect-timeout-ms=8000
trail.external-import.providers[0].read-timeout-ms=30000
trail.external-import.providers[0].retry=2
trail.external-import.providers[0].qps=2
trail.external-import.providers[0].whitelist-sites[0]=example.com
```

注意：

- 第三方密钥必须从环境变量注入
- 不应提交到 `application-local.properties`

## 错误码建议

- `EXTERNAL_TRAIL_PROVIDER_NOT_FOUND`
- `EXTERNAL_TRAIL_PROVIDER_DISABLED`
- `EXTERNAL_TRAIL_PROVIDER_TIMEOUT`
- `EXTERNAL_TRAIL_PROVIDER_RATE_LIMITED`
- `EXTERNAL_TRAIL_PROVIDER_REQUEST_FAILED`
- `EXTERNAL_TRAIL_PROVIDER_PAYLOAD_INVALID`
- `EXTERNAL_TRAIL_CANDIDATE_INVALID`
- `EXTERNAL_TRAIL_IMPORT_AUTHOR_MISSING`
- `EXTERNAL_TRAIL_IMPORT_PERSIST_FAILED`
- `EXTERNAL_TRAIL_DEDUP_FAILED`
- `EXTERNAL_TRAIL_MODERATION_FAILED`

## 日志与观测

建议结构化日志字段：

- `traceId`
- `batchNo`
- `providerCode`
- `providerRequestId`
- `requesterUserId`
- `candidateExternalId`
- `sourceSite`
- `sourceUrl`
- `latencyMs`
- `resultCount`
- `dedupeRule`
- `moderationDecision`
- `conversationVisible`
- `publiclyVisible`
- `errorCode`

## 验收标准

### 批次验收

1. 至少一个真实联网 provider 可在白名单域内召回候选
2. 候选可转成统一 `ExternalTrailCandidate`
3. 导入后仍走现有去重和 AI 审核链路
4. 导入结果仍满足“仅当前对话可见，不公开详情绕行”
5. 失败路径有统一错误码和结构化日志
6. stub provider 仍可用于本地测试或回退

## 推荐开发批次

### Batch 1：抽象成型，不接真网

- 拆 `ExternalTrailSearchService`
- 引入 provider registry / facade / extractor 抽象
- 将 stub 改造成测试/回退 provider

### Batch 2：配置与错误治理

- 新增 provider 级配置
- 新增错误码与结构化日志
- 清理本地配置中的真实密钥

### Batch 3：首个真实 provider 接入

- 接入 Firecrawl
- 支持白名单域内搜索发现
- 支持详情抽取

### Batch 4：站点专用适配优化

- 为高频白名单站点补 sitemap / 站内搜索 / Jsoup 详情抽取器

### Batch 5：多 provider 扩展

- 按需支持多 provider 聚合、排序、熔断、回退

## 推荐优先级

短期：

- `Firecrawl + 白名单站点适配层`

中期：

- 高频站点逐步替换成站点专用 adapter
- 静态详情页逐步替换成 Jsoup

长期：

- Firecrawl 保留兜底
- 每个白名单域维护 `source profile`

## 风险

1. provider 成本上升
2. 白名单站点改版导致抽取失效
3. 动态页延迟与限流影响对话时延
4. provider 错误被当成无结果，造成“误以为没路线”
5. 第三方密钥配置不规范带来安全风险

## 参考资料

- [Google Custom Search JSON API Overview](https://developers.google.com/custom-search/v1/overview)
- [Google Site Restricted JSON API](https://developers.google.com/custom-search/v1/site_restricted_api)
- [Brave Search API](https://brave.com/search/api/)
- [Firecrawl Search](https://docs.firecrawl.dev/api-reference/v2-endpoint/search)
- [Firecrawl Scrape](https://docs.firecrawl.dev/api-reference/endpoint/scrape)
- [Firecrawl Extract](https://docs.firecrawl.dev/api-reference/endpoint/extract)
- [jsoup Cookbook](https://jsoup.org/cookbook/)
- [Sitemaps Protocol](https://www.sitemaps.org/protocol.html)
