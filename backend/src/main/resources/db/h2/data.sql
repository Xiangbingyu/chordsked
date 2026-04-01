INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, login_fail_count, locked_until, data_scope_type, created_at, updated_at, is_deleted)
VALUES (1001, 'admin', '$2a$10$abcdefghijklmnopqrstuv', '13800000000', '系统管理员', NULL, 1, 0, 0, NULL, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_campus (name, address, phone, leader_id, leader_name, sort, status, remark, created_at, updated_at, is_deleted)
VALUES ('默认校区', '杭州', '0571-00000000', 1001, '系统管理员', 1, 1, '初始化校区', TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_teacher_user (id, teacher_no, name, phone, password, avatar, teaching_type, teacher_level, campus_id, status, must_change_password, login_fail_count, locked_until, last_login_at, experience, good_at, created_at, updated_at, is_deleted)
VALUES (1001, 'T0001', '王老师', '13900000000', '$2a$10$abcdefghijklmnopqrstuv', NULL, 'ALL', 'SENIOR', 1, 1, 0, 0, NULL, NULL, '5年教学经验', '钢琴基础', TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_student_user (id, phone, name, avatar, status, campus_id, created_at, updated_at, is_deleted)
VALUES (1001, '13700000000', '张同学', NULL, 1, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_role (code, name, description, status, created_at, updated_at, is_deleted)
VALUES ('ADMIN', '系统管理员', '系统预置管理员角色', 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_permission (id, code, name, type, parent_id, path, sort, status, created_at, updated_at, is_deleted)
VALUES (1, 'admin:role', '教务端入口权限', 2, NULL, NULL, 1, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_permission (id, code, name, type, parent_id, path, sort, status, created_at, updated_at, is_deleted)
VALUES (2, 'teacher:role', '教师端入口权限', 2, NULL, NULL, 2, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_permission (id, code, name, type, parent_id, path, sort, status, created_at, updated_at, is_deleted)
VALUES (3, 'student:role', '学员端入口权限', 2, NULL, NULL, 3, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_user_role (user_id, role_id, created_at, updated_at, is_deleted)
VALUES (1001, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_role_permission (role_id, permission_id, created_at, updated_at, is_deleted)
VALUES (1, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_user_campus (user_id, campus_id, is_primary, created_at, updated_at, is_deleted)
VALUES (1001, 1, 1, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00', 0);

INSERT INTO sys_data_scope (target_type, target_id, scope_type, campus_ids, created_at, updated_at)
VALUES ('USER', 1001, 'ALL', NULL, TIMESTAMP '2026-03-25 00:00:00', TIMESTAMP '2026-03-25 00:00:00');
