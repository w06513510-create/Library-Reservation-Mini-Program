package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.RuleConfig;
import org.dromara.library.domain.bo.RuleConfigBo;
import org.dromara.library.domain.vo.RuleConfigVo;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.RuleConfigMapper;
import org.dromara.library.service.IRuleConfigService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 规则配置Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class RuleConfigServiceImpl implements IRuleConfigService {

    private final RuleConfigMapper baseMapper;
    private final RuleConfigHelper ruleConfigHelper;

    @Override
    public RuleConfigVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<RuleConfigVo> queryPageList(RuleConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<RuleConfig> lqw = buildQueryWrapper(bo);
        Page<RuleConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<RuleConfigVo> queryList(RuleConfigBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<RuleConfig> buildQueryWrapper(RuleConfigBo bo) {
        LambdaQueryWrapper<RuleConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getRuleGroup()), RuleConfig::getRuleGroup, bo.getRuleGroup());
        lqw.like(StringUtils.isNotBlank(bo.getRuleKey()), RuleConfig::getRuleKey, bo.getRuleKey());
        lqw.orderByAsc(RuleConfig::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(RuleConfigBo bo) {
        RuleConfig add = MapstructUtils.convert(bo, RuleConfig.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            ruleConfigHelper.refresh();
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(RuleConfigBo bo) {
        RuleConfig update = MapstructUtils.convert(bo, RuleConfig.class);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            // 改了配置立即让缓存失效，下次业务读取即生效（前端「规则配置」真正生效的关键）
            ruleConfigHelper.refresh();
        }
        return flag;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag) {
            ruleConfigHelper.refresh();
        }
        return flag;
    }

}
