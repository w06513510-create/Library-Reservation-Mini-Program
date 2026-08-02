package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.PurchaseSuggestBo;
import org.dromara.library.domain.vo.PurchaseSuggestVo;

import java.util.Collection;
import java.util.List;

/**
 * 读者荐购Service接口
 *
 * @author library
 */
public interface IPurchaseSuggestService {

    /**
     * 查询读者荐购
     */
    PurchaseSuggestVo queryById(Long id);

    /**
     * 分页查询读者荐购列表
     */
    TableDataInfo<PurchaseSuggestVo> queryPageList(PurchaseSuggestBo bo, PageQuery pageQuery);

    /**
     * 查询读者荐购列表
     */
    List<PurchaseSuggestVo> queryList(PurchaseSuggestBo bo);

    /**
     * 新增读者荐购
     */
    Boolean insertByBo(PurchaseSuggestBo bo);

    /**
     * 修改读者荐购
     */
    Boolean updateByBo(PurchaseSuggestBo bo);

    /**
     * 校验并批量删除读者荐购
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 受理转采购 0→1
     */
    Boolean accept(Long id);

    /**
     * 驳回 0→2
     */
    Boolean reject(Long id, String reason);

    /**
     * 标记已采购 1→3
     */
    Boolean purchased(Long id);

}
