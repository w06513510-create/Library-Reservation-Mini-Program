-- ============================================================
-- 站内消息/通知中心：app_message 建表脚本（ruoyi-message 模块）
-- 通用私信，扁平存储；会话按无序对 (from_id,to_id) 归并，不建 conversation 表。
-- from_id/to_id 均为 app_user 主键。执行：在项目库里跑一次即可。
-- ============================================================
drop table if exists app_message;
create table app_message (
  id            bigint        not null                  comment '消息ID',
  tenant_id     varchar(20)   default '000000'          comment '租户编号',
  from_id       bigint        not null                  comment '发送人ID(app_user)',
  to_id         bigint        not null                  comment '接收人ID(app_user)',
  content       varchar(2000) not null                  comment '内容(纯文本)',
  is_read       tinyint       default 0                 comment '是否已读(0未读 1已读;站在接收人视角)',
  create_dept   bigint                                  comment '创建部门',
  create_by     bigint                                  comment '创建者',
  create_time   datetime                                comment '创建时间',
  update_by     bigint                                  comment '更新者',
  update_time   datetime                                comment '更新时间',
  del_flag      char(1)       default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_msg_to (to_id, is_read),
  key idx_msg_pair (from_id, to_id, create_time)
) engine=innodb comment='站内私信(通用)';
