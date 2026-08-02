package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.BookBo;
import org.dromara.library.domain.vo.BookVo;

import java.util.Collection;
import java.util.List;

/**
 * 书目（种/Bib）Service接口
 *
 * @author library
 */
public interface IBookService {

    /**
     * 查询书目
     */
    BookVo queryById(Long id);

    /**
     * 分页查询书目列表
     */
    TableDataInfo<BookVo> queryPageList(BookBo bo, PageQuery pageQuery);

    /**
     * 查询书目列表
     */
    List<BookVo> queryList(BookBo bo);

    /**
     * 新增书目
     */
    Boolean insertByBo(BookBo bo);

    /**
     * 修改书目
     */
    Boolean updateByBo(BookBo bo);

    /**
     * 校验并批量删除书目
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
