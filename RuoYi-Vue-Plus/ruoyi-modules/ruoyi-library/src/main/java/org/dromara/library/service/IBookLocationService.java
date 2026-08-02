package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.BookLocationBo;
import org.dromara.library.domain.vo.BookLocationVo;

import java.util.Collection;
import java.util.List;

/**
 * 藏地Service接口
 *
 * @author library
 */
public interface IBookLocationService {

    /**
     * 查询藏地
     */
    BookLocationVo queryById(Long id);

    /**
     * 分页查询藏地列表
     */
    TableDataInfo<BookLocationVo> queryPageList(BookLocationBo bo, PageQuery pageQuery);

    /**
     * 查询藏地列表
     */
    List<BookLocationVo> queryList(BookLocationBo bo);

    /**
     * 新增藏地
     */
    Boolean insertByBo(BookLocationBo bo);

    /**
     * 修改藏地
     */
    Boolean updateByBo(BookLocationBo bo);

    /**
     * 校验并批量删除藏地
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
