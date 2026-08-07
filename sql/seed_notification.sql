-- ============================================================
-- Phase 07 小程序 · 站内通知 demo 种子（app_notification）· 幂等
-- ------------------------------------------------------------
-- 让「消息通知」中心开箱有内容;内容对齐后端实际会推送的类型(违约/申诉结果/研讨间审批/到书)。
-- 后端已在 违约记录 / 申诉审批 / 研讨间审批 三处接 NotificationHelper.send 实时推送,
-- 事件发生即自动生成通知;本种子仅为演示读者端展示效果。
-- receiver_id = app_user 主键(读者 1001 张三 / 1002 李四)。应用顺序:seed_app_user 之后即可,可重复执行。
-- ============================================================
SET NAMES utf8mb4;

DELETE FROM app_notification WHERE id BETWEEN 90001 AND 90010;
INSERT INTO app_notification (id,tenant_id,receiver_id,title,content,biz_type,biz_id,is_read,create_dept,create_by,create_time,del_flag) VALUES
 (90001,'000000',1001,'违约提醒','你产生一条违约（座位爽约），扣信用分 10 分。如有异议，可在小程序「违约与申诉」中发起申诉。','violation',NULL,0,103,1,NOW(),'0'),
 (90002,'000000',1001,'申诉结果','你的违约申诉已通过，已解除该违约并回补相应信用分。','appeal',NULL,0,103,1,NOW(),'0'),
 (90003,'000000',1001,'到书通知','你预约的《深度学习》已到馆，请在 3 日内到预约架取书，逾期将释放并顺延下一位。','loan',NULL,1,103,1,NOW()-INTERVAL 1 DAY,'0'),
 (90004,'000000',1002,'研讨间预约已通过','你的研讨间预约已通过审批，请按预约时段到场使用。','roomReservation',NULL,0,103,1,NOW(),'0');
