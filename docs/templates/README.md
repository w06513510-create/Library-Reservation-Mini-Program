# 可复用脚手架 / 代码模板索引

本目录收录从 5 个毕设项目（社团 / 跑腿 / 二手 / 宿舍 / 宠物）横向调研提炼出的**业务耦合较高、更适合做成"复制即用"代码模板**的能力。
与基座里那些"运行时组件/模块"（如 `ruoyi-app` C 端接入基座、通用媒体上传组件）不同——这一梯队**不做成可编译模块**，而是给出
**DDL + Java 骨架 + 前端片段 + 说明文档**，新项目按需复制到自己的业务模块里改造。

> 命名占位约定：Java 包名用 `org.dromara.<biz>`（示例里写 `org.dromara.biz` / `org.dromara.survey`），
> 类名/表名/路径/权限前缀均为占位，复制后全局替换成你的业务名。代码风格对齐基座 `ruoyi-app`
> （`BaseMapperPlus`、`@AutoMapper`、`TenantEntity`、`PageQuery`/`TableDataInfo`、`LoginHelper`、`@SaCheckPermission`）。

## 模板一览

| # | 模板 | 用途 | 关键文件 | 提炼来源 |
|---|---|---|---|---|
| ⑤ | [审核状态机 + 流转日志](./audit-state-machine/) | "提交→待审→通过/驳回(带理由)"通用审核单；多状态工单可加流转轨迹留痕；审核后副作用走回调钩子 | `ddl/biz_audit.sql`、`service/impl/BizAuditServiceImpl.java`（状态机核心）、`service/AuditCallback.java`（回调） | 宿舍 `dorm_repair`+`dorm_repair_log`；跑腿 realname/runnerApply 等 4 处同构审核 |
| ⑥ | [看板 / 数据概览](./dashboard/) | `/overview` 一次性聚合下发 KPI 卡片 + 多图；echarts 页模板 | `mapper-xml/BizDashboardMapper.xml`（聚合 SQL 范式）、`vue/index.vue`（echarts 卡片+4图） | 跑腿 `ErrandDashboardController` + `dashboard/index.vue`；宠物 `PetStatsController` |
| ⑦ | [问卷 / 测评 / 考试引擎](./survey-engine/) | 一套表覆盖考试(有答案+自动评分)/调查(匿名+统计)/问卷；Excel 导题 | `ddl/survey.sql`、`utils/SurveyScoreUtils.java`（客观题判分）、`service/impl/SurveyServiceImpl.java` | 宿舍 `dorm_exam_*`/`dorm_survey_*`/`dorm_questionnaire`（原抄 3 份，抽成 1 份） |
| ⑧ | [评价 / 打分](./rating/) | 1-5 星评价，可双向；一单一评唯一约束；plus-ui el-rate + uni-app 星级 | `ddl/rating.sql`（`uk_biz_role`）、`service/impl/RatingServiceImpl.java`、`uni-app/StarRate.vue` | 跑腿 `ErrandEvaluationController`（双向）；宿舍报修 `evaluate`（单向 el-rate） |

## 每个模板的价值一句话

- **⑤ 审核状态机**：任何审批/工单业务不必再重抄状态字段+守卫+审核人回填+驳回理由+副作用，核心三段式（加载校验→状态守卫→改状态+日志+回调）拿来即用。
- **⑥ 看板**：把"多聚合 SQL + echarts 多图"这套八股固化，改表名列名和图种类就能出一个运营大屏。
- **⑦ 问卷引擎**：用 `objective`/`anonymous` 两个开关统一考试/调查/问卷，客观题自动评分与 Excel 导题范式现成。
- **⑧ 评价**：独立评价表 + 方向解析器 + 一单一评唯一约束，双向/单向评分都覆盖，前端 web/小程序星级片段齐备。

## 怎么用（通用步骤）

1. 建表：执行模板 `ddl/*.sql`（表名前缀替换成你的业务）。
2. 落 Java：把 `java/` 下文件复制到 `ruoyi-modules/ruoyi-<biz>/src/main/java/org/dromara/<biz>/` 对应包；全局替换占位（包名/类名/表名/路径/权限）。
3. 前端：`vue/` / `uni-app/` 片段按注释放到 `plus-ui` / `uni-app` 对应目录。
4. 接线（按基座 `ruoyi-app` 同款做法）：模块 pom 抄 `ruoyi-demo`，`ruoyi-modules/pom.xml` 加模块，`ruoyi-admin/pom.xml` 加依赖；Mapper 靠 `@MapperScan("org.dromara.**.mapper")` 自动注册；mapper XML 放到被扫描的 `resources/mapper/<biz>/`。
5. 配菜单/权限：在 `sys_menu` 配菜单与按钮权限（对应各模板里的 `xxx:xxx:*` 权限串）。

> 每个模板目录下的 `README.md` 有更细的"复制到哪、改什么、哪些通用哪些按业务改、自检要点"。

## 相关设计文档（基座运行时能力，非本目录模板）

- [C 端接入基座设计（ruoyi-app + uni-app 骨架）](../specs/2026-07-17-app-access-base-design.md)
- [通用媒体上传组件设计](../specs/2026-07-17-media-upload-components-design.md)
