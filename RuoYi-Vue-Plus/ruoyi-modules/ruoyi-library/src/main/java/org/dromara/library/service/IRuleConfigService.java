package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.RuleConfigBo;
import org.dromara.library.domain.vo.RuleConfigVo;

import java.util.Collection;
import java.util.List;

/**
 * 规则配置Service接口
 *
 * @author library
 */
public interface IRuleConfigService {

    /**
     * 查询规则配置
     */
    RuleConfigVo queryById(Long id);

    /**
     * 分页查询规则配置列表
     */
    TableDataInfo<RuleConfigVo> queryPageList(RuleConfigBo bo, PageQuery pageQuery);

    /**
     * 查询规则配置列表
     */
    List<RuleConfigVo> queryList(RuleConfigBo bo);

    /**
     * 新增规则配置
     */
    Boolean insertByBo(RuleConfigBo bo);

    /**
     * 修改规则配置
     */
    Boolean updateByBo(RuleConfigBo bo);

    /**
     * 校验并批量删除规则配置
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
