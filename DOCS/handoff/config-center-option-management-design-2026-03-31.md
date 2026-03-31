# 配置中心一期设计方案

日期：2026-03-31

## 1. 背景

当前 TrailQuest 前后台已经具备路线、用户、审核、治理等基础能力，但仍有一批“展示项 + 筛选项 + 用户画像项”散落在前端代码中，缺少统一的后台管理入口。

现状主要包括：

- 首页“按活动探索”入口写死在前台组件中
- 搜索页的难度、路线长度、装备类型、耗时类型写死在前台页面中
- 徒步画像的经验等级、常走类型、负重偏好写死在前台表单中
- 后端对部分选项已经存在稳定语义校验，但前台展示文案仍是静态写死

这会带来几个问题：

- 产品和运营无法通过后台调整展示文案、排序、图标、显隐
- 前端多个页面各自维护选项，后续容易出现不一致
- 用户画像、搜索筛选、AI 推荐依赖的底层语义没有统一配置中心承接

因此需要补一版“后台配置中心 P1”，把这一类低频变更、高影响的固定选项收口成统一来源。

## 2. 本期目标

本期不是做一个大而全的 CMS，而是先补齐“固定语义 + 可配置展示层”的最小能力。

一期覆盖 3 类配置：

1. 徒步画像选项
- 徒步经验
- 常走路线类型
- 负重偏好

2. 首页活动入口
- 首页“按活动探索”卡片

3. 搜索页筛选项
- 难度
- 路线长度
- 装备类型
- 耗时类型

## 3. 设计原则

### 3.1 固定 code，开放展示层

对于会影响业务语义的选项，不允许后台自由修改底层 code。

后台可管理字段：

- `label`
- `subLabel`
- `icon`
- `sort`
- `enabled`
- `description`

后台不允许随意修改字段：

- `code`
- 与搜索、推荐、发布、AI 逻辑强耦合的语义值

例如：

- `easy / moderate / hard`
- `light / heavy / both`
- `single_day / multi_day`
- `beginner / intermediate / expert`

### 3.2 前后台、搜索、AI 共享同一份配置来源

配置项需要成为统一基础设施，避免：

- 首页读一套
- 搜索页写一套
- 用户画像写一套
- AI 推荐再硬编码一套

### 3.3 “全部”类项不进入后台配置

例如：

- 全部难度
- 全部长度
- 全部装备
- 全部时长

这些属于前端筛选组件的通用占位项，不应存进后台字典表，否则会让后台配置和业务语义耦合过深。

## 4. 当前代码现状

### 4.1 徒步画像

前台表单选项当前写死在：

- [apps/web/src/components/profile/HikingProfileForm.vue](/Users/sheng/Documents/code/hiking/apps/web/src/components/profile/HikingProfileForm.vue)

但后端已经有稳定枚举：

- [apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingExperienceLevel.java](/Users/sheng/Documents/code/hiking/apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingExperienceLevel.java)
- [apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingTrailStyle.java](/Users/sheng/Documents/code/hiking/apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingTrailStyle.java)
- [apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingPackPreference.java](/Users/sheng/Documents/code/hiking/apps/api/src/main/java/com/sheng/hikingbackend/common/enums/HikingPackPreference.java)

画像保存链路当前由后端枚举校验兜底，不适合改成后台完全自由配置。

### 4.2 首页活动入口

当前纯前台静态写死：

- [apps/web/src/components/home/ActivityGrid.vue](/Users/sheng/Documents/code/hiking/apps/web/src/components/home/ActivityGrid.vue)

这一块最适合优先纳入后台管理。

### 4.3 搜索页筛选项

当前写死在：

- [apps/web/src/views/SearchView.vue](/Users/sheng/Documents/code/hiking/apps/web/src/views/SearchView.vue)

但其中部分值后端已有稳定语义，例如难度、装备、耗时：

- [apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java](/Users/sheng/Documents/code/hiking/apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java)

因此搜索项也应采用“底层 code 固定、展示层可配置”的方案。

## 5. 范围定义

### 5.1 适合后台完全管理的配置

首页活动入口采用“后台完全管理”：

- 名称
- 图标
- 排序
- 是否显示
- 描述
- 点击跳转行为
- 搜索预设词

### 5.2 适合后台半管理的配置

徒步画像和搜索页筛选采用“后台半管理”：

- 可改展示名称
- 可改副文案
- 可改图标
- 可改排序
- 可启用/停用
- 不可改底层 code
- 首版不允许删除 builtin 项

## 6. 数据设计

建议新增两张基础表。

### 6.1 `system_option_groups`

用途：定义配置分组。

建议字段：

- `id BIGINT PRIMARY KEY`
- `group_code VARCHAR(64) NOT NULL UNIQUE`
- `group_name VARCHAR(100) NOT NULL`
- `description VARCHAR(255) NULL`
- `status ENUM('active','inactive') NOT NULL DEFAULT 'active'`
- `created_at DATETIME NOT NULL`
- `updated_at DATETIME NOT NULL`

### 6.2 `system_option_items`

用途：定义分组下的具体配置项。

建议字段：

- `id BIGINT PRIMARY KEY`
- `group_id BIGINT NOT NULL`
- `item_code VARCHAR(64) NOT NULL`
- `item_label VARCHAR(100) NOT NULL`
- `item_sub_label VARCHAR(100) NULL`
- `icon_name VARCHAR(64) NULL`
- `sort_order INT NOT NULL DEFAULT 0`
- `enabled TINYINT(1) NOT NULL DEFAULT 1`
- `is_builtin TINYINT(1) NOT NULL DEFAULT 1`
- `extra_json JSON NULL`
- `created_at DATETIME NOT NULL`
- `updated_at DATETIME NOT NULL`

建议唯一约束：

- `UNIQUE KEY uk_group_item_code (group_id, item_code)`

建议索引：

- `KEY idx_group_sort (group_id, sort_order)`

## 7. 配置分组建议

建议首版种子数据包含以下 `group_code`：

- `hiking_experience_level`
- `hiking_trail_style`
- `hiking_pack_preference`
- `home_activity`
- `search_difficulty`
- `search_distance`
- `search_pack_type`
- `search_duration_type`

## 8. 配置项建议

### 8.1 徒步画像

#### `hiking_experience_level`

- `beginner`
- `intermediate`
- `expert`

#### `hiking_trail_style`

- `city_weekend`
- `long_distance`
- `both`

#### `hiking_pack_preference`

- `light`
- `heavy`
- `both`

### 8.2 搜索页筛选

#### `search_difficulty`

- `easy`
- `moderate`
- `hard`

#### `search_distance`

- `short`
- `medium`
- `long`

#### `search_pack_type`

- `light`
- `heavy`
- `both`

#### `search_duration_type`

- `single_day`
- `multi_day`

### 8.3 首页活动入口

#### `home_activity`

建议首版种子值：

- `hiking`
- `running`
- `cycling`
- `pet_friendly`
- `backpacking`
- `wheelchair_friendly`

这一组允许后续后台新增运营项。

## 9. 后端接口设计

接口分为两层：公开读取接口与后台管理接口。

### 9.1 公开读取接口

用于前台拉取可展示配置。

#### `GET /api/config/options`

查询参数：

- `groups=home_activity,search_difficulty,search_pack_type`

返回建议结构：

```json
{
  "success": true,
  "data": {
    "home_activity": [
      {
        "code": "hiking",
        "label": "徒步",
        "subLabel": null,
        "icon": "Footprints",
        "sort": 1,
        "enabled": true,
        "extra": {
          "query": "徒步"
        }
      }
    ],
    "search_difficulty": [
      {
        "code": "easy",
        "label": "简单",
        "icon": null,
        "sort": 1,
        "enabled": true
      }
    ]
  }
}
```

建议服务端自动过滤：

- 分组状态为 `inactive`
- 选项 `enabled = false`

### 9.2 后台管理接口

建议挂到 `/api/admin/config/**`。

#### `GET /api/admin/config/groups`

返回所有分组列表。

#### `GET /api/admin/config/groups/{groupCode}/items`

返回该分组所有配置项。

#### `POST /api/admin/config/groups/{groupCode}/items`

仅建议对 `home_activity` 开放新增。

#### `PUT /api/admin/config/groups/{groupCode}/items/{id}`

可修改：

- `itemLabel`
- `itemSubLabel`
- `iconName`
- `sortOrder`
- `enabled`
- `extraJson`

#### `POST /api/admin/config/groups/{groupCode}/reorder`

批量更新排序。

### 9.3 后端限制规则

- `is_builtin = true` 时不允许修改 `item_code`
- 搜索筛选和画像分组首版不允许删除 builtin 项
- 仅 `home_activity` 首版允许后台新增
- 对 `icon_name` 做白名单校验
- 对 `group_code` 做固定枚举路由校验，避免写成任意 key-value 仓库

## 10. 后台页面设计

建议放到后台“设置”下新增一级能力：配置中心。

### 10.1 页面结构

#### 配置分组页

展示：

- 分组名称
- 分组编码
- 描述
- 当前状态
- 入口按钮

#### 分组详情页

展示字段：

- 名称
- code
- 图标
- 排序
- 启用状态
- 描述
- 操作

支持能力：

- 编辑展示文案
- 编辑副文案
- 编辑图标
- 启停
- 调整排序
- 首页活动新增项

### 10.2 后台交互规则

- builtin 项禁止编辑 `code`
- 搜索筛选和画像分组不提供删除按钮
- `home_activity` 支持新增，但仍建议限制图标来源
- 修改后即时刷新该分组列表
- 可选补充操作日志，记录管理员修改配置行为

## 11. 前台接入方案

### 11.1 首页活动入口

文件：

- [apps/web/src/components/home/ActivityGrid.vue](/Users/sheng/Documents/code/hiking/apps/web/src/components/home/ActivityGrid.vue)

改造方式：

- 不再使用本地静态数组
- 改为读取 `home_activity`
- 根据配置渲染名称、图标、排序
- 点击后可跳到搜索页，并透传预设 query 或 tag

### 11.2 搜索页筛选项

文件：

- [apps/web/src/views/SearchView.vue](/Users/sheng/Documents/code/hiking/apps/web/src/views/SearchView.vue)

改造方式：

- `filters` 不再在页面中写死
- 改为从配置接口组装
- 每组前面由前端自动补一个 “全部...” 选项
- 若 URL 中存在已下线选项值，前端应自动回退为 `all`

### 11.3 徒步画像表单

文件：

- [apps/web/src/components/profile/HikingProfileForm.vue](/Users/sheng/Documents/code/hiking/apps/web/src/components/profile/HikingProfileForm.vue)

改造方式：

- 展示项从配置接口读取
- 提交时仍提交固定 `code`
- 后端当前画像接口结构不变

## 12. 状态管理与缓存建议

建议新增一个统一配置 store，例如：

- `apps/web/src/stores/useOptionConfigStore.ts`

职责：

- 拉取配置分组
- 做基础缓存
- 暴露按 group 获取配置项的方法

后端缓存建议：

- 配置读取接口可加本地缓存
- 当后台更新配置后主动清缓存

因为这类数据读多写少，很适合缓存化。

## 13. 风险与约束

### 13.1 不能让后台改坏业务语义

如果允许任意改 `code`，会直接影响：

- 路线搜索筛选
- AI 推荐条件解析
- 画像保存校验
- 发布路线时的选项映射

因此首版必须坚持“code 固定”。

### 13.2 图标来源必须受控

如果后台任意填写图标名，前台可能渲染失败或出现未知图标。

建议：

- 后台使用白名单下拉选择
- 白名单和当前图标组件支持集一致

### 13.3 旧链接与禁用配置兼容

若用户打开的 URL 中仍带有旧筛选值，而该配置项已禁用：

- 前端应自动降级到 `all`
- 不应让页面报错或筛选失效

### 13.4 不要把它做成任意 key-value 系统

这期目标是“选项中心”，不是“万能配置中心”。

建议只先覆盖明确场景，避免首版抽象过度。

## 14. 实施顺序

建议按以下顺序推进：

1. 新增数据库表和种子数据
2. 实现后端公开读取接口
3. 实现后台配置管理接口
4. 在 admin 设置模块下新增配置中心
5. 前台接入首页活动入口配置
6. 前台接入搜索页筛选项配置
7. 前台接入徒步画像展示配置
8. 验证 AI 推荐、搜索、画像保存链路不受影响

## 15. 测试建议

### 后端

- 配置读取接口返回顺序正确
- disabled 项不会出现在公开接口中
- builtin 项禁止修改 code
- 搜索筛选和画像分组禁止删除 builtin 项

### 前台

- 首页活动入口可正确展示、排序、跳转
- 搜索页筛选项可正常加载并参与搜索请求
- 徒步画像可正常展示并保存
- 配置项被禁用后，前台能优雅降级

### 后台

- 配置分组列表可正常打开
- 分组详情页可编辑文案、图标、排序、启停
- 首页活动可新增和修改
- 修改后前台刷新可见

## 16. 建议的提交边界

建议拆成 3 次提交：

1. `feat: 增加系统配置分组与选项管理能力`
2. `feat: 支持后台管理首页活动与搜索筛选项`
3. `feat: 前台接入统一配置中心选项`

## 17. 结论

这批配置项是典型的“业务语义稳定，但展示层和运营层有调整需求”的内容，最适合做成一版受控的后台配置中心。

首版最重要的不是“让后台什么都能改”，而是：

- 统一配置来源
- 保持底层 code 稳定
- 让运营和产品可以调整展示层
- 不破坏搜索、画像、推荐等既有业务链路

建议按本方案作为 P1 落地。
