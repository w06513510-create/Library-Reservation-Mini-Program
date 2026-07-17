package org.dromara.biz.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 看板聚合视图对象（/overview 一次性下发所有卡片 + 图表数据）
 * <p>提炼自跑腿 ErrandDashboardVo：一个 overview 标量 Map（KPI 卡片）+ 若干 List&lt;Map&gt;（图表系列）。
 * <p>用 Map/List&lt;Map&gt; 松结构，SQL 里的驼峰别名即 key；数值可能被序列化成字符串，前端用 Number() 兜底。
 * 强类型偏好者可把各 List 换成具体 record/DTO。
 *
 * @author ruoyi-template
 */
@Data
public class BizDashboardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 核心指标概览（一条 SQL 多聚合）——KPI 卡片数据。
     * 例：total / today / week / completed / cancelled / totalAmount / activeUsers / completionRate
     */
    private Map<String, Object> overview;

    /** 状态分布：{status, value} —— 饼图 */
    private List<Map<String, Object>> statusDist;

    /** 分类分布：{category, value} —— 柱图/饼图 */
    private List<Map<String, Object>> categoryDist;

    /** 近7日趋势：{date:'yyyy-MM-dd', count, amount} —— 折线图 */
    private List<Map<String, Object>> trend;

    /** 排行榜 Top10：{name, value, count} —— 横向柱图 */
    private List<Map<String, Object>> rank;

}
