-- ==================== 文件管理模块 (dfs_) ====================

DROP TABLE IF EXISTS dfs_file_tag;
DROP TABLE IF EXISTS dfs_file_version;
DROP TABLE IF EXISTS dfs_file_folder;
DROP TABLE IF EXISTS dfs_share;
DROP TABLE IF EXISTS dfs_tag;
DROP TABLE IF EXISTS dfs_file;
DROP TABLE IF EXISTS dfs_folder;
DROP TABLE IF EXISTS team_log;
DROP TABLE IF EXISTS team_file;
DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS team_team;

CREATE TABLE dfs_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '目录名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父目录ID，根目录为0',
    owner_id BIGINT NOT NULL COMMENT '所属用户ID(sys_user)',
    type VARCHAR(16) DEFAULT 'personal' COMMENT 'personal个人 / team团队',
    team_id BIGINT DEFAULT NULL COMMENT '关联团队ID',
    sort_order INT DEFAULT 0,
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录表';

CREATE TABLE dfs_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(256) NOT NULL COMMENT '文件名(含扩展名)',
    extension VARCHAR(32) COMMENT '扩展名',
    mime_type VARCHAR(128) COMMENT 'MIME类型',
    size BIGINT COMMENT '文件大小(字节)',
    md5 VARCHAR(64) COMMENT 'MD5哈希，用于秒传去重',
    storage_path VARCHAR(512) NOT NULL COMMENT 'MinIO存储路径',
    bucket VARCHAR(64) DEFAULT 'uams-files' COMMENT 'MinIO桶名',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    owner_id BIGINT COMMENT '所属用户ID',
    del_flag TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_md5 (md5),
    INDEX idx_owner (owner_id),
    INDEX idx_extension (extension)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

CREATE TABLE dfs_file_folder (
    file_id BIGINT NOT NULL,
    folder_id BIGINT NOT NULL,
    PRIMARY KEY (file_id, folder_id),
    INDEX idx_folder (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件-目录关联表';

CREATE TABLE dfs_file_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    version_num INT NOT NULL COMMENT '版本号',
    storage_path VARCHAR(512) NOT NULL COMMENT '该版本存储路径',
    size BIGINT COMMENT '该版本大小',
    md5 VARCHAR(64),
    create_by BIGINT COMMENT '上传人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件版本表';

CREATE TABLE dfs_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL COMMENT '标签名称',
    color VARCHAR(16) DEFAULT '#409eff' COMMENT '标签颜色',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE dfs_file_tag (
    file_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (file_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件-标签关联表';

CREATE TABLE dfs_share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '分享码(UUID)',
    file_id BIGINT COMMENT '分享的文件',
    folder_id BIGINT COMMENT '分享的目录',
    password VARCHAR(256) COMMENT '访问密码(BCrypt加密)',
    expire_time DATETIME COMMENT '过期时间',
    allow_download TINYINT DEFAULT 1 COMMENT '0禁止下载 1允许下载',
    visit_count INT DEFAULT 0 COMMENT '访问次数',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    create_by BIGINT NOT NULL COMMENT '分享人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

-- ==================== 团队管理模块 (team_) ====================

CREATE TABLE team_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT '团队名称',
    avatar VARCHAR(256) COMMENT '团队头像',
    description VARCHAR(512) COMMENT '团队描述',
    owner_id BIGINT NOT NULL COMMENT '创建人(sys_user.id)',
    max_member INT DEFAULT 50 COMMENT '最大成员数',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    del_flag TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

CREATE TABLE team_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.id)',
    role VARCHAR(16) NOT NULL DEFAULT 'member' COMMENT 'creator/admin/member/guest',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user (team_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

CREATE TABLE team_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    folder_id BIGINT COMMENT '目录ID',
    file_id BIGINT COMMENT '文件ID',
    permission VARCHAR(32) DEFAULT 'read' COMMENT 'read/view/download/edit/manage',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队文件权限表';

CREATE TABLE team_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '操作人',
    action VARCHAR(32) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(32) COMMENT '操作对象类型',
    target_id BIGINT COMMENT '操作对象ID',
    detail VARCHAR(512) COMMENT '操作详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_team_log (team_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队操作日志表';

-- ==================== 菜单数据 ====================

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '文件管理', 0, '/dfs', '', 'FolderOpened', 'M', '', 3
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='文件管理' AND menu_type='M');

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '我的文件', (SELECT id FROM sys_menu WHERE menu_name='文件管理' AND menu_type='M'), '/dfs/index', 'dfs/index', 'Document', 'C', 'dfs:file:list', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='我的文件');

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '我的分享', (SELECT id FROM sys_menu WHERE menu_name='文件管理' AND menu_type='M'), '/dfs/share', 'dfs/share', 'Share', 'C', 'dfs:share:list', 2
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='我的分享');

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '文件搜索', (SELECT id FROM sys_menu WHERE menu_name='文件管理' AND menu_type='M'), '/dfs/search', 'dfs/search', 'Search', 'C', 'dfs:search', 3
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='文件搜索');

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '团队管理', 0, '/team', '', 'Users', 'M', '', 4
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='团队管理' AND menu_type='M');

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order)
SELECT '我的团队', (SELECT id FROM sys_menu WHERE menu_name='团队管理' AND menu_type='M'), '/team/index', 'team/index', 'Avatar', 'C', 'team:list', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='我的团队');

-- 给超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name IN ('文件管理','我的文件','我的分享','文件搜索','团队管理','我的团队');
