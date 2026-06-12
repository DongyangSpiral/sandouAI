# sandouAI 项目交接文档

## 1. 系统架构与环境要求

### 后端技术栈 (Backend)
- **核心框架**: Spring Boot 3.2.5
- **语言版本**: Java 17
- **数据库**: MySQL 8.x (连接端口: `3307`)
- **持久层**: MyBatis-Plus 3.5.6
- **权限与会话管理**: Sa-Token 1.38.0
- **文件对象存储**: MinIO 8.5.9
- **缓存与中间件**: Redis (连接端口: `6379`), RabbitMQ (配置预留)
- **第三方API**: DeepSeek 大模型API接入 (用于文件智能解析和聊天)
- **文档内容提取**: Apache Tika (用于提取 PDF、Word 纯文本供 AI 阅读)

### 前端技术栈 (Frontend)
- **核心框架**: Vue 3 + Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **网络请求**: Axios

---

## 2. 环境搭建与启动方法

### 2.1 基础服务准备
在启动系统前，必须确保以下中间件正在运行：
1. **MySQL**: 确保监听在 `3307` 端口。用户名：`root`，密码：`hzy2005`。数据库名：`uams`。
2. **Redis**: 默认端口 `6379`，无密码。
3. **MinIO**: API端口 `9000`。
   - `access-key`: `minioadmin`
   - `secret-key`: `minioadmin`
   - 需要确保创建了名为 `uams-files` 的 bucket。

### 2.2 后端启动 (`sandouAI/backend`)
1. 确保安装了 JDK 17 和 Maven。
2. 刷新 Maven 依赖。
3. 检查配置文件：`src/main/resources/application-dev.yml`（必要时可修改数据库密码或更新 DeepSeek API key）。
4. 运行主启动类 `UamsApplication.java` 启动后端服务。

### 2.3 前端启动 (`sandouAI/frontend`)
1. 确保安装了 Node.js (推荐 v18+)。
2. 进入前端目录：`cd sandouAI/frontend`
3. 安装依赖：`npm install`
4. 本地启动服务：`npm run dev`
5. 服务通常会运行在 `http://localhost:5173/`。

---

## 3. 测试账号与数据说明

系统内置了以下核心测试账号用于不同角色的体验：

- **系统管理员账号（最高权限）**
  - **账号**: `admin`
  - **特点**: 作为系统的最高控制者，拥有系统最高权限。只有该账号拥有解散任意团队、强制移除成员、删除团队文件等破坏性“特权”。

- **普通用户账号**
  - **账号**: `testuser1`
  - **特点**: 普通员工视角，可以上传个人文件，接受管理员的组队邀请，并在获批后加入工作团队。

---

## 4. 核心功能及使用指南

本系统是一个集成了网盘存储、团队协作、大模型智能解析的高效办公平台。

1. **分布式文件存储 (DFS)**：
   - 支持多格式（文本、图片、PDF、压缩包等）文件的上传、下载和在线原声预览。
   - 文件底座由 MinIO 统一接管。

2. **团队协作机制**：
   - **双向确认邀请**：管理员发起邀请后，受邀人将在界面收到通知卡片，需手动点击“同意”才会真正入群。
   - **团队文件共享**：团队内的共享文件可供团队成员查阅，且具备极强的容错能力。
   - **安全管控**：系统严格鉴权，杜绝了普通用户越权进行高危操作。

3. **AI 智能助手 (DeepSeek)**：
   - 深度接入了 DeepSeek 语言模型。通过点击团队或个人空间中的智能分析按钮，可发起与文档内容的对话。
   - *温馨提示*：当前版本 AI 仅支持对文本类（如 TXT, PDF, Word）文件进行提炼。系统在前端做了柔性拦截，若对图片或压缩包等无法提炼纯文本的文件发起请求，会给出友好的弹窗提示。

---

## 5. 交接人防坑指南补充

- **Token 与权限问题**：此前系统彻底修复过登录 Token 错乱（`getUserId` 强转失败）引发的全员变 Admin 越权 Bug。目前的 `StpUtil` 已严格基于实际的 `userId`（而不是 username）签发凭证。**后续增加新接口时，获取当前登录用户 ID，请务必直接调用 `com.uams.common.AuthUtil.getUserId()` 即可，不要自行乱解 Token。**
- **文件接口路径规范**：目前的团队文件接口遵循 RESTful 规范（如 `@PostMapping("/upload")`, `@DeleteMapping("/delete")`）。若后续需要扩充新接口，请务必对齐前后端请求的 HTTP Method，以防触发 `RequestMethodNotSupportedException`。
