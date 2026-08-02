package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Room;
import org.dromara.library.domain.bo.RoomBo;
import org.dromara.library.domain.vo.RoomVo;
import org.dromara.library.mapper.RoomMapper;
import org.dromara.library.service.IRoomService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 研讨间Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements IRoomService {

    private final RoomMapper baseMapper;

    @Override
    public RoomVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<RoomVo> queryPageList(RoomBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Room> lqw = buildQueryWrapper(bo);
        Page<RoomVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<RoomVo> queryList(RoomBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Room> buildQueryWrapper(RoomBo bo) {
        LambdaQueryWrapper<Room> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getFloorId() != null, Room::getFloorId, bo.getFloorId());
        lqw.like(StringUtils.isNotBlank(bo.getRoomName()), Room::getRoomName, bo.getRoomName());
        lqw.eq(bo.getStatus() != null, Room::getStatus, bo.getStatus());
        lqw.orderByAsc(Room::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(RoomBo bo) {
        Room add = MapstructUtils.convert(bo, Room.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(RoomBo bo) {
        Room update = MapstructUtils.convert(bo, Room.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
