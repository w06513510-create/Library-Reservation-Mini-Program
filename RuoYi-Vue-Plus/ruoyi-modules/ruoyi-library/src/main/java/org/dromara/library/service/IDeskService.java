package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.DeskBo;
import org.dromara.library.domain.vo.DeskVo;

import java.util.Collection;
import java.util.List;

/**
 * 桌子Service接口
 *
 * @author library
 */
public interface IDeskService {

    /**
     * 查询桌子
     */
    DeskVo queryById(Long id);

    /**
     * 分页查询桌子列表
     */
    TableDataInfo<DeskVo> queryPageList(DeskBo bo, PageQuery pageQuery);

    /**
     * 查询桌子列表
     */
    List<DeskVo> queryList(DeskBo bo);

    /**
     * 新增桌子
     */
    Boolean insertByBo(DeskBo bo);

    /**
     * 修改桌子
     */
    Boolean updateByBo(DeskBo bo);

    /**
     * 校验并批量删除桌子
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
