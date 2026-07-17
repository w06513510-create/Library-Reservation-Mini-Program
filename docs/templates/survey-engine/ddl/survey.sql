-- ============================================================
-- 通用「问卷 / 测评 / 考试」引擎 DDL（统一一份，取代抄 3 份）
-- 提炼自宿舍 ruoyi-dorm 的 dorm_exam_*（有标准答案+评分）、dorm_survey_*（匿名+无评分）、
--            dorm_questionnaire（画像题库）—— 这三套是近重复实现，抽成一套可配置引擎。
--
-- 一套表覆盖三种用法，靠 survey 主表的两个开关区分：
--   objective=1 有标准答案+客观题自动评分（考试/测验）；objective=0 无标准答案（调查/问卷）
--   anonymous=1 匿名作答（answer.respondent_id 置空）；anonymous=0 实名
-- 用法：复制本文件，把表名前缀 survey 换成你的业务（如 club_vote、safety_exam），在项目库执行。
-- ============================================================

-- ---------- 主表：问卷/试卷 ----------
drop table if exists survey;
create table survey (
  id            bigint       not null                  comment '主键',
  tenant_id     varchar(20)  default '000000'          comment '租户编号',
  title         varchar(128) not null                  comment '标题',
  description   varchar(500)                            comment '说明/须知',
  objective     tinyint      default 0                 comment '是否客观题有标准答案(0调查问卷 1考试测验;=1才自动评分)',
  anonymous     tinyint      default 0                 comment '是否匿名(0实名 1匿名;=1时作答不记名)',
  allow_repeat  tinyint      default 0                 comment '是否允许重复作答(0否 1是)',
  total_score   int                                    comment '总分(objective=1有意义)',
  pass_score    int          default 60                comment '及格分(objective=1有意义)',
  duration      int          default 0                 comment '时长(分钟,0不限时)',
  start_time    datetime                               comment '开始时间',
  end_time      datetime                               comment '结束时间',
  status        tinyint      default 0                 comment '状态(0草稿 1进行中/已发布 2已结束/停用 3已归档)',
  create_dept   bigint                                 comment '创建部门',
  create_by     bigint                                 comment '创建者',
  create_time   datetime                               comment '创建时间',
  update_by     bigint                                 comment '更新者',
  update_time   datetime                               comment '更新时间',
  del_flag      char(1)      default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_survey_status (status),
  key idx_survey_tenant (tenant_id)
) engine=innodb comment='问卷/测评/试卷主表';

-- ---------- 题目表 ----------
drop table if exists survey_question;
create table survey_question (
  id             bigint        not null                 comment '主键',
  tenant_id      varchar(20)   default '000000'         comment '租户编号',
  survey_id      bigint        not null                 comment '所属问卷/试卷',
  question_text  varchar(500)  not null                 comment '题干',
  question_type  tinyint       not null                 comment '题型(0单选 1多选 2判断 3填空 4评分)',
  options        varchar(1000)                          comment '选项JSON [{"key":"A","text":"..."}];填空/评分可空',
  correct_answer varchar(64)                            comment '标准答案(客观题:单选A;多选A,C;判断1/0;可空)',
  score          int                                    comment '本题分值(objective=1时有意义;可空)',
  required       tinyint       default 1                comment '是否必答(0否 1是)',
  order_num      int           default 0                comment '题序',
  create_dept    bigint                                 comment '创建部门',
  create_by      bigint                                 comment '创建者',
  create_time    datetime                               comment '创建时间',
  update_by      bigint                                 comment '更新者',
  update_time    datetime                               comment '更新时间',
  del_flag       char(1)       default '0'              comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_sq_survey (survey_id),
  key idx_sq_tenant (tenant_id)
) engine=innodb comment='问卷/试卷题目';

-- ---------- 作答明细表（每人每题一行） ----------
drop table if exists survey_answer;
create table survey_answer (
  id            bigint        not null                  comment '主键',
  tenant_id     varchar(20)   default '000000'          comment '租户编号',
  record_id     bigint        default 0                 comment '作答会话(考试用survey_record.id;调查可空/0)',
  survey_id     bigint        not null                  comment '所属问卷/试卷',
  question_id   bigint        not null                  comment '题目',
  respondent_id bigint                                  comment '作答人(匿名为空)',
  answer_value  varchar(500)                            comment '答案(选项key / 评分值 / 文本)',
  is_correct    tinyint                                 comment '是否正确(客观题;0否 1是;可空)',
  score         int                                     comment '本题得分(客观题;可空)',
  submit_time   datetime                                comment '提交时间',
  create_dept   bigint                                  comment '创建部门',
  create_by     bigint                                  comment '创建者',
  create_time   datetime                                comment '创建时间',
  update_by     bigint                                  comment '更新者',
  update_time   datetime                                comment '更新时间',
  del_flag      char(1)       default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_sa_survey (survey_id),
  key idx_sa_question (question_id),
  key idx_sa_record (record_id),
  key idx_sa_tenant (tenant_id)
) engine=innodb comment='问卷/试卷作答明细';

-- ---------- 作答会话表（可选：考试型才需要，一次作答=一条记录，含总分/是否及格/第几次） ----------
-- 调查/问卷是"扁平作答"，不需要本表；考试需要"一次交卷=一条记录 + 明细预建行判分"时才建。
drop table if exists survey_record;
create table survey_record (
  id            bigint       not null                   comment '主键',
  tenant_id     varchar(20)  default '000000'           comment '租户编号',
  survey_id     bigint       not null                   comment '所属试卷',
  respondent_id bigint       not null                   comment '作答人(考试不匿名)',
  start_time    datetime                                comment '开始时间',
  submit_time   datetime                                comment '交卷时间',
  score         int                                     comment '得分',
  is_passed     tinyint                                 comment '是否及格(0否 1是)',
  attempt_no    int          default 1                  comment '第几次作答',
  duration_used int                                     comment '用时(秒)',
  status        tinyint      default 0                  comment '状态(0进行中 1已交卷 2已评分)',
  create_dept   bigint                                  comment '创建部门',
  create_by     bigint                                  comment '创建者',
  create_time   datetime                                comment '创建时间',
  update_by     bigint                                  comment '更新者',
  update_time   datetime                                comment '更新时间',
  del_flag      char(1)      default '0'                comment '删除标志(0存在 2删除)',
  primary key (id),
  key idx_sr_survey (survey_id),
  key idx_sr_respondent (respondent_id),
  key idx_sr_tenant (tenant_id)
) engine=innodb comment='考试作答会话(可选)';
