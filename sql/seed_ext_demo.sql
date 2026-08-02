-- 图书馆预约系统 · 补建模块演示数据(研讨间预约/荐购/占座监督)·幂等
SET NAMES utf8mb4;
SET @VENUE = 2083909208096411650;
SET @F3    = 2083914024663191553;

-- ============ 研讨间预约(各状态齐全：待审批/待使用/已完成) ============
DELETE FROM biz_room_reservation;
INSERT INTO biz_room_reservation
 (id,tenant_id,reader_id,room_id,reserve_date,start_time,end_time,user_count,status,check_in_time,approve_by,approve_time,reject_reason,create_dept,create_by,create_time,del_flag)
VALUES
 (1,'000000',1005,2,CURDATE()+INTERVAL 1 DAY,CONCAT(CURDATE()+INTERVAL 1 DAY,' 14:00:00'),CONCAT(CURDATE()+INTERVAL 1 DAY,' 16:00:00'),4,0,NULL,NULL,NULL,NULL,103,1,NOW(),'0'),
 (2,'000000',1003,1,CURDATE()+INTERVAL 1 DAY,CONCAT(CURDATE()+INTERVAL 1 DAY,' 10:00:00'),CONCAT(CURDATE()+INTERVAL 1 DAY,' 12:00:00'),3,1,NULL,NULL,NULL,NULL,103,1,NOW(),'0'),
 (3,'000000',1009,3,CURDATE()-INTERVAL 1 DAY,CONCAT(CURDATE()-INTERVAL 1 DAY,' 09:00:00'),CONCAT(CURDATE()-INTERVAL 1 DAY,' 11:00:00'),2,3,CONCAT(CURDATE()-INTERVAL 1 DAY,' 09:02:00'),1,CONCAT(CURDATE()-INTERVAL 2 DAY,' 18:00:00'),NULL,103,1,NOW(),'0');

-- ============ 荐购(各状态齐全：待受理/已受理/已驳回/已采购) ============
DELETE FROM biz_purchase_suggest;
INSERT INTO biz_purchase_suggest
 (id,tenant_id,reader_id,title,author,isbn,reason,status,handle_by,handle_time,reject_reason,create_dept,create_by,create_time,del_flag)
VALUES
 (1,'000000',1003,'深度学习','Ian Goodfellow 等','9787115461476','实验室做机器学习课题，急需这本经典教材',0,NULL,NULL,NULL,103,1,NOW(),'0'),
 (2,'000000',1002,'人月神话(纪念典藏版)','Frederick P. Brooks','9787111190059','软件工程经典，建议入藏',1,1,NOW(),NULL,103,1,NOW(),'0'),
 (3,'000000',1001,'活着','余华','9787506365437','希望能借到这本书',2,1,NOW(),'馆藏已有复本，可直接检索借阅',103,1,NOW(),'0'),
 (4,'000000',1005,'三体Ⅱ：黑暗森林','刘慈欣','9787536484573','《三体》续作，读者需求高',3,1,NOW(),NULL,103,1,NOW(),'0');

-- ============ 占座监督：需要一条"使用中"座位预约作为被监督对象 ============
DELETE FROM biz_reservation WHERE id=9110;
INSERT INTO biz_reservation
 (id,tenant_id,reader_id,seat_id,venue_id,floor_id,area_id,reserve_date,start_time,end_time,source,status,check_in_time,away_count,create_dept,create_by,create_time,del_flag)
VALUES
 (9110,'000000',1008,3,@VENUE,@F3,1,CURDATE(),NOW()-INTERVAL 2 HOUR,NOW()+INTERVAL 4 HOUR,1,1,NOW()-INTERVAL 2 HOUR,0,103,1,NOW(),'0');

DELETE FROM biz_supervise;
INSERT INTO biz_supervise
 (id,tenant_id,reservation_id,seat_id,reporter_id,report_time,deadline,status,resolve_time,create_dept,create_by,create_time,del_flag)
VALUES
 (1,'000000',9110,3,1003,NOW(),NOW()+INTERVAL 15 MINUTE,0,NULL,103,1,NOW(),'0'),
 (2,'000000',9110,3,1002,NOW()-INTERVAL 1 DAY,NOW()-INTERVAL 1 DAY+INTERVAL 15 MINUTE,1,NOW()-INTERVAL 1 DAY+INTERVAL 5 MINUTE,103,1,NOW(),'0'),
 (3,'000000',9110,3,1005,NOW()-INTERVAL 2 DAY,NOW()-INTERVAL 2 DAY+INTERVAL 15 MINUTE,2,NOW()-INTERVAL 2 DAY+INTERVAL 15 MINUTE,103,1,NOW(),'0');
