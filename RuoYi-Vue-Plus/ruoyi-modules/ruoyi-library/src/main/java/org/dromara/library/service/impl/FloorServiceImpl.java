package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Floor;
import org.dromara.library.domain.bo.FloorBo;
import org.dromara.library.domain.vo.FloorVo;
import org.dromara.library.mapper.FloorMapper;
import org.dromara.library.service.IFloorService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 楼层Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class FloorServiceImpl implements IFloorService {

    private final FloorMapper baseMapper;

    @Override
    public FloorVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<FloorVo> queryPageList(FloorBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Floor> lqw = buildQueryWrapper(bo);
        Page<FloorVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<FloorVo> queryList(FloorBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Floor> buildQueryWrapper(FloorBo bo) {
        LambdaQueryWrapper<Floor> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getVenueId() != null, Floor::getVenueId, bo.getVenueId());
        lqw.like(StringUtils.isNotBlank(bo.getFloorName()), Floor::getFloorName, bo.getFloorName());
        lqw.eq(bo.getStatus() != null, Floor::getStatus, bo.getStatus());
        lqw.orderByAsc(Floor::getSort);
        return lqw;
    }

    @Override
    public Boolean insertByBo(FloorBo bo) {
        Floor add = MapstructUtils.convert(bo, Floor.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(FloorBo bo) {
        Floor update = MapstructUtils.convert(bo, Floor.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
