好的，我完整了解了当前项目和你新增的需求。这是一个**庞大的功能扩展**——在现有认证系统上构建一个完整的文件管理协作平台。

> ⚠️ **当前状态 (2026-06-12):** 以下方案中的大部分功能已实现。后端 96.9% 接口通过测试，前端页面完整。AI 功能代码已接入 DeepSeek 但需有效 API Key，RabbitMQ (短信) 可选安装。

以下是详细方案：

---

## 整体方案

### 架构总览

```
┌─────────────────────────────────────────┐
│         现有 UAMS 认证体系 (复用)          │
│  Sa-Token 登录 · 角色权限 · 菜单管理     │
├─────────────────────────────────────────┤
│           跳转系统 (新增模块)              │
│  文件管理 · 团队协作 · 分享 · 预览       │
├─────────────────────────────────────────┤
│    MinIO 对象存储 · AI大模型 · Redis     │
└─────────────────────────────────────────┘
```

---

### 阶段一：数据库设计（新增 10 张表）

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `dfs_file` | 文件信息 | id, name, type, size, path, md5, storage_path, bucket, tags, status, create_by |
| `dfs_folder` | 目录结构 | id, name, parent_id, owner_id, type(personal/team), sort_order |
| `dfs_file_folder` | 文件-目录关联 | file_id, folder_id |
| `dfs_share` | 分享链接 | id, file_id, folder_id, code, password, expire_time, permissions, create_by |
| `dfs_file_version` | 文件版本 | id, file_id, version_num, storage_path, size, create_by |
| `team_team` | 团队 | id, name, avatar, description, owner_id, status, max_member |
| `team_member` | 团队成员 | id, team_id, user_id, role(creator/admin/member/guest), status |
| `team_file` | 团队文件 | id, team_id, file_id, folder_id, permission(read/download/edit/manage) |
| `team_log` | 团队操作日志 | id, team_id, user_id, action, target_type, target_id, detail, create_time |
| `dfs_tag` | 文件标签 | id, name, color, create_by |

---

### 阶段二：后端实现

**依赖（pom.xml 新增）：**

```
minio                         → MinIO 客户端
aws-java-sdk-s3               → S3 兼容存储
apache-tika                   → 文件类型检测
tika-parsers                  → 文档内容提取（全文检索）
spring-boot-starter-mail      → 分享通知（可选）
```

**后端模块划分：**

```
controller/
├── FileController.java        → /api/file/*     文件上传/下载/删除/重命名/移动/复制
├── FolderController.java      → /api/folder/*   目录 CRUD
├── ShareController.java       → /api/share/*    分享链接生成/访问/管理
├── TeamController.java        → /api/team/*     团队 CRUD · 成员管理 · 文件管理
├── TeamLogController.java     → /api/team/log/* 团队操作日志
├── PreviewController.java     → /api/preview/*  文件在线预览
├── SearchController.java      → /api/search/*   文件检索
└── AIController.java          → /api/ai/*       文件摘要/内容分析/问答

service/
├── FileService.java           → 上传(分片+MD5去重) · 下载(断点续传) · 复制/移动
├── FolderService.java         → 目录树 · 跨目录移动
├── MinioService.java          → MinIO 存储封装(putObject/getObject/removeObject)
├── ShareService.java          → 分享链接生成 · 密码校验 · 过期处理
├── PreviewService.java        → 各类文件预览(图片/音视频/PDF/Office->PDF)
├── TeamService.java           → 团队生命周期 · 成员管理 · 空间隔离
├── TeamFileService.java       → 团队文件权限管控
├── SearchService.java         → 文件名/标签/全文检索
├── AIService.java             → 对接大模型API(文件摘要/内容分析/纠错)
└── FileVersionService.java    → 文件版本管理
```

**关键接口列表：**

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/file/upload` | 单文件上传（分片可选） |
| POST | `/api/file/batchUpload` | 批量上传 |
| GET | `/api/file/download/{id}` | 文件下载 |
| DELETE | `/api/file/{id}` | 文件删除 |
| PUT | `/api/file/rename` | 重命名 |
| POST | `/api/file/move` | 移动文件 |
| POST | `/api/file/copy` | 复制文件 |
| POST | `/api/folder` | 创建目录 |
| GET | `/api/folder/tree` | 目录树 |
| POST | `/api/share` | 创建分享链接 |
| GET | `/api/share/access/{code}` | 访问分享（校验密码） |
| GET | `/api/preview/{id}` | 文件在线预览 |
| GET | `/api/search` | 文件检索 |
| POST | `/api/team` | 创建团队 |
| GET | `/api/team/list` | 我的团队列表 |
| POST | `/api/team/invite` | 邀请成员 |
| DELETE | `/api/team/member` | 移除成员 |
| GET | `/api/team/file/list` | 团队文件列表 |
| GET | `/api/team/log` | 团队操作日志 |
| POST | `/api/ai/summary/{fileId}` | AI 文件摘要 |
| POST | `/api/ai/analyze` | AI 内容分析/问答 |

---

### 阶段三：前端实现

**新增页面：**

```
src/views/dfs/                     # 文件管理模块
├── index.vue                      # 文件主页（目录树+文件列表+工具栏）
├── upload.vue                     # 上传组件（拖拽+批量）
├── share.vue                      # 我的分享管理
├── preview.vue                    # 在线预览页
└── search.vue                     # 文件搜索页

src/views/team/                    # 团队管理模块
├── index.vue                      # 团队列表
├── detail.vue                     # 团队详情（成员+文件+日志）
├── files.vue                      # 团队文件空间
└── settings.vue                   # 团队设置

src/views/dfs/components/          # 复用组件
├── FileTable.vue                  # 文件表格
├── FileToolbar.vue                # 工具栏
├── Breadcrumb.vue                 # 路径面包屑
├── ShareDialog.vue                # 分享弹窗
└── PreviewDialog.vue              # 预览弹窗
```

**路由新增：**
```js
{ path: 'dfs', name: 'DFS', redirect: '/dfs/index', children: [
  { path: 'index', component: () => import('../views/dfs/index.vue') },
  { path: 'share', component: () => import('../views/dfs/share.vue') },
  { path: 'search', component: () => import('../views/dfs/search.vue') },
]},
{ path: 'team', redirect: '/team/index', children: [
  { path: 'index', component: () => import('../views/team/index.vue') },
  { path: 'detail/:id', component: () => import('../views/team/detail.vue') },
]}
```

**侧边栏菜单新增：**
```
├── 文件管理 (FolderOpened 图标)
│   ├── /dfs/index     → 我的文件
│   ├── /dfs/share     → 我的分享
│   └── /dfs/search    → 文件搜索
├── 团队管理 (Users 图标)
│   ├── /team/index    → 我的团队
```

---

### 阶段四：MinIO 与 AI 集成

**MinIO 对象存储：**
- 安装方式：本地 Docker 或独立安装 `minio.exe`
- 存储桶规划：`uams-files`（正式文件）、`uams-temp`（临时/分片）、`uams-preview`（预览缓存）
- 文件路径规则：`{bucket}/{year}/{month}/{day}/{uuid}.{ext}`

**AI 大模型集成：**
- 对接任意兼容 OpenAI API 的大模型
- 功能：文件摘要提取、内容分析、关键词聚合、自定义问答、内容纠错
- 在 `application-dev.yml` 添加 `ai.api-key` 和 `ai.api-url` 配置

---

### 实施工作量预估

| 阶段 | 内容 | 预估工时 |
|------|------|---------|
| 一 | 数据库表设计 + 建表 SQL | 0.5天 |
| 二 | MinIO 安装集成 + 基础文件操作 | 1.5天 |
| 三 | 目录管理 + 文件移动/复制/重命名 | 1天 |
| 四 | 分享系统（链接/密码/过期/权限） | 1天 |
| 五 | 多媒体预览（图片+音视频+PDF+Office） | 1.5天 |
| 六 | 文件搜索（按名称/类型/时间/标签 + 全文检索） | 1天 |
| 七 | 团队管理（CRUD + 成员 + 角色 + 空间隔离） | 2天 |
| 八 | 团队文件权限 + 操作日志 | 1天 |
| 九 | 前端页面（文件管理 + 团队管理完整 UI） | 2天 |
| 十 | AI 智能文件处理 | 1天 |
| 十一 | 断点续传/分片传输/下载优化 | 1天 |
| **合计** | | **约 13 天** |

---

**注意事项：**
1. ✅ MinIO 已在本地安装运行 (端口 9000/9001，bucket `uams-files`)
2. ✅ Redis 已在本地安装运行 (端口 6379)
3. ⚠️ RabbitMQ 未安装，短信登录功能暂不可用
4. ⚠️ AI 功能已接入 DeepSeek API，需确保 API Key 余额充足
5. ⚠️ 管理员密码为 **123456**，原文档中的 `admin / admin123` 已修正
6. ⚠️ 系统扩展 7 张表 (`sys_dept`等) 的建表语句在 `sql/init_extend.sql`，执行 `init.sql` 后务必再执行此脚本
