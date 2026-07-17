-- ============================================================
-- 支付/钱包基座建表脚本（ruoyi-pay 模块）
-- 依赖 ① C端接入基座 app_user（先执行 ruoyi-app/.../sql/app_user.sql）。
-- 与 app_user 绑定（user_id = app_user.id），去业务耦合、通用化。
-- 执行：在项目库里跑一次即可。租户号默认 '000000'（单租户）。
-- 金额统一 decimal(12,2)。
-- ============================================================

-- 钱包：一个 app_user 一行，三层余额 + 乐观锁
drop table if exists app_wallet;
create table app_wallet (
  id             bigint         not null                 comment '钱包ID',
  user_id        bigint         not null                 comment '所属C端用户ID(app_user.id)',
  balance        decimal(12,2)  not null default 0.00    comment '可用余额',
  frozen         decimal(12,2)  not null default 0.00    comment '冻结余额',
  total_recharge decimal(12,2)  not null default 0.00    comment '累计充值(统计位,不参与对平)',
  version        int            not null default 0       comment '乐观锁版本',
  tenant_id      varchar(20)    default '000000'         comment '租户编号',
  create_dept    bigint                                  comment '创建部门',
  create_by      bigint                                  comment '创建者',
  create_time    datetime                                comment '创建时间',
  update_by      bigint                                  comment '更新者',
  update_time    datetime                                comment '更新时间',
  primary key (id),
  unique key uk_wallet_user (user_id)
) engine=innodb comment='C端用户钱包(三层余额)';

-- 资金流水：append-only（只增不改不删），对平核心
drop table if exists app_fund_flow;
create table app_fund_flow (
  id            bigint        not null                  comment '流水ID',
  user_id       bigint        not null                  comment '所属C端用户ID',
  direction     tinyint       not null                  comment '方向(1入:可用增加 2出:可用减少)',
  amount        decimal(12,2) not null                  comment '变动额(恒正)',
  balance_after decimal(12,2) not null                  comment '本次操作后可用余额',
  biz_type      varchar(32)                             comment '业务类型(recharge/deduct/freeze/unfreeze/...)',
  biz_no        varchar(64)                             comment '业务单号',
  idempotent_no varchar(96)   not null                  comment '幂等键(防重)',
  remark        varchar(255)                            comment '备注',
  create_time   datetime                                comment '创建时间',
  primary key (id),
  unique key uk_flow_idem (idempotent_no),
  key idx_flow_user (user_id)
) engine=innodb comment='资金流水(append-only,对平)';

-- 充值单：out_trade_no 幂等，状态机 0待支付→1已到账/2已关闭
drop table if exists app_recharge;
create table app_recharge (
  id           bigint        not null                  comment '充值单ID',
  user_id      bigint        not null                  comment '所属C端用户ID',
  out_trade_no varchar(64)   not null                  comment '商户订单号',
  amount       decimal(12,2) not null                  comment '充值金额',
  status       tinyint       not null default 0        comment '状态(0待支付 1已到账 2已关闭)',
  channel      varchar(16)                             comment '支付渠道(alipay/simulate)',
  trade_no     varchar(64)                             comment '支付宝交易号',
  subject      varchar(128)                            comment '订单标题',
  pay_time     datetime                                comment '到账时间',
  query_time   datetime                                comment '最近查单时间',
  tenant_id    varchar(20)   default '000000'          comment '租户编号',
  create_dept  bigint                                  comment '创建部门',
  create_by    bigint                                  comment '创建者',
  create_time  datetime                                comment '创建时间',
  update_by    bigint                                  comment '更新者',
  update_time  datetime                                comment '更新时间',
  primary key (id),
  unique key uk_recharge_out (out_trade_no),
  key idx_recharge_user (user_id)
) engine=innodb comment='充值单(支付宝/模拟)';
