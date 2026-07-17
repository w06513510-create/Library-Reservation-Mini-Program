package org.dromara.biz.service;

import org.dromara.biz.domain.bo.RatingBo;
import org.dromara.biz.domain.vo.RatingVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 通用评价 Service
 *
 * @author ruoyi-template
 */
public interface IRatingService {

    /**
     * 提交评价（C端）。方向与被评价人由 {@link RatingPartyResolver} 服务端解析，不信前端。
     */
    void evaluate(RatingBo bo);

    /** 查某业务某方向的评价（前端据此判断"是否已评"，渲染"已评价"标签或评价按钮） */
    RatingVo getByBizAndRole(String bizType, Long bizId, Integer evalRole);

    /** 被评价人平均分（保留两位小数；无评价返回 0） */
    Double avgScore(Long toUserId);

    /** 评价分页列表（后台） */
    TableDataInfo<RatingVo> queryPageList(RatingBo bo, PageQuery pageQuery);

}
