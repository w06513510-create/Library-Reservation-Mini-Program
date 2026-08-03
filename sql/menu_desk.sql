-- =============================================================
-- 桌子（工位组）管理菜单 menu_desk.sql
-- 在「图书馆管理(12000)」目录下新增「桌子管理」菜单 + 5 个按钮权限，权限点 library:desk:*。
-- 超管(admin, isAdmin)自动可见全部菜单，无需 sys_role_menu 授权；新增菜单后对应账号需重登刷新 /getRouters。
-- 幂等：先删 12190-12195 再插入。
-- =============================================================
SET NAMES utf8mb4;

DELETE FROM `sys_menu` WHERE `menu_id` IN (12190,12191,12192,12193,12194,12195);

INSERT INTO `sys_menu`
(`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(12190, '桌子管理', 12000, 6, 'desk', 'library/desk/index', '', 1, 0, 'C', '0', '0', 'library:desk:list',   'rank', 103, 1, NOW(), NULL, NULL, ''),
(12191, '桌子查询', 12190, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12192, '桌子新增', 12190, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12193, '桌子修改', 12190, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12194, '桌子删除', 12190, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12195, '桌子导出', 12190, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:export', '#', 103, 1, NOW(), NULL, NULL, '');
