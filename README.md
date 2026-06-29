# Sandou Drive（UAMS）

Sandou Drive 是一个前后端分离的企业文件协作平台，基于 UAMS 统一认证管理系统扩展，提供用户认证、系统管理、文件管理、团队协作、文件分享、在线预览和 AI 文档助手等能力。

## 功能概览

| 模块 | 功能 |
| --- | --- |
| 统一认证 | 管理员登录、自然人用户、企业用户、应用和 OAuth2 授权管理 |
| 系统管理 | 用户、角色、菜单、部门、岗位、字典、公告、监控与代码生成 |
| 文件管理 | 文件上传、下载、预览、搜索、排序、目录树、新建文件夹、删除和分享 |
| 团队协作 | 团队创建、成员邀请、成员角色、团队资源和权限管理 |
| 文件分享 | 访问链接、访问密码、过期时间、是否允许下载 |
| AI 助手 | 文本类文档摘要和问答，需要自行配置兼容 OpenAI 格式的大模型服务 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite 5、Vue Router、Pinia、Element Plus、ECharts、Axios |
| 后端 | Java 17、Spring Boot 3.2.5、MyBatis-Plus、Sa-Token、Knife4j |
| 数据与中间件 | MySQL 8、Redis、RabbitMQ、MinIO |
| 文件存储 | 默认使用 MinIO，也支持后端配置中的本地磁盘存储 |

## 目录结构

```text
.
├── README.md
├── sandouAI/
│   ├── backend/                 Spring Boot 后端
│   ├── frontend/                Vue 前端
│   ├── sql/                     数据库初始化脚本
│   ├── docker-compose.yml       MySQL、Redis、RabbitMQ、MinIO
│   ├── start.bat                Windows 启动脚本
│   └── start-backend.bat        仅启动后端
└── test/                        接口与性能测试脚本
```

## 从 Zip 本地运行

1. 解压项目压缩包。
2. 进入解压后的项目根目录，再进入 `sandouAI` 子目录。
3. 安装或准备下面的软件：
   - JDK 17
   - Maven 3.9+
   - Node.js 18+
   - Docker Desktop（推荐，用于启动 MySQL、Redis、RabbitMQ、MinIO）
4. 启动基础服务：

```powershell
cd sandouAI
docker compose up -d
```

首次启动 MySQL 容器时，会自动执行 `sql/` 目录下的初始化脚本，创建 `uams` 数据库和基础表数据。

5. 启动应用：

```powershell
.\start.bat
```

启动后访问：

| 服务 | 地址 |
| --- | --- |
| 管理门户 | http://localhost:5173 |
| 文件空间 | http://localhost:5174 |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/doc.html |
| MinIO 控制台 | http://localhost:9001 |
| RabbitMQ 控制台 | http://localhost:15672 |

默认管理员账号：`admin`

默认管理员密码：`123456`

## 手动启动

如果不使用 `start.bat`，可以分别启动后端和前端。

后端：

```powershell
cd sandouAI/backend
mvn spring-boot:run
```

前端管理门户：

```powershell
cd sandouAI/frontend
npm install
npm run dev:portal
```

前端文件空间：

```powershell
cd sandouAI/frontend
npm run dev:drive
```

## 配置说明

后端开发配置位于 `sandouAI/backend/src/main/resources/application-dev.yml`。默认值适配 `docker compose up -d` 启动的本地环境，也可以通过环境变量覆盖。

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3307/uams?...` | MySQL 连接地址 |
| `MYSQL_USERNAME` | `root` | MySQL 用户名 |
| `MYSQL_PASSWORD` | `root` | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ 地址 |
| `RABBITMQ_USERNAME` | `guest` | RabbitMQ 用户名 |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ 密码 |
| `MINIO_ENDPOINT` | `http://127.0.0.1:9000` | MinIO API 地址 |
| `MINIO_ACCESS_KEY` | `minioadmin` | MinIO Access Key |
| `MINIO_SECRET_KEY` | `minioadmin` | MinIO Secret Key |
| `MINIO_BUCKET` | `uams-files` | 文件桶名称 |
| `DEEPSEEK_API_KEY` | 空 | AI 助手的大模型 API Key |
| `DEEPSEEK_ENDPOINT` | `https://api.deepseek.com/chat/completions` | 兼容 OpenAI 格式的对话接口 |

Windows PowerShell 示例：

```powershell
$env:MYSQL_PASSWORD="root"
$env:DEEPSEEK_API_KEY="你的 API Key"
cd sandouAI/backend
mvn spring-boot:run
```

不要把真实数据库密码、API Key 或生产配置提交到 Git。

## 前端入口配置

前端提供两个入口：

| 命令 | 入口 | 地址 |
| --- | --- | --- |
| `npm run dev:portal` | 管理门户 | http://localhost:5173 |
| `npm run dev:drive` | 文件空间 | http://localhost:5174 |
| `npm run build:portal` | 构建管理门户 | `dist/` |
| `npm run build:drive` | 构建文件空间 | `dist-drive/` |

入口配置文件：

- `sandouAI/frontend/.env.portal`
- `sandouAI/frontend/.env.drive`

默认情况下，管理门户和文件空间会互相跳转到对应端口。

## 打包与验证

前端构建：

```powershell
cd sandouAI/frontend
npm run build:portal
npm run build:drive
```

后端打包：

```powershell
cd sandouAI/backend
mvn -DskipTests package
```

源码交付 Zip 不包含 `node_modules/`、`target/`、`dist/`、`dist-drive/`、日志、临时文件、数据库运行数据和 `.git/`。拿到 Zip 后按“从 Zip 本地运行”重新安装依赖并启动即可。

## 常见问题

| 问题 | 处理方式 |
| --- | --- |
| 后端提示 Java 版本不兼容 | 确认 `java -version` 是 JDK 17 或更高版本 |
| 前端依赖安装失败 | 确认 Node.js 18+ 可用，并重新执行 `npm install` |
| 数据库连接失败 | 确认 `docker compose ps` 中 MySQL 正常运行，端口为 `3307` |
| 文件上传失败 | 确认 MinIO 正常运行，并存在 `uams-files` bucket |
| AI 助手不可用 | 配置 `DEEPSEEK_API_KEY` 后重启后端 |
