-- 图书馆预约系统 · 补建模块菜单(研讨间预约/荐购/占座监督) sys_menu + 角色授权·幂等·id 段 12200-12299
SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 12200 AND 12299;
DELETE FROM sys_menu WHERE menu_id BETWEEN 12200 AND 12299;

-- C 菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark) VALUES
(12200, '研讨间预约', 12000, 19, 'roomReservation', 'library/roomReservation/index', '', 1, 0, 'C', '0', '0', 'library:roomReservation:list', 'guide', 103, 1, NOW(), NULL, NULL, ''),
(12210, '荐购受理', 12000, 20, 'purchaseSuggest', 'library/purchaseSuggest/index', '', 1, 0, 'C', '0', '0', 'library:purchaseSuggest:list', 'edit', 103, 1, NOW(), NULL, NULL, ''),
(12220, '占座监督', 12000, 21, 'supervise', 'library/supervise/index', '', 1, 0, 'C', '0', '0', 'library:supervise:list', 'eye-open', 103, 1, NOW(), NULL, NULL, '');

-- F 按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark) VALUES
(12201, '研讨间预约查询', 12200, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12202, '研讨间预约新增', 12200, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12203, '研讨间预约修改', 12200, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12204, '研讨间预约删除', 12200, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12205, '研讨间预约导出', 12200, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:export', '#', 103, 1, NOW(), NULL, NULL, ''),
(12206, '研讨间预约审批', 12200, 6, '', '', '', 1, 0, 'F', '0', '0', 'library:roomReservation:approve','#', 103, 1, NOW(), NULL, NULL, ''),
(12211, '荐购查询', 12210, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12212, '荐购新增', 12210, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12213, '荐购修改', 12210, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12214, '荐购删除', 12210, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12215, '荐购导出', 12210, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:export', '#', 103, 1, NOW(), NULL, NULL, ''),
(12216, '荐购受理', 12210, 6, '', '', '', 1, 0, 'F', '0', '0', 'library:purchaseSuggest:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(12221, '监督查询', 12220, 1, '', '', '', 1, 0, 'F', '0', '0', 'library:supervise:query',  '#', 103, 1, NOW(), NULL, NULL, ''),
(12222, '监督发起', 12220, 2, '', '', '', 1, 0, 'F', '0', '0', 'library:supervise:add',    '#', 103, 1, NOW(), NULL, NULL, ''),
(12223, '监督解除', 12220, 3, '', '', '', 1, 0, 'F', '0', '0', 'library:supervise:edit',   '#', 103, 1, NOW(), NULL, NULL, ''),
(12224, '监督删除', 12220, 4, '', '', '', 1, 0, 'F', '0', '0', 'library:supervise:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(12225, '监督导出', 12220, 5, '', '', '', 1, 0, 'F', '0', '0', 'library:supervise:export', '#', 103, 1, NOW(), NULL, NULL, '');

-- 角色授权(依 SRS 权限矩阵)：研讨间预约审批→流通(10)、占座监督→流通(10)、荐购受理→采编(11)
-- 流通管理员(10)：研讨间预约(12200-12206) + 占座监督(12220-12225)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 10, menu_id FROM sys_menu WHERE menu_id BETWEEN 12200 AND 12206 OR menu_id BETWEEN 12220 AND 12225;
-- 采编管理员(11)：荐购(12210-12216)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 11, menu_id FROM sys_menu WHERE menu_id BETWEEN 12210 AND 12216;
