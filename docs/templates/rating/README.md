# 通用「评价 / 打分」模板（1-5 星，可双向）

> 提炼自：跑腿 `ErrandEvaluationController`（订单双向评价：学生评跑腿员 / 跑腿员评学生，`uk_order_role` 一单一评），
> 宿舍报修 `evaluate`（`el-rate` 单向打分，直接写在工单行上——退化形态）。
> 核心：**独立评价表 + `biz_type`/`biz_id` 定位被评业务 + `eval_role` 定方向 + 唯一约束保证一单一评；评价人与方向服务端解析，不信前端。**

## 目录

```
rating/
├── README.md
├── ddl/rating.sql                       ← biz_type/biz_id/eval_role/score/content + uk_biz_role 一单一评
├── java/
│   ├── domain/Rating.java
│   ├── domain/bo/RatingBo.java          ← 提交只收 bizType/bizId/score/content
│   ├── domain/vo/RatingVo.java
│   ├── mapper/RatingMapper.java
│   ├── service/RatingPartyResolver.java ← 方向/双方解析扩展点(业务耦合部分)
│   ├── service/IRatingService.java
│   ├── service/impl/RatingServiceImpl.java  ← evaluate/getByBizAndRole/avgScore/list
│   └── controller/RatingController.java
├── vue/plus-ui-el-rate.vue              ← plus-ui 评价弹窗 + 只读星级片段
└── uni-app/
    ├── StarRate.vue                     ← 自包含小程序星级组件(无需 uni-ui)
    └── usage-snippet.md                 ← 小程序提交/展示用法
```

## 复制到哪、改什么

1. **建表**：执行 `ddl/rating.sql`。单业务可保留 `rating` 通用表；一表多业务用 `biz_type` 区分。
2. **落 Java**：`java/` 复制到 `ruoyi-modules/ruoyi-<biz>/.../org/dromara/<biz>/`；替换包名 `org.dromara.biz`、路径 `/biz/rating`、权限前缀 `biz:rating`。
3. **前端**：`vue/plus-ui-el-rate.vue` 片段贴进你的列表/详情页；小程序把 `uni-app/StarRate.vue` 放到 `uni-app/src/components/`，按 `usage-snippet.md` 用。
4. **关键：实现 `RatingPartyResolver`**（见下）——这是唯一与你业务强耦合、必须自己写的部分。

## 关键：方向解析（`RatingPartyResolver`）

通用评价服务不认识你的"订单/工单"表，所以把"校验可评价 + 定方向与双方"抽成 `RatingPartyResolver`，每个 `bizType` 各写一个 `@Component`。这段直接照跑腿 `evaluate` 的判角色逻辑写：

```java
@Component
@RequiredArgsConstructor
public class OrderRatingResolver implements RatingPartyResolver {

    private final IErrandOrderService orderService;   // 你的业务订单服务

    private static final int ROLE_STUDENT_TO_RUNNER = 1;
    private static final int ROLE_RUNNER_TO_STUDENT = 2;
    private static final int ORDER_FINISHED = 5;

    @Override public String bizType() { return "order"; }   // 对应 rating.biz_type

    @Override
    public Party resolve(Long bizId, Long currentUserId) {
        ErrandOrder order = orderService.getById(bizId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != ORDER_FINISHED) {
            throw new ServiceException("仅已完成订单可评价");
        }
        // 判角色：当前登录用户是发单方还是接单方 → 定方向与被评价人
        if (currentUserId.equals(order.getStudentId())) {
            return new Party(ROLE_STUDENT_TO_RUNNER, order.getRunnerId(), order.getOrderNo());
        } else if (currentUserId.equals(order.getRunnerId())) {
            return new Party(ROLE_RUNNER_TO_STUDENT, order.getStudentId(), order.getOrderNo());
        }
        throw new ServiceException("无权评价该订单");
    }
}
```

**单向评价**（如宿舍工单只有"用户评服务方"）：`resolve` 里固定 `evalRole=1`，`toUserId` 取处理人，就退化成单向打分。

## 通用 vs 按业务改

| 部分 | 通用（直接用） | 按业务改 |
|---|---|---|
| `rating` 表结构 + `uk_biz_role` 一单一评 | ✅ | 单业务表名可换；`biz_no` 冗余列可删 |
| `RatingServiceImpl`：登录取评价人、两层查重、1-5 校验、`avgScore`、分页 | ✅ 直接复用 | — |
| `RatingBo` 只收 bizType/bizId/score/content | ✅ 防越权 | — |
| `RatingPartyResolver` | 机制通用 | **必须每 bizType 各写一个**：校验状态 + 判角色 |
| 评分统计 `avgScore` 遍历求平均 | ✅ 够用 | 高频/大量数据改为写时维护 `eval_count/good_count`（跑腿做法，见下） |
| plus-ui `el-rate` / uni-app `StarRate` | ✅ | 文案/星数/颜色 |

## 一单一评 & 防重复（两层）

1. **应用层**：`evaluate` 插入前先 `selectCount(bizType, bizId, evalRole)`，命中给友好提示"您已评价过"。
2. **DB 层**：`unique key uk_biz_role (biz_type, biz_id, eval_role)` 并发兜底（两请求同时进来，第二条 insert 撞唯一键失败回滚）。
   - 注意：uk **不含 del_flag**，软删的评价仍占用唯一键（符合"一单一评不可改"语义；若要允许重评需另设计）。

## 评分聚合两种口径

- **读时算（本模板 `avgScore`）**：查该被评价人所有评分求平均。简单，数据量小够用。
- **写时维护（跑腿做法）**：评价落库时用 `LambdaUpdateWrapper.setSql("eval_count = eval_count + 1")`，`score>=4` 再 `good_count = good_count + 1`，把统计维护在用户档案表上。适合高频/榜单场景，避免每次聚合扫描。

## el-rate 属性速查（三处用法共性）

- `v-model` 绑定分值；`:max="5"`。
- 文字标签：`show-text :texts="['很差','较差','一般','满意','非常满意']"`；或数字：`show-score`。
- 只读展示：`:model-value="row.score"` + `disabled`。
- 提交默认值一般预置 5 星。
