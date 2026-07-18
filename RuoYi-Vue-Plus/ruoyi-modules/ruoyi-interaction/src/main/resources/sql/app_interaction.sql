-- ============================================================
-- 通用互动：app_interaction 建表脚本（ruoyi-interaction 模块）
-- 一条 = 一个用户对一个业务对象(biz_type/biz_id)的一种动作(favorite/like/follow)。
-- 取消互动为物理删除（无审计价值），故无 del_flag / 逻辑删除。
-- ============================================================
drop table if exists app_interaction;
create table app_interaction (
  id          bigint       not null                comment '主键',
  tenant_id   varchar(20)  default '000000'        comment '租户编号',
  user_id     bigint       not null                comment '发起用户(app_user)',
  action      varchar(20)  not null                comment '动作: favorite/like/follow',
  biz_type    varchar(50)  not null                comment '业务类型(product/post/user...)',
  biz_id      bigint       not null                comment '业务对象ID(关注人时为目标userId)',
  create_dept bigint                               comment '创建部门',
  create_by   bigint                               comment '创建者',
  create_time datetime                             comment '创建时间',
  update_by   bigint                               comment '更新者',
  update_time datetime                             comment '更新时间',
  primary key (id),
  unique key uk_uabb (user_id, action, biz_type, biz_id),
  key idx_target (action, biz_type, biz_id),
  key idx_mine (user_id, action, biz_type)
) engine=innodb comment='通用互动(收藏/点赞/关注)';
