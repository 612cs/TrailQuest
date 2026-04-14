# 5 系统详细设计与实现

## 5.1 开发环境搭建与项目初始化

系统开发环境围绕前后端分离架构展开组织。

在工程实现上，项目采用 monorepo 方式管理多个应用模块，将用户前端页面系统放在 `apps/web` 中，将后台管理端放在 `apps/admin` 中，将 Spring Boot 后端服务放在 `apps/api` 中。

这种组织方式便于统一管理依赖、脚本命令、接口联调和后续测试流程，也有利于在论文中明确区分前台用户界面、后台管理界面和服务端能力之间的边界。

前端部分通过 Vite 提供开发服务器与构建能力，后端部分通过 Maven 管理依赖并运行 Spring Boot 服务。

从命令组织来看，项目已经提供前端启动、后台启动、后端启动、前端构建、后端构建、前端单元测试、前端端到端测试和后端测试等脚本入口。

这说明系统已经具备较为完整的基础开发环境与工程化运行条件。

## 5.2 前端核心模块实现

### 5.2.1 首页实现

模块描述：首页是平台对外展示的第一入口，主要负责整合热门路线、主题活动入口和首页运营内容。

当前项目中的首页围绕热门路线卡片、活动入口和 AI 悬浮按钮等内容进行组织，用户进入系统后可以快速完成路线发现、导航跳转和热点内容浏览。

核心代码展示如下：

```ts
const trails = ref<TrailListItem[]>([])
const isLoading = ref(false)
const errorMessage = ref('')

const popularTrails = computed(() => trails.value
  .map((trail) => trailInteractionStore.applyToTrail(trail))
  .map(toHomeTrailCard))

onMounted(() => {
  setTimeout(() => {
    isInitialLoad.value = false
  }, 1000)
  void loadTrails()
})

async function loadTrails() {
  const data = await fetchTrails({
    sort: 'hot',
    pageNum: 1,
    pageSize: 6,
  })
  trails.value = data.list
  trailInteractionStore.hydrateTrails(data.list)
}
```

实现截图：图5.x 首页实现效果图（占位）

### 5.2.2 搜索页实现

模块描述：搜索页承担路线检索任务，支持用户按照关键字、难度、距离、耗时和装备类型等维度缩小结果范围。

该页面在实现上将筛选条件与接口查询参数进行映射，并在用户切换筛选项后立即刷新路线列表，从而形成“筛选即反馈”的交互体验。

核心代码展示如下，以下代码为核心实现摘录：

```ts
const filterModel = ref({
  difficulty: 'all',
  packType: 'all',
  durationType: 'all',
  distance: 'all',
})

const filters = computed<FilterItem[]>(() => [
  { key: 'difficulty', label: '难度', options: [{ label: '全部难度', value: 'all' }, ...resolveConfigOptions('search_difficulty')] },
  { key: 'distance', label: '路线长度', options: [{ label: '全部长度', value: 'all' }, ...resolveConfigOptions('search_distance')] },
  { key: 'packType', label: '装备', options: [{ label: '全部装备', value: 'all' }, ...resolveConfigOptions('search_pack_type')] },
  { key: 'durationType', label: '耗时', options: [{ label: '全部时长', value: 'all' }, ...resolveConfigOptions('search_duration_type')] },
])

async function loadTrails() {
  const data = await fetchTrails({
    keyword: currentSearchQuery.value || undefined,
    difficulty: withAllAsUndefined(filterModel.value.difficulty),
    packType: withAllAsUndefined(filterModel.value.packType),
    durationType: withAllAsUndefined(filterModel.value.durationType),
    distance: withAllAsUndefined(filterModel.value.distance),
    sort: 'latest',
    pageNum: 1,
    pageSize: 30,
  })
  trails.value = data.list
}
```

实现截图：图5.x 搜索页实现效果图（占位）

### 5.2.3 社区页实现

模块描述：社区页主要用于展示用户发布的路线体验、图文内容和互动反馈，是平台沉淀 UGC 内容的重要页面。

社区页通过分页加载方式获取最新路线动态，并结合点赞、收藏和分享行为让用户在浏览过程中完成轻量互动。

核心代码展示如下：

```ts
const pageSize = 2
const currentPage = ref(1)
const allPosts = ref<TrailListItem[]>([])

const currentPosts = computed(() => {
  return allPosts.value
    .map((trail) => trailInteractionStore.applyToTrail(trail))
    .map(toCommunityPost)
})

async function loadPosts() {
  const data = await fetchTrails({
    sort: 'latest',
    pageNum: currentPage.value,
    pageSize,
  })
  allPosts.value = data.list
  trailInteractionStore.hydrateTrails(data.list)
  totalPages.value = data.totalPages || 1
}

async function handleShare(post: TrailListItem) {
  await shareTrail({
    id: post.id,
    name: post.name,
    location: post.location,
  })
}
```

实现截图：图5.x 社区页实现效果图（占位）

### 5.2.4 路线详情页与地图展示实现

模块描述：路线详情页是系统信息最集中的页面，需要展示路线基础信息、评论、天气、景观预测、地图轨迹和图片入口等内容。

为了减少页面内多次请求造成的等待时间，系统在详情页加载阶段会并行获取路线详情、天气数据和景观预测数据，并将轨迹与地图展示能力绑定在同一页面中。

核心代码展示如下：

```ts
watch(trailData, async (trail, _prev, onCleanup) => {
  if (!trail) return

  const controller = new AbortController()
  onCleanup(() => controller.abort())

  void resolveGeo({
    ip: trail.ip,
    locationLabel: `${trail.location} ${trail.name}`,
    signal: controller.signal,
  })

  const [weatherResult, landscapeResult] = await Promise.allSettled([
    fetchTrailWeather(trail.id, controller.signal),
    fetchTrailLandscapePrediction(trail.id, 7, controller.signal),
  ])

  if (weatherResult.status === 'fulfilled') {
    trailWeather.value = weatherResult.value
  }
  if (landscapeResult.status === 'fulfilled') {
    landscapePrediction.value = landscapeResult.value
  }
}, { immediate: true })
```

实现截图：图5.x 路线详情页与地图展示效果图（占位）

### 5.2.5 个人中心页实现

模块描述：个人中心页主要负责用户资料管理、已发布路线管理、已收藏路线管理和徒步画像维护，是平台个性化服务的重要入口。

系统在该页面中以分页方式分别加载“我的发布”和“我的收藏”两类数据，并在切换标签时保持数据状态同步。

核心代码展示如下：

```ts
const feeds = reactive<Record<ProfileTab, ProfileTrailFeedState>>({
  posts: createFeedState(),
  saved: createFeedState(),
})

async function loadFeed(tab: ProfileTab, reset = false) {
  const feed = feeds[tab]
  const targetPage = reset ? 1 : feed.pageNum

  const response = tab === 'posts'
    ? await authApi.fetchMyPublishedTrails(targetPage, PAGE_SIZE)
    : await authApi.fetchMyFavoriteTrails(targetPage, PAGE_SIZE)

  trailInteractionStore.hydrateTrails(response.list)
  feed.items = reset ? response.list : [...feed.items, ...response.list]
  feed.pageNum = response.pageNum + 1
  feed.total = response.total
  feed.hasMore = response.pageNum < response.totalPages
}
```

实现截图：图5.x 个人中心页实现效果图（占位）

### 5.2.6 发布页实现

模块描述：发布页是路线内容生产的核心页面，用户可在此填写路线基础信息、上传封面与相册、导入轨迹文件并完成结构化路线提交。

当前项目中的发布页同时支持新建与编辑两种模式，并通过草稿机制、轨迹渲染和位置解析逻辑增强用户填写体验。

核心代码展示如下：

```ts
const editTrailId = computed<EntityId | null>(() => {
  const rawEdit = route.query.edit
  return Array.isArray(rawEdit) ? rawEdit[0] ?? null : rawEdit ?? null
})

async function prepareDraft() {
  const scopeKey = currentScopeKey.value
  const mode = isEditMode.value ? 'edit' : 'create'
  currentDraft.value = publishUploadStore.ensureDraft(scopeKey, mode, editTrailId.value)

  if (!isEditMode.value || !editTrailId.value || currentDraft.value.hydratedFromServer) {
    return
  }

  const detail = await fetchTrailDetail(editTrailId.value)
  currentDraft.value = publishUploadStore.hydrateDraftFromTrail(scopeKey, detail)
  await renderMapWithGeoJSON()
}
```

实现截图：图5.x 发布页实现效果图（占位）

### 5.2.7 AI 助手页实现

模块描述：AI 助手页是智能交互能力的前端承载页面，负责会话列表管理、消息展示、流式推荐结果接收和追问交互。

该页面通过聊天 store 与后端 AI 会话服务联动，在用户发送消息后实时接收流式返回，并将路线卡片和追问建议插入到消息流中。

核心代码展示如下：

```ts
const suggestions = [
  { icon: 'Compass', text: '推荐杭州周边适合周末单日徒步的路线' },
  { icon: 'Trees', text: '想找一条适合新手、风景好的森林路线' },
]

async function handleSend(text: string) {
  if (!userStore.isLoggedIn) {
    userStore.showAuthModal = true
    return
  }

  await chatStore.sendMessage(text, {
    currentCity: userStore.profile?.location || null,
  })
  scrollToBottom()
}

watch(
  () => chatStore.messages.length,
  () => scrollToBottom(),
)
```

实现截图：图5.x AI 助手页实现效果图（占位）

### 5.2.8 路线图片展示页实现

模块描述：路线图片展示页用于承接路线详情页中的图片浏览需求，使路线相册具备更强的沉浸式阅读效果。

该页面通过单独的图片漫游组件和预览模态框，为用户提供比普通相册列表更直观的视觉展示方式。

核心代码展示如下：

```ts
const galleryImages = computed(() => {
  const urls = [trailData.value?.image, ...(trailData.value?.gallery?.map((item) => item.url) ?? [])]
    .filter((item): item is string => !!item)
  return urls.filter((url, index) => urls.indexOf(url) === index)
})

async function loadTrailDetail(id: string) {
  trailData.value = await fetchTrailDetail(id)
}

function handlePreview(index: number) {
  previewIndex.value = index
  showPreview.value = true
}

function handleBack() {
  void router.push(`/trail/${trailId.value}`)
}
```

实现截图：图5.x 路线图片展示页实现效果图（占位）

### 5.2.9 Admin 系统页面实现

后台管理端作为独立应用存在，采用单独路由与壳层布局组织页面。

前端通过管理员登录、权限校验和页面路由守卫，确保后台功能只在管理员身份下可见。

#### 5.2.9.1 后台登录页

模块描述：后台登录页负责管理员身份验证与权限拦截，是后台系统的统一入口。

核心代码展示如下：

```ts
const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  if (!email.value.trim() || !password.value.trim()) {
    errorMessage.value = '请输入邮箱和密码'
    return
  }

  loading.value = true
  const result = await authStore.login(email.value.trim(), password.value)
  if (result.success) {
    await router.replace({ name: 'dashboard' })
  }
  loading.value = false
}
```

实现截图：图5.x 后台登录页实现效果图（占位）

#### 5.2.9.2 后台首页

模块描述：后台首页用于展示路线审核统计、治理风险提示、趋势信息和快捷入口，是管理员进入后台后的总览界面。

核心代码展示如下：

```ts
const { summary, loading, todoItems, loadSummary } = useDashboardSummary()

onMounted(() => {
  void loadSummary()
})

// 模板中依次渲染：
// 1. DashboardOverviewCards
// 2. DashboardTodoPanel
// 3. DashboardRiskFeed
// 4. DashboardTrendChart
// 5. DashboardQuickLinks
```

实现截图：图5.x 后台首页实现效果图（占位）

#### 5.2.9.3 操作日志页

模块描述：操作日志页用于按模块、动作、对象和时间维度检索后台治理日志，增强后台审计能力。

核心代码展示如下：

```ts
const {
  list,
  total,
  pageNum,
  moduleCode,
  actionCode,
  operatorKeyword,
  load,
  resetFilters,
  openDetail,
} = useOperationLogList()

onMounted(() => {
  void load()
})
```

实现截图：图5.x 操作日志页实现效果图（占位）

#### 5.2.9.4 路线审核列表与详情页

模块描述：路线审核模块由审核列表页和审核详情页组成，前者负责展示待审路线，后者负责完成通过或驳回操作。

核心代码展示如下：

```ts
const { list, reviewStatus, keyword, authorKeyword, load } = useTrailReviewList()

function openDetail(id: string) {
  router.push({ name: 'trail-review-detail', params: { id } })
}

async function handleApprove() {
  await approveTrail(trailId.value)
  await loadDetail()
}

async function handleReject(reason: string) {
  await rejectTrail(trailId.value, { remark: reason })
  await loadDetail()
}
```

实现截图：图5.x 路线审核列表与详情页实现效果图（占位）

#### 5.2.9.5 路线管理列表与详情页

模块描述：路线管理模块面向已进入平台的路线内容，支持按状态检索并执行下架或恢复操作。

核心代码展示如下：

```ts
const { list, status, keyword, authorKeyword, load } = useTrailManagementList()
const isOffline = computed(() => detail.value?.status === 'deleted')

async function handleConfirm() {
  if (isOffline.value) {
    await restoreTrail(trailId.value)
  } else {
    await offlineTrail(trailId.value)
  }
  await loadDetail()
}
```

实现截图：图5.x 路线管理列表与详情页实现效果图（占位）

#### 5.2.9.6 用户管理页

模块描述：用户管理页负责展示用户列表、角色、账号状态和必要的治理入口，并支持封禁与解封等操作。

核心代码展示如下：

```ts
const {
  list,
  keyword,
  role,
  load,
  banUser,
  unbanUser,
} = useUserManagement()

async function handleBan(reason: string) {
  await banUser(targetUser.value!.id, reason)
}

async function handleUnban() {
  await unbanUser(targetUser.value!.id)
}
```

实现截图：图5.x 用户管理页实现效果图（占位）

#### 5.2.9.7 评论管理页

模块描述：评论管理页负责查看评论内容、评论状态和所属路线信息，并支持隐藏、恢复、删除和批量治理。

核心代码展示如下：

```ts
const {
  list,
  selectedIds,
  load,
  openDetail,
  requestHide,
  requestRestore,
  requestDelete,
  requestBatchHide,
  requestBatchRestore,
} = useReviewManagement()

onMounted(() => {
  void load()
})
```

实现截图：图5.x 评论管理页实现效果图（占位）

#### 5.2.9.8 举报处理页

模块描述：举报处理页用于集中展示待处理举报信息，并提供人工核查后的处理入口。

核心代码展示如下：

```ts
const list = ref<AdminReportListItem[]>([])
const pageNum = ref(1)

async function load(page = pageNum.value) {
  const result = await fetchAdminReports({ pageNum: page, pageSize: pageSize.value })
  list.value = result.list
  total.value = result.total
}

async function handleResolve(id: string | number) {
  await resolveReport(id)
  await load(pageNum.value)
}
```

实现截图：图5.x 举报处理页实现效果图（占位）

#### 5.2.9.9 设置中心页

模块描述：设置中心页主要用于维护首页大图等站点级运营配置，并提供上传、保存和清空等操作。

核心代码展示如下：

```ts
const heroImageUrl = ref('')
const heroSaving = ref(false)
const heroUploading = ref(false)

async function loadHeroSetting() {
  const result = await fetchAdminHomeHeroSetting()
  heroImageUrl.value = result.imageUrl?.trim() || ''
}

async function saveHeroSetting() {
  heroSaving.value = true
  await updateAdminHomeHeroSetting({ imageUrl: heroImageUrl.value.trim() || undefined })
  heroSaving.value = false
}
```

实现截图：图5.x 设置中心页实现效果图（占位）

#### 5.2.9.10 配置中心页

模块描述：配置中心页用于维护系统选项组和选项项，包括徒步经验、路线风格、首页活动入口和搜索筛选项等动态配置数据。

核心代码展示如下：

```ts
const groups = ref<AdminOptionGroup[]>([])
const selectedGroupCode = ref('')
const drafts = ref<ItemDraft[]>([])

async function loadGroups() {
  const result = await fetchAdminOptionGroups()
  groups.value = result
  if (!selectedGroupCode.value && result.length) {
    selectedGroupCode.value = result[0].groupCode
  }
}

async function saveDraft(draft: ItemDraft) {
  await updateAdminOptionItem(selectedGroup.value!.groupCode, draft.id, {
    itemLabel: draft.itemLabel.trim(),
    sortOrder: Number(draft.sortOrder),
    enabled: draft.enabled,
  })
}
```

实现截图：图5.x 配置中心页实现效果图（占位）

## 5.3 后端核心服务实现

### 5.3.1 用户认证与权限控制

模块描述：用户认证与权限控制服务负责用户注册、登录、当前用户信息获取以及后台管理员权限隔离，是前后台全部业务的安全基础。

系统采用 Spring Security 与 JWT 机制实现登录态识别，并通过普通用户与管理员角色区分前台和后台接口访问权限。

核心代码展示如下：

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.getCurrentUser(userDetails.getId()));
    }
}
```

实现截图：图5.x 用户认证与权限控制接口调试图（占位）

### 5.3.2 路线发布服务

模块描述：路线发布服务负责接收前端发布页提交的结构化表单数据，并在后台完成路线新增、更新、删除、点赞和收藏等核心业务处理。

该服务在提交阶段会同时处理作者身份、地理信息来源 IP、审核状态初始化和路线详情对象返回，从而使前端能够在提交后获得完整反馈。

核心代码展示如下：

```java
@PostMapping
public ApiResponse<TrailDetailVo> createTrail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody CreateTrailRequest request) {
    return ApiResponse.success(
            "路线已提交审核，请耐心等待",
            trailService.createTrail(userDetails.getId(), resolveRequestIp(httpServletRequest), request));
}

@PutMapping("/{id}")
public ApiResponse<TrailDetailVo> updateTrail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody UpdateTrailRequest request) {
    return ApiResponse.success(
            "路线更新已提交审核，请耐心等待",
            trailService.updateTrail(id, userDetails.getId(), resolveRequestIp(httpServletRequest), request));
}
```

实现截图：图5.x 路线发布服务接口调试图（占位）

### 5.3.3 轨迹上传与解析服务

模块描述：轨迹上传与解析服务用于将 GPX 或 KML 文件解析为标准化 GeoJSON、距离、海拔和时长等结构化信息，并进一步落入 `trail_tracks` 表。

该服务既是地图轨迹展示的基础，也是天气定位、景观预测和 AI 路线解释的重要空间数据来源。

核心代码展示如下：

```java
@Service
@RequiredArgsConstructor
public class TrackParseServiceImpl implements TrackParseService {

    @Override
    public TrackParseResult parse(MediaFile mediaFile) {
        String extension = mediaFile.getExtension() == null ? "" : mediaFile.getExtension().trim().toLowerCase();

        try (InputStream inputStream = URI.create(mediaFile.getUrl()).toURL().openStream()) {
            Document document = factory.newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();

            return switch (extension) {
                case "gpx" -> parseGpx(document);
                case "kml" -> parseKml(document);
                default -> throw BusinessException.badRequest("UNSUPPORTED_TRACK_FORMAT", "仅支持 GPX 或 KML 轨迹文件");
            };
        }
    }
}
```

实现截图：图5.x 轨迹上传与解析服务运行结果图（占位）

### 5.3.4 评论服务

模块描述：评论服务负责路线评论加载、评论发布和评论删除等操作，并通过父评论关系和回复对象字段组织树状评论结构。

评论服务既服务于路线详情页，也服务于社区互动链路，是平台内容反馈的重要组成部分。

核心代码展示如下：

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/trails/{trailId}/reviews")
    public ApiResponse<ReviewPageVo> listByTrailId(@PathVariable Long trailId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(reviewService.listByTrailId(trailId, cursor, limit));
    }

    @PostMapping("/reviews")
    public ApiResponse<CreateReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success("评论发布成功", reviewService.createReview(userDetails.getId(), request));
    }
}
```

实现截图：图5.x 评论服务接口调试图（占位）

### 5.3.5 上传服务

模块描述：上传服务主要围绕头像、首页大图、路线封面、路线相册、轨迹文件和评论配图等媒体资源展开。

该服务通过上传凭证申请、文件完成回调和 Redis 上传会话校验，实现了面向 OSS 的统一媒体管理能力。

核心代码展示如下：

```java
@Override
public UploadStsVo createUploadSts(Long userId, CreateUploadStsRequest request) {
    MediaBizType bizType = requireBizType(request.getBizType());
    validateFileForBizType(bizType, request.getFileName(), request.getMimeType());

    String extension = extractExtension(request.getFileName());
    String objectKey = buildObjectKey(bizType, userId, extension);
    cacheIssuedObjectKey(userId, bizType, objectKey);

    AssumeRoleResponse.Credentials credentials = assumeRole(objectKey);
    return UploadStsVo.builder()
            .bucket(ossProperties.getBucketName())
            .objectKey(objectKey)
            .expireAt(System.currentTimeMillis() + ossProperties.getStsDurationSeconds() * 1000)
            .build();
}
```

实现截图：图5.x 上传服务接口调试图（占位）

### 5.3.6 后台管理与配置服务

模块描述：后台管理与配置服务用于承接路线审核、路线治理、用户治理、评论治理、举报处理和系统选项维护等后台业务。

该服务不仅负责完成具体治理动作，还要同步写入后台操作日志，以保证治理过程具备可审计性和可追溯性。

核心代码展示如下：

```java
@Transactional
public void banUser(Long userId, Long adminUserId, AdminBanUserRequest request) {
    User user = requireUser(userId);
    user.setStatus(UserStatus.BANNED.getCode());
    user.setBanReason(request.getReason());
    user.setBannedBy(adminUserId);
    user.setBannedAt(LocalDateTime.now());
    userMapper.updateById(user);

    adminOperationLogService.record(
            adminUserId,
            "user_management",
            "user_ban",
            "user",
            userId,
            user.getUsername(),
            request.getReason(),
            snapshot("status", UserStatus.ACTIVE.getCode()),
            snapshot("status", user.getStatus(), "banReason", user.getBanReason()));
}
```

实现截图：图5.x 后台管理与配置服务接口调试图（占位）

## 5.4 智能推荐与预测服务实现

### 5.4.1 基于大模型的路线推荐实现

模块描述：基于大模型的路线推荐服务是本系统的核心亮点之一。

按照任务书和开题报告既定技术路线，系统在设计上以 DeepSeek 大模型作为语义理解核心，以 Function Calling 机制作为模型能力与业务能力之间的桥梁，将用户的自然语言需求转化为地点、难度、距离、耗时和景观标签等结构化参数。

在工程链路上，系统先根据用户画像和会话上下文生成提示信息，再执行路线候选检索、排序与路线卡片封装，最终通过 SSE 或 WebSocket 将文本回复、路线卡片和追问建议持续返回给前端。

核心代码展示如下，以下代码为核心实现摘录：

```java
public SseEmitter streamChat(Long userId, AiChatStreamRequest request) {
    SseEmitter emitter = new SseEmitter(0L);
    aiTaskExecutor.execute(() -> handleStream(userId, request, emitter));
    return emitter;
}

private void handleStream(Long userId, AiChatStreamRequest request, SseEmitter emitter) {
    AiConversation conversation = resolveConversation(userId, request);
    aiConversationService.appendUserMessage(conversation.getId(), request.getMessage());

    AiRecommendationResult recommendation =
            aiRouteRecommendationService.planRecommendation(userId, request.getMessage());
    if (!recommendation.trailCards().isEmpty()) {
        sendEvent(emitter, "trail_cards", recommendation.trailCards());
    }

    String metadataJson = objectMapper.writeValueAsString(AiMessageMetadata.builder()
            .trailCards(recommendation.trailCards())
            .followUps(recommendation.followUps())
            .build());

    List<DashScopeMessage> promptMessages =
            buildPromptMessages(conversation.getId(), request, recommendation);
    String answer = dashScopeChatService.streamCompletion(
            promptMessages,
            resolveChatModel(recommendation.intent()),
            delta -> sendEventQuietly(emitter, "delta", Map.of("content", delta)));

    if (StringUtils.hasText(answer)) {
        aiConversationService.appendAssistantMessage(conversation.getId(), answer, metadataJson, null);
    }
    if (!recommendation.followUps().isEmpty()) {
        sendEvent(emitter, "follow_ups", recommendation.followUps());
    }
}
```

补充伪代码如下，用于说明论文中保留的 `DeepSeek + Function Calling` 路线参数结构化流程：

```java
AiParsedQuery query = deepSeekFunctionCalling.parse(message, userPreference)
TrailPageRequest request = convertToTrailRequest(query)
List<TrailDetailVo> candidates = trailService.pageTrails(request, userId).getList()
List<TrailDetailVo> ranked = rankByGeoAndPreference(candidates, query, userPreference)
List<AiTrailCardVo> cards = buildTrailCards(ranked)
return buildAiRecommendationResult(query, cards)
```

实现截图：图5.x AI 路线推荐服务运行结果图（占位）

### 5.4.2 天气与景观预测服务实现

模块描述：天气与景观预测服务负责将路线位置解析、天气查询、天文数据获取和景观预测能力组织为统一的环境辅助决策链路。

系统优先使用路线起点坐标作为天气服务的定位来源，在坐标缺失时再回退到文本地点解析；在完成七日天气、当前天气、小时天气、日出日落、月相和光污染信息获取后，再进一步构造随机森林景观预测所需特征。

按照既有论文主线，系统应通过 Python Flask 封装随机森林预测服务，对云海、雾凇和冰挂等景观给出概率判断，并将结果以结构化卡片形式返回到路线详情页。

核心代码展示如下，以下代码为核心实现摘录：

```java
public TrailWeatherResponseVo getTrailWeather(Long trailId) {
    Trail trail = trailMapper.selectActiveById(trailId);
    ResolvedTrailLocation resolvedLocation = resolveLocation(trail);

    List<TrailWeatherForecastDayVo> forecast =
            weatherService.getSevenDayForecast(resolvedLocation.lng(), resolvedLocation.lat());
    CurrentWeatherSnapshot currentWeather =
            landscapeMeteorologyService.getCurrentWeather(resolvedLocation.lng(), resolvedLocation.lat());
    List<HourlyWeatherPoint> hourlyForecast =
            landscapeMeteorologyService.getHourlyForecast(resolvedLocation.lng(), resolvedLocation.lat(), 24);
    SunAstronomyPoint sunInfo =
            landscapeAstronomyService.getSunInfo(resolvedLocation.lng(), resolvedLocation.lat(), LocalDate.now(), null);
    MoonAstronomyPoint moonInfo =
            landscapeAstronomyService.getMoonInfo(resolvedLocation.lng(), resolvedLocation.lat(), LocalDate.now());

    return TrailWeatherResponseVo.builder()
            .locationContext(TrailWeatherLocationContextVo.builder()
                    .lng(resolvedLocation.lng())
                    .lat(resolvedLocation.lat())
                    .resolvedFrom(resolvedLocation.resolvedFrom())
                    .build())
            .forecast(forecast)
            .current(toCurrentVo(currentWeather))
            .hourly(toHourlyVo(hourlyForecast))
            .astro(toAstroVo(sunInfo, moonInfo))
            .build();
}

public LandscapePredictionResponseVo getTrailPrediction(Long trailId, int days) {
    int normalizedDays = Math.min(Math.max(days, 1), 7);
    var context = landscapeContextService.build(trailId, normalizedDays);
    return LandscapePredictionResponseVo.builder()
            .sunriseSunset(sunriseSunsetPredictor.predict(context))
            .milkyWay(milkyWayPredictor.predict(context))
            .cloudSea(cloudSeaPredictor.predict(context))
            .rime(rimePredictor.predict(context))
            .icicle(iciclePredictor.predict(context))
            .build();
}
```

补充伪代码如下，用于说明论文既有材料中保留的 `Python Flask + 随机森林` 服务封装方式：

```python
@app.post("/predict/cloud-sea")
def predict_cloud_sea():
    features = request.json["features"]
    model = load_random_forest("cloud_sea.pkl")
    probability = model.predict_proba([features])[0][1]
    return {
        "landscapeType": "cloud_sea",
        "probability": probability,
        "bestWindow": infer_best_window(features),
        "risks": infer_risk_messages(features),
    }
```

实现截图：图5.x 天气与景观预测服务运行结果图（占位）

图位建议：本章后续可继续补充“路线发布页面效果图”“后台审核页面效果图”“AI 推荐结果图”和“景观预测结果图”等真实截图。
