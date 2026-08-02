-- =============================================================
-- 图书馆预约系统 菜单与权限点 sys_menu（租户 000000）
-- id 段：12000-12999（避开核心菜单，max menu_id=11806）
-- 幂等：先删本段菜单再重灌
-- 超管(role_id=1)天然 bypass 看全部菜单，无需 sys_role_menu；按角色可见范围在 04 阶段配
-- =============================================================
SET NAMES utf8mb4;

DELETE FROM sys_menu WHERE menu_id BETWEEN 12000 AND 12999;

-- 顶层目录：图书馆管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (12000, '图书馆管理', 0, 5, 'library', '', '', 1, 0, 'M', '0', '0', '', 'education', 103, 1, NOW(), NULL, NULL, '图书馆预约系统');

-- 场馆管理（参考模块样板）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (12010, '场馆管理', 12000, 1, 'venue', 'library/venue/index', '', 1, 0, 'C', '0', '0', 'library:venue:list', 'list', 103, 1, NOW(), NULL, NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark) VALUES
(12011, '场馆查询', 12010, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:venue:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12012, '场馆新增', 12010, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:venue:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12013, '场馆修改', 12010, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:venue:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12014, '场馆删除', 12010, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:venue:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12015, '场馆导出', 12010, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:venue:export', '#', 103, 1, NOW(), NULL, NULL, '');
