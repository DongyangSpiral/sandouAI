# UAMS 统一认证管理系统

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.6, Sa-Token 1.38.0 |
| 数据库 | MySQL 8.0, Redis |
| 消息队列 | RabbitMQ |
| 文件存储 | MinIO (可切换本地存储) |
| 前端 | Vue 3, Vite 5, Element Plus, ECharts |

## 环境要求

| 组件 | 版本 | 端口 |
|------|------|------|
| JDK | 17+ | - |
| Maven | 3.9+ | - |
| Node.js | 16+ | - |
| MySQL | 8.0 | 3306 |
| Redis | 3.0+ | 6379 |
| MinIO | latest | 9000/9001 |
| RabbitMQ | 3.x | 5672/15672 |

## 快速启动

### 1. 启动中间件
```powershell
# Redis: 手动启动
Start-Process "C:\Program Files\Redis\redis-server.exe" -ArgumentList "C:\Program Files\Redis\redis.windows.conf" -WindowStyle Hidden

# MinIO: 手动启动
minio server C:\minio_data --console-address :9001
```

### 2. 初始化数据库
```powershell
cd sandouAI\sql
Get-Content init.sql | mysql -u root -p uams
Get-Content init_dfs.sql | mysql -u root -p uams
```

### 3. 配置
修改 `backend/src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    password: 你的数据库密码
```

### 4. 启动后端
```powershell
cd sandouAI\backend
mvn spring-boot:run
# 访问: http://localhost:8080
# API文档: http://localhost:8080/doc.html
```

### 5. 启动前端
```powershell
cd sandouAI\frontend
npm install
npm run dev
# 访问: http://localhost:5173
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 自然人用户 | 13800000001 | 123456 |

## 项目结构
```
sandouAI/
├── backend/          Spring Boot 后端
│   ├── src/main/java/com/uams/
│   │   ├── controller/   API接口
│   │   ├── service/      业务逻辑
│   │   ├── mapper/       数据访问
│   │   ├── entity/       数据实体
│   │   ├── config/       配置类
│   │   └── common/       通用工具
│   └── src/main/resources/
│       ├── application.yml
│       └── application-dev.yml
├── frontend/         Vue 3 前端
│   └── src/
│       ├── api/           API封装
│       ├── views/         页面组件
│       ├── router/        路由配置
│       └── styles/        样式文件
├── sql/              数据库脚本
└── test/             测试脚本
```

## 功能模块

| 模块 | 说明 | 状态 |
|------|------|------|
| 系统管理 | 用户/角色/菜单 CRUD | 完成 |
| 系统扩展 | 部门/岗位/字典/参数/公告/日志 | 完成 |
| 统一认证(UAS) | 自然人/企业/应用管理 + 4种登录方式 | 完成 |
| 文件管理(DFS) | 上传/下载/目录/版本/标签 | 完成 |
| 分享管理 | 创建分享/密码验证/过期 | 完成 |
| 团队管理 | 创建团队/成员/文件权限 | 完成 |
| 系统监控 | 在线用户/服务状态/缓存 | 完成 |
| AI助手 | 对话聊天 | 部分(Mock数据) |

## API 文档

启动后端后访问: `http://localhost:8080/doc.html` (Knife4j)

## 常见问题

### Q: 后端启动报 RabbitMQ 连接失败?
A: RabbitMQ 未安装不影响核心功能，短信登录不可用，其他功能正常。

### Q: 文件上传失败?
A: 检查 MinIO 是否启动，确认 bucket `uams-files` 已创建。

### Q: 前端登录后跳转失败?
A: 检查前端 Vite 代理配置，确保 `/api` 请求转发到 `http://localhost:8080`。
