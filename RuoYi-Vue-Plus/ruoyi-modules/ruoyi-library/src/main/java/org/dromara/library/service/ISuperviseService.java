package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.SuperviseBo;
import org.dromara.library.domain.vo.SuperviseVo;

import java.util.Collection;
import java.util.List;

/**
 * 占座监督Service接口
 *
 * @author library
 */
public interface ISuperviseService {

    /**
     * 查询占座监督
     */
    SuperviseVo queryById(Long id);

    /**
     * 分页查询占座监督列表
     */
    TableDataInfo<SuperviseVo> queryPageList(SuperviseBo bo, PageQuery pageQuery);

    /**
     * 查询占座监督列表
     */
    List<SuperviseVo> queryList(SuperviseBo bo);

    /**
     * 发起占座监督（举报某使用中座位无人落座）
     */
    Boolean report(SuperviseBo bo);

    /**
     * 手动解除监督（原用户已按时落座）0→1
     */
    Boolean reseat(Long id);

    /**
     * 校验并批量删除占座监督
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
