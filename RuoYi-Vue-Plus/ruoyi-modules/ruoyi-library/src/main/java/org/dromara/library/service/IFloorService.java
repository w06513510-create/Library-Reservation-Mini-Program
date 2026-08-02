package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.FloorBo;
import org.dromara.library.domain.vo.FloorVo;

import java.util.Collection;
import java.util.List;

/**
 * 楼层Service接口
 *
 * @author library
 */
public interface IFloorService {

    /**
     * 查询楼层
     */
    FloorVo queryById(Long id);

    /**
     * 分页查询楼层列表
     */
    TableDataInfo<FloorVo> queryPageList(FloorBo bo, PageQuery pageQuery);

    /**
     * 查询楼层列表
     */
    List<FloorVo> queryList(FloorBo bo);

    /**
     * 新增楼层
     */
    Boolean insertByBo(FloorBo bo);

    /**
     * 修改楼层
     */
    Boolean updateByBo(FloorBo bo);

    /**
     * 校验并批量删除楼层
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
