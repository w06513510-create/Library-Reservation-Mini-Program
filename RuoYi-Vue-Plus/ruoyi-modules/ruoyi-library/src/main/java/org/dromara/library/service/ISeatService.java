package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.SeatBo;
import org.dromara.library.domain.vo.SeatVo;

import java.util.Collection;
import java.util.List;

/**
 * 座位Service接口
 *
 * @author library
 */
public interface ISeatService {

    /**
     * 查询座位
     */
    SeatVo queryById(Long id);

    /**
     * 分页查询座位列表
     */
    TableDataInfo<SeatVo> queryPageList(SeatBo bo, PageQuery pageQuery);

    /**
     * 查询座位列表
     */
    List<SeatVo> queryList(SeatBo bo);

    /**
     * 新增座位
     */
    Boolean insertByBo(SeatBo bo);

    /**
     * 修改座位
     */
    Boolean updateByBo(SeatBo bo);

    /**
     * 校验并批量删除座位
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
