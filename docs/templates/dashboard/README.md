# 看板 / 数据概览 模板（/overview 聚合 + echarts）

> 提炼自：跑腿 `ErrandDashboardController` + `plus-ui/src/views/errand/dashboard/index.vue`（`/data` 聚合 + 5 图），
> 宠物 `PetStatsController`（同形态、无 XML 的 QueryWrapper group-by 变体）。
> 核心范式：**一个 `/overview` 接口一次性下发"KPI 卡片(标量 Map) + 若干图表系列(List&lt;Map&gt;)"，不分页；后端每个板块一条 group-by/多聚合 SQL 组装成 VO；前端 echarts 卡片 + 多图。**

## 目录

```
dashboard/
├── README.md
├── java/
│   ├── controller/BizDashboardController.java     ← GET /biz/dashboard/overview
│   ├── domain/vo/BizDashboardVo.java              ← overview(Map) + statusDist/categoryDist/trend/rank(List<Map>)
│   ├── service/IBizDashboardService.java
│   ├── service/impl/BizDashboardServiceImpl.java  ← 每板块一条查询 → 组装
│   └── mapper/BizDashboardMapper.java             ← 只读聚合 Mapper(不继承 BaseMapperPlus)
├── mapper-xml/BizDashboardMapper.xml              ← 5 段聚合 SQL(多聚合/group-by/date_format/topN)
└── vue/
    ├── api-index.ts   → plus-ui/src/api/biz/dashboard/index.ts
    ├── api-types.ts   → plus-ui/src/api/biz/dashboard/types.ts
    └── index.vue      → plus-ui/src/views/biz/dashboard/index.vue
```

## 复制到哪、改什么

### 后端
1. `java/` 复制到 `ruoyi-modules/ruoyi-<biz>/src/main/java/org/dromara/<biz>/` 对应包。
2. `mapper-xml/BizDashboardMapper.xml` 复制到 `ruoyi-modules/ruoyi-<biz>/src/main/resources/mapper/<biz>/`（该目录被 `application.yml` 的 `mybatis-plus.mapperLocations` 扫描，无需改配置）。
3. 全局替换：包名 `org.dromara.biz`、路径 `/biz/dashboard`、权限 `biz:dashboard:view`、表名 `biz_order` 及列名/状态码。
4. **改 SQL 是主要工作**：把示例表 `biz_order` 和 `status/category/total_amount/owner_id/create_time` 换成你的真实表/列；状态码 `5/6/8` 换成你的业务状态。XML 里 `>=` 写成 `&gt;=`。

### 前端
1. 三个前端文件按注释放到 `plus-ui/src/api/biz/dashboard/` 与 `plus-ui/src/views/biz/dashboard/`。
2. `index.vue` 里改：`metricCards` 的标签/字段、`statusName` 状态映射（建议换成 `proxy.useDict('你的状态字典')` 驱动）、各图标题。
3. 在 `sys_menu` 配一个菜单指向 `biz/dashboard/index`，并给 `biz:dashboard:view` 按钮权限。

## 通用 vs 按业务改

| 部分 | 通用（直接用） | 按业务改 |
|---|---|---|
| Controller `/overview` 单接口、`R<Vo>`、无分页 | ✅ | 需时间范围就加 `@RequestParam beginTime/endTime` 透传 mapper |
| Service"每板块一条查询→组装"、null 兜底 | ✅ | 板块增减 |
| VO：`overview` 标量 Map + 图表 List&lt;Map&gt; | ✅ 结构 | 字段名随 SQL 别名走 |
| Mapper 多聚合 `SUM(CASE)` / group-by / `date_format` 分桶 / left join topN | ✅ 范式 | 表名、列名、状态码、维度 |
| 前端 echarts init/setOption/resize/dispose 生命周期、`num()` 兜底、7 天零填充 | ✅ | 图种类/标题/卡片 |

## echarts 用法要点（对齐基座）

- `import * as echarts from 'echarts'`；`echarts.init(ref, 'macarons')`（`macarons` 是基座内置主题，见 `monitor/cache/index.vue`）。
- 每图一个 `ref<HTMLElement>` + 一个 `ECharts` 实例句柄；`onMounted` 里 `loadData → nextTick → initCharts → renderAll`。
- `window.addEventListener('resize', handleResize)` 里逐图 `resize()`；`onBeforeUnmount` 里 `dispose()` 并置 null，防内存泄漏（基座 cache 页只加了 resize，没 dispose；本模板补齐更稳）。
- 后端数值可能被序列化成字符串，前端一律 `Number()`/`num()` 兜底。
- 趋势折线做了"最近 7 天零填充"，避免某天无数据时断线。

## 两种后端风格（按需选）

- **本模板 = 跑腿风格**：独立聚合 Mapper + XML 手写聚合 SQL，返回 `Map`/`List<Map>`。适合聚合逻辑复杂、跨表 join。
- **宠物风格（无 XML）**：复用实体已有 Mapper，用 `QueryWrapper.select("col","count(*) AS cnt").groupBy("col")` + `selectMaps` 拿 group-by，KPI 用 `selectCount` + `Wrappers.lambdaQuery().eq(...)`，金额用 `qw.select("IFNULL(SUM(amount),0) AS total")`。适合简单单表统计、不想写 XML。
