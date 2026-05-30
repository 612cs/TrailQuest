# DOCS 索引

`DOCS` 目录用于集中管理项目工程资料。命名规则优先使用英文 kebab-case 目录与文件名，工程图表源文件与导出文件按目录分层存放。

## 推荐阅读顺序

1. `handoff/`：查看最新交接文档，了解阶段进度与后续任务。
2. `backend/`、`frontend/`：查看前后端设计、规范与实施说明。
3. `database/`：查看建表 SQL、升级脚本与数据库结构资料。
4. `architecture-diagrams/`：查看系统架构、流程、用例、ER 图等工程图表。

## 顶层目录说明

### `backend/`

后端需求、方案、交接与功能设计文档。

### `frontend/`

前端 UI、交互与主题规范文档。

### `database/`

数据库初始化脚本、结构脚本与增量升级 SQL。

### `handoff/`

阶段交接、测试优先级、阶段计划与进度记录。

### `architecture-diagrams/`

工程图表资产，按源文件与导出文件分离：

- `sources/`：Draw.io 源文件。
- `exports/png/`：PNG 导出图。
- `exports/svg/`：SVG 导出图。
- `tasks/`：图表绘制任务记录。

### `archive/`

历史或工具生成的非主线资料：

- `drawio-editor-backups/`：Draw.io 编辑器临时备份文件。

## 维护规则

- 新增开发交接资料放入 `handoff/`，文件名建议包含日期。
- 新增数据库脚本放入 `database/`，升级脚本使用 `mysql-*-upgrade.sql` 命名。
- 新增工程图表源文件放入 `architecture-diagrams/sources/`，导出图按格式放入 `exports/`。
- `.DS_Store` 与 Draw.io `.$*.bkp` 文件不要作为主资料使用；如需保留，放入 `archive/`。
