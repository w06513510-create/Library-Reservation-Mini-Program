-- =============================================================
-- 高校图书馆预约系统 业务表结构  schema.sql
-- 字符集：utf8mb4 / utf8mb4_general_ci（与 RuoYi 核心库一致）
-- 导入顺序：先导 RuoYi 自带核心 SQL（script/sql/ry_vue_5.X.sql，sys_*）
--   + 基座业务表 app_user.sql / app_notification.sql，再导本文件
-- 命名：表/字段 snake_case；每表每字段带 COMMENT；主键 bigint（雪花，@TableId 默认 ASSIGN_ID，不自增）
-- 外键：逻辑外键（外键列 + 索引，不加物理约束，避免 errno 150、省心）
-- 复用基座：读者账号 = app_user；消息中心 = app_notification（biz_type/biz_id 挂载）；RBAC = sys_*
-- 本项目无金额/无支付：违约只记录 + 信用扣分 + 暂停权限；金额字段仅 biz_book.price（登记用）
-- =============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 域一 · 空间/座位（亮点① 选座 + 寻书 的平面图底座）
-- ============================================================

DROP TABLE IF EXISTS `biz_venue`;
CREATE TABLE `biz_venue` (
  `id`          bigint       NOT NULL                 COMMENT '场馆ID',
  `tenant_id`   varchar(20)  DEFAULT '000000'         COMMENT '租户编号',
  `venue_name`  varchar(100) NOT NULL                 COMMENT '场馆名称（如 中心图书馆/北馆）',
  `address`     varchar(255) DEFAULT NULL             COMMENT '地址',
  `open_time`   varchar(20)  DEFAULT NULL             COMMENT '开馆时间（HH:mm）',
  `close_time`  varchar(20)  DEFAULT NULL             COMMENT '闭馆时间（HH:mm）',
  `sort`        int          DEFAULT 0                COMMENT '排序',
  `status`      tinyint      DEFAULT 0                COMMENT '状态：0正常 1停用',
  `create_dept` bigint       DEFAULT NULL             COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆';

DROP TABLE IF EXISTS `biz_floor`;
CREATE TABLE `biz_floor` (
  `id`             bigint       NOT NULL              COMMENT '楼层ID',
  `tenant_id`      varchar(20)  DEFAULT '000000'      COMMENT '租户编号',
  `venue_id`       bigint       NOT NULL              COMMENT '所属场馆ID（biz_venue）',
  `floor_name`     varchar(50)  NOT NULL              COMMENT '楼层名称（如 三楼社科阅览）',
  `floor_no`       int          DEFAULT NULL          COMMENT '楼层号',
  `floor_plan_url` varchar(500) DEFAULT NULL          COMMENT '楼层平面图底图URL（MinIO；选座与寻书共用）',
  `sort`           int          DEFAULT 0             COMMENT '排序',
  `status`         tinyint      DEFAULT 0             COMMENT '状态：0正常 1停用',
  `create_dept`    bigint       DEFAULT NULL          COMMENT '创建部门',
  `create_by`      bigint       DEFAULT NULL          COMMENT '创建者',
  `create_time`    datetime     DEFAULT NULL          COMMENT '创建时间',
  `update_by`      bigint       DEFAULT NULL          COMMENT '更新者',
  `update_time`    datetime     DEFAULT NULL          COMMENT '更新时间',
  `del_flag`       char(1)      DEFAULT '0'           COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_floor_venue` (`venue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层';

DROP TABLE IF EXISTS `biz_area`;
CREATE TABLE `biz_area` (
  `id`          bigint       NOT NULL                 COMMENT '区域ID',
  `tenant_id`   varchar(20)  DEFAULT '000000'         COMMENT '租户编号',
  `floor_id`    bigint       NOT NULL                 COMMENT '所属楼层ID（biz_floor）',
  `area_name`   varchar(50)  NOT NULL                 COMMENT '区域名称（如 A区自习/研讨区）',
  `area_type`   tinyint      DEFAULT 0                COMMENT '区域类型：0自习阅览 1研讨区 2其它',
  `sort`        int          DEFAULT 0                COMMENT '排序',
  `status`      tinyint      DEFAULT 0                COMMENT '状态：0正常 1停用',
  `create_dept` bigint       DEFAULT NULL             COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_area_floor` (`floor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域';

-- 桌子（工位组）：区域 → 桌子 → 座位 三级空间模型的中间层。真实自习室一桌坐 N 人（清华/南开/复旦等）。
DROP TABLE IF EXISTS `biz_desk`;
CREATE TABLE `biz_desk` (
  `id`          bigint       NOT NULL                 COMMENT '桌子ID',
  `tenant_id`   varchar(20)  DEFAULT '000000'         COMMENT '租户编号',
  `area_id`     bigint       NOT NULL                 COMMENT '所属区域ID（biz_area）',
  `desk_no`     varchar(30)  NOT NULL                 COMMENT '桌号（区域内唯一，如 D01；扫桌面二维码定位到桌）',
  `capacity`    tinyint      DEFAULT 4                COMMENT '容量（座位数）：1单人 2双人 4四人 6六人',
  `shape`       tinyint      DEFAULT 0                COMMENT '桌形：0矩形 1圆 2吧台',
  `pos_x`       int          DEFAULT 0                COMMENT '平面图X坐标（桌子左上角绝对坐标）',
  `pos_y`       int          DEFAULT 0                COMMENT '平面图Y坐标（桌子左上角绝对坐标）',
  `width`       int          DEFAULT 150              COMMENT '平面图宽度（px）',
  `height`      int          DEFAULT 120              COMMENT '平面图高度（px）',
  `rotation`    int          DEFAULT 0                COMMENT '旋转角度（度，0不旋转）',
  `sort`        int          DEFAULT 0                COMMENT '排序',
  `status`      tinyint      DEFAULT 0                COMMENT '状态：0正常 1停用',
  `create_dept` bigint       DEFAULT NULL             COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_desk_area_no` (`area_id`, `desk_no`, `tenant_id`),
  KEY `idx_desk_area` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌子（工位组）';

DROP TABLE IF EXISTS `biz_seat`;
CREATE TABLE `biz_seat` (
  `id`          bigint       NOT NULL                 COMMENT '座位ID',
  `tenant_id`   varchar(20)  DEFAULT '000000'         COMMENT '租户编号',
  `area_id`     bigint       NOT NULL                 COMMENT '所属区域ID（biz_area）',
  `desk_id`     bigint       DEFAULT NULL             COMMENT '所属桌子ID（biz_desk；三级空间模型）',
  `seat_no`     varchar(30)  NOT NULL                 COMMENT '座位编号（区域内唯一，如 D01-1）',
  `seat_type`   tinyint      DEFAULT 0                COMMENT '座位类型：0普通 1靠窗 2沙发 3单间',
  `has_power`   tinyint      DEFAULT 0                COMMENT '有无插座：0无 1有',
  `pos_x`       int          DEFAULT 0                COMMENT '平面图X坐标（绝对；= 桌子pos_x + offset_x，冗余便于兼容）',
  `pos_y`       int          DEFAULT 0                COMMENT '平面图Y坐标（绝对；= 桌子pos_y + offset_y，冗余便于兼容）',
  `offset_x`    int          DEFAULT 0                COMMENT '相对所属桌子左上角的X偏移（px；配桌时座位成组排布）',
  `offset_y`    int          DEFAULT 0                COMMENT '相对所属桌子左上角的Y偏移（px）',
  `qr_code`     varchar(64)  DEFAULT NULL             COMMENT '桌面二维码标识（扫码签到用）',
  `status`      tinyint      DEFAULT 0                COMMENT '状态：0正常 1停用（是否可被预约；实时占用由预约单推导）',
  `create_dept` bigint       DEFAULT NULL             COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seat_area_no` (`area_id`, `seat_no`, `tenant_id`),
  KEY `idx_seat_area` (`area_id`),
  KEY `idx_seat_desk` (`desk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位';

DROP TABLE IF EXISTS `biz_room`;
CREATE TABLE `biz_room` (
  `id`           bigint       NOT NULL                COMMENT '研讨间ID',
  `tenant_id`    varchar(20)  DEFAULT '000000'        COMMENT '租户编号',
  `floor_id`     bigint       NOT NULL                COMMENT '所属楼层ID（biz_floor）',
  `room_name`    varchar(50)  NOT NULL                COMMENT '研讨间名称/编号',
  `capacity`     int          DEFAULT 0               COMMENT '容纳人数',
  `min_users`    int          DEFAULT 1               COMMENT '预约最少人数',
  `need_approve` tinyint      DEFAULT 0               COMMENT '是否需审批：0否 1是',
  `need_checkin` tinyint      DEFAULT 1               COMMENT '是否需签到：0否 1是',
  `pos_x`        int          DEFAULT 0               COMMENT '平面图X坐标',
  `pos_y`        int          DEFAULT 0               COMMENT '平面图Y坐标',
  `status`       tinyint      DEFAULT 0               COMMENT '状态：0正常 1停用',
  `create_dept`  bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`    bigint       DEFAULT NULL            COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    bigint       DEFAULT NULL            COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`     char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_room_floor` (`floor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研讨间（空间预约）';

-- ============================================================
-- 域二 · 座位/研讨间预约（含占座监督）
-- ============================================================

DROP TABLE IF EXISTS `biz_reservation`;
CREATE TABLE `biz_reservation` (
  `id`              bigint      NOT NULL              COMMENT '座位预约单ID',
  `tenant_id`       varchar(20) DEFAULT '000000'      COMMENT '租户编号',
  `reader_id`       bigint      NOT NULL              COMMENT '读者ID（app_user）',
  `seat_id`         bigint      NOT NULL              COMMENT '座位ID（biz_seat）',
  `venue_id`        bigint      DEFAULT NULL          COMMENT '场馆ID（冗余，便于统计）',
  `floor_id`        bigint      DEFAULT NULL          COMMENT '楼层ID（冗余）',
  `area_id`         bigint      DEFAULT NULL          COMMENT '区域ID（冗余）',
  `reserve_date`    date        NOT NULL              COMMENT '预约日期',
  `start_time`      datetime    NOT NULL              COMMENT '时段开始',
  `end_time`        datetime    NOT NULL              COMMENT '时段结束',
  `source`          tinyint     DEFAULT 1             COMMENT '预约方式：1平面图选座 2快速选座 3现场扫码',
  `status`          tinyint     DEFAULT 0             COMMENT '状态：0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约',
  `check_in_time`   datetime    DEFAULT NULL          COMMENT '签到时间',
  `away_start_time` datetime    DEFAULT NULL          COMMENT '本次暂离开始时间',
  `away_count`      int         DEFAULT 0             COMMENT '暂离次数',
  `actual_end_time` datetime    DEFAULT NULL          COMMENT '实际退座/结束时间',
  `cancel_time`     datetime    DEFAULT NULL          COMMENT '取消时间',
  `remark`          varchar(255) DEFAULT NULL         COMMENT '备注（如强制释放原因）',
  `create_dept`     bigint      DEFAULT NULL          COMMENT '创建部门',
  `create_by`       bigint      DEFAULT NULL          COMMENT '创建者',
  `create_time`     datetime    DEFAULT NULL          COMMENT '创建时间',
  `update_by`       bigint      DEFAULT NULL          COMMENT '更新者',
  `update_time`     datetime    DEFAULT NULL          COMMENT '更新时间',
  `del_flag`        char(1)     DEFAULT '0'           COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_rsv_reader` (`reader_id`, `status`),
  KEY `idx_rsv_seat_date` (`seat_id`, `reserve_date`),
  KEY `idx_rsv_status_end` (`status`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约单（座位资源不变式：同座同时段至多一条有效占用 0/1/2，由业务层校验兜底）';

DROP TABLE IF EXISTS `biz_room_reservation`;
CREATE TABLE `biz_room_reservation` (
  `id`            bigint      NOT NULL                COMMENT '研讨间预约单ID',
  `tenant_id`     varchar(20) DEFAULT '000000'        COMMENT '租户编号',
  `reader_id`     bigint      NOT NULL                COMMENT '预约读者ID（app_user）',
  `room_id`       bigint      NOT NULL                COMMENT '研讨间ID（biz_room）',
  `reserve_date`  date        NOT NULL                COMMENT '预约日期',
  `start_time`    datetime    NOT NULL                COMMENT '时段开始',
  `end_time`      datetime    NOT NULL                COMMENT '时段结束',
  `user_count`    int         DEFAULT 1               COMMENT '使用人数',
  `status`        tinyint     DEFAULT 0               COMMENT '状态：0待审批 1已通过待使用 2使用中 3已完成 4已取消 5已驳回 6已违约',
  `check_in_time` datetime    DEFAULT NULL            COMMENT '签到时间',
  `approve_by`    bigint      DEFAULT NULL            COMMENT '审批人（sys_user）',
  `approve_time`  datetime    DEFAULT NULL            COMMENT '审批时间',
  `reject_reason` varchar(255) DEFAULT NULL           COMMENT '驳回原因',
  `create_dept`   bigint      DEFAULT NULL            COMMENT '创建部门',
  `create_by`     bigint      DEFAULT NULL            COMMENT '创建者',
  `create_time`   datetime    DEFAULT NULL            COMMENT '创建时间',
  `update_by`     bigint      DEFAULT NULL            COMMENT '更新者',
  `update_time`   datetime    DEFAULT NULL            COMMENT '更新时间',
  `del_flag`      char(1)     DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_roomrsv_reader` (`reader_id`, `status`),
  KEY `idx_roomrsv_room_date` (`room_id`, `reserve_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研讨间预约单';

DROP TABLE IF EXISTS `biz_supervise`;
CREATE TABLE `biz_supervise` (
  `id`             bigint      NOT NULL               COMMENT '占座监督记录ID',
  `tenant_id`      varchar(20) DEFAULT '000000'       COMMENT '租户编号',
  `reservation_id` bigint      NOT NULL               COMMENT '被监督的座位预约单ID（biz_reservation）',
  `seat_id`        bigint      NOT NULL               COMMENT '座位ID',
  `reporter_id`    bigint      NOT NULL               COMMENT '举报读者ID（app_user）',
  `report_time`    datetime    NOT NULL               COMMENT '举报时间',
  `deadline`       datetime    NOT NULL               COMMENT '原用户落座截止时间',
  `status`         tinyint     DEFAULT 0              COMMENT '状态：0进行中 1已解除(已落座) 2超时释放',
  `resolve_time`   datetime    DEFAULT NULL           COMMENT '解除/释放时间',
  `create_dept`    bigint      DEFAULT NULL           COMMENT '创建部门',
  `create_by`      bigint      DEFAULT NULL           COMMENT '创建者',
  `create_time`    datetime    DEFAULT NULL           COMMENT '创建时间',
  `update_by`      bigint      DEFAULT NULL           COMMENT '更新者',
  `update_time`    datetime    DEFAULT NULL           COMMENT '更新时间',
  `del_flag`       char(1)     DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_sup_rsv` (`reservation_id`),
  KEY `idx_sup_status` (`status`, `deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='占座监督记录';

-- ============================================================
-- 域三 · 图书馆藏/采编（藏地→书架 支撑亮点① 寻书）
-- ============================================================

DROP TABLE IF EXISTS `biz_book_location`;
CREATE TABLE `biz_book_location` (
  `id`            bigint       NOT NULL               COMMENT '藏地ID',
  `tenant_id`     varchar(20)  DEFAULT '000000'       COMMENT '租户编号',
  `location_name` varchar(100) NOT NULL               COMMENT '藏地名称（如 三楼社科借阅室）',
  `floor_id`      bigint       DEFAULT NULL           COMMENT '所在楼层ID（biz_floor，寻书平面图定位）',
  `sort`          int          DEFAULT 0              COMMENT '排序',
  `status`        tinyint      DEFAULT 0              COMMENT '状态：0正常 1停用',
  `create_dept`   bigint       DEFAULT NULL           COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL           COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL           COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL           COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL           COMMENT '更新时间',
  `del_flag`      char(1)      DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_loc_floor` (`floor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏地（借阅室/阅览室）';

DROP TABLE IF EXISTS `biz_shelf`;
CREATE TABLE `biz_shelf` (
  `id`            bigint       NOT NULL               COMMENT '书架ID',
  `tenant_id`     varchar(20)  DEFAULT '000000'       COMMENT '租户编号',
  `location_id`   bigint       NOT NULL               COMMENT '所属藏地ID（biz_book_location）',
  `shelf_no`      varchar(30)  NOT NULL               COMMENT '架号（如 A12）',
  `call_no_start` varchar(50)  DEFAULT NULL           COMMENT '索书号起（排架区间起）',
  `call_no_end`   varchar(50)  DEFAULT NULL           COMMENT '索书号止（排架区间止）',
  `pos_x`         int          DEFAULT 0              COMMENT '平面图X坐标（亮点①寻书）',
  `pos_y`         int          DEFAULT 0              COMMENT '平面图Y坐标（亮点①寻书）',
  `status`        tinyint      DEFAULT 0              COMMENT '状态：0正常 1停用',
  `create_dept`   bigint       DEFAULT NULL           COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL           COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL           COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL           COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL           COMMENT '更新时间',
  `del_flag`      char(1)      DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_shelf_loc` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书架（按索书号排架，寻书平面图定位点）';

DROP TABLE IF EXISTS `biz_book`;
CREATE TABLE `biz_book` (
  `id`           bigint       NOT NULL                COMMENT '书目ID（种/Bib）',
  `tenant_id`    varchar(20)  DEFAULT '000000'        COMMENT '租户编号',
  `isbn`         varchar(20)  DEFAULT NULL            COMMENT 'ISBN',
  `title`        varchar(255) NOT NULL                COMMENT '题名',
  `author`       varchar(255) DEFAULT NULL            COMMENT '著者',
  `publisher`    varchar(150) DEFAULT NULL            COMMENT '出版社',
  `publish_date` varchar(20)  DEFAULT NULL            COMMENT '出版日期',
  `clc_no`       varchar(50)  DEFAULT NULL            COMMENT '中图法分类号（CLC）',
  `call_no`      varchar(50)  DEFAULT NULL            COMMENT '索书号（种级基准 = 分类号+著者号）',
  `cover_url`    varchar(500) DEFAULT NULL            COMMENT '封面图URL（MinIO）',
  `summary`      varchar(2000) DEFAULT NULL           COMMENT '内容简介',
  `price`        decimal(10,2) DEFAULT 0.00           COMMENT '定价（登记用，非交易；本项目不涉支付）',
  `total_qty`    int          DEFAULT 0               COMMENT '复本总数（册数，冗余统计）',
  `avail_qty`    int          DEFAULT 0               COMMENT '当前可借册数（冗余，借还时维护）',
  `status`       tinyint      DEFAULT 0               COMMENT '状态：0在编 1已上架(可借) 2已下架',
  `create_dept`  bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`    bigint       DEFAULT NULL            COMMENT '创建者（采编）',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    bigint       DEFAULT NULL            COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`     char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_book_isbn` (`isbn`),
  KEY `idx_book_title` (`title`),
  KEY `idx_book_clc` (`clc_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书目（种/Bib）';

DROP TABLE IF EXISTS `biz_book_item`;
CREATE TABLE `biz_book_item` (
  `id`              bigint       NOT NULL             COMMENT '馆藏册ID（册/Item）',
  `tenant_id`       varchar(20)  DEFAULT '000000'     COMMENT '租户编号',
  `book_id`         bigint       NOT NULL             COMMENT '所属书目ID（biz_book）',
  `barcode`         varchar(40)  NOT NULL             COMMENT '条码（每册唯一）',
  `call_no`         varchar(60)  DEFAULT NULL         COMMENT '索书号（含别本/种次号）',
  `location_id`     bigint       DEFAULT NULL         COMMENT '藏地ID（biz_book_location）',
  `shelf_id`        bigint       DEFAULT NULL         COMMENT '书架/排架位ID（biz_shelf，寻书定位）',
  `status`          tinyint      DEFAULT 0            COMMENT '状态：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销',
  `withdraw_type`   tinyint      DEFAULT NULL         COMMENT '注销类型：1剔旧 2报损 3遗失核销（status=6时填）',
  `withdraw_reason` varchar(255) DEFAULT NULL         COMMENT '注销原因',
  `withdraw_time`   datetime     DEFAULT NULL         COMMENT '注销时间',
  `create_dept`     bigint       DEFAULT NULL         COMMENT '创建部门',
  `create_by`       bigint       DEFAULT NULL         COMMENT '创建者（采编）',
  `create_time`     datetime     DEFAULT NULL         COMMENT '创建时间',
  `update_by`       bigint       DEFAULT NULL         COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL         COMMENT '更新时间',
  `del_flag`        char(1)      DEFAULT '0'          COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_barcode` (`barcode`, `tenant_id`),
  KEY `idx_item_book` (`book_id`),
  KEY `idx_item_shelf` (`shelf_id`),
  KEY `idx_item_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='馆藏册（册/Item，可借最小实体）';

-- ============================================================
-- 域四 · 流通借阅（借还/续借/预约hold/荐购）
-- ============================================================

DROP TABLE IF EXISTS `biz_loan`;
CREATE TABLE `biz_loan` (
  `id`              bigint      NOT NULL              COMMENT '借阅单ID',
  `tenant_id`       varchar(20) DEFAULT '000000'      COMMENT '租户编号',
  `reader_id`       bigint      NOT NULL              COMMENT '读者ID（app_user）',
  `item_id`         bigint      NOT NULL              COMMENT '馆藏册ID（biz_book_item）',
  `book_id`         bigint      DEFAULT NULL          COMMENT '书目ID（冗余，便于统计）',
  `borrow_time`     datetime    NOT NULL              COMMENT '借出时间',
  `due_time`        datetime    NOT NULL              COMMENT '应还日期',
  `renew_count`     int         DEFAULT 0             COMMENT '已续借次数',
  `return_time`     datetime    DEFAULT NULL          COMMENT '归还时间',
  `status`          tinyint     DEFAULT 0             COMMENT '状态：0在借 1已还 2逾期(在借且超期)',
  `overdue_flag`    tinyint     DEFAULT 0             COMMENT '是否曾逾期：0否 1是',
  `recall_flag`     tinyint     DEFAULT 0             COMMENT '是否被预约催还：0否 1是',
  `recall_time`     datetime    DEFAULT NULL          COMMENT '催还时间',
  `borrow_location` bigint      DEFAULT NULL          COMMENT '借出藏地（通借通还预留）',
  `return_location` bigint      DEFAULT NULL          COMMENT '归还藏地（通借通还预留）',
  `operator_id`     bigint      DEFAULT NULL          COMMENT '经办流通员（sys_user）',
  `create_dept`     bigint      DEFAULT NULL          COMMENT '创建部门',
  `create_by`       bigint      DEFAULT NULL          COMMENT '创建者',
  `create_time`     datetime    DEFAULT NULL          COMMENT '创建时间',
  `update_by`       bigint      DEFAULT NULL          COMMENT '更新者',
  `update_time`     datetime    DEFAULT NULL          COMMENT '更新时间',
  `del_flag`        char(1)     DEFAULT '0'           COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_loan_reader` (`reader_id`, `status`),
  KEY `idx_loan_item` (`item_id`),
  KEY `idx_loan_due` (`status`, `due_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅单';

DROP TABLE IF EXISTS `biz_hold`;
CREATE TABLE `biz_hold` (
  `id`            bigint      NOT NULL                COMMENT '图书预约(hold)ID',
  `tenant_id`     varchar(20) DEFAULT '000000'        COMMENT '租户编号',
  `reader_id`     bigint      NOT NULL                COMMENT '读者ID（app_user）',
  `book_id`       bigint      NOT NULL                COMMENT '书目ID（biz_book）',
  `item_id`       bigint      DEFAULT NULL            COMMENT '到书保留的馆藏册ID（到书后指定）',
  `queue_no`      int         DEFAULT NULL            COMMENT '队列位次',
  `status`        tinyint     DEFAULT 0               COMMENT '状态：0排队中 1到书保留(在预约架) 2已取书 3已取消 4过期释放',
  `hold_time`     datetime    NOT NULL                COMMENT '预约时间',
  `ready_time`    datetime    DEFAULT NULL            COMMENT '到书时间',
  `hold_deadline` datetime    DEFAULT NULL            COMMENT '预约架保留期截止',
  `pickup_time`   datetime    DEFAULT NULL            COMMENT '取书时间',
  `cancel_time`   datetime    DEFAULT NULL            COMMENT '取消时间',
  `create_dept`   bigint      DEFAULT NULL            COMMENT '创建部门',
  `create_by`     bigint      DEFAULT NULL            COMMENT '创建者',
  `create_time`   datetime    DEFAULT NULL            COMMENT '创建时间',
  `update_by`     bigint      DEFAULT NULL            COMMENT '更新者',
  `update_time`   datetime    DEFAULT NULL            COMMENT '更新时间',
  `del_flag`      char(1)     DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_hold_reader` (`reader_id`, `status`),
  KEY `idx_hold_book` (`book_id`, `status`),
  KEY `idx_hold_deadline` (`status`, `hold_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书预约(hold)/预约架';

DROP TABLE IF EXISTS `biz_purchase_suggest`;
CREATE TABLE `biz_purchase_suggest` (
  `id`            bigint       NOT NULL               COMMENT '荐购ID',
  `tenant_id`     varchar(20)  DEFAULT '000000'       COMMENT '租户编号',
  `reader_id`     bigint       NOT NULL               COMMENT '荐购读者ID（app_user）',
  `title`         varchar(255) NOT NULL               COMMENT '书名',
  `author`        varchar(255) DEFAULT NULL           COMMENT '著者',
  `isbn`          varchar(20)  DEFAULT NULL           COMMENT 'ISBN',
  `reason`        varchar(500) DEFAULT NULL           COMMENT '荐购理由',
  `status`        tinyint      DEFAULT 0              COMMENT '状态：0待受理 1已受理(转采购) 2已驳回 3已采购',
  `handle_by`     bigint       DEFAULT NULL           COMMENT '处理人（sys_user，采编）',
  `handle_time`   datetime     DEFAULT NULL           COMMENT '处理时间',
  `reject_reason` varchar(255) DEFAULT NULL           COMMENT '驳回原因',
  `create_dept`   bigint       DEFAULT NULL           COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL           COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL           COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL           COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL           COMMENT '更新时间',
  `del_flag`      char(1)      DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_sug_reader` (`reader_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者荐购';

-- ============================================================
-- 域五 · 读者档案 + 信用/违约/黑名单（亮点② 信用分体系）
-- ============================================================

DROP TABLE IF EXISTS `biz_reader`;
CREATE TABLE `biz_reader` (
  `id`               bigint      NOT NULL             COMMENT '读者档案ID',
  `tenant_id`        varchar(20) DEFAULT '000000'     COMMENT '租户编号',
  `user_id`          bigint      NOT NULL             COMMENT '关联C端账号ID（app_user，1:1）',
  `student_no`       varchar(30) NOT NULL             COMMENT '学号/校园卡号（实名唯一）',
  `real_name`        varchar(50) DEFAULT NULL         COMMENT '真实姓名',
  `college`          varchar(100) DEFAULT NULL        COMMENT '院系',
  `major`            varchar(100) DEFAULT NULL        COMMENT '专业',
  `credit_score`     int         DEFAULT 100          COMMENT '当前信用分（0-100，冗余；权威值=Σ biz_credit_log.delta）',
  `perform_count`    int         DEFAULT 0            COMMENT '守信(履约)次数',
  `blacklist_flag`   tinyint     DEFAULT 0            COMMENT '是否黑名单：0否 1是',
  `blacklist_end_time` datetime  DEFAULT NULL         COMMENT '黑名单暂停到期时间',
  `status`           tinyint     DEFAULT 0            COMMENT '状态：0正常 1受限 2停用',
  `create_dept`      bigint      DEFAULT NULL         COMMENT '创建部门',
  `create_by`        bigint      DEFAULT NULL         COMMENT '创建者',
  `create_time`      datetime    DEFAULT NULL         COMMENT '创建时间',
  `update_by`        bigint      DEFAULT NULL         COMMENT '更新者',
  `update_time`      datetime    DEFAULT NULL         COMMENT '更新时间',
  `del_flag`         char(1)     DEFAULT '0'          COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reader_user` (`user_id`, `tenant_id`),
  UNIQUE KEY `uk_reader_student_no` (`student_no`, `tenant_id`),
  KEY `idx_reader_blacklist` (`blacklist_flag`, `blacklist_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者档案（app_user 的实名+信用扩展，1:1）';

-- 信用流水：append-only，不软删（无 del_flag）；当前分 = Σ delta = 最后一条 score_after（对平校验）
DROP TABLE IF EXISTS `biz_credit_log`;
CREATE TABLE `biz_credit_log` (
  `id`          bigint      NOT NULL                  COMMENT '信用流水ID',
  `tenant_id`   varchar(20) DEFAULT '000000'          COMMENT '租户编号',
  `reader_id`   bigint      NOT NULL                  COMMENT '读者ID（app_user）',
  `delta`       int         NOT NULL                  COMMENT '本次变动分值（带符号，+/-）',
  `reason_type` tinyint     NOT NULL                  COMMENT '事由：1建档 2座位爽约 3暂离超时 4监督未落座 5未签退 6图书逾期 7预约架超期 8遗失损坏 9履约加分 10时间衰减 11申诉冲正 12黑名单校准',
  `reason_desc` varchar(255) DEFAULT NULL             COMMENT '事由说明',
  `biz_type`    varchar(50) DEFAULT NULL              COMMENT '关联业务类型（reservation/loan/violation/appeal/blacklist...）',
  `biz_id`      bigint      DEFAULT NULL              COMMENT '关联业务ID',
  `score_after` int         NOT NULL                  COMMENT '变动后信用分（=截至本条的 Σdelta，clamp 0-100，供查库对平）',
  `create_dept` bigint      DEFAULT NULL              COMMENT '创建部门',
  `create_by`   bigint      DEFAULT NULL              COMMENT '创建者（系统/管理员）',
  `create_time` datetime    DEFAULT NULL              COMMENT '创建时间',
  `update_by`   bigint      DEFAULT NULL              COMMENT '更新者（继承 BaseEntity；append-only 恒为 NULL，仅为兼容 insert 填充）',
  `update_time` datetime    DEFAULT NULL              COMMENT '更新时间（同上，恒为 NULL）',
  PRIMARY KEY (`id`),
  KEY `idx_creditlog_reader` (`reader_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用流水（append-only，不软删；一致性不变式 credit_score=clamp(Σdelta,0,100)）';

DROP TABLE IF EXISTS `biz_violation`;
CREATE TABLE `biz_violation` (
  `id`             bigint      NOT NULL               COMMENT '违约记录ID',
  `tenant_id`      varchar(20) DEFAULT '000000'       COMMENT '租户编号',
  `reader_id`      bigint      NOT NULL               COMMENT '读者ID（app_user）',
  `violation_type` tinyint     NOT NULL               COMMENT '违约类型：1座位爽约 2暂离超时 3监督未落座 4未签退 5图书逾期 6预约架超期 7遗失损坏',
  `biz_type`       varchar(50) DEFAULT NULL           COMMENT '关联业务类型',
  `biz_id`         bigint      DEFAULT NULL           COMMENT '关联业务ID（预约单/借阅单等）',
  `deduct_score`   int         DEFAULT 0              COMMENT '扣分',
  `occur_time`     datetime    NOT NULL               COMMENT '发生时间',
  `source`         tinyint     DEFAULT 0              COMMENT '来源：0系统判定 1管理员登记',
  `status`         tinyint     DEFAULT 0              COMMENT '状态：0有效 1已申诉解除',
  `create_dept`    bigint      DEFAULT NULL           COMMENT '创建部门',
  `create_by`      bigint      DEFAULT NULL           COMMENT '创建者',
  `create_time`    datetime    DEFAULT NULL           COMMENT '创建时间',
  `update_by`      bigint      DEFAULT NULL           COMMENT '更新者',
  `update_time`    datetime    DEFAULT NULL           COMMENT '更新时间',
  `del_flag`       char(1)     DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_vio_reader` (`reader_id`, `status`),
  KEY `idx_vio_type` (`violation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违约记录';

DROP TABLE IF EXISTS `biz_appeal`;
CREATE TABLE `biz_appeal` (
  `id`            bigint       NOT NULL               COMMENT '违约申诉ID',
  `tenant_id`     varchar(20)  DEFAULT '000000'       COMMENT '租户编号',
  `violation_id`  bigint       NOT NULL               COMMENT '被申诉的违约记录ID（biz_violation）',
  `reader_id`     bigint       NOT NULL               COMMENT '申诉读者ID（app_user）',
  `reason`        varchar(500) NOT NULL               COMMENT '申诉理由',
  `status`        tinyint      DEFAULT 0              COMMENT '状态：0待审 1通过(解除违约+冲正) 2驳回',
  `audit_by`      bigint       DEFAULT NULL           COMMENT '审批人（sys_user，流通）',
  `audit_time`    datetime     DEFAULT NULL           COMMENT '审批时间',
  `audit_remark`  varchar(255) DEFAULT NULL           COMMENT '审批意见',
  `create_dept`   bigint       DEFAULT NULL           COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL           COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL           COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL           COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL           COMMENT '更新时间',
  `del_flag`      char(1)      DEFAULT '0'            COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_appeal_vio` (`violation_id`),
  KEY `idx_appeal_reader` (`reader_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违约申诉';

DROP TABLE IF EXISTS `biz_blacklist`;
CREATE TABLE `biz_blacklist` (
  `id`           bigint       NOT NULL                COMMENT '黑名单ID',
  `tenant_id`    varchar(20)  DEFAULT '000000'        COMMENT '租户编号',
  `reader_id`    bigint       NOT NULL                COMMENT '读者ID（app_user）',
  `reason`       varchar(255) DEFAULT NULL            COMMENT '拉黑原因（低于阈值/累计违约）',
  `start_time`   datetime     NOT NULL                COMMENT '生效时间',
  `end_time`     datetime     DEFAULT NULL            COMMENT '暂停到期时间',
  `status`       tinyint      DEFAULT 0               COMMENT '状态：0生效中 1已解除',
  `release_type` tinyint      DEFAULT NULL            COMMENT '解除方式：1到期自动 2申诉通过 3手动',
  `release_time` datetime     DEFAULT NULL            COMMENT '解除时间',
  `create_dept`  bigint       DEFAULT NULL            COMMENT '创建部门',
  `create_by`    bigint       DEFAULT NULL            COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    bigint       DEFAULT NULL            COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`     char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_black_reader` (`reader_id`, `status`),
  KEY `idx_black_end` (`status`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单';

-- ============================================================
-- 域六 · 规则配置（座位时段参数 + 信用规则，系统管理员维护）
-- ============================================================

DROP TABLE IF EXISTS `biz_rule_config`;
CREATE TABLE `biz_rule_config` (
  `id`          bigint       NOT NULL                 COMMENT '规则配置ID',
  `tenant_id`   varchar(20)  DEFAULT '000000'         COMMENT '租户编号',
  `rule_group`  varchar(50)  NOT NULL                 COMMENT '规则分组：seat座位/book图书/credit信用/task定时任务(开关与cron)',
  `rule_key`    varchar(80)  NOT NULL                 COMMENT '规则键（如 checkin_window_min/away_minutes/hold_keep_days/init_score/threshold_blacklist）',
  `rule_value`  varchar(100) NOT NULL                 COMMENT '规则值',
  `remark`      varchar(255) DEFAULT NULL             COMMENT '说明',
  `create_dept` bigint       DEFAULT NULL             COMMENT '创建部门',
  `create_by`   bigint       DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   bigint       DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_key` (`rule_group`, `rule_key`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则配置（座位时段/图书借期/信用规则参数，可配）';
