package org.dromara.biz.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.biz.domain.vo.BizDashboardVo;
import org.dromara.biz.mapper.BizDashboardMapper;
import org.dromara.biz.service.IBizDashboardService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 看板聚合 Service 实现
 * <p>范式（提炼自跑腿 ErrandDashboardServiceImpl）：每个板块一条小的 group-by / 多聚合查询，
 * 在此组装进 VO，纯只读、无写库。KPI 概览用一条 SUM(CASE...) 多聚合 SQL 拿全。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class BizDashboardServiceImpl implements IBizDashboardService {

    private final BizDashboardMapper baseMapper;

    @Override
    public BizDashboardVo getDashboardData() {
        BizDashboardVo vo = new BizDashboardVo();
        Map<String, Object> overview = baseMapper.selectOverview();
        vo.setOverview(overview != null ? overview : new HashMap<>());   // null 兜底
        vo.setStatusDist(baseMapper.selectStatusDist());
        vo.setCategoryDist(baseMapper.selectCategoryDist());
        vo.setTrend(baseMapper.selectTrend());
        vo.setRank(baseMapper.selectRank());
        return vo;
    }

}
