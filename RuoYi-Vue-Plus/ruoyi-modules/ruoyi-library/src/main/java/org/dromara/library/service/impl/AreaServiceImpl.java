package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Area;
import org.dromara.library.domain.bo.AreaBo;
import org.dromara.library.domain.vo.AreaVo;
import org.dromara.library.mapper.AreaMapper;
import org.dromara.library.service.IAreaService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 区域Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class AreaServiceImpl implements IAreaService {

    private final AreaMapper baseMapper;

    @Override
    public AreaVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<AreaVo> queryPageList(AreaBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Area> lqw = buildQueryWrapper(bo);
        Page<AreaVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<AreaVo> queryList(AreaBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Area> buildQueryWrapper(AreaBo bo) {
        LambdaQueryWrapper<Area> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getFloorId() != null, Area::getFloorId, bo.getFloorId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), Area::getAreaName, bo.getAreaName());
        lqw.eq(bo.getAreaType() != null, Area::getAreaType, bo.getAreaType());
        lqw.eq(bo.getStatus() != null, Area::getStatus, bo.getStatus());
        lqw.orderByAsc(Area::getSort);
        return lqw;
    }

    @Override
    public Boolean insertByBo(AreaBo bo) {
        Area add = MapstructUtils.convert(bo, Area.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(AreaBo bo) {
        Area update = MapstructUtils.convert(bo, Area.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
