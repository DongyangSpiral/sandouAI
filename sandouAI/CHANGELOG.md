# UAMS 项目变更日志 (CHANGELOG)

> 记录从 GitHub 下载项目到完成测试运维的全部变更
> 更新日期: 2026-06-12

---

## 一、环境配置修正

### 1.1 数据库配置 (application-dev.yml)

| 配置项 | 原值 | 新值 | 原因 |
|--------|------|------|------|
| MySQL 端口 | `3307` | `3306` | 本机 MySQL 实际监听 3306 |
| MySQL 密码 | `hzy2005` | `2738634776q` | 本机实际密码 |
| 存储类型 | `minio` | `local` → `minio` | 先改用本地存储跑通，后装 MinIO 切回 |
| 存储路径 | `D:\schoolTools\uploads` | `./uploads` → MinIO | 适配 Windows 环境 |

### 1.2 存储配置 (application.yml)

| 配置项 | 原值 | 新值 |
|--------|------|------|
| `dfs.storage.local.path` | `D:\schoolTools\uploads` | `./uploads` |

### 1.3 后端 Bug 修复 (DfsConfig.java)

| 文件 | 修改 | 原因 |
|------|------|------|
| `config/DfsConfig.java` | `@ConfigurationProperties(prefix = "dfs")` → `"dfs.storage"` | 原配置前缀与 yml 结构不匹配，导致启动 NPE |

### 1.4 Maven 仓库修复 (.m2/settings.xml)

| 配置项 | 原值 | 新值 |
|--------|------|------|
| `localRepository` | `d:/repo` (不可访问) | `${user.home}/.m2/repository` |

---

## 二、环境搭建

### 2.1 已安装服务

| 组件 | 版本 | 端口 | 安装方式 |
|------|------|------|----------|
| MySQL 8.0 | 8.0 | 3306 | 已有 |
| JDK | 21.0.8 (兼容 17) | - | 已有 |
| Node.js | v24.16.0 | - | 已有 |
| Maven | 3.9.9 (新装) | - | 手动下载到 `C:\apache-maven-3.9.9` |
| Redis | 3.0.504 (新装) | 6379 | `winget install Redis.Redis` |
| MinIO Server | 2025.07.18 (新装) | 9000/9001 | `winget install MinIO.Server` |
| MinIO Client | 2025.08 (新装) | - | 手动下载 `mc.exe` |

### 2.2 未安装服务

| 组件 | 影响 |
|------|------|
| RabbitMQ | 短信登录功能不可用 |

---

## 三、数据库变更

### 3.1 执行过的 SQL 脚本

| 脚本 | 状态 |
|------|------|
| `sql/init.sql` | 已执行 (9 张基础表 + 初始数据) |
| `sql/init_dfs.sql` | 已执行 (11 张文件/团队表) |
| `sql/init_extend.sql` | **新建并执行** (7 张系统扩展表 + 示例数据) |

### 3.2 新建文件: sql/init_extend.sql

原项目 `init.sql` 缺失以下 7 张表的建表语句，导致所有系统扩展接口 500 错误：

| 表名 | 说明 |
|------|------|
| `sys_dept` | 部门表 |
| `sys_post` | 岗位表 |
| `sys_dict_type` | 字典类型表 |
| `sys_dict_data` | 字典数据表 |
| `sys_config` | 参数配置表 |
| `sys_notice` | 通知公告表 |
| `sys_oper_log` | 操作日志表 |

---

## 四、新增测试文件

```
sandouAI/
└── test/
    ├── uams-api-tests.postman_collection.json   # Postman 接口自动化 (8模块32条)
    ├── run-api-tests.ps1                        # Newman 执行脚本
    ├── performance-test.ps1                     # 性能测试脚本
    ├── 权限矩阵测试用例.md                       # 25 条越权测试用例
    ├── 功能测试报告.md                           # 完整测试报告 (通过率 96.9%)
    ├── 用户操作文档.md                           # 用户操作手册
    └── README.md                                # 项目技术文档
```

## 五、新增 CI/CD

```
.github/workflows/
└── ci.yml    # GitHub Actions 流水线 (Maven编译 + npm构建 + Newman API测试)
```

---

## 六、重要发现与修正

### 6.1 密码错误

| 项目 | 文档/代码写的是 | 实际正确值 |
|------|----------------|-----------|
| 管理员密码 | admin123 / 123456 (多处不一致) | **123456** |
| HANDOVER_DOC | admin123 | 已统一为 123456 |

### 6.2 AI 助手问题

AI 代码已正确接入 DeepSeek API (`api.deepseek.com`)，但当前 API Key `sk-fb0e397a...` **余额不足 (HTTP 402)**，需充值或更换 Key 即可恢复。

### 6.3 性能测试脚本兼容性

当前 PowerShell 沙箱环境不支持 `Invoke-RestMethod` 的 `-Form` 参数，已将 `performance-test.ps1` 中上传测试改用 `curl`。

---

## 七、功能测试结论

| 模块 | 通过率 | 备注 |
|------|--------|------|
| 系统管理 | 100% | 用户/角色/菜单 CRUD |
| 系统扩展 | 100% | 已修复，7 张表 + 示例数据 |
| 统一认证(UAS) | 100% | 自然人/企业/应用管理 |
| 文件管理(DFS) | 100% | MinIO 上传 74.6MB/s |
| 团队管理 | 100% | - |
| 分享管理 | 100% | - |
| Dashboard | 100% | - |
| 系统监控 | 100% | - |
| 越权测试 | 100% | 未登录全部拦截 |
| AI 助手 | 0% | API Key 余额不足 |

**总通过率: 96.9% (31/32)**

### 性能测试

| 指标 | 结果 |
|------|------|
| 列表查询平均 | < 3ms |
| 菜单树查询 | 1.6ms |
| 10MB 文件上传 | 134ms (74.6MB/s) |

---

## 八、残留问题

| ID | 优先级 | 描述 |
|----|--------|------|
| BUG-003 | 中 | Sa-Token 401 异常未统一 JSON 返回 |
| BUG-004 | 中 | DeepSeek API Key 需充值 |
| BUG-005 | 低 | RabbitMQ 未安装，短信功能不可用 |
