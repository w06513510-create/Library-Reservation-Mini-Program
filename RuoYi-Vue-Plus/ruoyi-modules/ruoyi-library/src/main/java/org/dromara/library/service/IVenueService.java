package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.VenueBo;
import org.dromara.library.domain.vo.VenueVo;

import java.util.Collection;
import java.util.List;

/**
 * 场馆Service接口
 *
 * @author library
 */
public interface IVenueService {

    /**
     * 查询场馆
     */
    VenueVo queryById(Long id);

    /**
     * 分页查询场馆列表
     */
    TableDataInfo<VenueVo> queryPageList(VenueBo bo, PageQuery pageQuery);

    /**
     * 查询场馆列表
     */
    List<VenueVo> queryList(VenueBo bo);

    /**
     * 新增场馆
     */
    Boolean insertByBo(VenueBo bo);

    /**
     * 修改场馆
     */
    Boolean updateByBo(VenueBo bo);

    /**
     * 校验并批量删除场馆
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
