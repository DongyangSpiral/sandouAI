# AGENTS.md — AI 助手协作指南

本文档帮助 AI 助手（如 opencode、Cursor、Copilot 等）理解项目结构、编码规范和行为约束，以便高效地协助开发。

## 项目概览

```
UAMS 统一认证管理系统 + 文件管理平台
├── backend/      Spring Boot 3.2.5 + Java 17 + MyBatis-Plus + Sa-Token
├── frontend/     Vue 3 + Vite 5 + Element Plus + ECharts
├── sql/          数据库脚本
├── 需求.md       完整需求文档
└── AGENTS.md     本文件
```

## 关键路径

### 后端路径规则

```
com.uams/
├── controller/    → @RestController, @RequestMapping("/api/...")
├── service/       → 业务逻辑
├── mapper/        → extends BaseMapper<T>
├── entity/        → @TableName, @TableId, @TableLogic
├── config/        → 配置类
├── common/        → Result 统一响应
└── mq/            → RabbitMQ 消费者
```

### 前端路径规则

```
src/
├── api/           → Axios 请求封装，每模块一个文件
│   ├── request.js → 统一拦截器（token + 错误处理）
│   ├── system.js  → 系统管理
│   ├── uas.js     → 统一认证
│   └── ...
├── views/         → 页面组件，按模块分目录
│   └── dfs/       → 文件管理页面
│   └── team/      → 团队管理页面
├── router/        → 路由配置（懒加载）
└── layout/        → 后台布局
```

## 编码规范

### 通用

- 缩进：4 空格（Java）/ 2 空格（前端/Vue）
- 命名：camelCase（Java）/ kebab-case（前端文件）
- 每行最长：120 字符
- 不要添加注释，除非必要解释复杂逻辑

### Java 规范

- 实体类使用 `@Data`（Lombok）
- ID 字段统一 `@TableId(type = IdType.AUTO)`
- 逻辑删除字段统一 `@TableLogic private Integer delFlag`
- 时间字段使用 `LocalDateTime`，自动填充使用 `@TableField(fill = FieldFill.INSERT)`
- Service 继承 `ServiceImpl<Mapper, Entity>`，提供业务方法
- Controller 返回统一使用 `Result<?>`
- 异常直接 throw `RuntimeException`，由 `GlobalExceptionHandler` 统一处理

### 前端规范

- `<script setup>` 组合式 API
- API 函数在 `src/api/` 中定义，组件中 import 调用
- 路由使用懒加载 `() => import(...)`
- 国际化：Element Plus 中文
- 表格使用 `el-table` + `el-pagination`

### 数据库规范

- 表前缀：`u_`（认证）/ `sys_`（系统）/ `dfs_`（文件）/ `team_`（团队）
- 字符集：`utf8mb4`
- 引擎：`InnoDB`
- 所有表有 `create_time` 和 `update_time`
- 逻辑删除：`del_flag TINYINT DEFAULT 0`（0=正常, 1=删除）
- 主键：`id BIGINT AUTO_INCREMENT`

## 现有模块清单

### 已完成

| 模块 | 后端状态 | 前端状态 | 说明 |
|------|---------|---------|------|
| 系统管理(用户/角色/菜单) | ✅ Controller + Service | ✅ 页面 | 完整 CRUD |
| 系统扩展(部门/岗位/字典等) | ✅ Controller + Service | ✅ 页面 | 完整 CRUD |
| 系统监控(在线用户/服务/缓存) | ✅ Controller | ✅ 页面 |  |
| OAuth2 授权 | ✅ Controller | ❌ | 只有后端 |
| 代码生成 | ✅ Controller | ✅ 页面 |  |
| 统一认证(UAS 用户/企业/应用) | ✅ Controller + Service | ✅ 页面 | 完整 CRUD |
| 登录(密码/短信/企业) | ✅ Controller + Service | ✅ 页面 | 4 种方式 |
| 文件管理(上传/下载/目录) | ✅ Controller + Service | ❌ | 后端已完成 |
| 分享管理 | ✅ Controller + Service | ❌ | 后端已完成 |
| 团队管理 | ❌ | ❌ | 暂不实现 |
| Sa-Token 权限拦截 | ✅ | — | 已更新拦截路径 |

### 未完成（待开发）

优先级从上到下：

1. **前端文件管理页面** — `src/views/dfs/`（文件列表、目录树、上传）
2. **前端文件分享页面** — `src/views/dfs/share.vue`
3. **前端 API 文件** — `src/api/dfs.js`（封装文件/目录/分享接口）
4. **路由配置** — `src/router/index.js` 添加 dfs 路由
5. **侧边栏菜单** — 更新 `layout/index.vue`

## 常见任务模板

### 新增一个 CRUD 模块

1. **数据库**：在 `sql/init_dfs.sql` 追加建表语句
2. **后端实体**：`entity/Xxx.java`，`@TableName` + `@Data`
3. **后端 Mapper**：`mapper/XxxMapper.java`，`extends BaseMapper<Xxx>`
4. **后端 Service**：`service/XxxService.java`，`extends ServiceImpl<XxxMapper, Xxx>`
5. **后端 Controller**：`controller/XxxController.java`，`@RequestMapping("/api/xxx")`
6. **前端 API**：`src/api/xxx.js`
7. **前端页面**：`src/views/xxx/index.vue`
8. **路由**：`src/router/index.js` 添加路由
9. **菜单**：`sql/init.sql` 追加菜单数据
10. **编译验证**：`mvn clean compile` + 前端 `npm run dev`

### 新增一个 API 接口

- 后端：在对应 Controller 中加方法，使用 `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`
- 前端：在对应 `src/api/*.js` 中加函数
- 需要登录的接口自动由 Sa-Token 拦截器保护

## 启动命令

```bash
# 后端（需 JDK 17）
cd backend
set JAVA_HOME=D:\codeTools\Java\JDK17
mvn spring-boot:run

# 前端
cd frontend
npm run dev

# 编译验证
cd backend && mvn clean compile
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 自然人用户 | 13800000001 | 123456 |

## 约束提醒（务必遵守）

- ❌ 不要随意修改已完成的 Controller/Service 签名（会影响前端调用）
- ❌ 不要在实体类中添加业务逻辑
- ❌ 不要修改 `application-dev.yml` 中的数据库密码和端口
- ✅ 新增配置放在 `application.yml` 中，不要放 `application-dev.yml`
- ✅ 新增依赖先检查 `pom.xml` 是否已存在
