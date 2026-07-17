-- ============================================================
-- 通用「审核单 + 流转日志」模板 DDL
-- 提炼自：宿舍 dorm_repair / dorm_repair_log（多状态工单 + 流转轨迹）
--         跑腿 errand_realname_auth / errand_runner_apply（0待审 1通过 2驳回 同构审核）
--
-- 用法：复制本文件，把表名 biz_audit / biz_audit_log 里的 "biz" 换成你的业务前缀
--       （如 club_join、errand_realname、shop_refund）。
--       ① 简单三态审核（提交→通过/驳回）：只需 biz_audit 表。
--       ② 多状态工单（受理/处理中/挂起/完工/评价… 需要留痕）：再加 biz_audit_log 表。
-- 租户号默认 '000000'（单租户）；字段/注释与 ruoyi-app app_user.sql 同款风格。
-- ============================================================

-- ---------- ① 审核单主表（必需） ----------
drop table if exists biz_audit;
create table biz_audit (
  id              bigint        not null                  comment '主键',
  tenant_id       varchar(20)   default '000000'          comment '租户编号',
  biz_type        varchar(32)   not null                  comment '业务类型(一表多用时区分不同审核: realname/runnerApply/refund...)',
  biz_id          bigint                                  comment '关联业务主键(审核对象在别的业务表时填其ID;审核对象即本行时可空)',
  apply_user_id   bigint        not null                  comment '申请人(C端 app_user.id 或后台 sys_user.id)',
  content         varchar(500)                            comment '申请内容/提交材料(纯文本或JSON,按业务改;复杂材料建议拆列或子表)',
  status          tinyint       default 0                 comment '审核状态(0待审 1通过 2驳回)',
  reject_reason   varchar(255)                            comment '驳回原因(status=2 时必填)',
  audit_by        bigint                                  comment '审核人(sys_user.id)',
  audit_time      datetime                                comment '审核时间',
  create_dept     bigint                                  comment '创建部门',
  create_by       bigint                                  comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       bigint                                  comment '更新者',
  update_time     datetime                                comment '更新时间',
  del_flag        char(1)       default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_audit_status (status),
  key idx_audit_apply (apply_user_id),
  key idx_audit_biz (biz_type, biz_id),
  key idx_audit_tenant (tenant_id)
) engine=innodb comment='通用审核单';

-- ---------- ② 流转日志表（可选：多状态/需要审批留痕时才建） ----------
-- 每发生一次状态流转就 append 一行，形成"操作台账"：谁、在什么时候、把状态从 X 改成 Y、动作是什么、意见/理由是什么。
drop table if exists biz_audit_log;
create table biz_audit_log (
  id               bigint       not null                  comment '主键',
  tenant_id        varchar(20)  default '000000'          comment '租户编号',
  audit_id         bigint       not null                  comment '关联审核单(biz_audit.id;若挂在别的业务主表上,改成 biz_id)',
  action           varchar(32)                            comment '动作(submit/approve/reject/accept/finish...业务自定义中文或英文)',
  from_status      tinyint                                comment '原状态',
  to_status        tinyint                                comment '新状态',
  operator_user_id bigint                                 comment '操作人(sys_user.id)',
  remark           varchar(255)                           comment '备注/审批意见/驳回原因',
  action_time      datetime                               comment '操作时间',
  create_dept      bigint                                 comment '创建部门',
  create_by        bigint                                 comment '创建者',
  create_time      datetime                               comment '创建时间',
  update_by        bigint                                 comment '更新者',
  update_time      datetime                               comment '更新时间',
  del_flag         char(1)      default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_auditlog_audit (audit_id),
  key idx_auditlog_tenant (tenant_id)
) engine=innodb comment='审核流转轨迹';
