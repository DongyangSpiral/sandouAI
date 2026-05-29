CREATE DATABASE IF NOT EXISTS uams DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE uams;

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS u_login_log;
DROP TABLE IF EXISTS u_app;
DROP TABLE IF EXISTS u_corp_user;
DROP TABLE IF EXISTS u_user;

CREATE TABLE u_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(256) COMMENT 'BCrypt',
    real_name VARCHAR(64),
    id_card_type VARCHAR(32) COMMENT 'ID_CARD/PASSPORT',
    id_card_no VARCHAR(128) COMMENT '加密存储',
    nickname VARCHAR(64),
    gender TINYINT DEFAULT 0 COMMENT '0未知 1男 2女',
    cert_level TINYINT DEFAULT 0 COMMENT '认证等级 0未认证 1实名 2高级',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    last_login_time DATETIME,
    last_login_ip VARCHAR(64),
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='自然人用户表';

CREATE TABLE u_corp_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corp_name VARCHAR(128) NOT NULL COMMENT '企业名称',
    credit_code VARCHAR(64) NOT NULL UNIQUE COMMENT '统一社会信用代码',
    phone VARCHAR(32) NOT NULL COMMENT '手机号',
    password VARCHAR(256) NOT NULL COMMENT 'BCrypt',
    contact_name VARCHAR(64) COMMENT '联系人',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    last_login_time DATETIME,
    last_login_ip VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='企业用户表';

CREATE TABLE u_app (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_name VARCHAR(128) NOT NULL,
    app_key VARCHAR(64) NOT NULL UNIQUE,
    app_secret VARCHAR(256) NOT NULL,
    description VARCHAR(512),
    redirect_uri VARCHAR(512) COMMENT 'OAuth2回调地址',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='第三方应用表';

CREATE TABLE u_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    login_type VARCHAR(32) COMMENT 'password/sms/enterprise',
    username VARCHAR(64) COMMENT '登录用户名/手机号',
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    status TINYINT DEFAULT 1 COMMENT '0失败 1成功',
    error_msg VARCHAR(512),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_login_type (login_type)
) ENGINE=InnoDB COMMENT='登录日志表';

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    nickname VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(32),
    avatar VARCHAR(256),
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    login_ip VARCHAR(64),
    login_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统管理员表';

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(64) NOT NULL,
    role_key VARCHAR(64) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(256),
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    icon VARCHAR(64) COMMENT '图标',
    menu_type CHAR(1) DEFAULT 'M' COMMENT 'M目录 C菜单 B按钮',
    perms VARCHAR(256) COMMENT '权限标识',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='菜单表';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

INSERT INTO sys_user (username, password, nickname, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'admin@uams.com', '13800000001', 1);

INSERT INTO sys_role (role_name, role_key, description, status, sort_order) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, 1),
('系统管理员', 'SYSTEM_ADMIN', '系统管理员', 1, 2),
('审计员', 'AUDITOR', '可查看操作日志', 1, 3),
('普通用户', 'USER', '普通用户权限', 1, 4);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, menu_type, perms, sort_order) VALUES
('系统管理', 0, '/system', '', 'Setting', 'M', '', 1),
('用户管理', 1, '/system/user', 'system/user/index', 'User', 'C', 'system:user:list', 1),
('角色管理', 1, '/system/role', 'system/role/index', 'UserFilled', 'C', 'system:role:list', 2),
('菜单管理', 1, '/system/menu', 'system/menu/index', 'Menu', 'C', 'system:menu:list', 3),
('统一认证', 0, '/uas', '', 'Key', 'M', '', 2),
('自然人用户', 5, '/uas/user', 'uas/user/index', 'Avatar', 'C', 'uas:user:list', 1),
('企业用户', 5, '/uas/corp', 'uas/corp/index', 'OfficeBuilding', 'C', 'uas:corp:list', 2),
('应用管理', 5, '/uas/app', 'uas/app/index', 'Grid', 'C', 'uas:app:list', 3),
('登录日志', 5, '/uas/log', 'uas/log/index', 'Document', 'C', 'uas:log:list', 4);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1),(1, 2),(1, 3),(1, 4),(1, 5),(1, 6),(1, 7),(1, 8),(1, 9);

INSERT INTO u_user (real_name, phone, password, nickname, cert_level, status) VALUES
('张三', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张先生', 2, 1),
('李四', '13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李女士', 1, 1);

INSERT INTO u_corp_user (corp_name, credit_code, phone, password, contact_name, status) VALUES
('上海科技有限公司', '91310000XXXXXXXX01', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 1),
('北京创新技术有限公司', '91110000XXXXXXXX0X', '13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 1);

INSERT INTO u_app (app_name, app_key, app_secret, description, redirect_uri, status) VALUES
('UAMS管理平台', 'uams-admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'UAMS统一认证管理系统', 'http://localhost:5173/callback', 1);
