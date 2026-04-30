CREATE TABLE IF NOT EXISTS sys_internal_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(200) NULL COMMENT '头像URL(阿里云OSS)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用 2:已删除)',
    must_change_password TINYINT NOT NULL DEFAULT 1 COMMENT '首次登录是否必须修改密码(0:否 1:是)',
    data_scope_type TINYINT NOT NULL COMMENT '数据权限类型(1:全公司 2:本部门及下属 3:本人 4:指定校区)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (username),
    UNIQUE (phone)
);

CREATE INDEX IF NOT EXISTS idx_sys_internal_user_status ON sys_internal_user(status);
CREATE INDEX IF NOT EXISTS idx_sys_internal_user_data_scope_type ON sys_internal_user(data_scope_type);

CREATE TABLE IF NOT EXISTS sys_campus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '校区ID',
    code VARCHAR(50) NOT NULL COMMENT '校区编码',
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address VARCHAR(200) NULL COMMENT '校区地址',
    phone VARCHAR(20) NULL COMMENT '联系电话',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用 2:已删除)',
    remark VARCHAR(200) NULL COMMENT '备注',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (code),
    UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_sys_campus_status ON sys_campus(status);

CREATE TABLE IF NOT EXISTS sys_teacher_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师ID',
    teacher_no VARCHAR(50) NOT NULL COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(200) NULL COMMENT '头像URL(阿里云OSS)',
    teacher_level TINYINT NOT NULL COMMENT '教师等级(1:初级 2:中级 3:高级 4:专家)',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:在职 2:离职 3:停用 4:已删除)',
    must_change_password TINYINT NOT NULL DEFAULT 1 COMMENT '首次登录是否必须修改密码(0:否 1:是)',
    last_login_at BIGINT NULL COMMENT '最后登录时间',
    experience TEXT NULL COMMENT '教学资历',
    good_at TEXT NULL COMMENT '擅长曲目',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (teacher_no),
    UNIQUE (phone),
    CONSTRAINT fk_sys_teacher_user_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_teacher_user_campus_id ON sys_teacher_user(campus_id);
CREATE INDEX IF NOT EXISTS idx_sys_teacher_user_status ON sys_teacher_user(status);

CREATE TABLE IF NOT EXISTS sys_student_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学员用户ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(200) NULL COMMENT '头像URL(阿里云OSS)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用 2:已删除)',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (phone),
    CONSTRAINT fk_sys_student_user_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_student_user_campus_id ON sys_student_user(campus_id);
CREATE INDEX IF NOT EXISTS idx_sys_student_user_status ON sys_student_user(status);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    code VARCHAR(50) NOT NULL COMMENT '角色代码(如ADMIN)',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用 2:已删除)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role(status);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    code VARCHAR(100) NOT NULL COMMENT '权限代码(如 admin:user:view)',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    type TINYINT NOT NULL COMMENT '类型(1:菜单 2:按钮)',
    parent_id BIGINT NULL COMMENT '父级权限ID',
    path VARCHAR(200) NULL COMMENT '前端路由路径',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    user_type VARCHAR(20) NOT NULL COMMENT '账号体系(ADMIN/TEACHER/STUDENT)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_sys_permission_status ON sys_permission(status);
CREATE INDEX IF NOT EXISTS idx_sys_permission_user_type ON sys_permission(user_type);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(教务端账号)',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES sys_internal_user (id),
    CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES sys_role (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role(role_id);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (role_id, permission_id),
    CONSTRAINT fk_sys_role_permission_role_id FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);

CREATE TABLE IF NOT EXISTS sys_user_campus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(教务端账号)',
    campus_id BIGINT NOT NULL COMMENT '校区ID',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主校区(1:是 0:否)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (user_id, campus_id),
    CONSTRAINT fk_sys_user_campus_user_id FOREIGN KEY (user_id) REFERENCES sys_internal_user (id),
    CONSTRAINT fk_sys_user_campus_campus_id FOREIGN KEY (campus_id) REFERENCES sys_campus (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_user_campus_campus_id ON sys_user_campus(campus_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_campus_is_primary ON sys_user_campus(is_primary);

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    user_type TINYINT NOT NULL COMMENT '用户类型(1:教务端 2:教师端 3:学员端)',
    ip VARCHAR(50) NOT NULL COMMENT '登录IP',
    user_agent VARCHAR(200) NULL COMMENT '用户代理',
    result TINYINT NOT NULL COMMENT '登录结果(0:失败 1:成功)',
    message VARCHAR(200) NULL COMMENT '失败原因或结果说明',
    created_at BIGINT NOT NULL COMMENT '登录时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_created_at ON sys_login_log(created_at);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_result ON sys_login_log(result);

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
    user_id BIGINT NULL COMMENT '操作人ID',
    user_name VARCHAR(50) NULL COMMENT '操作人名称',
    user_type VARCHAR(20) NULL COMMENT '账号体系(ADMIN/TEACHER/STUDENT)',
    module_name VARCHAR(50) NOT NULL COMMENT '业务模块名称',
    action_type VARCHAR(50) NOT NULL COMMENT '业务动作类型',
    biz_id BIGINT NULL COMMENT '关联业务主键ID',
    request_uri VARCHAR(255) NULL COMMENT '请求URI',
    request_method VARCHAR(10) NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    request_ip VARCHAR(50) NULL COMMENT '请求IP',
    user_agent VARCHAR(200) NULL COMMENT '请求User-Agent',
    request_params TEXT NULL COMMENT '核心请求参数(JSON)',
    response_result TEXT NULL COMMENT '核心响应结果(JSON/文本摘要)',
    status TINYINT NOT NULL COMMENT '执行状态(0:失败 1:成功)',
    error_msg VARCHAR(500) NULL COMMENT '失败原因摘要',
    created_at BIGINT NOT NULL COMMENT '创建时间(毫秒时间戳)',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_audit_log_user_id ON sys_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_user_type ON sys_audit_log(user_type);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_module_action ON sys_audit_log(module_name, action_type);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_biz_id ON sys_audit_log(biz_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_status ON sys_audit_log(status);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_created_at ON sys_audit_log(created_at);

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
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_module ON sys_operation_log(module);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operation ON sys_operation_log(operation);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operator_id ON sys_operation_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_created_at ON sys_operation_log(created_at);

CREATE TABLE IF NOT EXISTS sys_data_scope (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    target_type TINYINT NOT NULL COMMENT '目标类型(1:角色 2:用户)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    scope_type TINYINT NOT NULL COMMENT '范围类型(1:全部 2:指定校区 3:当前校区 4:本人)',
    campus_ids TEXT NULL COMMENT '指定校区ID列表(JSON数组)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_data_scope_target_type_target_id ON sys_data_scope(target_type, target_id);
