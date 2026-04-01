CREATE TABLE IF NOT EXISTS sys_internal_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(200) NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    must_change_password TINYINT NOT NULL DEFAULT 1 COMMENT '首次登录是否必须修改密码(0:否 1:是)',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME NULL COMMENT '账号锁定截止时间',
    data_scope_type TINYINT NOT NULL COMMENT '数据权限类型(1:全公司 2:本部门及下属 3:本人 4:指定校区)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_internal_user_username (username),
    UNIQUE INDEX uk_sys_internal_user_phone (phone),
    INDEX idx_sys_internal_user_status (status),
    INDEX idx_sys_internal_user_is_deleted (is_deleted),
    INDEX idx_sys_internal_user_data_scope_type (data_scope_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '教务端用户表';

CREATE TABLE IF NOT EXISTS sys_campus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '校区ID',
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address VARCHAR(200) NULL COMMENT '校区地址',
    phone VARCHAR(20) NULL COMMENT '联系电话',
    leader_id BIGINT NULL COMMENT '负责人ID',
    leader_name VARCHAR(50) NULL COMMENT '负责人姓名',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用)',
    remark VARCHAR(200) NULL COMMENT '备注',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_campus_name (name),
    INDEX idx_sys_campus_status (status),
    INDEX idx_sys_campus_is_deleted (is_deleted),
    CONSTRAINT fk_sys_campus_leader_id FOREIGN KEY (leader_id) REFERENCES sys_internal_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '校区表';

CREATE TABLE IF NOT EXISTS sys_teacher_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师ID',
    teacher_no VARCHAR(50) NOT NULL COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(200) NULL COMMENT '头像URL',
    teaching_type VARCHAR(20) NOT NULL COMMENT '授课类型(ONE_ON_ONE/SMALL/LARGE/ALL)',
    teacher_level VARCHAR(20) NOT NULL COMMENT '教师等级(JUNIOR/INTERMEDIATE/SENIOR/EXPERT)',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:在职 2:离职 3:停用)',
    must_change_password TINYINT NOT NULL DEFAULT 1 COMMENT '首次登录是否必须修改密码(0:否 1:是)',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME NULL COMMENT '账号锁定截止时间',
    last_login_at DATETIME NULL COMMENT '最后登录时间',
    experience TEXT NULL COMMENT '教学资历',
    good_at TEXT NULL COMMENT '擅长曲目',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_teacher_user_teacher_no (teacher_no),
    UNIQUE INDEX uk_sys_teacher_user_phone (phone),
    INDEX idx_sys_teacher_user_campus_id (campus_id),
    INDEX idx_sys_teacher_user_status (status),
    INDEX idx_sys_teacher_user_is_deleted (is_deleted),
    CONSTRAINT fk_sys_teacher_user_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '教师用户表';

CREATE TABLE IF NOT EXISTS sys_student_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学员用户ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(200) NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_student_user_phone (phone),
    INDEX idx_sys_student_user_campus_id (campus_id),
    INDEX idx_sys_student_user_status (status),
    INDEX idx_sys_student_user_is_deleted (is_deleted),
    CONSTRAINT fk_sys_student_user_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学员用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    code VARCHAR(50) NOT NULL COMMENT '角色代码(如ADMIN)',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_role_code (code),
    INDEX idx_sys_role_status (status),
    INDEX idx_sys_role_is_deleted (is_deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    code VARCHAR(100) NOT NULL COMMENT '权限代码',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    type TINYINT NOT NULL COMMENT '权限类型(1:菜单 2:按钮 3:数据)',
    parent_id BIGINT NULL COMMENT '父级权限ID',
    path VARCHAR(200) NULL COMMENT '前端路由路径',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_permission_code (code),
    INDEX idx_sys_permission_parent_id (parent_id),
    INDEX idx_sys_permission_status (status),
    INDEX idx_sys_permission_is_deleted (is_deleted),
    CONSTRAINT fk_sys_permission_parent_id FOREIGN KEY (parent_id) REFERENCES sys_permission (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(教务端账号)',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_user_role_user_id_role_id (user_id, role_id),
    INDEX idx_sys_user_role_role_id (role_id),
    INDEX idx_sys_user_role_is_deleted (is_deleted),
    CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES sys_internal_user (id),
    CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_role_permission_role_id_permission_id (role_id, permission_id),
    INDEX idx_sys_role_permission_permission_id (permission_id),
    INDEX idx_sys_role_permission_is_deleted (is_deleted),
    CONSTRAINT fk_sys_role_permission_role_id FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色权限关联表';

CREATE TABLE IF NOT EXISTS sys_user_campus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(教务端账号)',
    campus_id BIGINT NOT NULL COMMENT '校区ID',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主校区(1:是 0:否)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_user_campus_user_id_campus_id (user_id, campus_id),
    INDEX idx_sys_user_campus_campus_id (campus_id),
    INDEX idx_sys_user_campus_is_primary (is_primary),
    INDEX idx_sys_user_campus_is_deleted (is_deleted),
    CONSTRAINT fk_sys_user_campus_user_id FOREIGN KEY (user_id) REFERENCES sys_internal_user (id),
    CONSTRAINT fk_sys_user_campus_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户校区关联表';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    user_type VARCHAR(20) NOT NULL COMMENT '用户类型(INTERNAL/TEACHER/STUDENT)',
    ip VARCHAR(50) NOT NULL COMMENT '登录IP',
    user_agent VARCHAR(200) NULL COMMENT '用户代理',
    result TINYINT NOT NULL COMMENT '登录结果(0:失败 1:成功)',
    message VARCHAR(200) NULL COMMENT '失败原因或结果说明',
    created_at DATETIME NOT NULL COMMENT '登录时间',
    PRIMARY KEY (id),
    INDEX idx_sys_login_log_user_id (user_id),
    INDEX idx_sys_login_log_created_at (created_at),
    INDEX idx_sys_login_log_result (result)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '登录日志表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    ip VARCHAR(50) NOT NULL COMMENT '操作IP',
    user_agent VARCHAR(200) NULL COMMENT '用户代理',
    params TEXT NULL COMMENT '请求参数(JSON)',
    result VARCHAR(20) NOT NULL COMMENT '操作结果(SUCCESS/FAILED)',
    message VARCHAR(500) NULL COMMENT '返回消息',
    duration INT NULL COMMENT '执行时长(ms)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(1:是 0:否)',
    PRIMARY KEY (id),
    INDEX idx_sys_operation_log_module (module),
    INDEX idx_sys_operation_log_operation (operation),
    INDEX idx_sys_operation_log_operator_id (operator_id),
    INDEX idx_sys_operation_log_created_at (created_at),
    INDEX idx_sys_operation_log_is_deleted (is_deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

CREATE TABLE IF NOT EXISTS sys_sms_code (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(10) NOT NULL COMMENT '短信验证码',
    scene VARCHAR(20) NOT NULL COMMENT '用途(login/register/reset)',
    expire_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_sys_sms_code_phone (phone),
    INDEX idx_sys_sms_code_expire_at (expire_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '短信验证码表';

CREATE TABLE IF NOT EXISTS sys_data_scope (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型(ROLE/USER)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    scope_type VARCHAR(20) NOT NULL COMMENT '范围类型(ALL/SPECIFIED/CURRENT/SELF)',
    campus_ids TEXT NULL COMMENT '指定校区ID列表(JSON数组)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_sys_data_scope_target_type_target_id (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '数据权限配置表';

