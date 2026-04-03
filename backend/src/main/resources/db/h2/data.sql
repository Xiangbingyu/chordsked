INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, created_at, updated_at)
VALUES (1001, 'admin', '$2a$10$abcdefghijklmnopqrstuv', '13800000000', '系统管理员', NULL, 1, 0, 1, 1774483200000, 1774483200000);

INSERT INTO sys_campus (name, address, phone, leader_id, leader_name, sort, status, remark, created_at, updated_at)
VALUES ('默认校区', '杭州', '0571-00000000', 1001, '系统管理员', 1, 1, '初始化校区', 1774483200000, 1774483200000);

INSERT INTO sys_teacher_user (id, teacher_no, name, phone, password, avatar, teacher_level, campus_id, status, must_change_password, last_login_at, experience, good_at, created_at, updated_at)
VALUES (1001, 'T0001', '王老师', '13900000000', '$2a$10$abcdefghijklmnopqrstuv', NULL, 3, 1, 1, 0, NULL, '5年教学经验', '钢琴基础', 1774483200000, 1774483200000);

INSERT INTO sys_student_user (id, phone, name, avatar, status, campus_id, created_at, updated_at)
VALUES (1001, '13700000000', '张同学', NULL, 1, 1, 1774483200000, 1774483200000);

INSERT INTO sys_role (code, name, description, status, created_at, updated_at)
VALUES ('ADMIN', '系统管理员', '系统预置管理员角色', 1, 1774483200000, 1774483200000);

INSERT INTO sys_user_role (user_id, role_id, created_at, updated_at)
VALUES (1001, 1, 1774483200000, 1774483200000);

INSERT INTO sys_user_campus (user_id, campus_id, is_primary, created_at, updated_at)
VALUES (1001, 1, 1, 1774483200000, 1774483200000);

INSERT INTO sys_data_scope (target_type, target_id, scope_type, campus_ids, created_at, updated_at)
VALUES (2, 1001, 1, NULL, 1774483200000, 1774483200000);
