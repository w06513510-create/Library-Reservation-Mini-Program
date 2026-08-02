package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Seat;
import org.dromara.library.domain.bo.SeatBo;
import org.dromara.library.domain.vo.SeatVo;
import org.dromara.library.mapper.SeatMapper;
import org.dromara.library.service.ISeatService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 座位Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class SeatServiceImpl implements ISeatService {

    private final SeatMapper baseMapper;

    @Override
    public SeatVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SeatVo> queryPageList(SeatBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Seat> lqw = buildQueryWrapper(bo);
        Page<SeatVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SeatVo> queryList(SeatBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Seat> buildQueryWrapper(SeatBo bo) {
        LambdaQueryWrapper<Seat> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAreaId() != null, Seat::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getSeatNo()), Seat::getSeatNo, bo.getSeatNo());
        lqw.eq(bo.getStatus() != null, Seat::getStatus, bo.getStatus());
        lqw.orderByAsc(Seat::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(SeatBo bo) {
        Seat add = MapstructUtils.convert(bo, Seat.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SeatBo bo) {
        Seat update = MapstructUtils.convert(bo, Seat.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
