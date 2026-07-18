-- ============================================================
-- C端接入基座：app_user 建表脚本（ruoyi-app 模块）
-- 通用字段版；实名认证/信用分/业务归属/多角色等业务字段由各业务模块自行加列或加子表。
-- 执行：在项目库里跑一次即可。租户号默认 '000000'（单租户）。
-- ============================================================
drop table if exists app_user;
create table app_user (
  id              bigint        not null                  comment '用户ID',
  tenant_id       varchar(20)   default '000000'          comment '租户编号',
  phone           varchar(20)   not null                  comment '手机号(登录名)',
  password        varchar(100)                            comment '密码(BCrypt; 敏感,不下发)',
  openid          varchar(64)                             comment '微信openid(登录方式预留,可空)',
  unionid         varchar(64)                             comment '微信unionid(预留,可空)',
  nickname        varchar(50)                             comment '昵称',
  avatar          varchar(255)                            comment '头像(OSS url)',
  gender          tinyint       default 0                 comment '性别(0未知 1男 2女)',
  status          tinyint       default 0                 comment '账号状态(0正常 1受限 2封禁)',
  register_time   datetime                                comment '注册时间',
  last_login_time datetime                                comment '最后登录时间',
  create_dept     bigint                                  comment '创建部门',
  create_by       bigint                                  comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       bigint                                  comment '更新者',
  update_time     datetime                                comment '更新时间',
  del_flag        char(1)       default '0'               comment '删除标志(0存在 1删除, MP逻辑删除默认值)',
  primary key (id),
  unique key uk_user_phone (phone, tenant_id),
  key idx_user_openid (openid)
) engine=innodb comment='C端用户(通用接入基座)';
