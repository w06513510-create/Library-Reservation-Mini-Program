package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.HoldBo;
import org.dromara.library.domain.vo.HoldVo;

import java.util.List;

/**
 * 图书预约(hold)队列Service
 *
 * @author library
 */
public interface IHoldService {

    HoldVo queryById(Long id);

    TableDataInfo<HoldVo> queryPageList(HoldBo bo, PageQuery pageQuery);

    List<HoldVo> queryList(HoldBo bo);

    /** 预约：复本全借出时排队，分配队列位次 */
    Boolean createHold(HoldBo bo);

    /** 取书：到书保留→已取书，册转借出并生成借阅单 */
    Boolean pickup(Long holdId);

    /** 取消预约：排队中直接取消；到书保留则取消并把该册回架 */
    Boolean cancelHold(Long holdId);

}
