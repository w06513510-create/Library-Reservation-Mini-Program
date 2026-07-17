package org.dromara.biz.service;

import org.dromara.biz.domain.vo.BizDashboardVo;

/**
 * 看板聚合 Service
 *
 * @author ruoyi-template
 */
public interface IBizDashboardService {

    /** 聚合看板全量数据（一次性下发） */
    BizDashboardVo getDashboardData();

}
