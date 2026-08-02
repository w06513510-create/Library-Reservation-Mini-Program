package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.AreaBo;
import org.dromara.library.domain.vo.AreaVo;

import java.util.Collection;
import java.util.List;

/**
 * 区域Service接口
 *
 * @author library
 */
public interface IAreaService {

    /**
     * 查询区域
     */
    AreaVo queryById(Long id);

    /**
     * 分页查询区域列表
     */
    TableDataInfo<AreaVo> queryPageList(AreaBo bo, PageQuery pageQuery);

    /**
     * 查询区域列表
     */
    List<AreaVo> queryList(AreaBo bo);

    /**
     * 新增区域
     */
    Boolean insertByBo(AreaBo bo);

    /**
     * 修改区域
     */
    Boolean updateByBo(AreaBo bo);

    /**
     * 校验并批量删除区域
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
