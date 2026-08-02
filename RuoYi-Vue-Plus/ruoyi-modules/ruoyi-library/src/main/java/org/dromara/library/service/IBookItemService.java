package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.BookItemBo;
import org.dromara.library.domain.vo.BookItemVo;

import java.util.Collection;
import java.util.List;

/**
 * 馆藏册（册/Item）Service接口
 *
 * @author library
 */
public interface IBookItemService {

    /**
     * 查询馆藏册
     */
    BookItemVo queryById(Long id);

    /**
     * 分页查询馆藏册列表
     */
    TableDataInfo<BookItemVo> queryPageList(BookItemBo bo, PageQuery pageQuery);

    /**
     * 查询馆藏册列表
     */
    List<BookItemVo> queryList(BookItemBo bo);

    /**
     * 新增馆藏册
     */
    Boolean insertByBo(BookItemBo bo);

    /**
     * 修改馆藏册
     */
    Boolean updateByBo(BookItemBo bo);

    /**
     * 校验并批量删除馆藏册
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
