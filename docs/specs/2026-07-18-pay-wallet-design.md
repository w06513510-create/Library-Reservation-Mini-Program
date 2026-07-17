# 支付宝充值 + 钱包对平设计（ruoyi-pay）

- 日期：2026-07-18
- 状态：待用户 review 后合并（分支 `feat/pay-wallet`，勿自行合 main）
- 适用：基座 `ruoyi_template`（RuoYi-Vue-Plus 5.6.2），依赖已完成的 ① C 端接入基座（`ruoyi-app` / `org.dromara.app` / `AppUser` / `AppLoginHelper`）。
- 来源：横向抽取宠物（`PetFundServiceImpl` 对平引擎 + `PetRechargeServiceImpl` 支付宝沙箱）、二手（`WalletServiceImpl`/`RechargeServiceImpl`/`CsmFundFlow`）、社团三个毕设项目的资金/充值实现，去业务耦合、通用化、与 `app_user` 绑定后落成基座模块。

## 目标与非目标

**目标**：给基座一套"纯技术、与业务无关"的资金能力——
1. **钱包对平引擎**：每个 C 端用户一个钱包（三层：可用 `balance` / 冻结 `frozen` / 累计充值 `total_recharge`），所有动钱走唯一入口，append-only 流水，幂等 + CAS 防超扣 + 对平不变式自校验。
2. **支付宝充值套件**：下单（trade.page.pay）→ 查单为准（trade.query）→ 异步通知验签（notify），`out_trade_no` 幂等入账，走钱包引擎 `recharge`。**未配置支付宝时提供"模拟即时到账"降级路径**，保证 充值→对平 全链路在无真实沙箱凭证下可验证。

**非目标（YAGNI，本期不做）**：提现 / 分账 / 佣金 / 退款 / 捐赠 / 多币种 / 定时对账任务 / 管理端页面。业务模块（订单、押金等）后续依赖 `ruoyi-pay` 调 `IWalletService` 的 `deduct/freeze/unfreeze` 即可，本模块只提供引擎，不含任何业务语义。

## 决策与取舍

| 项 | 决策 | 理由 |
|---|---|---|
| 落法 | 新增独立模块 `ruoyi-modules/ruoyi-pay`（包 `org.dromara.pay`），依赖 `ruoyi-app` | 与 ① 同构；业务模块只依赖 `ruoyi-pay` 即可动钱，避免各模块 copy 对平逻辑 |
| 钱包三层 | `balance`（可用）/ `frozen`（冻结）/ `total_recharge`（累计充值，单调递增统计位），乐观锁 `version` | 严格按任务要求；`total_recharge` 是统计位不参与对平 |
| 流水表 | 精简 append-only：`id,user_id,direction(1入/2出),amount(正),balance_after,biz_type,biz_no,idempotent_no(uk),remark,create_time` | 严格按任务要求的列；`direction`+正 `amount` 而非带符号 delta |
| 动钱入口 | 只有 `IWalletService.recharge/deduct/freeze/unfreeze` 能改钱，组合动作同 `@Transactional` | 唯一入口，杜绝散落 SQL 改余额 |
| 防超扣 | **数据库条件 UPDATE（CAS）**：`update ... set balance=balance-#{amt} where user_id=#{uid} and balance>=#{amt}`，影响行数=0 即余额不足/并发失败 | 比"读-改-写 + 乐观锁"更强，天然无竞态；`version` 列同时 +1 保留乐观锁语义 |
| 幂等 | 每次动钱先查 `app_fund_flow` 是否已有该 `idempotent_no`（已存在则直接 no-op 返回）；`idempotent_no` 唯一键作并发硬兜底（并发重复时后者 insert 冲突→整事务回滚→不会重复动钱） | 双层：预检查覆盖顺序重试（notify+轮询），唯一键覆盖并发 |
| 租户上下文 | 引擎所有 DB 操作包 `TenantHelper.ignore(...)`，严格按 `user_id`（雪花全局唯一）归属 | notify 路径无登录=无租户上下文；用 `user_id` 定位与租户无关，`ignore` 后自写 CAS 的 `where` 不被租户插件改写。`tenant_id` 列由 DDL 默认 `'000000'` 兜底 |
| 支付宝装配 | 复用已存在的 `ruoyi-common-alipay`（`AlipayConfig @ConditionalOnProperty(prefix="alipay",name="app-id")` + `AlipayProperties`），`ruoyi-pay` 依赖它 | 基座已内置该 common 模块与 `application-alipay.yml.example`，无需在 `ruoyi-pay` 重造 config |
| 未配支付宝 | `AlipayClient` 用 `ObjectProvider` 惰性注入；未配置时 `createRecharge` 返回 `alipayConfigured=false` + 提供 `simulatePaid` 模拟到账 | 应用照常启动；充值→对平 可离线验证 |
| 查单为准 | 到账以 `trade.query` 结果为准；notify 仅验签后触发查单结算，不直接凭 notify 入账 | 本地无公网回调时也能靠查单结算；防伪造 notify |

## 一、数据库设计

三张表，建表脚本 `ruoyi-modules/ruoyi-pay/src/main/resources/sql/pay_wallet.sql`。

### app_wallet（钱包，一个 app_user 一行）
```
id            bigint PK
user_id       bigint  not null  唯一键 uk_wallet_user
balance       decimal(12,2) default 0   可用余额
frozen        decimal(12,2) default 0   冻结余额
total_recharge decimal(12,2) default 0  累计充值(统计位, 不参与对平)
version       int default 0             乐观锁
tenant_id/create_*/update_* 审计列(TenantEntity)
```

### app_fund_flow（资金流水，append-only，只增不改不删）
```
id            bigint PK
user_id       bigint not null           key idx_flow_user
direction     tinyint not null          1入 2出
amount        decimal(12,2) not null     变动额(恒正)
balance_after decimal(12,2) not null     本次操作后 balance(可用) 余额
biz_type      varchar(32)                业务类型 recharge/deduct/freeze/unfreeze/...
biz_no        varchar(64)                业务单号
idempotent_no varchar(96) not null       唯一键 uk_flow_idem 幂等键
remark        varchar(255)
create_time   datetime
```
无 `del_flag`、无 update 列——流水不可变。

### app_recharge（充值单）
```
id            bigint PK
user_id       bigint not null
out_trade_no  varchar(64) not null       唯一键 uk_recharge_out 商户订单号
amount        decimal(12,2) not null
status        tinyint default 0          0待支付 1已到账 2已关闭
channel       varchar(16)                alipay / simulate
trade_no      varchar(64)                支付宝交易号
subject       varchar(128)
pay_time/query_time datetime
tenant_id/create_*/update_* 审计列(TenantEntity)
```

## 二、对平不变式（核心正确性）

**流水 `direction` 语义**：记录的是对**可用余额 `balance`** 的影响。
- `recharge`：`balance += amount`，`total_recharge += amount` → 记 `direction=1(入)`
- `deduct` ：`balance -= amount` → 记 `direction=2(出)`
- `freeze` ：`balance -= amount` 且 `frozen += amount` → 记 `direction=2(出)`（可用减少）
- `unfreeze`：`frozen -= amount` 且 `balance += amount` → 记 `direction=1(入)`（可用增加）

每一次 `balance` 变动**恰好写一条流水**，`balance_after` = 该次操作后的可用余额。

**不变式**：对任一用户，`balance == Σ(direction=1 的 amount) − Σ(direction=2 的 amount)`。

`checkInvariant(userId)` 返回差额 `diff = balance − (Σ入 − Σ出)`，正常恒为 `0`（`frozen` 是可用与冻结之间的搬运，被上面的一进一出抵消，故不进不变式；`total_recharge` 是统计位也不进）。

推演：充值100→ balance=100,Σ入100,Σ出0；冻结30→ balance=70,frozen=30,Σ出30；解冻10→ balance=80,frozen=20,Σ入110；扣款50→ balance=30,Σ出80。`balance=30 == 110−80=30`，diff=0。✓

## 三、幂等 / CAS 如何保证

动钱方法统一流程（均 `@Transactional(rollbackFor=Exception.class)` + 全程 `TenantHelper.ignore`）：
1. 校验 `amount>0`；`getOrCreate(userId)` 确保钱包行存在。
2. **幂等预检查**：`app_fund_flow` 已存在该 `idempotent_no` → 直接返回（no-op），覆盖 notify + 轮询顺序重试。
3. **CAS 条件更新**：出账走 `where balance>=amount`（`freeze` 同理，`unfreeze` 走 `where frozen>=amount`），影响行数=0 → 抛"余额/冻结不足或并发冲突"；入账（`recharge`/`unfreeze` 的 balance+）无条件加。`version=version+1`。
4. 回读钱包拿 `balance_after`，insert 一条流水（携带 `idempotent_no`）。
5. **并发硬兜底**：若两个同 `idempotent_no` 请求并发穿过预检查，二者都做了 CAS 加减，但流水 insert 时 `uk_flow_idem` 唯一键只允许一条，冲突方抛异常 → 整事务回滚 → 其钱包变动被撤销 → 净效果只入账一次。

## 四、支付宝充值流程

- `createRecharge(userId, amount)`：insert 一条 `status=0` 充值单（`out_trade_no = "RC"+雪花`）。
  - 已配置支付宝：`trade.page.pay` 拿到支付表单 HTML，返回 `{outTradeNo, payForm, alipayConfigured=true, channel=alipay}`。
  - 未配置：返回 `{outTradeNo, payForm=null, alipayConfigured=false, channel=simulate}`，前端转调 `simulatePaid`。
- `queryAndSettle(outTradeNo)`（**查单为准**）：`trade.query`，`TRADE_SUCCESS/TRADE_FINISHED` 视为已付；用 **CAS `markPaid`**（`update app_recharge set status=1,... where out_trade_no=? and status=0`）抢占结算权，只有抢到的调 `walletService.recharge(idempotent_no=out_trade_no)` 入账。双层幂等（充值单状态 CAS + 流水唯一键）。
- `handleNotify(params)`：`AlipaySignature.rsaCheckV1` 验签→ 通过且 `trade_status` 成功→ 调 `queryAndSettle`（仍以查单为准，不凭 notify 直接入账）→ 返回 `success`/`failure`。
- `simulatePaid(userId, outTradeNo)`（**降级**）：仅当 `AlipayClient` 未装配时可用（已配置则拒绝，强制走真实支付）；校验单归属 → `markPaid`（channel=simulate）→ `recharge` 入账。用于无沙箱凭证时验证 充值→对平。

## 五、接口

### C 端钱包 `/app/wallet/*`（`@SaCheckLogin`，`AppLoginHelper.getUserId()` 强制归属）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/app/wallet/me` | 我的钱包（`getOrCreate` 后返回 `AppWalletVo`） |
| GET | `/app/wallet/flow/page` | 我的资金流水分页（`PageQuery`→`TableDataInfo<AppFundFlowVo>`） |
| GET | `/app/wallet/checkInvariant` | 我的钱包对平自检，返回差额 `BigDecimal`（0 为平） |

### C 端充值 `/app/recharge/*`
| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/app/recharge/create?amount=` | `@SaCheckLogin` | 创建充值单，返回 `AppRechargeCreateVo` |
| GET | `/app/recharge/query?outTradeNo=` | `@SaCheckLogin` | 主动查单结算（校验单归属当前用户），返回是否已付 |
| POST | `/app/recharge/simulatePaid?outTradeNo=` | `@SaCheckLogin` | 模拟到账（仅未配支付宝时可用，校验归属） |
| GET | `/app/recharge/page` | `@SaCheckLogin` | 我的充值单分页 |
| POST | `/app/recharge/notify` | **放行**（yml excludes） | 支付宝异步通知（服务器回调，无 token），验签后查单结算 |

**安全放行**：`application.yml` 的 `security.excludes` 追加 `- /app/recharge/notify`（仅通知回调；其余 `/app/**` 仍走拦截器 + `AppLoginHelper` 前缀二次校验）。

`deduct/freeze/unfreeze` **不出 C 端 HTTP 接口**（C 端用户不应任意扣自己钱），仅作 `IWalletService` 服务级 API 供后续业务模块调用。

## 六、未配 Alipay 的降级（务必）

- `ruoyi-common-alipay` 的 `AlipayConfig` 用 `@ConditionalOnProperty(prefix="alipay", name="app-id")`：未配 `application-alipay.yml` / 无 `app-id` → 不装配 `AlipayClient` → 应用照常启动。
- `RechargeServiceImpl` 用 `ObjectProvider<AlipayClient>` 惰性取：`getIfAvailable()==null` 即"未配置"。
  - `createRecharge`：未配置返回 `alipayConfigured=false`；`queryAndSettle`：未配置抛 `ServiceException("支付宝未配置...")`。
  - `simulatePaid`：**仅**未配置时可用，走 `channel=simulate` 直接到账 → 全链路 充值→对平 可离线验证。
- 附 `application-alipay.yml.example`（占位符，已存在于 `ruoyi-admin/src/main/resources/`；真实 `application-alipay*.yml` 已 `.gitignore`）。

## 七、三处 pom 接线（照 ruoyi-app）

1. 根 `pom.xml` `<dependencyManagement>` 加 `ruoyi-pay`（`${revision}`）。
2. `ruoyi-modules/pom.xml` `<modules>` 加 `ruoyi-pay`。
3. `ruoyi-admin/pom.xml` `<dependencies>` 加 `ruoyi-pay`。

`ruoyi-pay/pom.xml` 依赖：`ruoyi-app` + `ruoyi-common-alipay` + core/doc/mybatis/security/web/tenant/json/log（多为 `ruoyi-app` 传递，显式声明求稳）。

## 八、验证（运行时，需 MySQL）

1. 执行建表：`ruoyi-modules/ruoyi-app/src/main/resources/sql/app_user.sql` + `ruoyi-modules/ruoyi-pay/src/main/resources/sql/pay_wallet.sql`。
2. 起后端（未配 `application-alipay.yml` 也能启动）。
3. C 端注册 `/app/auth/register` → 登录 `/app/auth/login` 拿 token（带 `clientid` header）。
4. `POST /app/recharge/create?amount=100` → 返回 `alipayConfigured=false` + `outTradeNo`。
5. `POST /app/recharge/simulatePaid?outTradeNo=RC...` → 模拟到账。
6. `GET /app/wallet/me` → `balance=100.00, total_recharge=100.00`。
7. `GET /app/wallet/flow/page` → 一条 `direction=1, amount=100, balance_after=100` 的充值流水。
8. `GET /app/wallet/checkInvariant` → `0`（对平）。
9. （可选）真实沙箱：填 `application-alipay.yml` 后 `create` 返回支付表单，付款后 `GET /app/recharge/query?outTradeNo=` 查单结算，或等 notify。
