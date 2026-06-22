# Sandou Drive（UAMS）

Sandou Drive 是一个前后端分离的企业文件协作平台。项目基于既有的统一认证管理系统（UAMS）扩展，提供文件管理、团队协作、分享、预览、AI 文档辅助以及系统管理能力。

## 项目目标

- 让个人文件、团队资料和访问权限在一个工作空间内完成管理。
- 复用 UAMS 的登录、用户、角色和菜单体系，避免维护两套身份系统。
- 使用对象存储保存文件，并为文件分享、团队协作和后续 AI 文档分析提供基础能力。

## 核心能力

| 模块 | 功能 |
| --- | --- |
| 工作台 | 文件空间入口、系统数据概览、常用工作区、活动与趋势图。 |
| 文件管理 | 文件上传、下载、预览、搜索、排序、文件夹树、新建文件夹、删除和分享。 |
| 团队协作 | 创建团队、成员邀请、成员角色、团队资料和操作权限。 |
| 文件分享 | 为文件创建访问链接，可设置访问密码、失效时间和下载权限。 |
| AI 助手 | 对文本类文档生成摘要并进行内容问答；需配置可用的大模型服务。 |
| 统一认证 | 管理员、自然人、企业用户及应用管理；支持多种登录方式。 |
| 系统控制台 | 用户、角色、菜单、部门、岗位、字典、公告、监控与开发工具。 |

## 技术架构

```text
浏览器（Vue 3 + Element Plus + ECharts）
                │ /api
Spring Boot 3 + MyBatis-Plus + Sa-Token
       │              │               │
    MySQL          Redis         MinIO / 本地文件存储
```

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite 5、Vue Router、Pinia、Element Plus、ECharts、Axios |
| 后端 | Java 17、Spring Boot 3.2.5、MyBatis-Plus、Sa-Token |
| 数据 | MySQL 8、Redis |
| 文件存储 | MinIO（推荐）或本地磁盘存储 |
| 可选服务 | RabbitMQ（短信登录）、兼容 OpenAI 接口的大模型服务（AI 助手） |

## 目录说明

```text
.
├─ sandouAI/
│  ├─ backend/                 Spring Boot 服务
│  ├─ frontend/                Vue 前端
│  │  └─ src/views/
│  │     ├─ dashboard/         工作台
│  │     ├─ dfs/               文件管理
│  │     ├─ team/              团队协作
│  │     ├─ system/            系统管理
│  │     └─ uas/               统一认证
│  ├─ sql/                     数据库初始化脚本
│  ├─ docker-compose.yml       MinIO、Redis、RabbitMQ 容器配置
│  ├─ start.bat                Windows 一键启动脚本
│  └─ AGENTS.md                开发协作约束
└─ test/                       接口与性能测试脚本
```

## 快速启动（Windows）

### 1. 前置条件

- JDK 17 或更高版本
- Node.js 18 或更高版本
- Maven 3.9 或更高版本
- MySQL 8
- Redis 与 MinIO（文件上传功能需要）

### 2. 初始化数据库

在 `sandouAI/sql/` 中依次执行基础系统、文件团队与系统扩展脚本。系统扩展脚本不可省略，否则部门、岗位、字典、公告等模块会缺少数据表。

```powershell
mysql -u root -p < sandouAI/sql/init.sql
mysql -u root -p < sandouAI/sql/init_dfs.sql
mysql -u root -p < sandouAI/sql/init_extend.sql
```

### 3. 配置服务

检查 `sandouAI/backend/src/main/resources/application-dev.yml` 中的数据库连接信息；不要将真实密码提交到版本库。

如使用 Docker 启动基础设施：

```powershell
cd sandouAI
docker-compose up -d
```

### 4. 启动应用

推荐直接在 `sandouAI/` 下双击或运行：

```powershell
.\start.bat
```

该脚本会检查 Java、Maven、Node.js，并启动 MySQL、后端和前端。开发时也可分别运行：

```powershell
cd sandouAI/backend
mvn spring-boot:run

cd ../frontend
npm install
npm run dev
```

访问地址：

| 服务 | 地址 |
| --- | --- |
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/doc.html |
| MinIO 控制台 | http://localhost:9001 |

默认管理员账号为 `admin`，密码为 `123456`。

## 文件与团队工作流

1. 从工作台进入“我的文件”，可上传资料或创建文件夹。
2. 在文件行的更多菜单中预览、下载、分享或发起 AI 分析。
3. 进入“团队空间”创建团队、邀请成员并管理成员角色。
4. 分享文件时可选择密码、有效期和是否允许下载。
5. 文件页与团队页均提供“返回概览”和另一工作区的导航入口。

## 存储与安全说明

- 文件接口及目录接口受登录状态保护，客户端通过 `Authorization` 请求头携带 Sa-Token。
- 默认文件大小限制为 500 MB；配置位于 `application.yml` 的 `dfs.storage.local.max-size`。
- 推荐使用 MinIO 的 `uams-files` 存储桶；本地存储模式的目录为 `./uploads`。
- 文件删除采用逻辑删除，数据恢复需由管理员处理。
- AI 功能依赖有效的大模型 API 配置；未配置或额度不足时，文件管理与团队功能不受影响。

## 前端设计规范

当前界面以文件管理页为体验基准：

- 深色侧栏配合明确的工作区入口；文件与团队为一级导航。
- 文件、团队页面使用一致的顶部导航，提供返回概览和跨工作区跳转。
- 以留白、低饱和背景、渐变功能区和轻量卡片表达信息层级。
- 管理控制台继续复用 Element Plus，统一接入全局主题变量。

## 开发与验证

前端构建：

```powershell
cd sandouAI/frontend
npm run build
```

后端编译：

```powershell
cd sandouAI/backend
mvn clean compile
```

提交代码前，优先验证登录、文件列表、文件夹创建、上传、团队创建和分享链路。详细的代码协作规范见 [sandouAI/AGENTS.md](sandouAI/AGENTS.md)。
