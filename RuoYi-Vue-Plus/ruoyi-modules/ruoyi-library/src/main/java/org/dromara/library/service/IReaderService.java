package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.ReaderBo;
import org.dromara.library.domain.vo.ReaderVo;

import java.util.Collection;
import java.util.List;

/**
 * 读者档案Service接口
 *
 * @author library
 */
public interface IReaderService {

    /**
     * 查询读者档案
     */
    ReaderVo queryById(Long id);

    /**
     * 分页查询读者档案列表
     */
    TableDataInfo<ReaderVo> queryPageList(ReaderBo bo, PageQuery pageQuery);

    /**
     * 查询读者档案列表
     */
    List<ReaderVo> queryList(ReaderBo bo);

    /**
     * 新增读者档案
     */
    Boolean insertByBo(ReaderBo bo);

    /**
     * 修改读者档案
     */
    Boolean updateByBo(ReaderBo bo);

    /**
     * 校验并批量删除读者档案
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
