package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.ViolationBo;
import org.dromara.library.domain.vo.ViolationVo;

import java.util.List;

/**
 * 违约记录Service（中央扣分入口）
 *
 * @author library
 */
public interface IViolationService {

    ViolationVo queryById(Long id);

    TableDataInfo<ViolationVo> queryPageList(ViolationBo bo, PageQuery pageQuery);

    List<ViolationVo> queryList(ViolationBo bo);

    /**
     * 记违约（中央入口）：建违约记录 + 信用扣分 + 黑名单阈值判定。
     * type：1座位爽约 2暂离超时 3监督未落座 4未签退 5图书逾期 6预约架超期 7遗失损坏
     * deductScore 为 null 时按类型默认扣分。source：0系统 1管理员
     */
    Long recordViolation(Long readerId, int type, Integer deductScore, String bizType, Long bizId, int source);

    /** 管理员手动登记违约 */
    Boolean addByBo(ViolationBo bo);

}
