package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.RoomBo;
import org.dromara.library.domain.vo.RoomVo;

import java.util.Collection;
import java.util.List;

/**
 * 研讨间Service接口
 *
 * @author library
 */
public interface IRoomService {

    /**
     * 查询研讨间
     */
    RoomVo queryById(Long id);

    /**
     * 分页查询研讨间列表
     */
    TableDataInfo<RoomVo> queryPageList(RoomBo bo, PageQuery pageQuery);

    /**
     * 查询研讨间列表
     */
    List<RoomVo> queryList(RoomBo bo);

    /**
     * 新增研讨间
     */
    Boolean insertByBo(RoomBo bo);

    /**
     * 修改研讨间
     */
    Boolean updateByBo(RoomBo bo);

    /**
     * 校验并批量删除研讨间
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
