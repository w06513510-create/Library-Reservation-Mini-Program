# 通用互动模块设计（收藏 / 点赞 / 关注）ruoyi-interaction

- 日期：2026-07-18
- 状态：待用户过目
- 适用：基座 `ruoyi_template`
- 来源：5 项目里 收藏/点赞/关注 反复出现且各写各的（宠物 `pet_post_like`/`pet_post_favorite`/`pet_follow`、社团设计 `ccm_club_follow`、二手 `csm_product.favorite_count` 占位）。抽成"用户 × 动作 × 业务对象"的通用模块。

## 已定决策（我的取舍，待确认）

| 项 | 决策 | 理由 |
|---|---|---|
| 模块 | 新增 `ruoyi-interaction`（包 `org.dromara.interaction`），依赖 `ruoyi-app` | 与 message/pay 一致；业务模块依赖它即可用 |
| 表 | 单表 `app_interaction`（一条 = 一个用户对一个业务对象的一种动作） | 三种动作共用一张表，最通用 |
| 动作 | `action` 存字符串常量：`favorite`(收藏) / `like`(点赞) / `follow`(关注) | 简单；`InteractionAction` 常量类约束 |
| 计数 | 从 `app_interaction` `COUNT` 计算，**不改业务表、不预置 like_count 冗余列** | 通用模块不碰业务表；高并发可业务侧自行缓存/冗余 |
| 关注 | 关注用户 = `action=follow, biz_type=user, biz_id=目标userId`；也可关注任意业务对象 | biz_type/biz_id 天然支持"关注人"和"关注物" |
| 防重 | 唯一键 `uk(user_id, action, biz_type, biz_id)` | 一个用户对一个对象一种动作只一条 |

## 表 DDL

```sql
drop table if exists app_interaction;
create table app_interaction (
  id          bigint       not null                comment '主键',
  tenant_id   varchar(20)  default '000000'        comment '租户编号',
  user_id     bigint       not null                comment '发起用户(app_user)',
  action      varchar(20)  not null                comment '动作: favorite/like/follow',
  biz_type    varchar(50)  not null                comment '业务类型(如 product/post/user)',
  biz_id      bigint       not null                comment '业务对象ID(关注人时为目标userId)',
  create_time datetime                             comment '创建时间',
  del_flag    char(1)      default '0'             comment '删除标志(0存在 2删除)',
  primary key (id),
  unique key uk_uabb (user_id, action, biz_type, biz_id, del_flag),
  key idx_target (action, biz_type, biz_id),
  key idx_mine (user_id, action, biz_type)
) engine=innodb comment='通用互动(收藏/点赞/关注)';
```

## 服务 `IInteractionService`

- `boolean toggle(userId, action, bizType, bizId)` —— 幂等开关：无则加返回 true，有则删返回 false。
- `boolean has(userId, action, bizType, bizId)`
- `long count(action, bizType, bizId)` —— 某对象被多少人 收藏/点赞/关注。
- `Set<Long> hasBatch(userId, action, bizType, bizIds)` —— 列表渲染：这批里我操作过哪些。
- `Map<Long,Long> countBatch(action, bizType, bizIds)` —— 这批各自的计数。
- `TableDataInfo<Long> pageMyBizIds(userId, action, bizType, pageQuery)` —— 我的收藏/点赞/关注对象ID分页（业务据此查详情）。
- 关注便捷（`biz_type=user`）：`pageFollowing/pageFollowers(userId, pageQuery)` → `AppUserVo`（join `app_user`）；`followingCount/followerCount(userId)`。

## C 端接口 `/app/interaction/*`（`@SaCheckLogin` + `AppLoginHelper.getUserId()` 归属）

| 方法 | 路径 | 入参 | 返回 |
|---|---|---|---|
| POST | `/app/interaction/toggle` | `{action,bizType,bizId}` | `R<{active, count}>` |
| GET | `/app/interaction/has` | `action,bizType,bizId` | `R<Boolean>` |
| GET | `/app/interaction/count` | `action,bizType,bizId` | `R<Long>` |
| GET | `/app/interaction/my/page` | `action,bizType,pageNum,pageSize` | `TableDataInfo<Long>`（bizId） |
| GET | `/app/interaction/following/page` | `pageNum,pageSize` | `TableDataInfo<AppUserVo>` |
| GET | `/app/interaction/followers/page` | `pageNum,pageSize` | `TableDataInfo<AppUserVo>` |

业务模块也可直接注入 `IInteractionService` 用 `hasBatch/countBatch` 给自己的列表拼"我赞过没 + 赞数"。

## 前端

- uni-app：`composables/useInteraction.ts`（toggle + 本地 active/count 态）+ 通用按钮片段（❤/⭐/关注，点一下 toggle 翻转）；`pages/mine/favorites.vue` 用 `useList` 展示我的收藏（示例）。
- plus-ui：无（互动是 C 端能力）。管理端只读统计 YAGNI 略。

## YAGNI（不做）
业务表加冗余计数列；互动触发通知（业务自行 `NotificationHelper.send`）；"谁赞了"带头像明细（favorite/like 返回 bizId；follow-user 才返回 `AppUserVo`）；点赞排行榜。

## 验证
- JUnit `@SpringBootTest`（真实 DB）：toggle 幂等（同参两次 = 加/删、count 0↔1）、has、hasBatch、countBatch、pageMyBizIds、follow → followers/following（返回 AppUserVo）。
- 全 reactor `mvn compile`；uni-app `build:h5`。

## 落地
分支 `feat/interaction`，完成即推；验证过 `--no-ff` 合并 main；更新台账 `docs/通用能力清单.md` + `docs-site` 加一页「⑨ 通用互动」。
