-- =============================================================
-- 桌子（工位组）管理菜单 menu_desk.sql
-- 在「图书馆管理(12000)」目录下新增「桌子管理」菜单 + 5 个按钮权限，权限点 library:desk:*。
-- 超管(admin, isAdmin)自动可见全部菜单，无需 sys_role_menu 授权；新增菜单后对应账号需重登刷新 /getRouters。
-- 菜单 id 用 12230 段（12190 已被「数据大屏」占用，勿复用；12220=占座监督）。幂等：先删 12230-12235 再插入。
-- =============================================================
SET NAMES utf8mb4;

DELETE FROM `sys_menu` WHERE `menu_id` IN (12230,12231,12232,12233,12234,12235);

INSERT INTO `sys_menu`
(`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query_param`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_dept`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
(12230, '桌子管理', 12000, 6, 'desk', 'library/desk/index', '', 1, 0, 'C', '0', '0', 'library:desk:list',   'rank', 103, 1, NOW(), NULL, NULL, ''),
(12231, '桌子查询', 12230, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12232, '桌子新增', 12230, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12233, '桌子修改', 12230, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12234, '桌子删除', 12230, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12235, '桌子导出', 12230, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:desk:export', '#', 103, 1, NOW(), NULL, NULL, '');
