-- ============================================================
-- 去 RuoYi 化：隐藏 RuoYi 自带演示菜单（幂等，可重复执行）
-- 用法：导入 RuoYi 核心 ry_vue_5.X.sql 之后，对【本项目库】执行本脚本。
--       改完 sys_menu 需对应角色【重新登录】刷新 /getRouters（超管也要重登）。
-- 约定：visible '0'=显示 '1'=隐藏；本工程统一“只隐藏不物理删”，便于回滚。
-- 注：以下 menu_id 为 RuoYi 核心 SQL 的固定种子 ID，各项目导入后一致。
-- ============================================================

-- PLUS官网（外链 RuoYi 官方仓库，与业务无关）——按外链地址匹配，最稳
UPDATE sys_menu SET visible = '1'
 WHERE path = 'https://gitee.com/dromara/RuoYi-Vue-Plus';

-- 测试菜单（demo 演示菜单及其全部子菜单）
UPDATE sys_menu SET visible = '1'
 WHERE menu_id = 5 OR parent_id = 5;

-- ----- 以下为单租户项目可选项：默认保留，需要时取消注释 -----
-- 隐藏“租户管理 / 租户套餐管理”（多租户功能，单租户用不到）
-- UPDATE sys_menu SET visible = '1' WHERE path IN ('tenant');
-- UPDATE sys_menu SET visible = '1' WHERE menu_id = 6 OR parent_id = 6;
