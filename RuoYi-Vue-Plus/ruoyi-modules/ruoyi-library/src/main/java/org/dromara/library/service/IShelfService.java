package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.ShelfBo;
import org.dromara.library.domain.vo.ShelfVo;

import java.util.Collection;
import java.util.List;

/**
 * 书架Service接口
 *
 * @author library
 */
public interface IShelfService {

    /**
     * 查询书架
     */
    ShelfVo queryById(Long id);

    /**
     * 分页查询书架列表
     */
    TableDataInfo<ShelfVo> queryPageList(ShelfBo bo, PageQuery pageQuery);

    /**
     * 查询书架列表
     */
    List<ShelfVo> queryList(ShelfBo bo);

    /**
     * 新增书架
     */
    Boolean insertByBo(ShelfBo bo);

    /**
     * 修改书架
     */
    Boolean updateByBo(ShelfBo bo);

    /**
     * 校验并批量删除书架
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
