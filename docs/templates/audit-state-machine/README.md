# 通用「审核状态机 + 流转日志」模板

> 提炼自：宿舍 `dorm_repair` + `dorm_repair_log`（多状态工单 + `getRepair`/`writeLog` 双辅助），
> 跑腿 `errand_realname_auth` / `errand_runner_apply`（`0待审 1通过 2驳回` 同构审核 + 审核后差异化副作用）。
> 跑腿 4 处审核里，realname/runnerApply **结构完全一致**、withdraw 是 4 态变体、complaint 不属此形态——本模板取"最通用的三态审核"为核心，并把宿舍的"流转日志 + 状态守卫"范式合进来。

## 这个模板解决什么

任何"提交 → 待审 → 通过 / 驳回（带理由）"的业务：实名认证、跑腿员/骑手申请、提现审核、退款审核、社团入团、内容举报处理……
不必每个业务重抄一遍状态字段、守卫、审核人回填、驳回理由、审核后副作用。核心是三段式：

```
加载并校验存在 → 守卫当前状态(非法流转抛异常) → 改状态+回填审核人/时间 → [可选]append流转日志 → [可选]回调钩子(副作用)
```

## 目录

```
audit-state-machine/
├── README.md                 ← 本文件
├── ddl/biz_audit.sql         ← biz_audit(必需) + biz_audit_log(可选)
└── java/
    ├── constant/AuditStatus.java          ← 0待审/1通过/2驳回 常量
    ├── domain/BizAudit.java               ← 审核单实体(extends TenantEntity)
    ├── domain/BizAuditLog.java            ← 流转日志实体(可选)
    ├── domain/bo/BizAuditBo.java          ← 提交/查询 BO
    ├── domain/vo/BizAuditVo.java          ← 下发 VO
    ├── mapper/BizAuditMapper.java         ← BaseMapperPlus<BizAudit, BizAuditVo>
    ├── mapper/BizAuditLogMapper.java
    ├── service/IBizAuditService.java
    ├── service/AuditCallback.java         ← 审核后副作用扩展点(可选)
    ├── service/impl/BizAuditServiceImpl.java   ← 状态机核心
    └── controller/BizAuditController.java
```

## 复制到哪、改什么

1. **建表**：把 `ddl/biz_audit.sql` 里的表名前缀 `biz` 换成你的业务（如 `club_join`），在项目库执行。
   - 只需简单三态审核 → 只建 `biz_audit`，并删掉 `BizAuditLog.java`/`BizAuditLogMapper.java` 及 Service 里的 `writeLog`/`queryLogPage`。
   - 需要多状态工单留痕（受理→处理中→完工…）→ 同时建 `biz_audit_log`。
2. **落 Java**：把 `java/` 下所有文件复制到你的模块 `ruoyi-modules/ruoyi-<biz>/src/main/java/org/dromara/<biz>/` 对应包下。
3. **全局替换占位**：
   - 包名 `org.dromara.biz` → `org.dromara.<你的模块>`
   - 类名 `BizAudit*` → 你的业务名（如 `ClubJoinAudit`）
   - 表名 `biz_audit` → 你的表名（改 `@TableName`）
   - 请求路径 `/biz/audit` 和权限前缀 `biz:audit:*` → 你的业务（并在 `sys_menu` 配菜单/按钮权限）
4. **接线**（本仓库 `ruoyi-app` 已在用的标准做法）：模块 pom 抄 `ruoyi-demo`，`ruoyi-modules/pom.xml` 加模块，`ruoyi-admin/pom.xml` 加依赖。Mapper 靠 `@MapperScan("org.dromara.**.mapper")` 自动注册，无需额外配置。

## 通用 vs 按业务改

| 部分 | 通用（直接用） | 按业务改 |
|---|---|---|
| 三态常量 `AuditStatus` | ✅ 0/1/2 | 多状态工单再加码（参考宿舍 repair 的 0~7） |
| `getAudit` + `guardPending` 守卫 | ✅ 直接复用 | 多态时把 `guardPending` 换成"允许的 from 状态"判断 |
| `writeLog` append 日志 | ✅ 直接复用 | 不需留痕就删 |
| 审核人取 `LoginHelper.getUserId()` | ✅ 铁律，勿信前端 | — |
| `content` 申请材料字段 | 占位 varchar(500) | 复杂材料拆列或建子表 |
| `AuditCallback` 回调 | ✅ 机制通用 | **每个 bizType 各写一个 `@Component` 实现**：授角色 / 改 profile 标志 / 退款 / 发通知 |
| `submit` 申请人 | 默认 `LoginHelper.getUserId()`（后台账号） | C 端发起改用 `AppLoginHelper.getUserId()`（见 ruoyi-app） |

## 审核后副作用怎么接（回调钩子）

跑腿里"通过"后的副作用各不相同（实名→改标志；骑手申请→授角色；提现→打款/退款）。本模板把它抽成 `AuditCallback`：

```java
@Component
@RequiredArgsConstructor
public class RunnerApplyAuditCallback implements AuditCallback {
    private final IErrandProfileService profileService;

    @Override public String bizType() { return "runnerApply"; }   // 匹配 biz_audit.biz_type

    @Override public void afterPass(BizAudit audit) {
        // 通过：置 is_runner=1，授"跑腿员"角色（同事务，抛异常会整体回滚）
        profileService.grantRunner(audit.getApplyUserId());
    }
    @Override public void afterReject(BizAudit audit) {
        // 驳回：可选发通知
    }
}
```

`BizAuditServiceImpl` 注入 `List<AuditCallback>`，按 `bizType` 匹配后在 `approve`/`reject` 事务内回调；某类无副作用就不写实现（钩子默认空实现，不报错）。

## 自检要点

- `approve`/`reject` 都在 `@Transactional(rollbackFor = Exception.class)` 内：改状态、写日志、回调要么全成功要么全回滚。
- `reject` 强制校验 `rejectReason` 非空（Controller `@NotBlank` + Service 二次校验）。
- `guardPending` 防并发/重复审核（已审的单再点无效并给友好提示）；DB 层若要更强一致，可再加唯一约束或乐观锁 `version`。
- 权限：`approve` 与 `reject` 共用 `biz:audit:audit`（跑腿同款做法）；如需分权，拆成 `:approve` / `:reject`。
