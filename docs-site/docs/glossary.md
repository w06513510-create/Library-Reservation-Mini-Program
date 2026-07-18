# 术语与约定

| 术语 | 含义 |
|---|---|
| **基座 / `ruoyi_template`** | 干净可跑的 RuoYi-Vue-Plus 5.6.2 模板仓库，新项目复制它起步。 |
| **`app_user`** | C 端（小程序/H5）用户表，独立于后台 `sys_user`。 |
| **`sys_user`** | RuoYi 后台管理用户表。 |
| **Sa-Token loginId 命名空间** | C 端登录后 loginId 形如 `app_user:{id}`，与后台 `sys_user:{id}` 同一套 Sa-Token 但相互隔离；`AppLoginHelper.getUserId()` 靠前缀校验强制 C 端身份。 |
| **`clientid`** | 前后端约定的固定客户端标识串。安全拦截器校验"请求头 `clientid` == 登录时写入 token 的 `clientid`"，不一致抛 `-100`；不查 `sys_client` 表。 |
| **`AppLoginHelper`** | C 端登录助手：签发 token、`getUserId()` 取当前 C 端用户。 |
| **`NotificationHelper.send`** | 站内通知发送助手，供任意业务触发点一行调用（静默容错 + 独立事务）。 |
| **`biz_type` / `biz_id`** | 通用关联挂载点（varchar + bigint），把"这条通知/审核/评价属于哪个业务对象"解耦成两列，不写死外键。 |
| **对平不变式（checkInvariant）** | 钱包可用余额与流水的守恒式 `balance − (Σ入 − Σ出) = 0`，可随时查库自检。 |
| **CAS 防超扣** | 出账用条件 UPDATE `... where balance >= ?`，影响行数=0 即失败，避免读改写竞态。 |
| **幂等键（idempotent_no / out_trade_no）** | 唯一键，保证同一动作（回调重试/查单轮询）只入账一次。 |
| **URL 约定（媒体）** | 业务媒体字段直接存可访问 URL（逗号拼接多图 / 单 URL），不走 ossId。 |
| **`TenantHelper.ignore(...)`** | 临时忽略多租户过滤；登录/无租户上下文时按业务键查库必用。 |
| **运行时模块 vs 代码模板** | 前者（`ruoyi-app/message/pay`）编译即用；后者（`docs/templates/`）复制即用、不参与编译。 |
| **`@ConditionalOnProperty(alipay.app-id)`** | 未配置支付宝时 `AlipayClient` 不装配、不影响应用启动。 |
| **端口** | 后端 8199 / plus-ui 8188 / uni-app H5 dev 5188 / 本文档站 18100。 |

## 源

- `docs/通用能力清单.md` 与各设计稿 `docs/specs/*`
