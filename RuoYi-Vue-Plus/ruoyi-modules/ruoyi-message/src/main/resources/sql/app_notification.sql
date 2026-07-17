-- ============================================================
-- 站内消息/通知中心：app_notification 建表脚本（ruoyi-message 模块）
-- 通用系统通知，去业务耦合：业务信息挂到 biz_type/biz_id 两个通用字段。
-- 接收者 receiver_id 为 app_user 主键。执行：在项目库里跑一次即可。
-- ============================================================
drop table if exists app_notification;
create table app_notification (
  id            bigint        not null                  comment '通知ID',
  tenant_id     varchar(20)   default '000000'          comment '租户编号',
  receiver_id   bigint        not null                  comment '接收用户ID(app_user)',
  title         varchar(150)  not null                  comment '标题',
  content       varchar(1000)                           comment '内容',
  biz_type      varchar(50)                             comment '关联业务类型(通用挂载点,如 order/comment/system,可空)',
  biz_id        bigint                                  comment '关联业务ID(通用挂载点,可空)',
  is_read       tinyint       default 0                 comment '是否已读(0未读 1已读)',
  read_time     datetime                                comment '阅读时间',
  create_dept   bigint                                  comment '创建部门',
  create_by     bigint                                  comment '创建者',
  create_time   datetime                                comment '创建时间',
  update_by     bigint                                  comment '更新者',
  update_time   datetime                                comment '更新时间',
  del_flag      char(1)       default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_notify_receiver (receiver_id, is_read, create_time),
  key idx_notify_biz (biz_type, biz_id)
) engine=innodb comment='站内系统通知(通用)';
