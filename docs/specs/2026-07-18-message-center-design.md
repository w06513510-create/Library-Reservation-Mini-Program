# 站内消息/通知中心设计（ruoyi-message）

- 日期：2026-07-18
- 状态：待用户 review
- 分支：`feat/msg-center`（从 main，不自动合并）
- 适用：基座 `ruoyi_template`（后端 RuoYi-Vue-Plus 5.6.2 + uni-app 骨架）
- 依赖前置：① C 端接入基座（模块 `ruoyi-app`，`org.dromara.app`，`AppLoginHelper`）已在 main。
- 来源：横向调研 5 个毕设项目——二手/跑腿/宠物都各写了一遍"站内通知 + 私信 + send 触发点 + 我的消息页"。抽成通用模块，去业务耦合，复制即用。

## 定位与决策（我的取舍，待确认）

| 项 | 决策 | 理由 |
|---|---|---|
| 模块落法 | 新增独立模块 `ruoyi-message`（包 `org.dromara.message`），依赖 `ruoyi-app` | 复用 `AppLoginHelper.getUserId()` 拿 C 端用户；与业务模块解耦，任意业务模块依赖 `ruoyi-message` 即可 send 通知 |
| 通用化 | 表/实体去业务名：`app_notification`（前缀 `app_` 与 `app_user` 一致，非业务名）、`app_message` | 参考项目里字段耦合了 refType/petId/orderNo，本模块砍成 `biz_type`(varchar)+`biz_id`(bigint) 通用挂载点 |
| 通知模型 | 单向系统通知：`receiver_id` 一个收件人 | 系统→用户，最常见；群发由调用方循环 send |
| 私信模型 | 扁平存储 `app_message(from_id,to_id,content)`，会话按 (from,to) 无序对归并 | 无需 conversation 表；参考项目均如此，YAGNI |
| 复用核心 | 静态 `NotificationHelper.send(receiverId,title,content,bizType,bizId)` | 供任意业务触发点一行调用；静默容错 + `REQUIRES_NEW` 事务隔离，不污染主事务 |
| 越权防护 | 所有 C 端读写强制用 `AppLoginHelper.getUserId()` 作过滤/CAS 条件 | 只能读写属于自己的记录；标记已读用 `where id=? and owner=?` CAS |
| 管理端 | 提供 `plus-ui` 一个通知发送/列表页 + api | 简单即可；权限走 `@SaCheckPermission`，内置 admin(`*:*:*`) 可直接用，未附菜单/权限 SQL（见风险） |
| 未读 | 通知未读数 = `is_read=0` 计数；私信未读数 = `to_id=me and is_read=0` | 与参考一致 |

## YAGNI 边界（参考项目有，本模块不做）
- 会话/conversation 独立表、消息撤回/删除、已送达回执、@提及、群聊/群通知表。
- 推送渠道（微信订阅消息/APP push/短信/邮件）——`send` 只落库，渠道由业务方另接。
- 通知模板引擎、富文本、附件、分类 tab 配置化。
- 私信图片/表情消息（`content` 纯文本，图片由业务方把 OSS url 塞进 content 约定，前端自行渲染，本期不做）。
- 消息置顶/免打扰/黑名单。
- 管理端菜单/权限 SQL 自动注入（需运维在菜单管理里手动加，或后续补 SQL）。

## 一、后端设计（ruoyi-message）

### 模块与 pom 三处接线（照 ruoyi-app）
1. 新建 `RuoYi-Vue-Plus/ruoyi-modules/ruoyi-message/`（pom 抄 ruoyi-app，额外依赖 `ruoyi-app`）。
2. 根 `pom.xml` 的 `dependencyManagement` 加 `ruoyi-message`。
3. `ruoyi-modules/pom.xml` 的 `<modules>` 加 `ruoyi-message`。
4. `ruoyi-admin/pom.xml` 的 `<dependencies>` 加 `ruoyi-message`。

包结构：
```
org.dromara.message
├── controller/app/AppNoticeController.java     // /app/notice/*   (C端, @SaCheckLogin)
├── controller/app/AppMessageController.java     // /app/message/* (C端, @SaCheckLogin)
├── controller/NotificationController.java       // /message/notification/* (管理端, @SaCheckPermission)
├── domain/AppNotification.java                  // extends TenantEntity
├── domain/AppMessage.java                       // extends TenantEntity
├── domain/bo/NotificationSendBo.java            // 管理端发送入参(校验)
├── domain/bo/NotificationQueryBo.java           // 管理端查询条件
├── domain/bo/MessageSendBo.java                 // C端发私信入参(校验)
├── domain/vo/AppNotificationVo.java             // @AutoMapper
├── domain/vo/AppMessageVo.java                  // @AutoMapper
├── domain/vo/ConversationVo.java                // 会话列表聚合视图(非表)
├── mapper/AppNotificationMapper.java            // BaseMapperPlus
├── mapper/AppMessageMapper.java                 // BaseMapperPlus
├── service/INotificationService.java / impl
├── service/IMessageService.java / impl
└── utils/NotificationHelper.java                // 静态 send
```

### 建表 DDL（`src/main/resources/sql/`）
`app_notification`（系统通知）：`id, receiver_id(app_user), title, content, biz_type(varchar), biz_id(bigint), is_read(0/1), read_time, tenant_id, create_by/create_time/update_*, del_flag`。
`app_message`（私信）：`id, from_id, to_id, content, is_read(0/1), tenant_id, create_by/create_time/update_*, del_flag`；会话按 (from,to) 归并。
- 两表均 `extends TenantEntity`（继承 `create_time/tenant_id/create_by/...`），`del_flag` 逻辑删除。
- 索引：通知 `idx (receiver_id, is_read, create_time)`；私信 `idx (to_id, is_read)`、`idx (from_id, to_id, create_time)`。

### NotificationHelper（核心复用点）
```java
NotificationHelper.send(receiverId, title, content, bizType, bizId);
```
- 静态方法，内部 `SpringUtils.getBean(INotificationService.class).send(...)`。
- 静默容错：全程 try/catch，任何异常只记 `log.warn`，绝不外抛，绝不破坏调用方主流程。
- 不污染主事务：`INotificationService.send` 标注 `@Transactional(propagation = REQUIRES_NEW)`，
  在独立事务中落库——调用方事务回滚不影响已发通知，通知落库失败也不 taint 调用方事务（不会 UnexpectedRollbackException）。
- `receiverId == null` 直接返回 null（不发）。
- 权衡：REQUIRES_NEW 下，若主业务随后回滚，通知仍留存（可能出现"为已回滚业务发的通知"）。此为
  "不污染主事务"的正常代价，可接受；若业务需强一致，可在业务提交后再 send。

### C 端接口（`/app/notice/*`、`/app/message/*`，均 `@SaCheckLogin` + `AppLoginHelper.getUserId()` 强制归属）
通知：
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/notice/list` | 我的通知分页（`receiver_id=me`，倒序） |
| GET | `/app/notice/unreadCount` | 我的未读通知数 |
| PUT | `/app/notice/read/{id}` | 标记单条已读（CAS：`id=? and receiver_id=me`） |
| PUT | `/app/notice/readAll` | 我的全部未读置已读 |

私信：
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/message/conversations` | 我的会话列表（按对端聚合，最近在前，带对端昵称/头像 + 未读数） |
| GET | `/app/message/chat?peerId=&pageNum=&pageSize=` | 与某对端的消息分页（并把对端发我的未读置已读） |
| POST | `/app/message/send` | 发私信 `{toId, content}`（from 强制取 me；禁给自己发；对端须存在） |
| GET | `/app/message/unreadCount` | 我的未读私信总数 |

越权防护：所有查询/更新条件都带 `me = AppLoginHelper.getUserId()`；标记已读用 CAS 带 owner 条件；chat 只能拉与自己相关的消息。

### 管理端接口（`/message/notification/*`，`@SaCheckPermission`）
| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/message/notification/list` | `message:notification:list` | 通知分页（可按 receiverId/bizType/isRead 过滤） |
| POST | `/message/notification/send` | `message:notification:send` | 发送通知 `{receiverId,title,content,bizType,bizId}` |

### 安全放行
无需改 `security.excludes`：`/app/notice/*`、`/app/message/*` 走安全拦截器鉴权（与 `/app/auth/getInfo` 同）；管理端走 sys_user 会话 + 权限。**不放行任何 `/app/**` 通配**。

## 二、前端 uni-app
新增页面（`pages.json` 注册）：
- `pages/message/notice.vue`：通知列表（`useList` + `/app/notice/list`），点击标记已读、顶部"全部已读"。
- `pages/message/conversations.vue`：会话列表（`/app/message/conversations`），点进入 chat。
- `pages/message/chat.vue`：会话详情（`useList` 拉 `/app/message/chat` 分页）+ 底部输入发送。
- `api/message.ts`：封装上述接口。
- 首页/我的页加入口（可选）。
复用现有 `request`/`useList`/`store`。

## 三、集成坑
1. `ruoyi-message` 依赖 `ruoyi-app`，`AppUserMapper`（`org.dromara.app.mapper`）在 `mapperPackage: org.dromara.**.mapper` 扫描范围内，可直接注入以批量取对端昵称/头像。
2. `NotificationHelper` 是静态类，靠 `SpringUtils.getBean` 拿 service，须保证 Spring 已启动（业务运行期调用天然满足）。
3. 通知/私信 send 时若在 C 端登录上下文，`create_by` 由 MetaObjectHandler 按登录填充；跨模块 send 无强要求，`tenant_id` 由 DDL 默认 `000000` 兜底。
4. 管理端权限：内置 admin(`*:*:*`) 可直接访问；普通角色需在菜单管理里配 `message:notification:*` 权限（本期未附 SQL）。

## 四、自检
- 后端：`mvn -q -f RuoYi-Vue-Plus/pom.xml -pl ruoyi-modules/ruoyi-message -am -DskipTests compile`。
- 前端：`npm run --prefix uni-app build:h5`。

## 五、运行时验证（需起后端 + MySQL）
1. 跑 `ruoyi-message/src/main/resources/sql/*.sql` 建表。
2. C 端登录拿 token。
3. 用管理端/或临时在某业务触发点调 `NotificationHelper.send(uid,"标题","内容","order",1L)` 造一条通知 → C 端 `/app/notice/list` 应能看到、`/app/notice/unreadCount` 为 1、`/app/notice/read/{id}` 后未读归零。
4. 两个 C 端账号互发 `/app/message/send`，各自 `/app/message/conversations`、`/app/message/chat` 验证归并与已读、`/app/message/unreadCount`。
