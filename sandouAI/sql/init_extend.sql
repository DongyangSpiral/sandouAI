-- UAMS 系统扩展模块建表语句
-- 此脚本创建 SystemExtendController 依赖的 7 张数据表

USE uams;

CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(512) DEFAULT '' COMMENT '祖级列表',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(32) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(32) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(64) DEFAULT '' COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '0停用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='部门表';

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(64) NOT NULL COMMENT '岗位名称',
    post_sort INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 1 COMMENT '0停用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='岗位表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(64) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(64) NOT NULL COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '0停用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_sort INT DEFAULT 0 COMMENT '字典排序',
    dict_label VARCHAR(128) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(128) NOT NULL COMMENT '字典键值',
    dict_type VARCHAR(64) NOT NULL COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '0停用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(128) NOT NULL COMMENT '参数名称',
    config_key VARCHAR(128) NOT NULL COMMENT '参数键名',
    config_value VARCHAR(512) DEFAULT '' COMMENT '参数键值',
    config_type VARCHAR(10) DEFAULT 'N' COMMENT '系统内置(Y/N)',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='参数配置表';

CREATE TABLE IF NOT EXISTS sys_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_title VARCHAR(256) NOT NULL COMMENT '公告标题',
    notice_type VARCHAR(10) DEFAULT '1' COMMENT '公告类型(1通知 2公告)',
    notice_content TEXT COMMENT '公告内容',
    status TINYINT DEFAULT 1 COMMENT '0停用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(256) DEFAULT '' COMMENT '模块标题',
    oper_name VARCHAR(64) DEFAULT '' COMMENT '操作人员',
    oper_ip VARCHAR(64) DEFAULT '' COMMENT '操作IP',
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='操作日志表';

-- 插入示例数据
INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, status) VALUES
(0, '0', '总公司', 0, 'Admin', '13800000001', 1),
(1, '0,1', '技术部', 1, 'CTO', '13800000002', 1),
(1, '0,1', '市场部', 2, 'CMO', '13800000003', 1),
(2, '0,1,2', '后端组', 1, '', '', 1),
(2, '0,1,2', '前端组', 2, '', '', 1);

INSERT INTO sys_post (post_code, post_name, post_sort, status) VALUES
('CEO', '首席执行官', 1, 1),
('CTO', '技术总监', 2, 1),
('MANAGER', '部门经理', 3, 1),
('ENGINEER', '工程师', 4, 1);

INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES
('用户性别', 'sys_user_sex', 1),
('系统状态', 'sys_normal_disable', 1),
('公告类型', 'sys_notice_type', 1);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status) VALUES
(1, '男', '0', 'sys_user_sex', 1),
(2, '女', '1', 'sys_user_sex', 1),
(3, '未知', '2', 'sys_user_sex', 1),
(1, '正常', '1', 'sys_normal_disable', 1),
(2, '停用', '0', 'sys_normal_disable', 1),
(1, '通知', '1', 'sys_notice_type', 1),
(2, '公告', '2', 'sys_notice_type', 1);

INSERT INTO sys_config (config_name, config_key, config_value, config_type) VALUES
('用户管理-初始密码', 'sys.user.initPassword', '123456', 'Y'),
('文件管理-最大上传大小(MB)', 'file.maxSize', '500', 'Y');

INSERT INTO sys_notice (notice_title, notice_type, notice_content, status) VALUES
('系统上线公告', '2', 'UAMS 统一认证管理系统已正式上线运行，欢迎大家使用！', 1),
('操作提示', '1', '请及时修改初始密码，保障账户安全。', 1);
