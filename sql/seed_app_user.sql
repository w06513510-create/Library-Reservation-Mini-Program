-- ============================================================
-- Phase 07 小程序 · C 端登录账号补种（app_user）· 幂等
-- ------------------------------------------------------------
-- 背景：读者 1003–1012 的 app_user 登录账号 + biz_reader 档案已由 seed_support.sql 种好；
--       但读者 1001/1002（张三/李四）早期只建了 biz_reader、缺 app_user 登录账号，
--       且这两条 biz_reader 未落任何种子文件（仅存活于实时库，全量重建会丢）。
--       本文件补齐这两条：既给它们 C 端登录账号，又让 biz_reader 1/2 可随全量重建复现。
-- 登录：手机号 = 学号（2021001/2021002），密码 = admin123（与 1003–1012 同一 BCrypt 串）。
-- 应用顺序：在 seed_support.sql 之后执行即可（无其它依赖）。可重复执行。
-- ============================================================
SET NAMES utf8mb4;

-- C 端登录账号（app_user）：仅补 1001/1002，不动 seed_support 管的 1003–1012
DELETE FROM app_user WHERE id IN (1001, 1002);
INSERT INTO app_user (id,tenant_id,phone,password,nickname,gender,status,register_time,create_dept,create_by,create_time,del_flag) VALUES
 (1001,'000000','2021001','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','张三',1,0,NOW(),103,1,NOW(),'0'),
 (1002,'000000','2021002','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','李四',2,0,NOW(),103,1,NOW(),'0');

-- 读者档案（biz_reader）：补 1/2（其余 3–12 在 seed_support.sql）
DELETE FROM biz_reader WHERE id IN (1, 2);
INSERT INTO biz_reader (id,tenant_id,user_id,student_no,real_name,college,major,credit_score,perform_count,blacklist_flag,status,create_dept,create_by,create_time,del_flag) VALUES
 (1,'000000',1001,'2021001','张三','信息学院',NULL,80,0,0,0,103,1,NOW(),'0'),
 (2,'000000',1002,'2021002','李四','外语学院',NULL,90,0,0,0,103,1,NOW(),'0');
