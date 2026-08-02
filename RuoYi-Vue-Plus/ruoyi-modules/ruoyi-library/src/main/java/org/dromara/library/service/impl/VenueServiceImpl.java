package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Venue;
import org.dromara.library.domain.bo.VenueBo;
import org.dromara.library.domain.vo.VenueVo;
import org.dromara.library.mapper.VenueMapper;
import org.dromara.library.service.IVenueService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 场馆Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class VenueServiceImpl implements IVenueService {

    private final VenueMapper baseMapper;

    @Override
    public VenueVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<VenueVo> queryPageList(VenueBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Venue> lqw = buildQueryWrapper(bo);
        Page<VenueVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<VenueVo> queryList(VenueBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Venue> buildQueryWrapper(VenueBo bo) {
        LambdaQueryWrapper<Venue> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getVenueName()), Venue::getVenueName, bo.getVenueName());
        lqw.eq(bo.getStatus() != null, Venue::getStatus, bo.getStatus());
        lqw.orderByAsc(Venue::getSort);
        return lqw;
    }

    @Override
    public Boolean insertByBo(VenueBo bo) {
        Venue add = MapstructUtils.convert(bo, Venue.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(VenueBo bo) {
        Venue update = MapstructUtils.convert(bo, Venue.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
