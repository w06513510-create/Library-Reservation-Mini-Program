package org.dromara.biz.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 看板聚合 Mapper（只读聚合查询，不继承 BaseMapperPlus）
 * <p>SQL 写在 BizDashboardMapper.xml；返回 Map / List&lt;Map&gt;，驼峰别名即 key。
 * <p>靠 {@code @MapperScan("org.dromara.**.mapper")} 自动注册；@Mapper 可留可去。
 *
 * @author ruoyi-template
 */
@Mapper
public interface BizDashboardMapper {

    /** 核心指标（一条 SQL 多聚合） */
    Map<String, Object> selectOverview();

    /** 状态分布 */
    List<Map<String, Object>> selectStatusDist();

    /** 分类分布 */
    List<Map<String, Object>> selectCategoryDist();

    /** 近7日趋势 */
    List<Map<String, Object>> selectTrend();

    /** 排行榜 Top10 */
    List<Map<String, Object>> selectRank();

}
