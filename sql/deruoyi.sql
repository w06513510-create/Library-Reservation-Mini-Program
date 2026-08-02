-- ============================================================
-- 去 RuoYi 化（E 菜单可见范围 + F 部门/演示账号）· 幂等可重跑
-- 用法：对【本项目库 library_reservation】执行；改 sys_menu 后对应角色需【重新登录】刷新 /getRouters（超管也要重登）。
-- 约定：sys_menu.visible '0'=显示 '1'=隐藏；sys_dept/sys_user 用 del_flag='1' 逻辑删。统一“只隐藏/逻辑删，不物理删”，便于回滚。
-- menu_id 为 RuoYi 核心 SQL 固定种子 ID，各项目导入后一致。
-- ============================================================

-- ========== E. 菜单可见范围（隐藏 RuoYi 演示 / 官方 / 与本项目无关项）==========
-- PLUS官网（外链官方仓库）——按外链地址匹配最稳
UPDATE sys_menu SET visible='1' WHERE path='https://gitee.com/dromara/RuoYi-Vue-Plus';
-- 测试菜单（demo）及其子菜单
UPDATE sys_menu SET visible='1' WHERE menu_id=5 OR parent_id=5;
-- 租户管理 / 租户套餐（单租户项目用不到）
UPDATE sys_menu SET visible='1' WHERE menu_id=6 OR parent_id=6;
-- 工作流 / 我的任务（本项目审批为自研，未用 ruoyi-workflow）
UPDATE sys_menu SET visible='1' WHERE menu_id=11616 OR parent_id=11616;
UPDATE sys_menu SET visible='1' WHERE menu_id=11618 OR parent_id=11618;
-- 系统工具（代码生成 / 表单构建，开发期工具，交付产品隐藏）
UPDATE sys_menu SET visible='1' WHERE menu_id=3 OR parent_id=3;
-- 系统监控 · Admin监控（SpringBoot-Admin 外部面板，RuoYi 基础设施）
UPDATE sys_menu SET visible='1' WHERE menu_id=117;
-- 系统管理 · 岗位管理 / 通知公告(sys_notice) / 客户端管理（本项目不用）
UPDATE sys_menu SET visible='1' WHERE menu_id IN (104,107,123);
-- 保留：系统管理(用户/角色/菜单/部门/字典/参数/日志/文件)、系统监控(在线用户/缓存监控/任务调度中心)、图书馆管理

-- ========== F. 部门树 sys_dept（RuoYi 演示公司 → 图书馆组织）==========
UPDATE sys_dept SET dept_name='图书馆'       WHERE dept_id=100;
UPDATE sys_dept SET dept_name='读者服务部'   WHERE dept_id=101;
UPDATE sys_dept SET dept_name='流通部'       WHERE dept_id=103;
UPDATE sys_dept SET dept_name='采编部'       WHERE dept_id=104;
UPDATE sys_dept SET dept_name='技术信息部'   WHERE dept_id=105;
-- 冗余演示部门逻辑删（长沙分公司 / 财务 / 运维 / 分公司市场·财务）
UPDATE sys_dept SET del_flag='1' WHERE dept_id IN (102,106,107,108,109);

-- ========== F. 管理员昵称 + 演示账号 ==========
UPDATE sys_user SET nick_name='系统管理员', dept_id=100 WHERE user_id=1;   -- 原“疯狂的狮子Li”
UPDATE sys_user SET del_flag='1' WHERE user_name IN ('test','test1');        -- 软删自带演示账号
