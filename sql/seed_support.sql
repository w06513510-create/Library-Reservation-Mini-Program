-- 图书馆 配套主数据(楼层/区域/藏地/书架/研讨间/读者)·幂等
SET NAMES utf8mb4;

DELETE FROM biz_floor WHERE id IN (2,3);
INSERT INTO biz_floor (id,tenant_id,venue_id,floor_name,floor_no,sort,status,create_dept,create_by,create_time,del_flag) VALUES (2,'000000',2083909208096411650,'一楼综合借阅',1,1,0,103,1,NOW(),'0'),(3,'000000',2083909208096411650,'二楼自然科学',2,2,0,103,1,NOW(),'0');

DELETE FROM biz_area WHERE id IN (2,3,4);
INSERT INTO biz_area (id,tenant_id,floor_id,area_name,area_type,sort,status,create_dept,create_by,create_time,del_flag) VALUES (2,'000000',2083914024663191553,'B区研讨',1,2,0,103,1,NOW(),'0'),(3,'000000',2,'一楼借阅区',0,3,0,103,1,NOW(),'0'),(4,'000000',3,'二楼借阅区',0,4,0,103,1,NOW(),'0');

DELETE FROM biz_book_location WHERE id IN (1,2,3);
INSERT INTO biz_book_location (id,tenant_id,location_name,floor_id,sort,status,create_dept,create_by,create_time,del_flag) VALUES (1,'000000','社科借阅室',2083914024663191553,1,0,103,1,NOW(),'0'),(2,'000000','自然科学借阅室',3,2,0,103,1,NOW(),'0'),(3,'000000','文学借阅室',2,3,0,103,1,NOW(),'0');

DELETE FROM biz_shelf WHERE id BETWEEN 1 AND 6;
INSERT INTO biz_shelf (id,tenant_id,location_id,shelf_no,call_no_start,call_no_end,pos_x,pos_y,status,create_dept,create_by,create_time,del_flag) VALUES (1,'000000',2,'TP-01','TP','TP999',100,120,0,103,1,NOW(),'0'),
(2,'000000',2,'O-01','O','O199',260,120,0,103,1,NOW(),'0'),
(3,'000000',3,'I-01','I','I299',100,120,0,103,1,NOW(),'0'),
(4,'000000',1,'F-01','F','F999',100,120,0,103,1,NOW(),'0'),
(5,'000000',1,'K-01','K','K999',260,120,0,103,1,NOW(),'0'),
(6,'000000',1,'B-H01','B','H999',420,120,0,103,1,NOW(),'0');

DELETE FROM biz_room WHERE id IN (1,2,3);
INSERT INTO biz_room (id,tenant_id,floor_id,room_name,capacity,min_users,need_approve,need_checkin,pos_x,pos_y,status,create_dept,create_by,create_time,del_flag) VALUES (1,'000000',2083914024663191553,'研讨间301',6,3,0,1,120,320,0,103,1,NOW(),'0'),
(2,'000000',2083914024663191553,'研讨间302',8,4,1,1,300,320,0,103,1,NOW(),'0'),
(3,'000000',3,'研讨间201',4,2,0,1,120,200,0,103,1,NOW(),'0');

DELETE FROM biz_reader WHERE id BETWEEN 3 AND 12;
DELETE FROM app_user WHERE id BETWEEN 1003 AND 1012;
INSERT INTO app_user (id,tenant_id,phone,password,nickname,gender,status,register_time,create_dept,create_by,create_time,del_flag) VALUES (1003,'000000','2021003','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','王五',0,0,NOW(),103,1,NOW(),'0'),
(1004,'000000','2021004','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','王芳',0,0,NOW(),103,1,NOW(),'0'),
(1005,'000000','2021005','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','陈明',0,0,NOW(),103,1,NOW(),'0'),
(1006,'000000','2021006','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','刘洋',0,0,NOW(),103,1,NOW(),'0'),
(1007,'000000','2021007','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','赵蕾',0,0,NOW(),103,1,NOW(),'0'),
(1008,'000000','2021008','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','孙浩',0,0,NOW(),103,1,NOW(),'0'),
(1009,'000000','2021009','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','周婷',0,0,NOW(),103,1,NOW(),'0'),
(1010,'000000','2021010','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','吴强',0,0,NOW(),103,1,NOW(),'0'),
(1011,'000000','2021011','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','郑爽',0,0,NOW(),103,1,NOW(),'0'),
(1012,'000000','2021012','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','冯磊',0,0,NOW(),103,1,NOW(),'0');
INSERT INTO biz_reader (id,tenant_id,user_id,student_no,real_name,college,major,credit_score,perform_count,blacklist_flag,status,create_dept,create_by,create_time,del_flag) VALUES (3,'000000',1003,'2021003','王五','信息学院','软件工程',100,0,0,0,103,1,NOW(),'0'),
(4,'000000',1004,'2021004','王芳','外语学院','英语',95,0,0,0,103,1,NOW(),'0'),
(5,'000000',1005,'2021005','陈明','文学院','汉语言文学',88,0,0,0,103,1,NOW(),'0'),
(6,'000000',1006,'2021006','刘洋','数学学院','统计学',72,0,0,0,103,1,NOW(),'0'),
(7,'000000',1007,'2021007','赵蕾','经管学院','会计学',60,0,0,0,103,1,NOW(),'0'),
(8,'000000',1008,'2021008','孙浩','历史学院','世界史',45,0,0,0,103,1,NOW(),'0'),
(9,'000000',1009,'2021009','周婷','物理学院','应用物理',100,0,0,0,103,1,NOW(),'0'),
(10,'000000',1010,'2021010','吴强','化学学院','材料化学',92,0,0,0,103,1,NOW(),'0'),
(11,'000000',1011,'2021011','郑爽','法学院','法学',100,0,0,0,103,1,NOW(),'0'),
(12,'000000',1012,'2021012','冯磊','艺术学院','视觉传达',30,0,0,0,103,1,NOW(),'0');
