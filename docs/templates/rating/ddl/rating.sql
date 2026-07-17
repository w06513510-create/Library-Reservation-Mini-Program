-- ============================================================
-- 通用「评价 / 打分」模板 DDL（1-5 星 + 可双向）
-- 提炼自跑腿 errand_evaluation（订单双向评价：学生评跑腿员 / 跑腿员评学生，uk 一单一评）
--            宿舍报修 evaluate（el-rate 单向打分，直接写在工单行上——退化形态）。
--
-- 通用做法：独立评价表，biz_type + biz_id 定位被评业务，eval_role 定方向，唯一约束保证"一单一评"。
-- 用法：复制本文件，把表名 rating 换成你的业务（或直接用 rating 作为跨业务通用评价表）。
-- ============================================================
drop table if exists rating;
create table rating (
  id            bigint       not null                   comment '主键',
  tenant_id     varchar(20)  default '000000'           comment '租户编号',
  biz_type      varchar(32)  not null                   comment '业务类型(order/repair/course...一表多业务时区分)',
  biz_id        bigint       not null                   comment '被评业务主键(订单/工单/...的ID)',
  biz_no        varchar(32)                             comment '业务单号(冗余,便于展示/检索,可空)',
  eval_role     tinyint      default 1                  comment '评价方向(1甲评乙,如学生评跑腿员;2乙评甲;单向评价固定填1)',
  from_user_id  bigint       not null                   comment '评价人',
  to_user_id    bigint                                  comment '被评价人(单向评价指向服务方;可空)',
  score         tinyint      not null                   comment '评分(1-5星)',
  content       varchar(500)                            comment '评价内容',
  is_default    tinyint      default 0                  comment '是否系统默认好评(0用户评 1逾期未评系统默认;可空)',
  create_dept   bigint                                  comment '创建部门',
  create_by     bigint                                  comment '创建者',
  create_time   datetime                                comment '创建时间',
  update_by     bigint                                  comment '更新者',
  update_time   datetime                                comment '更新时间',
  del_flag      char(1)      default '0'                comment '删除标志(0存在 2删除)',
  primary key (id),
  -- 一单一评：同一业务、同一对象、同一方向只能有一条（并发安全兜底；注意软删行仍占用唯一键）
  unique key uk_biz_role (biz_type, biz_id, eval_role),
  key idx_rating_to_user (to_user_id),
  key idx_rating_tenant (tenant_id)
) engine=innodb comment='通用评价/打分表';
